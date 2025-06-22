package com.thebluecode.trxautophone.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;
import androidx.room.ColumnInfo;
import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Enhanced Task entity with improved validation and metadata
 */
@Entity(tableName = "tasks")
public class Task {
    @PrimaryKey(autoGenerate = true)
    private long id;
    
    @NonNull
    @ColumnInfo(name = "name")
    private String name;
    
    @ColumnInfo(name = "description")
    private String description;
    
    @TypeConverters(StepListConverter.class)
    @ColumnInfo(name = "steps")
    private List<Step> steps;
    
    @ColumnInfo(name = "enabled")
    private boolean isEnabled;
    
    @ColumnInfo(name = "created_at")
    private long createdAt;
    
    @ColumnInfo(name = "updated_at")
    private long updatedAt;
    
    @ColumnInfo(name = "last_executed")
    private long lastExecuted;
    
    @ColumnInfo(name = "execution_count")
    private int executionCount;
    
    @ColumnInfo(name = "success_count")
    private int successCount;
    
    @ColumnInfo(name = "category")
    private String category;
    
    @ColumnInfo(name = "tags")
    private String tags; // Comma-separated tags
    
    @ColumnInfo(name = "repeat_count")
    private int repeatCount;
    
    @ColumnInfo(name = "repeat_delay")
    private long repeatDelay;

    /**
     * Task execution status
     */
    public enum Status {
        IDLE("Idle"),
        RUNNING("Running"),
        PAUSED("Paused"),
        COMPLETED("Completed"),
        FAILED("Failed"),
        CANCELLED("Cancelled");

        private final String displayName;

        Status(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // Constructors
    public Task() {
        this.steps = new ArrayList<>();
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
        this.isEnabled = true;
        this.executionCount = 0;
        this.successCount = 0;
        this.repeatCount = 1;
        this.repeatDelay = 0;
        this.category = "General";
        this.tags = "";
    }

    public Task(@NonNull String name) {
        this();
        this.name = name;
    }

    public Task(@NonNull String name, String description) {
        this(name);
        this.description = description;
    }

    // Getters and Setters with validation
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    @NonNull
    public String getName() { return name != null ? name : ""; }
    public void setName(@NonNull String name) { 
        this.name = name != null ? name.trim() : "";
        updateTimestamp();
    }

    public String getDescription() { return description != null ? description : ""; }
    public void setDescription(String description) { 
        this.description = description != null ? description.trim() : "";
        updateTimestamp();
    }

    public List<Step> getSteps() { 
        return steps != null ? steps : new ArrayList<>(); 
    }
    
    public void setSteps(List<Step> steps) { 
        this.steps = steps != null ? steps : new ArrayList<>();
        updateTimestamp();
    }

    public boolean isEnabled() { return isEnabled; }
    public void setEnabled(boolean enabled) { 
        this.isEnabled = enabled;
        updateTimestamp();
    }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }

    public long getLastExecuted() { return lastExecuted; }
    public void setLastExecuted(long lastExecuted) { this.lastExecuted = lastExecuted; }

    public int getExecutionCount() { return executionCount; }
    public void setExecutionCount(int executionCount) { 
        this.executionCount = Math.max(0, executionCount); 
    }

    public int getSuccessCount() { return successCount; }
    public void setSuccessCount(int successCount) { 
        this.successCount = Math.max(0, successCount); 
    }

    public String getCategory() { return category != null ? category : "General"; }
    public void setCategory(String category) { 
        this.category = category != null ? category.trim() : "General";
        updateTimestamp();
    }

    public String getTags() { return tags != null ? tags : ""; }
    public void setTags(String tags) { 
        this.tags = tags != null ? tags.trim() : "";
        updateTimestamp();
    }

    public int getRepeatCount() { return repeatCount; }
    public void setRepeatCount(int repeatCount) { 
        this.repeatCount = Math.max(1, Math.min(repeatCount, 1000)); // Max 1000 repeats
        updateTimestamp();
    }

