package org.droidmate.analyzer;

import net.dongliu.apk.parser.ApkFile;
import net.dongliu.apk.parser.bean.ApkMeta;
import org.droidmate.analyzer.tools.BoxMateWrapper;
import org.droidmate.analyzer.tools.DummyApkMeta;
import org.droidmate.analyzer.tools.ExplorationResult;
import org.droidmate.analyzer.tools.Scenario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Application under evaluation
 */
public class AppUnderTest
{
  private static final Logger logger = LoggerFactory.getLogger(AppUnderTest.class);

  private Configuration  cfg;
  private BoxMateWrapper boxMate;
  private ApkFile        apk;
  private Path           apkFile;
  private int            xPrivacyId;
  private Path           inlinedApkFile;
  private List<Scenario> scenarios;
  private int            currExplDepth;

  AppUnderTest(Configuration cfg, Path path)
  {
    this.scenarios = new ArrayList<>();
    this.currExplDepth = 0;
    this.cfg = cfg;
    this.boxMate = new BoxMateWrapper(this.cfg);
    this.apkFile = path.toAbsolutePath();
    try
    {
      this.apk = new ApkFile(this.apkFile.toFile());
    }
    catch (IOException e)
    {
      logger.error(e.getMessage(), e);
    }

    assert this.apk != null;
  }

  /*public ApkFile getApk()
  {
    return this.apk;
  }*/

  public Path getApkFile()
  {
    return this.apkFile;
  }

  private void setInlinedApkFile(Path inlinedApkFile)
  {
    this.inlinedApkFile = inlinedApkFile;
  }

  public Path getInlinedApkFile()
  {
    return this.inlinedApkFile;
  }

  private void setXPrivacyId(int id)
  {
    this.xPrivacyId = id;
  }

  public int getXPrivacyId()
  {
    return this.xPrivacyId;
  }

  ApkMeta getMeta()
  {
    ApkMeta apkMeta = null;
    try
    {
      apkMeta = this.apk.getApkMeta();
    }
    catch (IOException e)
    {
      logger.error(e.getMessage(), e);
    }

    if (apkMeta == null)
      return new DummyApkMeta();

    return apkMeta;
  }

  Scenario getInitialExpl()
  {
    Stream<Scenario> scenarioStream = this.scenarios.stream()
      .filter(p -> p.getExplDepth() == 0);

    if (scenarioStream.findFirst().isPresent())
      return scenarioStream.findFirst().get();

    return null;
  }

  int getCurrExplDepth()
  {
    return this.currExplDepth;
  }

  public List<Scenario> getScenarios()
  {
    return this.scenarios;
  }

  public void addScenario(Scenario expl)
  {
    assert expl != null;
    this.scenarios.add(expl);
  }

  public void addScenarios(Collection<Scenario> expl)
  {
    assert (expl != null);

    if (expl.size() > 0)
      this.scenarios.addAll(expl);
  }

  private List<Scenario> getPendingScenarios()
  {
    return this.scenarios.stream()
      .filter(p -> p.getResult() == null)
      .collect(Collectors.toList());
  }

  private boolean hasPendingScenarios()
  {
    return this.getPendingScenarios().size() > 0;
  }

  String getVersionName()
  {
    return this.getMeta().getVersionName();
  }

  public String getPackageName()
  {
    return this.getMeta().getPackageName();
  }

  private void inline()
  {
    // Inline app
    //Path inlinedFile = boxMate.inlineApp(app);
    Path inlinedFile = Paths.get("data", "inlined", "air.com.demute.TaoMix_v1.1.13-inlined.apk");
    this.setInlinedApkFile(inlinedFile);
    assert this.getInlinedApkFile() != null;
  }

  private void extractConfig()
  {
    // Extract configuration and XPrivacy id
    ConfiguratorExtractor cfgExtr  = new ConfiguratorExtractor(this.cfg);
    //int xPrivacyId = cfgExtr.extractConfiguration(apk);
    int xPrivacyId = 9999;
    this.setXPrivacyId(xPrivacyId);
    assert this.getXPrivacyId() > 0;
  }

  void explore(ExplorationStrategy strategy)
  {
    this.extractConfig();
    this.inline();

    // Initial expl
    List<Scenario> initialExpl = strategy.generateScenarios(this);
    this.addScenarios(initialExpl);

    while (this.hasPendingScenarios())
    {
      for (Scenario scenario : this.getPendingScenarios())
      {
        ExplorationResult explRes;

        if (this.getCurrExplDepth() == 0)
        {
          Path explDir = Paths.get("data", "exploration", "first-run", "air.com.demute.TaoMix_1.1.13");
          explRes = new ExplorationResult(explDir);
        }
        else
        {
          explRes = boxMate.explore(this, scenario);
        }
        scenario.setResult(explRes);
        assert (scenario.getResult() != null) && (Files.exists(scenario.getResult().getExplDir()));
      }

      ++this.currExplDepth;
      List<Scenario> newScenarios = strategy.generateScenarios(this);
      this.addScenarios(newScenarios);
    }
  }
}
