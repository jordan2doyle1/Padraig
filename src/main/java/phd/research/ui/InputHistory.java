package phd.research.ui;

import io.appium.java_client.android.AndroidElement;
import org.openqa.selenium.Point;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class InputHistory {

    Map<String, Set<InputRecord>> history;

    public InputHistory() {
        this.history = new HashMap<>();
    }

    public boolean containsInput(String activity, AndroidElement control) {
        Set<InputRecord> records = this.history.getOrDefault(activity, new HashSet<>());

        for (InputRecord record : records) {
            if (control.getCenter().equals(record.getLocation())) {
                return true;
            }
        }
        return false;
    }

    public void addInput(String activity, AndroidElement control) {
        InputRecord record = new InputRecord(activity, control.getCenter());
        Set<InputRecord> records = this.history.getOrDefault(activity, new HashSet<>());
        records.add(record);
        this.history.put(activity, records);
    }

    public InputRecord getInputRecord(String activity, AndroidElement control) {
        return this.getInputRecord(activity, control.getCenter());
    }

    public InputRecord getInputRecord(String activity, Point centre) {
        Set<InputRecord> records = this.history.getOrDefault(activity, new HashSet<>());

        for (InputRecord record : records) {
            if (centre.equals(record.getLocation())) {
                return record;
            }
        }

        return null;
    }

    public void setAsLeaf(String activity, Point centre) {
        InputRecord record = this.getInputRecord(activity, centre);

        if (record != null) {
            record.setLeaf(true);
        }
    }

    public void incrementInputCount(String activity, AndroidElement control) {
        InputRecord record = this.getInputRecord(activity, control);

        if (record != null) {
            record.incrementInputCount();
        }
    }
}
