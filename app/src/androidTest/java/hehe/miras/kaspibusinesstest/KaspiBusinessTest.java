package hehe.miras.kaspibusinesstest;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.Until;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertNotNull;

@RunWith(AndroidJUnit4.class)
public class KaspiBusinessTest {

    private UiDevice device;
    private static final String APP_PACKAGE = "hr.asseco.android.kaspibusiness";
    private static final int LAUNCH_TIMEOUT = 5000;

    @Before
    public void setUp() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());

        // Launch the app using the correct SplashActivity
        String command = "am start -n hr.asseco.android.kaspibusiness/kz.kaspibusiness.view.ui.auth.splash.SplashActivity";
        try {
            device.executeShellCommand(command);
        } catch (Exception e) {
            throw new RuntimeException("Failed to launch the app with command: " + command, e);
        }

        // Wait for the app to appear
        device.wait(Until.hasObject(By.pkg(APP_PACKAGE).depth(0)), LAUNCH_TIMEOUT);
    }

    @Test
    public void testAppLaunchesSuccessfully() {
        assertNotNull("App did not open successfully", device.findObject(By.pkg(APP_PACKAGE)));
    }

    // --- New Methods for BusinessActivity actions ---

    /**
     * Presses the tab button at the bottom of the screen.
     * Assumes the tab button has a content description "Удалённо".
     */
    private void pressTabButton() {
        device.findObject(By.desc("Удалённо")).click();
        // Allow time for the tab to open.
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Enters the given price into the price input field.
     * Uses the resource ID "amountPhoneEt".
     *
     * @param price the amount to enter
     */
    private void enterPrice(int price) {
        device.findObject(By.res(APP_PACKAGE, "amountPhoneEt"))
                .setText(String.valueOf(price));
        // Allow time for the app to process the price input.
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Enters the provided phone number into the phone number input field.
     * Uses the resource ID "phoneNumberEt".
     *
     * @param phone the phone number to enter
     */
    private void enterPhoneNumber(String phone) {
        device.findObject(By.res(APP_PACKAGE, "phoneNumberEt"))
                .setText(phone);
        // Allow time for the app to process the phone input.
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Clicks the send button.
     * Uses the resource ID "sendTransferBtn".
     */
    private void clickSendButton() {
        device.findObject(By.res(APP_PACKAGE, "sendTransferBtn")).click();
        // Allow time for the send action to complete.
        try {
            Thread.sleep(3500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Clicks the close button on the popup screen.
     * Uses the resource ID "closeBtn".
     */
    private void clickCloseButton() {
        device.findObject(By.res(APP_PACKAGE, "closeBtn")).click();
        // Allow time for the popup to be dismissed.
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Executes the transaction flow:
     * 1. Press the tab button.
     * 2. Enter an amount of money.
     * 3. Enter a phone number.
     * 4. Press the send button.
     * 5. Close the popup by pressing the close button.
     */
    @Test
    public void testTransactionFlow() {
        // Wait for 10 seconds before starting the transaction flow.
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        pressTabButton();
        enterPrice(120000);
        enterPhoneNumber("77471022106");
        clickSendButton();
        clickCloseButton();
    }
}
