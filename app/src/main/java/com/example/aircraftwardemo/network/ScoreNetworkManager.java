package com.example.aircraftwardemo.network;

import android.content.Context;
import android.util.Log;

import com.example.aircraftwardemo.data.ScoreRecord;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * 网络管理类，处理分数上传和下载
 */
public class ScoreNetworkManager {

    private static final String TAG = "ScoreNetworkManager";

    // 服务器URL - 需要根据实际服务器地址修改
    private static final String BASE_URL = "https://your-server.com/api/";
    private static final String SUBMIT_SCORE_URL = BASE_URL + "score/submit";
    private static final String GET_TOP_SCORES_URL = BASE_URL + "score/top100";

    private OkHttpClient client;
    private Context context;
    private Gson gson;

    public ScoreNetworkManager(Context context) {
        this.context = context.getApplicationContext();
        this.gson = new Gson();

        // 创建OkHttp客户端
        this.client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)    // 连接超时
                .readTimeout(10, TimeUnit.SECONDS)       // 读取超时
                .writeTimeout(10, TimeUnit.SECONDS)      // 写入超时
                .build();
    }

    /**
     * 上传分数到服务器
     */
    public void uploadScore(ScoreRecord scoreRecord, ScoreUploadCallback callback) {
        new Thread(() -> {
            try {
                // 转换为JSON
                JSONObject json = new JSONObject();
                json.put("player_name", scoreRecord.getName());
                json.put("score", scoreRecord.getScore());
                json.put("date_time", scoreRecord.getDate());

                // 创建请求体
                MediaType JSON = MediaType.parse("application/json; charset=utf-8");
                RequestBody body = RequestBody.create(json.toString(), JSON);

                // 创建请求
                Request request = new Request.Builder()
                        .url(SUBMIT_SCORE_URL)
                        .post(body)
                        .addHeader("Content-Type", "application/json")
                        .build();

                // 发送请求
                Response response = client.newCall(request).execute();

                if (response.isSuccessful()) {
                    Log.d(TAG, "分数上传成功: " + scoreRecord.getScore());
                    callback.onSuccess();
                } else {
                    Log.e(TAG, "分数上传失败: " + response.code());
                    callback.onFailure("服务器错误: " + response.code());
                }

                response.close();

            } catch (Exception e) {
                Log.e(TAG, "上传分数异常: " + e.getMessage());
                callback.onFailure("网络错误: " + e.getMessage());
            }
        }).start();
    }

    /**
     * 获取全球排行榜
     */
    public void getGlobalRanking(RankingCallback callback) {
        new Thread(() -> {
            try {
                // 创建请求
                Request request = new Request.Builder()
                        .url(GET_TOP_SCORES_URL)
                        .get()
                        .build();

                // 发送请求
                Response response = client.newCall(request).execute();

                if (response.isSuccessful() && response.body() != null) {
                    String json = response.body().string();

                    // 解析JSON
                    List<ScoreRecord> scoreList = gson.fromJson(
                            json,
                            new TypeToken<List<ScoreRecord>>(){}.getType()
                    );

                    Log.d(TAG, "获取到排行榜数据: " + scoreList.size() + " 条记录");
                    callback.onSuccess(scoreList);

                } else {
                    Log.e(TAG, "获取排行榜失败: " + response.code());
                    callback.onFailure("获取失败: " + response.code());
                }

                response.close();

            } catch (Exception e) {
                Log.e(TAG, "获取排行榜异常: " + e.getMessage());
                callback.onFailure("网络错误: " + e.getMessage());
            }
        }).start();
    }

    /**
     * 测试服务器连接
     */
    public void testConnection(ConnectionTestCallback callback) {
        new Thread(() -> {
            try {
                Request request = new Request.Builder()
                        .url(BASE_URL)
                        .head()  // 使用HEAD方法，只获取头部信息
                        .build();

                Response response = client.newCall(request).execute();
                boolean isConnected = response.isSuccessful();
                response.close();

                callback.onTestResult(isConnected);

            } catch (Exception e) {
                callback.onTestResult(false);
            }
        }).start();
    }

    // 回调接口
    public interface ScoreUploadCallback {
        void onSuccess();
        void onFailure(String error);
    }

    public interface RankingCallback {
        void onSuccess(List<ScoreRecord> scores);
        void onFailure(String error);
    }

    public interface ConnectionTestCallback {
        void onTestResult(boolean isConnected);
    }

    // 设置服务器地址
    public void setServerUrl(String baseUrl) {
        // 可以动态设置服务器地址
    }
}