package com.thebluecode.trxautophone.utils;

/**
 * Enhanced constants class with organized categories
 */
public final class Constants {
    private Constants() {
        // Private constructor to prevent instantiation
    }

    /**
     * Database related constants
     */
    public static final class Database {
        public static final String NAME = "autoclick.db";
        public static final String BACKUP_SUFFIX = ".backup";
        public static final int VERSION = 1;
        public static final long MAX_DATABASE_SIZE = 50 * 1024 * 1024; // 50MB
    }

    /**
     * Notification related constants
     */
    public static final class Notification {
        public static final String CHANNEL_ID = "autoclick_service";
        public static final String CHANNEL_NAME = "AutoClick Service";
        public static final String CHANNEL_DESCRIPTION = "Notifications for AutoClick service";
        public static final int SERVICE_NOTIFICATION_ID = 1001;
        public static final int TASK_NOTIFICATION_ID = 1002;
        public static final int ERROR_NOTIFICATION_ID = 1003;
        public static final int PROGRESS_NOTIFICATION_ID = 1004;
    }

    /**
     * Preference keys
     */
    public static final class Preferences {
        public static final String KEY_FIRST_RUN = "first_run";
        public static final String KEY_LAST_TASK_ID = "last_task_id";
        public static final String KEY_RECENT_TASKS = "recent_tasks";
        public static final String KEY_DEFAULT_DELAY = "default_delay";
        public static final String KEY_AUTO_START = "auto_start";
        public static final String KEY_VIBRATION = "vibration";
        public static final String KEY_NOTIFICATIONS = "notifications";
        public static final String KEY_DARK_MODE = "dark_mode";
        public static final String KEY_COORDINATE_HISTORY = "coordinate_history";
        public static final String KEY_TEXT_SEARCH_HISTORY = "text_search_history";
        public static final String KEY_CUSTOM_PRESETS = "custom_presets";
        public static final String KEY_EXECUTION_LOG_ENABLED = "execution_log_enabled";
        public static final String KEY_EXECUTION_LOG_MAX_SIZE = "execution_log_max_size";
    }

    /**
     * Intent actions
     */
    public static final class IntentActions {
        public static final String ACTION_START_TASK = "com.thebluecode.trxautophone.START_TASK";
        public static final String ACTION_STOP_TASK = "com.thebluecode.trxautophone.STOP_TASK";
        public static final String ACTION_PAUSE_TASK = "com.thebluecode.trxautophone.PAUSE_TASK";
        public static final String ACTION_RESUME_TASK = "com.thebluecode.trxautophone.RESUME_TASK";
    }

    /**
     * Request codes
     */
    public static final class RequestCodes {
        public static final int ACCESSIBILITY_PERMISSION = 1001;
        public static final int OVERLAY_PERMISSION = 1002;
        public static final int IMPORT_TASK = 1003;
        public static final int EXPORT_TASK = 1004;
    }

    /**
     * Limits and thresholds
     */
    public static final class Limits {
        public static final int MAX_TASKS = 100;
        public static final int MAX_STEPS_PER_TASK = 50;
        public static final int MAX_RECENT_TASKS = 10;
        public static final int MAX_TASK_NAME_LENGTH = 100;
        public static final int MAX_DESCRIPTION_LENGTH = 500;
        public static final int MAX_REPEAT_COUNT = 999;
        public static final long MAX_REPEAT_DELAY = 3600000; // 1 hour
        public static final long MAX_DELAY = 3600000; // 1 hour
        public static final long MIN_DELAY = 0;
        public static final long DEFAULT_DELAY = 1000; // 1 second
        public static final long DEFAULT_LONG_PRESS_DURATION = 500; // 0.5 seconds
        public static final long DEFAULT_SWIPE_DURATION = 300; // 0.3 seconds
        public static final int MAX_RETRIES = 3;
        public static final long RETRY_DELAY = 1000; // 1 second
    }

    /**
     * UI related constants
     */
    public static final class UI {
        public static final int ANIMATION_DURATION = 300;
        public static final int SNACKBAR_DURATION = 3000;
        public static final float MIN_CLICK_AREA = 48; // dp
        public static final int LIST_PAGE_SIZE = 20;
        public static final int SEARCH_DELAY = 300;
    }

    /**
     * Error messages
     */
    public static final class ErrorMessages {
        public static final String SERVICE_NOT_RUNNING = "Accessibility service is not running";
        public static final String INVALID_STEP = "Invalid step configuration";
        public static final String TASK_NOT_FOUND = "Task not found";
        public static final String PERMISSION_REQUIRED = "Required permission not granted";
        public static final String EXECUTION_ERROR = "Error executing task";
        public static final String DATABASE_ERROR = "Database error";
    }

    /**
     * File extensions and MIME types
     */
    public static final class Files {
        public static final String TASK_EXTENSION = ".task";
        public static final String JSON_EXTENSION = ".json";
        public static final String LOG_EXTENSION = ".log";
        public static final String MIME_TYPE_JSON = "application/json";
        public static final String MIME_TYPE_TEXT = "text/plain";
    }

    /**
     * Time constants
     */
    public static final class Time {
        public static final long SECOND = 1000;
        public static final long MINUTE = 60 * SECOND;
        public static final long HOUR = 60 * MINUTE;
        public static final long DAY = 24 * HOUR;
        public static final long WEEK = 7 * DAY;
    }

    /**
     * Default values
     */
    public static final class Defaults {
        public static final int REPEAT_COUNT = 1;
        public static final long REPEAT_DELAY = 1000;
        public static final long STEP_DELAY = 500;
        public static final long LONG_PRESS_DURATION = 500;
        public static final long SWIPE_DURATION = 300;
        public static final boolean AUTO_START = false;
        public static final boolean VIBRATION = true;
        public static final boolean NOTIFICATIONS = true;
        public static final boolean DARK_MODE = false;
    }

    /**
     * Log tags
     */
    public static final class Tags {
        public static final String APP = "AutoClick";
        public static final String SERVICE = "AutoClickService";
        public static final String EXECUTOR = "TaskExecutor";
        public static final String DATABASE = "Database";
        public static final String ACCESSIBILITY = "Accessibility";
    }
}
