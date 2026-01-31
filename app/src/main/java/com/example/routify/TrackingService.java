package com.example.routify;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import org.osmdroid.util.GeoPoint;
import java.util.ArrayList;

public class TrackingService extends Service {
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private Location lastLocation;
    private final float MIN_METERS_DIFF = 5;
    private float totalDistanceMeters = 0;

    public static boolean isPaused = false;

    public static MutableLiveData<Double> distanceInKm = new MutableLiveData<>();

    // LiveData to pass position to map
    public static MutableLiveData<Location> locationLive = new MutableLiveData<>();
    public static boolean isTracking = false;
    public static ArrayList<ArrayList<GeoPoint>> routeSegments = new ArrayList<>();

    @Override
    public void onCreate() {
        super.onCreate();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) return;
                for (Location location : locationResult.getLocations()) {
                    calculateDistance(location);
                }
            }
        };
    }

    private void calculateDistance(Location newLocation) {
        if (isPaused) return;

        if (!routeSegments.isEmpty()) {
            ArrayList<GeoPoint> currentSegment = routeSegments.get(routeSegments.size() - 1);
            currentSegment.add(new GeoPoint(newLocation.getLatitude(), newLocation.getLongitude()));
        }

        // We're passing localization to activity for drawing a lines
        locationLive.postValue(newLocation);

        if (lastLocation != null) {
            float distance = lastLocation.distanceTo(newLocation);
            if (distance > MIN_METERS_DIFF) { // FILTER, VERY IMPORTANT
                totalDistanceMeters += distance;
                lastLocation = newLocation;
                distanceInKm.postValue(totalDistanceMeters / 1000.0);
            }
        } else {
            lastLocation = newLocation;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            if ("START_TRACKING".equals(action)) {
                routeSegments.clear();
                routeSegments.add(new ArrayList<>());
                isPaused = false;
                totalDistanceMeters = 0;
                lastLocation = null;
                distanceInKm.postValue(0.0);

                startForegroundService();
                startLocationUpdates();
            } else if ("PAUSE_TRACKING".equals(action)) {
                isPaused = true;
                updateNotification("Trasa wstrzymana");
            }
            // Resume handling
            else if ("RESUME_TRACKING".equals(action)) {
                isPaused = false;
                lastLocation = null; // IMPORTANT => Reseting last location is important, because otherwise it will draw a line for a pause moment
                routeSegments.add(new ArrayList<>());

                updateNotification("Naliczanie kilometrów...");
            }
            else if ("STOP_TRACKING".equals(action)) {
                stopLocationUpdates();
                stopForeground(true);
                stopSelf();
            }
        }
        return START_NOT_STICKY;
    }

    private void updateNotification(String contentText) {
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        Notification notification = new NotificationCompat.Builder(this, "location_channel")
                .setContentTitle("Routify")
                .setContentText(contentText)
                .setSmallIcon(android.R.drawable.ic_menu_mylocation)
                .setOnlyAlertOnce(true) // Dont vibrate when text updates
                .build();
        notificationManager.notify(1, notification);
    }

    @SuppressLint("ForegroundServiceType")
    private void startForegroundService() {
        String channelId = "location_channel";
        NotificationManager notificationManager = getSystemService(NotificationManager.class);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Śledzenie Trasy",
                    NotificationManager.IMPORTANCE_LOW
            );
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }

        Notification notification = new NotificationCompat.Builder(this, channelId)
                .setContentTitle("Śledzenie trasy")
                .setContentText("Liczenie kilometrów w tle...")
                .setSmallIcon(android.R.drawable.ic_menu_mylocation)
                .build();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10+ (especially 14) requires to pass a service type
            try {
                startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION);
            } catch (Exception e) {
                startForeground(1, notification);
            }
        } else {
            startForeground(1, notification);
        }
    }

    private void startLocationUpdates() {
        isTracking = true;
        isPaused = false;

        LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 500)
                .setMinUpdateDistanceMeters(MIN_METERS_DIFF)
                .build();

        try {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    private void stopLocationUpdates() {
        isTracking = false;
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}