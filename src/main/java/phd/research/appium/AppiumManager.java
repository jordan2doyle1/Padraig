package phd.research.appium;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import io.appium.java_client.MobileBy;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.AndroidElement;
import io.appium.java_client.remote.AndroidMobileCapabilityType;
import io.appium.java_client.remote.MobileCapabilityType;
import io.appium.java_client.service.local.AppiumDriverLocalService;
import io.appium.java_client.service.local.AppiumServerHasNotBeenStartedLocallyException;
import io.appium.java_client.service.local.AppiumServiceBuilder;
import io.appium.java_client.service.local.flags.ServerArgument;
import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import phd.research.singletons.TraversalSettings;
import phd.research.ui.BackElement;
import phd.research.ui.UiLogger;
import phd.research.utilities.LogHandler;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * @author Jordan Doyle
 */

public class AppiumManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppiumManager.class);

    private final Thread appiumHook;

    private UiLogger uiLogger;
    private AppiumDriverLocalService service;
    private AndroidDriver<WebElement> driver;

    public AppiumManager() {
        this.appiumHook = new Thread(this::cleanup);
    }

    public static String getElementDescription(AndroidElement element) {
        StringBuilder builder = new StringBuilder("[");

        AppiumManager.appendDescription(builder, "name", element.getAttribute("name"));
        AppiumManager.appendDescription(builder, "id", element.getAttribute("resource-id"));
        AppiumManager.appendDescription(builder, "class", element.getAttribute("class"));
        AppiumManager.appendDescription(builder, "desc", element.getAttribute("content-desc"));
        AppiumManager.appendDescription(builder, "clickable", element.getAttribute("clickable"));
        AppiumManager.appendDescription(builder, "scrollable", element.getAttribute("scrollable"));
        AppiumManager.appendDescription(builder, "long-clickable", element.getAttribute("long-clickable"));
        AppiumManager.appendDescription(builder, "enabled", element.getAttribute("enabled"));
        AppiumManager.appendDescription(builder, "text", element.getText());
        AppiumManager.appendDescription(builder, "location", element.getLocation().toString());
        AppiumManager.appendDescription(builder, "centre", element.getCenter().toString());

        builder.append("]");
        return builder.toString();
    }

    private static void appendDescription(StringBuilder builder, String valueId, String value) {
        if (value != null && !value.equals("null") && !value.isEmpty()) {
            if (builder.charAt(builder.length() - 1) != '[') {
                builder.append(", ");
            }
            builder.append(valueId).append(": ").append(value);
        }
    }

    public static void waitForUi(float seconds) {
        try {
            long milliseconds = (long) seconds * 1000;
            LOGGER.debug("Waiting {} seconds for UI.", seconds);
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void startAppiumService() {
        LOGGER.info("Starting Appium Service...");

        ServerArgument allowInsecureArg = () -> "--allow-insecure";
        ServerArgument basePathArg = () -> "--base-path";
        AppiumServiceBuilder builder = new AppiumServiceBuilder().withArgument(allowInsecureArg, "adb_shell")
                .withArgument(basePathArg, "/wd/hub");

        this.service = AppiumDriverLocalService.buildService(builder);
        this.service.clearOutPutStreams();

        try {
            this.service.addOutPutStream(Files.newOutputStream(Paths.get("target/logs/appium.log")));
        } catch (IOException e) {
            LOGGER.error("Problem adding Appium log file. {}", e.getMessage());
        }

        int attempts = 2;
        while (attempts > 0) {
            try {
                this.service.start();
            } catch (AppiumServerHasNotBeenStartedLocallyException e) {
                attempts -= 1;
                LOGGER.error("Failed to start Appium service ({} attempts left): {}", attempts, e.getMessage());
                continue;
            }
            break;
        }

        Runtime.getRuntime().addShutdownHook(this.appiumHook);
    }

    public void createAppiumDriver() {
        LOGGER.info("Creating Appium Driver...");

        DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities.setCapability(MobileCapabilityType.AUTOMATION_NAME, "UIAutomator2");
        capabilities.setCapability(MobileCapabilityType.DEVICE_NAME, "Android Emulator");
        capabilities.setCapability(MobileCapabilityType.FULL_RESET, "false");
        capabilities.setCapability(AndroidMobileCapabilityType.AUTO_GRANT_PERMISSIONS, "true");
        capabilities.setCapability(MobileCapabilityType.APP, TraversalSettings.v().getApkFile().getAbsolutePath());
        capabilities.setCapability(MobileCapabilityType.NO_RESET, "true");
        capabilities.setCapability(AndroidMobileCapabilityType.AUTO_LAUNCH, "false");
        capabilities.setCapability(MobileCapabilityType.NEW_COMMAND_TIMEOUT, 3600);

        if (this.service == null) {
            this.startAppiumService();
        }

        this.driver = new AndroidDriver<>(this.service.getUrl(), capabilities);
        this.driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
    }

    public void successfulCleanup() {
        Runtime.getRuntime().removeShutdownHook(this.appiumHook);
        cleanup();
    }

    public void cleanup() {
        if (this.driver != null) {
            LOGGER.info("Stopping logcat broadcast.");
            this.driver.stopLogcatBroadcast();
            LOGGER.info("Quitting Appium driver.");
            this.driver.quit();
        }

        if (this.service != null) {
            LOGGER.info("Stopping Appium service.");
            this.service.stop();
        }
    }

    public void startAppiumLogcatBroadcast() {
        LOGGER.info("Starting Appium Logcat Broadcast.");

        Consumer<String> logcatMessageConsumer = logMessage -> {
            if (logMessage.contains(LogHandler.M_TAG) || logMessage.contains(LogHandler.C_TAG) ||
                    logMessage.contains(LogHandler.I_TAG)) {
                LogHandler.handleMessage(logMessage);
            }
        };

        if (this.driver == null) {
            this.createAppiumDriver();
        }

        this.driver.addLogcatMessagesListener(logcatMessageConsumer);
        this.driver.startLogcatBroadcast();
    }

    public void startCoverage(String packageName) throws IOException {
        String[] command = {"acv", "start", packageName};

        ProcessBuilder builder = new ProcessBuilder(command);
        builder.directory(new File(System.getProperty("user.home")));
        File outputFile = new File(TraversalSettings.v().getOutputDirectory() + "/acv_start.log");
        builder.redirectOutput(outputFile);
        builder.redirectError(outputFile);

        LOGGER.info("Starting ACV coverage.");
        builder.start();
    }

    public AndroidDriver<WebElement> getDriver() {
        if (this.driver == null) {
            this.createAppiumDriver();
        }
        return this.driver;
    }

    public UiLogger getUiLogger() {
        if (this.uiLogger == null) {
            this.uiLogger = new UiLogger();
        }

        return this.uiLogger;
    }

    public String getCurrentActivity() {
        if (this.driver == null) {
            this.createAppiumDriver();
        }

        String activity = this.driver.currentActivity();
        if (activity.startsWith(".")) {
            activity = this.driver.getCurrentPackage() + activity;
        }

        LOGGER.debug("Current activity is {}", activity);
        return activity;
    }

    public String getCurrentPackage() {
        if (this.driver == null) {
            this.createAppiumDriver();
        }

        String currentPackage = this.driver.getCurrentPackage();
        LOGGER.debug("Current package is {}", currentPackage);
        return currentPackage;
    }

    public void navigateBack() {
        if (this.driver == null) {
            this.createAppiumDriver();
        }

        LOGGER.debug("Navigating back.");
        this.driver.navigate().back();
    }

    public void launchApp(String bundleId) {
        if (this.driver == null) {
            this.createAppiumDriver();
        }

        LOGGER.debug("Launching app.");
        this.driver.activateApp(bundleId);
    }

    public void activateApp(String bundleId, String launchActivity) {
        String activity = bundleId + "/" + launchActivity;
        List<String> activateArgs = Arrays.asList("start", "-n", activity);
        Map<String, Object> activateCmd = ImmutableMap.of("command", "am", "args", activateArgs);
        driver.executeScript("mobile: shell", activateCmd);
    }

    public void activateAppUsingMonkey(String bundleId) {
        List<String> activateArgs = Arrays.asList("-p", bundleId, "1");
        Map<String, Object> activateCmd = ImmutableMap.of("command", "monkey", "args", activateArgs);
        driver.executeScript("mobile: shell", activateCmd);
    }

    public void hideKeyboard() {
        if (this.driver == null) {
            this.createAppiumDriver();
        }

        if (this.driver.isKeyboardShown()) {
            LOGGER.debug("Hiding keyboard.");
            this.driver.hideKeyboard();
        }
    }

    @SuppressWarnings("unused")
    public List<String> findFragmentsOnScreen() {
        LOGGER.info("Searching for fragments in the current Activity... ");
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("command", "dumpsys");
        arguments.put("args",
                Lists.newArrayList("activity", this.getCurrentPackage() + "/" + this.getCurrentActivity())
                     );

        List<String> fragments = new ArrayList<>();

        String scriptOutput = this.driver.executeScript("mobile: shell", arguments).toString();
        List<String> output = Arrays.stream(scriptOutput.split("\n")).map(String::trim).collect(Collectors.toList());
        for (int i = 0; i < output.size(); i++) {
            if (output.get(i).contains("Added Fragments:")) {
                if (output.get(i + 1).startsWith("#")) {
                    String fragment = output.get(i + 1).replaceFirst("#\\d+:\\s+", "").replaceFirst("\\{.+", "");
                    fragments.add(fragment);
                }
            }
        }

        LOGGER.info("Found {} fragments in the current Activity.", fragments.size());
        return fragments;
    }

    public List<AndroidElement> getElementsOnScreen() {
        if (this.uiLogger == null) {
            this.uiLogger = new UiLogger();
        }

        LOGGER.info("Searching for elements on the screen... ");
        List<WebElement> elements = this.driver.findElementsByXPath("//*");
        List<AndroidElement> inputElements = new ArrayList<>();

        String activity = this.getCurrentActivity();
        for (WebElement element : elements) {
            AndroidElement androidElement = (AndroidElement) element;
            this.uiLogger.logElement(activity, androidElement);

            try {
                if (androidElement.getAttribute("clickable").equalsIgnoreCase("true") ||
                        androidElement.getAttribute("long-clickable").equalsIgnoreCase("true")) {
                    LOGGER.info("Found input element '{}'.", AppiumManager.getElementDescription(androidElement));
                    inputElements.add(androidElement);
                } else {
                    LOGGER.debug("Found view element '{}'.", AppiumManager.getElementDescription(androidElement));
                }
            } catch (StaleElementReferenceException ignored) {
                LOGGER.error("Stale Android element, ignoring and moving on.");
            }
        }

        BackElement backElement = new BackElement(this);
        LOGGER.info("Found input element '{}'.", AppiumManager.getElementDescription(backElement));
        inputElements.add(backElement);

        return inputElements;
    }

    @SuppressWarnings("unused")
    public List<AndroidElement> getAllElementsOnScreen() {
        LOGGER.info("Searching for all elements on the screen... ");
        List<WebElement> elements = this.driver.findElementsByXPath("//*");
        return getAndroidElements(elements);
    }

    @SuppressWarnings("unused")
    public List<AndroidElement> getClickElementsOnScreen() {
        LOGGER.info("Searching for click elements on the screen... ");
        By descriptor = MobileBy.AndroidUIAutomator("new UiSelector().clickable(true)");
        List<WebElement> elements = this.driver.findElements(descriptor);
        return getAndroidElements(elements);
    }

    private List<AndroidElement> getAndroidElements(List<WebElement> elements) {
        List<AndroidElement> androidElements = new ArrayList<>();

        for (WebElement element : elements) {
            AndroidElement androidElement = (AndroidElement) element;
            // LOGGER.info("Found view element '" + AppiumManager.getElementDescription(androidElement) + "'.");
            androidElements.add(androidElement);
        }

        BackElement backElement = new BackElement(this);
        // LOGGER.info("Found view element '" + AppiumManager.getElementDescription(backElement) + "'.");
        androidElements.add(backElement);

        return androidElements;
    }
}
