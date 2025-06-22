package com.thebluecode.trxautophone.utils;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.thebluecode.trxautophone.JsonConverter;
import com.thebluecode.trxautophone.models.Task;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class FileUtils {
    private static final String TAG = "FileUtils";
    private static final String TASKS_DIR = "tasks";
    private static final String EXPORT_DIR = "exports";
    private static final String FILE_EXTENSION = ".json";

    public static File getTasksDirectory(Context context) {
        File tasksDir = new File(context.getFilesDir(), TASKS_DIR);
        if (!tasksDir.exists()) {
            tasksDir.mkdirs();
        }
        return tasksDir;
    }

    public static File getExportDirectory(Context context) {
        File exportDir = new File(context.getExternalFilesDir(null), EXPORT_DIR);
        if (!exportDir.exists()) {
            exportDir.mkdirs();
        }
        return exportDir;
    }

    @Nullable
    public static File exportTask(@NonNull Context context, @NonNull Task task) {
        try {
            String fileName = generateFileName(task.getName());
            File exportFile = new File(getExportDirectory(context), fileName);
            
            String json = JsonConverter.taskToJson(task);
            if (json == null) {
                return null;
            }

            try (BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(exportFile)))) {
                writer.write(json);
            }

            return exportFile;
        } catch (IOException e) {
            Log.e(TAG, "Error exporting task", e);
            return null;
        }
    }

    @Nullable
    public static File exportTasks(@NonNull Context context, @NonNull List<Task> tasks) {
        try {
            String fileName = "tasks_backup_" + 
                new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date()) + 
                FILE_EXTENSION;
            File exportFile = new File(getExportDirectory(context), fileName);
            
            String json = JsonConverter.taskListToJson(tasks);
            if (json == null) {
                return null;
            }

            try (BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(exportFile)))) {
                writer.write(json);
            }

            return exportFile;
        } catch (IOException e) {
            Log.e(TAG, "Error exporting tasks", e);
            return null;
        }
    }

    @Nullable
    public static Task importTask(@NonNull Context context, @NonNull Uri uri) {
        try {
            String json = readFileContent(context, uri);
            if (json == null || !JsonConverter.isValidTaskJson(json)) {
                return null;
            }
            return JsonConverter.jsonToTask(json);
        } catch (Exception e) {
            Log.e(TAG, "Error importing task", e);
            return null;
        }
    }

    @Nullable
    public static List<Task> importTasks(@NonNull Context context, @NonNull Uri uri) {
        try {
            String json = readFileContent(context, uri);
            if (json == null || !JsonConverter.isValidTaskListJson(json)) {
                return null;
            }
            return JsonConverter.jsonToTaskList(json);
        } catch (Exception e) {
            Log.e(TAG, "Error importing tasks", e);
            return null;
        }
    }

    @Nullable
    private static String readFileContent(@NonNull Context context, @NonNull Uri uri) {
        StringBuilder content = new StringBuilder();
        try (InputStream inputStream = context.getContentResolver().openInputStream(uri);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append('\n');
            }
            return content.toString();
        } catch (IOException e) {
            Log.e(TAG, "Error reading file content", e);
            return null;
        }
    }

    private static String generateFileName(String taskName) {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
                .format(new Date());
        String sanitizedName = taskName.replaceAll("[^a-zA-Z0-9]", "_");
        return sanitizedName + "_" + timestamp + FILE_EXTENSION;
    }

    public static void deleteExportedFile(File file) {
        if (file != null && file.exists()) {
            file.delete();
        }
    }

    public static boolean isJsonFile(Uri uri) {
        String path = uri.getPath();
        return path != null && path.toLowerCase().endsWith(FILE_EXTENSION);
    }

    public static void cleanupOldExports(Context context, long maxAgeMillis) {
        File exportDir = getExportDirectory(context);
        if (!exportDir.exists()) {
            return;
        }

        File[] files = exportDir.listFiles();
        if (files == null) {
            return;
        }

        long now = System.currentTimeMillis();
        for (File file : files) {
            if (now - file.lastModified() > maxAgeMillis) {
                file.delete();
            }
        }
    }
}
