package com.thebluecode.trxautophone;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.thebluecode.trxautophone.models.Step;
import com.thebluecode.trxautophone.models.Task;

import java.lang.reflect.Type;
import java.util.List;

public class JsonConverter {
    private static final String TAG = "JsonConverter";
    private static final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    public static String taskToJson(Task task) {
        try {
            JsonObject taskObject = new JsonObject();
            taskObject.addProperty("version", 1); // For future compatibility
            taskObject.addProperty("name", task.getName());
            taskObject.addProperty("description", task.getDescription());
            taskObject.addProperty("created_at", task.getCreatedAt());
            taskObject.addProperty("updated_at", task.getUpdatedAt());
            taskObject.add("steps", gson.toJsonTree(task.getSteps()));
            
            return gson.toJson(taskObject);
        } catch (Exception e) {
            Log.e(TAG, "Error converting task to JSON", e);
            return null;
        }
    }

    public static Task jsonToTask(String json) {
        try {
            JsonObject taskObject = gson.fromJson(json, JsonObject.class);
            
            // Create new task
            Task task = new Task();
            task.setName(taskObject.get("name").getAsString());
            task.setDescription(taskObject.get("description").getAsString());
            task.setCreatedAt(taskObject.get("created_at").getAsLong());
            task.setUpdatedAt(taskObject.get("updated_at").getAsLong());
            
            // Convert steps
            Type stepListType = new TypeToken<List<Step>>(){}.getType();
            List<Step> steps = gson.fromJson(taskObject.get("steps"), stepListType);
            task.setSteps(steps);
            
            return task;
        } catch (Exception e) {
            Log.e(TAG, "Error converting JSON to task", e);
            return null;
        }
    }

    public static String taskListToJson(List<Task> tasks) {
        try {
            JsonObject exportObject = new JsonObject();
            exportObject.addProperty("version", 1);
            exportObject.add("tasks", gson.toJsonTree(tasks));
            
            return gson.toJson(exportObject);
        } catch (Exception e) {
            Log.e(TAG, "Error converting task list to JSON", e);
            return null;
        }
    }

    public static List<Task> jsonToTaskList(String json) {
        try {
            JsonObject importObject = gson.fromJson(json, JsonObject.class);
            Type taskListType = new TypeToken<List<Task>>(){}.getType();
            return gson.fromJson(importObject.get("tasks"), taskListType);
        } catch (Exception e) {
            Log.e(TAG, "Error converting JSON to task list", e);
            return null;
        }
    }

    public static String stepToJson(Step step) {
        try {
            return gson.toJson(step);
        } catch (Exception e) {
            Log.e(TAG, "Error converting step to JSON", e);
            return null;
        }
    }

    public static Step jsonToStep(String json) {
        try {
            return gson.fromJson(json, Step.class);
        } catch (Exception e) {
            Log.e(TAG, "Error converting JSON to step", e);
            return null;
        }
    }

    public static boolean isValidTaskJson(String json) {
        try {
            JsonObject taskObject = gson.fromJson(json, JsonObject.class);
            return taskObject.has("version") && 
                   taskObject.has("name") && 
                   taskObject.has("steps");
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isValidTaskListJson(String json) {
        try {
            JsonObject importObject = gson.fromJson(json, JsonObject.class);
            return importObject.has("version") && 
                   importObject.has("tasks");
        } catch (Exception e) {
            return false;
        }
    }
}
