package com.example.aircraftwardemo.network;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.HTTP;
import retrofit2.http.Path;
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

    // 按分数字段组合删除一条记录（DELETE + Body）。
    @HTTP(method = "DELETE", path = "/api/score/delete", hasBody = true)
    Call<ResponseBody> deleteScore(@Body NetworkScoreRecord scoreRecord);

    // 按数据库 id 删除一条记录。
    @DELETE("/api/score/delete/{id}")
    Call<ResponseBody> deleteScoreById(@Path("id") long id);
}
