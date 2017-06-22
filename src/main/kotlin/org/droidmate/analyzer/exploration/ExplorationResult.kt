package org.droidmate.analyzer.exploration

import org.droidmate.analyzer.api.Api
import org.droidmate.analyzer.api.IApi
import org.droidmate.report.OutputDir
import org.droidmate.report.uniqueActionableWidgets
import org.droidmate.report.uniqueApis
import org.droidmate.report.uniqueClickedWidgets
import org.slf4j.LoggerFactory
import java.nio.file.Path
import java.util.*

class ExplorationResult(override val explDir: Path) : IExplorationResult {
    //private val internalResult = OutputDir(explDir.resolve("output_device")).explorationOutput2[0]
    private val internalResult = OutputDir(explDir).explorationOutput2

    override val nrWidgetsObserved : Int
            get() {
                if (internalResult.isNotEmpty())
                    return internalResult[0].uniqueActionableWidgets.size
                return 0
            }

    override val nrWidgetsExplored : Int
        get() {
            if (internalResult.isNotEmpty())
                return internalResult[0].uniqueClickedWidgets.size
            return 0
        }
    override val apiList: MutableList<IApi> = ArrayList()

    init {
        logger.debug(String.format("Reading exploration results in %s", explDir.toString()))

        if (internalResult.isNotEmpty())
            internalResult[0].uniqueApis
                    .forEach { p ->
                        var uri = ""
                        if (p.uniqueString.contains("uri:"))
                            uri = p.uniqueString.split("uri: ").last()
                        this.apiList.add(Api.build(p.objectClass, p.methodName,
                                p.paramTypes, uri))
                    }
    }

    override fun hasCrashed(): Boolean {
        return this.internalResult.isEmpty() || this.internalResult[0].exceptionIsPresent
    }

    override val size: Double
        get() = Math.sqrt(Math.pow(this.nrWidgetsExplored.toDouble(), 2.0) + Math.pow(this.nrWidgetsObserved.toDouble(), 2.0))

    override fun toBracedNotation(): String {
        val b = StringBuilder("root{")
        this.apiList.forEach { p -> b.append(String.format("{%s}", p.toString())) }
        b.append("}")

        return b.toString()
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ExplorationResult::class.java)
    }
}
