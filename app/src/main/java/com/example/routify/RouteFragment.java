package com.example.routify;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RouteFragment extends Fragment {

    // UI Elements
    private MaterialButton btnStart;
    private FloatingActionButton btnPause, btnResume;
    private MaterialButton btnStop;
    private LinearLayout layoutPaused;
    private MapView map;
    private TextView tvDistance;

    // Logic
    private enum UIState { IDLE, TRACKING, PAUSED }
    private Marker currentMarker;
    private List<Polyline> allPolylines = new ArrayList<>();
    private Polyline currentPolyline;
    private FusedLocationProviderClient fusedLocationClient;
    private PermissionManager permissionManager;

    // Usunięto: Context context = requireContext(); -> To powodowało błąd przy uruchamianiu!

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // 1. Najpierw inflatujemy widok
        View view = inflater.inflate(R.layout.route_fragment, container, false);
        Context context = requireContext(); // Pobieramy kontekst bezpiecznie wewnątrz metody

        // OSM configuration
        Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(context));
        Configuration.getInstance().setUserAgentValue(context.getPackageName()); // Poprawka: context.getPackageName()

        // Maps tiles cache
        File osmdroidBasePath = new File(context.getCacheDir(), "osmdroid"); // Poprawka: context.getCacheDir()
        osmdroidBasePath.mkdirs();
        Configuration.getInstance().setOsmdroidBasePath(osmdroidBasePath);

        File osmdroidTileCache = new File(osmdroidBasePath, "tiles");
        osmdroidTileCache.mkdirs();
        Configuration.getInstance().setOsmdroidTileCache(osmdroidTileCache);

        // Permissions check (opcjonalne tutaj, bo PermissionManager to obsłuży, ale zostawiam logikę)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                // Uwaga: W fragmencie lepiej używać requestPermissions zamiast ActivityCompat.requestPermissions, aby callback wrócił do fragmentu
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);

        // Przekazujemy 'view' do metody inicjalizującej
        initializeViews(view);
        setupMap();
        setupButtons();

        // --- OBSERWATORZY SERWISU ---

        // 1. Obserwuj dystans
        // Używamy getViewLifecycleOwner() zamiast 'this' dla bezpieczeństwa w Fragmentach
        TrackingService.distanceInKm.observe(getViewLifecycleOwner(), new Observer<Double>() {
            @Override
            public void onChanged(Double km) {
                if (tvDistance != null) {
                    tvDistance.setText(String.format(Locale.US, "%.2f km", km));
                }
            }
        });

        // 2. Obserwuj lokalizację
        TrackingService.locationLive.observe(getViewLifecycleOwner(), new Observer<Location>() {
            @Override
            public void onChanged(Location location) {
                if (location != null) {
                    updateMap(location);
                }
            }
        });

        // Inicjalizacja Permission Managera
        permissionManager = new PermissionManager(requireActivity(), new PermissionManager.PermissionCallback() {
            @Override
            public void onPermissionsGranted() {
                if (map != null) {
                    map.onResume();
                    map.invalidate();
                }
                centerMapOnUser();
            }

            @Override
            public void onPermissionsDenied() {
                Toast.makeText(requireContext(), "Aplikacja ma ograniczoną funkcjonalność bez uprawnień.", Toast.LENGTH_LONG).show();
            }
        });

        if (permissionManager.hasLocationPermission()) {
            centerMapOnUser();
        }

        if (!permissionManager.hasAllPermissions()) {
            permissionManager.requestPermissionsWithRationale();
        }

        return view;
    }

    // Metoda przyjmuje teraz View, aby móc szukać elementów
    private void initializeViews(View view) {
        map = view.findViewById(R.id.map);
        tvDistance = view.findViewById(R.id.tvDistance);
        btnStart = view.findViewById(R.id.btnStart);
        // Usunięto duplikat btnStart
        btnStop = view.findViewById(R.id.btnStop);
        btnPause = view.findViewById(R.id.btnPause);
        btnResume = view.findViewById(R.id.btnResume);
        layoutPaused = view.findViewById(R.id.layoutPaused);
    }

    private void setupMap() {
        if (map == null) return;

        map.setTileSource(org.osmdroid.tileprovider.tilesource.TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);
        map.getController().setZoom(18.0);
        map.setBuiltInZoomControls(false);

        currentMarker = new Marker(map);
        currentMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
        currentMarker.setInfoWindow(null);

        // Poprawka: użycie ContextCompat zamiast getResources().getDrawable()
        currentMarker.setIcon(ContextCompat.getDrawable(requireContext(), R.drawable.baseline_location_on_24));

        map.getOverlays().add(currentMarker);
    }

    private void setupButtons() {
        btnStart.setOnClickListener(v -> {
            if (permissionManager.hasAllPermissions()) {
                sendCommandToService("START_TRACKING");
                startNewSegment();
                map.invalidate();
                updateUI(UIState.TRACKING);
            } else {
                permissionManager.requestPermissionsWithRationale();
            }
        });

        btnPause.setOnClickListener(v -> {
            sendCommandToService("PAUSE_TRACKING");
            updateUI(UIState.PAUSED);
        });

        btnResume.setOnClickListener(v -> {
            sendCommandToService("RESUME_TRACKING");
            startNewSegment();
            updateUI(UIState.TRACKING);
        });

        btnStop.setOnClickListener(v -> {
            sendCommandToService("STOP_TRACKING");
            updateUI(UIState.IDLE);
            tvDistance.setText("0.00 km");

            for (Polyline line : allPolylines) {
                map.getOverlays().remove(line);
            }
            allPolylines.clear();
            currentPolyline = null;
            map.invalidate();
        });

        if (TrackingService.isTracking) {
            if (TrackingService.isPaused) {
                updateUI(UIState.PAUSED);
            } else {
                updateUI(UIState.TRACKING);
            }
        }
    }

    private void sendCommandToService(String action) {
        Context context = requireContext(); // Bezpieczne pobranie kontekstu
        Intent intent = new Intent(context, TrackingService.class);
        intent.setAction(action);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }
    }

    private void updateUI(UIState state) {
        if (btnStart == null) return; // Zabezpieczenie

        btnStart.setVisibility(View.GONE);
        btnPause.setVisibility(View.GONE);
        layoutPaused.setVisibility(View.GONE);

        switch (state) {
            case IDLE:
                btnStart.setVisibility(View.VISIBLE);
                break;
            case TRACKING:
                btnPause.setVisibility(View.VISIBLE);
                break;
            case PAUSED:
                layoutPaused.setVisibility(View.VISIBLE);
                break;
        }
    }

    @SuppressLint("MissingPermission")
    private void centerMapOnUser() {
        if (!permissionManager.hasLocationPermission()) return;

        // Poprawka: usunięto rzutowanie (Executor) this, które powodowało błąd
        // Używamy wersji metody addOnSuccessListener, która nie wymaga Executora (działa na głównym wątku)
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        updateMapWithLocation(location);
                    } else {
                        forceCurrentLocation();
                    }
                });
    }

    @SuppressLint("MissingPermission")
    private void forceCurrentLocation() {
        if (!permissionManager.hasLocationPermission()) return;

        Toast.makeText(requireContext(), "Ustalanie pozycji GPS...", Toast.LENGTH_SHORT).show();

        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, new CancellationTokenSource().getToken())
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        updateMapWithLocation(location);
                    } else {
                        Toast.makeText(requireContext(), "Nie udało się ustalić lokalizacji. Wyjdź na zewnątrz.", Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void updateMap(Location location) {
        if (map == null) return;

        GeoPoint newPoint = new GeoPoint(location.getLatitude(), location.getLongitude());

        if (currentPolyline == null) {
            startNewSegment();
        }

        currentPolyline.addPoint(newPoint);

        if (currentMarker != null) {
            currentMarker.setPosition(newPoint);
        }

        map.getController().animateTo(newPoint);
        map.invalidate();
    }

    private void updateMapWithLocation(Location location) {
        if (map == null) return;

        GeoPoint userLocation = new GeoPoint(location.getLatitude(), location.getLongitude());
        map.getController().animateTo(userLocation);
        if (currentMarker != null) {
            currentMarker.setPosition(userLocation);
        }
        map.invalidate();
    }

    private void startNewSegment() {
        if (map == null) return;

        currentPolyline = new Polyline();
        currentPolyline.setColor(Color.RED);
        currentPolyline.setWidth(15f);

        map.getOverlays().add(currentPolyline);
        allPolylines.add(currentPolyline);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (permissionManager != null) {
            permissionManager.handleRequestPermissionsResult(requestCode, grantResults, permissions);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (map != null) {
            map.onResume();
        }
        if (permissionManager != null && permissionManager.hasLocationPermission()) {
            centerMapOnUser();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (map != null) {
            map.onPause();
        }
    }
}