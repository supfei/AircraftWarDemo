package com.example.aircraftwardemo.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import androidx.room.Room;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author 有朝宿云
 * Created on 2025/10/17 17:30
 **/
public class ScoreDaoImpl implements ScoreDao {

    private static final String TAG = "ScoreDaoImpl";
    private static final String FILE_NAME = "scores.csv";
    private static final String DB_NAME = "scores.db";
    private static final String PREF_NAME = "score_storage_migration";
    private static final String KEY_CSV_MIGRATED = "scores_csv_migrated";
    private final Context context;
    private final RoomScoreDao roomScoreDao;
    private final ExecutorService dbExecutor;
    private final Object cacheLock = new Object();
    private final List<ScoreRecord> cache = new ArrayList<>();

    public ScoreDaoImpl(Context context) {
        this.context = context.getApplicationContext();
        AppDatabase database = Room.databaseBuilder(this.context, AppDatabase.class, DB_NAME)
                .build();
        this.roomScoreDao = database.roomScoreDao();
        this.dbExecutor = Executors.newSingleThreadExecutor();
        dbExecutor.execute(() -> {
            migrateCsvToRoomIfNeeded();
            refreshCacheFromDb();
        });
    }

    @Override
    public void addScore(ScoreRecord scoreRecord) {
        synchronized (cacheLock) {
            cache.add(scoreRecord);
            sortCacheInPlace();
        }
        dbExecutor.execute(() -> roomScoreDao.insert(toEntity(scoreRecord)));
    }

    @Override
    public List<ScoreRecord> getAllScores() {
        synchronized (cacheLock) {
            return new ArrayList<>(cache);
        }
    }

    @Override
    public void deleteScore(String name, int score, String date) {
        synchronized (cacheLock) {
            cache.removeIf(record ->
                    record.getName().equals(name)
                            && record.getScore() == score
                            && record.getDate().equals(date)
            );
        }
        dbExecutor.execute(() -> roomScoreDao.deleteByFields(name, score, date));
    }

    private void migrateCsvToRoomIfNeeded() {
        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        if (preferences.getBoolean(KEY_CSV_MIGRATED, false)) {
            return;
        }

        try {
            int rowCount = roomScoreDao.count();
            if (rowCount > 0) {
                markMigrationDone(preferences);
                return;
            }

            List<ScoreRecord> csvRecords = loadScoresFromCSV();
            if (csvRecords.isEmpty()) {
                markMigrationDone(preferences);
                return;
            }

            List<ScoreEntity> entities = new ArrayList<>(csvRecords.size());
            for (ScoreRecord record : csvRecords) {
                entities.add(toEntity(record));
            }
            roomScoreDao.insertAll(entities);
            Log.d(TAG, "CSV migration success, count=" + entities.size());
            markMigrationDone(preferences);
        } catch (Exception e) {
            Log.e(TAG, "CSV migration failed", e);
        }
    }

    private void refreshCacheFromDb() {
        List<ScoreEntity> entities = roomScoreDao.getAllOrderByScoreDesc();
        List<ScoreRecord> latest = new ArrayList<>(entities.size());
        for (ScoreEntity entity : entities) {
            latest.add(toRecord(entity));
        }
        synchronized (cacheLock) {
            cache.clear();
            cache.addAll(latest);
        }
    }

    private void sortCacheInPlace() {
        cache.sort((a, b) -> Integer.compare(b.getScore(), a.getScore()));
    }

    private void markMigrationDone(SharedPreferences preferences) {
        preferences.edit().putBoolean(KEY_CSV_MIGRATED, true).apply();
    }

    private List<ScoreRecord> loadScoresFromCSV() {
        List<ScoreRecord> loadedScores = new ArrayList<>();
        File file = getCsvFile();

        if (!file.exists() || file.length() == 0) {
            return loadedScores;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            boolean isFirstLine = true;
            while ((line = reader.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }
                String[] parts = line.split(",");
                if (parts.length >= 3) {
                    String name = parts[0].trim();
                    try {
                        int score = Integer.parseInt(parts[1].trim());
                        String date = parts[2].trim();
                        loadedScores.add(new ScoreRecord(name, score, date));
                    } catch (NumberFormatException e) {
                        Log.w(TAG, "Skip bad score row: " + line);
                    }
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "Read CSV failed", e);
        }
        return loadedScores;
    }

    private File getCsvFile() {
        return new File(context.getFilesDir(), FILE_NAME);
    }

    private ScoreEntity toEntity(ScoreRecord record) {
        return new ScoreEntity(record.getName(), record.getScore(), record.getDate());
    }

    private ScoreRecord toRecord(ScoreEntity entity) {
        return new ScoreRecord(entity.name, entity.score, entity.date);
    }
}