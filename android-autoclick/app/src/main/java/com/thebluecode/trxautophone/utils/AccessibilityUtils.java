package com.thebluecode.trxautophone.utils;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.accessibility.AccessibilityNodeInfo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Enhanced utility class for accessibility operations with improved error handling
 * and additional helper methods for UI interaction
 */
public class AccessibilityUtils {
    private static final String TAG = "AccessibilityUtils";
    private static final int GESTURE_COMPLETION_TIMEOUT = 5000; // 5 seconds

    private AccessibilityUtils() {
        // Private constructor to prevent instantiation
    }

    /**
     * Find a node by exact text match
     */
    @Nullable
    public static AccessibilityNodeInfo findNodeByText(@Nullable AccessibilityNodeInfo root, @NonNull String text) {
        if (root == null || text.isEmpty()) {
            return null;
        }

        List<AccessibilityNodeInfo> nodes = root.findAccessibilityNodeInfosByText(text);
        for (AccessibilityNodeInfo node : nodes) {
            if (node != null) {
                CharSequence nodeText = node.getText();
                if (nodeText != null && text.equals(nodeText.toString())) {
                    return node;
                } else {
                    node.recycle();
                }
            }
        }
        return null;
    }

    /**
     * Find a node by content description
     */
    @Nullable
    public static AccessibilityNodeInfo findNodeByDescription(@Nullable AccessibilityNodeInfo root, @NonNull String description) {
        if (root == null || description.isEmpty()) {
            return null;
        }

        if (description.equals(root.getContentDescription())) {
            return root;
        }

        for (int i = 0; i < root.getChildCount(); i++) {
            AccessibilityNodeInfo child = root.getChild(i);
            if (child != null) {
                AccessibilityNodeInfo target = findNodeByDescription(child, description);
                if (target != null) {
                    child.recycle();
                    return target;
                }
                child.recycle();
            }
        }
        return null;
    }

    /**
     * Find a clickable node at specific coordinates
     */
    @Nullable
    public static AccessibilityNodeInfo findClickableNodeAtLocation(@Nullable AccessibilityNodeInfo root, float x, float y) {
        if (root == null) {
            return null;
        }

        Rect bounds = new Rect();
        root.getBoundsInScreen(bounds);

        if (bounds.contains((int) x, (int) y)) {
            if (root.isClickable()) {
                return root;
            }

            for (int i = 0; i < root.getChildCount(); i++) {
                AccessibilityNodeInfo child = root.getChild(i);
                if (child != null) {
                    AccessibilityNodeInfo target = findClickableNodeAtLocation(child, x, y);
                    if (target != null) {
                        child.recycle();
                        return target;
                    }
                    child.recycle();
                }
            }
        }
        return null;
    }

