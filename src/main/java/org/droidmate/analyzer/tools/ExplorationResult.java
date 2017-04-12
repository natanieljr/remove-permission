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

  public ExplorationResult(Path explDir)
  {
    logger.debug(String.format("Reading exploration results in %s", explDir.toString()));
    this.apiList = new ArrayList<>();
    this.explDir = explDir;

    this.readStats();
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

  private void readStats()
  {
    Path statsFile = Paths.get(this.getReportFolder().toString(), "aggregate_stats.txt");

    if (!Files.exists(statsFile))
    {
      this.crashed = true;
      this.nrWidgetsObs = 0;
      this.nrWidgetsExpl = 0;
      this.exceptionText = "Missing stats file";
    }
    else
    {
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
            String methodName, className, paramVal;

            if (data[data.length - 2].contains("uri:"))
            {
              paramVal = data[data.length - 1];
              methodName = data[data.length - 3];
              className = data[data.length - 5];
            }
            else
            {
              paramVal = "";
              methodName = data[data.length - 1];
              className = data[data.length - 3];
            }

            // remove : from method name
            className = className.replace(":", "");

            i += 1;
            Api api = new Api(className, methodName, paramVal);
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
