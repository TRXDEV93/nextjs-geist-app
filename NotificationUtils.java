package com.thebluecode.trxautophone.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.thebluecode.trxautophone.MainActivity;
import com.thebluecode.trxautophone.PreferenceManager;
import com.thebluecode.trxautophone.R;
import com.thebluecode.trxautophone.models.Task;


public class NotificationUtils {
    private static final String CHANNEL_ID = "autoclick_service";
    private static final String CHANNEL_NAME = "AutoClick Service";
    private static final int NOTIFICATION_ID = 1001;
    private static final int PENDING_INTENT_FLAGS;

    static {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PENDING_INTENT_FLAGS = PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE;
        } else {
            PENDING_INTENT_FLAGS = PendingIntent.FLAG_UPDATE_CURRENT;
        }
    }

    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Notifications for AutoClick service status");
            channel.enableVibration(false);
            channel.setShowBadge(false);

            NotificationManager notificationManager = 
                context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    public static android.app.Notification createServiceNotification(Context context) {
        Intent notificationIntent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
            context, 0, notificationIntent, PENDING_INTENT_FLAGS);

        return new NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(context.getString(R.string.app_name))
            .setContentText("Service is running")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build();
    }

    public static void showTaskRunningNotification(Context context, Task task) {
        if (!new PreferenceManager(context).isNotificationEnabled()) {
            return;
        }

        Intent notificationIntent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
            context, 0, notificationIntent, PENDING_INTENT_FLAGS);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("Task Running")
            .setContentText(task.getName())
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setProgress(0, 0, true);

        NotificationManagerCompat.from(context)
            .notify(NOTIFICATION_ID, builder.build());
    }

    public static void updateTaskProgress(Context context, Task task, int progress) {
        if (!new PreferenceManager(context).isNotificationEnabled()) {
            return;
        }

        Intent notificationIntent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
            context, 0, notificationIntent, PENDING_INTENT_FLAGS);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("Task Running: " + task.getName())
            .setContentText(progress + "% complete")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setProgress(100, progress, false);

        NotificationManagerCompat.from(context)
            .notify(NOTIFICATION_ID, builder.build());
    }

    public static void showTaskCompletedNotification(Context context, Task task, boolean success) {
        if (!new PreferenceManager(context).isNotificationEnabled()) {
            return;
        }

        Intent notificationIntent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
            context, 0, notificationIntent, PENDING_INTENT_FLAGS);

        String title = success ? "Task Completed" : "Task Failed";
        String content = success ? 
            task.getName() + " completed successfully" : 
            task.getName() + " failed to complete";

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true);

        NotificationManagerCompat.from(context)
            .notify(NOTIFICATION_ID, builder.build());
    }

    public static void cancelTaskNotification(Context context) {
        NotificationManagerCompat.from(context)
            .cancel(NOTIFICATION_ID);
    }

    public static void showErrorNotification(Context context, String error) {
        if (!new PreferenceManager(context).isNotificationEnabled()) {
            return;
        }

        Intent notificationIntent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
            context, 0, notificationIntent, PENDING_INTENT_FLAGS);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("Error")
            .setContentText(error)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true);

        NotificationManagerCompat.from(context)
            .notify(NOTIFICATION_ID + 1, builder.build());
    }
}
