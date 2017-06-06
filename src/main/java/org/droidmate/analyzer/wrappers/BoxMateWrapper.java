package org.droidmate.analyzer.wrappers;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.droidmate.analyzer.Configuration;
import org.droidmate.analyzer.exploration.ExplorationResult;
import org.droidmate.analyzer.exploration.IExplorationResult;
import org.droidmate.frontend.DroidmateFrontend;
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

    private AdbWrapper adbWrapper;
    private Configuration cfg;

    private List<String> getExploreArgs(Path apksDir){
        List<String> args = new ArrayList<>();
        args.add(BoxMateConsts.ARGS_API23);
        args.add(BoxMateConsts.ARGS_REPLACE_RESOURCES);
        args.add(BoxMateConsts.ARGS_RESET);
        args.add(BoxMateConsts.ARGS_SEED);
        args.add(BoxMateConsts.ARGS_SNAP);
        args.add(BoxMateConsts.ARGS_TIME);
        args.add(String.format(BoxMateConsts.ARGS_EXPL_OUTPUT_DIR, this.getExplorationOutputDir().toString()));
        args.add(String.format(BoxMateConsts.ARGS_DEVICE, this.getAdbWrapper().getDeviceIdx()));
        args.add(String.format(BoxMateConsts.ARGS_DIR,
                apksDir.toAbsolutePath().toString()));

        return args;
    }

    private List<String> getinlineArgs(Path apksDir){
        List<String> args = new ArrayList<>();
        args.add(BoxMateConsts.ARGS_INLINE);
        args.add(BoxMateConsts.ARGS_API23);
        args.add(String.format(BoxMateConsts.ARGS_DEVICE, this.cfg.deviceIdx));
        args.add(String.format(BoxMateConsts.ARGS_DIR,
                apksDir.toString()));

        return args;
    }

    private Path getExplorationOutputDir(){
        return Paths.get("output_device" + this.getAdbWrapper().getDeviceIdx());
    }

    public BoxMateWrapper(Configuration cfg) {
        this.cfg = cfg;
    }

    private AdbWrapper getAdbWrapper(){
        if (this.adbWrapper == null){
            this.adbWrapper = new AdbWrapper(cfg.deviceIdx);
        }

        return this.adbWrapper;
    }

    private Path copyApkToWorkDir(Path src) {
        Path dst = this.cfg.getWorkDir().resolve(src.getFileName());

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
            Stream<Path> files = Files.list(this.cfg.getWorkDir());

            Optional<Path> inlinedFile = files.filter(p -> p.getFileName().toString().contains(apkFileName)).findFirst();
            assert inlinedFile.isPresent();
            assert Files.exists(inlinedFile.get());

            dst = inlinedFile.get();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }

        return dst;
    }

    public Path inlineApp(Path apk) {
        String fileName = apk.getFileName().toString();
        logger.info(String.format("BoxMate inline: %s", fileName));

        try {
            FileUtils.cleanDirectory(this.cfg.getWorkDir().toFile());
            Path apkToInline = this.copyApkToWorkDir(apk);

            List<String> args = this.getinlineArgs(apkToInline.toAbsolutePath().getParent());
            this.runBoxMate(args.toArray(new String[0]));

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

        Path unpackedDir = explDir.resolve("raw_data");
        assert Files.exists(unpackedDir);
    }

    private void cleanDroidmateDirectories() {
        try {
            Path output = this.getExplorationOutputDir();
            if (Files.exists(output)) {
                FileUtils.cleanDirectory(output.toFile());
                Files.delete(output);
            }
        }
        catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    private void deployPoliciesFile(Path policiesFile){
        Path dst = this.cfg.getExtractedResDir().resolve(BoxMateConsts.FILE_API_POLICIES);
        try{
            Files.deleteIfExists(dst);
            assert !Files.exists(dst);
            Files.copy(policiesFile, dst);
        }catch(IOException e){
            logger.error(e.getMessage(), e);
        }

        assert Files.exists(dst);
    }

    public IExplorationResult explore(Path apk, Path policiesFile, boolean isInitialExpl) {
        String fileName = apk.getFileName().toString();
        if (isInitialExpl)
            logger.info(String.format("BoxMate explore: %s", fileName));
        else
            logger.info(String.format("BoxMate explore scenario: %s", apk.getFileName().toString()));

        this.cleanDroidmateDirectories();
        this.deployPoliciesFile(policiesFile);

        try {
            FileUtils.cleanDirectory(this.cfg.getWorkDir().toFile());
            Path apkToExplore = this.copyApkToWorkDir(apk);

            // Reboot and unlock the device to ensure all tests will be correctly executed
            // Due to exceptions generated form the monitor, sometimes the devices crashes
            //this.adbWrapper.rebootAndUnlock();

            List<String> args = this.getExploreArgs(apkToExplore.getParent());
            this.runBoxMate(args.toArray(new String[0]));

            Path explDir = this.getExplorationOutputDir();
            this.unpackSERFile(explDir);
            return new ExplorationResult(explDir);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }

        return null;
    }
}
