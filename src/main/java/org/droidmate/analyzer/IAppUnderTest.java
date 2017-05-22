package org.droidmate.analyzer;

import org.droidmate.analyzer.api.IApi;
import org.droidmate.analyzer.exploration.IExplorationStrategy;
import org.droidmate.analyzer.exploration.IScenario;

import java.nio.file.Path;
import java.util.List;


/**
 * Application under evaluation
 */
public interface IAppUnderTest {
    Path getApkFile();

    Path getDir();

    IScenario getInitialExpl();

    int getCurrExplDepth();

    List<IScenario> getScenarios(int depth);

    String getPackageName();

    List<IApi> getInitialMonitoredApiList();

    void explore(IExplorationStrategy strategy);

    List<IScenario> getScenarios();

    List<IScenario> getSuccessfulScenarios();

    List<IScenario> getFailScenarios();
}
