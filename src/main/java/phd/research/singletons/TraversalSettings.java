package phd.research.singletons;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * @author Jordan Doyle
 */

public class TraversalSettings {

    private static final Logger LOGGER = LoggerFactory.getLogger(TraversalSettings.class);

    private static TraversalSettings instance = null;

    private boolean loggerActive;
    private boolean startAcv;

    private File outputDirectory;
    private File apkFile;

    private int maxInteractionCount;
    private int maxNoInteractionCount;
    private int randomSeed;

    private TraversalSettings() {
        this.loggerActive = true;
        this.outputDirectory = new File(System.getProperty("user.dir") + File.separator + "output");
        this.maxInteractionCount = 500;
        this.maxNoInteractionCount = 100;
        this.randomSeed = 123456;
        this.startAcv = false;
    }

    public static TraversalSettings v() {
        if (instance == null) {
            instance = new TraversalSettings();
        }
        return instance;
    }

    public void validate() throws IOException {
        this.loggerActive = false;
        setOutputDirectory(this.outputDirectory);
        setApkFile(this.apkFile);
        this.loggerActive = true;
    }

    public File getApkFile() {
        return this.apkFile;
    }

    public void setApkFile(File apkFile) throws IOException {
        if (apkFile == null || !apkFile.isFile()) {
            throw new IOException("Apk file does not exist or is not a file (" + apkFile + ").");
        }

        this.apkFile = apkFile;

        if (this.loggerActive) {
            LOGGER.info("Apk file set as '{}'.", apkFile.getAbsolutePath());
        }
    }

    public File getOutputDirectory() {
        return this.outputDirectory;
    }

    public void setOutputDirectory(File outputDirectory) throws IOException {
        if (!outputDirectory.isDirectory()) {
            throw new IOException("Output directory does not exist or is not a directory (" + outputDirectory + ").");
        }

        this.outputDirectory = outputDirectory;

        if (this.loggerActive) {
            LOGGER.info("Output directory set as '{}.", outputDirectory.getAbsolutePath());
        }
    }

    public int getMaxInteractionCount() {
        return this.maxInteractionCount;
    }

    public void setMaxInteractionCount(int count) {
        this.maxInteractionCount = count;
        LOGGER.info("Max interaction count set as {}.", count);
    }

    public int getMaxNoInteractionCount() {
        return this.maxNoInteractionCount;
    }

    public void setMaxNoInteractionCount(int count) {
        this.maxNoInteractionCount = count;
        LOGGER.info("Max no interaction count set as {}.", count);
    }

    public int getRandomSeed() {
        return this.randomSeed;
    }

    public void setRandomSeed(int seed) {
        this.randomSeed = seed;
        LOGGER.info("Random seed set as {}.", seed);
    }

    public boolean isStartAcv() {
        return this.startAcv;
    }

    public void setStartAcv(boolean start) {
        this.startAcv = start;
    }
}
