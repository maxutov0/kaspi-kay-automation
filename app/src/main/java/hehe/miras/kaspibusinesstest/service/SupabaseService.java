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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

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

        Response<Void> response = api.insertAppointment(data, SUPABASE_KEY).execute();

        if (!response.isSuccessful()) {
            throw new IOException("Failed to insert appointment: " + response.errorBody().string());
        }
    }

    public List<Appointment> getAppointmentsSync(String altegioId, String createdAt, String status, String orderBy)
            throws IOException {
        Response<List<Object>> response = api.getAppointment(altegioId, createdAt, status, orderBy, SUPABASE_KEY)
                .execute();

        if (!response.isSuccessful()) {
            throw new IOException("Failed to get appointments: " + response.errorBody().string());
        }

        List<Appointment> appointments = new ArrayList<>();

        Log.d("KaspiBusinessTest", "Response: " + response.body());

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
                    (String) map.get("created_at")));
        }

        return appointments;
    }
}