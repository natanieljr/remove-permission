package org.droidmate.analyzer.evaluation;

import org.droidmate.analyzer.exploration.IScenario;

/**
 * Evaluate the scenario on the basis that it did not crash
 */
@SuppressWarnings("unused")
public class DidNotCrash implements IScenarioEvaluationStrategy {
    @Override
    public boolean valid(IScenario scenario) {
        return !scenario.hasCrashed();
    }
}
