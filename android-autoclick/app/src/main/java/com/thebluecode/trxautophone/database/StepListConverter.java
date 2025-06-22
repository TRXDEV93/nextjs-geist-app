package com.thebluecode.trxautophone.database;

import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.thebluecode.trxautophone.models.Step;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Enhanced TypeConverter for Step lists with improved error handling
 */
public class StepListConverter {
    private static final String TAG = "StepListConverter";
    private static final Gson gson = new GsonBuilder()
        .serializeNulls()
        .create();

    /**
     * Convert Step list to JSON string
     */
    @TypeConverter
    public static String fromStepList(List<Step> steps) {
        if (steps == null) {
            return null;
        }

        try {
            Type type = new TypeToken<List<Step>>() {}.getType();
            return gson.toJson(steps, type);
        } catch (Exception e) {
            android.util.Log.e(TAG, "Error converting steps to JSON", e);
            return null;
        }
    }

    /**
     * Convert JSON string to Step list
     */
    @TypeConverter
    public static List<Step> toStepList(String stepsJson) {
        if (stepsJson == null) {
            return new ArrayList<>();
        }

        try {
            Type type = new TypeToken<List<Step>>() {}.getType();
            List<Step> steps = gson.fromJson(stepsJson, type);
            return steps != null ? steps : new ArrayList<>();
        } catch (Exception e) {
            android.util.Log.e(TAG, "Error converting JSON to steps", e);
            return new ArrayList<>();
        }
    }

    /**
     * Convert single Step to JSON string
     */
    @TypeConverter
    public static String fromStep(Step step) {
        if (step == null) {
            return null;
        }

        try {
            return gson.toJson(step);
        } catch (Exception e) {
            android.util.Log.e(TAG, "Error converting step to JSON", e);
            return null;
        }
    }

    /**
     * Convert JSON string to single Step
     */
    @TypeConverter
    public static Step toStep(String stepJson) {
        if (stepJson == null) {
            return null;
        }

        try {
            return gson.fromJson(stepJson, Step.class);
        } catch (Exception e) {
            android.util.Log.e(TAG, "Error converting JSON to step", e);
            return null;
        }
    }

    /**
     * Convert Step list to compact JSON string
     */
    public static String toCompactJson(List<Step> steps) {
        if (steps == null) {
            return "[]";
        }

        try {
            // Create a minimal representation for storage efficiency
            List<Object[]> compact = new ArrayList<>();
            for (Step step : steps) {
                compact.add(new Object[]{
                    step.getId(),
                    step.getType().ordinal(),
                    step.getOrder(),
                    step.isEnabled(),
                    step.getDelay(),
                    step.getActionData()
                });
            }
            return gson.toJson(compact);
        } catch (Exception e) {
            android.util.Log.e(TAG, "Error creating compact JSON", e);
            return "[]";
        }
    }

    /**
     * Convert compact JSON string to Step list
     */
    public static List<Step> fromCompactJson(String json) {
        if (json == null || json.equals("[]")) {
            return new ArrayList<>();
        }

        try {
            Type type = new TypeToken<List<Object[]>>() {}.getType();
            List<Object[]> compact = gson.fromJson(json, type);
            List<Step> steps = new ArrayList<>();

            for (Object[] data : compact) {
                Step step = new Step();
                step.setId(((Number) data[0]).longValue());
                step.setType(Step.StepType.values()[((Number) data[1]).intValue()]);
                step.setOrder(((Number) data[2]).intValue());
                step.setEnabled((Boolean) data[3]);
                step.setDelay(((Number) data[4]).longValue());
                step.setActionData((String) data[5]);
                steps.add(step);
            }

            return steps;
        } catch (Exception e) {
            android.util.Log.e(TAG, "Error parsing compact JSON", e);
            return new ArrayList<>();
        }
    }

    /**
     * Validate JSON format
     */
    public static boolean isValidStepJson(String json) {
        if (json == null || json.trim().isEmpty()) {
            return false;
        }

        try {
            Type type = new TypeToken<List<Step>>() {}.getType();
            List<Step> steps = gson.fromJson(json, type);
            return steps != null;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Deep copy a Step list
     */
    public static List<Step> deepCopy(List<Step> steps) {
        if (steps == null) {
            return new ArrayList<>();
        }

        try {
            String json = fromStepList(steps);
            return toStepList(json);
        } catch (Exception e) {
            android.util.Log.e(TAG, "Error creating deep copy", e);
            return new ArrayList<>(steps); // Fallback to shallow copy
        }
    }

    /**
     * Merge two Step lists
     */
    public static List<Step> mergeLists(List<Step> list1, List<Step> list2) {
        List<Step> merged = new ArrayList<>();
        
        if (list1 != null) {
            merged.addAll(deepCopy(list1));
        }
        
        if (list2 != null) {
            List<Step> copy2 = deepCopy(list2);
            // Update order of second list
            int offset = merged.size();
            for (Step step : copy2) {
                step.setOrder(step.getOrder() + offset);
            }
            merged.addAll(copy2);
        }

        return merged;
    }

    /**
     * Get Gson instance
     */
    public static Gson getGson() {
        return gson;
    }
}
