package org.droidmate.analyzer;

import java.util.ArrayList;
import java.util.List;

public class MonitoredApiList
{
  private ArrayList<MonitoredApi> monitoredApis;

  public MonitoredApiList()
  {
    this.monitoredApis = new ArrayList<>();
  }

  public List<MonitoredApi> getMonitoredApis()
  {
    return this.monitoredApis;
  }

  public void setMonitoredApis(ArrayList<MonitoredApi> monitoredApis)
  {
    this.monitoredApis = monitoredApis;
  }

  public void add(MonitoredApi api)
  {
    this.monitoredApis.add(api);
  }
}
