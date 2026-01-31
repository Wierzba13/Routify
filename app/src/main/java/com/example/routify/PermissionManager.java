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
        void onPermissionsGranted();
        void onPermissionsDenied();
    }

    public PermissionManager(Activity activity, PermissionCallback callback) {
        this.activity = activity;
        this.callback = callback;
    }

    public boolean hasAllPermissions() {
        boolean gps = ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        boolean notif = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notif = ContextCompat.checkSelfPermission(activity, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED;
        }
        return gps && notif;
    }

    // if we have GPS we can display a map
    public boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    public void requestPermissionsWithRationale() {
        if (hasAllPermissions()) {
            callback.onPermissionsGranted();
            return;
        }

        StringBuilder message = new StringBuilder("Aplikacja do prawidłowego działania wymaga uprawnień do:\n");
        if (!hasLocationPermission()) message.append("- Lokalizacji \n");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                message.append("- Powiadomień \n");
            }
        }

        new AlertDialog.Builder(activity)
                .setTitle("Wymagane uprawnienia")
                .setMessage(message.toString())
                .setCancelable(false)
                .setPositiveButton("Przyznaj uprawnienia", (dialog, which) -> {
                    askSystemForPermissions();
                })
                .setNegativeButton("Nie teraz", (dialog, which) -> {
                    callback.onPermissionsDenied();
                })
                .show();
    }

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

    public void handleRequestPermissionsResult(int requestCode, int[] grantResults, String[] permissions) {
        if (requestCode != REQUEST_CODE) return;

        if (hasAllPermissions()) {
            callback.onPermissionsGranted();
            return;
        }

        /*
            If we're here we have some missing permissions,
            so we have to make sure if user denided too many times or android blocked us
         */

        boolean permanentlyDenied = false;

        for (int i = 0; i < permissions.length; i++) {
            String perm = permissions[i];

            // Checking only denided permissions
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {

                /* shouldShowRequestPermissionRationale returns:

                    true -> when user denided one time
                    false -> when user dont want us to ask again or system blocked us from asking again
                 */

                if (!ActivityCompat.shouldShowRequestPermissionRationale(activity, perm)) {
                    permanentlyDenied = true;
                    break;
                }
            }
        }

        if (permanentlyDenied) {
            // system blocked us
            showSettingsDialog();
        } else {
            callback.onPermissionsDenied();
        }
    }

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