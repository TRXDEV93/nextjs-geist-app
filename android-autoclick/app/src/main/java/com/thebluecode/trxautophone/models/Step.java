package com.thebluecode.trxautophone.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;
import androidx.annotation.NonNull;

/**
 * Enhanced Step entity with improved type safety and validation
 */
@Entity(tableName = "steps")
public class Step {
    @PrimaryKey(autoGenerate = true)
    private long id;
    
    @NonNull
    @ColumnInfo(name = "step_type")
    private StepType type;
    
    @ColumnInfo(name = "action_data")
    private String actionData; // JSON string containing action-specific data
    
    @ColumnInfo(name = "step_order")
    private int order;
    
    @ColumnInfo(name = "task_id")
    private long taskId;
    
    @ColumnInfo(name = "delay_ms")
    private long delay; // Delay in milliseconds before executing this step
    
    @ColumnInfo(name = "enabled")
    private boolean enabled = true;
    
    @ColumnInfo(name = "description")
    private String description;

    /**
     * Enhanced step types with better categorization
     */
    public enum StepType {
        // System Actions
        SYSTEM_KEY(1, "System Key", "Perform system key actions like Home, Back, Recent Apps"),
        
        // Touch Gestures
        TAP(2, "Tap", "Single tap at specific coordinates"),
        LONG_PRESS(3, "Long Press", "Long press at specific coordinates"),
        SWIPE(4, "Swipe", "Swipe gesture between two points"),
        
        // Search and Interaction
        TEXT_SEARCH(5, "Text Search", "Search for text and perform action"),
        IMAGE_SEARCH(6, "Image Search", "Search for image and perform action"),
        
        // Flow Control
        DELAY(7, "Delay", "Wait for specified duration"),
        CONDITION(8, "Condition", "If-then-else conditional logic"),
        LOOP(9, "Loop", "Repeat actions for specified count"),
        
        // Input Actions
        INPUT_TEXT(10, "Input Text", "Type text into focused field"),
        
        // App Control
        LAUNCH_APP(11, "Launch App", "Launch specific application"),
        CLOSE_APP(12, "Close App", "Close specific application"),
        
        // Device Control
        PRESS_BACK(13, "Press Back", "Press device back button"),
        PRESS_HOME(14, "Press Home", "Press device home button"),
        TOGGLE_AIRPLANE_MODE(15, "Airplane Mode", "Toggle airplane mode on/off"),
        
        // Advanced Actions
        SCREENSHOT(16, "Screenshot", "Take screenshot for verification"),
        WAIT_FOR_ELEMENT(17, "Wait for Element", "Wait until element appears"),
        SCROLL(18, "Scroll", "Scroll in specified direction");

        private final int id;
        private final String displayName;
        private final String description;

        StepType(int id, String displayName, String description) {
            this.id = id;
            this.displayName = displayName;
            this.description = description;
        }

        public int getId() { return id; }
        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }

        public static StepType fromId(int id) {
            for (StepType type : values()) {
                if (type.id == id) return type;
            }
            return TAP; // Default fallback
        }
    }

    // Constructors
    public Step() {
        this.delay = 0;
        this.enabled = true;
        this.description = "";
    }

    public Step(@NonNull StepType type, String actionData) {
        this();
        this.type = type;
        this.actionData = actionData;
        this.description = type.getDisplayName();
    }

    public Step(@NonNull StepType type, String actionData, long delay) {
        this(type, actionData);
        this.delay = delay;
    }

    // Getters and Setters with validation
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    @NonNull
    public StepType getType() { return type; }
    public void setType(@NonNull StepType type) { 
        this.type = type;
        if (description == null || description.isEmpty()) {
            this.description = type.getDisplayName();
        }
    }

    public String getActionData() { return actionData; }
    public void setActionData(String actionData) { this.actionData = actionData; }

    public int getOrder() { return order; }
    public void setOrder(int order) { this.order = Math.max(0, order); }

    public long getTaskId() { return taskId; }
    public void setTaskId(long taskId) { this.taskId = taskId; }

    public long getDelay() { return delay; }
    public void setDelay(long delay) { 
        this.delay = Math.max(0, Math.min(delay, 300000)); // Max 5 minutes
    }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public String getDescription() { return description; }
    public void setDescription(String description) { 
        this.description = description != null ? description : "";
    }

    /**
     * Validate step configuration
     */
    public boolean isValid() {
        if (type == null) return false;
        if (actionData == null && requiresActionData()) return false;
        if (delay < 0) return false;
        return true;
    }

    /**
     * Check if this step type requires action data
     */
    private boolean requiresActionData() {
        switch (type) {
            case DELAY:
            case PRESS_BACK:
            case PRESS_HOME:
            case TOGGLE_AIRPLANE_MODE:
            case SCREENSHOT:
                return false;
            default:
                return true;
        }
    }

    /**
     * Get human-readable step summary
     */
    public String getSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append(type.getDisplayName());
        
        if (actionData != null && !actionData.isEmpty()) {
            summary.append(": ").append(getActionSummary());
        }
        
        if (delay > 0) {
            summary.append(" (Delay: ").append(delay).append("ms)");
        }
        
        return summary.toString();
    }

    /**
     * Get action-specific summary text
     */
    private String getActionSummary() {
        // This would parse actionData JSON and return human-readable summary
        // For now, return truncated action data
        if (actionData.length() > 50) {
            return actionData.substring(0, 47) + "...";
        }
        return actionData;
    }

    @Override
    public String toString() {
        return "Step{" +
                "id=" + id +
                ", type=" + type +
                ", order=" + order +
                ", delay=" + delay +
                ", enabled=" + enabled +
                '}';
    }
}
