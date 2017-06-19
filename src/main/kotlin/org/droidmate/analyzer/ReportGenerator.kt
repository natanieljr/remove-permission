package org.droidmate.analyzer

import org.apache.commons.lang3.tuple.ImmutableTriple
import org.apache.commons.lang3.tuple.Triple
import org.droidmate.analyzer.exploration.IScenario
import org.slf4j.LoggerFactory
import java.io.IOException
import java.nio.file.Files
import java.util.*

/**
 * Produce application report
 */
internal class ReportGenerator {

    private val experiments: MutableList<ImmutableTriple<IAppUnderTest, Date, Date>>

    init {
        this.experiments = ArrayList<ImmutableTriple<IAppUnderTest, Date, Date>>()
    }

    fun addApp(app: IAppUnderTest, startTime: Date, endTime: Date) {
        val triple = ImmutableTriple(app, startTime, endTime)
        this.experiments.add(triple)
    }

    private fun createReportHeader(experiment: Triple<IAppUnderTest, Date, Date>): String {
        val app = experiment.left
        val start = experiment.middle
        val end = experiment.right

        val scenarios = app.scenarios
        val nrScenarios = scenarios.size
        val nrScenariosSuccess = app.successfulScenarios.size
        val nrScenariosFail = app.failScenarios.size
        val nrApis = app.initialMonitoredApiList.size

        val headerFormat = "APP:\t\t%s\n" +
                "START:\t\t%s\n" +
                "END:\t\t%s\n" +
                "APIS:\t\t%d\n" +
                "SCENARIOS:\t%d\n" +
                "  SUCCESS:\t%d\n" +
                "  FAIL:\t\t%d\n\n\n"

        return String.format(headerFormat,
                app.toString(),
                ReportFormatter.formatDate(start),
                ReportFormatter.formatDate(end),
                nrApis,
                nrScenarios,
                nrScenariosSuccess,
                nrScenariosFail)
    }

    private fun createScenarios(experiment: Triple<IAppUnderTest, Date, Date>): String {
        val app = experiment.left
        val scenarios = app.scenarios

        val scenarioFormat = "SCENARIO:\t\t\t%s\n" +
                "VALID:\t\t\t\t%s\n" +
                "DISSIMILARITY:\t\t%.6f\n" +
                "WIDGETS:\n" +
                "  OBSERVED:\t\t\t%d\n" +
                "  EXPLORED:\t\t\t%d\n" +
                "RESTRICTED APIS:\t%d\n" +
                "%s" +
                "EXPLORED APIS:\n" +
                "%s\n"

        val b = StringBuilder()

        scenarios.forEach { p ->
            val restricted = p.restrictedApiList
            val restrictedStr = ReportFormatter.formatApiList(restricted)
            val explored = p.exploredApiList
            val exploredStr = ReportFormatter.formatApiList(explored)

            val result = p.result!!
            val nrWidgetsExpl = result.nrWidgetsExplored
            val nrWidgetsObs = result.nrWidgetsObserved
            val dirName = result.explDir.parent.fileName.toString()

            b.append(String.format(scenarioFormat,
                    dirName,
                    p.isValid.toString() + "",
                    p.dissimilarity,
                    nrWidgetsObs,
                    nrWidgetsExpl,
                    restricted.size,
                    restrictedStr,
                    exploredStr
            ))
        }

        return b.toString()
    }

    private fun createSummary(experiment: Triple<IAppUnderTest, Date, Date>): String {
        val app = experiment.left

        var firstError: IScenario? = null
        var lastSuccess: IScenario? = null

        for (scenario in app.scenarios) {
            // updated first error
            if (firstError == null && !scenario.isValid)
                firstError = scenario

            // update last success
            if (scenario.isValid) {
                if (lastSuccess != null) {
                    val maxBlockedApis = lastSuccess.restrictedApiList.size
                    val currBlockedApis = scenario.restrictedApiList.size

                    if (currBlockedApis > maxBlockedApis)
                        lastSuccess = scenario
                } else {
                    lastSuccess = scenario
                }
            }
        }

        val nrApis = app.initialMonitoredApiList.size.toDouble()
        var nrApisBeforreError = nrApis.toInt()
        var nrApisBlocked = 0
        var firstErrorStr = "NO ERROR"
        var lastSuccessStr = "NO SUCCESS"

        if (firstError != null) {
            nrApisBeforreError = Math.max(firstError.restrictedApiList.size - 1, 0)
            firstErrorStr = firstError.result!!.explDir.parent.fileName.toString()
        }

        if (lastSuccess != null) {
            nrApisBlocked = lastSuccess.restrictedApiList.size
            lastSuccessStr = lastSuccess.result!!.explDir.parent.fileName.toString()
        }

        val summaryFormat = //"VALID SCENARIOS (%):\t%.2f\n" +
                "FIRST ERROR (scenario):\t%s\n" +
                        "LAST SUCCESS (scenario):\t%s\n" +
                        "MAX APIS BEFORE ERROR:\t%d\n" +
                        "MAX APIS BLOCKED:\t%d\n" +
                        "MAX APIS BEFORE ERROR (PERC):\t%.2f\n" +
                        "MAX APIS BLOCKED (PERC):\t%.2f\n"


        return String.format(summaryFormat,
                firstErrorStr,
                lastSuccessStr,
                nrApisBeforreError,
                nrApisBlocked,
                nrApisBeforreError / nrApis * 100,
                nrApisBlocked / nrApis * 100
        )
    }

    private fun createReport(experiment: Triple<IAppUnderTest, Date, Date>) {
        val header = this.createReportHeader(experiment)
        val scenarios = this.createScenarios(experiment)
        val summary = this.createSummary(experiment)

        val fileData = String.format("%s\n%s\n%s", header, scenarios, summary)
        val app = experiment.left
        val reportFile = app.dir.resolve("report.txt")
        try {
            Files.write(reportFile, fileData.toByteArray())
        } catch (e: IOException) {
            logger.error(e.message, e)
        }

        assert(Files.exists(reportFile))
    }

    fun generateReport() {
        this.experiments.forEach { p -> this.createReport(p) }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ReportGenerator::class.java)
    }
}
