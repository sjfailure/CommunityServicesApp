package io.github.sjfailure.kccommunityconnect;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;

public interface ApiService {
    @GET("/api")
    Call<ResponseBody> getData();
}
