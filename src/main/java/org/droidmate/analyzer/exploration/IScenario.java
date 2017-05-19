package org.droidmate.analyzer.exploration;

import org.droidmate.analyzer.api.IApi;

import java.nio.file.Path;
import java.util.List;

/**
 * Scenario (to be) executed during the experiment
 */
public interface IScenario {
    IExplorationResult getResult();

    void initialize();

    void setResult(IExplorationResult result);

    int getExplDepth();

    Path getCfgFile();

    Path getInlinedApk();

    void setInlinedApk(Path inlinedApk);

    List<IApi> getExploredApiList();

    List<IApi> getRestrictedApiList();

    boolean isValid();

    double getDissimilarity();
}
