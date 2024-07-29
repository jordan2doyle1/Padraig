package phd.research.ui;

import org.openqa.selenium.Point;

public class InputRecord {

    private final String activity;
    private final Point location;

    private Integer count;
    private Boolean leaf;

    public InputRecord(String activity, Point location) {
        this(activity, 1, false, location);
    }

    public InputRecord(String activity, Integer count, Boolean leaf, Point location) {
        this.activity = activity;
        this.count = count;
        this.leaf = leaf;
        this.location = location;
    }

    public boolean isLeaf() {
        return this.leaf;
    }

    public void setLeaf(boolean leaf) {
        this.leaf = leaf;
    }

    public int getInputCount() {
        return this.count;
    }

    public void incrementInputCount() {
        this.count++;
    }

    public Point getLocation() {
        return this.location;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof InputRecord)) {
            return false;
        }

        InputRecord inputRecord = (InputRecord) o;
        return activity.equals(inputRecord.activity) && location.equals(inputRecord.location);
    }

    @Override
    public int hashCode() {
        int result = activity.hashCode();
        result = 31 * result + location.hashCode();
        return result;
    }
}
