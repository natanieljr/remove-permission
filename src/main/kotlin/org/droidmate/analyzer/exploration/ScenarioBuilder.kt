package org.droidmate.analyzer.exploration

import org.droidmate.analyzer.IAppUnderTest
import org.droidmate.analyzer.api.IApi
import org.droidmate.analyzer.evaluation.IEvaluationStrategy
import org.droidmate.apis.ApiPolicy
import java.util.*

/**
 * Builder pattern to create IScenario objects
 */
class ScenarioBuilder {

    fun build(app: IAppUnderTest, restrictedApis: List<IApi>, explDepth: Int, policy: ApiPolicy,
                       evaluator: IEvaluationStrategy): IScenario {
        return Scenario(app, restrictedApis, explDepth, policy, evaluator)
    }

    fun build(s1: IScenario, s2: IScenario, app: IAppUnderTest, explDepth: Int, policy: ApiPolicy,
                       evaluator: IEvaluationStrategy): IScenario {
        val restrictedApis = ArrayList<IApi>()
        val scenario1 = s1 as Scenario
        restrictedApis.addAll(scenario1.restrictedApiList)

        (s2 as Scenario).restrictedApiList
                .filterNot { restrictedApis.contains(it) }
                .forEach { restrictedApis.add(it) }

        restrictedApis.sortBy { p -> p.toString() }

        return this.build(app, restrictedApis, explDepth, policy, evaluator)
    }

}
