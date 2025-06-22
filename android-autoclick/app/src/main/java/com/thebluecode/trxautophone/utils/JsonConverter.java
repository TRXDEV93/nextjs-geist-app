package com.thebluecode.trxautophone.utils;

import android.graphics.Point;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.thebluecode.trxautophone.models.LogEntry;
import com.thebluecode.trxautophone.models.Step;
import com.thebluecode.trxautophone.models.Task;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Enhanced JSON converter with custom type adapters and validation
 */
public class JsonConverter {
    private static final String TAG = "JsonConverter";
    private static final Gson gson;

    static {
        gson = new GsonBuilder()
            .registerTypeAdapter(Point.class, new PointTypeAdapter())
            .registerTypeAdapter(Step.class, new StepTypeAdapter())
            .registerTypeAdapter(Task.class, new TaskTypeAdapter())
            .registerTypeAdapter(LogEntry.class, new LogEntryTypeAdapter())
            .setPrettyPrinting()
            .create();
    }

    private JsonConverter() {
        // Private constructor to prevent instantiation
    }

    /**
     * Convert object to JSON
     */
    @Nullable
    public static String toJson(@Nullable Object obj) {
        if (obj == null) return null;
        try {
            return gson.toJson(obj);
        } catch (Exception e) {
            Log.e(TAG, "Error converting to JSON", e);
            return null;
        }
    }

    /**
     * Convert JSON to object
     */
    @Nullable
    public static <T> T fromJson(@Nullable String json, @NonNull Class<T> classOfT) {
        if (json == null) return null;
        try {
            return gson.fromJson(json, classOfT);
        } catch (Exception e) {
            Log.e(TAG, "Error converting from JSON", e);
            return null;
        }
    }

    /**
     * Convert JSON to list
     */
    @NonNull
    public static <T> List<T> fromJsonList(@Nullable String json, @NonNull Class<T> classOfT) {
        if (json == null) return new ArrayList<>();
        try {
            com.google.gson.reflect.TypeToken<List<T>> typeToken = 
                new com.google.gson.reflect.TypeToken<List<T>>() {};
            return gson.fromJson(json, typeToken.getType());
        } catch (Exception e) {
            Log.e(TAG, "Error converting from JSON list", e);
            return new ArrayList<>();
        }
    }

    /**
     * Validate JSON format
     */
    public static boolean isValidJson(@Nullable String json) {
        if (json == null) return false;
        try {
            gson.fromJson(json, Object.class);
            return true;
        } catch (JsonParseException e) {
            return false;
        }
    }

    /**
     * Point type adapter
     */
    private static class PointTypeAdapter implements JsonSerializer<Point>, JsonDeserializer<Point> {
        @Override
        public JsonElement serialize(Point src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject obj = new JsonObject();
            obj.addProperty("x", src.x);
            obj.addProperty("y", src.y);
            return obj;
        }

