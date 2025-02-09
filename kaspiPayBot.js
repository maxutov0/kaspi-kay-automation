const { remote } = require("webdriverio");

const caps = {
  platformName: "Android",
  deviceName: "emulator-5554", // Для реального устройства можно указать adb ID
  automationName: "UiAutomator2",
  appPackage: "kz.kaspi.pay",
  appActivity: ".MainActivity",
  noReset: true // Чтобы не разлогинивало пользователя
};

(async () => {
  const driver = await remote({
    logLevel: "info",
    path: "/wd/hub",
    port: 4723,
    capabilities: caps
  });

  try {
    console.log("Ожидание загрузки приложения...");
    await driver.pause(5000);

    // Вводим сумму предоплаты
    const amountInput = await driver.$("android=new UiSelector().resourceId('kz.kaspi.pay:id/amountInput')");
    await amountInput.setValue("5000");
    console.log("Ввели сумму 5000");

    // Нажимаем кнопку "Отправить"
    const sendButton = await driver.$("android=new UiSelector().resourceId('kz.kaspi.pay:id/sendButton')");
    await sendButton.click();
    console.log("Нажали кнопку отправки");

    await driver.pause(3000); // Даем время на отправку

  } catch (error) {
    console.error("Ошибка в боте:", error);
  } finally {
    await driver.deleteSession(); // Завершаем сессию
  }
})();