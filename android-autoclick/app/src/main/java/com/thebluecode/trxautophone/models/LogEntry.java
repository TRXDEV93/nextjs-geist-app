package com.thebluecode.trxautophone.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Enhanced log entry model with detailed execution information
 */
@Entity(
    tableName = "logs",
    foreignKeys = @ForeignKey(
        entity = Task.class,
        parentColumns = "id",
        childColumns = "taskId",
        onDelete = ForeignKey.CASCADE
    ),
    indices = {
        @Index("taskId"),
        @Index("timestamp")
    }
)
public class LogEntry implements Parcelable {
    @PrimaryKey(autoGenerate = true)
    private long id;
    private long taskId;
    private String taskName;
    private long timestamp;
    private boolean success;
    private int stepsCompleted;
    private long duration;
    private String error;
    private String details;
    private String deviceInfo;

    public LogEntry() {
        this.timestamp = System.currentTimeMillis();
    }

    protected LogEntry(Parcel in) {
        id = in.readLong();
        taskId = in.readLong();
        taskName = in.readString();
        timestamp = in.readLong();
        success = in.readByte() != 0;
        stepsCompleted = in.readInt();
        duration = in.readLong();
        error = in.readString();
        details = in.readString();
        deviceInfo = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeLong(taskId);
        dest.writeString(taskName);
        dest.writeLong(timestamp);
        dest.writeByte((byte) (success ? 1 : 0));
        dest.writeInt(stepsCompleted);
        dest.writeLong(duration);
        dest.writeString(error);
        dest.writeString(details);
        dest.writeString(deviceInfo);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<LogEntry> CREATOR = new Creator<LogEntry>() {
        @Override
        public LogEntry createFromParcel(Parcel in) {
            return new LogEntry(in);
        }

        @Override
        public LogEntry[] newArray(int size) {
            return new LogEntry[size];
        }
    };

    // Getters and Setters

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getTaskId() {
        return taskId;
    }

    public void setTaskId(long taskId) {
        this.taskId = taskId;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public int getStepsCompleted() {
        return stepsCompleted;
    }

    public void setStepsCompleted(int stepsCompleted) {
        this.stepsCompleted = stepsCompleted;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getDeviceInfo() {
        return deviceInfo;
    }

    public void setDeviceInfo(String deviceInfo) {
        this.deviceInfo = deviceInfo;
    }

    /**
     * Create success log entry
     */
    public static LogEntry createSuccessLog(Task task, int stepsCompleted, long duration) {
        LogEntry log = new LogEntry();
        log.setTaskId(task.getId());
        log.setTaskName(task.getName());
        log.setSuccess(true);
        log.setStepsCompleted(stepsCompleted);
        log.setDuration(duration);
        log.setDeviceInfo(getDeviceInfo());
        return log;
    }

    /**
     * Create error log entry
     */
    public static LogEntry createErrorLog(Task task, int stepsCompleted, long duration, 
                                        String error, String details) {
        LogEntry log = new LogEntry();
        log.setTaskId(task.getId());
        log.setTaskName(task.getName());
        log.setSuccess(false);
        log.setStepsCompleted(stepsCompleted);
        log.setDuration(duration);
        log.setError(error);
        log.setDetails(details);
        log.setDeviceInfo(getDeviceInfo());
        return log;
    }

    /**
     * Get device information
     */
    private static String getDeviceInfo() {
        return String.format("Android %s (API %d), %s %s",
            android.os.Build.VERSION.RELEASE,
            android.os.Build.VERSION.SDK_INT,
            android.os.Build.MANUFACTURER,
            android.os.Build.MODEL);
    }

    /**
     * Get formatted summary
     */
    public String getSummary() {
        if (success) {
            return String.format("Completed %d steps in %s",
                stepsCompleted,
                formatDuration(duration));
        } else {
            return String.format("Failed at step %d: %s",
                stepsCompleted + 1,
                error != null ? error : "Unknown error");
        }
    }

    /**
     * Format duration for display
     */
    private String formatDuration(long durationMs) {
        if (durationMs < 1000) {
            return durationMs + "ms";
        }
        
        long seconds = durationMs / 1000;
        if (seconds < 60) {
            return seconds + "s";
        }
        
        long minutes = seconds / 60;
        seconds = seconds % 60;
        if (minutes < 60) {
            return String.format("%dm %ds", minutes, seconds);
        }
        
        long hours = minutes / 60;
        minutes = minutes % 60;
        return String.format("%dh %dm %ds", hours, minutes, seconds);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LogEntry logEntry = (LogEntry) o;
        return id == logEntry.id;
    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }

    @Override
    public String toString() {
        return "LogEntry{" +
               "id=" + id +
               ", taskId=" + taskId +
               ", taskName='" + taskName + '\'' +
               ", timestamp=" + timestamp +
               ", success=" + success +
               ", stepsCompleted=" + stepsCompleted +
               ", duration=" + duration +
               ", error='" + error + '\'' +
               '}';
    }
}
