package org.droidmate.analyzer.wrappers;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.droidmate.analyzer.Configuration;
import org.droidmate.analyzer.exploration.ExplorationResult;
import org.droidmate.analyzer.exploration.IExplorationResult;
import org.droidmate.frontend.DroidmateFrontend;
import org.gradle.tooling.BuildLauncher;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProjectConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Wrapper to BoxMate
 */
public class BoxMateWrapper {
    private static final Logger logger = LoggerFactory.getLogger(BoxMateWrapper.class);

    private AdbWrapper adbWrapper = new AdbWrapper();
    private Configuration cfg;

    public BoxMateWrapper(Configuration cfg) {
        this.cfg = cfg;
    }

    private Path copyApkToWorkDir(Path src) {
        Path dst = Paths.get(this.cfg.workDir.toString(), src.getFileName().toString());

        try {
            Files.copy(src, dst);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }

        return dst;
    }

    private void runBoxMate(String[] args) {
        try {
            //int exitCode =
            DroidmateFrontend.main(args, null);
            Thread.sleep(1000);
            //return exitCode;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            //return 1;
        }
    }

    private Path findInlinedFile(Path apk) {
        Path dst = null;

        String apkFileName = FilenameUtils.removeExtension(apk.getFileName().toString());
        try {
            Stream<Path> files = Files.list(this.cfg.workDir);

            Optional<Path> inlinedFile = files.filter(p -> p.getFileName().toString().contains(apkFileName)).findFirst();
            assert inlinedFile.isPresent();
            assert Files.exists(inlinedFile.get());

            dst = inlinedFile.get();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }

        return dst;
    }

    public Path inlineApp(Path apk, Path monitoredApis) {
        this.recompileMonitor(monitoredApis);

        String fileName = apk.getFileName().toString();
        logger.info(String.format("BoxMate inline: %s", fileName));

        try {
            FileUtils.cleanDirectory(this.cfg.workDir.toFile());
            Path apkToInline = this.copyApkToWorkDir(apk);

            String[] args = new String[]{
                    BoxMateConsts.ARGS_INLINE,
                    BoxMateConsts.ARGS_API23,
                    String.format(BoxMateConsts.ARGS_DIR,
                            apkToInline.toAbsolutePath().getParent().toString())};
            this.runBoxMate(args);

            return this.findInlinedFile(apk);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }

        return null;
    }

    private void unpackSERFile(Path explDir) {
        String[] args = new String[]{
                BoxMateConsts.ARGS_UNPACK,
                BoxMateConsts.ARGS_API23,
                String.format(BoxMateConsts.ARGS_DIR,
                        explDir.toAbsolutePath().toString())};
        this.runBoxMate(args);

        Path unpackedDir = Paths.get(explDir.toString(), "raw_data");
        assert Files.exists(unpackedDir);
    }

    private void cleanDroidmateDirectories() {
        try {
            Path output = Paths.get("output_device1");
            if (Files.exists(output)) {
                FileUtils.cleanDirectory(output.toFile());
                Files.delete(output);
            }

            Path resources = Paths.get("temp_extracted_resources");
            if (Files.exists(resources)) {
                FileUtils.cleanDirectory(resources.toFile());
                Files.delete(resources);
            }
        }
        catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public IExplorationResult explore(Path apk, boolean isInitialExpl) {
        String fileName = apk.getFileName().toString();
        if (isInitialExpl)
            logger.info(String.format("BoxMate explore: %s", fileName));
        else
            logger.info(String.format("BoxMate explore scenario: %s", apk.getFileName().toString()));

        this.cleanDroidmateDirectories();

        try {
            FileUtils.cleanDirectory(this.cfg.workDir.toFile());
            Path apkToExplore = this.copyApkToWorkDir(apk);

            // Reboot and unlock the device to ensure all tests will be correctly executed
            // Due to exceptions generated form the monitor, sometimes the devices crashes
            this.adbWrapper.rebootAndUnlock();

            List<String> args = new ArrayList<>();
            args.add(BoxMateConsts.ARGS_API23);
            args.add(BoxMateConsts.ARGS_RESET);
            args.add(BoxMateConsts.ARGS_SEED);
            args.add(BoxMateConsts.ARGS_SNAP);
            args.add(BoxMateConsts.ARGS_TIME);
            args.add(String.format(BoxMateConsts.ARGS_DIR,
                    apkToExplore.toAbsolutePath().getParent().toString()));

            this.runBoxMate(args.toArray(new String[0]));

            Path explDir = Paths.get("output_device1");
            this.unpackSERFile(explDir);
            return new ExplorationResult(explDir);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }

        return null;
    }

    private void removeCompiledMonitorApkFiles() {
        logger.debug("Removing compiled DoridMate monitor to ensure gradle will deploy correctly");
        try {
            if (Files.exists(Paths.get("temp_extracted_resources")))
                new CommandLineWrapper().execute("find temp_extracted_resources -name '*.apk' -type f -delete");
            new CommandLineWrapper().execute("find " + cfg.droidMateGradleFileDir.toString() + " -name '*.apk' -type f -delete");
        }
        catch (IOException e){
            logger.error(e.getMessage(), e);
        }
    }

    private void copyMonitorAPKToDestination() {
        try {
            if (Files.exists(cfg.droidMateExtractedRes)){
                FileUtils.cleanDirectory(cfg.droidMateExtractedRes.toFile());
                Files.delete(cfg.droidMateExtractedRes);
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    private void createMonitoredApisBackup() {
        Path monitoredAPIs = this.cfg.droidMateMonitoredApis;
        Path monitoredAPKBak = monitoredAPIs.resolveSibling(monitoredAPIs.getFileName() + ".bak");

        if (!Files.exists(monitoredAPKBak)) {
            logger.debug("Backing up original monitored_apis.json file");
            try {
                Files.copy(monitoredAPIs, monitoredAPKBak, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }

        assert Files.exists(monitoredAPKBak);
    }

    private void deployNewMonitoredApisFile(Path newFile) {
        try {
            Files.deleteIfExists(this.cfg.droidMateMonitoredApis);

            Files.copy(newFile, this.cfg.droidMateMonitoredApis, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }

        assert Files.exists(this.cfg.droidMateMonitoredApis);
    }

    private void recompileMonitor(Path newMonitoredApisFile) {
        this.createMonitoredApisBackup();
        this.removeCompiledMonitorApkFiles();
        this.deployNewMonitoredApisFile(newMonitoredApisFile);

        logger.info("Recompiling DroidMate (gradlew clean build)");
        ProjectConnection connection = GradleConnector.newConnector()
                .forProjectDirectory(this.cfg.droidMateGradleFileDir.toFile())
                .connect();
        try {
            BuildLauncher build = connection.newBuild();
            build.forTasks("clean", "build");
            build.setStandardOutput(null);
            //build.setStandardOutput(System.out);
            build.run();
        } finally {
            connection.close();
        }

        this.copyMonitorAPKToDestination();
    }
}
