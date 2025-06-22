package com.thebluecode.trxautophone.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;
import java.util.ArrayList;
import java.util.List;

@Entity(tableName = "tasks")
public class Task {
    @PrimaryKey(autoGenerate = true)
    private long id;
    private String name;
    private String description;
    @TypeConverters(StepListConverter.class)
    private List<Step> steps;
    private boolean isEnabled;
    private long createdAt;
    private long updatedAt;

    public Task() {
        this.steps = new ArrayList<>();
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    // Getters and Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public List<Step> getSteps() { return steps; }
    public void setSteps(List<Step> steps) { this.steps = steps; }
    
    public boolean isEnabled() { return isEnabled; }
    public void setEnabled(boolean enabled) { isEnabled = enabled; }
    
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    
    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }

    public void addStep(Step step) {
        if (steps == null) {
            steps = new ArrayList<>();
        }
        steps.add(step);
        updatedAt = System.currentTimeMillis();
    }

    public void removeStep(int position) {
        if (steps != null && position >= 0 && position < steps.size()) {
            steps.remove(position);
            updatedAt = System.currentTimeMillis();
        }
    }
}
