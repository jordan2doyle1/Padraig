package phd.research.main;

import org.apache.commons.cli.*;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import phd.research.helper.Timer;
import phd.research.singletons.GraphSettings;
import phd.research.singletons.TraversalSettings;
import phd.research.traversal.ModelSearch;
import phd.research.traversal.RandomSearch;
import phd.research.traversal.Search;
import phd.research.traversal.SystematicSearch;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * @author Jordan Doyle
 */

public class Main {

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        Options options = new Options();
        options.addOption(Option.builder("a").longOpt("apk-file").required().hasArg().numberOfArgs(1).argName("FILE")
                .desc("The APK file to analyse.").build());
        options.addOption(Option.builder("o").longOpt("output-directory").hasArg().numberOfArgs(1).argName("DIRECTORY")
                .desc("The directory for storing output files.").build());
        options.addOption(Option.builder("t").longOpt("traversal").hasArg().numberOfArgs(1).argName("TYPE")
                .desc("The type of traversal to perform on the app - RANDOM, DFS, MODEL").build());
        options.addOption(Option.builder("m").longOpt("max-interactions").hasArg().numberOfArgs(1).argName("INT")
                .desc("The number of interactions to perform on the app.").build());
        options.addOption(Option.builder("n").longOpt("max-no-interactions").hasArg().numberOfArgs(1).argName("INT")
                .desc("The number of iterations without an interaction before exiting loop.").build());
        options.addOption(Option.builder("s").longOpt("seed").hasArg().numberOfArgs(1).argName("INT")
                .desc("Seed value for random number generator.").build());

        options.addOption(Option.builder("c").longOpt("clean-directory").desc("Clean output directory.").build());
        options.addOption(Option.builder("v").longOpt("start-acv").desc("Start acv coverage before search.").build());
        options.addOption(Option.builder("h").longOpt("help").desc("Display help.").build());

        options.addOption(Option.builder("i").longOpt("import-CG").hasArg().numberOfArgs(1).argName("FILE")
                .desc("Import AndroGuard call graph from the given file.").build());
        options.addOption(Option.builder("l").longOpt("load-CFG").hasArg().numberOfArgs(1).argName("FILE")
                .desc("Load the control flow graph from the given file.").build());
        options.addOption(Option.builder("p").longOpt("android-platform").hasArg().numberOfArgs(1).argName("DIRECTORY")
                .desc("The Android SDK platform directory.").build());
        options.addOption(Option.builder("z").longOpt("serialized-callbacks").hasArg().numberOfArgs(1).argName("FILE")
                .desc("The FlowDroid serialized callbacks file.").build());


        CommandLine cmd = null;
        try {
            CommandLineParser parser = new DefaultParser();
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            HelpFormatter formatter = new HelpFormatter();
            final PrintWriter writer = new PrintWriter(System.out);
            formatter.printUsage(writer, 80, "Padraig", options);
            writer.flush();
            System.exit(10);
        }

