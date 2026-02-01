package com.example.routify;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Route.class, Vehicle.class}, version = 3)
public abstract class AppDatabase extends RoomDatabase {
    public abstract RouteDao routeDao();
    public abstract VehicleDao vehicleDao();
    private static AppDatabase instance;

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                    AppDatabase.class, "routify_db").fallbackToDestructiveMigration().build();
        }
        return instance;
    }
}