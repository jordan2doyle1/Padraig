package phd.research.traversal;

import io.appium.java_client.android.AndroidElement;
import org.openqa.selenium.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import phd.research.appium.AppiumManager;
import phd.research.ui.BackElement;
import phd.research.ui.InputHistory;
import phd.research.ui.InputRecord;

import java.util.List;

/**
 * @author Jordan Doyle
 */

public class SystematicSearch extends Search {

    private static final Logger LOGGER = LoggerFactory.getLogger(SystematicSearch.class);

    private final InputHistory inputHistory;

    public SystematicSearch() {
        super();
        this.inputHistory = new InputHistory();
    }

    @Override
    protected List<AndroidElement> getAvailableControls() {
        return this.appiumManager.getElementsOnScreen();
    }

    @Override
    protected AndroidElement getNextControl(String activity, List<AndroidElement> controls) {
        for (AndroidElement currentControl : controls) {
            if (!this.inputHistory.containsInput(activity, currentControl)) {
                this.inputHistory.addInput(activity, currentControl);
                return currentControl;
            }
        }

        AndroidElement nextControl = controls.get(0);
        InputRecord nextRecord = this.inputHistory.getInputRecord(activity, nextControl);
        for (AndroidElement currentControl : controls) {
            InputRecord currentRecord = this.inputHistory.getInputRecord(activity, currentControl);
            if (currentRecord.isLeaf()) {
                continue;
            }

            if (currentRecord.getInputCount() < nextRecord.getInputCount()) {
                nextControl = currentControl;
                nextRecord = currentRecord;
            }
        }

        return nextControl;
    }

    @Override
    public void applyInput(String activity, AndroidElement control) {
        String activityBeforeInput = this.appiumManager.getCurrentActivity();
        this.inputHistory.incrementInputCount(activity, control);
        String cmd = control instanceof BackElement ? Search.backCommand() : Search.tapCommand(control.getCenter());
        this.inputTracker.logInput(cmd, AppiumManager.getElementDescription(control));
        Point centre = control.getCenter();
        control.click();

        AppiumManager.waitForUi(3f);
        if (activityBeforeInput.equals(this.appiumManager.getCurrentActivity())) {
            LOGGER.info("Setting last input as a leaf.");
            this.inputHistory.setAsLeaf(activity, centre);
        }
    }

}
