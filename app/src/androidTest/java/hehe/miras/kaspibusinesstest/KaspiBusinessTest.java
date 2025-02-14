package hehe.miras.kaspibusinesstest;

import android.util.Log;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.Until;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static org.junit.Assert.assertNotNull;

import hehe.miras.kaspibusinesstest.api.AltegioApi;
import hehe.miras.kaspibusinesstest.model.Appointment;
import hehe.miras.kaspibusinesstest.service.AltegioService;

@RunWith(AndroidJUnit4.class)
public class KaspiBusinessTest {

    private UiDevice device;
    private static final String APP_PACKAGE = "hr.asseco.android.kaspibusiness";
    private static final int LAUNCH_TIMEOUT = 5000;

    private List<Appointment> appointments; // Список записей из Altegio

    @Before
    public void setUp() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());

        // Получаем записи из Altegio перед запуском UI-тестов
        fetchAppointments();

        // Запуск приложения Kaspi Business
        String command = "am start -n hr.asseco.android.kaspibusiness/kz.kaspibusiness.view.ui.auth.splash.SplashActivity";
        try {
            device.executeShellCommand(command);
        } catch (Exception e) {
            throw new RuntimeException("Не удалось запустить приложение: " + command, e);
        }

        // Ждем загрузки приложения
        device.wait(Until.hasObject(By.pkg(APP_PACKAGE).depth(0)), LAUNCH_TIMEOUT);
    }

    @Test
    public void testTransactionFlow() {
        assertNotNull("Приложение не запустилось", device.findObject(By.pkg(APP_PACKAGE)));

        if (appointments != null && !appointments.isEmpty()) {
            Appointment firstAppointment = appointments.get(0); // Берем первую запись из CRM

            pressTabButton();
            enterPrice(5000); // Можно заменить на цену из CRM
            enterPhoneNumber(firstAppointment.getPhone()); // Используем номер клиента из Altegio
            clickSendButton();
            clickCloseButton();
        } else {
            Log.e("KaspiBusinessTest", "Нет записей из Altegio, тест не выполняется.");
        }
    }

    // --- Методы для взаимодействия с UI Kaspi Business ---
    private void pressTabButton() {
        device.findObject(By.desc("Удалённо")).click();
        sleep(2000);
    }

    private void enterPrice(int price) {
        device.findObject(By.res(APP_PACKAGE, "amountPhoneEt")).setText(String.valueOf(price));
        sleep(2000);
    }

    private void enterPhoneNumber(String phone) {
        device.findObject(By.res(APP_PACKAGE, "phoneNumberEt")).setText(phone);
        sleep(2000);
    }

    private void clickSendButton() {
        device.findObject(By.res(APP_PACKAGE, "sendTransferBtn")).click();
        sleep(3500);
    }

    private void clickCloseButton() {
        device.findObject(By.res(APP_PACKAGE, "closeBtn")).click();
        sleep(2000);
    }

    private void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // --- Подключение к API Altegio ---
    private void fetchAppointments() {
        CountDownLatch latch = new CountDownLatch(1);

        AltegioService altegioService = new AltegioService();
        altegioService.getAppointments(new Callback<List<Appointment>>() {
            @Override
            public void onResponse(Call<List<Appointment>> call, Response<List<Appointment>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    appointments = response.body();
                    Log.d("Altegio", "Получено записей: " + appointments.size());
                } else {
                    Log.e("Altegio", "Ошибка запроса: " + response.code());
                }
                latch.countDown(); // Освобождаем поток после завершения запроса
            }

            @Override
            public void onFailure(Call<List<Appointment>> call, Throwable t) {
                Log.e("Altegio", "Ошибка подключения: " + t.getMessage());
                latch.countDown(); // Освобождаем поток даже при ошибке
            }
        });

        try {
            latch.await(); // Ожидание завершения запроса перед выполнением теста
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}