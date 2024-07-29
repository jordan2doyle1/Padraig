package phd.research.traversal;

import io.appium.java_client.android.AndroidElement;
import phd.research.singletons.TraversalSettings;

import java.util.List;
import java.util.Random;

/**
 * @author Jordan Doyle
 */

public class RandomSearch extends Search {

    private final Random random;

    public RandomSearch() {
        super();
        this.random = new Random(TraversalSettings.v().getRandomSeed());
    }

    @Override
    protected List<AndroidElement> getAvailableControls() {
        return this.appiumManager.getElementsOnScreen();
    }

    @Override
    protected AndroidElement getNextControl(String activity, List<AndroidElement> controls) {
        return controls.get(this.random.nextInt(controls.size()));
    }
}
