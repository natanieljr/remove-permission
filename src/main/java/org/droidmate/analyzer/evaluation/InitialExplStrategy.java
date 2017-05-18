package org.droidmate.analyzer.evaluation;

import org.droidmate.analyzer.exploration.IScenario;

/**
 * Validation for the initial exploration, just check if it crashed or not
 */
public class InitialExplStrategy implements IScenarioEvaluationStrategy {
    @Override
    public boolean valid(IScenario scenario) {
        return (scenario.getResult() != null) && !scenario.getResult().hasCrashed();
    }
}
