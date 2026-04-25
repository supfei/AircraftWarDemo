package com.example.aircraftwardemo.network.match;

import android.util.Log;

import com.example.aircraftwardemo.BuildConfig;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public class MultiplayerSessionManager {

    private static final String TAG = "MultiplayerSessionManager";
    private final MatchApiService matchApiService;

    public MultiplayerSessionManager() {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BuildConfig.SCORE_API_BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        this.matchApiService = retrofit.create(MatchApiService.class);
    }

    public String createPlayerId() {
        return UUID.randomUUID().toString();
    }

    public void createRoom(String playerId, RoomCallback callback) {
        RoomCreateRequest request = new RoomCreateRequest(playerId);
        matchApiService.createRoom(request).enqueue(new Callback<RoomCreateResponse>() {
            @Override
            public void onResponse(Call<RoomCreateResponse> call, Response<RoomCreateResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().roomId != null) {
                    callback.onSuccess(response.body().roomId);
                } else {
                    callback.onFailure("创建房间失败");
                }
            }

            @Override
            public void onFailure(Call<RoomCreateResponse> call, Throwable t) {
                String error = t.getMessage() == null ? "网络错误" : t.getMessage();
                Log.e(TAG, "createRoom failed: " + error);
                callback.onFailure(error);
            }
        });
    }

    public void joinRoom(String roomId, String playerId, JoinCallback callback) {
        JoinRoomRequest request = new JoinRoomRequest(roomId, playerId);
        matchApiService.joinRoom(request).enqueue(new Callback<SimpleResponse>() {
            @Override
            public void onResponse(Call<SimpleResponse> call, Response<SimpleResponse> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess();
                } else {
                    callback.onFailure("加入房间失败");
                }
            }

            @Override
            public void onFailure(Call<SimpleResponse> call, Throwable t) {
                String error = t.getMessage() == null ? "网络错误" : t.getMessage();
                Log.e(TAG, "joinRoom failed: " + error);
                callback.onFailure(error);
            }
        });
    }

    public void getRoomStatus(String roomId, String playerId, RoomStatusCallback callback) {
        matchApiService.getRoomStatus(roomId, playerId).enqueue(new Callback<RoomStatusResponse>() {
            @Override
            public void onResponse(Call<RoomStatusResponse> call, Response<RoomStatusResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onFailure("获取房间状态失败");
                }
            }

            @Override
            public void onFailure(Call<RoomStatusResponse> call, Throwable t) {
                String error = t.getMessage() == null ? "网络错误" : t.getMessage();
                callback.onFailure(error);
            }
        });
    }

    public void startMatch(String roomId, String playerId, SimpleCallback callback) {
        StartMatchRequest request = new StartMatchRequest(roomId, playerId);
        matchApiService.startMatch(request).enqueue(new Callback<SimpleResponse>() {
            @Override
            public void onResponse(Call<SimpleResponse> call, Response<SimpleResponse> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess();
                } else {
                    callback.onFailure("开始游戏失败");
                }
            }

            @Override
            public void onFailure(Call<SimpleResponse> call, Throwable t) {
                String error = t.getMessage() == null ? "网络错误" : t.getMessage();
                callback.onFailure(error);
            }
        });
    }

    public void syncScore(String roomId, String playerId, int myScore, ScoreSyncCallback callback) {
        ScoreSyncRequest request = new ScoreSyncRequest(roomId, playerId, myScore);
        matchApiService.syncScore(request).enqueue(new Callback<ScoreSyncResponse>() {
            @Override
            public void onResponse(Call<ScoreSyncResponse> call, Response<ScoreSyncResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body().enemyScore);
                } else {
                    callback.onFailure("同步分数失败");
                }
            }

            @Override
            public void onFailure(Call<ScoreSyncResponse> call, Throwable t) {
                String error = t.getMessage() == null ? "网络错误" : t.getMessage();
                callback.onFailure(error);
            }
        });
    }

    public interface RoomCallback {
        void onSuccess(String roomId);
        void onFailure(String error);
    }

    public interface JoinCallback {
        void onSuccess();
        void onFailure(String error);
    }

    public interface RoomStatusCallback {
        void onSuccess(RoomStatusResponse response);
        void onFailure(String error);
    }

    public interface ScoreSyncCallback {
        void onSuccess(int enemyScore);
        void onFailure(String error);
    }

    public interface SimpleCallback {
        void onSuccess();
        void onFailure(String error);
    }

    public interface MatchApiService {
        @POST("/api/match/room/create")
        Call<RoomCreateResponse> createRoom(@Body RoomCreateRequest request);

        @POST("/api/match/room/join")
        Call<SimpleResponse> joinRoom(@Body JoinRoomRequest request);

        @GET("/api/match/room/status")
        Call<RoomStatusResponse> getRoomStatus(
                @Query("room_id") String roomId,
                @Query("player_id") String playerId
        );

        @POST("/api/match/room/start")
        Call<SimpleResponse> startMatch(@Body StartMatchRequest request);

        @POST("/api/match/score/sync")
        Call<ScoreSyncResponse> syncScore(@Body ScoreSyncRequest request);
    }

    public static class RoomCreateRequest {
        public final String playerId;

        public RoomCreateRequest(String playerId) {
            this.playerId = playerId;
        }
    }

    public static class RoomCreateResponse {
        public String roomId;
    }

    public static class JoinRoomRequest {
        public final String roomId;
        public final String playerId;

        public JoinRoomRequest(String roomId, String playerId) {
            this.roomId = roomId;
            this.playerId = playerId;
        }
    }

    public static class StartMatchRequest {
        public final String roomId;
        public final String playerId;

        public StartMatchRequest(String roomId, String playerId) {
            this.roomId = roomId;
            this.playerId = playerId;
        }
    }

    public static class ScoreSyncRequest {
        public final String roomId;
        public final String playerId;
        public final int myScore;

        public ScoreSyncRequest(String roomId, String playerId, int myScore) {
            this.roomId = roomId;
            this.playerId = playerId;
            this.myScore = myScore;
        }
    }

    public static class ScoreSyncResponse {
        public int enemyScore;
    }

    public static class RoomStatusResponse {
        public boolean matched;
        public boolean started;
        public boolean host;
    }

    public static class SimpleResponse {
        public boolean success;
        public String message;
    }
}
