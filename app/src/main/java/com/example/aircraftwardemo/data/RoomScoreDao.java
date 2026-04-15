package com.example.aircraftwardemo.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface RoomScoreDao {

    @Insert
    long insert(ScoreEntity scoreEntity);

    @Insert
    void insertAll(List<ScoreEntity> entities);

    @Query("SELECT * FROM scores ORDER BY score DESC, id ASC")
    List<ScoreEntity> getAllOrderByScoreDesc();

    @Query("SELECT COUNT(*) FROM scores")
    int count();

    @Query("DELETE FROM scores WHERE name = :name AND score = :score AND date = :date")
    int deleteByFields(String name, int score, String date);
}