    /**
     * Perform a click at specific coordinates
     */
    public static boolean clickAtLocation(@NonNull AccessibilityService service, float x, float y) {
        Path clickPath = new Path();
        clickPath.moveTo(x, y);

        GestureDescription.Builder builder = new GestureDescription.Builder();
        builder.addStroke(new GestureDescription.StrokeDescription(clickPath, 0, 100));

        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicBoolean success = new AtomicBoolean(false);

        service.dispatchGesture(builder.build(), new AccessibilityService.GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gestureDescription) {
                success.set(true);
                latch.countDown();
            }

            @Override
            public void onCancelled(GestureDescription gestureDescription) {
                latch.countDown();
            }
        }, null);

        try {
            latch.await(GESTURE_COMPLETION_TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Log.e(TAG, "Click gesture interrupted", e);
            Thread.currentThread().interrupt();
            return false;
        }

        return success.get();
    }

    /**
     * Perform a long press at specific coordinates
     */
    public static boolean longPressAtLocation(@NonNull AccessibilityService service, float x, float y, long duration) {
        Path pressPath = new Path();
        pressPath.moveTo(x, y);

        GestureDescription.Builder builder = new GestureDescription.Builder();
        builder.addStroke(new GestureDescription.StrokeDescription(pressPath, 0, duration));

        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicBoolean success = new AtomicBoolean(false);

        service.dispatchGesture(builder.build(), new AccessibilityService.GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gestureDescription) {
                success.set(true);
                latch.countDown();
            }

            @Override
            public void onCancelled(GestureDescription gestureDescription) {
                latch.countDown();
            }
        }, null);

        try {
            latch.await(GESTURE_COMPLETION_TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Log.e(TAG, "Long press gesture interrupted", e);
            Thread.currentThread().interrupt();
            return false;
        }

        return success.get();
    }

    /**
     * Perform a swipe gesture
     */
    public static boolean performSwipe(@NonNull AccessibilityService service, 
                                     float startX, float startY, 
                                     float endX, float endY, 
                                     long duration) {
        Path swipePath = new Path();
        swipePath.moveTo(startX, startY);
        swipePath.lineTo(endX, endY);

        GestureDescription.Builder builder = new GestureDescription.Builder();
        builder.addStroke(new GestureDescription.StrokeDescription(swipePath, 0, duration));

        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicBoolean success = new AtomicBoolean(false);

        service.dispatchGesture(builder.build(), new AccessibilityService.GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gestureDescription) {
                success.set(true);
                latch.countDown();
            }

            @Override
            public void onCancelled(GestureDescription gestureDescription) {
                latch.countDown();
            }
        }, null);

        try {
            latch.await(GESTURE_COMPLETION_TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Log.e(TAG, "Swipe gesture interrupted", e);
            Thread.currentThread().interrupt();
            return false;
        }

        return success.get();
    }

    /**
     * Input text into a focused field
     */
    public static boolean inputText(@NonNull AccessibilityService service, @NonNull String text) {
        AccessibilityNodeInfo root = service.getRootInActiveWindow();
        if (root == null) {
            return false;
        }

        try {
            AccessibilityNodeInfo focused = root.findFocus(AccessibilityNodeInfo.FOCUS_INPUT);
            if (focused == null) {
                return false;
            }

            Bundle arguments = new Bundle();
            arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text);
            return focused.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments);
        } finally {
            root.recycle();
        }
    }

    /**
     * Find all nodes containing specific text
     */
    @NonNull
    public static List<AccessibilityNodeInfo> findNodesContainingText(@Nullable AccessibilityNodeInfo root, @NonNull String text) {
        List<AccessibilityNodeInfo> matches = new ArrayList<>();
        if (root == null || text.isEmpty()) {
            return matches;
        }

        List<AccessibilityNodeInfo> nodes = root.findAccessibilityNodeInfosByText(text);
        for (AccessibilityNodeInfo node : nodes) {
            if (node != null) {
                CharSequence nodeText = node.getText();
                if (nodeText != null && nodeText.toString().contains(text)) {
                    matches.add(node);
                } else {
                    node.recycle();
                }
            }
        }
        return matches;
    }

    /**
     * Get node bounds in screen coordinates
     */
    @NonNull
    public static Rect getNodeBounds(@NonNull AccessibilityNodeInfo node) {
        Rect bounds = new Rect();
        node.getBoundsInScreen(bounds);
        return bounds;
    }

    /**
     * Check if coordinates are within node bounds
     */
    public static boolean isWithinNodeBounds(@NonNull AccessibilityNodeInfo node, float x, float y) {
        Rect bounds = getNodeBounds(node);
        return bounds.contains((int) x, (int) y);
    }

    /**
     * Get center coordinates of a node
     */
    @NonNull
    public static float[] getNodeCenter(@NonNull AccessibilityNodeInfo node) {
        Rect bounds = getNodeBounds(node);
        return new float[] {
            bounds.exactCenterX(),
            bounds.exactCenterY()
        };
    }

    /**
     * Scroll to make a node visible
     */
    public static boolean scrollToNode(@NonNull AccessibilityNodeInfo node) {
        AccessibilityNodeInfo parent = node;
        while (parent != null) {
            if (parent.isScrollable()) {
                return parent.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
            }
            parent = parent.getParent();
        }
        return false;
    }

    /**
     * Check if a node is visible on screen
     */
    public static boolean isNodeVisible(@NonNull AccessibilityNodeInfo node) {
        if (!node.isVisibleToUser()) {
            return false;
        }

        Rect bounds = getNodeBounds(node);
        return bounds.width() > 0 && bounds.height() > 0;
    }

    /**
     * Find the first clickable ancestor of a node
     */
    @Nullable
    public static AccessibilityNodeInfo findClickableAncestor(@NonNull AccessibilityNodeInfo node) {
        AccessibilityNodeInfo current = node;
        while (current != null) {
            if (current.isClickable()) {
                return current;
            }
            AccessibilityNodeInfo parent = current.getParent();
            if (current != node) {
                current.recycle();
            }
            current = parent;
        }
        return null;
    }

    /**
     * Perform click on a node or its clickable ancestor
     */
    public static boolean clickNode(@NonNull AccessibilityNodeInfo node) {
        if (node.isClickable()) {
            return node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
        }

        AccessibilityNodeInfo clickable = findClickableAncestor(node);
        if (clickable != null) {
            boolean result = clickable.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            if (clickable != node) {
                clickable.recycle();
            }
            return result;
        }

        return false;
    }

    /**
     * Wait for a node with specific text to appear
     */
    public static boolean waitForNode(@NonNull AccessibilityService service, 
                                    @NonNull String text, 
                                    long timeout) {
        long endTime = System.currentTimeMillis() + timeout;
        while (System.currentTimeMillis() < endTime) {
            AccessibilityNodeInfo root = service.getRootInActiveWindow();
            if (root != null) {
                AccessibilityNodeInfo node = findNodeByText(root, text);
                root.recycle();
                if (node != null) {
                    node.recycle();
                    return true;
                }
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        return false;
    }

    /**
     * Get all clickable nodes in the hierarchy
     */
    @NonNull
    public static List<AccessibilityNodeInfo> findAllClickableNodes(@Nullable AccessibilityNodeInfo root) {
        List<AccessibilityNodeInfo> clickables = new ArrayList<>();
        if (root == null) {
            return clickables;
        }

        if (root.isClickable()) {
            clickables.add(root);
        }

        for (int i = 0; i < root.getChildCount(); i++) {
            AccessibilityNodeInfo child = root.getChild(i);
            if (child != null) {
                clickables.addAll(findAllClickableNodes(child));
                child.recycle();
            }
        }

        return clickables;
    }

    /**
     * Clean up node resources
     */
    public static void recycleNodes(List<AccessibilityNodeInfo> nodes) {
        for (AccessibilityNodeInfo node : nodes) {
            if (node != null) {
                node.recycle();
            }
        }
        nodes.clear();
    }
}
