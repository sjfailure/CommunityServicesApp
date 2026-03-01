package io.github.sjfailure.kccommunityconnect;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;

public interface ApiProvider {
    @GET("/api/providers")
    Call<ResponseBody> getData();

//    @GET("/api/{id}")
//    Call<ResponseBody> getEventDetails(@Path("id") String eventId);


}
