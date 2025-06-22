package com.thebluecode.trxautophone.utils;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Enhanced file utility class with improved error handling and compression
 */
public class FileUtils {
    private static final String TAG = "FileUtils";
    private static final int BUFFER_SIZE = 8192;

    private FileUtils() {
        // Private constructor to prevent instantiation
    }

    /**
     * Read text from file
     */
    @Nullable
    public static String readTextFile(@NonNull Context context, @NonNull Uri uri) {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
            new InputStreamReader(context.getContentResolver().openInputStream(uri)))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            return content.toString();
        } catch (IOException e) {
            Log.e(TAG, "Error reading file: " + uri, e);
            return null;
        }
    }

    /**
     * Write text to file
     */
    public static boolean writeTextFile(@NonNull Context context, @NonNull Uri uri, 
                                      @NonNull String content) {
        try (BufferedWriter writer = new BufferedWriter(
            new OutputStreamWriter(context.getContentResolver().openOutputStream(uri)))) {
            writer.write(content);
            return true;
        } catch (IOException e) {
            Log.e(TAG, "Error writing file: " + uri, e);
            return false;
        }
    }

    /**
     * Copy file
     */
    public static boolean copyFile(@NonNull File source, @NonNull File destination) {
        if (!source.exists()) {
            return false;
        }

        try (FileChannel sourceChannel = new FileInputStream(source).getChannel();
             FileChannel destChannel = new FileOutputStream(destination).getChannel()) {
            destChannel.transferFrom(sourceChannel, 0, sourceChannel.size());
            return true;
        } catch (IOException e) {
            Log.e(TAG, "Error copying file", e);
            return false;
        }
    }

    /**
     * Create zip file
     */
    public static boolean createZipFile(@NonNull File[] files, @NonNull File zipFile) {
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile))) {
            byte[] buffer = new byte[BUFFER_SIZE];

            for (File file : files) {
                if (!file.exists() || !file.isFile()) continue;

                ZipEntry entry = new ZipEntry(file.getName());
                zos.putNextEntry(entry);

                try (FileInputStream fis = new FileInputStream(file)) {
                    int length;
                    while ((length = fis.read(buffer)) > 0) {
                        zos.write(buffer, 0, length);
                    }
                }
                zos.closeEntry();
            }
            return true;
        } catch (IOException e) {
            Log.e(TAG, "Error creating zip file", e);
            return false;
        }
    }

    /**
     * Extract zip file
     */
    public static boolean extractZipFile(@NonNull File zipFile, @NonNull File destDir) {
        if (!destDir.exists() && !destDir.mkdirs()) {
            return false;
        }

        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
            byte[] buffer = new byte[BUFFER_SIZE];
            ZipEntry entry;

            while ((entry = zis.getNextEntry()) != null) {
                File file = new File(destDir, entry.getName());

                // Create parent directories if needed
                File parent = file.getParentFile();
                if (parent != null && !parent.exists() && !parent.mkdirs()) {
                    continue;
                }

                try (FileOutputStream fos = new FileOutputStream(file)) {
                    int length;
                    while ((length = zis.read(buffer)) > 0) {
                        fos.write(buffer, 0, length);
                    }
                }
                zis.closeEntry();
            }
            return true;
        } catch (IOException e) {
            Log.e(TAG, "Error extracting zip file", e);
            return false;
        }
    }

    /**
     * Create backup file
     */
    public static File createBackupFile(@NonNull Context context, @NonNull String prefix) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US);
        String timestamp = sdf.format(new Date());
        return new File(context.getExternalFilesDir(null), 
            prefix + "_" + timestamp + ".backup");
    }

    /**
     * Get file size
     */
    public static long getFileSize(@NonNull Context context, @NonNull Uri uri) {
        try {
            return context.getContentResolver().openFileDescriptor(uri, "r").getStatSize();
        } catch (IOException e) {
            Log.e(TAG, "Error getting file size: " + uri, e);
            return -1;
        }
    }

    /**
     * Delete file or directory
     */
    public static boolean deleteRecursively(@NonNull File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()) {
            File[] files = fileOrDirectory.listFiles();
            if (files != null) {
                for (File child : files) {
                    deleteRecursively(child);
                }
            }
        }
        return fileOrDirectory.delete();
    }

    /**
     * Clean directory
     */
    public static boolean cleanDirectory(@NonNull File directory, long maxAge, long maxSize) {
        if (!directory.exists() || !directory.isDirectory()) {
            return false;
        }

        File[] files = directory.listFiles();
        if (files == null) {
            return false;
        }

        long currentTime = System.currentTimeMillis();
        long totalSize = 0;

        // First pass: Calculate total size and delete old files
        for (File file : files) {
            if (maxAge > 0 && currentTime - file.lastModified() > maxAge) {
                file.delete();
                continue;
            }
            totalSize += file.length();
        }

        // Second pass: Delete files if total size exceeds maxSize
        if (maxSize > 0 && totalSize > maxSize) {
            // Sort files by last modified time
            java.util.Arrays.sort(files, (f1, f2) -> 
                Long.compare(f1.lastModified(), f2.lastModified()));

            // Delete oldest files until we're under maxSize
            for (File file : files) {
                if (totalSize <= maxSize) break;
                totalSize -= file.length();
                file.delete();
            }
        }

        return true;
    }

    /**
     * Get MIME type from file extension
     */
    @NonNull
    public static String getMimeType(@NonNull String fileName) {
        String extension = getFileExtension(fileName);
        switch (extension.toLowerCase()) {
            case "json":
                return "application/json";
            case "txt":
                return "text/plain";
            case "html":
                return "text/html";
            case "csv":
                return "text/csv";
            case "zip":
                return "application/zip";
            case "pdf":
                return "application/pdf";
            case "png":
                return "image/png";
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            default:
                return "application/octet-stream";
        }
    }

    /**
     * Get file extension
     */
    @NonNull
    public static String getFileExtension(@NonNull String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        return lastDot > 0 ? fileName.substring(lastDot + 1) : "";
    }
}
