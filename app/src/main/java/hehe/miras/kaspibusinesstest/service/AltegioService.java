package hehe.miras.kaspibusinesstest.service;

import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
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
    private static final String LOGIN = "darmeddir@gmail.com";
    private static final String PASSWORD = "D1a2i3r4";
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

    public void authenticateSync() throws IOException {
        // Подготовка данных для аутентификации
        Map<String, String> credentials = new HashMap<>();
        credentials.put("login", LOGIN);
        credentials.put("password", PASSWORD);

        // Синхронный вызов API для аутентификации
        Response<AltegioAuthResponse> response = api.authenticate(
                PARTNER_TOKEN,
                "application/vnd.api.v2+json",
                credentials).execute();

        // Проверка успешности ответа
        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
            userToken = response.body().getData().getUserToken(); // Сохраняем токен
        }
    }

    public List<Appointment> fetchAppointmentsSync() throws IOException {
        List<Appointment> appointments = new ArrayList<>();

        // Сначала выполняем аутентификацию
        authenticateSync();

        // Формирование заголовка авторизации
        String authHeader = PARTNER_TOKEN + ", User " + userToken;

        // Синхронный вызов API для получения записей
        Response<AltegioResponse> response = api.getAppointments(
                authHeader,
                "application/vnd.api.v2+json",
                COMPANY_ID).execute();

        // Проверка успешности ответа
        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
            return new ArrayList<>(response.body().getData()); // Возвращаем список записей
        }

        return appointments;
    }
}
