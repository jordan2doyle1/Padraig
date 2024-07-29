package phd.research.ui;

import io.appium.java_client.android.AndroidElement;
import org.openqa.selenium.Point;
import phd.research.appium.AppiumManager;

import java.util.HashMap;
import java.util.Map;

public class BackElement extends AndroidElement {

    private static final Map<String, String> ATTRIBUTES;

    static {
        ATTRIBUTES = new HashMap<>();
        ATTRIBUTES.put("class", "android.widget.Button");
        ATTRIBUTES.put("name", "BACK");
        ATTRIBUTES.put("content-desc", "Navigate Back");
    }

    private final AppiumManager appiumManager;

    public BackElement(AppiumManager appiumManager) {
        super();
        this.appiumManager = appiumManager;

        BackElement.ATTRIBUTES.put("resource-id", this.appiumManager.getCurrentPackage() + ":id/back");
    }

    @Override
    public String getAttribute(String name) {
        return BackElement.ATTRIBUTES.getOrDefault(name, null);
    }

    @Override
    public String getText() {
        return "BACK";
    }

    @Override
    public String getTagName() {
        return "";
    }

    @Override
    public Point getLocation() {
        return new Point(-1, -1);
    }

    @Override
    public Point getCenter() {
        return new Point(-1, -1);
    }

    @Override
    public void click() {
        this.appiumManager.navigateBack();
    }
}
