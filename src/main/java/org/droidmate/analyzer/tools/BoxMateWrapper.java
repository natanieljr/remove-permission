package org.droidmate.analyzer.tools;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.droidmate.analyzer.Configuration;
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

import static org.droidmate.analyzer.tools.BoxMateConsts.*;

/**
 * Wrapper to BoxMate
 */
public class BoxMateWrapper
{
  private static final Logger logger = LoggerFactory.getLogger(BoxMateWrapper.class);

  private Configuration cfg;

  public BoxMateWrapper(Configuration cfg)
  {
    this.cfg = cfg;
  }

  private Path copyApkToWorkDir(Path src)
  {
    Path dst = Paths.get(this.cfg.workDir.toString(), src.getFileName().toString());

    try
    {
      Files.copy(src, dst);
    }
    catch (IOException e)
    {
      logger.error(e.getMessage(), e);
    }

    return dst;
  }

  private void runBoxMate(String[] args)
  {
    try
    {
      //int exitCode =
      DroidmateFrontend.main(args, null);
      Thread.sleep(1000);
      //return exitCode;
    }
    catch (Exception e)
    {
      logger.error(e.getMessage(), e);
      //return 1;
    }
  }

  private Path findInlinedFile(Path apk)
  {
    Path dst = null;

    String apkFileName = FilenameUtils.removeExtension(apk.getFileName().toString());
    try
    {
      Stream<Path> files = Files.list(this.cfg.workDir);

      Optional<Path> inlinedFile = files.filter(p -> p.getFileName().toString().contains(apkFileName)).findFirst();
      assert inlinedFile.isPresent();
      assert Files.exists(inlinedFile.get());

      dst = inlinedFile.get();
    }
    catch (IOException e)
    {
      logger.error(e.getMessage(), e);
    }

    return dst;
  }

  public Path inlineApp(Path apk, Path monitoredApis)
  {
    this.recompileMonitor(monitoredApis);

    String fileName = apk.getFileName().toString();
    logger.info(String.format("BoxMate inline: %s", fileName));

    try
    {
      FileUtils.cleanDirectory(this.cfg.workDir.toFile());
      Path apkToInline = this.copyApkToWorkDir(apk);

      String[] args = new String[] {
        BoxMateConsts.ARGS_INLINE,
        BoxMateConsts.ARGS_API23,
        String.format(BoxMateConsts.ARGS_DIR,
          apkToInline.toAbsolutePath().getParent().toString())};
      this.runBoxMate(args);

      return this.findInlinedFile(apk);
    }
    catch (IOException e)
    {
      logger.error(e.getMessage(), e);
    }

    return null;
  }

  private void unpackSERFile(Path explDir)
  {
    String[] args = new String[] {
      BoxMateConsts.ARGS_UNPACK,
      BoxMateConsts.ARGS_API23,
      String.format(BoxMateConsts.ARGS_DIR,
        explDir.toAbsolutePath().toString())};
    this.runBoxMate(args);

    Path unpackedDir = Paths.get(explDir.toString(), "raw_data");
    assert Files.exists(unpackedDir);
  }

  public ExplorationResult explore(Path apk, boolean isInitialExpl)
  {
    String fileName = apk.getFileName().toString();
    if (isInitialExpl)
      logger.info(String.format("BoxMate explore: %s", fileName));
    else
      logger.info(String.format("BoxMate explore scenario: %s", apk.getFileName().toString()));

    try
    {
      FileUtils.cleanDirectory(this.cfg.workDir.toFile());
      Path apkToExplore = this.copyApkToWorkDir(apk);

      List<String> args = new ArrayList<>();
      args.add(ARGS_API23);
      args.add(ARGS_RESET);
      args.add(ARGS_SEED);
      args.add(ARGS_SNAP);
      args.add(ARGS_TIME);
      args.add(String.format(ARGS_DIR,
          apkToExplore.toAbsolutePath().getParent().toString()));

      this.runBoxMate(args.stream().toArray(String[]::new));

      Path explDir = Paths.get("output_device1");
      this.unpackSERFile(explDir);
      return new ExplorationResult(explDir);
    } catch (IOException e)
    {
      logger.error(e.getMessage(), e);
    }

    return null;
  }

  private void removeCompiledMonitorApkFiles(){
    try
    {
      if (Files.exists(this.cfg.droidMateCompiledMonitor))
        FileUtils.deleteDirectory(this.cfg.droidMateCompiledMonitor.toFile());
    } catch (IOException e){
      logger.error(e.getMessage(), e);
    }
  }

  private void copyMonitorAPKToDestination(){
    try
    {
      // Remove monitor
      Files.deleteIfExists(this.cfg.droidMateMonitorAPK);
      assert !Files.exists(this.cfg.droidMateMonitorAPK);
      assert Files.exists(this.cfg.droidMateMonitorAPKTmp);

      Files.copy(this.cfg.droidMateMonitorAPKTmp, this.cfg.droidMateMonitorAPK,
              StandardCopyOption.REPLACE_EXISTING);


      assert Files.exists(this.cfg.droidMateMonitorAPK);

    }
    catch (IOException e)
    {
      logger.error(e.getMessage(), e);
    }
  }

  private void createMonitoredApisBackup(){
    Path monitoredAPIs = this.cfg.droidMateMonitoredApis;
    Path monitoredAPKBak = monitoredAPIs.resolveSibling(monitoredAPIs.getFileName() + ".bak");

    if (!Files.exists(monitoredAPKBak)){
      try{
        Files.copy(monitoredAPIs, monitoredAPKBak, StandardCopyOption.REPLACE_EXISTING);
      }
      catch(IOException e){
        logger.error(e.getMessage(), e);
      }
    }

    assert Files.exists(monitoredAPKBak);
  }

  private void deployNewMonitoredApisFile(Path newFile){
    try{
      Files.deleteIfExists(this.cfg.droidMateMonitoredApis);

      Files.copy(newFile, this.cfg.droidMateMonitoredApis, StandardCopyOption.REPLACE_EXISTING);
    }
    catch (IOException e){
      logger.error(e.getMessage(), e);
    }

    assert Files.exists(this.cfg.droidMateMonitoredApis);
  }

  private void recompileMonitor(Path newMonitoredApisFile){
    this.createMonitoredApisBackup();
    this.removeCompiledMonitorApkFiles();
    this.deployNewMonitoredApisFile(newMonitoredApisFile);

    ProjectConnection connection = GradleConnector.newConnector()
            .forProjectDirectory(this.cfg.droidMateGradleFileDir.toFile())
            .connect();
    try {
      BuildLauncher build = connection.newBuild();
      build.forTasks(":projects:monitor-generator:buildMonitorApk_api23");
      build.setStandardOutput(System.out);
      build.run();
    } finally {
      connection.close();
    }

    this.copyMonitorAPKToDestination();
  }
}
