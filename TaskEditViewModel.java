package com.thebluecode.trxautophone.models;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;

import com.thebluecode.trxautophone.database.TaskDao;
import com.thebluecode.trxautophone.database.TaskDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TaskEditViewModel extends AndroidViewModel {
    private final TaskDao taskDao;
    private final ExecutorService executorService;
    private final MutableLiveData<Task> task = new MutableLiveData<>();
    private final MutableLiveData<List<Step>> steps = new MutableLiveData<>(new ArrayList<>());
    private boolean isNewTask = true;

    public TaskEditViewModel(Application application) {
        super(application);
        taskDao = TaskDatabase.getInstance(application).taskDao();
        executorService = Executors.newSingleThreadExecutor();
    }

    public void loadTask(long taskId) {
        isNewTask = false;
        executorService.execute(() -> {
            Task loadedTask = taskDao.getTaskById(taskId).getValue();
            if (loadedTask != null) {
                task.postValue(loadedTask);
                steps.postValue(loadedTask.getSteps());
            }
        });
    }

    public void saveTask(Task updatedTask, SaveCallback callback) {
        executorService.execute(() -> {
            try {
                // Update steps list in task
                updatedTask.setSteps(steps.getValue());
                
                if (isNewTask) {
                    long taskId = taskDao.insertTask(updatedTask);
                    updatedTask.setId(taskId);
                    
                    // Insert steps with the new task ID
                    for (Step step : updatedTask.getSteps()) {
                        step.setTaskId(taskId);
                        taskDao.insertStep(step);
                    }
                } else {
                    taskDao.updateTask(updatedTask);
                    
                    // Update existing steps
                    taskDao.deleteStepsByTaskId(updatedTask.getId());
                    for (Step step : updatedTask.getSteps()) {
                        step.setTaskId(updatedTask.getId());
                        taskDao.insertStep(step);
                    }
                }
                
                callback.onSaveComplete(true);
            } catch (Exception e) {
                e.printStackTrace();
                callback.onSaveComplete(false);
            }
        });
    }

    public void addStep(Step step) {
        List<Step> currentSteps = new ArrayList<>(steps.getValue());
        step.setOrder(currentSteps.size());
        currentSteps.add(step);
        steps.setValue(currentSteps);
    }

    public void updateStep(int position, Step updatedStep) {
        List<Step> currentSteps = new ArrayList<>(steps.getValue());
        if (position >= 0 && position < currentSteps.size()) {
            updatedStep.setOrder(position);
            currentSteps.set(position, updatedStep);
            steps.setValue(currentSteps);
        }
    }

    public void removeStep(int position) {
        List<Step> currentSteps = new ArrayList<>(steps.getValue());
        if (position >= 0 && position < currentSteps.size()) {
            currentSteps.remove(position);
            // Update order of remaining steps
            for (int i = position; i < currentSteps.size(); i++) {
                currentSteps.get(i).setOrder(i);
            }
            steps.setValue(currentSteps);
        }
    }

    public void moveStepUp(int position) {
        if (position > 0) {
            List<Step> currentSteps = new ArrayList<>(steps.getValue());
            Step stepToMove = currentSteps.get(position);
            Step stepAbove = currentSteps.get(position - 1);
            
            // Swap orders
            int tempOrder = stepToMove.getOrder();
            stepToMove.setOrder(stepAbove.getOrder());
            stepAbove.setOrder(tempOrder);
            
            // Swap positions in list
            currentSteps.set(position - 1, stepToMove);
            currentSteps.set(position, stepAbove);
            
            steps.setValue(currentSteps);
        }
    }

    public void moveStepDown(int position) {
        List<Step> currentSteps = steps.getValue();
        if (position < currentSteps.size() - 1) {
            Step stepToMove = currentSteps.get(position);
            Step stepBelow = currentSteps.get(position + 1);
            
            // Swap orders
            int tempOrder = stepToMove.getOrder();
            stepToMove.setOrder(stepBelow.getOrder());
            stepBelow.setOrder(tempOrder);
            
            // Swap positions in list
            currentSteps.set(position + 1, stepToMove);
            currentSteps.set(position, stepBelow);
            
            steps.setValue(currentSteps);
        }
    }

    public LiveData<Task> getTask() {
        return task;
    }

    public LiveData<List<Step>> getSteps() {
        return steps;
    }

    public boolean isNewTask() {
        return isNewTask;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdown();
    }

    public interface SaveCallback {
        void onSaveComplete(boolean success);
    }

    // Factory for creating TaskEditViewModel with application context
    public static class Factory implements ViewModelProvider.Factory {
        private final Application application;

        public Factory(Application application) {
            this.application = application;
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T extends androidx.lifecycle.ViewModel> T create(Class<T> modelClass) {
            return (T) new TaskEditViewModel(application);
        }
    }
}
