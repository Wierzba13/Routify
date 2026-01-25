package com.example.routify;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.util.ArrayList;
import java.util.List;

public class PermissionManager {

    private final Activity activity;
    private final PermissionCallback callback;
    public static final int REQUEST_CODE = 100;

    public interface PermissionCallback {
        void onPermissionsGranted(); // Wszystko przyznane
        void onPermissionsDenied();  // Użytkownik odmówił
    }

    public PermissionManager(Activity activity, PermissionCallback callback) {
        this.activity = activity;
        this.callback = callback;
    }

    // --- 1. SPRAWDZANIE STANU ---

    // Czy mamy komplet uprawnień?
    public boolean hasAllPermissions() {
        boolean gps = ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        boolean notif = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notif = ContextCompat.checkSelfPermission(activity, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED;
        }
        return gps && notif;
    }

    // Czy mamy chociaż GPS (żeby pokazać mapę)?
    public boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    // --- 2. LOGIKA PROŚBY (To "ładne okienko") ---

    public void requestPermissionsWithRationale() {
        if (hasAllPermissions()) {
            callback.onPermissionsGranted();
            return;
        }

        // Budujemy treść komunikatu
        StringBuilder message = new StringBuilder("Aplikacja potrzebuje dostępu do:\n");
        if (!hasLocationPermission()) message.append("- Lokalizacji (aby rysować trasę)\n");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                message.append("- Powiadomień (aby działać w tle)\n");
            }
        }
        message.append("\nBez tych uprawnień trening nie będzie działał poprawnie.");

        // Wyświetlamy ŁADNE OKIENKO (AlertDialog)
        new AlertDialog.Builder(activity)
                .setTitle("Wymagane uprawnienia")
                .setMessage(message.toString())
                .setCancelable(false) // Użytkownik musi kliknąć przycisk
                .setPositiveButton("Przyznaj uprawnienia", (dialog, which) -> {
                    // Dopiero teraz wywołujemy systemowe okienka
                    askSystemForPermissions();
                })
                .setNegativeButton("Nie teraz", (dialog, which) -> {
                    callback.onPermissionsDenied();
                })
                .show();
    }

    // --- 3. WŁAŚCIWE ZAPYTANIE SYSTEMOWE ---

    protected void askSystemForPermissions() {
        List<String> permissionsToRequest = new ArrayList<>();

        if (!hasLocationPermission()) {
            permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION);
            permissionsToRequest.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS);
            }
        }

        ActivityCompat.requestPermissions(activity, permissionsToRequest.toArray(new String[0]), REQUEST_CODE);
    }

    // --- 4. OBSŁUGA ODPOWIEDZI ---

    public void handleRequestPermissionsResult(int requestCode, int[] grantResults, String[] permissions) {
        if (requestCode != REQUEST_CODE) return;

        // Ponowna weryfikacja - czy po operacji mamy już wszystko?
        if (hasAllPermissions()) {
            callback.onPermissionsGranted();
            return;
        }

        // Jeśli tu jesteśmy, to znaczy, że czegoś brakuje (ODMOWA).
        // Musimy sprawdzić, czy odmowa jest "Zwykła" czy "Trwała".

        boolean permanentlyDenied = false;

        for (int i = 0; i < permissions.length; i++) {
            String perm = permissions[i];

            // Sprawdzamy tylko te uprawnienia, które zostały odrzucone
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {

                // shouldShowRequestPermissionRationale zwraca:
                // TRUE -> Jeśli użytkownik odmówił raz (można pytać ponownie)
                // FALSE -> Jeśli użytkownik zaznaczył "Nie pytaj ponownie" LUB system zablokował (Permanent)

                if (!ActivityCompat.shouldShowRequestPermissionRationale(activity, perm)) {
                    permanentlyDenied = true;
                    // Wystarczy, że jedno uprawnienie jest zablokowane trwale, żebyśmy musieli iść do ustawień
                    break;
                }
            }
        }

        if (permanentlyDenied) {
            // System zablokował prośbę. Wyświetlamy okno "Ostatniej Szansy"
            showSettingsDialog();
        } else {
            // Zwykła odmowa (użytkownik kliknął Anuluj/Odmów, ale nie zablokował trwale)
            // Możemy tu albo nic nie robić, albo wyświetlić komunikat.
            // Zgodnie z Twoim życzeniem - wyświetlamy komunikat i dajemy szansę poprawy.
            callback.onPermissionsDenied();
        }
    }

    // Okno ostateczne - gdy Android zablokuje przycisk "Przyznaj"
    private void showSettingsDialog() {
        new AlertDialog.Builder(activity)
                .setTitle("Uprawnienia zablokowane")
                .setMessage("System Android zablokował prośbę o uprawnienia (zbyt wiele odmów). \n\nMusisz włączyć je ręcznie w ustawieniach, klikając przycisk poniżej.")
                .setCancelable(false)
                .setPositiveButton("Otwórz Ustawienia", (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
                    intent.setData(uri);
                    activity.startActivity(intent);
                })
                .setNegativeButton("Anuluj", (dialog, which) -> callback.onPermissionsDenied())
                .show();
    }
}