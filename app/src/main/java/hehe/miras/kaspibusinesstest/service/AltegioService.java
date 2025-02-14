package hehe.miras.kaspibusinesstest.service;

import android.util.Log;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.Callback;
import retrofit2.Response;
import hehe.miras.kaspibusinesstest.model.Appointment;
import hehe.miras.kaspibusinesstest.api.AltegioApi;
import hehe.miras.kaspibusinesstest.model.AltegioAuthResponse;
import hehe.miras.kaspibusinesstest.model.AltegioResponse;

public class AltegioService {
    private static final String BASE_URL = "https://api.alteg.io/";
    private static final String PARTNER_TOKEN = "Bearer 6f8c65e9z3j5ssnjsc45"; // <-- Реальный токен
    private static final int COMPANY_ID = 690192;

    private AltegioApi api;
    private String userToken; // Сохраненный userToken после авторизации

    public AltegioService() {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();

        api = retrofit.create(AltegioApi.class);
    }

    // Метод для получения user_token
    public void authenticateAndFetchAppointments(Callback<List<Appointment>> callback) {
        Map<String, String> credentials = new HashMap<>();
        credentials.put("login", "darmeddir@gmail.com");
        credentials.put("password", "D1a2i3r4");

        api.authenticate(PARTNER_TOKEN, "application/vnd.api.v2+json", credentials).enqueue(new Callback<AltegioAuthResponse>() {
            @Override
            public void onResponse(Call<AltegioAuthResponse> call, Response<AltegioAuthResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    userToken = response.body().getData().getUserToken();
                    Log.d("Altegio", "Получен userToken: " + userToken);
                    fetchAppointments(callback);
                } else {
                    Log.e("Altegio", "Ошибка авторизации: " + response.code());
                    callback.onFailure(null, new Throwable("Ошибка авторизации: " + response.code()));
                }
            }

            @Override
            public void onFailure(Call<AltegioAuthResponse> call, Throwable t) {
                Log.e("Altegio", "Ошибка подключения при авторизации: " + t.getMessage());
                callback.onFailure(null, t);
            }
        });
    }

    // Метод для получения списка записей с правильными заголовками
    private void fetchAppointments(Callback<List<Appointment>> callback) {
        if (userToken == null) {
            Log.e("Altegio", "userToken отсутствует, невозможно выполнить запрос");
            callback.onFailure(null, new Throwable("Не удалось получить userToken"));
            return;
        }

        String authHeader = PARTNER_TOKEN + ", User " + userToken;

        api.getAppointments(authHeader, "application/vnd.api.v2+json", COMPANY_ID).enqueue(new Callback<AltegioResponse>() {
            @Override
            public void onResponse(Call<AltegioResponse> call, Response<AltegioResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    callback.onResponse(null, Response.success(response.body().getData()));
                } else {
                    Log.e("Altegio", "Ошибка получения записей: " + response.code());
                    callback.onFailure(null, new Throwable("Ошибка API: " + response.code()));
                }
            }

            @Override
            public void onFailure(Call<AltegioResponse> call, Throwable t) {
                Log.e("Altegio", "Ошибка подключения: " + t.getMessage());
                callback.onFailure(null, t);
            }
        });
    }
}
