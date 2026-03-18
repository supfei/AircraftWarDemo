package com.example.aircraftwardemo.data;

import java.util.List;

public interface ScoreDao {
    void addScore(ScoreRecord scoreRecord);
    List<ScoreRecord> getAllScores();
    void deleteScore(String name, int score, String date);
}