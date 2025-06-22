package com.thebluecode.trxautophone.services;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Path;
import android.graphics.Point;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

import com.thebluecode.trxautophone.utils.AccessibilityUtils;
import com.thebluecode.trxautophone.utils.Constants;

import java.util.List;

public class AutoClickAccessibilityService extends AccessibilityService {
    private static final String TAG = "AutoClickService";
    private static AutoClickAccessibilityService instance;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // Handle accessibility events if needed
    }

    @Override
    public void onInterrupt() {
        Log.d(TAG, "Service interrupted");
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        instance = this;
        Log.d(TAG, "Service connected");
        showToast("AutoClick Service Connected");
    }

    public static AutoClickAccessibilityService getInstance() {
        return instance;
    }

    // System Key Actions
    public boolean performSystemAction(int action) {
        try {
            return AccessibilityUtils.performGlobalAction(this, action);
        } catch (Exception e) {
            Log.e(TAG, "Error performing system action: " + e.getMessage());
            return false;
        }
    }

    // Tap Action
    public boolean performTap(Point point) {
        if (point == null) {
            Log.e(TAG, Constants.Errors.INVALID_COORDINATES);
            return false;
        }

        Path clickPath = new Path();
        clickPath.moveTo(point.x, point.y);

        GestureDescription.Builder builder = new GestureDescription.Builder();
        builder.addStroke(new GestureDescription.StrokeDescription(clickPath, 0, 50));

        return dispatchGesture(builder.build(), new GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gestureDescription) {
                Log.d(TAG, "Tap completed at: " + point.x + "," + point.y);
            }

            @Override
            public void onCancelled(GestureDescription gestureDescription) {
                Log.w(TAG, "Tap cancelled at: " + point.x + "," + point.y);
            }
        }, null);
    }

    // Long Press Action
    public boolean performLongPress(Point point, long duration) {
        if (point == null) {
            Log.e(TAG, Constants.Errors.INVALID_COORDINATES);
            return false;
        }

        duration = Math.max(duration, Constants.Screen.DEFAULT_LONG_PRESS_TIMEOUT);
        
        Path clickPath = new Path();
        clickPath.moveTo(point.x, point.y);

        GestureDescription.Builder builder = new GestureDescription.Builder();
        builder.addStroke(new GestureDescription.StrokeDescription(clickPath, 0, duration));

        return dispatchGesture(builder.build(), new GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gestureDescription) {
                Log.d(TAG, "Long press completed at: " + point.x + "," + point.y);
            }
        }, null);
    }

    // Swipe Action
    public boolean performSwipe(Point start, Point end, long duration) {
        if (start == null || end == null) {
            Log.e(TAG, Constants.Errors.INVALID_COORDINATES);
            return false;
        }

        Path swipePath = new Path();
        swipePath.moveTo(start.x, start.y);
        swipePath.lineTo(end.x, end.y);

        GestureDescription.Builder builder = new GestureDescription.Builder();
        builder.addStroke(new GestureDescription.StrokeDescription(swipePath, 0, duration));

        return dispatchGesture(builder.build(), new GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gestureDescription) {
                Log.d(TAG, "Swipe completed from: " + start.x + "," + start.y + " to: " + end.x + "," + end.y);
            }
        }, null);
    }

    // Text Search Action
    public boolean findAndClickText(String text) {
        if (text == null || text.isEmpty()) {
            Log.e(TAG, "Invalid text search parameter");
            return false;
        }

        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root == null) {
            Log.e(TAG, "No active window");
            return false;
        }

        boolean result = AccessibilityUtils.findAndClickNodeByText(root, text);
        root.recycle();
        return result;
    }

    // Text Search without Click
    public AccessibilityNodeInfo findText(String text) {
        if (text == null || text.isEmpty()) {
            Log.e(TAG, "Invalid text search parameter");
            return null;
        }

        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root == null) {
            Log.e(TAG, "No active window");
            return null;
        }

        List<AccessibilityNodeInfo> nodes = AccessibilityUtils.findNodesByText(root, text);
        root.recycle();

        if (!nodes.isEmpty()) {
            AccessibilityNodeInfo result = nodes.get(0);
            for (int i = 1; i < nodes.size(); i++) {
                nodes.get(i).recycle();
            }
            return result;
        }
        return null;
    }

    // Image Search Actions
    public boolean findAndClickImage(String base64Image, float threshold) {
        // Note: Image search functionality requires OpenCV or similar
        // This is a placeholder that always returns false
        Log.w(TAG, "Image search not implemented");
        return false;
    }

    public AccessibilityNodeInfo findImage(String base64Image, float threshold) {
        // Note: Image search functionality requires OpenCV or similar
        // This is a placeholder that always returns null
        Log.w(TAG, "Image search not implemented");
        return null;
    }

    private void showToast(final String message) {
        mainHandler.post(() -> Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show());
    }
}
