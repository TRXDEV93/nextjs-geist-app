package com.thebluecode.trxautophone.services;

import android.graphics.Point;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.accessibility.AccessibilityNodeInfo;

import com.thebluecode.trxautophone.models.Step;
import com.thebluecode.trxautophone.models.Task;
import com.thebluecode.trxautophone.utils.Constants;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class TaskExecutor {
    private static final String TAG = TaskExecutor.class.getSimpleName();
    private static TaskExecutor instance;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private final AtomicBoolean isPaused = new AtomicBoolean(false);
    private Task currentTask;
    private int currentStepIndex;
    private ExecutionCallback callback;
    private final Gson gson = new Gson();
    private final List<LogEntry> executionLog = new ArrayList<>();

    public static class LogEntry {
        private final long timestamp;
        private final String message;
        private final LogType type;

        public enum LogType {
            INFO,
            ERROR,
            SUCCESS
        }

        public LogEntry(String message, LogType type) {
            this.timestamp = System.currentTimeMillis();
            this.message = message;
            this.type = type;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public String getMessage() {
            return message;
        }

        public LogType getType() {
            return type;
        }
    }

    public interface ExecutionCallback {
        void onStepStarted(Step step);
        void onStepExecuted(Step step, boolean success);
        void onTaskCompleted(Task task, boolean success);
        void onTaskPaused(Task task, int currentStep);
        void onError(String error);
    }

    private TaskExecutor() {}

    public static TaskExecutor getInstance() {
        if (instance == null) {
            instance = new TaskExecutor();
        }
        return instance;
    }

    public void executeTask(Task task, ExecutionCallback callback) {
        if (isRunning.get()) {
            callback.onError(Constants.Errors.SERVICE_NOT_RUNNING);
            return;
        }

        this.currentTask = task;
        this.callback = callback;
        this.currentStepIndex = 0;
        this.isRunning.set(true);
        this.isPaused.set(false);

        executeNextStep();
    }

    public void pauseExecution() {
        if (isRunning.get() && !isPaused.get()) {
            isPaused.set(true);
            if (callback != null && currentTask != null) {
                callback.onTaskPaused(currentTask, currentStepIndex);
            }
        }
    }

    public void resumeExecution() {
        if (isRunning.get() && isPaused.get()) {
            isPaused.set(false);
            executeNextStep();
        }
    }

    public void stopExecution() {
        isRunning.set(false);
        isPaused.set(false);
        currentTask = null;
        currentStepIndex = 0;
    }

    private void executeNextStep() {
        if (!isRunning.get() || isPaused.get() || currentTask == null || 
            currentStepIndex >= currentTask.getSteps().size()) {
            completeTask(true);
            return;
        }

        Step currentStep = currentTask.getSteps().get(currentStepIndex);
        if (callback != null) {
            mainHandler.post(() -> callback.onStepStarted(currentStep));
        }
        
        long delay = Math.max(
            Math.min(currentStep.getDelay(), Constants.MAX_DELAY),
            Constants.MIN_DELAY
        );
        
        if (delay > 0) {
            mainHandler.postDelayed(() -> executeStep(currentStep), delay);
        } else {
            executeStep(currentStep);
        }
    }

    private void executeStep(Step step) {
        if (!isRunning.get() || isPaused.get()) return;

        AutoClickAccessibilityService service = AutoClickAccessibilityService.getInstance();
        if (service == null) {
            addLogEntry("Accessibility service not running", LogEntry.LogType.ERROR);
            handleError(Constants.Errors.SERVICE_NOT_RUNNING);
            return;
        }

        boolean success = false;
        try {
            addLogEntry("Executing step: " + step.getType(), LogEntry.LogType.INFO);
            JsonObject actionData = gson.fromJson(step.getActionData(), JsonObject.class);
            if (actionData == null) {
                throw new IllegalArgumentException("Invalid action data for step: " + step.getId());
            }

            switch (step.getType()) {
                case SYSTEM_KEY:
                    success = handleSystemKey(service, actionData);
                    break;
                case TAP:
                    success = handleTap(service, actionData);
                    break;
                case LONG_PRESS:
                    success = handleLongPress(service, actionData);
                    break;
                case SWIPE:
                    success = handleSwipe(service, actionData);
                    break;
                case TEXT_SEARCH:
                    success = handleTextSearch(service, actionData);
                    break;
                case IMAGE_SEARCH:
                    success = handleImageSearch(service, actionData);
                    break;
                case DELAY:
                    success = true; // Delay is handled before step execution
                    break;
                case CONDITION:
                    success = handleCondition(service, actionData);
                    break;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error executing step: " + e.getMessage());
            handleError("Error executing step: " + e.getMessage());
            return;
        }

        addLogEntry(
            success ? "Step executed successfully" : "Step execution failed",
            success ? LogEntry.LogType.SUCCESS : LogEntry.LogType.ERROR
        );
        
        final boolean stepSuccess = success;
        mainHandler.post(() -> {
            if (callback != null) {
                callback.onStepExecuted(step, stepSuccess);
            }
            if (stepSuccess) {
                currentStepIndex++;
                executeNextStep();
            } else {
                completeTask(false);
            }
        });
    }

    private boolean handleSystemKey(AutoClickAccessibilityService service, JsonObject actionData) {
        try {
            int action = actionData.get("action").getAsInt();
            return service.performSystemAction(action);
        } catch (Exception e) {
            Log.e(TAG, "Error performing system action", e);
            return false;
        }
    }

    private boolean handleTap(AutoClickAccessibilityService service, JsonObject actionData) {
        try {
            Point tapPoint = new Point(
                actionData.get("x").getAsInt(),
                actionData.get("y").getAsInt()
            );
            return service.performTap(tapPoint);
        } catch (Exception e) {
            Log.e(TAG, Constants.Errors.INVALID_COORDINATES, e);
            return false;
        }
    }

    private boolean handleLongPress(AutoClickAccessibilityService service, JsonObject actionData) {
        try {
            Point pressPoint = new Point(
                actionData.get("x").getAsInt(),
                actionData.get("y").getAsInt()
            );
            long duration = actionData.get("duration").getAsLong();
            return service.performLongPress(pressPoint, duration);
        } catch (Exception e) {
            Log.e(TAG, Constants.Errors.INVALID_COORDINATES, e);
            return false;
        }
    }

    private boolean handleSwipe(AutoClickAccessibilityService service, JsonObject actionData) {
        try {
            Point startPoint = new Point(
                actionData.get("startX").getAsInt(),
                actionData.get("startY").getAsInt()
            );
            Point endPoint = new Point(
                actionData.get("endX").getAsInt(),
                actionData.get("endY").getAsInt()
            );
            long duration = actionData.get("duration").getAsLong();
            return service.performSwipe(startPoint, endPoint, duration);
        } catch (Exception e) {
            Log.e(TAG, Constants.Errors.INVALID_COORDINATES, e);
            return false;
        }
    }

    private boolean handleTextSearch(AutoClickAccessibilityService service, JsonObject actionData) {
        try {
            String searchText = actionData.get("text").getAsString();
            return service.findAndClickText(searchText);
        } catch (Exception e) {
            Log.e(TAG, "Error performing text search", e);
            return false;
        }
    }

    private boolean handleImageSearch(AutoClickAccessibilityService service, JsonObject actionData) {
        try {
            String base64Image = actionData.get("image").getAsString();
            float threshold = actionData.has("threshold") ? 
                actionData.get("threshold").getAsFloat() : 0.8f;
            return service.findAndClickImage(base64Image, threshold);
        } catch (Exception e) {
            Log.e(TAG, "Error performing image search", e);
            return false;
        }
    }

    private boolean handleCondition(AutoClickAccessibilityService service, JsonObject actionData) {
        try {
            String type = actionData.get("type").getAsString();
            switch (type) {
                case "text_exists":
                    String text = actionData.get("text").getAsString();
                    AccessibilityNodeInfo node = service.findText(text);
                    boolean result = node != null;
                    if (node != null) node.recycle();
                    return result;
                case "image_exists":
                    String base64Image = actionData.get("image").getAsString();
                    float threshold = actionData.has("threshold") ? 
                        actionData.get("threshold").getAsFloat() : 0.8f;
                    return service.findImage(base64Image, threshold) != null;
                default:
                    return false;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error handling condition", e);
            return false;
        }
    }

    private void handleError(String error) {
        if (callback != null) {
            mainHandler.post(() -> callback.onError(error));
        }
        completeTask(false);
    }

    private void completeTask(boolean success) {
        if (!isRunning.get()) return;
        
        isRunning.set(false);
        isPaused.set(false);
        
        if (callback != null && currentTask != null) {
            Task completedTask = currentTask;
            mainHandler.post(() -> callback.onTaskCompleted(completedTask, success));
        }
        
        currentTask = null;
        currentStepIndex = 0;
    }

    public boolean isRunning() {
        return isRunning.get();
    }

    public boolean isPaused() {
        return isPaused.get();
    }

    public Task getCurrentTask() {
        return currentTask;
    }

    public int getCurrentStepIndex() {
        return currentStepIndex;
    }

    private void addLogEntry(String message, LogEntry.LogType type) {
        executionLog.add(new LogEntry(message, type));
    }

    public List<LogEntry> getExecutionLog() {
        return new ArrayList<>(executionLog);
    }

    public void clearLog() {
        executionLog.clear();
    }
}
