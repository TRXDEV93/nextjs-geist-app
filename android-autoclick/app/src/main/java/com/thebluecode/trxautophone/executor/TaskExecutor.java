package com.thebluecode.trxautophone.executor;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Path;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.accessibility.AccessibilityNodeInfo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.thebluecode.trxautophone.AutoClickApplication;
import com.thebluecode.trxautophone.models.Step;
import com.thebluecode.trxautophone.models.Task;
import com.thebluecode.trxautophone.utils.AccessibilityUtils;
import com.thebluecode.trxautophone.utils.Constants;
import com.thebluecode.trxautophone.utils.NotificationUtils;

import org.json.JSONObject;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Enhanced TaskExecutor with improved error handling and execution flow
 */
public class TaskExecutor {
    private static final String TAG = "TaskExecutor";

    private final AutoClickApplication application;
    private final AccessibilityService service;
    private final Handler mainHandler;
    private final ExecutionCallback callback;

    private Task currentTask;
    private List<Step> steps;
    private int currentStepIndex;
    private int currentRepeatCount;
    private final AtomicBoolean isRunning;
    private final AtomicBoolean isPaused;
    private final AtomicInteger successfulSteps;
    private long lastStepTime;

    public interface ExecutionCallback {
        void onExecutionStarted(Task task);
        void onStepStarted(Step step, int position, int total);
        void onStepCompleted(Step step, boolean success);
        void onExecutionPaused(Task task);
        void onExecutionResumed(Task task);
        void onExecutionCompleted(Task task, boolean success);
        void onExecutionError(String error);
    }

    public TaskExecutor(@NonNull AutoClickApplication application,
                       @NonNull AccessibilityService service,
                       @NonNull ExecutionCallback callback) {
        this.application = application;
        this.service = service;
        this.callback = callback;
        this.mainHandler = new Handler(Looper.getMainLooper());
        this.isRunning = new AtomicBoolean(false);
        this.isPaused = new AtomicBoolean(false);
        this.successfulSteps = new AtomicInteger(0);
    }

    /**
     * Start executing a task
     */
    public void executeTask(@NonNull Task task) {
        if (isRunning.get()) {
            Log.w(TAG, "Task execution already in progress");
            return;
        }

        this.currentTask = task;
        this.steps = task.getSteps();
        this.currentStepIndex = 0;
        this.currentRepeatCount = 1;
        this.successfulSteps.set(0);
        this.lastStepTime = System.currentTimeMillis();

        if (!validateTask()) {
            return;
        }

        isRunning.set(true);
        isPaused.set(false);

        Log.i(TAG, "Starting task execution: " + task.getName());
        notifyExecutionStarted();
        executeNextStep();
    }

    /**
     * Validate task before execution
     */
    private boolean validateTask() {
        if (currentTask == null) {
            notifyError("No task provided");
            return false;
        }

        if (steps == null || steps.isEmpty()) {
            notifyError("Task has no steps");
            return false;
        }

        if (!currentTask.isEnabled()) {
            notifyError("Task is disabled");
            return false;
        }

        return true;
    }

    /**
     * Execute the next step in the sequence
     */
    private void executeNextStep() {
        if (!isRunning.get() || isPaused.get()) {
            return;
        }

        if (currentStepIndex >= steps.size()) {
            handleRepeatOrComplete();
            return;
        }

        Step step = steps.get(currentStepIndex);
        if (!step.isEnabled()) {
            currentStepIndex++;
            executeNextStep();
            return;
        }

        notifyStepStarted(step);

        // Handle step delay
        long delay = step.getDelay();
        if (delay > 0) {
            mainHandler.postDelayed(() -> executeStep(step), delay);
        } else {
            executeStep(step);
        }
    }

