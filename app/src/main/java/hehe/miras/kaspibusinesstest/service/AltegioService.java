package hehe.miras.kaspibusinesstest.service;

import android.util.Log;

import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.Callback;
import retrofit2.Response;
import hehe.miras.kaspibusinesstest.model.Appointment;
import hehe.miras.kaspibusinesstest.api.AltegioApi;

public class AltegioService {
    private static final String BASE_URL = "https://api.alteg.io/v1/";
    private static final String API_TOKEN = "Bearer YOUR_REAL_API_TOKEN"; // <-- Обязательно используй реальный токен
    private static final int COMPANY_ID = 456; // ID вашей компании

    private AltegioApi api;

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

    // Новый метод: получает список записей через колбэк
    public void getAppointments(Callback<List<Appointment>> callback) {
        Call<List<Appointment>> call = api.getAppointments(API_TOKEN, "application/json", COMPANY_ID);
        call.enqueue(callback);
    }
}