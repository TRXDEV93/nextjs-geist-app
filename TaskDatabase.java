package com.thebluecode.trxautophone.database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.thebluecode.trxautophone.models.Step;
import com.thebluecode.trxautophone.models.StepListConverter;
import com.thebluecode.trxautophone.models.Task;
import com.thebluecode.trxautophone.utils.Constants;


@Database(entities = {Task.class, Step.class}, version = Constants.DATABASE_VERSION, exportSchema = false)
@TypeConverters({StepListConverter.class})
public abstract class TaskDatabase extends RoomDatabase {
    
    private static volatile TaskDatabase INSTANCE;
    private static RoomDatabase.Callback roomCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(SupportSQLiteDatabase db) {
            super.onCreate(db);
        }

        @Override
        public void onOpen(SupportSQLiteDatabase db) {
            super.onOpen(db);
        }
    };
    
    public abstract TaskDao taskDao();

    public static TaskDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (TaskDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                        context.getApplicationContext(),
                        TaskDatabase.class,
                        Constants.DATABASE_NAME
                    )
                    .fallbackToDestructiveMigration()
                    .addCallback(roomCallback)
                    .build();
                }
            }
        }
        return INSTANCE;
    }

    public static void addDatabaseCallback(RoomDatabase.Callback callback) {
        if (callback != null) {
            roomCallback = callback;
        }
    }

    public static void destroyInstance() {
        INSTANCE = null;
    }
}
