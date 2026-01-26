package io.github.sjfailure.kccommunityconnect;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface ApiService {
    @GET("/api")
    Call<ResponseBody> getData();

    @GET("/api/{id}")
    Call<ResponseBody> getEventDetails(@Path("id") String eventId);
}
