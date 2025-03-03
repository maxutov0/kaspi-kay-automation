package hehe.miras.kaspibusinesstest;

import android.util.Log;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.Until;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.UiObject;
import androidx.test.uiautomator.UiObject2;
import androidx.test.uiautomator.UiSelector;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static org.junit.Assert.assertNotNull;

import hehe.miras.kaspibusinesstest.api.AltegioApi;
import hehe.miras.kaspibusinesstest.model.Appointment;
import hehe.miras.kaspibusinesstest.service.AltegioService;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.TimeZone;

import android.content.Context;

import hehe.miras.kaspibusinesstest.service.WappiService;
import hehe.miras.kaspibusinesstest.service.SupabaseService;
import hehe.miras.kaspibusinesstest.service.AltegioService;

import static org.junit.Assert.fail;

import androidx.test.uiautomator.BySelector;

@RunWith(AndroidJUnit4.class)
public class KaspiBusinessTest {

    private static final String APP_PACKAGE = "hr.asseco.android.kaspibusiness";
    private static final String MAIN_ACTIVITY = "kz.kaspibusiness.view.ui.auth.splash.SplashActivity";
    private static final String TAG = "KaspiBusinessTest";
    private static final int LAUNCH_TIMEOUT = 300;
    private static final int TRANSACTION_AMOUNT = 1;

    private UiDevice device;
    private WappiService wappiService;
    private SupabaseService supabaseService;
    private AltegioService altegioService;

    private List<Appointment> appointments;
    
    private List<String> allowedPhones = new ArrayList<>(Arrays.asList("77753251368", "77477898496", "77471022106", "77058805927"));

    @Before
    public void setUp() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());

        // Инициализация сервисов
        supabaseService = new SupabaseService();
        wappiService = new WappiService();
        altegioService = new AltegioService();

        // Запуск приложения Kaspi Pay
        String command = "am start -n " + APP_PACKAGE + "/" + MAIN_ACTIVITY;
        
        try {
            device.executeShellCommand(command);
        } catch (Throwable e) {
            throw new RuntimeException("Ошибка при запуске приложения", e);
        }

        // Ждем загрузки приложения
        device.wait(Until.hasObject(By.pkg(APP_PACKAGE).depth(0)), LAUNCH_TIMEOUT);
    }

    @Test
    public void mainTest() {
        Log.d(TAG, "Запуск теста");

        // Обновляем статусы выставленных счетов
        // syncSentInvoices();

        // Отправляем счета или напоминания
        sendInvoices();

        // Отправляем напоминания
        // sendReminders();

        Log.d(TAG, "Тест завершен");
    }

    public void syncSentInvoices() {
        device.wait(Until.hasObject(By.res(APP_PACKAGE, "historyFragment")), LAUNCH_TIMEOUT);
        sleep(1000);

        device.findObject(By.res(APP_PACKAGE, "historyFragment")).click();
        sleep(1000);

        // Получаем список всех счетов

    }

    public void sendInvoices() {
        Log.d(TAG, "Отправка счетов");

        try {
            appointments = altegioService.fetchAppointmentsSync();
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при получении записей из Altegio", e);
        }

        // Проверяем, есть ли записи из Altegio
        if (appointments == null || appointments.isEmpty()) {
            Log.d(TAG, "Нет записей из Altegio");
            return;
        }

        // Фильтруем записи по разрешенным номерам телефонов
        Log.d(TAG, "Фильтрация записей по разрешенным номерам телефонов");

        List<Appointment> filteredAppointments = new ArrayList<>();

        for (Appointment appointment : appointments) {
            String phone = appointment.getClient().getPhone();

            if (allowedPhones.contains(phone)) {
                filteredAppointments.add(appointment);
            }
        }

        // Фильтруем записи по дате
        Log.d(TAG, "Фильтрация записей по дате");

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

        TimeZone timeZone = TimeZone.getTimeZone("Asia/Oral");
        dateFormat.setTimeZone(timeZone);

        long now = System.currentTimeMillis();
        long twentyFourHoursInMillis = TimeUnit.HOURS.toMillis(24);
        long maxTime = now + twentyFourHoursInMillis;

        for (Appointment appointment : filteredAppointments) {
            try {
                Date appointmentDate = dateFormat.parse(appointment.getDate());
                if (appointmentDate != null) {
                    long appointmentTime = appointmentDate.getTime();

                    // Проверяем, что запись находится в пределах 24 часов от текущего времени
                    if (appointmentTime >= now && appointmentTime <= maxTime) {
                        continue;
                    } 
                } else {
                }
            } catch (Throwable e) {
                Log.e(TAG, "Ошибка при парсинге даты записи " + appointment.getId(), e);
            }

            filteredAppointments.remove(appointment);
        }

        // Фильтруем записи по статусу из Supabase
        Log.d(TAG, "Фильтрация записей по статусу из Supabase");
        for (Appointment appointment : filteredAppointments) {
            try {
                Object supabaseAppointment = supabaseService.getAppointmentSync(appointment.getId());
                
                if (supabaseAppointment != null) {
                    filteredAppointments.remove(appointment);
                }
            } catch (Exception e) {
                Log.e(TAG, "Ошибка при получении записи из Supabase для записи " + appointment.getId(), e);
                continue;
            }
        }

        // Проверяем, есть ли отфильтрованные записи для отправки счетов
        if (filteredAppointments.isEmpty()) {
            Log.d(TAG, "Нет записей для отправки счетов");
            return;
        }

        // Отправляем счета в первый раз
        for (Appointment appointment : filteredAppointments) {
            try {
                sendInvoice(appointment);
            } catch (Throwable e) {
                Log.e(TAG, "Ошибка при отправке счета для записи " + appointment.getId(), e);
            }
        }
    }

    public void sendReminders()
    {

    }

    public void sendInvoice(Appointment appointment) {
        Log.d(TAG, "Отправка счета для записи " + appointment.getId());

        // // Ждем загрузки приложения
        // device.wait(Until.hasObject(By.res(APP_PACKAGE, "remotePaymentFragment")), LAUNCH_TIMEOUT);

        // device.findObject(By.res(APP_PACKAGE, "remotePaymentFragment")).click();
        // sleep(1000);

        // device.findObject(By.res(APP_PACKAGE, "amountPhoneEt")).setText(String.valueOf(TRANSACTION_AMOUNT));
        // sleep(1000);

        // // device.findObject(By.res(APP_PACKAGE, "phoneNumberEt")).setText(appointment.getClient().getPhone());
        // device.findObject(By.res(APP_PACKAGE, "phoneNumberEt")).setText("77477898496");
        // sleep(1000);

        // device.findObject(By.res(APP_PACKAGE, "editText")).setText("" + appointment.getId());
        // sleep(1000);

        // device.findObject(By.res(APP_PACKAGE, "sendTransferBtn")).click();
        // sleep(3500);

        // device.findObject(By.res(APP_PACKAGE, "closeBtn")).click();
        // sleep(1000);

        // Сохраняем запись в Supabase
        try {
            supabaseService.addAppointmentSync(appointment);
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при добавлении Supabase записи " + appointment.getId(), e);
        }

        Log.d(TAG, "Счет отправлен для записи " + appointment.getId());
    }

    public void sendReminder(Appointment appointment) {

    }

    private void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (Throwable e) {
            Log.e(TAG, "Ошибка при паузе", e);
        }
    }
}
