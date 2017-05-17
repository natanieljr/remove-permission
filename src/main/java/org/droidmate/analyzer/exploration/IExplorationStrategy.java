package org.droidmate.analyzer.exploration;

import org.droidmate.analyzer.AppUnderTest;

import java.util.List;

/**
 * Define how scenarios are generated and combined
 */
public interface IExplorationStrategy {
    List<Scenario> generateScenarios(AppUnderTest app);
}
