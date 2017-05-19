package org.droidmate.analyzer.wrappers;

import org.droidmate.analyzer.IAppUnderTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Wrapper to the ADB command line tool
 */
public class AdbWrapper {
    private static final Logger logger = LoggerFactory.getLogger(AdbWrapper.class);

    public int installApk(IAppUnderTest apk) {
        logger.info(String.format("ADB INSTALL: %s", apk.getApkFile().getFileName()));

        String filePath = apk.getApkFile().toString();
        CommandLineWrapper exec = new CommandLineWrapper();

        try {
            String[] output = exec.execute(AdbConsts.INSTALL, filePath);
            return CommandLineWrapper.evalOutput(output);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }

        return 1;
    }

    public int uninstallApk(IAppUnderTest apk) {
        String packageName = apk.getPackageName();
        logger.info(String.format("ADB UNINSTALL: %s", packageName));

        CommandLineWrapper exec = new CommandLineWrapper();
        try {
            String[] output = exec.execute(AdbConsts.UNINSTALL, packageName);
            return CommandLineWrapper.evalOutput(output);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }

        return 1;
    }

    public int pull(Path srcFile, Path dstFile) {
        // Convert from Windows do Linux path if necessary
        String src = srcFile.toString().replace('\\', '/');
        String dst = dstFile.toAbsolutePath().toString();

        logger.info(String.format("ADB PULL: %s %s", src, dst));

        try {
            // Create dir if not exists
            Path dstDir = dstFile.getParent();
            if (!Files.exists(dstDir))
                Files.createDirectories(dstDir);

            // Remove old file if necessary
            Files.deleteIfExists(dstFile);

            CommandLineWrapper exec = new CommandLineWrapper();
            String[] output = exec.execute(AdbConsts.PULL,
                    src, dst);
            return CommandLineWrapper.evalOutput(output);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }

        return 1;
    }

    public int remove(Path srcFile) {
        // Convert from Windows do Linux path if necessary
        String src = srcFile.toString().replace('\\', '/');

        logger.info(String.format("ADB REMOVE: %s", src));

        CommandLineWrapper exec = new CommandLineWrapper();
        try {
            String[] output = exec.execute(AdbConsts.REMOVE, src);
            return CommandLineWrapper.evalOutput(output);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }

        return 1;
    }

    public int runIntent(String intent, String... params) {
        logger.info(String.format("ADB SHELL START: %s", intent));

        CommandLineWrapper exec = new CommandLineWrapper();
        List<String> command = new ArrayList<>();
        command.addAll(Arrays.asList(AdbConsts.RUN_INTENT.split(" ")));
        command.addAll(Arrays.asList(intent.split(" ")));
        command.addAll(Arrays.asList(params));
        try {
            String[] output = exec.execute((String[]) command.toArray());
            return CommandLineWrapper.evalOutput(output);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }

        return 1;
    }
}
