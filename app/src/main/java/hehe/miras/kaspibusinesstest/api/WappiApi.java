package hehe.miras.kaspibusinesstest.api;

import java.util.Map;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface WappiApi {
    @POST("sync/message/send")
    Call<Void> sendMessage(
        @Header("Authorization") String authHeader,
        @Query("profile_id") String profileId, // profile_id передается как query-параметр
        @Body Map<String, String> body // Тело запроса содержит только recipient и body
    );
}