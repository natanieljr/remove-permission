package org.droidmate.analyzer.evaluation;

import org.droidmate.analyzer.exploration.IExplorationResult;

/**
 * Validation for the initial exploration, just check if it crashed or not
 */
public class InitialExplStrategy implements IEvaluationStrategy {
    InitialExplStrategy(){
        super();
    }

    @Override
    public boolean isValid(IExplorationResult result) {
        return (result != null) && !result.hasCrashed();
    }

    @Override
    public double getDissimilarity(IExplorationResult result) {
        return 0;
    }
}
