package phd.research.ui;

import com.google.gson.Gson;
import io.appium.java_client.android.AndroidElement;
import org.openqa.selenium.StaleElementReferenceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import phd.research.singletons.TraversalSettings;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UiLogger {

    private static final Logger LOGGER = LoggerFactory.getLogger(UiLogger.class);

    Map<String, List<UiElement>> elements;

    public UiLogger() {
        elements = new HashMap<>();
    }

    public void logElement(String activity, AndroidElement element) {
        UiElement uiElement;
        try {
            uiElement = new UiElement(activity, element);
        } catch (StaleElementReferenceException ignored) {
            LOGGER.error("Stale Android element, failed to log UI element.");
            return;
        }

        List<UiElement> activityElements = this.elements.getOrDefault(activity, new ArrayList<>());
        if (!activityElements.contains(uiElement)) {
            activityElements.add(uiElement);
            this.elements.put(activity, activityElements);
        }
    }

    public void writeUiLog() {
        String log = TraversalSettings.v().getOutputDirectory() + File.separator + "ui.json";

        Gson gson = new Gson();
        String json = gson.toJson(this.elements);

        try (FileWriter fileWriter = new FileWriter(log, true)) {
            try (BufferedWriter bufferedWriter = new BufferedWriter(fileWriter)) {
                bufferedWriter.write(json);
            } catch (IOException e) {
                LOGGER.error("Failed to write message to log. {}", e.getMessage());
            }
        } catch (IOException e) {
            LOGGER.error("Failed to open FileWriter for log. {}", e.getMessage());
        }
    }
}
