package org.droidmate.analyzer.tools;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.droidmate.analyzer.AppUnderTest;
import org.droidmate.analyzer.Configuration;
import org.droidmate.frontend.DroidmateFrontend;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

  private Path copyApkToWorkDir(AppUnderTest app, boolean inlined)
  {
    Path src, dst;
    if (inlined)
      src = app.getInlinedApkFile();
    else
      src = app.getApkFile();

    dst = Paths.get(this.cfg.workDir.toString(), src.getFileName().toString());

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

  private int runBoxMate(String[] args)
  {
    try
    {
      int exitCode = DroidmateFrontend.main(args, null);
      Thread.sleep(1000);
      return exitCode;
    }
    catch (Exception e)
    {
      logger.error(e.getMessage(), e);
      return 1;
    }
  }

  private Path findInlinedFile(AppUnderTest app)
  {
    Path dst = null;

    String apkFileName = FilenameUtils.removeExtension(app.getApkFile().getFileName().toString());
    try
    {
      Stream<Path> files = Files.list(this.cfg.workDir);

      Optional<Path> inlinedFile = files.filter(p -> p.getFileName().toString().contains(apkFileName)).findFirst();
      assert inlinedFile.isPresent();
      assert Files.exists(inlinedFile.get());

      dst = Paths.get(this.cfg.inlinedDir.toString(),
        apkFileName + "-inlined.apk");

      Files.deleteIfExists(dst);
      Files.copy(inlinedFile.get(), dst);
    }
    catch (IOException e)
    {
      logger.error(e.getMessage(), e);
    }

    return dst;
  }

  public Path inlineApp(AppUnderTest app)
  {
    String fileName = app.getApkFile().getFileName().toString();
    logger.info(String.format("BoxMate inline: %s", fileName));

    try
    {
      FileUtils.cleanDirectory(this.cfg.workDir.toFile());
      Path apkToInline = this.copyApkToWorkDir(app, false);

      // Create new file content
      //String configFileData = String.format(BOXMATE_ARGS_INLINE,
      //  apkToInline.toAbsolutePath().parent.toString());
      //this.createConfigFile(configFileData);

      String[] args = new String[] {
        BoxMateConsts.ARGS_INLINE,
        BoxMateConsts.ARGS_API23,
        String.format(BoxMateConsts.ARGS_DIR,
          apkToInline.toAbsolutePath().getParent().toString())};
      this.runBoxMate(args);

      return this.findInlinedFile(app);
    }
    catch (IOException e)
    {
      logger.error(e.getMessage(), e);
    }

    return null;
  }

  private void copyExplOutputToDir(Path dst)
  {
    Path explDir = Paths.get(".", "output_device1");

    try
    {
      if (Files.exists(dst))
        FileUtils.cleanDirectory(dst.toFile());
      Files.deleteIfExists(dst);

      FileUtils.copyDirectory(explDir.toFile() , dst.toFile());

      FileUtils.cleanDirectory(explDir.toFile());
      Files.deleteIfExists(explDir);
    }
    catch (IOException e)
    {
      logger.error(e.getMessage(), e);
    }
  }

  private Path getExplOutputDir(Scenario scenario)
  {
    String dstDirName = scenario.getCfgFile().getFileName().toString();
    Path dst = Paths.get(this.cfg.explorationDir.toString(), dstDirName);

    this.copyExplOutputToDir(dst);

    return dst;
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

  public ExplorationResult explore(AppUnderTest app, Scenario scenario)
  {
    String fileName = app.getInlinedApkFile().getFileName().toString();
    boolean hasScenario = scenario.getRestrictedApi() != null;
    if (hasScenario)
      logger.info(String.format("BoxMate explore scenario: %s", scenario.getCfgFile().getFileName().toString()));
    else
      logger.info(String.format("BoxMate explore: %s", fileName));

    try
    {
      FileUtils.cleanDirectory(this.cfg.workDir.toFile());
      Path apkToExplore = this.copyApkToWorkDir(app, true);

      List<String> args = new ArrayList<>();
      args.add(ARGS_API23);
      args.add(ARGS_RESET);
      args.add(ARGS_SEED);
      args.add(ARGS_SNAP);
      args.add(ARGS_TIME);
      args.add(String.format(ARGS_DIR,
          apkToExplore.toAbsolutePath().getParent().toString()));

      if (hasScenario)
        args.add(String.format(ARGS_XPRIVACY,
          scenario.getCfgFile().toAbsolutePath().toString()));

      this.runBoxMate(args.stream().toArray(String[]::new));

      Path explDir = this.getExplOutputDir(scenario);
      this.unpackSERFile(explDir);
      return new ExplorationResult(explDir);
    } catch (IOException e)
    {
      logger.error(e.getMessage(), e);
    }

    return null;
  }
}
