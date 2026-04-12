package io.github.sjfailure.kccommunityconnect;

import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface ApiService {
    @GET("/api")
    Call<ResponseBody> getData();

    @POST("/api/feedback/")
    Call<ResponseBody> sendFeedback(@Body Map<String, String> feedbackData);
}
