package com.thebluecode.trxautophone.utils;

import android.Manifest;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.view.accessibility.AccessibilityManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.thebluecode.trxautophone.services.AutoClickAccessibilityService;
import java.util.ArrayList;
import java.util.List;

public class PermissionUtils {
    private static final int PERMISSION_REQUEST_CODE = 1001;
    private static final int OVERLAY_PERMISSION_REQUEST_CODE = 1002;

    public static boolean checkAndRequestPermissions(Activity activity) {
        List<String> permissions = new ArrayList<>();

        // Check storage permissions for Android 10 and below
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(activity, 
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
            if (ContextCompat.checkSelfPermission(activity, 
                    Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        }

        // Add any other required permissions here

        if (!permissions.isEmpty()) {
            ActivityCompat.requestPermissions(activity,
                    permissions.toArray(new String[0]),
                    PERMISSION_REQUEST_CODE);
            return false;
        }

        return true;
    }

    public static boolean isAccessibilityServiceEnabled(Context context) {
        AccessibilityManager am = (AccessibilityManager) 
            context.getSystemService(Context.ACCESSIBILITY_SERVICE);
        if (am == null) return false;

        List<AccessibilityServiceInfo> enabledServices = 
            am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK);

        String serviceId = context.getPackageName() + "/" + 
            AutoClickAccessibilityService.class.getName();

        for (AccessibilityServiceInfo service : enabledServices) {
            if (service.getId().equals(serviceId)) {
                return true;
            }
        }

        return false;
    }

    public static void openAccessibilitySettings(Context context) {
        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static boolean checkOverlayPermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return Settings.canDrawOverlays(context);
        }
        return true;
    }

    public static void requestOverlayPermission(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + activity.getPackageName()));
            activity.startActivityForResult(intent, OVERLAY_PERMISSION_REQUEST_CODE);
        }
    }

    public static boolean handlePermissionResult(Activity activity, int requestCode, 
            String[] permissions, int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            return allGranted;
        }
        return false;
    }

    public static boolean handleOverlayPermissionResult(Context context, int requestCode) {
        if (requestCode == OVERLAY_PERMISSION_REQUEST_CODE) {
            return checkOverlayPermission(context);
        }
        return false;
    }

    public static boolean shouldShowPermissionRationale(Activity activity, String permission) {
        return ActivityCompat.shouldShowRequestPermissionRationale(activity, permission);
    }

    public static void openAppSettings(Context context) {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", context.getPackageName(), null);
        intent.setData(uri);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static int getPermissionRequestCode() {
        return PERMISSION_REQUEST_CODE;
    }

    public static int getOverlayPermissionRequestCode() {
        return OVERLAY_PERMISSION_REQUEST_CODE;
    }
}
