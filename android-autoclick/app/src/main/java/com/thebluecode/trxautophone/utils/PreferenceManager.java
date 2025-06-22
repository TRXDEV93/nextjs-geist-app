package com.thebluecode.trxautophone.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Enhanced PreferenceManager with encryption and type safety
 */
public class PreferenceManager {
    private static final String TAG = "PreferenceManager";
    private static final String PREF_FILE_NAME = "autoclick_preferences";
    private static final String SECURE_PREF_FILE_NAME = "autoclick_secure_preferences";

    private final SharedPreferences preferences;
    private final SharedPreferences securePreferences;
    private final Gson gson;

    public PreferenceManager(@NonNull Context context) {
        // Initialize regular preferences
        preferences = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
        
        // Initialize encrypted preferences
        SharedPreferences encrypted = null;
        try {
            MasterKey masterKey = new MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build();

            encrypted = EncryptedSharedPreferences.create(
                context,
                SECURE_PREF_FILE_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (Exception e) {
            Log.e(TAG, "Error initializing encrypted preferences", e);
            // Fallback to regular preferences if encryption fails
            encrypted = context.getSharedPreferences(SECURE_PREF_FILE_NAME, Context.MODE_PRIVATE);
        }
        securePreferences = encrypted;
        
        gson = new Gson();
    }

    /**
     * Check if this is the first run
     */
    public boolean isFirstRun() {
        boolean isFirst = preferences.getBoolean(Constants.Preferences.KEY_FIRST_RUN, true);
        if (isFirst) {
            preferences.edit().putBoolean(Constants.Preferences.KEY_FIRST_RUN, false).apply();
        }
        return isFirst;
    }

    /**
     * Get/Set last task ID
     */
    public long getLastTaskId() {
        return preferences.getLong(Constants.Preferences.KEY_LAST_TASK_ID, -1);
    }

    public void setLastTaskId(long taskId) {
        preferences.edit().putLong(Constants.Preferences.KEY_LAST_TASK_ID, taskId).apply();
    }

    /**
     * Get/Set recent tasks
     */
    public List<Long> getRecentTasks() {
        String json = preferences.getString(Constants.Preferences.KEY_RECENT_TASKS, null);
        if (json != null) {
            try {
                return gson.fromJson(json, new TypeToken<List<Long>>(){}.getType());
            } catch (Exception e) {
                Log.e(TAG, "Error parsing recent tasks", e);
            }
        }
        return new ArrayList<>();
    }

    public void setRecentTasks(List<Long> taskIds) {
        try {
            String json = gson.toJson(taskIds);
            preferences.edit().putString(Constants.Preferences.KEY_RECENT_TASKS, json).apply();
        } catch (Exception e) {
            Log.e(TAG, "Error saving recent tasks", e);
        }
    }

    /**
     * Add task to recent list
     */
    public void addRecentTask(long taskId) {
        List<Long> recent = getRecentTasks();
        recent.remove(taskId); // Remove if exists
        recent.add(0, taskId); // Add to front
        
        // Trim list if needed
        while (recent.size() > Constants.Limits.MAX_RECENT_TASKS) {
            recent.remove(recent.size() - 1);
        }
        
        setRecentTasks(recent);
    }

    /**
     * Get/Set default delay
     */
    public long getDefaultDelay() {
        return preferences.getLong(Constants.Preferences.KEY_DEFAULT_DELAY, 
            Constants.Limits.DEFAULT_DELAY);
    }

    public void setDefaultDelay(long delay) {
        preferences.edit().putLong(Constants.Preferences.KEY_DEFAULT_DELAY, delay).apply();
    }

    /**
     * Get/Set auto start preference
     */
    public boolean isAutoStartEnabled() {
        return preferences.getBoolean(Constants.Preferences.KEY_AUTO_START, false);
    }

    public void setAutoStartEnabled(boolean enabled) {
        preferences.edit().putBoolean(Constants.Preferences.KEY_AUTO_START, enabled).apply();
    }

    /**
     * Get/Set vibration preference
     */
    public boolean isVibrationEnabled() {
        return preferences.getBoolean(Constants.Preferences.KEY_VIBRATION, true);
    }

    public void setVibrationEnabled(boolean enabled) {
        preferences.edit().putBoolean(Constants.Preferences.KEY_VIBRATION, enabled).apply();
    }

    /**
     * Get/Set notifications preference
     */
    public boolean areNotificationsEnabled() {
        return preferences.getBoolean(Constants.Preferences.KEY_NOTIFICATIONS, true);
    }

    public void setNotificationsEnabled(boolean enabled) {
        preferences.edit().putBoolean(Constants.Preferences.KEY_NOTIFICATIONS, enabled).apply();
    }

    /**
     * Get/Set dark mode preference
     */
    public boolean isDarkModeEnabled() {
        return preferences.getBoolean(Constants.Preferences.KEY_DARK_MODE, false);
    }

    public void setDarkModeEnabled(boolean enabled) {
        preferences.edit().putBoolean(Constants.Preferences.KEY_DARK_MODE, enabled).apply();
    }

    /**
     * Get/Set coordinate history
     */
    public Set<String> getCoordinateHistory() {
        return preferences.getStringSet(Constants.Preferences.KEY_COORDINATE_HISTORY, 
            new HashSet<>());
    }

    public void addCoordinateHistory(String coordinate) {
        Set<String> history = new HashSet<>(getCoordinateHistory());
        history.add(coordinate);
        preferences.edit().putStringSet(Constants.Preferences.KEY_COORDINATE_HISTORY, history).apply();
    }

    public void clearCoordinateHistory() {
        preferences.edit().remove(Constants.Preferences.KEY_COORDINATE_HISTORY).apply();
    }

    /**
     * Get/Set text search history
     */
    public List<String> getTextSearchHistory() {
        String json = preferences.getString(Constants.Preferences.KEY_TEXT_SEARCH_HISTORY, null);
        if (json != null) {
            try {
                return gson.fromJson(json, new TypeToken<List<String>>(){}.getType());
            } catch (Exception e) {
                Log.e(TAG, "Error parsing text search history", e);
            }
        }
        return new ArrayList<>();
    }

    public void addTextSearchHistory(String text) {
        List<String> history = new ArrayList<>(getTextSearchHistory());
        history.remove(text); // Remove if exists
        history.add(0, text); // Add to front
        
        // Trim list if needed
        while (history.size() > 20) {
            history.remove(history.size() - 1);
        }
        
        try {
            String json = gson.toJson(history);
            preferences.edit().putString(Constants.Preferences.KEY_TEXT_SEARCH_HISTORY, json).apply();
        } catch (Exception e) {
            Log.e(TAG, "Error saving text search history", e);
        }
    }

    public void clearTextSearchHistory() {
        preferences.edit().remove(Constants.Preferences.KEY_TEXT_SEARCH_HISTORY).apply();
    }

    /**
     * Get/Set custom presets
     */
    public String getCustomPresets() {
        return preferences.getString(Constants.Preferences.KEY_CUSTOM_PRESETS, "[]");
    }

    public void setCustomPresets(String presetsJson) {
        preferences.edit().putString(Constants.Preferences.KEY_CUSTOM_PRESETS, presetsJson).apply();
    }

    /**
     * Get/Set execution log settings
     */
    public boolean isExecutionLogEnabled() {
        return preferences.getBoolean(Constants.Preferences.KEY_EXECUTION_LOG_ENABLED, true);
    }

    public void setExecutionLogEnabled(boolean enabled) {
        preferences.edit().putBoolean(Constants.Preferences.KEY_EXECUTION_LOG_ENABLED, enabled).apply();
    }

    public long getExecutionLogMaxSize() {
        return preferences.getLong(Constants.Preferences.KEY_EXECUTION_LOG_MAX_SIZE, 
            10 * 1024 * 1024); // 10MB default
    }

    public void setExecutionLogMaxSize(long maxSize) {
        preferences.edit().putLong(Constants.Preferences.KEY_EXECUTION_LOG_MAX_SIZE, maxSize).apply();
    }

    /**
     * Secure storage methods
     */
    public void setSecureString(@NonNull String key, @NonNull String value) {
        securePreferences.edit().putString(key, value).apply();
    }

    @Nullable
    public String getSecureString(@NonNull String key) {
        return securePreferences.getString(key, null);
    }

    public void removeSecureValue(@NonNull String key) {
        securePreferences.edit().remove(key).apply();
    }

    /**
     * Clear all preferences
     */
    public void clearAll() {
        preferences.edit().clear().apply();
        securePreferences.edit().clear().apply();
    }

    /**
     * Clear specific preference category
     */
    public void clearCategory(@NonNull String category) {
        SharedPreferences.Editor editor = preferences.edit();
        for (String key : preferences.getAll().keySet()) {
            if (key.startsWith(category)) {
                editor.remove(key);
            }
        }
        editor.apply();
    }

    /**
     * Export preferences to JSON
     */
    public String exportPreferences() {
        try {
            Map<String, ?> all = preferences.getAll();
            return gson.toJson(all);
        } catch (Exception e) {
            Log.e(TAG, "Error exporting preferences", e);
            return null;
        }
    }

    /**
     * Import preferences from JSON
     */
    public boolean importPreferences(String json) {
        try {
            Map<String, ?> data = gson.fromJson(json, 
                new TypeToken<Map<String, ?>>(){}.getType());
            
            SharedPreferences.Editor editor = preferences.edit();
            for (Map.Entry<String, ?> entry : data.entrySet()) {
                Object value = entry.getValue();
                if (value instanceof String) {
                    editor.putString(entry.getKey(), (String) value);
                } else if (value instanceof Boolean) {
                    editor.putBoolean(entry.getKey(), (Boolean) value);
                } else if (value instanceof Integer) {
                    editor.putInt(entry.getKey(), (Integer) value);
                } else if (value instanceof Long) {
                    editor.putLong(entry.getKey(), (Long) value);
                } else if (value instanceof Float) {
                    editor.putFloat(entry.getKey(), (Float) value);
                }
            }
            editor.apply();
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error importing preferences", e);
            return false;
        }
    }
}
