package com.thebluecode.trxautophone.utils;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.util.Log;
import android.view.accessibility.AccessibilityManager;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.thebluecode.trxautophone.service.AutoClickAccessibilityService;

import java.util.List;

/**
 * Enhanced permission utility class with improved checks and request handling
 */
public class PermissionUtils {
    private static final String TAG = "PermissionUtils";

    private PermissionUtils() {
        // Private constructor to prevent instantiation
    }

    /**
     * Check if accessibility service is enabled
     */
    public static boolean isAccessibilityServiceEnabled(@NonNull Context context) {
        try {
            AccessibilityManager manager = (AccessibilityManager) 
                context.getSystemService(Context.ACCESSIBILITY_SERVICE);
            if (manager != null) {
                List<AccessibilityServiceInfo> list = manager
                    .getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK);
                
                for (AccessibilityServiceInfo info : list) {
                    String id = info.getId();
                    if (id != null && id.contains(context.getPackageName())) {
                        Log.d(TAG, "Accessibility service is enabled");
                        return true;
                    }
                }
            }
            Log.d(TAG, "Accessibility service is not enabled");
            return false;
        } catch (Exception e) {
            Log.e(TAG, "Error checking accessibility service status", e);
            return false;
        }
    }

    /**
     * Check if overlay permission is granted
     */
    public static boolean canDrawOverlays(@NonNull Context context) {
        try {
            boolean canDraw = Settings.canDrawOverlays(context);
            Log.d(TAG, "Can draw overlays: " + canDraw);
            return canDraw;
        } catch (Exception e) {
            Log.e(TAG, "Error checking overlay permission", e);
            return false;
        }
    }

    /**
     * Request overlay permission
     */
    public static void requestOverlayPermission(@NonNull Activity activity, int requestCode) {
        try {
            Intent intent = new Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + activity.getPackageName())
            );
            activity.startActivityForResult(intent, requestCode);
            Log.d(TAG, "Requested overlay permission");
        } catch (Exception e) {
            Log.e(TAG, "Error requesting overlay permission", e);
        }
    }

    /**
     * Open accessibility settings
     */
    public static void openAccessibilitySettings(@NonNull Activity activity, int requestCode) {
        try {
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            activity.startActivityForResult(intent, requestCode);
            Log.d(TAG, "Opened accessibility settings");
        } catch (Exception e) {
            Log.e(TAG, "Error opening accessibility settings", e);
        }
    }

    /**
     * Check if all required permissions are granted
     */
    public static boolean hasAllRequiredPermissions(@NonNull Context context) {
        return isAccessibilityServiceEnabled(context) && canDrawOverlays(context);
    }

    /**
     * Check if a specific permission is granted
     */
    public static boolean hasPermission(@NonNull Context context, @NonNull String permission) {
        try {
            return ContextCompat.checkSelfPermission(context, permission) == 
                android.content.pm.PackageManager.PERMISSION_GRANTED;
        } catch (Exception e) {
            Log.e(TAG, "Error checking permission: " + permission, e);
            return false;
        }
    }

    /**
     * Request a specific permission
     */
    public static void requestPermission(@NonNull Activity activity, 
                                       @NonNull String permission, 
                                       int requestCode) {
        try {
            ActivityCompat.requestPermissions(activity, new String[]{permission}, requestCode);
            Log.d(TAG, "Requested permission: " + permission);
        } catch (Exception e) {
            Log.e(TAG, "Error requesting permission: " + permission, e);
        }
    }

    /**
     * Check if should show permission rationale
     */
    public static boolean shouldShowRequestPermissionRationale(@NonNull Activity activity, 
                                                             @NonNull String permission) {
        try {
            return ActivityCompat.shouldShowRequestPermissionRationale(activity, permission);
        } catch (Exception e) {
            Log.e(TAG, "Error checking permission rationale: " + permission, e);
            return false;
        }
    }

    /**
     * Open app settings
     */
    public static void openAppSettings(@NonNull Activity activity, int requestCode) {
        try {
            Intent intent = new Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.parse("package:" + activity.getPackageName())
            );
            activity.startActivityForResult(intent, requestCode);
            Log.d(TAG, "Opened app settings");
        } catch (Exception e) {
            Log.e(TAG, "Error opening app settings", e);
        }
    }

    /**
     * Check if accessibility service is running
     */
    public static boolean isAccessibilityServiceRunning() {
        return AutoClickAccessibilityService.getInstance() != null;
    }

    /**
     * Get accessibility service status message
     */
    public static String getAccessibilityServiceStatus(@NonNull Context context) {
        if (isAccessibilityServiceEnabled(context)) {
            if (isAccessibilityServiceRunning()) {
                return "Accessibility service is running";
            } else {
                return "Accessibility service is enabled but not running";
            }
        } else {
            return "Accessibility service is not enabled";
        }
    }

    /**
     * Check if device is rooted
     */
    public static boolean isDeviceRooted() {
        try {
            String buildTags = android.os.Build.TAGS;
            if (buildTags != null && buildTags.contains("test-keys")) {
                return true;
            }

            // Check for common root-only files
            String[] rootFiles = {
                "/system/app/Superuser.apk",
                "/system/xbin/su",
                "/system/bin/su",
                "/sbin/su",
                "/system/su",
                "/system/bin/.ext/.su"
            };

            for (String file : rootFiles) {
                if (new java.io.File(file).exists()) {
                    return true;
                }
            }

            return false;
        } catch (Exception e) {
            Log.e(TAG, "Error checking root status", e);
            return false;
        }
    }

    /**
     * Check if USB debugging is enabled
     */
    public static boolean isUsbDebuggingEnabled(@NonNull Context context) {
        try {
            return Settings.Global.getInt(context.getContentResolver(), 
                Settings.Global.ADB_ENABLED, 0) == 1;
        } catch (Exception e) {
            Log.e(TAG, "Error checking USB debugging status", e);
            return false;
        }
    }

    /**
     * Check if developer options are enabled
     */
    public static boolean isDeveloperOptionsEnabled(@NonNull Context context) {
        try {
            return Settings.Global.getInt(context.getContentResolver(), 
                Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0) == 1;
        } catch (Exception e) {
            Log.e(TAG, "Error checking developer options status", e);
            return false;
        }
    }

    /**
     * Get security recommendations
     */
    public static List<String> getSecurityRecommendations(@NonNull Context context) {
        List<String> recommendations = new ArrayList<>();

        if (isDeviceRooted()) {
            recommendations.add("Device is rooted, which may pose security risks");
        }

        if (isUsbDebuggingEnabled(context)) {
            recommendations.add("USB debugging is enabled, consider disabling when not needed");
        }

        if (isDeveloperOptionsEnabled(context)) {
            recommendations.add("Developer options are enabled, consider disabling when not needed");
        }

        if (!canDrawOverlays(context)) {
            recommendations.add("Overlay permission is required for proper functionality");
        }

        if (!isAccessibilityServiceEnabled(context)) {
            recommendations.add("Accessibility service is required for automation");
        }

        return recommendations;
    }
}
