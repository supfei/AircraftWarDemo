package com.example.aircraftwardemo.network;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;
import java.util.List;

/**
 * Retrofit API接口定义
 */
public interface ScoreApiService {

    // 提交分数
    @POST("/api/score/submit")
    Call<ResponseBody> submitScore(@Body NetworkScoreRecord scoreRecord);

    // 获取排行榜
    @GET("/api/score/top100")
    Call<List<NetworkScoreRecord>> getTopScores();

    // 可选的：按时间范围查询
    @GET("/api/score/range")
    Call<List<NetworkScoreRecord>> getScoresByDateRange(
            @Query("start_date") String startDate,
            @Query("end_date") String endDate
    );
}
