package com.thebluecode.trxautophone.utils;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.app.Instrumentation;
import android.content.ClipData;
import android.graphics.Path;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.accessibility.AccessibilityNodeInfo;

import android.content.ClipboardManager;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import java.util.List;

public class AccessibilityUtils {

    private static final String TAG = "AccessibilityUtils";
    private static AccessibilityService service;

    public static void setService(AccessibilityService service) {
        AccessibilityUtils.service = service;
    }

    public static void findAndClickText(String text) {
        if (service == null) {
            Log.e(TAG, "AccessibilityService chưa được thiết lập");
            return;
        }

        AccessibilityNodeInfo root = service.getRootInActiveWindow();
        if (root == null) return;

        List<AccessibilityNodeInfo> nodes = root.findAccessibilityNodeInfosByText(text);
        for (AccessibilityNodeInfo node : nodes) {
            if (node != null && node.isClickable()) {
                node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                break;
            }
        }
    }

    public static void inputText(Context context, String text) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("text", text);
        clipboard.setPrimaryClip(clip);

        final Instrumentation inst = new Instrumentation();
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(() -> {
            try {
                inst.sendKeyDownUpSync(KeyEvent.KEYCODE_PASTE);
            } catch (Exception e) {
                Toast.makeText(context, "Không thể dán nội dung", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static void gestureClick(int x, int y) {
        if (service == null) {
            Log.e(TAG, "AccessibilityService chưa được thiết lập");
            return;
        }

        Path path = new Path();
        path.moveTo(x, y);
        path.lineTo(x, y);

        GestureDescription.Builder builder = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            builder = new GestureDescription.Builder();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            builder.addStroke(new GestureDescription.StrokeDescription(path, 0, 100));
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            service.dispatchGesture(builder.build(), new AccessibilityService.GestureResultCallback() {}, null);
        }
    }

    public static void swipeUp() {
        if (service == null) {
            Log.e(TAG, "AccessibilityService chưa được thiết lập");
            return;
        }

        DisplayMetrics metrics = service.getResources().getDisplayMetrics();
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;

        Path path = new Path();
        path.moveTo(width / 2, height - 100);
        path.lineTo(width / 2, 100);

        GestureDescription.Builder builder = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            builder = new GestureDescription.Builder();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            builder.addStroke(new GestureDescription.StrokeDescription(path, 0, 300));
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            service.dispatchGesture(builder.build(), new AccessibilityService.GestureResultCallback() {}, null);
        }
    }

    public static void swipeDown() {
        if (service == null) {
            Log.e(TAG, "AccessibilityService chưa được thiết lập");
            return;
        }

        DisplayMetrics metrics = service.getResources().getDisplayMetrics();
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;

        Path path = new Path();
        path.moveTo(width / 2, 100);
        path.lineTo(width / 2, height - 100);

        GestureDescription.Builder builder = new GestureDescription.Builder();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            builder.addStroke(new GestureDescription.StrokeDescription(path, 0, 300));
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            service.dispatchGesture(builder.build(), new AccessibilityService.GestureResultCallback() {}, null);
        }
    }
}