        @Override
        public Point deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) 
            throws JsonParseException {
            JsonObject obj = json.getAsJsonObject();
            return new Point(
                obj.get("x").getAsInt(),
                obj.get("y").getAsInt()
            );
        }
    }

    /**
     * Step type adapter
     */
    private static class StepTypeAdapter implements JsonSerializer<Step>, JsonDeserializer<Step> {
        @Override
        public JsonElement serialize(Step src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject obj = new JsonObject();
            obj.addProperty("id", src.getId());
            obj.addProperty("taskId", src.getTaskId());
            obj.addProperty("type", src.getType().name());
            obj.addProperty("order", src.getOrder());
            obj.addProperty("enabled", src.isEnabled());
            obj.addProperty("delay", src.getDelay());
            obj.addProperty("actionData", src.getActionData());
            return obj;
        }

        @Override
        public Step deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) 
            throws JsonParseException {
            JsonObject obj = json.getAsJsonObject();
            Step step = new Step();
            step.setId(obj.get("id").getAsLong());
            step.setTaskId(obj.get("taskId").getAsLong());
            step.setType(Step.StepType.valueOf(obj.get("type").getAsString()));
            step.setOrder(obj.get("order").getAsInt());
            step.setEnabled(obj.get("enabled").getAsBoolean());
            step.setDelay(obj.get("delay").getAsLong());
            step.setActionData(obj.get("actionData").getAsString());
            return step;
        }
    }

    /**
     * Task type adapter
     */
    private static class TaskTypeAdapter implements JsonSerializer<Task>, JsonDeserializer<Task> {
        @Override
        public JsonElement serialize(Task src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject obj = new JsonObject();
            obj.addProperty("id", src.getId());
            obj.addProperty("name", src.getName());
            obj.addProperty("description", src.getDescription());
            obj.addProperty("enabled", src.isEnabled());
            obj.addProperty("repeatCount", src.getRepeatCount());
            obj.addProperty("repeatDelay", src.getRepeatDelay());
            obj.addProperty("executionCount", src.getExecutionCount());
            obj.addProperty("successCount", src.getSuccessCount());
            obj.addProperty("lastExecuted", src.getLastExecuted());
            obj.add("steps", context.serialize(src.getSteps()));
            return obj;
        }

        @Override
        public Task deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) 
            throws JsonParseException {
            JsonObject obj = json.getAsJsonObject();
            Task task = new Task(obj.get("name").getAsString());
            task.setId(obj.get("id").getAsLong());
            task.setDescription(obj.get("description").getAsString());
            task.setEnabled(obj.get("enabled").getAsBoolean());
            task.setRepeatCount(obj.get("repeatCount").getAsInt());
            task.setRepeatDelay(obj.get("repeatDelay").getAsLong());
            task.setExecutionCount(obj.get("executionCount").getAsInt());
            task.setSuccessCount(obj.get("successCount").getAsInt());
            task.setLastExecuted(obj.get("lastExecuted").getAsLong());
            
            Type stepListType = new com.google.gson.reflect.TypeToken<List<Step>>(){}.getType();
            List<Step> steps = context.deserialize(obj.get("steps"), stepListType);
            task.setSteps(steps);
            
            return task;
        }
    }

    /**
     * LogEntry type adapter
     */
    private static class LogEntryTypeAdapter implements JsonSerializer<LogEntry>, 
                                                      JsonDeserializer<LogEntry> {
        @Override
        public JsonElement serialize(LogEntry src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject obj = new JsonObject();
            obj.addProperty("id", src.getId());
            obj.addProperty("taskId", src.getTaskId());
            obj.addProperty("taskName", src.getTaskName());
            obj.addProperty("timestamp", src.getTimestamp());
            obj.addProperty("success", src.isSuccess());
            obj.addProperty("stepsCompleted", src.getStepsCompleted());
            obj.addProperty("duration", src.getDuration());
            obj.addProperty("error", src.getError());
            obj.addProperty("details", src.getDetails());
            obj.addProperty("deviceInfo", src.getDeviceInfo());
            return obj;
        }

        @Override
        public LogEntry deserialize(JsonElement json, Type typeOfT, 
                                  JsonDeserializationContext context) throws JsonParseException {
            JsonObject obj = json.getAsJsonObject();
            LogEntry log = new LogEntry();
            log.setId(obj.get("id").getAsLong());
            log.setTaskId(obj.get("taskId").getAsLong());
            log.setTaskName(obj.get("taskName").getAsString());
            log.setTimestamp(obj.get("timestamp").getAsLong());
            log.setSuccess(obj.get("success").getAsBoolean());
            log.setStepsCompleted(obj.get("stepsCompleted").getAsInt());
            log.setDuration(obj.get("duration").getAsLong());
            log.setError(obj.get("error").getAsString());
            log.setDetails(obj.get("details").getAsString());
            log.setDeviceInfo(obj.get("deviceInfo").getAsString());
            return log;
        }
    }
}
