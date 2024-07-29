package phd.research.startup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import phd.research.appium.AppiumManager;

/**
 * @author Jordan Doyle
 */

public abstract class AppLauncher {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppLauncher.class);
    protected final AppiumManager appiumManager;
    private final String bundleId;
    private final String launchActivity;

    public AppLauncher(String bundleId, String launchActivity, AppiumManager appiumManager) {
        this.bundleId = bundleId;
        this.launchActivity = launchActivity;
        this.appiumManager = appiumManager;
    }

    public String getBundleId() {
        return this.bundleId;
    }

    public String getLaunchActivity() {
        return launchActivity;
    }

    protected abstract void setup();

    public void launchApp() {
        int attempt = 0;

        do {
            switch (attempt) {
                case 0:
                    try {
                        this.appiumManager.launchApp(this.bundleId);
                    } catch (Exception e) {
                        LOGGER.error("Failed to launch the app using Appium and bundle ID '{}'", this.bundleId);
                    }
                    break;
                case 1:
                    LOGGER.info("Pressing back to return to the app.");
                    this.appiumManager.navigateBack();
                    break;
                case 2:
                    LOGGER.info("Launching the app using Monkey.");
                    this.appiumManager.activateAppUsingMonkey(this.bundleId);
                    break;
                case 3:
                    LOGGER.info("Launching the app using ADB start command.");
                    this.appiumManager.activateApp(this.bundleId, this.launchActivity);
                    break;
                default:
                    throw new RuntimeException("Failed to activate the app with bundle ID '" + this.bundleId + "'");
            }

            AppiumManager.waitForUi(0.5f);
            attempt = attempt + 1;
        } while (!this.appiumManager.getCurrentPackage().startsWith(this.getBundleId()));

        setup();
    }

}
