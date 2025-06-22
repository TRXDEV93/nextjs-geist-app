package com.thebluecode.trxautophone.utils;

import android.accessibilityservice.AccessibilityService;

public final class Constants {
    private Constants() {
        // Private constructor to prevent instantiation
    }

    // Database
    public static final String DATABASE_NAME = "autoclick_database";
    public static final int DATABASE_VERSION = 1;

    // Task Defaults
    public static final long DEFAULT_DELAY = 1000; // 1 second
    public static final long MIN_DELAY = 100; // 100 milliseconds
    public static final long MAX_DELAY = 60000; // 60 seconds
    public static final int MAX_STEPS = 100;
    public static final int MAX_RECENT_TASKS = 10;

    // Step Types
    public static final class StepTypes {
        public static final String SYSTEM_KEY = "system_key";
        public static final String TAP = "tap";
        public static final String LONG_PRESS = "long_press";
        public static final String SWIPE = "swipe";
        public static final String TEXT_SEARCH = "text_search";
        public static final String IMAGE_SEARCH = "image_search";
        public static final String DELAY = "delay";
        public static final String CONDITION = "condition";
    }

    // System Actions
    public static final class SystemActions {
        public static final int BACK = AccessibilityService.GLOBAL_ACTION_BACK;
        public static final int HOME = AccessibilityService.GLOBAL_ACTION_HOME;
        public static final int RECENTS = AccessibilityService.GLOBAL_ACTION_RECENTS;
        public static final int NOTIFICATIONS = AccessibilityService.GLOBAL_ACTION_NOTIFICATIONS;
        public static final int QUICK_SETTINGS = AccessibilityService.GLOBAL_ACTION_QUICK_SETTINGS;
    }

    // Intent Actions
    public static final class IntentActions {
        public static final String ACTION_START_TASK = "com.thebluecode.trxautophone.action.START_TASK";
        public static final String ACTION_STOP_TASK = "com.thebluecode.trxautophone.action.STOP_TASK";
        public static final String ACTION_PAUSE_TASK = "com.thebluecode.trxautophone.action.PAUSE_TASK";
        public static final String ACTION_RESUME_TASK = "com.thebluecode.trxautophone.action.RESUME_TASK";
    }

    // Intent Extras
    public static final class IntentExtras {
        public static final String EXTRA_TASK_ID = "task_id";
        public static final String EXTRA_STEP_ID = "step_id";
        public static final String EXTRA_POSITION = "position";
    }

    // Notification
    public static final class Notification {
        public static final String CHANNEL_ID = "autoclick_service";
        public static final String CHANNEL_NAME = "AutoClick Service";
        public static final int SERVICE_NOTIFICATION_ID = 1001;
        public static final int TASK_NOTIFICATION_ID = 1002;
        public static final int ERROR_NOTIFICATION_ID = 1003;
    }

    // File Operations
    public static final class Files {
        public static final String EXPORT_DIR = "AutoClick/exports";
        public static final String TASKS_DIR = "tasks";
        public static final String FILE_EXTENSION = ".json";
        public static final long MAX_FILE_AGE = 7 * 24 * 60 * 60 * 1000; // 7 days
    }

    // Preferences
    public static final class Preferences {
        public static final String KEY_FIRST_RUN = "first_run";
        public static final String KEY_LAST_TASK_ID = "last_task_id";
        public static final String KEY_RECENT_TASKS = "recent_tasks";
        public static final String KEY_DEFAULT_DELAY = "default_delay";
        public static final String KEY_AUTO_START = "auto_start";
        public static final String KEY_VIBRATION = "vibration";
        public static final String KEY_NOTIFICATIONS = "notifications";
        public static final String KEY_DARK_MODE = "dark_mode";
    }

    // Request Codes
    public static final class RequestCodes {
        public static final int PERMISSION_REQUEST = 1001;
        public static final int OVERLAY_PERMISSION_REQUEST = 1002;
        public static final int FILE_PICKER_REQUEST = 1003;
        public static final int EDIT_TASK_REQUEST = 1004;
    }

    // Result Codes
    public static final class ResultCodes {
        public static final int TASK_CREATED = 101;
        public static final int TASK_UPDATED = 102;
        public static final int TASK_DELETED = 103;
    }

    // Time Constants
    public static final class Time {
        public static final long SECOND = 1000;
        public static final long MINUTE = 60 * SECOND;
        public static final long HOUR = 60 * MINUTE;
        public static final long DAY = 24 * HOUR;
    }

    // Screen Metrics
    public static final class Screen {
        public static final int MIN_CLICK_TARGET = 48; // dp
        public static final int MIN_TOUCH_SLOP = 8; // dp
        public static final int DEFAULT_LONG_PRESS_TIMEOUT = 500; // ms
    }

    // Error Messages
    public static final class Errors {
        public static final String SERVICE_NOT_RUNNING = "Accessibility service not running";
        public static final String INVALID_COORDINATES = "Invalid coordinates";
        public static final String INVALID_DELAY = "Invalid delay value";
        public static final String TASK_NOT_FOUND = "Task not found";
        public static final String STEP_NOT_FOUND = "Step not found";
        public static final String IMPORT_ERROR = "Error importing task";
        public static final String EXPORT_ERROR = "Error exporting task";
    }
}
