package com.thebluecode.trxautophone.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "steps")
public class Step {
    @PrimaryKey(autoGenerate = true)
    private long id;
    private StepType type;
    private String actionData; // JSON string containing action-specific data
    private int order;
    private long taskId;
    private long delay; // Delay in milliseconds before executing this step

    public enum StepType {
        SYSTEM_KEY,      // Home, Back, Recent Apps
        TAP,            // Single tap at coordinates
        LONG_PRESS,     // Long press at coordinates
        SWIPE,          // Swipe gesture
        TEXT_SEARCH,    // Search for text and perform action
        IMAGE_SEARCH,   // Search for image and perform action
        DELAY,          // Wait for specified duration
        CONDITION      // If-then-else condition
    }

    public Step() {
        this.delay = 0;
    }

    // Getters and Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public StepType getType() { return type; }
    public void setType(StepType type) { this.type = type; }

    public String getActionData() { return actionData; }
    public void setActionData(String actionData) { this.actionData = actionData; }

    public int getOrder() { return order; }
    public void setOrder(int order) { this.order = order; }

    public long getTaskId() { return taskId; }
    public void setTaskId(long taskId) { this.taskId = taskId; }

    public long getDelay() { return delay; }
    public void setDelay(long delay) { this.delay = delay; }
}
