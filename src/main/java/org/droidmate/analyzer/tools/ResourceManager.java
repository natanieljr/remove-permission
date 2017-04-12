package org.droidmate.analyzer.tools;

import org.droidmate.analyzer.AppUnderTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Manages data read from resource files
 */
public class ResourceManager
{
  private static final Logger logger = LoggerFactory.getLogger(ResourceManager.class);
  private static HashMap<String, List<String>> apiMapping;

  private void initializeApiMapping()
  {
    apiMapping = new HashMap<>();
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

        String[] classAndMethodName = data[0].split("->");
        String className = classAndMethodName[0];
        String methodName = classAndMethodName[1];
        String paramVal;

        // No parameter
        if (data.length == 2)
          paramVal = "";
        else
          paramVal = data[1];

        Api api = new Api(className, methodName, paramVal);

        List<String> restrictions;
        if (apiMapping.containsKey(api.toString()))
          restrictions = apiMapping.get(api.toString());
        else
          restrictions = new ArrayList<>();

        restrictions.add(data[data.length - 1]);
        apiMapping.put(api.toString(), restrictions);
      }
    } catch (Exception e)
    {
      logger.error(e.getMessage(), e);
    }
  }

  public List<String> getRestrictions(Api api)
  {
    return apiMapping.get(api.toString());
  }

  public boolean isPrivacySensitive(Api api)
  {
    if (apiMapping== null)
      this.initializeApiMapping();

    return apiMapping.containsKey(api.toString());
  }

  private List<String> getTemplate()
  {
    ClassLoader classLoader = getClass().getClassLoader();
    try
    {
      URL resource = classLoader.getResource("xprivacy_config_template.xml");

      if (resource != null)
      {
        Path templateFile = Paths.get(resource.toURI());
        return Files.readAllLines(templateFile);
      }
    } catch (Exception e)
    {
      logger.error(e.getMessage(), e);
    }

    return null;
  }

  public List<String> getFormattedTemplate(AppUnderTest app)
  {
    List<String> data = this.getTemplate();

    String apkId = Integer.toString(app.getXPrivacyId());
    String packageName = app.getPackageName();

    if (data != null)
      data.replaceAll(s -> s.replace("**ID**", apkId).replace("**PACKAGE**", packageName));

    return data;
  }
}
