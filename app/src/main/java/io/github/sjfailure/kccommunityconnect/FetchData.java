package io.github.sjfailure.kccommunityconnect;

import androidx.annotation.NonNull;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class FetchData {

    private static Retrofit retrofit = null;

    public interface OnDataReadyCallback {
        void onDataReady(JSONObject data);
        void onFailure(Exception e);
    }

    public interface OnFeedbackSentCallback {
        void onSuccess();
        void onFailure(Exception e);
    }

    private static Retrofit getRetrofitInstance() {
        if (retrofit == null) {
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BuildConfig.API_BASE_URL)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    public void fetch(OnDataReadyCallback callback) {
        ApiService service = getRetrofitInstance().create(ApiService.class);
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

    public void sendFeedback(Map<String, String> feedbackData, OnFeedbackSentCallback callback) {
        ApiService service = getRetrofitInstance().create(ApiService.class);
        Call<ResponseBody> call = service.sendFeedback(feedbackData);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess();
                } else {
                    callback.onFailure(new Exception("Feedback submission failed: " + response.code()));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                callback.onFailure(new Exception(t));
            }
        });
    }
}
