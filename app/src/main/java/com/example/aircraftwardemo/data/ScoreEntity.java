package com.example.aircraftwardemo.data;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "scores")
public class ScoreEntity {

    @PrimaryKey(autoGenerate = true)
    public long id;

    @ColumnInfo(name = "name")
    public String name;

    @ColumnInfo(name = "score")
    public int score;

    @ColumnInfo(name = "date")
    public String date;

    public ScoreEntity(String name, int score, String date) {
        this.name = name;
        this.score = score;
        this.date = date;
    }
}
