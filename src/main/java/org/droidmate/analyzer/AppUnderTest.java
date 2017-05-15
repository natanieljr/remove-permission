package org.droidmate.analyzer;

import net.dongliu.apk.parser.ApkFile;
import net.dongliu.apk.parser.bean.ApkMeta;
import org.apache.commons.io.FileUtils;
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
  private List<Scenario> scenarios;
  private int            currExplDepth;
  private Path           dir;

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
    this.createExperimentDir();
  }

  private void createExperimentDir(){
    ApkMeta meta = this.getMeta();
    String dirName = String.format("%s_%s", meta.getPackageName(), meta.getVersionName());
    this.dir = Paths.get(this.cfg.dataDir.toString(), dirName);

    try{
      Files.createDirectories(this.dir);
      FileUtils.cleanDirectory(this.dir.toFile());
    }
    catch (IOException e){
      logger.error(e.getMessage(), e);
    }

    assert Files.exists(this.dir);
  }

  public Path getApkFile()
  {
    return this.apkFile;
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

  public Path getDir(){
    return this.dir;
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

  public String getPackageName()
  {
    return this.getMeta().getPackageName();
  }

  private void inline(Scenario scenario)
  {
    // Inline app
    Path inlinedFile = boxMate.inlineApp(this.getApkFile(), scenario.getCfgFile());
    scenario.setInlinedApk(inlinedFile);
    assert scenario.getInlinedApk() != null;
  }

  void explore(ExplorationStrategy strategy)
  {

    // Initial expl
    List<Scenario> initialExpl = strategy.generateScenarios(this);
    this.addScenarios(initialExpl);

    //this.inline();

    while (this.hasPendingScenarios())
    {
      for (Scenario scenario : this.getPendingScenarios())
      {
        this.inline(scenario);
        ExplorationResult explRes;

        explRes = boxMate.explore(scenario.getInlinedApk(), scenario.getExplDepth() == 0);
        scenario.setResult(explRes);
        assert (scenario.getResult() != null) && (Files.exists(scenario.getResult().getExplDir()));
      }

      ++this.currExplDepth;
      List<Scenario> newScenarios = strategy.generateScenarios(this);
      this.addScenarios(newScenarios);
    }
  }
}
