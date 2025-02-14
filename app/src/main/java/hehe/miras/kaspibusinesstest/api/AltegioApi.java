package hehe.miras.kaspibusinesstest.api;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;
import hehe.miras.kaspibusinesstest.model.Appointment;

// Интерфейс для API Altegio
public interface AltegioApi {
    @GET("records")
    Call<List<Appointment>> getAppointments(
            @Header("Authorization") String token,
            @Header("Accept") String accept,
            @Query("company_id") int companyId
    );
}