package org.droidmate.analyzer.exploration

import org.droidmate.analyzer.api.IApi

import java.nio.file.Path

/**
 * Processed exploration results
 */
interface IExplorationResult {
    val explDir: Path

    fun hasCrashed(): Boolean

    val apiList: List<IApi>

    val size: Double

    fun toBracedNotation(): String

    val nrWidgetsExplored: Int

    val nrWidgetsObserved: Int
}
