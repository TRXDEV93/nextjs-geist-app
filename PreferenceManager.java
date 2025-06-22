package com.thebluecode.trxautophone;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PreferenceManager {
    private static final String KEY_FIRST_RUN = "first_run";
    private static final String KEY_LAST_TASK_ID = "last_task_id";
    private static final String KEY_RECENT_TASKS = "recent_tasks";
    private static final String KEY_DEFAULT_DELAY = "default_delay";
    private static final String KEY_AUTO_START_ENABLED = "auto_start_enabled";
    private static final String KEY_VIBRATION_ENABLED = "vibration_enabled";
    private static final String KEY_NOTIFICATION_ENABLED = "notification_enabled";
    private static final String KEY_DARK_MODE = "dark_mode";
    private static final String KEY_COORDINATE_HISTORY = "coordinate_history";
    private static final String KEY_TEXT_SEARCH_HISTORY = "text_search_history";
    private static final String KEY_CUSTOM_PRESETS = "custom_presets";

    private final SharedPreferences preferences;
    private final Gson gson;

    public PreferenceManager(@NonNull Context context) {
        preferences = getDefaultSharedPreferences(context);
        gson = new Gson();
    }

    // First Run Check
    public boolean isFirstRun() {
        boolean isFirst = preferences.getBoolean(KEY_FIRST_RUN, true);
        if (isFirst) {
            preferences.edit().putBoolean(KEY_FIRST_RUN, false).apply();
        }
        return isFirst;
    }

    // Last Used Task
    public void setLastTaskId(long taskId) {
        preferences.edit().putLong(KEY_LAST_TASK_ID, taskId).apply();
    }

    public long getLastTaskId() {
        return preferences.getLong(KEY_LAST_TASK_ID, -1);
    }

    // Recent Tasks
    public void addRecentTask(long taskId) {
        Set<String> recentTasks = preferences.getStringSet(KEY_RECENT_TASKS, new HashSet<>());
        Set<String> updatedTasks = new HashSet<>(recentTasks);
        updatedTasks.add(String.valueOf(taskId));
        
        // Keep only last 10 tasks
        if (updatedTasks.size() > 10) {
            List<String> tasksList = new ArrayList<>(updatedTasks);
            updatedTasks = new HashSet<>(tasksList.subList(tasksList.size() - 10, tasksList.size()));
        }
        
        preferences.edit().putStringSet(KEY_RECENT_TASKS, updatedTasks).apply();
    }

    public List<Long> getRecentTasks() {
        Set<String> recentTasks = preferences.getStringSet(KEY_RECENT_TASKS, new HashSet<>());
        List<Long> taskIds = new ArrayList<>();
        for (String taskId : recentTasks) {
            try {
                taskIds.add(Long.parseLong(taskId));
            } catch (NumberFormatException e) {
                // Skip invalid IDs
            }
        }
        return taskIds;
    }

    // Default Delay
    public void setDefaultDelay(long delay) {
        preferences.edit().putLong(KEY_DEFAULT_DELAY, delay).apply();
    }

    public long getDefaultDelay() {
        return preferences.getLong(KEY_DEFAULT_DELAY, 1000); // Default 1 second
    }

    // Auto Start
    public void setAutoStartEnabled(boolean enabled) {
        preferences.edit().putBoolean(KEY_AUTO_START_ENABLED, enabled).apply();
    }

    public boolean isAutoStartEnabled() {
        return preferences.getBoolean(KEY_AUTO_START_ENABLED, false);
    }

    // Vibration
    public void setVibrationEnabled(boolean enabled) {
        preferences.edit().putBoolean(KEY_VIBRATION_ENABLED, enabled).apply();
    }

    public boolean isVibrationEnabled() {
        return preferences.getBoolean(KEY_VIBRATION_ENABLED, true);
    }

    // Notifications
    public void setNotificationEnabled(boolean enabled) {
        preferences.edit().putBoolean(KEY_NOTIFICATION_ENABLED, enabled).apply();
    }

    public boolean isNotificationEnabled() {
        return preferences.getBoolean(KEY_NOTIFICATION_ENABLED, true);
    }

    // Dark Mode
    public void setDarkMode(boolean enabled) {
        preferences.edit().putBoolean(KEY_DARK_MODE, enabled).apply();
    }

    public boolean isDarkModeEnabled() {
        return preferences.getBoolean(KEY_DARK_MODE, false);
    }

    // Coordinate History
    public void addCoordinateHistory(String coordinates) {
        List<String> history = getCoordinateHistory();
        if (!history.contains(coordinates)) {
            history.add(0, coordinates);
            if (history.size() > 10) {
                history = history.subList(0, 10);
            }
            preferences.edit().putString(KEY_COORDINATE_HISTORY, gson.toJson(history)).apply();
        }
    }

    public List<String> getCoordinateHistory() {
        String json = preferences.getString(KEY_COORDINATE_HISTORY, null);
        if (json == null) {
            return new ArrayList<>();
        }
        Type type = new TypeToken<List<String>>(){}.getType();
        return gson.fromJson(json, type);
    }

    // Text Search History
    public void addTextSearchHistory(String text) {
        List<String> history = getTextSearchHistory();
        if (!history.contains(text)) {
            history.add(0, text);
            if (history.size() > 10) {
                history = history.subList(0, 10);
            }
            preferences.edit().putString(KEY_TEXT_SEARCH_HISTORY, gson.toJson(history)).apply();
        }
    }

    public List<String> getTextSearchHistory() {
        String json = preferences.getString(KEY_TEXT_SEARCH_HISTORY, null);
        if (json == null) {
            return new ArrayList<>();
        }
        Type type = new TypeToken<List<String>>(){}.getType();
        return gson.fromJson(json, type);
    }

    // Custom Presets
    public void saveCustomPresets(List<String> presets) {
        preferences.edit().putString(KEY_CUSTOM_PRESETS, gson.toJson(presets)).apply();
    }

    public List<String> getCustomPresets() {
        String json = preferences.getString(KEY_CUSTOM_PRESETS, null);
        if (json == null) {
            return new ArrayList<>();
        }
        Type type = new TypeToken<List<String>>(){}.getType();
        return gson.fromJson(json, type);
    }

    // Clear All Preferences
    public void clearAll() {
        preferences.edit().clear().apply();
    }
}
