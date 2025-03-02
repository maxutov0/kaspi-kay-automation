package hehe.miras.kaspibusinesstest.service;

import android.util.Log;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SupabaseService {

    private static final String SUPABASE_URL = "https://lwfrkbkcvkgzxolvvpra.supabase.co/rest/v1/";
    private static final String SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Imx3ZnJrYmtjdmtnenhvbHZ2cHJhIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDA5MzA0OTYsImV4cCI6MjA1NjUwNjQ5Nn0.BaV5YELMqHErjr2WkZiHyNRlSBnCNpOtC7QJUle6AOs"; // Замените

    private SupabaseApi api;

    public SupabaseService() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(SUPABASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        api = retrofit.create(SupabaseApi.class);
    }

    // Добавление записи в Supabase
    public void addProcessedAppointment(int appointmentId, long timestamp) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", appointmentId);
        data.put("timestamp", timestamp);

        Call<Void> call = api.insertAppointment(data, SUPABASE_KEY);
        call.enqueue(new retrofit2.Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, retrofit2.Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d("SupabaseService", "Запись успешно добавлена в Supabase: ID=" + appointmentId);
                } else {
                    Log.e("SupabaseService", "Ошибка при добавлении записи: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("SupabaseService", "Ошибка подключения: " + t.getMessage());
            }
        });
    }

    // Проверка, была ли запись уже обработана (асинхронная)
    public void isAppointmentProcessed(int appointmentId, SupabaseCallback<Boolean> callback) {
        Call<Map<String, Object>> call = api.getAppointment(appointmentId, SUPABASE_KEY);
        call.enqueue(new retrofit2.Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, retrofit2.Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onResult(true); // Запись найдена, значит, она уже обработана
                } else {
                    callback.onResult(false); // Запись не найдена
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Log.e("SupabaseService", "Ошибка подключения: " + t.getMessage());
                callback.onResult(false);
            }
        });
    }

    // Проверка, была ли запись уже обработана (синхронная)
    public boolean isAppointmentProcessedSync(int appointmentId) {
        Call<Map<String, Object>> call = api.getAppointment(appointmentId, SUPABASE_KEY);
        Log.d("SupabaseService", "Запрос к Supabase: " + call.request().url()); // Логируем URL запроса
    
        try {
            Response<Map<String, Object>> response = call.execute();
            Log.d("SupabaseService", "Ответ от Supabase: " + response.code()); // Логируем код ответа
            if (response.isSuccessful() && response.body() != null) {
                return true; // Запись найдена, значит, она уже обработана
            }
        } catch (IOException e) {
            Log.e("SupabaseService", "Ошибка подключения: " + e.getMessage());
        }
        return false; // Запись не найдена
    }

    // Получение времени отправки счета
    public long getAppointmentTimestamp(int appointmentId) {
        Call<Map<String, Object>> call = api.getAppointment(appointmentId, SUPABASE_KEY);
        try {
            Response<Map<String, Object>> response = call.execute();
            if (response.isSuccessful() && response.body() != null) {
                return (long) response.body().get("timestamp");
            }
        } catch (IOException e) {
            Log.e("SupabaseService", "Ошибка при получении времени отправки счета: " + e.getMessage());
        }
        return -1; // Возвращаем -1, если время не найдено
    }

    // Добавление messageId в Supabase
    public void addMessageId(String messageId) {
        Map<String, Object> data = new HashMap<>();
        data.put("message_id", messageId);

        Call<Void> call = api.insertMessageId(data, SUPABASE_KEY);
        call.enqueue(new retrofit2.Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, retrofit2.Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d("SupabaseService", "Message ID успешно добавлен в Supabase: " + messageId);
                } else {
                    Log.e("SupabaseService", "Ошибка при добавлении Message ID: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("SupabaseService", "Ошибка подключения: " + t.getMessage());
            }
        });
    }

    // Интерфейс для обратного вызова
    public interface SupabaseCallback<T> {
        void onResult(T result);
    }

    // Интерфейс Supabase API
    private interface SupabaseApi {
        @POST("processed_appointments")
        Call<Void> insertAppointment(@Body Map<String, Object> data, @Query("apikey") String apiKey);

        @GET("processed_appointments")
        Call<Map<String, Object>> getAppointment(@Query("id") int appointmentId, @Query("apikey") String apiKey);

        @POST("message_ids") // Предположим, что таблица для message_id называется "message_ids"
        Call<Void> insertMessageId(@Body Map<String, Object> data, @Query("apikey") String apiKey);
    }
}