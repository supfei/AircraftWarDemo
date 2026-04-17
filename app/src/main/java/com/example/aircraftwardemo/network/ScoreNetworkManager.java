package com.example.aircraftwardemo.network;

import android.content.Context;
import android.util.Log;

import com.example.aircraftwardemo.data.ScoreRecord;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * 网络管理类，处理分数上传和下载
 */
public class ScoreNetworkManager {

    // 用于日志定位网络流程。
    private static final String TAG = "ScoreNetworkManager";

    // 服务器根地址，接口路径由 ScoreApiService 维护。
    private static final String BASE_URL = "https://your-server.com/";

    // 提供全局 JSON 序列化能力。
    private final Gson gson;

    // Retrofit 接口实例，统一所有分数相关请求。
    private final ScoreApiService scoreApiService;

    // 初始化网络客户端并绑定 API 接口。
    public ScoreNetworkManager(Context context) {
        this.gson = new Gson();

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        this.scoreApiService = retrofit.create(ScoreApiService.class);
    }

    /**
     * 上传分数到服务器
     */
    // 通过 Retrofit 提交网络模型分数记录。
    public void uploadScore(ScoreRecord scoreRecord, ScoreUploadCallback callback) {
        NetworkScoreRecord networkScoreRecord = new NetworkScoreRecord(scoreRecord);
        scoreApiService.submitScore(networkScoreRecord).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "分数上传成功: " + scoreRecord.getScore());
                    callback.onSuccess();
                } else {
                    Log.e(TAG, "分数上传失败: " + response.code());
                    callback.onFailure("服务器错误: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                String error = t.getMessage() == null ? "未知网络错误" : t.getMessage();
                Log.e(TAG, "上传分数异常: " + error);
                callback.onFailure("网络错误: " + error);
            }
        });
    }

    /**
     * 获取全球排行榜
     */
    // 拉取网络模型列表并映射成 UI 使用模型。
    public void getGlobalRanking(RankingCallback callback) {
        scoreApiService.getTopScores().enqueue(new Callback<List<NetworkScoreRecord>>() {
            @Override
            public void onResponse(Call<List<NetworkScoreRecord>> call, Response<List<NetworkScoreRecord>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<ScoreRecord> scoreList = mapToScoreRecords(response.body());
                    Log.d(TAG, "获取到排行榜数据: " + scoreList.size() + " 条记录");
                    callback.onSuccess(scoreList);
                } else {
                    Log.e(TAG, "获取排行榜失败: " + response.code());
                    callback.onFailure("获取失败: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<NetworkScoreRecord>> call, Throwable t) {
                String error = t.getMessage() == null ? "未知网络错误" : t.getMessage();
                Log.e(TAG, "获取排行榜异常: " + error);
                callback.onFailure("网络错误: " + error);
            }
        });
    }

    /**
     * 测试服务器连接
     */
    // 通过排行榜接口快速探测服务可用性。
    public void testConnection(ConnectionTestCallback callback) {
        scoreApiService.getTopScores().enqueue(new Callback<List<NetworkScoreRecord>>() {
            @Override
            public void onResponse(Call<List<NetworkScoreRecord>> call, Response<List<NetworkScoreRecord>> response) {
                callback.onTestResult(response.isSuccessful());
            }

            @Override
            public void onFailure(Call<List<NetworkScoreRecord>> call, Throwable t) {
                callback.onTestResult(false);
            }
        });
    }

    // 上传结果回调接口。
    public interface ScoreUploadCallback {
        void onSuccess();
        void onFailure(String error);
    }

    // 排行榜结果回调接口。
    public interface RankingCallback {
        void onSuccess(List<ScoreRecord> scores);
        void onFailure(String error);
    }

    // 连接测试回调接口。
    public interface ConnectionTestCallback {
        void onTestResult(boolean isConnected);
    }

    // 将网络模型列表转换为本地展示模型列表。
    private List<ScoreRecord> mapToScoreRecords(List<NetworkScoreRecord> networkScoreList) {
        List<ScoreRecord> scoreRecords = new ArrayList<>();
        if (networkScoreList == null) {
            return scoreRecords;
        }

        for (NetworkScoreRecord networkScoreRecord : networkScoreList) {
            if (networkScoreRecord == null) {
                continue;
            }
            scoreRecords.add(new ScoreRecord(
                    networkScoreRecord.getPlayerName(),
                    networkScoreRecord.getScore(),
                    networkScoreRecord.getDateTime()
            ));
        }
        return scoreRecords;
    }

    // 设置服务器地址
    public void setServerUrl(String baseUrl) {
        // 可以动态设置服务器地址
    }
}