package io.github.sjfailure.kccommunityconnect;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class FetchProviderData {

    private static Retrofit retrofit = null;

    public interface OnDataReadyCallback {
        void onDataReady(JSONObject data);
        void onFailure(Exception e);
    }

    /**
     * Singleton accessor for Retrofit.
     * Configures a custom OkHttpClient with 30-second timeouts.
     */
    private static Retrofit getRetrofitInstance() {
        if (retrofit == null) {
            // 1. Create the OkHttpClient with custom timeouts
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .build();

            // 2. Build Retrofit using that client
            retrofit = new Retrofit.Builder()
                    .baseUrl(BuildConfig.API_BASE_URL)
                    .client(okHttpClient) // Use the custom client
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    public void fetch(OnDataReadyCallback callback) {
        // Use the singleton instance
        ApiProvider service = getRetrofitInstance().create(ApiProvider.class);
        Call<ResponseBody> call = service.getData();

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String jsonData = response.body().string();
                        JSONObject jobj = new JSONObject(jsonData);
                        callback.onDataReady(jobj);
                    } catch (IOException | JSONException e) {
                        callback.onFailure(e);
                    }
                } else {
                    callback.onFailure(new Exception("API error: " + response.code()));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                callback.onFailure(new Exception(t));
            }
        });
    }
}