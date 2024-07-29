package phd.research.traversal;

import io.appium.java_client.android.AndroidElement;
import org.openqa.selenium.Point;
import org.openqa.selenium.StaleElementReferenceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import phd.research.appium.AppiumManager;
import phd.research.helper.Terminal;
import phd.research.helper.Timer;
import phd.research.singletons.TraversalSettings;
import phd.research.startup.AppLauncher;
import phd.research.startup.GenericLauncher;
import phd.research.startup.TimetableLauncher;
import phd.research.startup.VolumeControlLauncher;
import phd.research.ui.BackElement;
import phd.research.ui.InputTracker;

import java.io.IOException;
import java.util.List;

public abstract class Search {

    private static final Logger LOGGER = LoggerFactory.getLogger(Search.class);

    protected final AppiumManager appiumManager;
    protected final AppLauncher appLauncher;
    protected final InputTracker inputTracker;

    public Search() {
        this.appiumManager = new AppiumManager();
        this.appLauncher = this.getAppLauncher();
        this.inputTracker = new InputTracker();
    }

    protected static String tapCommand(Point location) {
        return "input tap " + location.x + " " + location.y;
    }

    protected static String backCommand() {
        return "input keyevent KEYCODE_BACK";
    }

    protected static String launchCommand(String bundleId, String activity) {
        return "am start -n " + bundleId + "/" + activity;
    }

    private AppLauncher getAppLauncher() {
        String bundleId = this.getBundleId();
        String launchActivity = this.getLaunchActivity();
        AppLauncher launcher;

        switch (bundleId) {
            //noinspection SpellCheckingInspection
            case "com.asdoi.timetable":
                launcher = new TimetableLauncher(bundleId, launchActivity, this.appiumManager);
                break;
            //noinspection SpellCheckingInspection
            case "com.punksta.apps.volumecontrol":
                launcher = new VolumeControlLauncher(bundleId, launchActivity, this.appiumManager);
                break;
            default:
                launcher = new GenericLauncher(bundleId, launchActivity, this.appiumManager);
                break;
        }

        return launcher;
    }

    private String getBundleId() {
        return Terminal.executeCommand("aapt dump badging " + TraversalSettings.v().getApkFile() +
                " | grep package:\\ name | cut -d \"'\" -f 2");
    }

    private String getLaunchActivity() {
        return Terminal.executeCommand("aapt dump badging " + TraversalSettings.v().getApkFile() +
                " | grep launchable-activity:\\ name | cut -d \"'\" -f 2");
    }

    public AppiumManager getAppiumManager() {
        return this.appiumManager;
    }

    public void applyInput(String activity, AndroidElement control) {
        String cmd = control instanceof BackElement ? backCommand() : tapCommand(control.getCenter());
        this.inputTracker.logInput(cmd, AppiumManager.getElementDescription(control));
        control.click();
    }

    protected abstract List<AndroidElement> getAvailableControls();

    protected abstract AndroidElement getNextControl(String activity, List<AndroidElement> controls);

    public void traverseUi() {
        Timer timer = new Timer();
        LOGGER.info("Running traversal search... ({})", timer.start(true));

        this.appiumManager.startAppiumLogcatBroadcast();

        LOGGER.info("BundleId is {}", this.appLauncher.getBundleId());
        LOGGER.info("Launch Activity is {}", this.appLauncher.getLaunchActivity());

        if (TraversalSettings.v().isStartAcv()) {
            try {
                this.appiumManager.startCoverage(this.appLauncher.getBundleId());
                AppiumManager.waitForUi(3f);
            } catch (IOException e) {
                LOGGER.error("Problem starting ACV coverage: {}", e.getMessage());
                return;
            }
        }

        try {
            String launchCmd = launchCommand(this.appLauncher.getBundleId(), this.appLauncher.getLaunchActivity());
            this.inputTracker.logInput(launchCmd, "");
            this.appLauncher.launchApp();
        } catch (RuntimeException e) {
            LOGGER.error(e.getMessage());
            this.appiumManager.successfulCleanup();
            return;
        }

        AppiumManager.waitForUi(0.5f);

        while (this.inputTracker.canContinue()) {
            this.appiumManager.hideKeyboard();
            String currentActivity = this.appiumManager.getCurrentActivity();

            if (!this.appiumManager.getCurrentPackage().startsWith(this.appLauncher.getBundleId())) {
                try {
                    this.inputTracker.logInput(
                            launchCommand(this.appLauncher.getBundleId(), this.appLauncher.getLaunchActivity()), "");
                    this.appLauncher.launchApp();
                } catch (RuntimeException e) {
                    LOGGER.error(e.getMessage());
                    this.appiumManager.successfulCleanup();
                    return;
                }
            } else {
                try {
                    List<AndroidElement> availableControls = this.getAvailableControls();
                    LOGGER.info("{} controls available.", availableControls.size());
                    AndroidElement nextControl = this.getNextControl(currentActivity, availableControls);
                    LOGGER.info("Next control is '{}.", AppiumManager.getElementDescription(nextControl));
                    this.applyInput(currentActivity, nextControl);
                } catch (StaleElementReferenceException ignored) {
                    LOGGER.error("Stale Android element, ignoring and moving on.");
                    continue;
                }
            }

            AppiumManager.waitForUi(0.5f);
        }

        if (this.inputTracker.isIncomplete()) {
            LOGGER.error("Only completed {} interactions.", this.inputTracker.getInputCount());
        }

        this.appiumManager.successfulCleanup();
        LOGGER.info("({} traversal search took {} second(s).", timer.end(), timer.secondsDuration());

        LOGGER.info("Outputting test suite and UI log... ({})", timer.start(true));
        this.inputTracker.writeInputScript();
        this.appiumManager.getUiLogger().writeUiLog();
        LOGGER.info("({} output took {} second(s).", timer.end(), timer.secondsDuration());
    }

}
