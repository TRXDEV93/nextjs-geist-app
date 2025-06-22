package com.thebluecode.trxautophone.viewmodels;

import android.app.Application;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.thebluecode.trxautophone.AutoClickApplication;
import com.thebluecode.trxautophone.database.TaskDao;
import com.thebluecode.trxautophone.models.Task;
import com.thebluecode.trxautophone.utils.PreferenceManager;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * Enhanced ViewModel with improved error handling and task management
 */
public class MainViewModel extends AndroidViewModel {
    private static final String TAG = "MainViewModel";

    private final TaskDao taskDao;
    private final ExecutorService executor;
    private final PreferenceManager preferenceManager;

    private final MutableLiveData<List<Task>> tasks = new MutableLiveData<>();
    private final MutableLiveData<Task> selectedTask = new MutableLiveData<>();
    private final MutableLiveData<ExecutionStatus> executionStatus = new MutableLiveData<>(ExecutionStatus.IDLE);
    private final MutableLiveData<String> error = new MutableLiveData<>();

    public enum ExecutionStatus {
        IDLE,
        RUNNING,
        PAUSED
    }

    public MainViewModel(@NonNull Application application) {
        super(application);
        AutoClickApplication app = (AutoClickApplication) application;
        taskDao = app.getDatabase().taskDao();
        executor = app.getExecutor();
        preferenceManager = app.getPreferenceManager();
        loadTasks();
    }

    /**
     * Get tasks LiveData
     */
    public LiveData<List<Task>> getTasks() {
        return tasks;
    }

    /**
     * Get selected task LiveData
     */
    public LiveData<Task> getSelectedTask() {
        return selectedTask;
    }

    /**
     * Get execution status LiveData
     */
    public LiveData<ExecutionStatus> getExecutionStatus() {
        return executionStatus;
    }

    /**
     * Get error LiveData
     */
    public LiveData<String> getError() {
        return error;
    }

    /**
     * Load all tasks
     */
    public void loadTasks() {
        executor.execute(() -> {
            try {
                List<Task> taskList = taskDao.getAllTasks();
                tasks.postValue(taskList);
            } catch (Exception e) {
                Log.e(TAG, "Error loading tasks", e);
                error.postValue("Error loading tasks: " + e.getMessage());
            }
        });
    }

    /**
     * Refresh tasks
     */
    public void refreshTasks() {
        loadTasks();
    }

    /**
     * Set selected task
     */
    public void setSelectedTask(Task task) {
        selectedTask.setValue(task);
    }

    /**
     * Set execution status
     */
    public void setExecutionStatus(ExecutionStatus status) {
        executionStatus.setValue(status);
    }

    /**
     * Delete task
     */
    public void deleteTask(Task task) {
        executor.execute(() -> {
            try {
                taskDao.deleteTask(task);
                loadTasks();
            } catch (Exception e) {
                Log.e(TAG, "Error deleting task", e);
                error.postValue("Error deleting task: " + e.getMessage());
            }
        });
    }

    /**
     * Import task from file
     */
    public void importTask(Uri uri) {
        executor.execute(() -> {
            try {
                StringBuilder json = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(
                        getApplication().getContentResolver().openInputStream(uri)))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        json.append(line);
                    }
                }

                Task task = Task.fromJson(json.toString());
                if (task != null) {
                    // Reset IDs to prevent conflicts
                    task.setId(0);
                    task.getSteps().forEach(step -> step.setId(0));
                    
                    // Save task
                    long taskId = taskDao.insertTask(task);
                    task.setId(taskId);
                    
                    // Save steps
                    taskDao.insertSteps(task.getSteps());
                    
                    loadTasks();
                } else {
                    error.postValue("Invalid task file format");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error importing task", e);
                error.postValue("Error importing task: " + e.getMessage());
            }
        });
    }

    /**
     * Export task to file
     */
    public void exportTask(Task task, Uri uri) {
        executor.execute(() -> {
            try {
                String json = task.toJson();
                try (BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(
                        getApplication().getContentResolver().openOutputStream(uri)))) {
                    writer.write(json);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error exporting task", e);
                error.postValue("Error exporting task: " + e.getMessage());
            }
        });
    }

    /**
     * Enable/disable task
     */
    public void setTaskEnabled(Task task, boolean enabled) {
        executor.execute(() -> {
            try {
                task.setEnabled(enabled);
                taskDao.updateTask(task);
                loadTasks();
            } catch (Exception e) {
                Log.e(TAG, "Error updating task", e);
                error.postValue("Error updating task: " + e.getMessage());
            }
        });
    }

    /**
     * Add task to recent list
     */
    public void addToRecentTasks(Task task) {
        preferenceManager.addRecentTask(task.getId());
    }

    /**
     * Get recent tasks
     */
    public void loadRecentTasks() {
        executor.execute(() -> {
            try {
                List<Long> recentIds = preferenceManager.getRecentTasks();
                List<Task> recentTasks = taskDao.getTasksByIds(recentIds);
                tasks.postValue(recentTasks);
            } catch (Exception e) {
                Log.e(TAG, "Error loading recent tasks", e);
                error.postValue("Error loading recent tasks: " + e.getMessage());
            }
        });
    }

    /**
     * Search tasks
     */
    public void searchTasks(String query) {
        executor.execute(() -> {
            try {
                List<Task> searchResults = taskDao.searchTasks("%" + query + "%");
                tasks.postValue(searchResults);
            } catch (Exception e) {
                Log.e(TAG, "Error searching tasks", e);
                error.postValue("Error searching tasks: " + e.getMessage());
            }
        });
    }

    /**
     * Get task by ID
     */
    public void loadTask(long taskId) {
        executor.execute(() -> {
            try {
                Task task = taskDao.getTaskById(taskId);
                if (task != null) {
                    selectedTask.postValue(task);
                } else {
                    error.postValue("Task not found");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error loading task", e);
                error.postValue("Error loading task: " + e.getMessage());
            }
        });
    }

    /**
     * Clear error message
     */
    public void clearError() {
        error.setValue(null);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        // Clean up any resources if needed
    }
}
