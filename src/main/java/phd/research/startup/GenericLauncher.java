package phd.research.startup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import phd.research.appium.AppiumManager;

public class GenericLauncher extends AppLauncher {

    private static final Logger LOGGER = LoggerFactory.getLogger(GenericLauncher.class);

    public GenericLauncher(String bundleId, String launchActivity, AppiumManager appiumManager) {
        super(bundleId, launchActivity, appiumManager);
    }

    @Override
    protected void setup() {
        LOGGER.info("App launch setup complete.");
    }

}
