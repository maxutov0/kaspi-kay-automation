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
import hehe.miras.kaspibusinesstest.database.AppointmentRepository;

import android.content.Context;

import hehe.miras.kaspibusinesstest.service.WappiService;
import hehe.miras.kaspibusinesstest.service.SupabaseService;

import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class KaspiBusinessTest {

    private UiDevice device;
    private WappiService wappiService;
    private SupabaseService supabaseService;
    private static final String APP_PACKAGE = "hr.asseco.android.kaspibusiness";
    private static final String MAIN_ACTIVITY = "kz.kaspibusiness.view.ui.auth.splash.SplashActivity";
    private static final int LAUNCH_TIMEOUT = 30000;
    private static final String TAG = "KaspiBusinessTest";
    private static final String MESSAGE_ID_TAG = "MessageIdExtractor";
    private static final int TRANSACTION_AMOUNT = 1; // Сумма транзакции по умолчанию
    private List<Appointment> appointments; // Список записей из Altegio
    private List<String> allowedPhones = new ArrayList<>(); // Массив разрешенных номеров телефонов
    private List<String> extractedMessageIds = new ArrayList<>(); // List to store extracted message IDs

    @Before
    public void setUp() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());

        // Инициализация сервисов
        supabaseService = new SupabaseService();
        wappiService = new WappiService();

        // Добавляем разрешенные номера телефонов
        allowedPhones.add("77753251368");
        allowedPhones.add("77477898496");
        allowedPhones.add("77471022106");
        allowedPhones.add("77058805927");

        // Получаем записи из Altegio перед запуском UI-тестов
        fetchAppointments();

        // Запуск приложения Kaspi Business
        String command = "am start -n " + APP_PACKAGE + "/" + MAIN_ACTIVITY;
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
        // Проверяем, что приложение запустилось
        assertNotNull("Приложение не запустилось", device.findObject(By.pkg(APP_PACKAGE)));

        // Проверяем, есть ли записи из Altegio
        if (appointments == null || appointments.isEmpty()) {
            Log.e("KaspiBusinessTest", "Нет записей из Altegio, тест не выполняется.");
            return; // Прерываем выполнение теста, если записей нет
        }

        // Фильтруем записи по разрешенным номерам телефонов и дате
        List<Appointment> filteredAppointments = filterByDate(
                filterAppointmentsByAllowedPhones(appointments),
                InstrumentationRegistry.getInstrumentation().getTargetContext());

        // Проверяем, есть ли отфильтрованные записи
        if (filteredAppointments.isEmpty()) {
            Log.e("KaspiBusinessTest", "Нет записей с разрешенными номерами телефонов, тест не выполняется.");
            return; // Прерываем выполнение теста, если отфильтрованных записей нет
        }

        Log.d("KaspiBusinessTest", "Filtered appointments count: " + filteredAppointments.size());

        // Обрабатываем каждую запись
        for (Appointment appointment : filteredAppointments) {
            Log.d("KaspiBusinessTest", "Processing appointment with phone: " + appointment.getClient().getPhone());

            // Ждем загрузки приложения
            device.wait(Until.hasObject(By.res(APP_PACKAGE, "remotePaymentFragment")), LAUNCH_TIMEOUT);

            // Проверяем, был ли уже выставлен счет по данной записи
            supabaseService.isAppointmentProcessed(appointment.getId(),
                    new SupabaseService.SupabaseCallback<Boolean>() {
                        @Override
                        public void onResult(Boolean isProcessed) {
                            if (isProcessed) {
                                // Если счет уже был отправлен, проверяем, прошло ли 10 часов
                                long lastSentTimestamp = supabaseService.getAppointmentTimestamp(appointment.getId());
                                long currentTime = System.currentTimeMillis();
                                long tenHoursInMillis = TimeUnit.HOURS.toMillis(10);

                                if (currentTime - lastSentTimestamp >= tenHoursInMillis) {
                                    // Отправляем напоминание через Wappi
                                    String phone = appointment.getClient().getPhone();
                                    String message = "Напоминаем, что у вас есть неоплаченный счет. Пожалуйста, проверьте ваш Kaspi Bank.";
                                    wappiService.sendMessage(phone, message);
                                    Log.d("KaspiBusinessTest", "Напоминание отправлено на номер: " + phone);
                                }
                            } else {
                                // Если счет не был отправлен, отправляем его
                                try {
                                    pressTabButton();
                                    enterPrice(TRANSACTION_AMOUNT); // Используем константу для цены
                                    enterPhoneNumber(appointment.getClient().getPhone());
                                    clickSendButton();
                                    clickCloseButton();

                                    // Сохраняем информацию о транзакции в Supabase
                                    supabaseService.addProcessedAppointment(appointment.getId(),
                                            System.currentTimeMillis());
                                    Log.d("KaspiBusinessTest",
                                            "Счет отправлен на номер: " + appointment.getClient().getPhone());
                                } catch (Exception e) {
                                    Log.e("KaspiBusinessTest", "Ошибка при обработке транзакции: " + e.getMessage());
                                    fail("Тест завершился с ошибкой: " + e.getMessage());
                                }
                            }
                        }
                    });
        }

        // Собираем информацию из истории транзакций и записываем в базу данных
        navigateToHistoryTab();
        List<String> extractedMessageIds = extractMessageIds();
        for (String messageId : extractedMessageIds) {
            // Сохраняем messageId в Supabase
            supabaseService.addMessageId(messageId);
            Log.d("KaspiBusinessTest", "Message ID сохранен в базе данных: " + messageId);
        }
    }

    private List<Appointment> filterByDate(List<Appointment> appointments, Context context) {
        SupabaseService supabaseService = new SupabaseService();
        List<Appointment> filteredAppointments = new ArrayList<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

        // Устанавливаем таймзону UTC+5
        TimeZone timeZone = TimeZone.getTimeZone("Asia/Oral");
        dateFormat.setTimeZone(timeZone);

        long now = System.currentTimeMillis() + timeZone.getRawOffset();
        long twentyFourHoursInMillis = TimeUnit.HOURS.toMillis(24);
        long maxTime = now + twentyFourHoursInMillis;

        
        for (Appointment appointment : appointments) {            
            // Проверяем, была ли запись уже обработана
            supabaseService.isAppointmentProcessed(appointment.getId(),
                    new SupabaseService.SupabaseCallback<Boolean>() {
                        @Override
                        public void onResult(Boolean isProcessed) {
                            Log.d("KaspiBusinessTest", "❌❌❌❌❌❌");

                            if (isProcessed) {
                                Log.d("KaspiBusinessTest", "❌ Пропущено (уже обработано): ID=" + appointment.getId());
                                return;
                            }


                            try {
                                Date appointmentDate = dateFormat.parse(appointment.getDate());
                                if (appointmentDate != null) {
                                    long appointmentTime = appointmentDate.getTime();

                                    if (appointmentTime >= now && appointmentTime <= maxTime) {
                                        Log.d("KaspiBusinessTest", "✅ Запись добавлена: ID=" + appointment.getId());
                                        filteredAppointments.add(appointment);

                                        // Добавляем запись в Supabase
                                        supabaseService.addProcessedAppointment(appointment.getId(),
                                                System.currentTimeMillis());
                                    } else {
                                        Log.d("KaspiBusinessTest",
                                                "❌ Пропущено (не в пределах 24 часов): ID=" + appointment.getId());
                                    }
                                } else {
                                    Log.d("KaspiBusinessTest", "❌ Ошибка парсинга даты: ID=" + appointment.getId());
                                }
                            } catch (ParseException e) {
                                Log.e("KaspiBusinessTest", "❌ Ошибка парсинга даты: ID=" + appointment.getId(), e);
                            }
                        }
                    });
        }

        Log.d("KaspiBusinessTest",
                "📊 Итоговый список записей после фильтрации: " + filteredAppointments.size() + " записей.");
        return filteredAppointments;
    }

    /**
     * Navigate to the History tab if not already there
     */
    private void navigateToHistoryTab() {
        Log.d(MESSAGE_ID_TAG, "Navigating to History tab");

        // Check if we're already on the History tab
        UiObject2 historyTab = device.findObject(
                By.res(APP_PACKAGE, "historyFragment"));

        if (historyTab != null) {
            if (!isSelected(historyTab)) {
                Log.d(MESSAGE_ID_TAG, "Clicking History tab");
                historyTab.click();
                sleep(2000); // Wait for UI to update
            } else {
                Log.d(MESSAGE_ID_TAG, "Already on History tab");
            }
        } else {
            Log.e(MESSAGE_ID_TAG, "Could not find History tab");
        }
    }

    /**
     * Extract message IDs from the history list
     * 
     * @return List of message IDs
     */
    private List<String> extractMessageIds() {
        List<String> messageIds = new ArrayList<>();

        Log.d(MESSAGE_ID_TAG, "Extracting message IDs");

        // Find the recyclerview containing history items
        UiObject2 historyList = device.findObject(By.res(APP_PACKAGE, "remoteRV"));

        if (historyList != null) {
            Log.d(MESSAGE_ID_TAG, "Found history list");

            // Find all elements with comment resource ID
            List<UiObject2> commentElements = device.findObjects(By.res(APP_PACKAGE, "comment"));
            Log.d(MESSAGE_ID_TAG, "Found " + commentElements.size() + " comment elements");

            // Extract text from each comment element
            for (UiObject2 comment : commentElements) {
                String messageId = comment.getText();
                messageIds.add(messageId);
                Log.d(MESSAGE_ID_TAG, "Found message ID: " + messageId);
            }
        } else {
            Log.e(MESSAGE_ID_TAG, "Could not find history list");
        }

        return messageIds;
    }

    /**
     * Helper method to check if a UI element is selected
     */
    private boolean isSelected(UiObject2 element) {
        Boolean selected = element.isSelected();
        return selected != null && selected;
    }

    private List<Appointment> filterAppointmentsByAllowedPhones(List<Appointment> appointments) {
        List<Appointment> filteredAppointments = new ArrayList<>();
        for (Appointment appointment : appointments) {
            String phone = appointment.getClient().getPhone();
            // Log.d("KaspiBusinessTest", "Checking phone: " + phone); // Log the phone being checked

            if (allowedPhones.contains(phone)) {
                Log.d("KaspiBusinessTest", "Phone allowed: " + phone); // Log the phone if it's allowed
                filteredAppointments.add(appointment);
            } else {
                // Log.d("KaspiBusinessTest", "Phone not allowed: " + phone); // Log the phone if it's not allowed
            }
        }
        return filteredAppointments;
    }

    private void pressTabButton() {
        try {
            // Ищем элемент по resource-id
            UiObject tabButton = device.findObject(
                    new UiSelector().resourceId("hr.asseco.android.kaspibusiness:id/remotePaymentFragment"));

            // Если элемент найден, кликаем
            if (tabButton.exists() && tabButton.isEnabled()) {
                tabButton.click();
                sleep(2000);
            } else {
                throw new AssertionError("Не удалось найти или активировать кнопку 'Удалённо'");
            }
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при нажатии на кнопку 'Удалённо': " + e.getMessage());
        }
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

        // Сохраняем время отправки счета
        long currentTime = System.currentTimeMillis();
        int appointmentId = getCurrentAppointmentId(); // Предположим, что у вас есть метод для получения ID текущего
                                                       // appointment
        AppointmentRepository repository = new AppointmentRepository(
                InstrumentationRegistry.getInstrumentation().getTargetContext());
        repository.addProcessedAppointment(appointmentId, currentTime);

        // Отправка сообщения через Wappi через 10 часов
        scheduleWappiMessage(appointmentId, device.findObject(By.res(APP_PACKAGE, "phoneNumberEt")).getText());
    }

    private void scheduleWappiMessage(int appointmentId, String phone) {
        new Thread(() -> {
            try {
                // Ждем 10 часов
                Thread.sleep(TimeUnit.HOURS.toMillis(10));

                // Проверяем, прошло ли 10 часов с момента отправки счета
                AppointmentRepository repository = new AppointmentRepository(
                        InstrumentationRegistry.getInstrumentation().getTargetContext());
                long timestamp = repository.getAppointmentTimestamp(appointmentId);

                if (timestamp != -1 && System.currentTimeMillis() - timestamp >= TimeUnit.HOURS.toMillis(10)) {
                    // Отправляем сообщение через Wappi
                    WappiService wappiService = new WappiService();
                    String message = "Напоминаем, что у вас есть неоплаченный счет. Пожалуйста, проверьте ваш Kaspi Bank.";
                    wappiService.sendMessage(phone, message);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private int getCurrentAppointmentId() {
        // Реализуйте логику для получения ID текущего appointment
        // Например, если у вас есть список appointments, вы можете использовать его
        return appointments.get(0).getId(); // Пример, замените на реальную логику
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

    private void fetchAppointments() {
        CountDownLatch latch = new CountDownLatch(1);

        AltegioService altegioService = new AltegioService();
        altegioService.authenticateAndFetchAppointments(new Callback<List<Appointment>>() {
            @Override
            public void onResponse(Call<List<Appointment>> call, Response<List<Appointment>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    appointments = response.body();

                    Log.d("KaspiBusinessTest", "Fetched " + appointments.size() + " records from Altegio");

                    // Log details of each fetched record
                    // for (Appointment appointment : appointments) {
                    //     Log.d("KaspiBusinessTest", "Fetched Record - ID: " + appointment.getId() +
                    //             ", Client Name: " + appointment.getClient().getName() +
                    //             ", Phone: " + appointment.getClient().getPhone() +
                    //             ", Datetime: " + appointment.getDate());
                    // }
                } else {
                    Log.e("KaspiBusinessTest", "Ошибка запроса: " + response.code());
                }
                latch.countDown(); // Освобождаем поток после завершения запроса
            }

            @Override
            public void onFailure(Call<List<Appointment>> call, Throwable t) {
                Log.e("KaspiBusinessTest", "Ошибка подключения: " + t.getMessage());
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
