package com.thebluecode.trxautophone.viewmodels;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.thebluecode.trxautophone.AutoClickApplication;
import com.thebluecode.trxautophone.database.TaskDao;
import com.thebluecode.trxautophone.models.Step;
import com.thebluecode.trxautophone.models.Task;
import com.thebluecode.trxautophone.utils.Constants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * Enhanced ViewModel for task editing with improved validation and state management
 */
public class TaskEditViewModel extends AndroidViewModel {
    private static final String TAG = "TaskEditViewModel";

    private final TaskDao taskDao;
    private final ExecutorService executor;
    private final MutableLiveData<Task> task = new MutableLiveData<>();
    private final MutableLiveData<List<Step>> steps = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<String> validationError = new MutableLiveData<>();
    private boolean isModified = false;

    public TaskEditViewModel(@NonNull Application application) {
        super(application);
        AutoClickApplication app = (AutoClickApplication) application;
        taskDao = app.getDatabase().taskDao();
        executor = app.getExecutor();
    }

    /**
     * Get task LiveData
     */
    public LiveData<Task> getTask() {
        return task;
    }

    /**
     * Get steps LiveData
     */
    public LiveData<List<Step>> getSteps() {
        return steps;
    }

    /**
     * Get validation error LiveData
     */
    public LiveData<String> getValidationError() {
        return validationError;
    }

    /**
     * Load existing task
     */
    public void loadTask(long taskId) {
        executor.execute(() -> {
            try {
                Task loadedTask = taskDao.getTaskById(taskId);
                if (loadedTask != null) {
                    task.postValue(loadedTask);
                    steps.postValue(new ArrayList<>(loadedTask.getSteps()));
                } else {
                    validationError.postValue("Task not found");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error loading task", e);
                validationError.postValue("Error loading task: " + e.getMessage());
            }
        });
    }

    /**
     * Add new step
     */
    public void addStep(Step step) {
        List<Step> currentSteps = steps.getValue();
        if (currentSteps == null) {
            currentSteps = new ArrayList<>();
        }

        if (currentSteps.size() >= Constants.Limits.MAX_STEPS_PER_TASK) {
            validationError.setValue("Maximum number of steps reached");
            return;
        }

        step.setOrder(currentSteps.size());
        currentSteps.add(step);
        steps.setValue(currentSteps);
        isModified = true;
    }

    /**
     * Update existing step
     */
    public void updateStep(Step step) {
        List<Step> currentSteps = steps.getValue();
        if (currentSteps != null) {
            int index = -1;
            for (int i = 0; i < currentSteps.size(); i++) {
                if (currentSteps.get(i).getId() == step.getId()) {
                    index = i;
                    break;
                }
            }
            if (index != -1) {
                currentSteps.set(index, step);
                steps.setValue(currentSteps);
                isModified = true;
            }
        }
    }

    /**
     * Remove step
     */
    public void removeStep(int position) {
        List<Step> currentSteps = steps.getValue();
        if (currentSteps != null && position >= 0 && position < currentSteps.size()) {
            currentSteps.remove(position);
            // Update order of remaining steps
            for (int i = position; i < currentSteps.size(); i++) {
                currentSteps.get(i).setOrder(i);
            }
            steps.setValue(currentSteps);
            isModified = true;
        }
    }

    /**
     * Move step from one position to another
     */
    public void moveStep(int fromPosition, int toPosition) {
        List<Step> currentSteps = steps.getValue();
        if (currentSteps != null && 
            fromPosition >= 0 && fromPosition < currentSteps.size() &&
            toPosition >= 0 && toPosition < currentSteps.size()) {
            
            Collections.swap(currentSteps, fromPosition, toPosition);
            
            // Update order of affected steps
            int start = Math.min(fromPosition, toPosition);
            int end = Math.max(fromPosition, toPosition);
            for (int i = start; i <= end; i++) {
                currentSteps.get(i).setOrder(i);
            }
            
            steps.setValue(currentSteps);
            isModified = true;
        }
    }

    /**
     * Save task
     */
    public void saveTask(String name, String description, int repeatCount, long repeatDelay) {
        if (!validateTask(name, repeatCount, repeatDelay)) {
            return;
        }

        executor.execute(() -> {
            try {
                Task currentTask = task.getValue();
                List<Step> currentSteps = steps.getValue();

                if (currentTask == null) {
                    currentTask = new Task(name);
                } else {
                    currentTask.setName(name);
                }

                currentTask.setDescription(description);
                currentTask.setRepeatCount(repeatCount);
                currentTask.setRepeatDelay(repeatDelay);
                currentTask.setSteps(currentSteps != null ? currentSteps : new ArrayList<>());

                if (currentTask.getId() > 0) {
                    // Update existing task
                    taskDao.updateTask(currentTask);
                    if (isModified && currentSteps != null) {
                        taskDao.updateSteps(currentSteps);
                    }
                } else {
                    // Insert new task
                    long taskId = taskDao.insertTask(currentTask);
                    currentTask.setId(taskId);
                    if (currentSteps != null) {
                        for (Step step : currentSteps) {
                            step.setTaskId(taskId);
                        }
                        taskDao.insertSteps(currentSteps);
                    }
                }

                task.postValue(currentTask);
                isModified = false;

            } catch (Exception e) {
                Log.e(TAG, "Error saving task", e);
                validationError.postValue("Error saving task: " + e.getMessage());
            }
        });
    }

    /**
     * Validate task data
     */
    private boolean validateTask(String name, int repeatCount, long repeatDelay) {
        if (name == null || name.trim().isEmpty()) {
            validationError.setValue("Task name is required");
            return false;
        }

        if (name.length() > Constants.Limits.MAX_TASK_NAME_LENGTH) {
            validationError.setValue("Task name is too long");
            return false;
        }

        if (repeatCount < 1 || repeatCount > Constants.Limits.MAX_REPEAT_COUNT) {
            validationError.setValue("Invalid repeat count");
            return false;
        }

        if (repeatDelay < 0 || repeatDelay > Constants.Limits.MAX_REPEAT_DELAY) {
            validationError.setValue("Invalid repeat delay");
            return false;
        }

        List<Step> currentSteps = steps.getValue();
        if (currentSteps == null || currentSteps.isEmpty()) {
            validationError.setValue("Task must have at least one step");
            return false;
        }

        if (currentSteps.size() > Constants.Limits.MAX_STEPS_PER_TASK) {
            validationError.setValue("Too many steps");
            return false;
        }

        // Validate each step
        for (Step step : currentSteps) {
            if (!step.isValid()) {
                validationError.setValue("Invalid step configuration");
                return false;
            }
        }

        return true;
    }

    /**
     * Check if task has been modified
     */
    public boolean isModified() {
        return isModified;
    }

    /**
     * Clear validation error
     */
    public void clearValidationError() {
        validationError.setValue(null);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        // Clean up any resources if needed
    }
}
