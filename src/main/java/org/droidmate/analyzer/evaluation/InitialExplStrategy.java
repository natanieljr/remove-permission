package org.droidmate.analyzer.evaluation;

import org.droidmate.analyzer.exploration.IExplorationResult;

/**
 * Validation for the initial exploration, just check if it crashed or not
 */
public class InitialExplStrategy implements IEvaluationStrategy {
    @Override
    public boolean valid(IExplorationResult result) {
        return (result != null) && !result.hasCrashed();
    }
}
