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
    private val internalResult = OutputDir(explDir).explorationOutput2[0]

    override val nrWidgetsObserved = internalResult.uniqueActionableWidgets.size
    override val nrWidgetsExplored = internalResult.uniqueClickedWidgets.size
    override val apiList: MutableList<IApi> = ArrayList()

    init {
        logger.debug(String.format("Reading exploration results in %s", explDir.toString()))

        internalResult.uniqueApis
                .forEach { p ->
                    var uri = ""
                    if (p.uniqueString.contains("uri:"))
                        uri = p.uniqueString.split("uri: ").last()
                    this.apiList.add(Api.build(p.objectClass, p.methodName,
                            p.paramTypes, uri))
                }
    }

    override fun hasCrashed(): Boolean {
        return this.internalResult.exceptionIsPresent
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