        if (cmd.hasOption("h")) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("Padraig", options);
            System.exit(0);
        }

        GraphSettings graphSettings = GraphSettings.v();
        TraversalSettings traversalSettings = TraversalSettings.v();

        try {
            traversalSettings.setApkFile(new File(cmd.getOptionValue("a")));

            if (cmd.hasOption("t") && cmd.getOptionValue("t").equals("MODEL")) {
                graphSettings.setApkFile(new File(cmd.getOptionValue("a")));
                if (cmd.hasOption("i")) {
                    graphSettings.setCallGraphFile(new File(cmd.getOptionValue("i")));
                } else {
                    LOGGER.error("Option -i, an AndroGuard call graph is required for a model traversal.");
                    System.exit(10);
                }
            }
        } catch (IOException e) {
            LOGGER.error("APK file missing: {}", e.getMessage());
            System.exit(20);
        }

        if (cmd.hasOption("o")) {
            try {
                traversalSettings.setOutputDirectory(new File(cmd.getOptionValue("o")));

                if (cmd.hasOption("t") && cmd.getOptionValue("t").equals("MODEL")) {
                    graphSettings.setOutputDirectory(new File(cmd.getOptionValue("o")));
                }
            } catch (IOException e) {
                LOGGER.error("Output directory missing: {}", e.getMessage());
                System.exit(30);
            }
        }

        if (cmd.hasOption("t") && cmd.getOptionValue("t").equals("MODEL")) {
            if (cmd.hasOption("p")) {
                try {
                    graphSettings.setPlatformDirectory(new File(cmd.getOptionValue("p")));
                } catch (IOException e) {
                    LOGGER.error("Platform directory missing: {}", e.getMessage());
                    System.exit(40);
                }
            }

            if (cmd.hasOption("z")) {
                File serializedCallbacks = new File(cmd.getOptionValue("z"));
                if (serializedCallbacks.isFile()) {
                    graphSettings.setFlowDroidCallbacksFile(new File(cmd.getOptionValue("z")));
                } else {
                    LOGGER.error(
                            "Files missing: FlowDroid serialized callbacks file does not exist or is not a file ({}).",
                            serializedCallbacks
                                );
                    System.exit(50);
                }
            }

            if (cmd.hasOption("l")) {
                try {
                    graphSettings.setImportControlFlowGraph(new File(cmd.getOptionValue("l")));
                } catch (IOException e) {
                    LOGGER.error("Graph file missing: {}", e.getMessage());
                    System.exit(60);
                }
            }
        }

        if (cmd.hasOption("m")) {
            traversalSettings.setMaxInteractionCount(Integer.parseInt(cmd.getOptionValue("m")));
        }

        if (cmd.hasOption("n")) {
            traversalSettings.setMaxNoInteractionCount(Integer.parseInt(cmd.getOptionValue("n")));
        }

        if (cmd.hasOption("s")) {
            traversalSettings.setRandomSeed(Integer.parseInt(cmd.getOptionValue("s")));
        }

        if (cmd.hasOption("v")) {
            traversalSettings.setStartAcv(true);
        }

        try {
            traversalSettings.validate();

            if (cmd.hasOption("t") && cmd.getOptionValue("t").equals("MODEL")) {
                graphSettings.validate();
            }
        } catch (IOException e) {
            LOGGER.error("Files missing: {}", e.getMessage());
            System.exit(70);
        }

        if (cmd.hasOption("c")) {
            try {
                FileUtils.cleanDirectory(traversalSettings.getOutputDirectory());
            } catch (IOException e) {
                LOGGER.error("Failed to clean output directory.{}", e.getMessage());
            }
        }

        Timer timer = new Timer();
        LOGGER.info("Start time: {}", timer.start());

        Search traversal = null;
        String type = cmd.hasOption("t") ? cmd.getOptionValue("t") : "RANDOM";
        switch (type) {
            case "RANDOM":
                LOGGER.info("Creating random traversal search.");
                traversal = new RandomSearch();
                break;
            case "DFS":
                LOGGER.info("Creating systematic traversal search.");
                traversal = new SystematicSearch();
                break;
            case "MODEL":
                LOGGER.info("Creating model-based traversal search.");
                traversal = new ModelSearch();
                break;
            default:
                LOGGER.error("Unrecognised traversal type provided: {}", type);
                System.exit(50);
        }

        try {
            traversal.traverseUi();
        } catch (Exception e) {
            StringWriter stringWriter = new StringWriter();
            e.printStackTrace(new PrintWriter(stringWriter));
            LOGGER.error("Error occurred during traversal search: {}", stringWriter);
            traversal.getAppiumManager().successfulCleanup();
            System.exit(80);
        }


        LOGGER.info("End time: {}", timer.end());
        LOGGER.info("Execution time: {} second(s).", timer.secondsDuration());
    }
}