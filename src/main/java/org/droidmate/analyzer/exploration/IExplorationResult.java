package org.droidmate.analyzer.exploration;

import org.droidmate.analyzer.api.IApi;

import java.nio.file.Path;
import java.util.List;

/**
 * Created by nataniel on 18.05.17.
 */
public interface IExplorationResult {
    Path getExplDir();

    boolean hasCrashed();

    List<IApi> getApiList();

    double getSize();

    String toBrackedNotation();
}
