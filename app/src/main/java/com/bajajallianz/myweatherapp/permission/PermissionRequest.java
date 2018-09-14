package com.bajajallianz.myweatherapp.permission;


import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.bajajallianz.myweatherapp.R;
import com.canelmas.let.DeniedPermission;
import com.canelmas.let.Let;
import com.canelmas.let.RuntimePermissionRequest;

import java.util.List;

public class PermissionRequest {

    public static final int RECORD_PERMISSION_REQUEST_CODE = 1;
    public static final int EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE = 2;
    public static final int CAMERA_PERMISSION_REQUEST_CODE = 3;
    Activity activity;

    public PermissionRequest(Activity activity) {
        this.activity = activity;
    }

    private PermissionRequest() {
    }


    private static PermissionRequest instance = null;

    public static PermissionRequest getInstance(Activity activity) {
        if (instance == null)
            return new PermissionRequest(activity);
        else
            return instance;
    }


    public boolean checkPermissionForRecord() {
        int result = ContextCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    public boolean checkPermissionForExternalStorage() {
        int result = ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    public boolean checkPermissionForCamera() {
        int result = ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    public void requestPermissionForRecord() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.RECORD_AUDIO)) {
            Toast.makeText(activity, "Microphone permission needed for recording. Please allow in App Settings for additional functionality.", Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.RECORD_AUDIO}, RECORD_PERMISSION_REQUEST_CODE);
        }
    }

    public void requestPermissionForExternalStorage() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Toast.makeText(activity, "External Storage permission needed. Please allow in App Settings for additional functionality.", Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE);
        }
    }

    public void requestPermissionForCamera() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.CAMERA)) {
            Toast.makeText(activity, "Camera permission needed. Please allow in App Settings for additional functionality.", Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
        }
    }


    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        try {
            Let.handle(activity, requestCode, permissions, grantResults);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onShowPermissionRationale(List<String> permissions, final RuntimePermissionRequest request) {
        try {
            //  tell user why you need these permissions
            final StringBuilder sb = new StringBuilder();

            for (String permission : permissions) {
                sb.append(getRationale(permission));
                sb.append("\n");
            }

            new AlertDialog.Builder(activity).setTitle("Permission Required!")
                    .setMessage(sb.toString())
                    .setCancelable(true)
                    .setNegativeButton("No Thanks", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .setPositiveButton("Allow", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            request.retry();
                        }
                    })
                    .show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onPermissionDenied(List<DeniedPermission> results) {
        try {


            /**
             * Let's just do nothing if permission is denied without
             * 'Never ask Again' checked.
             *
             * If it's the case show more informative message and prompt user
             * to the app settings screen.
             */

            final StringBuilder sb = new StringBuilder();

            for (DeniedPermission result : results) {

                if (result.isNeverAskAgainChecked()) {
                    sb.append("To use Insurance Wallet functionality allow below permissions for " + result.getPermission());
                    sb.append("\n");
                }

            }

            if (sb.length() != 0) {

                new AlertDialog.Builder(activity).setTitle("Go Settings and Grant Permission")
                        .setMessage(sb.toString())
                        .setCancelable(true)
                        .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
                                intent.setData(uri);
                                activity.startActivityForResult(intent, 1);

                                dialog.dismiss();
                            }
                        }).show();

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private String getRationale(String permission) {
        try {

            if (permission.equals(Manifest.permission.ACCESS_NETWORK_STATE)) {
                return activity.getString(R.string.rationale_network_state);
            }
//            else {
//                return activity.getString(R.string.rationale_general);
//            }
            return "";
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

}