package phd.research.utilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import phd.research.singletons.TraversalSettings;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * @author Jordan Doyle
 */

public class LogHandler {

    public static final String M_TAG = "<METHOD>";
    public static final String I_TAG = "<INTERACTION>";
    public static final String C_TAG = "<CONTROL>";

    private static final Logger LOGGER = LoggerFactory.getLogger(LogHandler.class);

    public static void handleMessage(String message) {
        if (!message.startsWith("<")) {
            message = message.substring(message.indexOf('<')).replaceAll("'", "");
        }

        String log = TraversalSettings.v().getOutputDirectory() + File.separator + "traversal.log";

        try (FileWriter fileWriter = new FileWriter(log, true)) {
            try (BufferedWriter bufferedWriter = new BufferedWriter(fileWriter)) {
                bufferedWriter.write(message + "\n");
            } catch (IOException e) {
                LOGGER.error("Failed to write message to log. {}", e.getMessage());
            }
        } catch (IOException e) {
            LOGGER.error("Failed to open FileWriter for log. {}", e.getMessage());
        }
    }
}
