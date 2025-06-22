package com.thebluecode.trxautophone.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.thebluecode.trxautophone.AutoClickApplication;
import com.thebluecode.trxautophone.MainActivity;
import com.thebluecode.trxautophone.R;
import com.thebluecode.trxautophone.models.Task;

/**
 * Enhanced notification utility class with improved channel management
 * and notification building
 */
public class NotificationUtils {
    private static final String TAG = "NotificationUtils";

    private NotificationUtils() {
        // Private constructor to prevent instantiation
    }

    /**
     * Create notification channels for Android O and above
     */
    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                NotificationManager notificationManager = 
                    context.getSystemService(NotificationManager.class);

                // Service channel
                NotificationChannel serviceChannel = new NotificationChannel(
                    Constants.Notification.CHANNEL_ID,
                    Constants.Notification.CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_LOW
                );
                serviceChannel.setDescription(Constants.Notification.CHANNEL_DESCRIPTION);
                serviceChannel.enableLights(true);
                serviceChannel.setLightColor(Color.BLUE);
                serviceChannel.enableVibration(false);
                serviceChannel.setShowBadge(false);
                notificationManager.createNotificationChannel(serviceChannel);

                // Task channel
                NotificationChannel taskChannel = new NotificationChannel(
                    "task_channel",
                    "Task Execution",
                    NotificationManager.IMPORTANCE_DEFAULT
                );
                taskChannel.setDescription("Notifications for task execution status");
                taskChannel.enableLights(true);
                taskChannel.setLightColor(Color.GREEN);
                taskChannel.enableVibration(true);
                taskChannel.setShowBadge(true);
                notificationManager.createNotificationChannel(taskChannel);

                // Error channel
                NotificationChannel errorChannel = new NotificationChannel(
                    "error_channel",
                    "Errors",
                    NotificationManager.IMPORTANCE_HIGH
                );
                errorChannel.setDescription("Important error notifications");
                errorChannel.enableLights(true);
                errorChannel.setLightColor(Color.RED);
                errorChannel.enableVibration(true);
                errorChannel.setShowBadge(true);
                notificationManager.createNotificationChannel(errorChannel);

