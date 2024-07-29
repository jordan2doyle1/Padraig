package phd.research.startup;

import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.AndroidElement;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import phd.research.appium.AppiumManager;

/**
 * @author Jordan Doyle
 */

public class VolumeControlLauncher extends AppLauncher {

    private static final Logger LOGGER = LoggerFactory.getLogger(VolumeControlLauncher.class);

    public VolumeControlLauncher(String bundleId, String launchActivity, AppiumManager appiumManager) {
        super(bundleId, launchActivity, appiumManager);
    }

    public void setup() {
        AndroidDriver<WebElement> driver = this.appiumManager.getDriver();

        try {
            AppiumManager.waitForUi(2f);
            AndroidElement okButton = (AndroidElement) driver.findElementById("android:id/button1");
            LOGGER.info("Pressing OK Button.");
            okButton.click();

            AppiumManager.waitForUi(2f);
            String automatorCommand = "new UiSelector().textContains(\"Volume Control\")";
            AndroidElement permissionButton = (AndroidElement) driver.findElementByAndroidUIAutomator(automatorCommand);
            LOGGER.info("Pressing Permission Button.");
            permissionButton.click();

            AppiumManager.waitForUi(2f);
            AndroidElement allowButton = (AndroidElement) driver.findElementById("android:id/button1");
            LOGGER.info("Pressing Allow Button.");
            allowButton.click();

            AppiumManager.waitForUi(2f);
            LOGGER.info("Pressing Back Button.");
            driver.navigate().back();
        } catch (NoSuchElementException ignored) {

        }

        LOGGER.info("App launch setup complete.");
    }
}