    public long getRepeatDelay() { return repeatDelay; }
    public void setRepeatDelay(long repeatDelay) { 
        this.repeatDelay = Math.max(0, Math.min(repeatDelay, 3600000)); // Max 1 hour
        updateTimestamp();
    }

    // Helper methods
    private void updateTimestamp() {
        this.updatedAt = System.currentTimeMillis();
    }

    /**
     * Add a step to the task
     */
    public void addStep(Step step) {
        if (steps == null) {
            steps = new ArrayList<>();
        }
        if (step != null) {
            step.setTaskId(this.id);
            step.setOrder(steps.size());
            steps.add(step);
            updateTimestamp();
        }
    }

    /**
     * Remove a step at the specified position
     */
    public void removeStep(int position) {
        if (steps != null && position >= 0 && position < steps.size()) {
            steps.remove(position);
            // Update order of remaining steps
            for (int i = position; i < steps.size(); i++) {
                steps.get(i).setOrder(i);
            }
            updateTimestamp();
        }
    }

    /**
     * Move a step from one position to another
     */
    public void moveStep(int fromPosition, int toPosition) {
        if (steps != null && 
            fromPosition >= 0 && fromPosition < steps.size() &&
            toPosition >= 0 && toPosition < steps.size() &&
            fromPosition != toPosition) {
            
            Step step = steps.remove(fromPosition);
            steps.add(toPosition, step);
            
            // Update order of all affected steps
            int start = Math.min(fromPosition, toPosition);
            int end = Math.max(fromPosition, toPosition);
            for (int i = start; i <= end; i++) {
                steps.get(i).setOrder(i);
            }
            updateTimestamp();
        }
    }

    /**
     * Get the total estimated execution time in milliseconds
     */
    public long getEstimatedDuration() {
        long totalDelay = 0;
        if (steps != null) {
            for (Step step : steps) {
                totalDelay += step.getDelay();
                // Add estimated execution time for each step type
                totalDelay += getStepExecutionTime(step.getType());
            }
        }
        return totalDelay * repeatCount + (repeatDelay * (repeatCount - 1));
    }

    /**
     * Get estimated execution time for a step type
     */
    private long getStepExecutionTime(Step.StepType type) {
        switch (type) {
            case TAP:
            case SYSTEM_KEY:
                return 100; // 100ms
            case LONG_PRESS:
                return 500; // 500ms
            case SWIPE:
                return 300; // 300ms
            case TEXT_SEARCH:
            case IMAGE_SEARCH:
                return 1000; // 1 second
            case INPUT_TEXT:
                return 200; // 200ms
            default:
                return 50; // 50ms default
        }
    }

    /**
     * Get success rate as percentage
     */
    public float getSuccessRate() {
        if (executionCount == 0) return 0f;
        return (float) successCount / executionCount * 100f;
    }

    /**
     * Validate task configuration
     */
    public boolean isValid() {
        if (name == null || name.trim().isEmpty()) return false;
        if (steps == null || steps.isEmpty()) return false;
        
        // Validate all steps
        for (Step step : steps) {
            if (!step.isValid()) return false;
        }
        
        return true;
    }

    /**
     * Record execution result
     */
    public void recordExecution(boolean success) {
        this.executionCount++;
        if (success) {
            this.successCount++;
        }
        this.lastExecuted = System.currentTimeMillis();
        updateTimestamp();
    }

    /**
     * Get task summary for display
     */
    public String getSummary() {
        int stepCount = steps != null ? steps.size() : 0;
        return String.format("%d steps • %s • %.1f%% success", 
                stepCount, category, getSuccessRate());
    }

    @Override
    public String toString() {
        return "Task{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", steps=" + (steps != null ? steps.size() : 0) +
                ", enabled=" + isEnabled +
                ", executions=" + executionCount +
                '}';
    }
}