                Log.d(TAG, "Notification channels created successfully");
            } catch (Exception e) {
                Log.e(TAG, "Error creating notification channels", e);
            }
        }
    }

    /**
     * Show the persistent service notification
     */
    public static void showServiceNotification(@NonNull AutoClickApplication application,
                                             @NonNull String title,
                                             @NonNull String content) {
        try {
            Intent notificationIntent = new Intent(application, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(
                application,
                0,
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            Notification notification = new NotificationCompat.Builder(application, Constants.Notification.CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(content)
                .setSmallIcon(R.drawable.ic_service_notification)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();

            NotificationManagerCompat.from(application)
                .notify(Constants.Notification.SERVICE_NOTIFICATION_ID, notification);

            Log.d(TAG, "Service notification shown");
        } catch (Exception e) {
            Log.e(TAG, "Error showing service notification", e);
        }
    }

    /**
     * Show task execution notification
     */
    public static void showTaskNotification(@NonNull AutoClickApplication application,
                                          @NonNull Task task,
                                          @NonNull String status,
                                          int progress) {
        try {
            // Create intent for opening the app
            Intent notificationIntent = new Intent(application, MainActivity.class);
            notificationIntent.putExtra("task_id", task.getId());
            PendingIntent pendingIntent = PendingIntent.getActivity(
                application,
                0,
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            // Create stop action
            Intent stopIntent = new Intent(Constants.IntentActions.ACTION_STOP_TASK);
            stopIntent.putExtra("task_id", task.getId());
            PendingIntent stopPendingIntent = PendingIntent.getBroadcast(
                application,
                1,
                stopIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            NotificationCompat.Builder builder = new NotificationCompat.Builder(application, "task_channel")
                .setContentTitle(task.getName())
                .setContentText(status)
                .setSmallIcon(R.drawable.ic_task_notification)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .addAction(R.drawable.ic_stop, "Stop", stopPendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

            if (progress >= 0) {
                builder.setProgress(100, progress, false);
            }

            NotificationManagerCompat.from(application)
                .notify(Constants.Notification.TASK_NOTIFICATION_ID, builder.build());

            Log.d(TAG, "Task notification shown: " + status);
        } catch (Exception e) {
            Log.e(TAG, "Error showing task notification", e);
        }
    }

    /**
     * Update task progress notification
     */
    public static void updateTaskProgress(@NonNull AutoClickApplication application,
                                        @NonNull Task task,
                                        @NonNull String status,
                                        int progress) {
        try {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(application, "task_channel")
                .setContentTitle(task.getName())
                .setContentText(status)
                .setSmallIcon(R.drawable.ic_task_notification)
                .setProgress(100, progress, false)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

            NotificationManagerCompat.from(application)
                .notify(Constants.Notification.PROGRESS_NOTIFICATION_ID, builder.build());

            Log.d(TAG, String.format("Task progress updated: %s (%d%%)", status, progress));
        } catch (Exception e) {
            Log.e(TAG, "Error updating task progress notification", e);
        }
    }

    /**
     * Show error notification
     */
    public static void showErrorNotification(@NonNull AutoClickApplication application,
                                           @NonNull String error) {
        try {
            Intent notificationIntent = new Intent(application, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(
                application,
                0,
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            NotificationCompat.Builder builder = new NotificationCompat.Builder(application, "error_channel")
                .setContentTitle("Error")
                .setContentText(error)
                .setSmallIcon(R.drawable.ic_error_notification)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

            NotificationManagerCompat.from(application)
                .notify(Constants.Notification.ERROR_NOTIFICATION_ID, builder.build());

            Log.e(TAG, "Error notification shown: " + error);
        } catch (Exception e) {
            Log.e(TAG, "Error showing error notification", e);
        }
    }

    /**
     * Cancel a specific notification
     */
    public static void cancelNotification(@NonNull Context context, int notificationId) {
        try {
            NotificationManagerCompat.from(context).cancel(notificationId);
            Log.d(TAG, "Notification cancelled: " + notificationId);
        } catch (Exception e) {
            Log.e(TAG, "Error cancelling notification", e);
        }
    }

    /**
     * Cancel all notifications
     */
    public static void cancelAllNotifications(@NonNull Context context) {
        try {
            NotificationManagerCompat.from(context).cancelAll();
            Log.d(TAG, "All notifications cancelled");
        } catch (Exception e) {
            Log.e(TAG, "Error cancelling all notifications", e);
        }
    }

    /**
     * Check if notifications are enabled
     */
    public static boolean areNotificationsEnabled(@NonNull Context context) {
        try {
            return NotificationManagerCompat.from(context).areNotificationsEnabled();
        } catch (Exception e) {
            Log.e(TAG, "Error checking notification status", e);
            return false;
        }
    }

    /**
     * Get notification channel importance
     */
    public static int getChannelImportance(@NonNull Context context, @NonNull String channelId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                NotificationManager notificationManager = 
                    context.getSystemService(NotificationManager.class);
                NotificationChannel channel = notificationManager.getNotificationChannel(channelId);
                return channel != null ? channel.getImportance() : NotificationManager.IMPORTANCE_NONE;
            } catch (Exception e) {
                Log.e(TAG, "Error getting channel importance", e);
                return NotificationManager.IMPORTANCE_NONE;
            }
        }
        return NotificationManager.IMPORTANCE_NONE;
    }

    /**
     * Build a basic notification
     */
    @NonNull
    public static NotificationCompat.Builder buildBasicNotification(@NonNull Context context,
                                                                  @NonNull String channelId,
                                                                  @NonNull String title,
                                                                  @NonNull String content,
                                                                  @Nullable PendingIntent intent) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(R.drawable.ic_notification_default)
            .setAutoCancel(true);

        if (intent != null) {
            builder.setContentIntent(intent);
        }

        return builder;
    }
}
