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

import java.io.IOException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.TimeZone;
import java.util.HashSet;

import android.content.Context;

import hehe.miras.kaspibusinesstest.service.WappiService;
import hehe.miras.kaspibusinesstest.service.SupabaseService;
import hehe.miras.kaspibusinesstest.service.AltegioService;

import static org.junit.Assert.fail;

import androidx.test.uiautomator.BySelector;

import androidx.test.uiautomator.UiScrollable;
import androidx.test.uiautomator.Direction;

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

    private List<String> allowedPhones = new ArrayList<>(
            Arrays.asList("77753251368", "77477898496", "77471022106", "77058805927"));

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
        syncSentInvoices();

        // Отправляем счета или напоминания
        sendInvoices();

        // Отправляем напоминания
        sendReminders();

        Log.d(TAG, "Тест завершен");
    }

    public void syncSentInvoices() {
        Log.d(TAG, "Сбор истории");

        // Ожидание появления фрагмента истории
        device.wait(Until.hasObject(By.res(APP_PACKAGE, "historyFragment")), LAUNCH_TIMEOUT);
        sleep(2000);

        // Переход на экран истории
        device.findObject(By.res(APP_PACKAGE, "historyFragment")).click();
        sleep(1000);

        // Ожидание появления списка счетов
        // androidx.recyclerview.widget.RecyclerView[@resource-id="hr.asseco.android.kaspibusiness:id/operationsRv"]//androidx.recyclerview.widget.RecyclerView[@resource-id="hr.asseco.android.kaspibusiness:id/operationsRv"]
        device.wait(Until.hasObject(By.res(APP_PACKAGE, "operationsRv")), LAUNCH_TIMEOUT);
        sleep(1000);

        // Находим список счетов как UiScrollable
        UiObject2 invoiceList = device.findObject(By.res(APP_PACKAGE, "operationsRv"));

        if(invoiceList == null) {
            Log.d(TAG, "Список счетов не найден");
            return;
        }

        int lastFoundCount = 0; // Хранит количество найденных элементов на предыдущей итерации
        int unchangedCount = 0; // Счетчик для отслеживания, сколько раз количество не менялось

        Set<String> invoicesIds = new HashSet<String>();

        while (true) {
            // Находим все элементы
            List<UiObject2> items = device.findObjects(By.res(APP_PACKAGE, "sellerComment"));

            Log.d(TAG, "Нашлось " + items.size() + " элементов");

            // Если количество элементов не изменилось
            if (items.size() == lastFoundCount) {
                unchangedCount++; // Увеличиваем счетчик
            } else {
                unchangedCount = 0; // Сбрасываем счетчик, если количество изменилось
            }

            // Если количество не менялось 3 раза подряд, выходим из цикла
            if (unchangedCount >= 3) {
                // Log.d(TAG, "Количество счетов не менялось 3 раза подряд. Завершение.");
                break;
            }

            // Обновляем lastFoundCount
            lastFoundCount = items.size();

            // Обрабатываем найденные элементы
            for (UiObject2 item : items) {
                invoicesIds.add(item.getText());
            }

            // Прокручиваем список вниз
            invoiceList.scroll(Direction.DOWN, 10);
            sleep(100); // Пауза для стабилизации списка
        }

        Log.d(TAG, "Найдено " + invoicesIds.size() + " счетов");

        // Список для хранения данных о счетах
        List<Appointment> appointments = new ArrayList<>();

        for(String invoideId: invoicesIds) {
            appointments.add(new Appointment(
                Integer.parseInt(invoideId),
                null,
                null,
                "paid",
                null,
                null,
                null
            ));
        }

        // Обновляем записи Supabase
        for(Appointment appointment: appointments) {
            try {
                supabaseService.updateAppointmentSync(appointment.getId(), appointment.getStatus());
                Log.d(TAG, "Запись в Supabase обновлен ID: " + appointment.getId()); 
            } catch (Exception e) {
                Log.e(TAG, "Ошибка при получении записи Supabase", e);
            }
        }

        Log.d(TAG, "Завершено обновления базы данных Supabase");
    }

    public void sendInvoices() {
        Log.d(TAG, "Отправка счетов");

        Log.d(TAG, "Получение записей из Altegio");
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

        Log.d(TAG, "Получено записей из Altegio: " + appointments.size());

        // Фильтруем записи по разрешенным номерам телефонов
        Log.d(TAG, "Фильтрация записей по разрешенным номерам телефонов");
        Log.d(TAG, "Разрешенные номера телефонов: " + allowedPhones);
        List<Appointment> filteredAppointments = new ArrayList<>();

        for (Appointment appointment : appointments) {
            String phone = appointment.getClient().getPhone();

            if (!allowedPhones.contains(phone)) {
                continue;
            }

            Log.d(TAG, "Телефон клиента " + phone + " разрешен");
            filteredAppointments.add(appointment);
        }

        // Фильтруем записи по дате
        Log.d(TAG, "Фильтрация записей по дате");

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

        TimeZone timeZone = TimeZone.getTimeZone("Asia/Oral");
        dateFormat.setTimeZone(timeZone);

        long now = System.currentTimeMillis() - timeZone.getRawOffset();
        long twentyFourHoursInMillis = TimeUnit.HOURS.toMillis(24);
        long maxTime = now + twentyFourHoursInMillis;

        for (Appointment appointment : filteredAppointments) {
            try {
                Date appointmentDate = dateFormat.parse(appointment.getDate());
                if (appointmentDate != null) {
                    long appointmentTime = appointmentDate.getTime();

                    // Проверяем, что запись находится в пределах 24 часов от текущего времени
                    if (appointmentTime >= now && appointmentTime <= maxTime) {
                        Log.d(TAG, "Запись " + appointment.getId() + " в пределах 24 часов");
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
                String altegioIdParam = "eq." + appointment.getId(); // Формируем параметр для altegio_id

                List<Appointment> supabaseAppointments = supabaseService.getAppointmentsSync(altegioIdParam, null, null,
                        null, null);

                if (supabaseAppointments.isEmpty()) {
                    Log.d(TAG, "Запись в Supabase не найдена");
                    continue;
                }

                Log.d(TAG, "Счет " + appointment.getId() + " уже отправлен");
                filteredAppointments.remove(appointment);
            } catch (Exception e) {
                Log.e(TAG, "Ошибка при получении записи из Supabase для записи " + appointment.getId(), e);
                filteredAppointments.remove(appointment);
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

    public void sendReminders() {

        // Получаем записи из Supabase
        Log.d(TAG, "Получение записей из Supabase");

        List<Appointment> appointmentsWithInvoices = new ArrayList<>();

        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
            dateFormat.setTimeZone(TimeZone.getTimeZone("UTC")); // Устанавливаем часовой пояс UTC
        
            long now = System.currentTimeMillis();
            long tenHoursInMillis = TimeUnit.HOURS.toMillis(10);
            long oneHourInMillis = TimeUnit.HOURS.toMillis(1);
        
            // Формируем параметры в формате ISO 8601 с указанием временной зоны
            String createdAtParam = "lte." + dateFormat.format(new Date(now - oneHourInMillis));
            String statusParam = "eq.invoice_sent"; // Формируем параметр для status
            String orderByParam = "id.desc"; // Формируем параметр для order
            String dateParam = "lte." + dateFormat.format(new Date(now + tenHoursInMillis));
        
            appointmentsWithInvoices = supabaseService.getAppointmentsSync(null, createdAtParam, statusParam,
                    orderByParam, dateParam);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при получении записей из Supabase", e);
        }

        Log.d(TAG, "Получено записей из Supabase: " + appointmentsWithInvoices.size());

        // Проверяем, есть ли записи для отправки напоминаний
        if (appointmentsWithInvoices == null || appointmentsWithInvoices.isEmpty()) {
            Log.d(TAG, "Нет записей для отправки напоминаний");
            return;
        }

        // Отправляем напоминания
        Log.d(TAG, "Отправка напоминаний");
        for (Appointment appointment : appointmentsWithInvoices) {
            try {
                sendReminder(appointment);
            } catch (Throwable e) {
                Log.e(TAG, "Ошибка при отправке напоминания для записи " +
                        appointment.getId(), e);
            }
        }
    }

    public void sendInvoice(Appointment appointment) {
        Log.d(TAG, "Отправка счета для записи " + appointment.getId());

        // Ждем загрузки приложения
        device.wait(Until.hasObject(By.res(APP_PACKAGE, "remotePaymentFragment")), LAUNCH_TIMEOUT);

        device.findObject(By.res(APP_PACKAGE, "remotePaymentFragment")).click();
        sleep(1000);

        device.findObject(By.res(APP_PACKAGE, "amountPhoneEt")).setText(String.valueOf(TRANSACTION_AMOUNT));
        sleep(1000);

        device.findObject(By.res(APP_PACKAGE, "phoneNumberEt")).setText(appointment.getClient().getPhone());
        sleep(1000);

        device.findObject(By.res(APP_PACKAGE, "editText")).setText("" + appointment.getId());
        sleep(1000);

        device.findObject(By.res(APP_PACKAGE, "sendTransferBtn")).click();
        sleep(3500);

        device.findObject(By.res(APP_PACKAGE, "closeBtn")).click();
        sleep(1000);

        // Сохраняем запись в Supabase
        try {
            supabaseService.addAppointmentSync(appointment);
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при добавлении Supabase записи " + appointment.getId(), e);
        }

        Log.d(TAG, "Счет отправлен для записи " + appointment.getId());
    }

    public void sendReminder(Appointment appointment) {
        Log.d(TAG, "Отправка напоминания для записи " + appointment.getId());

        // Формируем текст напоминания
        String message = "Уважаемый, " + appointment.getName() +
                "\n\n\rС момента выставления предоплаты прошло 10 часов. Просим вас оплатить услугу в ближайшее время."
                +
                "\n\n\rВ случае отсутствия оплаты ваша бронь будет аннулирована." +
                "\n\n\rС уважением, \n\rКлиника “Darmed”";

        // Отправляем напоминание через Wappi
        try {
            wappiService.sendMessageSync(appointment.getPhone(), message);
            Log.d(TAG, "Напоминание отправлено для записи " + appointment.getId());
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при отправке напоминания через Wappi для записи " + appointment.getId(), e);
            return;
        }

        // Обновляем статус записи в Supabase
        try {
            supabaseService.updateAppointmentSync(appointment.getId(), "reminder_sent");
            Log.d(TAG, "Статус записи обновлен в Supabase для записи " + appointment.getId());
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при обновлении статуса записи в Supabase " + appointment.getId(), e);
        }
    }

    private void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (Throwable e) {
            Log.e(TAG, "Ошибка при паузе", e);
        }
    }
}
