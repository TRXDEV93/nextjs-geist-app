package com.thebluecode.trxautophone.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.thebluecode.trxautophone.models.Step;
import com.thebluecode.trxautophone.models.Task;

import java.util.List;

/**
 * Enhanced Data Access Object for tasks and steps with complex queries
 */
@Dao
public interface TaskDao {
    // Task Operations

    /**
     * Insert a new task
     * @return the ID of the inserted task
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertTask(Task task);

    /**
     * Update an existing task
     */
    @Update
    void updateTask(Task task);

    /**
     * Delete a task and its steps
     */
    @Delete
    void deleteTask(Task task);

    /**
     * Get all tasks ordered by last executed time
     */
    @Query("SELECT * FROM tasks ORDER BY lastExecuted DESC")
    List<Task> getAllTasks();

    /**
     * Get all tasks as LiveData
     */
    @Query("SELECT * FROM tasks ORDER BY lastExecuted DESC")
    LiveData<List<Task>> getAllTasksLive();

    /**
     * Get task by ID
     */
    @Query("SELECT * FROM tasks WHERE id = :taskId")
    Task getTaskById(long taskId);

    /**
     * Get task by ID as LiveData
     */
    @Query("SELECT * FROM tasks WHERE id = :taskId")
    LiveData<Task> getTaskByIdLive(long taskId);

    /**
     * Get tasks by IDs
     */
    @Query("SELECT * FROM tasks WHERE id IN (:taskIds) ORDER BY lastExecuted DESC")
    List<Task> getTasksByIds(List<Long> taskIds);

    /**
     * Search tasks by name or description
     */
    @Query("SELECT * FROM tasks WHERE name LIKE :query OR description LIKE :query " +
           "ORDER BY lastExecuted DESC")
    List<Task> searchTasks(String query);

    /**
     * Get enabled tasks
     */
    @Query("SELECT * FROM tasks WHERE enabled = 1 ORDER BY lastExecuted DESC")
    List<Task> getEnabledTasks();

    /**
     * Get tasks that were executed successfully
     */
    @Query("SELECT * FROM tasks WHERE successCount > 0 ORDER BY lastExecuted DESC")
    List<Task> getSuccessfulTasks();

    /**
     * Get tasks that failed execution
     */
    @Query("SELECT * FROM tasks WHERE executionCount > successCount ORDER BY lastExecuted DESC")
    List<Task> getFailedTasks();

    // Step Operations

    /**
     * Insert steps
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertSteps(List<Step> steps);

    /**
     * Update steps
     */
    @Update
    void updateSteps(List<Step> steps);

    /**
     * Delete step
     */
    @Delete
    void deleteStep(Step step);

    /**
     * Get steps for a task
     */
    @Query("SELECT * FROM steps WHERE taskId = :taskId ORDER BY `order` ASC")
    List<Step> getStepsForTask(long taskId);

    /**
     * Get steps for a task as LiveData
     */
    @Query("SELECT * FROM steps WHERE taskId = :taskId ORDER BY `order` ASC")
    LiveData<List<Step>> getStepsForTaskLive(long taskId);

    /**
     * Get enabled steps for a task
     */
    @Query("SELECT * FROM steps WHERE taskId = :taskId AND enabled = 1 ORDER BY `order` ASC")
    List<Step> getEnabledStepsForTask(long taskId);

    // Complex Operations

    /**
     * Delete task and all its steps
     */
    @Transaction
    default void deleteTaskWithSteps(Task task) {
        deleteStepsForTask(task.getId());
        deleteTask(task);
    }

    /**
     * Delete steps for a task
     */
    @Query("DELETE FROM steps WHERE taskId = :taskId")
    void deleteStepsForTask(long taskId);

    /**
     * Update task execution stats
     */
    @Query("UPDATE tasks SET executionCount = executionCount + 1, " +
           "successCount = successCount + :success, " +
           "lastExecuted = :timestamp " +
           "WHERE id = :taskId")
    void updateTaskStats(long taskId, int success, long timestamp);

    /**
     * Get task count
     */
    @Query("SELECT COUNT(*) FROM tasks")
    int getTaskCount();

    /**
     * Get enabled task count
     */
    @Query("SELECT COUNT(*) FROM tasks WHERE enabled = 1")
    int getEnabledTaskCount();

    /**
     * Get step count for task
     */
    @Query("SELECT COUNT(*) FROM steps WHERE taskId = :taskId")
    int getStepCount(long taskId);

    /**
     * Get enabled step count for task
     */
    @Query("SELECT COUNT(*) FROM steps WHERE taskId = :taskId AND enabled = 1")
    int getEnabledStepCount(long taskId);

    /**
     * Get tasks executed within time range
     */
    @Query("SELECT * FROM tasks WHERE lastExecuted BETWEEN :startTime AND :endTime " +
           "ORDER BY lastExecuted DESC")
    List<Task> getTasksInTimeRange(long startTime, long endTime);

    /**
     * Get tasks with minimum success rate
     */
    @Query("SELECT * FROM tasks WHERE CAST(successCount AS FLOAT) / " +
           "CASE WHEN executionCount = 0 THEN 1 ELSE executionCount END >= :minSuccessRate " +
           "ORDER BY lastExecuted DESC")
    List<Task> getTasksWithMinSuccessRate(float minSuccessRate);

    /**
     * Reset execution stats for all tasks
     */
    @Query("UPDATE tasks SET executionCount = 0, successCount = 0, lastExecuted = 0")
    void resetAllTaskStats();

    /**
     * Reset execution stats for a task
     */
    @Query("UPDATE tasks SET executionCount = 0, successCount = 0, lastExecuted = 0 " +
           "WHERE id = :taskId")
    void resetTaskStats(long taskId);

    /**
     * Enable/disable all tasks
     */
    @Query("UPDATE tasks SET enabled = :enabled")
    void setAllTasksEnabled(boolean enabled);

    /**
     * Enable/disable all steps for a task
     */
    @Query("UPDATE steps SET enabled = :enabled WHERE taskId = :taskId")
    void setAllStepsEnabled(long taskId, boolean enabled);

    /**
     * Delete all tasks and steps
     */
    @Transaction
    default void deleteAllData() {
        deleteAllSteps();
        deleteAllTasks();
    }

    @Query("DELETE FROM steps")
    void deleteAllSteps();

    @Query("DELETE FROM tasks")
    void deleteAllTasks();
}
