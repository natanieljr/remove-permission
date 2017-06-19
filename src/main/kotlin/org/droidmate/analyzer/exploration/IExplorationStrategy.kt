package org.droidmate.analyzer.exploration

import org.droidmate.analyzer.IAppUnderTest

/**
 * Strategy to create scenarios during the experiment
 */
interface IExplorationStrategy {
    /**
     * Create new scenarios to be explored
     * @param app An application to be explored
     * *
     * @return List of new scenarios ot be explored. If it is not possible to generate new scenarios, returns an empty list
     */
    fun generateScenarios(app: IAppUnderTest): List<IScenario>
}
