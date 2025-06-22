package com.thebluecode.trxautophone.models;

import androidx.room.TypeConverter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.List;

public class StepListConverter {
    private static final Gson gson = new Gson();

    @TypeConverter
    public static String fromStepList(List<Step> steps) {
        if (steps == null) {
            return null;
        }
        return gson.toJson(steps);
    }

    @TypeConverter
    public static List<Step> toStepList(String stepsString) {
        if (stepsString == null) {
            return null;
        }
        Type listType = new TypeToken<List<Step>>() {}.getType();
        return gson.fromJson(stepsString, listType);
    }
}
