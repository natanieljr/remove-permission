package org.droidmate.analyzer;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Experiment configuration
 */
public class Configuration
{
  // Configuration
  String apkInputDir = "/users/nataniel/Documents/saarland/repositories/remove-permission/apks";

  // Device variables
  //Path xPrivacyConfigDir = Paths.get("/storage/emulated/0/.xprivacy/");

  private Path droidMateDir             = Paths.get("/users/nataniel/Documents/saarland/repositories/droidmate/");
  public Path droidMateGradleFileDir    = Paths.get(droidMateDir.toString(), "dev/droidmate");
  public Path droidMateMonitoredApis    = Paths.get(droidMateGradleFileDir.toString(), "projects/resources/monitored_apis.json");
  public Path droidMateCompiledMonitor  = Paths.get(droidMateGradleFileDir.toString(), "projects/monitor-generator/build");
  public Path droidMateMonitorAPKTmp    = Paths.get(droidMateCompiledMonitor.toString(), "monitor_api23.apk");
  public Path droidMateMonitorAPK       = Paths.get(droidMateGradleFileDir.toString(), "temp_extracted_resources/monitor_api23.apk");

  public Path dataDir               = Paths.get("data");
  public Path workDir                = Paths.get(dataDir.toString(), "tmp");
}
