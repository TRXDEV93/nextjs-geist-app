package com.thebluecode.trxautophone.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.thebluecode.trxautophone.models.LogEntry;

import java.util.List;

/**
 * Enhanced Data Access Object for log entries with pagination and filtering
 */
@Dao
public interface LogDao {
    /**
     * Insert a new log entry
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertLog(LogEntry log);

    /**
     * Update an existing log entry
     */
    @Update
    void updateLog(LogEntry log);

    /**
     * Delete a log entry
     */
    @Delete
    void deleteLog(LogEntry log);

    /**
     * Get all logs with pagination
     */
    @Query("SELECT * FROM logs ORDER BY timestamp DESC LIMIT :limit OFFSET :offset")
    List<LogEntry> getAllLogs(int limit, int offset);

    /**
     * Get all logs as LiveData
     */
    @Query("SELECT * FROM logs ORDER BY timestamp DESC")
    LiveData<List<LogEntry>> getAllLogsLive();

    /**
     * Get all logs for export (no pagination)
     */
    @Query("SELECT * FROM logs ORDER BY timestamp DESC")
    List<LogEntry> getAllLogsForExport();

    /**
     * Get successful logs with pagination
     */
    @Query("SELECT * FROM logs WHERE success = 1 ORDER BY timestamp DESC LIMIT :limit OFFSET :offset")
    List<LogEntry> getSuccessLogs(int limit, int offset);

    /**
     * Get error logs with pagination
     */
    @Query("SELECT * FROM logs WHERE success = 0 ORDER BY timestamp DESC LIMIT :limit OFFSET :offset")
    List<LogEntry> getErrorLogs(int limit, int offset);

    /**
     * Get logs within time range with pagination
     */
    @Query("SELECT * FROM logs WHERE timestamp BETWEEN :startTime AND :endTime " +
           "ORDER BY timestamp DESC LIMIT :limit OFFSET :offset")
    List<LogEntry> getLogsInTimeRange(long startTime, long endTime, int limit, int offset);

    /**
     * Get logs for a specific task with pagination
     */
    @Query("SELECT * FROM logs WHERE taskId = :taskId ORDER BY timestamp DESC LIMIT :limit OFFSET :offset")
    List<LogEntry> getLogsForTask(long taskId, int limit, int offset);

    /**
     * Get logs for a specific task as LiveData
     */
    @Query("SELECT * FROM logs WHERE taskId = :taskId ORDER BY timestamp DESC")
    LiveData<List<LogEntry>> getLogsForTaskLive(long taskId);

    /**
     * Get log by ID
     */
    @Query("SELECT * FROM logs WHERE id = :logId")
    LogEntry getLogById(long logId);

    /**
     * Get log by ID as LiveData
     */
    @Query("SELECT * FROM logs WHERE id = :logId")
    LiveData<LogEntry> getLogByIdLive(long logId);

    /**
     * Get log count
     */
    @Query("SELECT COUNT(*) FROM logs")
    int getLogCount();

    /**
     * Get success log count
     */
    @Query("SELECT COUNT(*) FROM logs WHERE success = 1")
    int getSuccessLogCount();

    /**
     * Get error log count
     */
    @Query("SELECT COUNT(*) FROM logs WHERE success = 0")
    int getErrorLogCount();

    /**
     * Get log count for task
     */
    @Query("SELECT COUNT(*) FROM logs WHERE taskId = :taskId")
    int getLogCountForTask(long taskId);

    /**
     * Get success rate for task
     */
    @Query("SELECT CAST(COUNT(CASE WHEN success = 1 THEN 1 END) AS FLOAT) / " +
           "CAST(COUNT(*) AS FLOAT) * 100 FROM logs WHERE taskId = :taskId")
    float getSuccessRateForTask(long taskId);

    /**
     * Delete all logs
     */
    @Query("DELETE FROM logs")
    void deleteAllLogs();

    /**
     * Delete logs older than timestamp
     */
    @Query("DELETE FROM logs WHERE timestamp < :timestamp")
    void deleteOldLogs(long timestamp);

    /**
     * Delete logs for task
     */
    @Query("DELETE FROM logs WHERE taskId = :taskId")
    void deleteLogsForTask(long taskId);

    /**
     * Get latest log for task
     */
    @Query("SELECT * FROM logs WHERE taskId = :taskId ORDER BY timestamp DESC LIMIT 1")
    LogEntry getLatestLogForTask(long taskId);

    /**
     * Get execution statistics for task
     */
    @Transaction
    default TaskStats getTaskStats(long taskId) {
        int totalCount = getLogCountForTask(taskId);
        int successCount = (int) getSuccessRateForTask(taskId) * totalCount / 100;
        int errorCount = totalCount - successCount;
        LogEntry latestLog = getLatestLogForTask(taskId);
        
        return new TaskStats(totalCount, successCount, errorCount, 
            latestLog != null ? latestLog.getTimestamp() : 0);
    }

    /**
     * Task statistics class
     */
    class TaskStats {
        public final int totalExecutions;
        public final int successfulExecutions;
        public final int failedExecutions;
        public final long lastExecutionTime;

        public TaskStats(int total, int success, int failed, long lastTime) {
            this.totalExecutions = total;
            this.successfulExecutions = success;
            this.failedExecutions = failed;
            this.lastExecutionTime = lastTime;
        }

        public float getSuccessRate() {
            if (totalExecutions == 0) return 0;
            return (float) successfulExecutions / totalExecutions * 100;
        }
    }

    /**
     * Clean up old logs
     */
    @Transaction
    default void cleanupOldLogs(int maxLogs, long maxAge) {
        int currentCount = getLogCount();
        if (currentCount > maxLogs) {
            // Delete oldest logs exceeding the limit
            int toDelete = currentCount - maxLogs;
            String query = String.format(
                "DELETE FROM logs WHERE id IN " +
                "(SELECT id FROM logs ORDER BY timestamp ASC LIMIT %d)",
                toDelete);
            androidx.room.RoomDatabase.JournalMode.TRUNCATE.name();
        }

        // Delete logs older than maxAge
        if (maxAge > 0) {
            long cutoffTime = System.currentTimeMillis() - maxAge;
            deleteOldLogs(cutoffTime);
        }
    }
}
