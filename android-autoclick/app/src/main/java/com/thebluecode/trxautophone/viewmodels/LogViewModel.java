package com.thebluecode.trxautophone.viewmodels;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.thebluecode.trxautophone.AutoClickApplication;
import com.thebluecode.trxautophone.database.LogDao;
import com.thebluecode.trxautophone.models.LogEntry;
import com.thebluecode.trxautophone.utils.Constants;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;

/**
 * Enhanced ViewModel for execution log management with filtering and pagination
 */
public class LogViewModel extends AndroidViewModel {
    private static final String TAG = "LogViewModel";
    private static final int PAGE_SIZE = Constants.UI.LIST_PAGE_SIZE;

    private final LogDao logDao;
    private final ExecutorService executor;
    private final MutableLiveData<List<LogEntry>> logs = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<String> filterInfo = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();

    private int currentPage = 0;
    private boolean isLoading = false;
    private boolean hasMoreData = true;
    private LogFilter currentFilter = LogFilter.ALL;
    private long startTime = 0;
    private long endTime = 0;

    public enum LogFilter {
        ALL,
        SUCCESS,
        ERROR,
        TODAY,
        THIS_WEEK,
        CUSTOM
    }

    public LogViewModel(@NonNull Application application) {
        super(application);
        AutoClickApplication app = (AutoClickApplication) application;
        logDao = app.getDatabase().logDao();
        executor = app.getExecutor();
        loadLogs();
    }

    /**
     * Get logs LiveData
     */
    public LiveData<List<LogEntry>> getLogs() {
        return logs;
    }

    /**
     * Get filter info LiveData
     */
    public LiveData<String> getFilterInfo() {
        return filterInfo;
    }

    /**
     * Get error LiveData
     */
    public LiveData<String> getError() {
        return error;
    }

    /**
     * Load logs with current filter
     */
    private void loadLogs() {
        if (isLoading || !hasMoreData) return;

        isLoading = true;
        executor.execute(() -> {
            try {
                List<LogEntry> newLogs;
                switch (currentFilter) {
                    case SUCCESS:
                        newLogs = logDao.getSuccessLogs(PAGE_SIZE, currentPage * PAGE_SIZE);
                        break;
                    case ERROR:
                        newLogs = logDao.getErrorLogs(PAGE_SIZE, currentPage * PAGE_SIZE);
                        break;
                    case TODAY:
                        newLogs = logDao.getLogsInTimeRange(getTodayStart(), System.currentTimeMillis(),
                            PAGE_SIZE, currentPage * PAGE_SIZE);
                        break;
                    case THIS_WEEK:
                        newLogs = logDao.getLogsInTimeRange(getWeekStart(), System.currentTimeMillis(),
                            PAGE_SIZE, currentPage * PAGE_SIZE);
                        break;
                    case CUSTOM:
                        newLogs = logDao.getLogsInTimeRange(startTime, endTime,
                            PAGE_SIZE, currentPage * PAGE_SIZE);
                        break;
                    default:
                        newLogs = logDao.getAllLogs(PAGE_SIZE, currentPage * PAGE_SIZE);
                        break;
                }

                List<LogEntry> currentLogs = logs.getValue();
                if (currentLogs == null) {
                    currentLogs = new ArrayList<>();
                }

                if (currentPage == 0) {
                    currentLogs.clear();
                }

                currentLogs.addAll(newLogs);
                logs.postValue(currentLogs);

                hasMoreData = newLogs.size() == PAGE_SIZE;
                currentPage++;
                updateFilterInfo();

            } catch (Exception e) {
                Log.e(TAG, "Error loading logs", e);
                error.postValue("Error loading logs: " + e.getMessage());
            } finally {
                isLoading = false;
            }
        });
    }

    /**
     * Refresh logs
     */
    public void refreshLogs() {
        currentPage = 0;
        hasMoreData = true;
        loadLogs();
    }

