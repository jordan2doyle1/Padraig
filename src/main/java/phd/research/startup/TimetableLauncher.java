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

public class TimetableLauncher extends AppLauncher {

    private static final Logger LOGGER = LoggerFactory.getLogger(TimetableLauncher.class);

    public TimetableLauncher(String bundleId, String launchActivity, AppiumManager appiumManager) {
        super(bundleId, launchActivity, appiumManager);
    }

    @Override
    protected void setup() {
        AndroidDriver<WebElement> driver = this.appiumManager.getDriver();

        try {
            AppiumManager.waitForUi(2f);
            LOGGER.info("Pressing Grant Button.");
            AndroidElement grantButton =
                    (AndroidElement) driver.findElementById(this.getBundleId() + ":id/md_buttonDefaultPositive");
            grantButton.click();

            AppiumManager.waitForUi(2f);
            String automatorCommand = "new UiSelector().textContains(\"Timetable\")";
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
