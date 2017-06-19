package org.droidmate.analyzer.exploration

import org.droidmate.analyzer.api.IApi

import java.nio.file.Path

/**
 * Scenario (to be) executed during the experiment
 */
interface IScenario {
    var result: IExplorationResult?

    fun initialize()

    val explDepth: Int

    val cfgFile: Path

    var inlinedApk: Path

    val exploredApiList: List<IApi>

    val restrictedApiList: List<IApi>

    val isValid: Boolean

    val dissimilarity: Double
}