    /**
     * Load more logs
     */
    public void loadMoreLogs() {
        loadLogs();
    }

    /**
     * Filter by status
     */
    public void filterByStatus(boolean success) {
        currentFilter = success ? LogFilter.SUCCESS : LogFilter.ERROR;
        currentPage = 0;
        hasMoreData = true;
        loadLogs();
    }

    /**
     * Filter by today
     */
    public void filterByToday() {
        currentFilter = LogFilter.TODAY;
        currentPage = 0;
        hasMoreData = true;
        loadLogs();
    }

    /**
     * Filter by this week
     */
    public void filterByThisWeek() {
        currentFilter = LogFilter.THIS_WEEK;
        currentPage = 0;
        hasMoreData = true;
        loadLogs();
    }

    /**
     * Filter by custom time range
     */
    public void filterByTimeRange(long start, long end) {
        currentFilter = LogFilter.CUSTOM;
        startTime = start;
        endTime = end;
        currentPage = 0;
        hasMoreData = true;
        loadLogs();
    }

    /**
     * Clear filter
     */
    public void clearFilter() {
        currentFilter = LogFilter.ALL;
        currentPage = 0;
        hasMoreData = true;
        loadLogs();
    }

    /**
     * Export logs to file
     */
    public LiveData<Boolean> exportLogs(String fileName) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();

        executor.execute(() -> {
            try {
                List<LogEntry> allLogs = logDao.getAllLogsForExport();
                File exportFile = new File(getApplication().getExternalFilesDir(null), fileName);

                try (BufferedWriter writer = new BufferedWriter(new FileWriter(exportFile))) {
                    SimpleDateFormat dateFormat = new SimpleDateFormat(
                        "yyyy-MM-dd HH:mm:ss", Locale.US);

                    // Write header
                    writer.write("Timestamp,Task,Status,Steps Completed,Duration,Error\n");

                    // Write logs
                    for (LogEntry log : allLogs) {
                        writer.write(String.format("%s,%s,%s,%d,%d,\"%s\"\n",
                            dateFormat.format(log.getTimestamp()),
                            escapeCsv(log.getTaskName()),
                            log.isSuccess() ? "Success" : "Error",
                            log.getStepsCompleted(),
                            log.getDuration(),
                            escapeCsv(log.getError())
                        ));
                    }
                }

                result.postValue(true);
            } catch (Exception e) {
                Log.e(TAG, "Error exporting logs", e);
                error.postValue("Error exporting logs: " + e.getMessage());
                result.postValue(false);
            }
        });

        return result;
    }

    /**
     * Clear all logs
     */
    public void clearLogs() {
        executor.execute(() -> {
            try {
                logDao.deleteAllLogs();
                refreshLogs();
            } catch (Exception e) {
                Log.e(TAG, "Error clearing logs", e);
                error.postValue("Error clearing logs: " + e.getMessage());
            }
        });
    }

    /**
     * Update filter info text
     */
    private void updateFilterInfo() {
        String info;
        switch (currentFilter) {
            case SUCCESS:
                info = "Showing successful executions";
                break;
            case ERROR:
                info = "Showing failed executions";
                break;
            case TODAY:
                info = "Showing today's executions";
                break;
            case THIS_WEEK:
                info = "Showing this week's executions";
                break;
            case CUSTOM:
                SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd", Locale.getDefault());
                info = String.format("Showing executions from %s to %s",
                    dateFormat.format(startTime),
                    dateFormat.format(endTime));
                break;
            default:
                info = "";
                break;
        }
        filterInfo.postValue(info);
    }

    /**
     * Get start of today
     */
    private long getTodayStart() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    /**
     * Get start of week
     */
    private long getWeekStart() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    /**
     * Escape CSV field
     */
    private String escapeCsv(String field) {
        if (field == null) return "";
        return "\"" + field.replace("\"", "\"\"") + "\"";
    }
}
