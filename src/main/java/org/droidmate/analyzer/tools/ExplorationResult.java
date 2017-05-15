package org.droidmate.analyzer.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Processed exploration results
 */
public class ExplorationResult
{
  private static final Logger logger = LoggerFactory.getLogger(ExplorationResult.class);

  private Path      explDir;
  private boolean   crashed;
  private String    exceptionText;
  private int       nrWidgetsObs;
  private int       nrWidgetsExpl;
  private List<Api> apiList;

  ExplorationResult(Path explDir)
  {
    logger.debug(String.format("Reading exploration results in %s", explDir.toString()));
    this.apiList = new ArrayList<>();
    this.explDir = explDir;

    this.tryReadStats();
    this.readSummary();
  }

  public Path getExplDir()
  {
    return this.explDir;
  }

  public boolean isCrashed()
  {
    return this.crashed;
  }

  private Path getReportFolder()
  {
    return Paths.get(this.explDir.toString(), "report");
  }

  private void readStatsFile(Path statsFile){
    try
    {
      // First line is header
      String[] lineData = Files.readAllLines(statsFile).get(1).trim().split("\t");

      this.nrWidgetsObs = Integer.parseInt(lineData[5]);
      this.nrWidgetsExpl = Integer.parseInt(lineData[6]);

      this.crashed = !lineData[lineData.length - 1].equals("N/A (lack of DeviceException)");

      // Extract exception
      if (this.crashed)
        this.exceptionText = lineData[lineData.length - 1];
    }
    catch (IOException e)
    {
      logger.error(e.getMessage(), e);
    }
  }

  private void createErrorData(){
    this.crashed = true;
    this.nrWidgetsObs = 0;
    this.nrWidgetsExpl = 0;
    this.exceptionText = "Missing stats file";
  }

  private void tryReadStats()
  {
    Path statsFile = Paths.get(this.getReportFolder().toString(), "aggregate_stats.txt");

    if (!Files.exists(statsFile))
      this.createErrorData();
    else
      this.readStatsFile(statsFile);
  }

  private void readSummary()
  {
    Path summaryFile = Paths.get(this.getReportFolder().toString(), "summary.txt");

    if (!Files.exists(summaryFile))
      return;

    try
    {
      List<String> lines = Files.readAllLines(summaryFile);

      int l = lines.size();
      int i = 0;

      while (i < l)
      {
        String line = lines.get(i);
        // Api list
        if (line.contains("pairs count observed"))
        {
          // Jump to start of the list
          i += 3;
          line = lines.get(i).trim();

          while ((i < l) && (!line.contains("==================")) && (line.length() > 0))
          {
            line = lines.get(i).trim();

            String[] data = line.split(" ");
            String methodSignature, className, uri;

            // Has Uri, must load
            if (data[data.length - 2].contains("uri:"))
            {
              uri = data[data.length - 1];
              methodSignature = data[data.length - 3];
              className = data[data.length - 5];
            }
            else
            {
              uri = "";
              methodSignature = data[data.length - 1];
              className = data[data.length - 3];
            }

            String params = Api.getParamsFromMethodSignature(methodSignature);
            String methodName = Api.getMethodNameFromSignature(methodSignature);

            // remove : from method name
            className = className.replace(":", "");

            i += 1;
            Api api = Api.build(className, methodName, params, uri);
            logger.debug(String.format("Identified API %s", api.toString()));
            this.apiList.add(api);
          }
        }
        ++i;
      }
    }
    catch (IOException e)
    {
      logger.error(e.getMessage(), e);
    }
  }

  public List<Api> getApiList()
  {
    return this.apiList;
  }
}
