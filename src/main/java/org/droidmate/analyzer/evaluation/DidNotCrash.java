package org.droidmate.analyzer.evaluation;

import org.droidmate.analyzer.exploration.Scenario;

/**
 * Evaluate the scenario on the basis that it did not crash
 */
public class DidNotCrash implements IScenarioEvaluationStrategy {
    @Override
    public boolean valid(Scenario scenario) {
        return scenario.hasCrashed();
    }
}
