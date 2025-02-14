package hehe.miras.kaspibusinesstest.api;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
import hehe.miras.kaspibusinesstest.model.AltegioAuthResponse;
import hehe.miras.kaspibusinesstest.model.AltegioResponse;

public interface AltegioApi {

    // Авторизация для получения user_token
    @POST("api/v1/auth")
    Call<AltegioAuthResponse> authenticate(
            @Header("Authorization") String partnerToken,
            @Header("Accept") String accept,
            @Body Map<String, String> credentials
    );

    // Получение записей с учетом нового заголовка
    @GET("api/v1/records/{company_id}")
    Call<AltegioResponse> getAppointments(
            @Header("Authorization") String authHeader,
            @Header("Accept") String accept,
            @Path("company_id") int companyId
    );
}