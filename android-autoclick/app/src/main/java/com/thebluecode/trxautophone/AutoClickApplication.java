package com.thebluecode.trxautophone;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.thebluecode.trxautophone.database.TaskDatabase;
import com.thebluecode.trxautophone.utils.NotificationUtils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Main Application class that initializes core components and manages application lifecycle.
 * Provides centralized access to executors, handlers, preferences, and database.
 */
public class AutoClickApplication extends Application {
    private static final String TAG = "AutoClickApplication";
    private static AutoClickApplication instance;
    private ExecutorService executorService;
    private Handler mainHandler;
    private PreferenceManager preferenceManager;
    private TaskDatabase database;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        initialize();
    }

    /**
     * Initialize all core components with proper error handling
     */
    private void initialize() {
        try {
            // Initialize executor service for background tasks
            executorService = Executors.newFixedThreadPool(4);
            Log.d(TAG, "Executor service initialized with 4 threads");
            
            // Initialize main thread handler
            mainHandler = new Handler(Looper.getMainLooper());
            Log.d(TAG, "Main thread handler initialized");
            
            // Initialize preferences
            preferenceManager = new PreferenceManager(this);
            Log.d(TAG, "Preference manager initialized");
            
            // Add database callback for logging and error handling
            TaskDatabase.addDatabaseCallback(new RoomDatabase.Callback() {
                @Override
                public void onOpen(SupportSQLiteDatabase db) {
                    Log.d(TAG, "Database opened successfully");
                }

                @Override
                public void onCreate(SupportSQLiteDatabase db) {
                    Log.d(TAG, "Database created successfully");
                }
            });
            
            // Initialize database with error handling
            database = TaskDatabase.getInstance(this);
            Log.d(TAG, "Database initialized");
            
            // Create notification channel
            NotificationUtils.createNotificationChannel(this);
            Log.d(TAG, "Notification channel created");
            
            // Perform first run tasks if needed
            if (preferenceManager.isFirstRun()) {
                onFirstRun();
            }
            
            Log.i(TAG, "Application initialization completed successfully");
            
        } catch (Exception e) {
            Log.e(TAG, "Error during application initialization", e);
            // Don't crash the app, but log the error for debugging
        }
    }

    /**
     * Handle first run initialization tasks
     */
    private void onFirstRun() {
        Log.d(TAG, "First run initialization started");
        executeAsync(() -> {
            try {
                // Perform any first-run database initialization
                // Create default tasks, sample data, etc.
                Log.d(TAG, "First run database setup completed");
            } catch (Exception e) {
                Log.e(TAG, "Error during first run initialization", e);
            }
        });
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        try {
            if (executorService != null && !executorService.isShutdown()) {
                executorService.shutdown();
                Log.d(TAG, "Executor service shutdown completed");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error during application termination", e);
        }
    }

    // Singleton access
    public static AutoClickApplication getInstance() {
        return instance;
    }

    // Component getters with null safety
    public ExecutorService getExecutorService() {
        return executorService;
    }

    public Handler getMainHandler() {
        return mainHandler;
    }

    public PreferenceManager getPreferenceManager() {
        return preferenceManager;
    }

    public TaskDatabase getDatabase() {
        return database;
    }

    /**
     * Execute a task on the background thread with error handling
     */
    public void executeAsync(Runnable runnable) {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.execute(() -> {
                try {
                    runnable.run();
                } catch (Exception e) {
                    Log.e(TAG, "Error executing async task", e);
                }
            });
        } else {
            Log.w(TAG, "Executor service not available for async execution");
        }
    }

    /**
     * Execute a task on the main thread with error handling
     */
    public void executeOnMainThread(Runnable runnable) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            try {
                runnable.run();
            } catch (Exception e) {
                Log.e(TAG, "Error executing on main thread", e);
            }
        } else {
            if (mainHandler != null) {
                mainHandler.post(() -> {
                    try {
                        runnable.run();
                    } catch (Exception e) {
                        Log.e(TAG, "Error executing posted task on main thread", e);
                    }
                });
            }
        }
    }

    /**
     * Execute a task on the main thread with delay and error handling
     */
    public void executeOnMainThreadDelayed(Runnable runnable, long delayMillis) {
        if (mainHandler != null) {
            mainHandler.postDelayed(() -> {
                try {
                    runnable.run();
                } catch (Exception e) {
                    Log.e(TAG, "Error executing delayed task on main thread", e);
                }
            }, delayMillis);
        }
    }

    /**
     * Remove pending main thread tasks
     */
    public void removeMainThreadCallbacks(Runnable runnable) {
        if (mainHandler != null) {
            mainHandler.removeCallbacks(runnable);
        }
    }

    /**
     * Clear all pending main thread tasks
     */
    public void clearMainThreadCallbacks() {
        if (mainHandler != null) {
            mainHandler.removeCallbacksAndMessages(null);
            Log.d(TAG, "All main thread callbacks cleared");
        }
    }

    /**
     * Check if the application is properly initialized
     */
    public boolean isInitialized() {
        return executorService != null && 
               mainHandler != null && 
               preferenceManager != null && 
               database != null;
    }

    /**
     * Get application health status for debugging
     */
    public String getHealthStatus() {
        StringBuilder status = new StringBuilder();
        status.append("ExecutorService: ").append(executorService != null ? "OK" : "NULL").append("\n");
        status.append("MainHandler: ").append(mainHandler != null ? "OK" : "NULL").append("\n");
        status.append("PreferenceManager: ").append(preferenceManager != null ? "OK" : "NULL").append("\n");
        status.append("Database: ").append(database != null ? "OK" : "NULL").append("\n");
        return status.toString();
    }
}
