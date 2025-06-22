package com.thebluecode.trxautophone;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;

import android.util.Pair;

import com.thebluecode.trxautophone.database.TaskDao;
import com.thebluecode.trxautophone.database.TaskDatabase;
import com.thebluecode.trxautophone.models.Task;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainViewModel extends AndroidViewModel {
    private final TaskDao taskDao;
    private final ExecutorService executorService;
    private final LiveData<List<Task>> tasks;
    private final MutableLiveData<Task> selectedTask = new MutableLiveData<>();
    private final MutableLiveData<Pair<Long, Integer>> executionProgress = new MutableLiveData<>();

    public MainViewModel(Application application) {
        super(application);
        taskDao = TaskDatabase.getInstance(application).taskDao();
        executorService = Executors.newSingleThreadExecutor();
        tasks = taskDao.getAllTasks();
    }

    public LiveData<List<Task>> getTasks() {
        return tasks;
    }

    public LiveData<Task> getSelectedTask() {
        return selectedTask;
    }

    public void setSelectedTask(Task task) {
        selectedTask.setValue(task);
    }

    public LiveData<Pair<Long, Integer>> getExecutionProgress() {
        return executionProgress;
    }

    public void updateExecutionProgress(long taskId, int progress) {
        executionProgress.postValue(new Pair<>(taskId, progress));
    }

    public void deleteTask(Task task) {
        executorService.execute(() -> {
            taskDao.deleteTaskWithSteps(task);
        });
    }

    public void importTask(String jsonTask) {
        executorService.execute(() -> {
            try {
                // Implement JSON to Task conversion and database insertion
                // This is a placeholder for the actual implementation
                // Task task = JsonConverter.fromJson(jsonTask);
                // taskDao.importTask(task, task.getSteps());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public String exportTask(Task task) {
        // Implement Task to JSON conversion
        // This is a placeholder for the actual implementation
        // return JsonConverter.toJson(task);
        return "";
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdown();
    }

    // Factory for creating MainViewModel with application context
    public static class Factory implements ViewModelProvider.Factory {
        private final Application application;

        public Factory(Application application) {
            this.application = application;
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T extends androidx.lifecycle.ViewModel> T create(Class<T> modelClass) {
            return (T) new MainViewModel(application);
        }
    }
}
