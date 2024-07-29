package phd.research.ui;

import io.appium.java_client.android.AndroidElement;
import org.openqa.selenium.Point;
import org.openqa.selenium.StaleElementReferenceException;

import java.util.Objects;

public class UiElement {

    private final String activity;

    private final String name;
    private final String id;
    private final String clazz;
    private final String description;
    private final String text;

    private final boolean clickable;
    private final boolean longClickable;
    private final boolean scrollable;
    private final boolean enabled;

    private final Point location;
    private final Point centre;

    public UiElement(String activity, AndroidElement element) throws StaleElementReferenceException {
        this.activity = activity;

        this.name = element.getAttribute("name");
        this.id = element.getAttribute("resource-id");
        this.clazz = element.getAttribute("class");
        this.description = element.getAttribute("content-desc");
        this.text = element.getText();

        this.clickable = Boolean.parseBoolean(element.getAttribute("clickable"));
        this.longClickable = Boolean.parseBoolean(element.getAttribute("long-clickable"));
        this.scrollable = Boolean.parseBoolean(element.getAttribute("scrollable"));
        this.enabled = Boolean.parseBoolean(element.getAttribute("enabled"));

        this.location = element.getLocation();
        this.centre = element.getCenter();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof UiElement)) {
            return false;
        }

        UiElement uiElement = (UiElement) o;
        return this.clickable == uiElement.clickable && this.longClickable == uiElement.longClickable &&
                this.scrollable == uiElement.scrollable && this.enabled == uiElement.enabled &&
                this.activity.equals(uiElement.activity) && Objects.equals(this.name, uiElement.name) &&
                Objects.equals(this.id, uiElement.id) && this.clazz.equals(uiElement.clazz) &&
                Objects.equals(this.description, uiElement.description) && Objects.equals(this.text, uiElement.text) &&
                location.equals(uiElement.location) && centre.equals(uiElement.centre);
    }

    @Override
    public int hashCode() {
        int result = this.activity.hashCode();
        result = 31 * result + (this.name != null ? this.name.hashCode() : 0);
        result = 31 * result + (this.id != null ? this.id.hashCode() : 0);
        result = 31 * result + this.clazz.hashCode();
        result = 31 * result + (this.description != null ? this.description.hashCode() : 0);
        result = 31 * result + (this.text != null ? this.text.hashCode() : 0);
        result = 31 * result + (this.clickable ? 1 : 0);
        result = 31 * result + (this.longClickable ? 1 : 0);
        result = 31 * result + (this.scrollable ? 1 : 0);
        result = 31 * result + (this.enabled ? 1 : 0);
        result = 31 * result + this.location.hashCode();
        result = 31 * result + this.centre.hashCode();
        return result;
    }
}