    /**
     * Execute a single step
     */
    private void executeStep(Step step) {
        try {
            Log.d(TAG, "Executing step: " + step.getSummary());
            lastStepTime = System.currentTimeMillis();

            switch (step.getType()) {
                case TAP:
                    handleTap(step);
                    break;
                case LONG_PRESS:
                    handleLongPress(step);
                    break;
                case SWIPE:
                    handleSwipe(step);
                    break;
                case TEXT_SEARCH:
                    handleTextSearch(step);
                    break;
                case SYSTEM_KEY:
                    handleSystemKey(step);
                    break;
                case DELAY:
                    handleDelay(step);
                    break;
                case INPUT_TEXT:
                    handleInputText(step);
                    break;
                default:
                    Log.w(TAG, "Unsupported step type: " + step.getType());
                    onStepComplete(step, false);
                    break;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error executing step: " + e.getMessage());
            onStepComplete(step, false);
        }
    }

    /**
     * Handle tap gesture
     */
    private void handleTap(Step step) {
        try {
            JSONObject data = new JSONObject(step.getActionData());
            float x = (float) data.getDouble("x");
            float y = (float) data.getDouble("y");

            Path path = new Path();
            path.moveTo(x, y);

            GestureDescription.Builder builder = new GestureDescription.Builder();
            builder.addStroke(new GestureDescription.StrokeDescription(
                    path, 0, Constants.Defaults.DEFAULT_TAP_DURATION));

            service.dispatchGesture(builder.build(), new AccessibilityService.GestureResultCallback() {
                @Override
                public void onCompleted(GestureDescription gestureDescription) {
                    onStepComplete(step, true);
                }

                @Override
                public void onCancelled(GestureDescription gestureDescription) {
                    onStepComplete(step, false);
                }
            }, null);
        } catch (Exception e) {
            Log.e(TAG, "Error performing tap: " + e.getMessage());
            onStepComplete(step, false);
        }
    }

    /**
     * Handle long press gesture
     */
    private void handleLongPress(Step step) {
        try {
            JSONObject data = new JSONObject(step.getActionData());
            float x = (float) data.getDouble("x");
            float y = (float) data.getDouble("y");
            long duration = data.optLong("duration", Constants.Defaults.DEFAULT_LONG_PRESS_DURATION);

            Path path = new Path();
            path.moveTo(x, y);

            GestureDescription.Builder builder = new GestureDescription.Builder();
            builder.addStroke(new GestureDescription.StrokeDescription(
                    path, 0, duration));

            service.dispatchGesture(builder.build(), new AccessibilityService.GestureResultCallback() {
                @Override
                public void onCompleted(GestureDescription gestureDescription) {
                    onStepComplete(step, true);
                }

                @Override
                public void onCancelled(GestureDescription gestureDescription) {
                    onStepComplete(step, false);
                }
            }, null);
        } catch (Exception e) {
            Log.e(TAG, "Error performing long press: " + e.getMessage());
            onStepComplete(step, false);
        }
    }

    /**
     * Handle swipe gesture
     */
    private void handleSwipe(Step step) {
        try {
            JSONObject data = new JSONObject(step.getActionData());
            float startX = (float) data.getDouble("startX");
            float startY = (float) data.getDouble("startY");
            float endX = (float) data.getDouble("endX");
            float endY = (float) data.getDouble("endY");
            long duration = data.optLong("duration", Constants.Defaults.DEFAULT_SWIPE_DURATION);

            Path path = new Path();
            path.moveTo(startX, startY);
            path.lineTo(endX, endY);

            GestureDescription.Builder builder = new GestureDescription.Builder();
            builder.addStroke(new GestureDescription.StrokeDescription(
                    path, 0, duration));

            service.dispatchGesture(builder.build(), new AccessibilityService.GestureResultCallback() {
                @Override
                public void onCompleted(GestureDescription gestureDescription) {
                    onStepComplete(step, true);
                }

                @Override
                public void onCancelled(GestureDescription gestureDescription) {
                    onStepComplete(step, false);
                }
            }, null);
        } catch (Exception e) {
            Log.e(TAG, "Error performing swipe: " + e.getMessage());
            onStepComplete(step, false);
        }
    }

    /**
     * Handle text search and click
     */
    private void handleTextSearch(Step step) {
        try {
            JSONObject data = new JSONObject(step.getActionData());
            String searchText = data.getString("text");
            boolean clickAfterFound = data.optBoolean("click", true);

            AccessibilityNodeInfo root = service.getRootInActiveWindow();
            if (root == null) {
                Log.e(TAG, "No active window");
                onStepComplete(step, false);
                return;
            }

            AccessibilityNodeInfo node = AccessibilityUtils.findNodeByText(root, searchText);
            if (node != null) {
                if (clickAfterFound) {
                    boolean clicked = node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    onStepComplete(step, clicked);
                } else {
                    onStepComplete(step, true);
                }
                node.recycle();
            } else {
                onStepComplete(step, false);
            }
            root.recycle();
        } catch (Exception e) {
            Log.e(TAG, "Error performing text search: " + e.getMessage());
            onStepComplete(step, false);
        }
    }

    /**
     * Handle system key actions
     */
    private void handleSystemKey(Step step) {
        try {
            JSONObject data = new JSONObject(step.getActionData());
            int action = data.getInt("action");
            boolean success = service.performGlobalAction(action);
            onStepComplete(step, success);
        } catch (Exception e) {
            Log.e(TAG, "Error performing system action: " + e.getMessage());
            onStepComplete(step, false);
        }
    }

    /**
     * Handle delay step
     */
    private void handleDelay(Step step) {
        try {
            JSONObject data = new JSONObject(step.getActionData());
            long delay = data.optLong("delay", Constants.Defaults.DEFAULT_DELAY);
            mainHandler.postDelayed(() -> onStepComplete(step, true), delay);
        } catch (Exception e) {
            Log.e(TAG, "Error handling delay: " + e.getMessage());
            onStepComplete(step, false);
        }
    }

    /**
     * Handle text input
     */
    private void handleInputText(Step step) {
        try {
            JSONObject data = new JSONObject(step.getActionData());
            String text = data.getString("text");
            
            AccessibilityNodeInfo root = service.getRootInActiveWindow();
            if (root == null) {
                Log.e(TAG, "No active window");
                onStepComplete(step, false);
                return;
            }

            AccessibilityNodeInfo focusedNode = root.findFocus(AccessibilityNodeInfo.FOCUS_INPUT);
            if (focusedNode != null) {
                Bundle arguments = new Bundle();
                arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text);
                boolean success = focusedNode.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments);
                focusedNode.recycle();
                onStepComplete(step, success);
            } else {
                onStepComplete(step, false);
            }
            root.recycle();
        } catch (Exception e) {
            Log.e(TAG, "Error inputting text: " + e.getMessage());
            onStepComplete(step, false);
        }
    }

    /**
     * Handle step completion
     */
    private void onStepComplete(Step step, boolean success) {
        if (success) {
            successfulSteps.incrementAndGet();
        }
        
        notifyStepCompleted(step, success);
        currentStepIndex++;
        executeNextStep();
    }

    /**
     * Handle task repetition or completion
     */
    private void handleRepeatOrComplete() {
        if (currentRepeatCount < currentTask.getRepeatCount()) {
            currentRepeatCount++;
            currentStepIndex = 0;
            long repeatDelay = currentTask.getRepeatDelay();
            
            if (repeatDelay > 0) {
                mainHandler.postDelayed(this::executeNextStep, repeatDelay);
            } else {
                executeNextStep();
            }
        } else {
            completeExecution();
        }
    }

    /**
     * Complete task execution
     */
    private void completeExecution() {
        isRunning.set(false);
        isPaused.set(false);

        boolean success = successfulSteps.get() == getTotalRequiredSteps();
        currentTask.recordExecution(success);
        
        // Save execution result
        application.executeAsync(() -> {
            try {
                application.getDatabase().taskDao().updateTask(currentTask);
            } catch (Exception e) {
                Log.e(TAG, "Error saving execution result: " + e.getMessage());
            }
        });

        notifyExecutionCompleted(success);
    }

    /**
     * Get total number of required successful steps
     */
    private int getTotalRequiredSteps() {
        int enabledSteps = 0;
        for (Step step : steps) {
            if (step.isEnabled()) {
                enabledSteps++;
            }
        }
        return enabledSteps * currentTask.getRepeatCount();
    }

    /**
     * Pause task execution
     */
    public void pauseExecution() {
        if (isRunning.get() && !isPaused.get()) {
            isPaused.set(true);
            notifyExecutionPaused();
        }
    }

    /**
     * Resume task execution
     */
    public void resumeExecution() {
        if (isRunning.get() && isPaused.get()) {
            isPaused.set(false);
            notifyExecutionResumed();
            executeNextStep();
        }
    }

    /**
     * Stop task execution
     */
    public void stopExecution() {
        isRunning.set(false);
        isPaused.set(false);
        mainHandler.removeCallbacksAndMessages(null);
        notifyExecutionCompleted(false);
    }

    // Notification methods
    private void notifyExecutionStarted() {
        mainHandler.post(() -> {
            callback.onExecutionStarted(currentTask);
            NotificationUtils.showTaskNotification(application, currentTask, "Starting execution...", 0);
        });
    }

    private void notifyStepStarted(Step step) {
        mainHandler.post(() -> {
            int progress = (currentStepIndex * 100) / steps.size();
            callback.onStepStarted(step, currentStepIndex + 1, steps.size());
            NotificationUtils.updateTaskProgress(application, currentTask, step.getSummary(), progress);
        });
    }

    private void notifyStepCompleted(Step step, boolean success) {
        mainHandler.post(() -> callback.onStepCompleted(step, success));
    }

    private void notifyExecutionPaused() {
        mainHandler.post(() -> {
            callback.onExecutionPaused(currentTask);
            NotificationUtils.showTaskNotification(application, currentTask, "Execution paused", -1);
        });
    }

    private void notifyExecutionResumed() {
        mainHandler.post(() -> {
            callback.onExecutionResumed(currentTask);
            NotificationUtils.showTaskNotification(application, currentTask, "Execution resumed", -1);
        });
    }

    private void notifyExecutionCompleted(boolean success) {
        mainHandler.post(() -> {
            callback.onExecutionCompleted(currentTask, success);
            String message = success ? "Execution completed successfully" : "Execution failed";
            NotificationUtils.showTaskNotification(application, currentTask, message, 100);
        });
    }

    private void notifyError(String error) {
        mainHandler.post(() -> {
            callback.onExecutionError(error);
            NotificationUtils.showErrorNotification(application, error);
        });
    }

    /**
     * Check if a task is currently running
     */
    public boolean isRunning() {
        return isRunning.get();
    }

    /**
     * Check if execution is paused
     */
    public boolean isPaused() {
        return isPaused.get();
    }

    /**
     * Get the current task being executed
     */
    @Nullable
    public Task getCurrentTask() {
        return currentTask;
    }

    /**
     * Get the current step index
     */
    public int getCurrentStepIndex() {
        return currentStepIndex;
    }

    /**
     * Get the current repeat count
     */
    public int getCurrentRepeatCount() {
        return currentRepeatCount;
    }

    /**
     * Get execution progress as percentage
     */
    public int getProgress() {
        if (steps == null || steps.isEmpty()) return 0;
        return (currentStepIndex * 100) / steps.size();
    }

    /**
     * Get time elapsed since last step execution
     */
    public long getTimeSinceLastStep() {
        return System.currentTimeMillis() - lastStepTime;
    }
}
