package com.example.runningtracker.model;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {Run.class}, version = 5, exportSchema = false)
public abstract class RunDatabase extends RoomDatabase {
    public abstract RunDao runDao();

    private static volatile RunDatabase INSTANCE;

    public static final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(4);

    public static RunDatabase getDatabase(final Context context) {

        if (INSTANCE == null) {
            synchronized (RunDatabase.class) {
                if (INSTANCE == null) {
                    // fallbacktoDestructive = no migration needed when database is destroyed
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(), RunDatabase.class, "run_database").fallbackToDestructiveMigration().build();
                }
            }
        }

        return INSTANCE;
    }
}
