package org.droidmate.analyzer

import net.dongliu.apk.parser.ApkFile
import net.dongliu.apk.parser.bean.ApkMeta
import org.apache.commons.io.FileUtils
import org.droidmate.analyzer.api.DummyApkMeta
import org.droidmate.analyzer.api.IApi
import org.droidmate.analyzer.exploration.IExplorationStrategy
import org.droidmate.analyzer.exploration.IScenario
import org.droidmate.analyzer.wrappers.BoxMateWrapper
import org.slf4j.LoggerFactory
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.util.*

class AppUnderTest internal constructor(private val cfg: Configuration, path: Path) : IAppUnderTest {
    private val boxMate: BoxMateWrapper
    private var apk: ApkFile? = null
    override val apkFile: Path
    override val scenarios: MutableList<IScenario> = ArrayList()
    override var currExplDepth: Int = 0
        private set
    override var dir: Path = this.createExperimentDir()
        private set

    init {
        this.currExplDepth = 0
        this.boxMate = BoxMateWrapper(this.cfg)
        this.apkFile = path.toAbsolutePath()

        try {
            this.apk = ApkFile(this.apkFile.toFile())
        } catch (e: IOException) {
            logger.error(e.message, e)
        }

        assert(this.apk != null)
        this.createExperimentDir()
    }

    private fun createExperimentDir(): Path {
        val meta = this.meta
        val dirName = String.format("%s_%s", meta.packageName, meta.versionName)
        val newDir = this.cfg.dataDir.resolve(dirName)

        try {
            Files.createDirectories(this.dir)
            FileUtils.cleanDirectory(this.dir.toFile())
        } catch (e: IOException) {
            logger.error(e.message, e)
        }

        assert(Files.exists(this.dir))

        return newDir
    }

    private val meta: ApkMeta
        get() {
            var apkMeta: ApkMeta? = null
            try {
                apkMeta = this.apk!!.apkMeta
            } catch (e: IOException) {
                logger.error(e.message, e)
            }

            if (apkMeta == null)
                return DummyApkMeta()

            return apkMeta
        }

    override val initialExpl: IScenario
        get() {
            val scenarioStream = this.scenarios.stream()
                    .filter { p -> p.explDepth == 0 }
                    .findFirst()

            return scenarioStream.orElse(null)
        }

    override fun getScenariosDepth(depth: Int): List<IScenario> {
        return this.scenarios
                .filter { p -> p.explDepth == depth }
                .toList()
    }

    private fun addScenarios(expl: Collection<IScenario>) {
        if (expl.isNotEmpty())
            this.scenarios.addAll(expl)
    }

    private val pendingScenarios: List<IScenario>
        get() = this.scenarios
                .filter { p -> p.result == null }
                .toList()

    private fun hasPendingScenarios(): Boolean {
        return this.pendingScenarios.isNotEmpty()
    }

    override val packageName: String
        get() = this.meta.packageName

    private fun inline(scenario: IScenario) {
        // Inline app
        scenario.inlinedApk = boxMate.inlineApp(this.apkFile)
    }

    private val initialApiList: List<IApi>
        get() {
            return this.initialExpl.exploredApiList
        }

    override val initialMonitoredApiList: List<IApi>
        get() = this.initialApiList
                .filter { p -> p.hasRestriction() }
                .distinct()
                .toList()

    override fun explore(strategy: IExplorationStrategy) {
        // Initial expl
        val initialExpl = strategy.generateScenarios(this)
        this.addScenarios(initialExpl)

        while (this.hasPendingScenarios()) {
            for (scenario in this.pendingScenarios) {
                this.inline(scenario)
                scenario.result = boxMate.explore(scenario.inlinedApk, scenario.cfgFile, scenario.explDepth == 0)
                assert(scenario.result != null && Files.exists(scenario.result!!.explDir))
            }

            ++this.currExplDepth
            val newScenarios = strategy.generateScenarios(this)
            this.addScenarios(newScenarios)
        }
    }

    override fun toString(): String {
        return this.apkFile.fileName.toString()
    }

    override val successfulScenarios: List<IScenario>
        get() = this.scenarios
                .filter { p -> p.isValid }
                .toList()

    override val failScenarios: List<IScenario>
        get() = this.scenarios
                .filterNot { p -> p.isValid }
                .toList()

    companion object {
        private val logger = LoggerFactory.getLogger(AppUnderTest::class.java)
    }
}
