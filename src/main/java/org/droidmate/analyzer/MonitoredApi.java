package org.droidmate.analyzer;

public class MonitoredApi
{
  private String descriptor;
  private String returnCode;

  public MonitoredApi() { }

  public String getDescriptor()
  {
    return this.descriptor;
  }

  public String getReturnCode()
  {
    return this.returnCode;
  }

  public void setDescriptor(String val)
  {
    this.descriptor = val;
  }

  public void setReturnCode(String val)
  {
    this.returnCode = val;
  }

  @Override
  public String toString()
  {
    return this.descriptor + " => " + this.returnCode;
  }
}
