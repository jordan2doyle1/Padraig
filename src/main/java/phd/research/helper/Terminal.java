package phd.research.helper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Terminal {

    private static final Logger LOGGER = LoggerFactory.getLogger(Terminal.class);

    public Terminal() {

    }

    public static String executeCommand(String command) {
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command("bash", "-c", command);

        try {
            LOGGER.debug("Executing command '{}'.", command);
            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();

            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            process.waitFor();
            return output.toString().trim();
        } catch (IOException | InterruptedException e) {
            LOGGER.error("Failed to execute command: {}", command);
            LOGGER.error("Command error message: {}", e.getMessage());
            return null;
        }
    }

}
