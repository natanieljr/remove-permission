package org.droidmate.analyzer;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Experiment configuration
 */
public class Configuration
{
  // Configuration
  String apkInputDir = "C:\\Users\\natan_000\\Desktop\\Saarland\\repositories\\apks\\original\\top5_apks";

  // Device variables
  Path xPrivacyConfigDir = Paths.get("/storage/emulated/0/.xprivacy/");

  //
  private Path dataDir       = Paths.get(".", "data");
  public Path workDir        = Paths.get(dataDir.toString(), "tmp");
  public Path inlinedDir     = Paths.get(dataDir.toString(), "inlined");
  Path origConfigDir         = Paths.get(dataDir.toString(), "original_cfg");
  Path configDir             = Paths.get(dataDir.toString(), "edited_cfg");
  public Path explorationDir        = Paths.get(dataDir.toString(), "exploration");
}
