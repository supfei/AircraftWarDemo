package com.example.aircraftwardemo.data;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {ScoreEntity.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract RoomScoreDao roomScoreDao();
}
