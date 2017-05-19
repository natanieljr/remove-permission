package org.droidmate.analyzer.exploration;

import org.droidmate.analyzer.api.IApi;

import java.nio.file.Path;
import java.util.List;

/**
 * Processed exploration results
 */
public interface IExplorationResult {
    Path getExplDir();

    boolean hasCrashed();

    List<IApi> getApiList();

    double getSize();

    String toBrackedNotation();

    int getNrWidgetsExplored();

    int getNrWidgetsObserved();
}
