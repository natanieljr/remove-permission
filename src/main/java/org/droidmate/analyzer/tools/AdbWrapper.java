package org.droidmate.analyzer.tools;

import org.droidmate.analyzer.AppUnderTest;
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
public class AdbWrapper
{
  private static final Logger logger = LoggerFactory.getLogger(AdbWrapper.class);

  public int installApk(AppUnderTest apk)
  {
    logger.info(String.format("ADB INSTALL: %s", apk.getApkFile().getFileName()));

    String filePath = apk.getApkFile().toString();
    CommandExecutor exec = new CommandExecutor();

    try
    {
      String[] output = exec.execute(AdbConsts.INSTALL, filePath);
      return CommandExecutor.evalOutput(output);
    }
    catch (IOException e)
    {
      logger.error(e.getMessage(), e);
    }

    return 1;
  }

  public int uninstallApk(AppUnderTest apk)
  {
    String packageName = apk.getPackageName();
    logger.info(String.format("ADB UNINSTALL: %s", packageName));

    CommandExecutor exec = new CommandExecutor();
    try
    {
      String[] output = exec.execute(AdbConsts.UNINSTALL, packageName);
      return CommandExecutor.evalOutput(output);
    }
    catch (IOException e)
    {
      logger.error(e.getMessage(), e);
    }

    return 1;
  }

  public int pull(Path srcFile, Path dstFile)
  {
    // Convert from Windows do Linux path if necessary
    String src = srcFile.toString().replace('\\', '/');
    String dst = dstFile.toAbsolutePath().toString();

    logger.info(String.format("ADB PULL: %s %s", src, dst));

    try
    {
      // Create dir if not exists
      Path dstDir = dstFile.getParent();
      if (!Files.exists(dstDir))
        Files.createDirectories(dstDir);

      // Remove old file if necessary
      Files.deleteIfExists(dstFile);

      CommandExecutor exec = new CommandExecutor();
      String[] output = exec.execute(AdbConsts.PULL,
        src, dst);
      return CommandExecutor.evalOutput(output);
    }
    catch (IOException e)
    {
      logger.error(e.getMessage(), e);
    }

    return 1;
  }

  public int remove(Path srcFile)
  {
    // Convert from Windows do Linux path if necessary
    String src = srcFile.toString().replace('\\', '/');

    logger.info(String.format("ADB REMOVE: %s", src));

    CommandExecutor exec = new CommandExecutor();
    try
    {
    String[] output = exec.execute(AdbConsts.REMOVE, src);
    return CommandExecutor.evalOutput(output);
    }
    catch (IOException e)
    {
      logger.error(e.getMessage(), e);
    }

    return 1;
  }

  public int runIntent(String intent, String... params)
  {
    logger.info(String.format("ADB SHELL START: %s", intent));

    CommandExecutor exec = new CommandExecutor();
    List<String> command = new ArrayList<>();
    command.addAll(Arrays.asList(AdbConsts.RUN_INTENT.split(" ")));
    command.addAll(Arrays.asList(intent.split(" ")));
    command.addAll(Arrays.asList(params));
    try
    {
      String[] output = exec.execute((String[])command.toArray());
      return CommandExecutor.evalOutput(output);
    }
    catch (IOException e)
    {
      logger.error(e.getMessage(), e);
    }

    return 1;
  }
}
