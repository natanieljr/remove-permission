package org.droidmate.analyzer

import org.droidmate.analyzer.api.IApi
import org.droidmate.analyzer.exploration.IExplorationStrategy
import org.droidmate.analyzer.exploration.IScenario

import java.nio.file.Path


/**
 * Application under evaluation
 */
interface IAppUnderTest {
    val apkFile: Path

    val dir: Path

    val initialExpl: IScenario?

    val currExplDepth: Int

    fun getScenariosDepth(depth: Int): List<IScenario>

    val packageName: String

    val initialMonitoredApiList: List<IApi>

    fun explore(strategy: IExplorationStrategy)

    val scenarios: MutableList<IScenario>

    val successfulScenarios: List<IScenario>

    val failScenarios: List<IScenario>
}
