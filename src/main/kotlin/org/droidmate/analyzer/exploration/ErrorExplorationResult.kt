package org.droidmate.analyzer.exploration

import org.droidmate.analyzer.Constants
import org.droidmate.analyzer.api.IApi
import java.nio.file.Path
import java.time.Duration

/**
 * Exploration result for error state
 */
class ErrorExplorationResult : IExplorationResult{

    override val explDir: Path
        get() = Constants.EMPTY_PATH

    override fun hasCrashed(): Boolean {
        return true
    }

    override val apiList: List<IApi>
        get() = ArrayList()

    override val size: Double
        get() = 0.0

    override fun toBracedNotation(): String {
        return "{}"
    }

    override fun toSortedBracedNotation(): String {
        return "{}"
    }

    override fun getPolicies(): List<IApi> {
        return ArrayList()
    }

    override val duration: Duration
        get() = Duration.ZERO

    override val nrWidgetsExplored: Int
        get() = 0

    override val nrWidgetsObserved: Int
        get() = 0

}
