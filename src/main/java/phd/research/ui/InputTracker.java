package phd.research.ui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import phd.research.singletons.TraversalSettings;
import phd.research.utilities.LogHandler;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class InputTracker {

    private static final Logger LOGGER = LoggerFactory.getLogger(InputTracker.class);
    private final List<String> inputs;
    private int inputCount;
    private int previousInputCount;
    private int noInputCount;

    public InputTracker() {
        this.inputCount = 0;
        this.noInputCount = 0;
        this.inputs = new ArrayList<>();
    }

    public int getInputCount() {
        return this.inputCount;
    }

    public void logInput(String input, String description) {
        this.inputCount++;
        this.inputs.add(input);
        LogHandler.handleMessage(
                LogHandler.I_TAG + " InputRecord " + this.inputCount + ": " + input + " " + description);
        LOGGER.info("InputRecord {}: {} {}", this.inputCount, input, description);
    }

    public boolean isIncomplete() {
        return this.inputCount < TraversalSettings.v().getMaxInteractionCount();
    }

    public boolean canContinue() {
        LOGGER.info("Checking for stopping conditions.");

        if (this.previousInputCount == this.inputCount) {
            this.noInputCount++;
        } else {
            this.previousInputCount = this.inputCount;
            this.noInputCount = 0;
        }

        if (this.noInputCount >= TraversalSettings.v().getMaxNoInteractionCount()) {
            LOGGER.error("Infinite loop detected, no input found in {} iterations... Stopping.", this.noInputCount);
            return false;
        }

        if (this.inputCount >= TraversalSettings.v().getMaxInteractionCount()) {
            LOGGER.info("InputRecord count has reached {}... Stopping.", this.inputCount);
            return false;
        }

        return true;
    }

    public void writeInputScript() {
        String script = TraversalSettings.v().getOutputDirectory() + File.separator + "test_suite.txt";

        try (FileWriter fileWriter = new FileWriter(script, true)) {
            try (BufferedWriter bufferedWriter = new BufferedWriter(fileWriter)) {
                for (String input : this.inputs) {
                    bufferedWriter.write(input + "\n");
                }
            } catch (IOException e) {
                LOGGER.error("Failed to write message to script. {}", e.getMessage());
            }
        } catch (IOException e) {
            LOGGER.error("Failed to open FileWriter for script. {}", e.getMessage());
        }
    }

}
