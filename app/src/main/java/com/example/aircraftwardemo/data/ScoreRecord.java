package com.example.aircraftwardemo.data;

import java.io.Serializable;

/**
 * @author 有朝宿云
 * Created on 2025/10/17 17:25
 **/
public class ScoreRecord implements Serializable {
    private String name;      // 玩家名称
    private int score;        // 分数
    private String date;      // 日期时间（String 格式，如 "2024-06-01 12:00:00"）

    // 构造方法
    public ScoreRecord(String name, int score, String date) {
        this.name = name;
        this.score = score;
        this.date = date;
    }

    // Getter 方法
    public String getName() { return name; }
    public int getScore() { return score; }
    public String getDate() { return date; }

    @Override
    public String toString() {
        return name + "," + score + "," + date;
    }
}