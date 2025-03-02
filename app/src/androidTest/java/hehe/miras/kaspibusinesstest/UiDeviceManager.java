package hehe.miras.kaspibusinesstest;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.UiDevice;

public class UiDeviceManager {

    private static UiDevice instance;
    private static boolean initialized = false;

    /**
     * Gets the shared UiDevice instance
     */
    public static UiDevice getInstance() {
        if (instance == null) {
            instance = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
            initialized = true;
        }
        return instance;
    }

    /**
     * Launches the specified application
     */
    public static void launchApp(String packageName, String activityName) {
        UiDevice device = getInstance();
        String command = "am start -n " + packageName + "/" + activityName;
        try {
            device.executeShellCommand(command);
            initialized = true;
        } catch (Exception e) {
            throw new RuntimeException("Failed to launch app: " + packageName, e);
        }
    }

    /**
     * Checks if the UiDevice is initialized
     */
    public static boolean isInitialized() {
        return initialized;
    }
}
