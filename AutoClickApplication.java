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

    private void initialize() {
        // Initialize executor service for background tasks
        executorService = Executors.newFixedThreadPool(4);
        
        // Initialize main thread handler
        mainHandler = new Handler(Looper.getMainLooper());
        
        // Initialize preferences
        preferenceManager = new PreferenceManager(this);
        
        // Add database callback for logging
        TaskDatabase.addDatabaseCallback(new RoomDatabase.Callback() {
            @Override
            public void onOpen(SupportSQLiteDatabase db) {
                Log.d(TAG, "Database opened");
            }
        });
        
        // Initialize database
        database = TaskDatabase.getInstance(this);
        
        // Create notification channel
        NotificationUtils.createNotificationChannel(this);
        
        // Perform first run tasks if needed
        if (preferenceManager.isFirstRun()) {
            onFirstRun();
        }
    }

    private void onFirstRun() {
        Log.d(TAG, "First run initialization");
        executorService.execute(() -> {
            // Perform any first-run database initialization
            // Create default tasks, etc.
        });
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        executorService.shutdown();
    }

    public static AutoClickApplication getInstance() {
        return instance;
    }

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
     * Execute a task on the background thread
     */
    public void executeAsync(Runnable runnable) {
        executorService.execute(runnable);
    }

    /**
     * Execute a task on the main thread
     */
    public void executeOnMainThread(Runnable runnable) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            runnable.run();
        } else {
            mainHandler.post(runnable);
        }
    }

    /**
     * Execute a task on the main thread with delay
     */
    public void executeOnMainThreadDelayed(Runnable runnable, long delayMillis) {
        mainHandler.postDelayed(runnable, delayMillis);
    }

    /**
     * Remove pending main thread tasks
     */
    public void removeMainThreadCallbacks(Runnable runnable) {
        mainHandler.removeCallbacks(runnable);
    }

    /**
     * Clear all pending main thread tasks
     */
    public void clearMainThreadCallbacks() {
        mainHandler.removeCallbacksAndMessages(null);
    }
}
