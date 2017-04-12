package org.droidmate.analyzer.tools;

import java.nio.file.Path;

/**
 * Scenario (to be) explored
 */
public class Scenario
{
  private ExplorationResult result;
  private Api               restrictedApi;
  private int               explDepth;
  private Path              cfgFile;

  public Scenario(Api restrictedApi, int explDepth)
  {
    this.restrictedApi = restrictedApi;
    this.explDepth = explDepth;
  }

  public ExplorationResult getResult()
  {
    return this.result;
  }

  Api getRestrictedApi()
  {
    return this.restrictedApi;
  }

  public void setResult(ExplorationResult result)
  {
    this.result = result;
  }

  public int getExplDepth()
  {
    return this.explDepth;
  }

  public void setCfgFile(Path cfgFile)
  {
    this.cfgFile = cfgFile;
  }

  Path getCfgFile()
  {
    return this.cfgFile;
  }
}
