package org.droidmate.analyzer.exploration

import org.droidmate.analyzer.IAppUnderTest
import org.droidmate.analyzer.evaluation.EvaluationStrategyBuilder
import org.droidmate.apis.ApiPolicy
import org.slf4j.LoggerFactory

/**
 * Exploration strategy which generates all scenarios at once for exploration
 */
class GenerateAllExplorationStrategy(policy: ApiPolicy,
                                     evaluatorBuilder: EvaluationStrategyBuilder,
                                     scenarioBuilder: ScenarioBuilder) : DefaultExplorationStrategy(policy,
        evaluatorBuilder, scenarioBuilder) {

    private fun generateSimpleScenarios(app: IAppUnderTest): List<IScenario> {
        logger.info("Generating simple scenarios")

        // filter privacy sensitive APIs (unique)
        val apiList = app.initialMonitoredApiList

        return this.generateSimpleScenarios(app, apiList)
    }

    private fun generateCompositeScenarios(app: IAppUnderTest, simpleScenarios: List<IScenario>): List<IScenario> {
        logger.info("Generating composite scenarios")

        val scenarios : MutableList<IScenario> = ArrayList()
        var currDepth = app.currExplDepth + 1

        var previousScenarios = simpleScenarios;
        while (currDepth <= app.initialMonitoredApiList.size) {
            val newScenarios = this.generateCompositeScenarios(app, previousScenarios, currDepth++)
            scenarios.addAll(newScenarios)

            previousScenarios = newScenarios
        }

        return scenarios
    }

    private fun generateAllScenarios(app: IAppUnderTest) : List<IScenario>{
        val scenarios : MutableList<IScenario> = ArrayList()
        val simpleScenarios = this.generateSimpleScenarios(app)
        val compositeScenarios = this.generateCompositeScenarios(app, simpleScenarios)
        scenarios.addAll(simpleScenarios)
        scenarios.addAll(compositeScenarios)

        return scenarios
    }

    override fun generateScenarios(app: IAppUnderTest): List<IScenario> {
        val currDepth = app.currExplDepth

        // Initial exploration
        if (currDepth == 0)
            return this.generateInitialExpl(app)
        else if (currDepth == 1) {
            logger.info(String.format("Generating scenarios of depth %d", currDepth))
            return this.generateAllScenarios(app)
        }

        return ArrayList()
    }

    companion object {
        private val logger = LoggerFactory.getLogger(GenerateAllExplorationStrategy::class.java)
    }
}
