package org.droidmate.analyzer.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages data read from resource files
 */
class ResourceManager
{
  private static final Logger logger = LoggerFactory.getLogger(ResourceManager.class);
  private static List<Api> restrictableApis;

  private void processLine(String[] line){
    String[] classAndMethodName = line[0].split("->");
    String className = classAndMethodName[0];
    String methodName = classAndMethodName[1];
    String uri;

    // No parameter
    if (line.length == 2)
      uri = "";
    else
      uri = line[1];

    String params = Api.getParamsFromMethodSignature(methodName);

    Api api = Api.build(className, methodName, params, uri);

    ResourceManager.restrictableApis.add(api);
  }

  private void initializeApiMapping()
  {
    ResourceManager.restrictableApis = new ArrayList<>();

    ClassLoader classLoader = ResourceManager.class.getClassLoader();
    try
    {
      URL resource = classLoader.getResource("api_mapping.txt");

      List<String> mapping = new ArrayList<>();
      if (resource != null)
      {
        Path mappingFile = Paths.get(resource.toURI());
        mapping.addAll(Files.readAllLines(mappingFile));
      }

      for (String line : mapping)
      {
        String[] data = line.trim().split("\t");

        if ((data.length != 2) && (data.length != 3))
          continue;

        this.processLine(data);
      }
    } catch (Exception e)
    {
      logger.error(e.getMessage(), e);
    }

    assert ResourceManager.restrictableApis.size() > 0;
  }

  boolean isPrivacySensitive(Api api)
  {
    if (ResourceManager.restrictableApis == null)
      this.initializeApiMapping();

    return restrictableApis.contains(api);
  }

  Path getDefaultMonitoredApisFile(){
    ClassLoader classLoader = getClass().getClassLoader();
    try
    {
      URL resource = classLoader.getResource("monitored_apis.xml");

      if (resource != null)
          return Paths.get(resource.toURI());
    } catch (Exception e)
    {
      logger.error(e.getMessage(), e);
    }

    return null;
  }
}
