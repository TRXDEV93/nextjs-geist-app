package com.thebluecode.trxautophone.database;

import androidx.lifecycle.LiveData;
import androidx.room.*;

import com.thebluecode.trxautophone.models.Step;
import com.thebluecode.trxautophone.models.Task;

import java.util.List;

@Dao
public interface TaskDao {
    // Task Operations
    @Insert
    long insertTask(Task task);

    @Update
    void updateTask(Task task);

    @Delete
    void deleteTask(Task task);

    @Query("SELECT * FROM tasks ORDER BY updatedAt DESC")
    LiveData<List<Task>> getAllTasks();

    @Query("SELECT * FROM tasks WHERE id = :taskId")
    LiveData<Task> getTaskById(long taskId);

    // Step Operations
    @Insert
    long insertStep(Step step);

    @Update
    void updateStep(Step step);

    @Delete
    void deleteStep(Step step);

    @Query("SELECT * FROM steps WHERE taskId = :taskId ORDER BY `order` ASC")
    LiveData<List<Step>> getStepsByTaskId(long taskId);

    @Query("DELETE FROM steps WHERE taskId = :taskId")
    void deleteStepsByTaskId(long taskId);

    // Transaction Operations
    @Transaction
    @Query("SELECT * FROM tasks WHERE id = :taskId")
    LiveData<Task> getTaskWithSteps(long taskId);

    // Import/Export Operations
    @Transaction
    default void importTask(Task task, List<Step> steps) {
        long taskId = insertTask(task);
        for (Step step : steps) {
            step.setTaskId(taskId);
            insertStep(step);
        }
    }

    @Transaction
    default void deleteTaskWithSteps(Task task) {
        deleteStepsByTaskId(task.getId());
        deleteTask(task);
    }

    // Utility Queries
    @Query("SELECT COUNT(*) FROM tasks")
    int getTaskCount();

    @Query("UPDATE steps SET `order` = `order` + 1 WHERE taskId = :taskId AND `order` >= :fromPosition")
    void shiftStepsUp(long taskId, int fromPosition);

    @Query("UPDATE steps SET `order` = `order` - 1 WHERE taskId = :taskId AND `order` >= :fromPosition")
    void shiftStepsDown(long taskId, int fromPosition);
}
