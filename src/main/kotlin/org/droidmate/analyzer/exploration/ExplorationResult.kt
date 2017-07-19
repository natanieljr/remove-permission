package org.droidmate.analyzer.exploration

import org.droidmate.analyzer.ResourceManager
import org.droidmate.analyzer.api.Api
import org.droidmate.analyzer.api.IApi
import org.droidmate.exploration.data_aggregators.IApkExplorationOutput2
import org.droidmate.report.OutputDir
import org.droidmate.report.uniqueActionableWidgets
import org.droidmate.report.uniqueApis
import org.droidmate.report.uniqueClickedWidgets
import org.slf4j.LoggerFactory
import java.nio.file.Path
import java.time.Duration
import kotlin.collections.ArrayList

class ExplorationResult(override val explDir: Path, val report: Boolean = false) : IExplorationResult {
    var internalResult : List<IApkExplorationOutput2> = ArrayList()
    private var apiPolicies : List<IApi> = ArrayList()
    override val apiList: MutableList<IApi> = ArrayList()

    init {
        var dir : Path = explDir
        if (report) {
            dir = explDir.resolve("output_device")
            this.readApiPolicies()
        }

        internalResult = OutputDir(dir).explorationOutput2
        System.out.println(String.format("Reading exploration results in %s", explDir.toString()))
        //logger.debug(String.format("Reading exploration results in %s", explDir.toString()))

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

    internal fun readApiPolicies(){
        val apiPoliciesFile = explDir.resolve("api_policies.txt")
        this.apiPolicies = ResourceManager().loadApiMapping(apiPoliciesFile)
    }

    override val duration: Duration
        get() {
            if (internalResult.isNotEmpty())
                return internalResult[0].explorationDuration
            return Duration.ZERO
        }

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

    override fun getPolicies() : List<IApi>{
        return this.apiPolicies
    }

    override fun hasCrashed(): Boolean {
        return this.internalResult.isEmpty() || this.internalResult[0].exceptionIsPresent
    }

    override val size: Double
        get() = Math.sqrt(Math.pow(this.nrWidgetsExplored.toDouble(), 2.0) + Math.pow(this.nrWidgetsObserved.toDouble(), 2.0))

    override fun toSortedBracedNotation(): String {
        val b = StringBuilder("root{")
        this.apiList
                .sortedBy { p -> p.toString() }
                .forEach { p -> b.append(String.format("{%s}", p.toString())) }
        b.append("}")

        return b.toString()
    }

    override fun toBracedNotation(): String {
        val b = StringBuilder("root{")
        this.apiList.forEach { p -> b.append(String.format("{%s}", p.toString())) }
        b.append("}")

        return b.toString()
    }

    companion object {
        //private val logger = LoggerFactory.getLogger(ExplorationResult::class.java)
    }
}
