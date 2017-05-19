package org.droidmate.analyzer.evaluation;

import org.droidmate.analyzer.exploration.IExplorationResult;

/**
 * Evaluate the scenario on the basis that it did not crash
 */
@SuppressWarnings("unused")
public class DidNotCrash implements IEvaluationStrategy {
    DidNotCrash(){
        super();
    }

    @Override
    public boolean isValid(IExplorationResult result) {
        return !result.hasCrashed();
    }

    @Override
    public double getDissimilarity(IExplorationResult result) {
        if (this.isValid(result))
            return 0;
        else
            return 1;
    }
}
