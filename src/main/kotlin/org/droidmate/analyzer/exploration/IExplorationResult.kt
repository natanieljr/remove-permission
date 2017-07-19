package org.droidmate.analyzer.exploration

import org.droidmate.analyzer.api.IApi

import java.nio.file.Path
import java.time.Duration

/**
 * Processed exploration results
 */
interface IExplorationResult {
    val explDir: Path

    fun hasCrashed(): Boolean

    val apiList: List<IApi>

    val size: Double

    fun toSortedBracedNotation(): String

    fun toBracedNotation(): String

    fun getPolicies() : List<IApi>

    val duration: Duration

    val nrWidgetsExplored: Int

    val nrWidgetsObserved: Int
}
