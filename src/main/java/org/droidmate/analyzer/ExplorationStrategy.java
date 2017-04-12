package org.droidmate.analyzer;

import org.droidmate.analyzer.tools.Api;
import org.droidmate.analyzer.tools.ResourceManager;
import org.droidmate.analyzer.tools.Scenario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static java.nio.file.Files.exists;

/**
 * Define how scenarios are generated and combined
 */
class ExplorationStrategy
{
  private static final Logger logger = LoggerFactory.getLogger(ExplorationStrategy.class);
  private Configuration cfg;
  private int           lambdaIdx;

  ExplorationStrategy(Configuration cfg)
  {
    this.cfg = cfg;
  }

  private void saveScenarioConfig(Path path, List<String> data)
  {
    StringBuilder sb = new StringBuilder();
    for (String s : data)
    {
      sb.append(s);
      sb.append("\n");
    }

    try
    {
      if (exists(path))
          Files.delete(path);

      Files.write(path, sb.toString().getBytes());
    } catch (IOException e)
    {
      logger.error(e.getMessage(), e);
    }

    assert exists(path);
  }

  private List<Scenario> generateSimpleScenarios(AppUnderTest app)
  {
    logger.info("Generating simple scenarios");

    int xPrivacyId = app.getXPrivacyId();
    List<Scenario> scenarios = new ArrayList<>();

    // filter privacy sensitive APIs
    ResourceManager resourceManager = new ResourceManager();
    Stream<Api> apiStream = app.getInitialExpl().getResult().getApiList().stream()
      .filter(Api::isPrivacySensitive);

    // workaround, local vairables have to be final, instance's not
    this.lambdaIdx = 0;
    apiStream.forEachOrdered(api ->
    {
      Scenario scenario = new Scenario(api, app.getCurrExplDepth());
      List<String> changeList = resourceManager.getRestrictions(api);
      List<String> template = new ResourceManager().getFormattedTemplate(app);
      for (String change : changeList)
      {
        String falseVal = String.format(change, xPrivacyId, "**RESTRICTED**");
        String trueVal = String.format(change, xPrivacyId, "true");

        template.replaceAll(s -> s.replace(falseVal, trueVal));
      }

      template.replaceAll(s -> s.replace("**RESTRICTED**", "false")
        .replace("**APIs**", api.toString()));

      Path cfgPath = null;
      try
      {
        String fileName = String.format("%s_%s_%d_%d.xml", app.getPackageName(),
          app.getVersionName(), app.getCurrExplDepth(), this.lambdaIdx++);
        cfgPath = Paths.get(this.cfg.configDir.toString(), fileName);
        Files.createDirectories(cfgPath.getParent());
        this.saveScenarioConfig(cfgPath, template);
        scenario.setCfgFile(cfgPath);
        scenarios.add(scenario);
      } catch (IOException e)
      {
        logger.error(e.getMessage(), e);
      }

      assert Files.exists(cfgPath);
    });

    return scenarios;
  }

  private List<Scenario> generateCompositeScenarios(AppUnderTest app)
  {
    return null;
  }

  List<Scenario> generateScenarios(AppUnderTest app)
  {
    // Initial exploration
    if (app.getCurrExplDepth() == 0)
    {
      Scenario s = new Scenario(null, app.getCurrExplDepth());
      List<Scenario> res = new ArrayList<>();
      res.add(s);
      return res;
    }

    // First generation is simple scenarios
    if (app.getCurrExplDepth() == 1)
      return this.generateSimpleScenarios(app);
    else
      return this.generateCompositeScenarios(app);
  }
}
