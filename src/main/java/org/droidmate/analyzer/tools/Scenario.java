package org.droidmate.analyzer.tools;

import org.apache.commons.io.FileUtils;
import org.droidmate.analyzer.AppUnderTest;
import org.droidmate.apis.ApiPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Scenario (to be) explored
 */
public class Scenario
{
  private static final Logger logger = LoggerFactory.getLogger(Scenario.class);

  private ExplorationResult result;
  private List<Api>         restrictedApis;
  private int               explDepth;
  private Path              dir;
  private Path              cfgFile;
  private Path              inlinedApk;
  private AppUnderTest      app;
  private ApiPolicy         policy;

  private static Path createCfgFile(List<Api> restrictedApis){

    Path defaultFile = new ResourceManager().getDefaultMonitoredApisFile();

    // Initial exploration
    if (restrictedApis.size() == 0)
      return defaultFile;

    DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
    try
    {
      byte[] fileData = Files.readAllBytes(defaultFile);

      DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
      Document doc = dBuilder.parse(new ByteArrayInputStream(fileData));

      //optional, but recommended
      //read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
      doc.getDocumentElement().normalize();

      NodeList nPolicies = doc.getElementsByTagName("apiPolicy");

      for (int x = 0; x < nPolicies.getLength(); x++) {
        Node nPolicy = nPolicies.item(x);
        Node nApi = nPolicy.getChildNodes().item(1);

        Element eApi = (Element) nApi;



        //String versionRestriction = eApi.getElementsByTagName("version").item(0).getTextContent();

        // If API should be ignored on Andorid 23, skip it
        /*if ((res.androidApi == AndroidAPI.API_23) && (versionRestriction.startsWith("!API23")))
          continue

                  // Components from the API tag
          String objectClass = eApi.getElementsByTagName("class").item(0).getTextContent()
        String methodName = eApi.getElementsByTagName("method").item(0).getTextContent()
        String returnClass = eApi.getElementsByTagName("return").item(0).getTextContent()
        boolean isStatic = eApi.getElementsByTagName("static").item(0).getTextContent().equalsIgnoreCase("True")
        List<String> params = new ArrayList<>()

        Element eParams = (Element) eApi.getElementsByTagName("params").item(0)
        for (Node nParam : eParams.getElementsByTagName("param")) {
          params.add(nParam.getTextContent())
        }

        // Componenets from the Policy tag
        Element ePolicy = (Element) nPolicy;
        String policy = ePolicy.getElementsByTagName("policy").item(0).getTextContent()
        String hook = ePolicy.getElementsByTagName("hook").item(0).getTextContent()
        String name = ePolicy.getElementsByTagName("name").item(0).getTextContent()
        String logId = ePolicy.getElementsByTagName("logId").item(0).getTextContent()
        String invokeCode = ePolicy.getElementsByTagName("invoke").item(0).getTextContent()
        String defaultValue = ePolicy.getElementsByTagName("defaultValue").item(0).getTextContent()

        ApiMethodSignature api = ApiMethodSignature.fromDescriptor(objectClass, returnClass, methodName, params, isStatic,
                policy, hook, name, logId, invokeCode, defaultValue);
        apiList.add(api);*/
      }
    }
    catch (Exception e){
      logger.error(e.getMessage(), e);
    }

    return null;
  }

  public static Scenario build(AppUnderTest app, List<Api> restrictedApis, int explDepth, ApiPolicy policy){
    if (restrictedApis == null)
      restrictedApis = new ArrayList<>();

    Path cfgFile = Scenario.createCfgFile(restrictedApis);

    return new Scenario(app, restrictedApis, explDepth, cfgFile, policy);
  }

  private Scenario(AppUnderTest app, List<Api> restrictedApis, int explDepth, Path cfgFile, ApiPolicy policy)
  {
    this.app = app;
    this.restrictedApis = restrictedApis;
    this.explDepth = explDepth;
    this.policy = policy;

    this.createDir();
    this.setCfgFile(cfgFile);
  }

  private void createDir(){
    String timeStamp = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(new Date());
    this.dir = Paths.get(this.app.getDir().toString(), timeStamp);

    try{
      Files.createDirectories(dir);
    }
    catch(IOException e){
      logger.error(e.getMessage(), e);
    }

    assert Files.exists(this.dir);
  }

  private void setCfgFile(Path cfgFile){
    String fileName = cfgFile.getFileName().toString();
    this.cfgFile = Paths.get(this.getDir().toString(), fileName);

    try{
      Files.copy(cfgFile, this.cfgFile, StandardCopyOption.REPLACE_EXISTING);
    }
    catch(IOException e){
      logger.error(e.getMessage(), e);
    }

    assert Files.exists(this.cfgFile);
  }

  public Path getDir(){
    return this.dir;
  }

  public ExplorationResult getResult()
  {
    return this.result;
  }

  List<Api> getRestrictedApis()
  {
    return this.restrictedApis;
  }

  private Path copyExplOutputToDir(ExplorationResult res)
  {
    Path src = res.getExplDir();
    Path dst = Paths.get(this.getDir().toString(), "output_device1");

    try
    {
      if (Files.exists(dst))
        FileUtils.cleanDirectory(dst.toFile());
      Files.deleteIfExists(dst);

      FileUtils.copyDirectory(src.toFile() , dst.toFile());

      FileUtils.cleanDirectory(src.toFile());
      Files.deleteIfExists(src);
    }
    catch (IOException e)
    {
      logger.error(e.getMessage(), e);
    }

    assert Files.exists(dst);
    return dst;
  }

  public void setResult(ExplorationResult result)
  {
    Path newResDir = this.copyExplOutputToDir(result);
    this.result = new ExplorationResult(newResDir);
  }

  public ApiPolicy getPolicy(){
    return this.policy;
  }

  public int getExplDepth()
  {
    return this.explDepth;
  }

  public Path getCfgFile(){
    return this.cfgFile;
  }

  public Path getInlinedApk() {
    return this.inlinedApk;
  }

  public void setInlinedApk(Path inlinedApk) {
    String fileName = inlinedApk.getFileName().toString();
    this.inlinedApk = Paths.get(this.getDir().toString(), fileName);

    try{
      Files.copy(inlinedApk, this.inlinedApk, StandardCopyOption.REPLACE_EXISTING);
    }
    catch(IOException e){
      logger.error(e.getMessage(), e);
    }

    assert Files.exists(this.inlinedApk);
  }
}
