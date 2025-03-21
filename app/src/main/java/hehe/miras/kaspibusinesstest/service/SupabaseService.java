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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.List;
import java.util.Locale;

import hehe.miras.kaspibusinesstest.api.SupabaseApi;
import hehe.miras.kaspibusinesstest.model.Appointment;

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

    public void addAppointmentSync(Appointment appointment) throws IOException {
        Map<String, Object> data = new HashMap<>();
        data.put("altegio_id", appointment.getId());
        data.put("phone", appointment.getClient().getPhone());
        data.put("name", appointment.getClient().getName());
        // Input date string in Asia/Oral timezone
        String dateString = appointment.getDate(); // e.g., "2025-03-06 04:45:00"

        // Create a SimpleDateFormat for parsing the input date string
        SimpleDateFormat oralDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        TimeZone oralTimeZone = TimeZone.getTimeZone("Asia/Oral");
        oralDateFormat.setTimeZone(oralTimeZone);

        // Create a SimpleDateFormat for formatting the date in UTC
        SimpleDateFormat utcDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        TimeZone utcTimeZone = TimeZone.getTimeZone("UTC");
        utcDateFormat.setTimeZone(utcTimeZone);

        try {
            // Step 1: Parse the date string in Asia/Oral timezone
            Date date = oralDateFormat.parse(dateString);
            // Step 2: Format the parsed date into UTC
            String utcDateString = utcDateFormat.format(date);
            // Step 3: Store the UTC date string in the data map
            data.put("date", utcDateString);
        } catch (ParseException e) {
            Log.e("SupabaseService", "Failed to parse date", e);
        }

        Response<Void> response = api.insertAppointment(data,
                SUPABASE_KEY).execute();

        if (!response.isSuccessful()) {
            throw new IOException("Failed to insert appointment: " +
                    response.errorBody().string());
        }
    }

    public List<Appointment> getAppointmentsSync(String altegioId, String createdAt, String status, String orderBy,
            String date)
            throws IOException {
        Response<List<Object>> response = api.getAppointment(altegioId, createdAt, status, orderBy, date, SUPABASE_KEY)
                .execute();

        if (!response.isSuccessful()) {
            throw new IOException("Failed to get appointments: " + response.errorBody().string());
        }

        List<Appointment> appointments = new ArrayList<>();

        for (Object obj : response.body()) {
            Map<String, Object> map = (Map<String, Object>) obj;
            double altegioIdDouble = (Double) map.get("altegio_id"); // Получаем значение как Double
            int id = (int) altegioIdDouble; // Преобразуем Double в int

            appointments.add(new Appointment(
                    id, // Используем преобразованное значение
                    null,
                    null,
                    (String) map.get("status"),
                    (String) map.get("phone"),
                    (String) map.get("name"),
                    (String) map.get("created_at")));

        }

        return appointments;
    }

    public void updateAppointmentSync(int altegioId, String status) throws IOException {
        // Формируем тело запроса
        Map<String, String> data = new HashMap<>();
        data.put("status", status); // Новый статус

        // Выполняем запрос на обновление
        Response<Void> response = api.updateAppointment(
                "eq." + altegioId, // Условие для поиска записи
                data, // Данные для обновления
                SUPABASE_KEY // API-ключ
        ).execute();

        // Логируем результат
        if (!response.isSuccessful()) {
            throw new IOException("Failed to update appointment: " + response.errorBody().string());
        }
    }
}