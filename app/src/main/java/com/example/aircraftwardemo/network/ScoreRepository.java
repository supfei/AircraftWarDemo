package com.example.aircraftwardemo.network;

import android.content.Context;
import android.util.Log;
import com.example.aircraftwardemo.data.*;
import java.util.List;

/**
 * 统一管理本地和远程分数存储
 */
public class ScoreRepository {

    private static final String TAG = "ScoreRepository";
    private static ScoreRepository instance;

    private ScoreDao localDao;
    private ScoreNetworkManager networkManager;
    private Context context;

    private ScoreRepository(Context context) {
        this.context = context.getApplicationContext();
        this.localDao = new ScoreDaoImpl(this.context);
        this.networkManager = new ScoreNetworkManager(context);
    }

    public static synchronized ScoreRepository getInstance(Context context) {
        if (instance == null) {
            instance = new ScoreRepository(context);
        }
        return instance;
    }

    /**
     * 添加分数记录（同时保存到本地和上传到服务器）
     */
    public void addScore(ScoreRecord scoreRecord) {
        // 1. 先保存到本地
        localDao.addScore(scoreRecord);
        Log.d(TAG, "分数已保存到本地: " + scoreRecord.getScore());

        // 2. 异步上传到服务器
        networkManager.uploadScore(scoreRecord, new ScoreNetworkManager.ScoreUploadCallback() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "分数上传到服务器成功: " + scoreRecord.getScore());
            }

            @Override
            public void onFailure(String error) {
                Log.e(TAG, "分数上传到服务器失败: " + error);
                // 可以在这里实现失败重试逻辑
            }
        });
    }

    /**
     * 获取本地排行榜
     */
    public List<ScoreRecord> getLocalScores() {
        return localDao.getAllScores();
    }

    /**
     * 获取全球排行榜
     */
    public void getGlobalScores(ScoreNetworkManager.RankingCallback callback) {
        networkManager.getGlobalRanking(callback);
    }

    /**
     * 删除分数记录
     */
    public void deleteScore(ScoreRecord scoreRecord) {
        localDao.deleteScore(
                scoreRecord.getName(),
                scoreRecord.getScore(),
                scoreRecord.getDate()
        );
    }

    /**
     * 测试网络连接
     */
    public void testNetworkConnection(ScoreNetworkManager.ConnectionTestCallback callback) {
        networkManager.testConnection(callback);
    }
}