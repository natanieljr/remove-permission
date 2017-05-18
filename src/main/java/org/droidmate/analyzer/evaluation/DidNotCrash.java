package org.droidmate.analyzer.evaluation;

import org.droidmate.analyzer.exploration.IExplorationResult;
import org.droidmate.analyzer.exploration.IScenario;

/**
 * Evaluate the scenario on the basis that it did not crash
 */
@SuppressWarnings("unused")
public class DidNotCrash implements IEvaluationStrategy {
    @Override
    public boolean valid(IExplorationResult result) {
        return !result.hasCrashed();
    }
}
