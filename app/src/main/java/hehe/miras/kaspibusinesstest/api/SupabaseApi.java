package hehe.miras.kaspibusinesstest.api;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface SupabaseApi {
        @POST("appointments")
        Call<Void> insertAppointment(@Body Map<String, Object> data, @Query("apikey") String apiKey);

        @GET("appointments")
        Call<List<Object>> getAppointment(
                        @Query("altegio_id") String filter,
                        @Query("apikey") String apiKey
        );
}