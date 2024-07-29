package phd.research.main;

import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import phd.research.helper.Timer;

import java.io.*;

/**
 * @author Jordan Doyle
 */

public class RemoveAnsiColors {

    private static final Logger logger = LoggerFactory.getLogger(RemoveAnsiColors.class);

    public static void main(String[] args) {
        Options options = new Options();
        options.addOption(Option.builder("l").longOpt("log-file").required().hasArg().numberOfArgs(1).argName("FILE")
                .desc("Log file containing ANSI colors").build());
        options.addOption(Option.builder("h").longOpt("help").desc("Display help.").build());

        CommandLine cmd = null;
        try {
            CommandLineParser parser = new DefaultParser();
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            HelpFormatter formatter = new HelpFormatter();
            final PrintWriter writer = new PrintWriter(System.out);
            formatter.printUsage(writer, 80, "RemoveANSIColors", options);
            writer.flush();
            System.exit(0);
        }

        if (cmd.hasOption("h")) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("RemoveANSIColors", options);
            System.exit(0);
        }

        Timer timer = new Timer();
        logger.info("Start time: {}", timer.start());

        File log = new File(cmd.getOptionValue("l"));
        if (!log.exists()) {
            logger.error("Log file does not exist ({}).", log);
            System.exit(10);
        }

        try {
            BufferedReader inputLog = new BufferedReader(new FileReader(log));
            StringBuilder outputBuilder = new StringBuilder();
            String line;

            while ((line = inputLog.readLine()) != null) {
                line = line.replaceAll("\u001B\\[[;\\d]*m", "");
                outputBuilder.append(line);
                outputBuilder.append('\n');
            }
            inputLog.close();

            FileOutputStream outputLog = new FileOutputStream(log);
            outputLog.write(outputBuilder.toString().getBytes());
            outputLog.close();
        } catch (IOException e) {
            logger.error("Problem reading log file ({}).", log);
            System.exit(20);
        }

        logger.info("End time: {}", timer.end());
        logger.info("Execution time: {} second(s).", timer.secondsDuration());
    }
}
