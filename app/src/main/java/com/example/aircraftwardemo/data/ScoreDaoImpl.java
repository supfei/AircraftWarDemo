package com.example.aircraftwardemo.data;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import android.content.Context;
import android.os.Environment;
import android.util.Log;

/**
 * @author 有朝宿云
 * Created on 2025/10/17 17:30
 **/
public class ScoreDaoImpl implements ScoreDao {

    private List<ScoreRecord> scores;
    private Context context;
    private  String FILE_NAME = "scores.csv";

    // 修改构造函数，接收Context参数
    public ScoreDaoImpl(Context context) {
        this.context = context.getApplicationContext();  // 使用Application Context避免内存泄漏
        scores = loadScoresFromCSV();
    }

    // 确保使用context.getFilesDir()获取内部存储路径
    private String getFilePath() {
        if (context == null) {
            Log.e("ScoreDaoImpl", "Context is null!");
            return "scores.csv";  // 返回默认文件名

            // 回退方案：使用外部存储
//            File externalDir = Environment.getExternalStorageDirectory();
//            File file = new File(externalDir, "AircraftWar/scores.csv");
//            return file.getAbsolutePath();
        }
        File dir = context.getFilesDir();
        File file = new File(dir, FILE_NAME);
        Log.d("ScoreDaoImpl", "File path: " + file.getAbsolutePath());
        return file.getAbsolutePath();
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
        if (context == null) {
            Log.e("ScoreDaoImpl", "Context is null in loadScoresFromCSV");
            return loadedScores;
        }
        File file = new File(getFilePath());  // 使用getFilePath()

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
        if (context == null) {
            Log.e("ScoreDaoImpl", "Context is null in saveScoresToCSV");
            return;
        }
        File file = new File(getFilePath());  // 使用getFilePath()

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