package org.droidmate.analyzer.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * API identified during exploration
 */
public class Api
{
  private static final Logger logger = LoggerFactory.getLogger(Api.class);

  private String className;
  private String methodName;
  private List<String> params;
  private String uri;
  private int    count;

  static Api build(String className, String methodName, String paramStr, String uri){
    List<String> paramList = new ArrayList<>();

    if (paramStr.contains(",")) {
      String[] paramArr = paramStr.split(",");
      paramList.addAll(Arrays.asList(paramArr));
    }
    else if (paramStr.length() > 0){
      paramList.add(paramStr);
    }

    return Api.build(className, methodName, paramList, uri);
  }

  private static Api build(String className, String methodName, List<String> params, String uri){
    return new Api(className, methodName, params, uri);
  }

  static String getParamsFromMethodSignature(String methodName){
    String pattern;
    if (methodName.contains("("))
      pattern = "\\(";
    else
      pattern = "<";
    String[] data = methodName.split(pattern);

    return data[1].replace(")", "");
  }

  static String getMethodNameFromSignature(String methodSignature){
    String params = Api.getParamsFromMethodSignature(methodSignature);

    return methodSignature
            .replace(params, "")
            .replace("(", "")
            .replace(")", "");
  }

  private Api(String className, String methodName, List<String> params, String uri)
  {
    this.className = className;
    this.methodName = methodName;
    this.params = params;
    this.uri = uri;

    if (this.params == null)
      this.params = new ArrayList<>();

    this.count = 1;
  }

  public void setCount(int count)
  {
    assert count >= 1;

    this.count = count;
  }

  public String getMethodName()
  {
    return this.methodName;
  }

  public String getClassName()
  {
    return this.className;
  }

  public List<String> getParams()
  {
    return this.params;
  }

  public String getUri(){
    return this.uri;
  }

  public int getCount()
  {
    return this.count;
  }

  public boolean isPrivacySensitive()
  {
    boolean isPrivacySensitive = new ResourceManager().isPrivacySensitive(this);
    logger.debug(String.format("(%s) => isPrivacySensitive: %s", this.toString(), isPrivacySensitive + ""));

    return isPrivacySensitive;
  }

  @Override
  public String toString()
  {
    return String.format("%s\t%s\t%s", this.className, this.methodName, this.getParamsStr());
  }

  private String getParamsStr(){
    return String.join(",", this.params);
  }

  @Override
  public boolean equals(Object other){
    if (!(other instanceof Api))
      return false;

    Api otherApi = (Api)other;
    return false;
  }
}
