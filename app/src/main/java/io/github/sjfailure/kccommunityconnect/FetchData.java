package io.github.sjfailure.kccommunityconnect;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class FetchData {

    public interface OnDataReadyCallback {
        void onDataReady(JSONObject data) throws InterruptedException;
        void onFailure(Exception e);
    }

    public void fetch(OnDataReadyCallback callback) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:8000/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService service = retrofit.create(ApiService.class);
        Call<ResponseBody> call = service.getData();
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
                        String jsonData = response.body().string();
                        JSONObject jobj = new JSONObject(jsonData);
                        callback.onDataReady(jobj);
                    } catch (IOException | JSONException | InterruptedException e) {
                        callback.onFailure(e);
                    }
                } else {
                    callback.onFailure(new Exception("API call failed with response code: " + response.code()));
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                callback.onFailure(new Exception(t));
            }
        });
    }

    public void detailFetch(OnDataReadyCallback callback, String eventId) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:8000/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService service = retrofit.create(ApiService.class);
        Call<ResponseBody> call = service.getEventDetails(eventId);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
                        String jsonData = response.body().string();
                        JSONObject jobj = new JSONObject(jsonData);
                        callback.onDataReady(jobj);
                    } catch (IOException | JSONException | InterruptedException e) {
                        callback.onFailure(e);
                    }
                } else {
                    callback.onFailure(new Exception("API call failed with response code: " + response.code()));
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                callback.onFailure(new Exception(t));
            }
        });
    }
}
