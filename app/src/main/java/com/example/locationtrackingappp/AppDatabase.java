package com.example.locationtrackingappp;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {LocationEntity.class}, version = 2)
public abstract class AppDatabase extends RoomDatabase {
    public abstract LocationDao locationDao();

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "location_database")
                            .fallbackToDestructiveMigration()   // This fixes the error by clearing old DB
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}