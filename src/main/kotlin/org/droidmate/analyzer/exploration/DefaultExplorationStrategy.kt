package org.droidmate.analyzer.exploration

import org.droidmate.analyzer.IAppUnderTest
import org.droidmate.analyzer.evaluation.EvaluationStrategyBuilder
import org.droidmate.analyzer.evaluation.IEvaluationStrategy
import org.droidmate.apis.ApiPolicy
import org.droidmate.analyzer.api.IApi
import org.slf4j.LoggerFactory
import java.util.*

open class DefaultExplorationStrategy(protected val policy: ApiPolicy, protected val evaluatorBuilder: EvaluationStrategyBuilder,
                                      protected val scenarioBuilder: ScenarioBuilder) : IExplorationStrategy {

    protected fun getValidScenarios(app: IAppUnderTest): List<IScenario> {
        return app
                .getScenariosDepth(app.currExplDepth - 1)
                .filter { p -> p.isValid }
                .toList()
    }

    protected fun getEvaluationStrategy(app: IAppUnderTest): IEvaluationStrategy {
        val initialExpl = app.initialExpl
        return this.evaluatorBuilder.build(initialExpl)
    }

    protected fun generateSimpleScenarios(app: IAppUnderTest, apiList: List<IApi>): List<IScenario> {
        val scenarios: MutableList<IScenario> = ArrayList()

        apiList.forEach { api ->
            val scenario = this.scenarioBuilder.build(app, listOf(api), app.currExplDepth,
                    this.policy, this.getEvaluationStrategy(app))

            // Somehow, the distinct operation of the stream class does not work and
            // the java.net.URL->openConnection() appears multiple times
            if (!scenarios.contains(scenario)) {
                scenario.initialize()
                scenarios.add(scenario)
            }
        }

        return scenarios
    }

    private fun generateSimpleScenarios(app: IAppUnderTest): List<IScenario> {
        logger.info("Generating simple scenarios")

        val scenarios = ArrayList<IScenario>()

        // Don't continue the experiment if the initial exploration is not valid
        if (app.initialExpl!!.isValid) {

            // filter privacy sensitive APIs (unique)
            val apiList = app.initialMonitoredApiList

            return this.generateSimpleScenarios(app, apiList)
        }

        return scenarios
    }

    protected fun generateCompositeScenarios(app: IAppUnderTest, previousScenarios: List<IScenario>, explDepth: Int): List<IScenario> {
        val scenarios = ArrayList<IScenario>()

        for (s1 in previousScenarios)
            for (s2 in previousScenarios)
                if (s1 != s2) {
                    val newScenario = this.scenarioBuilder.build(s1, s2, app, explDepth, this.policy,
                            this.getEvaluationStrategy(app))

                    if (!scenarios.contains(newScenario)) {
                        newScenario.initialize()
                        scenarios.add(newScenario)
                    }
                }
        return scenarios
    }

    private fun generateCompositeScenarios(app: IAppUnderTest): List<IScenario> {
        logger.info("Generating composite scenarios")

        val previousScenarios = this.getValidScenarios(app)

        return this.generateCompositeScenarios(app, previousScenarios, app.currExplDepth)
    }

    protected fun generateInitialExpl(app: IAppUnderTest): List<IScenario> {
        val s = this.scenarioBuilder.build(app, ArrayList(), app.currExplDepth, this.policy,
                this.getEvaluationStrategy(app))
        s.initialize()
        return listOf(s)
    }

    override fun generateScenarios(app: IAppUnderTest): List<IScenario> {
        val currDepth = app.currExplDepth
        logger.info(String.format("Generating scenarios of depth %d", currDepth))

        // Initial exploration
        if (currDepth == 0)
            return this.generateInitialExpl(app)
        else if (currDepth == 1)
            return this.generateSimpleScenarios(app)
        else
            return this.generateCompositeScenarios(app)// First generation is simple scenarios
    }

    companion object {
        private val logger = LoggerFactory.getLogger(DefaultExplorationStrategy::class.java)
    }
}
