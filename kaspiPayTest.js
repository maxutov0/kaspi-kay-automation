const { remote } = require("webdriverio");

const caps = {
  "platformName": "Android",
  "appium:deviceName": "emulator-5554",
  "appium:automationName": "UiAutomator2",
  "appium:appPackage": "hr.asseco.android.kaspibusiness",
  "appium:appActivity": "com.google.android.apps.youtube.app.watchwhile.WatchWhileActivity",
  "appium:noReset": true
};

(async () => {
  const driver = await remote({
    logLevel: "info",
    path: "/",
    port: 4723,
    capabilities: caps
  });

  try {
    console.log("Запускаем KaspiPay...");
    await driver.pause(5000);

  } catch (error) {
    console.error("Ошибка в тесте:", error);
  } finally {
    await driver.deleteSession();
  }
})();