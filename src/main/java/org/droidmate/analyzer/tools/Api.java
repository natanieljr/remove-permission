package org.droidmate.analyzer.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * API identified during exploration
 */
public class Api
{
  private static final Logger logger = LoggerFactory.getLogger(Api.class);

  private String className;
  private String methodName;
  private String paramVal;
  private int    count;

  Api(String className, String methodName, String paramVal)
  {
    this.className = className;
    this.methodName = methodName;
    this.paramVal = paramVal;

    if (this.paramVal == null)
      this.paramVal = "";

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

  public String getParamVal()
  {
    return this.paramVal;
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
    return String.format("%s\t%s\t%s", this.className, this.methodName, this.paramVal);
  }
}
