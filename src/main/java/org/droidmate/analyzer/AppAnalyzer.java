package org.droidmate.analyzer;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;

/**
 * Experiment
 */
class AppAnalyzer
{
  private static final Logger logger = LoggerFactory.getLogger(AppAnalyzer.class);

  private Configuration cfg;

  AppAnalyzer(Configuration cfg)
  {
    this.cfg = cfg;
  }

  private void initialize()
  {
    try
    {
      if (!Files.exists(this.cfg.workDir))
        Files.createDirectory(this.cfg.workDir);

      if (!Files.exists(this.cfg.inlinedDir))
        Files.createDirectory(this.cfg.inlinedDir);

      if (!Files.exists(this.cfg.configDir))
        Files.createDirectory(this.cfg.configDir);

      if (!Files.exists(this.cfg.origConfigDir))
        Files.createDirectory(this.cfg.origConfigDir);

      if (!Files.exists(this.cfg.explorationDir))
        Files.createDirectory(this.cfg.explorationDir);

      FileUtils.cleanDirectory(this.cfg.workDir.toFile());
    }
    catch (IOException e)
    {
      logger.error(e.getMessage(), e);
    }
  }

  void analyze(AppUnderTest app)
  {
    //this.initialize();

    ExplorationStrategy strategy = new ExplorationStrategy(this.cfg);
    app.explore(strategy);
  }
}
