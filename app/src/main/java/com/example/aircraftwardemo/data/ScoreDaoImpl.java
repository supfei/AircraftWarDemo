package com.example.aircraftwardemo.data;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author 有朝宿云
 * Created on 2025/10/17 17:30
 **/
public class ScoreDaoImpl implements ScoreDao {

    private List<ScoreRecord> scores;
    private static final String FILE_NAME = "data/scores.csv";

    public ScoreDaoImpl() {
        scores = loadScoresFromCSV();
    }

    @Override
    public void addScore(ScoreRecord scoreRecord) {
        scores.add(scoreRecord);
        saveScoresToCSV();
    }

    @Override
    public List<ScoreRecord> getAllScores() {
        // 按分数降序排序（可选，用于排行榜）
        scores.sort((a, b) -> Integer.compare(b.getScore(), a.getScore()));
        return scores;
    }
    @Override
    public void deleteScore(String name, int score, String date) {
        // 直接从内存中的 scores 列表中删除匹配的记录
        scores.removeIf(record ->
                record.getName().equals(name) &&
                        record.getScore() == score &&
                        record.getDate().equals(date)
        );

        // 将更新后的列表重新保存到 CSV 文件
        saveScoresToCSV();
    }

    // ==================== CSV 文件读取 ====================
    private List<ScoreRecord> loadScoresFromCSV() {
        List<ScoreRecord> loadedScores = new ArrayList<>();
        File file = new File(FILE_NAME);

        // 如果文件不存在或为空，返回空列表
        if (!file.exists() || file.length() == 0) {
            return loadedScores;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            boolean isFirstLine = true;

            while ((line = reader.readLine()) != null) {
                // 跳过表头
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }

                // 按逗号分割
                String[] parts = line.split(",");
                if (parts.length >= 3) {
                    String name = parts[0].trim();
                    try {
                        int score = Integer.parseInt(parts[1].trim());
                        String date = parts[2].trim();
                        loadedScores.add(new ScoreRecord(name, score, date));
                    } catch (NumberFormatException e) {
                        System.err.println("无法解析分数: " + line);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("读取 CSV 文件失败: " + e.getMessage());
            e.printStackTrace();
        }

        return loadedScores;
    }

    // ==================== CSV 文件写入 ====================
    private void saveScoresToCSV() {
        File file = new File(FILE_NAME);

        // 确保 data 目录存在
        File dir = file.getParentFile();
        if (dir != null && !dir.exists()) {
            dir.mkdirs(); // 创建 data 目录
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            // 写入表头
            writer.write("name,score,date");
            writer.newLine();

            // 写入每一条记录
            for (ScoreRecord record : scores) {
                writer.write(record.getName() + "," +
                        record.getScore() + "," +
                        record.getDate());
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("保存 CSV 文件失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}