package org.droidmate.analyzer;

import net.dongliu.apk.parser.bean.ApkMeta;
import org.droidmate.analyzer.tools.AdbWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Extract configuration from XPrivacy
 */
class ConfiguratorExtractor
{
  private static final Logger logger = LoggerFactory.getLogger(ConfiguratorExtractor.class);

  private Configuration cfg;
  private AdbWrapper adbWrapper;

  private static final String XPRIVACY_EXPORT = "-a biz.bokhorst.xprivacy.action.EXPORT";
  private static final String DEVICE_FILE_NAME = "xPrivacyConfig.xml";

  ConfiguratorExtractor(Configuration cfg)
  {
    this.cfg = cfg;
    this.adbWrapper = new AdbWrapper();
  }

  private Path exportAndPull(AppUnderTest app)
  {
    Path deviceFile = Paths.get(this.cfg.xPrivacyConfigDir.toString(),
      DEVICE_FILE_NAME);

    String deviceFileStr = deviceFile.toString().replace('\\', '/');

    // Export configuration file
    this.adbWrapper.runIntent(XPRIVACY_EXPORT,
      String.format("-e FileName %s", deviceFileStr));

    // Sleep for 30 seconds to allow data to be exported
    try
    {
      Thread.sleep(30 * 1000);
    } catch (InterruptedException e)
    {
      logger.error(e.getMessage(), e);
    }

    // Copy file from device
    ApkMeta apkMeta = app.getMeta();
    String fileName = String.format("%s_%s",
      apkMeta.getPackageName(), apkMeta.getVersionName());
    Path localFile = Paths.get(this.cfg.origConfigDir.toString(), fileName);
    this.adbWrapper.pull(deviceFile, localFile);

    // Remove file from the device
    this.adbWrapper.remove(deviceFile);

    return localFile;
  }

  private int extractIdFromConfig(AppUnderTest app, Path cfgFile)
  {
    String pattern1 = "<PackageInfo Id=";
    String pattern2 = String.format("Name=\"%s", app.getPackageName());

    try
    {
      List<String> data = Files.readAllLines(cfgFile);
      String line = null;

      for (String l : data)
        if (l.contains(pattern1) && l.contains(pattern2))
        {
          line = l;
          break;
        }

      assert line != null;

      String id = line.split("\"")[1];

      return Integer.parseInt(id);
    }
    catch (IOException e)
    {
      logger.error(e.getMessage(), e);
    }

    return 0;
  }

  public int extractConfiguration(AppUnderTest app)
  {
    logger.info(String.format("Extracting configuration %s", app.getPackageName()));

    // Install APK
    this.adbWrapper.installApk(app);

    Path localFile = this.exportAndPull(app);

    // Uninstall APK
    this.adbWrapper.uninstallApk(app);

    return this.extractIdFromConfig(app, localFile);
  }
}
