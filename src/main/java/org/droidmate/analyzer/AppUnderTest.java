package org.droidmate.analyzer;

import net.dongliu.apk.parser.ApkFile;
import net.dongliu.apk.parser.bean.ApkMeta;
import org.apache.commons.io.FileUtils;
import org.droidmate.analyzer.api.DummyApkMeta;
import org.droidmate.analyzer.api.IApi;
import org.droidmate.analyzer.exploration.ExplorationResult;
import org.droidmate.analyzer.exploration.IExplorationStrategy;
import org.droidmate.analyzer.exploration.IScenario;
import org.droidmate.analyzer.exploration.Scenario;
import org.droidmate.analyzer.wrappers.BoxMateWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Application under evaluation
 */
public class AppUnderTest {
    private static final Logger logger = LoggerFactory.getLogger(AppUnderTest.class);

    private Configuration cfg;
    private BoxMateWrapper boxMate;
    private ApkFile apk;
    private Path apkFile;
    private List<IScenario> scenarios;
    private int currExplDepth;
    private Path dir;

    AppUnderTest(Configuration cfg, Path path) {
        this.scenarios = new ArrayList<>();
        this.currExplDepth = 0;
        this.cfg = cfg;
        this.boxMate = new BoxMateWrapper(this.cfg);
        this.apkFile = path.toAbsolutePath();

        try {
            this.apk = new ApkFile(this.apkFile.toFile());
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }

        assert this.apk != null;
        this.createExperimentDir();
    }

    private void createExperimentDir() {
        ApkMeta meta = this.getMeta();
        String dirName = String.format("%s_%s", meta.getPackageName(), meta.getVersionName());
        this.dir = Paths.get(this.cfg.dataDir.toString(), dirName);

        try {
            Files.createDirectories(this.dir);
            FileUtils.cleanDirectory(this.dir.toFile());
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }

        assert Files.exists(this.dir);
    }

    public Path getApkFile() {
        return this.apkFile;
    }

    private ApkMeta getMeta() {
        ApkMeta apkMeta = null;
        try {
            apkMeta = this.apk.getApkMeta();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }

        if (apkMeta == null)
            return new DummyApkMeta();

        return apkMeta;
    }

    public Path getDir() {
        return this.dir;
    }

    public IScenario getInitialExpl() {
        Optional<IScenario> scenarioStream = this.scenarios.stream()
                .filter(p -> p.getExplDepth() == 0)
                .findFirst();

        return scenarioStream.orElse(null);
    }

    public int getCurrExplDepth() {
        return this.currExplDepth;
    }

    public List<IScenario> getScenarios(int depth) {
        return this.scenarios.stream()
                .filter(p -> p.getExplDepth() == depth)
                .collect(Collectors.toList());
    }

    private void addScenarios(Collection<IScenario> expl) {
        assert (expl != null);

        if (expl.size() > 0)
            this.scenarios.addAll(expl);
    }

    private List<IScenario> getPendingScenarios() {
        return this.scenarios.stream()
                .filter(p -> p.getResult() == null)
                .collect(Collectors.toList());
    }

    private boolean hasPendingScenarios() {
        return this.getPendingScenarios().size() > 0;
    }

    public String getPackageName() {
        return this.getMeta().getPackageName();
    }

    private void inline(IScenario scenario) {
        // Inline app
        Path inlinedFile = boxMate.inlineApp(this.getApkFile(), scenario.getCfgFile());
        scenario.setInlinedApk(inlinedFile);
        assert scenario.getInlinedApk() != null;
    }

    private List<IApi> getInitialApiList(){
        IScenario initialExpl = this.getInitialExpl();
        if (initialExpl == null)
            return new ArrayList<>();

        return initialExpl.getExploredApiList();
    }

    public List<IApi> getInitialMonitoredApiList(){
        return this.getInitialApiList().stream()
                .filter(IApi::hasRestriction)
                .distinct()
                .collect(Collectors.toList());
    }

    void explore(IExplorationStrategy strategy) {
        // Initial expl
        List<IScenario> initialExpl = strategy.generateScenarios(this);
        this.addScenarios(initialExpl);

        while (this.hasPendingScenarios()) {
            for (IScenario scenario : this.getPendingScenarios()) {
                this.inline(scenario);
                ExplorationResult explRes;

                explRes = boxMate.explore(scenario.getInlinedApk(), scenario.getExplDepth() == 0);
                scenario.setResult(explRes);
                assert (scenario.getResult() != null) && (Files.exists(scenario.getResult().getExplDir()));
            }

            ++this.currExplDepth;
            List<IScenario> newScenarios = strategy.generateScenarios(this);
            this.addScenarios(newScenarios);
        }
    }
}
