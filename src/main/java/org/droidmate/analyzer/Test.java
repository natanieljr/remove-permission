package org.droidmate.analyzer;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;

import java.beans.XMLEncoder;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by natan_000 on 13/04/2017.
 */
public class Test
{
  public static void main(String[] args)
  {
    MonitoredApiList apiList = new MonitoredApiList();
    List<String> data = Test.readMonitorFile();
    for (String line: data)
    {
      MonitoredApi api = new MonitoredApi();
      api.setDescriptor(line);
      api.setReturnCode("NULL");

      apiList.add(api);
    }

    for(MonitoredApi a : apiList.getMonitoredApis())
      System.out.println(a);

    try
    {
      Path output = Paths.get("C:\\Users\\natan_000\\Desktop\\Saarland\\repositories\\konrad\\droidmate-master\\dev\\droidmate\\projects\\resources\\monitored_apis.xml");
      serialize(apiList, output);
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
  }

  private static void serialize(MonitoredApiList list, Path file)
  {
    XStream xstream = new XStream();
    xstream.alias("apiList", MonitoredApiList.class);
    xstream.alias("api", MonitoredApi.class);
    xstream.addImplicitArray(MonitoredApiList.class, "monitoredApis");
    String xml = xstream.toXML(list);
    try
    {
      Files.write(file, xml.getBytes());
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
  }

  private static List<String> readMonitorFile()
  {
    Path path = Paths.get("C:\\Users\\natan_000\\Desktop\\Saarland\\repositories\\konrad\\droidmate-master\\dev\\droidmate\\projects\\resources\\monitored_apis.txt");

    try
    {
      List<String> data = Files.readAllLines(path);

      return data.stream()
        .filter(p -> (!p.startsWith("#")
          && (p.trim().length() > 0)
          && (!p.startsWith("!API23 "))))
        .collect(Collectors.toList());
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }

    return null;
  }
}
