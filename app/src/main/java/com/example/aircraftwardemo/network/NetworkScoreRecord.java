package com.example.aircraftwardemo.network;

import com.example.aircraftwardemo.data.ScoreRecord;
import com.google.gson.annotations.SerializedName;

/**
 * 用于网络传输的分数记录
 */
public class NetworkScoreRecord {
    @SerializedName("player_name")
    private String playerName;

    @SerializedName("score")
    private int score;

    @SerializedName("date_time")
    private String dateTime;

    public NetworkScoreRecord(String playerName, int score, String dateTime) {
        this.playerName = playerName;
        this.score = score;
        this.dateTime = dateTime;
    }

    // 从本地ScoreRecord转换
    public NetworkScoreRecord(ScoreRecord scoreRecord) {
        this.playerName = scoreRecord.getName();
        this.score = scoreRecord.getScore();
        this.dateTime = scoreRecord.getDate();
    }

    // Getters
    public String getPlayerName() { return playerName; }
    public int getScore() { return score; }
    public String getDateTime() { return dateTime; }
}
