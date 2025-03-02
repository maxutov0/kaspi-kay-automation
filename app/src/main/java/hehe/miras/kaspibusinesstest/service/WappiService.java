package hehe.miras.kaspibusinesstest.service;

import android.util.Log;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;
import java.util.HashMap;
import java.util.Map;

public class WappiService {

    private static final String BASE_URL = "https://wappi.pro/api/";
    private static final String AUTH_TOKEN = "c44d5e95a19dbe26415a0b5847cea3c4a806a42b";
    private static final String TEST_PROFILE_ID = "fb29cf6d-33ad";

    private WappiApi api;

    public WappiService() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        api = retrofit.create(WappiApi.class);
    }

    public void sendMessage(String phone, String message) {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("profile_id", TEST_PROFILE_ID);
        requestBody.put("recipient", phone);
        requestBody.put("body", message);

        Call<Void> call = api.sendMessage("Bearer " + AUTH_TOKEN, requestBody);
        call.enqueue(new retrofit2.Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, retrofit2.Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d("WappiService", "Сообщение успешно отправлено на номер: " + phone);
                } else {
                    Log.e("WappiService", "Ошибка при отправке сообщения: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("WappiService", "Ошибка подключения: " + t.getMessage());
            }
        });
    }

    private interface WappiApi {
        @POST("sync/message/send")
        Call<Void> sendMessage(@Header("Authorization") String authHeader, @Body Map<String, String> body);
    }
}