package com.thebluecode.trxautophone.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.Gson;

@Entity(tableName = "task_table")
public class TaskEntity {
    @PrimaryKey(autoGenerate = true)
    private int id;

    private String name;
    private String stepsJson; // JSON string của List<Step>
    private int repeatCount = 1;

    public TaskEntity(String name, String stepsJson, int repeatCount) {
        this.name = name;
        this.stepsJson = stepsJson;
        this.repeatCount = repeatCount;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getStepsJson() {
        return stepsJson;
    }

    public int getRepeatCount() {
        return repeatCount;
    }

    public void setRepeatCount(int repeatCount) {
        this.repeatCount = repeatCount;
    }

    public static TaskEntity fromTask(Task task) {
        Gson gson = new Gson();
        String stepsJson = gson.toJson(task.getSteps());
        return new TaskEntity(task.getName(), stepsJson, task.getRepeatCount());
    }
}
