package com.thebluecode.trxautophone.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.thebluecode.trxautophone.models.Step;
import com.thebluecode.trxautophone.models.Task;
import com.thebluecode.trxautophone.utils.Constants;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Enhanced Room database with migration support and type converters
 */
@Database(
    entities = {Task.class, Step.class},
    version = Constants.Database.VERSION,
    exportSchema = true
)
@TypeConverters({StepListConverter.class})
public abstract class TaskDatabase extends RoomDatabase {
    private static final String TAG = "TaskDatabase";
    private static volatile TaskDatabase instance;
    private static final ExecutorService databaseExecutor = 
        Executors.newFixedThreadPool(4);

    public abstract TaskDao taskDao();

    public static TaskDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (TaskDatabase.class) {
                if (instance == null) {
                    instance = createDatabase(context);
                }
            }
        }
        return instance;
    }

    private static TaskDatabase createDatabase(Context context) {
        return Room.databaseBuilder(
            context.getApplicationContext(),
            TaskDatabase.class,
            Constants.Database.NAME)
            .addCallback(new RoomDatabase.Callback() {
                @Override
                public void onCreate(@NonNull SupportSQLiteDatabase db) {
                    super.onCreate(db);
                    // Perform any initialization if needed
                }

                @Override
                public void onOpen(@NonNull SupportSQLiteDatabase db) {
                    super.onOpen(db);
                    // Perform any operations when database is opened
                }
            })
            .addMigrations(
                // Add migrations here when schema changes
                // Example: new Migration_1_2(), new Migration_2_3(), etc.
            )
            .setQueryExecutor(databaseExecutor)
            .build();
    }

    /**
     * Get database executor
     */
    public ExecutorService getExecutor() {
        return databaseExecutor;
    }

    /**
     * Example migration from version 1 to 2
     */
    /*
    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // Example migration SQL statements
            database.execSQL("ALTER TABLE Task ADD COLUMN description TEXT");
        }
    };
    */

    /**
     * Close database
     */
    public static void closeDatabase() {
        if (instance != null && instance.isOpen()) {
            instance.close();
            instance = null;
        }
    }

    /**
     * Clear all data
     */
    public void clearAllTables() {
        if (isOpen()) {
            databaseExecutor.execute(() -> {
                try {
                    clearAllTables();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }

    /**
     * Check if database is too large
     */
    public boolean isDatabaseTooLarge() {
        if (!isOpen()) return false;

        try {
            long size = getDatabaseFile().length();
            return size > Constants.Database.MAX_DATABASE_SIZE;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Create backup
     */
    public boolean createBackup(Context context) {
        if (!isOpen()) return false;

        try {
            String backupPath = context.getDatabasePath(
                Constants.Database.NAME + Constants.Database.BACKUP_SUFFIX).getPath();
            
            // Close database before backup
            close();

            // Copy database file
            java.nio.file.Files.copy(
                getDatabaseFile().toPath(),
                new java.io.File(backupPath).toPath(),
                java.nio.file.StandardCopyOption.REPLACE_EXISTING
            );

            // Reopen database
            instance = createDatabase(context);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Restore from backup
     */
    public boolean restoreFromBackup(Context context) {
        try {
            String backupPath = context.getDatabasePath(
                Constants.Database.NAME + Constants.Database.BACKUP_SUFFIX).getPath();
            java.io.File backupFile = new java.io.File(backupPath);
            
            if (!backupFile.exists()) {
                return false;
            }

            // Close current database
            close();

            // Copy backup file to main database
            java.nio.file.Files.copy(
                backupFile.toPath(),
                getDatabaseFile().toPath(),
                java.nio.file.StandardCopyOption.REPLACE_EXISTING
            );

            // Reopen database
            instance = createDatabase(context);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get database file
     */
    private java.io.File getDatabaseFile() {
        return new java.io.File(getOpenHelper().getWritableDatabase().getPath());
    }
}
