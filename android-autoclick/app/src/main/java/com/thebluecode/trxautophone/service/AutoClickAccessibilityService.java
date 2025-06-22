package com.thebluecode.trxautophone.service;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Intent;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import androidx.annotation.NonNull;

import com.thebluecode.trxautophone.AutoClickApplication;
import com.thebluecode.trxautophone.executor.TaskExecutor;
import com.thebluecode.trxautophone.models.Step;
import com.thebluecode.trxautophone.models.Task;
import com.thebluecode.trxautophone.utils.Constants;
import com.thebluecode.trxautophone.utils.NotificationUtils;

/**
 * Enhanced Accessibility Service with improved error handling and execution monitoring
 */
public class AutoClickAccessibilityService extends AccessibilityService {
    private static final String TAG = "AutoClickService";
    private static AutoClickAccessibilityService instance;
    private TaskExecutor taskExecutor;
    private boolean isInitialized = false;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Service created");
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        instance = this;
        
        try {
            configureService();
            initializeExecutor();
            isInitialized = true;
            NotificationUtils.showServiceNotification(
                (AutoClickApplication) getApplication(),
                "Service is running",
                "Tap to open app"
            );
            Log.i(TAG, "Service connected and initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing service", e);
            isInitialized = false;
        }
    }

    /**
     * Configure service capabilities
     */
    private void configureService() {
        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        
        // Set flags for enhanced capabilities
        info.flags = AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS |
                    AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS |
                    AccessibilityServiceInfo.FLAG_REQUEST_ENHANCED_WEB_ACCESSIBILITY |
                    AccessibilityServiceInfo.FLAG_REQUEST_TOUCH_EXPLORATION_MODE;
        
        // Set feedback type
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
        
        // Set event types to monitor
        info.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED |
                         AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED |
                         AccessibilityEvent.TYPE_VIEW_CLICKED |
                         AccessibilityEvent.TYPE_VIEW_FOCUSED;
        
        // Set notification timeout
        info.notificationTimeout = 100;
        
        setServiceInfo(info);
        Log.d(TAG, "Service configured with enhanced capabilities");
    }

    /**
     * Initialize task executor
     */
    private void initializeExecutor() {
        taskExecutor = new TaskExecutor(
            (AutoClickApplication) getApplication(),
            this,
            new TaskExecutor.ExecutionCallback() {
                @Override
                public void onExecutionStarted(Task task) {
                    Log.i(TAG, "Started executing task: " + task.getName());
                }

                @Override
                public void onStepStarted(Step step, int position, int total) {
                    Log.d(TAG, String.format("Executing step %d/%d: %s", 
                        position, total, step.getSummary()));
                }

                @Override
                public void onStepCompleted(Step step, boolean success) {
                    Log.d(TAG, String.format("Step completed: %s, success: %b", 
                        step.getSummary(), success));
                }

                @Override
                public void onExecutionPaused(Task task) {
                    Log.i(TAG, "Task execution paused: " + task.getName());
                }

                @Override
                public void onExecutionResumed(Task task) {
                    Log.i(TAG, "Task execution resumed: " + task.getName());
                }

                @Override
                public void onExecutionCompleted(Task task, boolean success) {
                    Log.i(TAG, String.format("Task execution completed: %s, success: %b", 
                        task.getName(), success));
                }

                @Override
                public void onExecutionError(String error) {
                    Log.e(TAG, "Execution error: " + error);
                }
            }
        );
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (!isInitialized) {
            Log.w(TAG, "Service not fully initialized, ignoring event");
            return;
        }

        try {
            int eventType = event.getEventType();
            switch (eventType) {
                case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                    handleWindowStateChanged(event);
                    break;
                    
                case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED:
                    handleWindowContentChanged(event);
                    break;
                    
                case AccessibilityEvent.TYPE_VIEW_CLICKED:
                    handleViewClicked(event);
                    break;
                    
                case AccessibilityEvent.TYPE_VIEW_FOCUSED:
                    handleViewFocused(event);
                    break;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error handling accessibility event", e);
        }
    }

    /**
     * Handle window state changes
     */
    private void handleWindowStateChanged(AccessibilityEvent event) {
        if (event.getPackageName() != null) {
            Log.d(TAG, "Window changed: " + event.getPackageName());
        }
    }

    /**
     * Handle window content changes
     */
    private void handleWindowContentChanged(AccessibilityEvent event) {
        // Monitor content changes if needed
    }

    /**
     * Handle view click events
     */
    private void handleViewClicked(AccessibilityEvent event) {
        // Monitor click events if needed
    }

    /**
     * Handle view focus events
     */
    private void handleViewFocused(AccessibilityEvent event) {
        // Monitor focus events if needed
    }

    @Override
    public void onInterrupt() {
        Log.w(TAG, "Service interrupted");
        if (taskExecutor != null && taskExecutor.isRunning()) {
            taskExecutor.stopExecution();
        }
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.i(TAG, "Service unbound");
        instance = null;
        isInitialized = false;
        return super.onUnbind(intent);
    }

    /**
     * Start executing a task
     */
    public void startTask(@NonNull Task task) {
        if (!isInitialized) {
            Log.e(TAG, "Cannot start task, service not initialized");
            return;
        }

        if (taskExecutor.isRunning()) {
            Log.w(TAG, "Cannot start task, another task is already running");
            return;
        }

        taskExecutor.executeTask(task);
    }

    /**
     * Stop the current task
     */
    public void stopTask() {
        if (taskExecutor != null && taskExecutor.isRunning()) {
            taskExecutor.stopExecution();
        }
    }

    /**
     * Pause the current task
     */
    public void pauseTask() {
        if (taskExecutor != null && taskExecutor.isRunning()) {
            taskExecutor.pauseExecution();
        }
    }

    /**
     * Resume the current task
     */
    public void resumeTask() {
        if (taskExecutor != null && taskExecutor.isPaused()) {
            taskExecutor.resumeExecution();
        }
    }

    /**
     * Get the current task being executed
     */
    public Task getCurrentTask() {
        return taskExecutor != null ? taskExecutor.getCurrentTask() : null;
    }

    /**
     * Check if a task is currently running
     */
    public boolean isTaskRunning() {
        return taskExecutor != null && taskExecutor.isRunning();
    }

    /**
     * Check if task execution is paused
     */
    public boolean isTaskPaused() {
        return taskExecutor != null && taskExecutor.isPaused();
    }

    /**
     * Get current execution progress
     */
    public int getExecutionProgress() {
        return taskExecutor != null ? taskExecutor.getProgress() : 0;
    }

    /**
     * Get singleton instance
     */
    public static AutoClickAccessibilityService getInstance() {
        return instance;
    }

    /**
     * Check if service is ready
     */
    public static boolean isServiceReady() {
        return instance != null && instance.isInitialized;
    }

    /**
     * Handle system actions
     */
    public boolean performSystemAction(int action) {
        if (!isInitialized) {
            Log.e(TAG, "Cannot perform action, service not initialized");
            return false;
        }

        try {
            return performGlobalAction(action);
        } catch (Exception e) {
            Log.e(TAG, "Error performing system action: " + action, e);
            return false;
        }
    }

    /**
     * Get the root node in active window
     */
    @Override
    public AccessibilityNodeInfo getRootInActiveWindow() {
        try {
            AccessibilityNodeInfo root = super.getRootInActiveWindow();
            if (root == null) {
                Log.w(TAG, "No active window found");
            }
            return root;
        } catch (Exception e) {
            Log.e(TAG, "Error getting root node", e);
            return null;
        }
    }

    /**
     * Clean up resources
     */
    @Override
    public void onDestroy() {
        Log.i(TAG, "Service being destroyed");
        if (taskExecutor != null && taskExecutor.isRunning()) {
            taskExecutor.stopExecution();
        }
        instance = null;
        isInitialized = false;
        super.onDestroy();
    }
}
