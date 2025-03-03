package hehe.miras.kaspibusinesstest.service;

import android.util.Log;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import hehe.miras.kaspibusinesstest.api.WappiApi;

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

    public void sendMessageSync(String phone, String message) throws IOException {
        // Очищаем номер телефона от всех символов, кроме цифр
        String cleanedPhone = phone.replaceAll("[^0-9]", "");

        // Проверяем, что очищенный номер телефона не пустой
        if (cleanedPhone.isEmpty()) {
            Log.e("KaspiBusinessTest", "Номер телефона после очистки пустой: " + phone);
            throw new IllegalArgumentException("Номер телефона после очистки пустой");
        }

        // Тело запроса содержит только recipient и body
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("recipient", cleanedPhone); // Используем очищенный номер телефона
        requestBody.put("body", message);

        // Выполняем синхронный запрос
        Call<Void> call = api.sendMessage(AUTH_TOKEN, TEST_PROFILE_ID, requestBody);
        Response<Void> response = call.execute();

        // Логируем результат
        if (!response.isSuccessful()) {
            Log.e("KaspiBusinessTest", "Ошибка при отправке сообщения: " + response.errorBody().string());
            throw new IOException("Ошибка при отправке сообщения: " + response.errorBody().string());
        }
    }
}