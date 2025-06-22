package com.thebluecode.trxautophone.services;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.accessibilityservice.GestureDescription;
import android.app.Instrumentation;
import android.app.Service;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Path;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityManager;
import android.widget.Toast;

import com.thebluecode.trxautophone.models.Step;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AutoClickService extends AccessibilityService {

    private static AutoClickService instance;
    private Handler mainHandler = new Handler(Looper.getMainLooper());
    private List<Step> currentSteps;
    private int currentStepIndex = 0;
    private boolean isRunning = false;
    private ExecutorService executor = Executors.newSingleThreadExecutor();

    public static boolean isRunning() {
        return instance != null && instance.isRunning;
    }

    public static void start(Context context, List<Step> steps) {
        if (instance == null) {
            Toast.makeText(context, "Vui lòng bật dịch vụ truy cập!", Toast.LENGTH_LONG).show();
            context.startActivity(new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS));
            return;
        }
        instance.executeSteps(steps);
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        instance = this;
        isRunning = false;
        Toast.makeText(this, "AutoClick Service đã kết nối", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onInterrupt() {
        Toast.makeText(this, "AutoClick bị ngắt", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // Xử lý sự kiện nếu cần
    }

    private void executeSteps(List<Step> steps) {
        this.currentSteps = steps;
        this.currentStepIndex = 0;
        this.isRunning = true;
        runNextStep();
    }

    private void runNextStep() {
        if (currentStepIndex >= currentSteps.size()) {
            stopExecution();
            return;
        }

        Step step = currentSteps.get(currentStepIndex++);
        long delay = getRandomDelay(step.getMinDelay(), step.getMaxDelay());

        mainHandler.postDelayed(() -> {
            if (!isRunning) return;
            performStep(step);
            runNextStep();
        }, delay);
    }

    private long getRandomDelay(long min, long max) {
        return (long) (Math.random() * (max - min)) + min;
    }

    private void performStep(Step step) {
        switch (step.getType()) {
            case CLICK_TEXT:
                findAndClickText(step.getValue());
                break;
            case CLICK_IMAGE:
                Toast.makeText(this, "Chức năng nhận diện hình ảnh đang phát triển", Toast.LENGTH_SHORT).show();
                break;
            case CLICK_COORDINATE:
                gestureClick(step.getX(), step.getY());
                break;
            case INPUT_TEXT:
                inputText(step.getValue());
                break;
            case SWIPE_UP:
                swipeUp();
                break;
            case SWIPE_DOWN:
                swipeDown();
                break;
            case LAUNCH_APP:
                launchApp(step.getValue());
                break;
            case CLOSE_APP:
                closeApp(step.getValue());
                break;
            case TOGGLE_AIRPLANE_MODE:
                toggleAirplaneMode();
                break;
            default:
                Toast.makeText(this, "Loại thao tác chưa hỗ trợ", Toast.LENGTH_SHORT).show();
        }
    }

    private void findAndClickText(String text) {
        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root == null) return;

        List<AccessibilityNodeInfo> nodes = root.findAccessibilityNodeInfosByViewId("android:id/text1");
        for (AccessibilityNodeInfo node : nodes) {
            if (text.equals(node.getText())) {
                node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                break;
            }
        }
    }

    private void inputText(String text) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("text", text);
        clipboard.setPrimaryClip(clip);

        final Instrumentation inst = new Instrumentation();
        mainHandler.post(() -> {
            try {
                inst.sendKeyDownUpSync(KeyEvent.KEYCODE_PASTE);
            } catch (Exception e) {
                Toast.makeText(this, "Không thể dán nội dung", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void gestureClick(int x, int y) {
        Path path = new Path();
        path.moveTo(x, y);
        path.lineTo(x, y);

        GestureDescription.Builder builder = new GestureDescription.Builder();
        builder.addStroke(new GestureDescription.StrokeDescription(path, 0, 100));
        dispatchGesture(builder.build(), new GestureResultCallback() {}, null);
    }

    private void swipeUp() {
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;

        Path path = new Path();
        path.moveTo(width / 2, height - 100);
        path.lineTo(width / 2, 100);

        GestureDescription.Builder builder = new GestureDescription.Builder();
        builder.addStroke(new GestureDescription.StrokeDescription(path, 0, 300));
        dispatchGesture(builder.build(), new GestureResultCallback() {}, null);
    }

    private void swipeDown() {
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;

        Path path = new Path();
        path.moveTo(width / 2, 100);
        path.lineTo(width / 2, height - 100);

        GestureDescription.Builder builder = new GestureDescription.Builder();
        builder.addStroke(new GestureDescription.StrokeDescription(path, 0, 300));
        dispatchGesture(builder.build(), new GestureResultCallback() {}, null);
    }

    private void launchApp(String packageName) {
        Intent intent = getPackageManager().getLaunchIntentForPackage(packageName);
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } else {
            Toast.makeText(this, "Không tìm thấy ứng dụng", Toast.LENGTH_SHORT).show();
        }
    }

    private void closeApp(String packageName) {
        android.app.ActivityManager am = (android.app.ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        if (am != null) {
            am.killBackgroundProcesses(packageName);
        }
    }

    private void toggleAirplaneMode() {
        boolean isEnabled = Settings.Global.getInt(getContentResolver(),
                Settings.Global.AIRPLANE_MODE_ON, 0) == 1;

        Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        intent.putExtra("state", !isEnabled);
        sendBroadcast(intent);

        Settings.Global.putInt(getContentResolver(),
                Settings.Global.AIRPLANE_MODE_ON, isEnabled ? 0 : 1);
    }

    private void stopExecution() {
        isRunning = false;
        Toast.makeText(this, "Hoàn tất tác vụ", Toast.LENGTH_SHORT).show();
    }

    public static boolean isAccessibilityEnabled(Context context, Class<? extends Service> service) {
        AccessibilityManager am = (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);
        if (am == null) return false;

        for (AccessibilityServiceInfo info : am.getInstalledAccessibilityServiceList()) {
            if (info.getId().equals(service.getName())) {
                return true;
            }
        }
        return false;
    }
}