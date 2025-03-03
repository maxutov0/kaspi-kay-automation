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

    // Получение записи из Supabase
    public List<Object> getAppointmentSync(Appointment appointment) throws IOException {
        String filter = "eq." + appointment.getId();
    
        Response<List<Object>> response = api.getAppointment(filter, SUPABASE_KEY).execute();

        if (response.isSuccessful() && response.body() != null) {
            return response.body();
        }

        return null;
    }
}