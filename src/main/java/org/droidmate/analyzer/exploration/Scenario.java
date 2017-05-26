package org.droidmate.analyzer.exploration;

import org.apache.commons.io.FileUtils;
import org.droidmate.analyzer.IAppUnderTest;
import org.droidmate.analyzer.api.IApi;
import org.droidmate.analyzer.evaluation.IEvaluationStrategy;
import org.droidmate.analyzer.wrappers.BoxMateConsts;
import org.droidmate.apis.ApiPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

/**
 * Scenario (to be) explored
 */
public class Scenario implements IScenario {
    private static final Logger logger = LoggerFactory.getLogger(Scenario.class);

    private IExplorationResult result;
    private List<IApi> restrictedApis;
    private int explDepth;
    private Path dir;
    private Path cfgFile;
    private Path inlinedApk;
    private IAppUnderTest app;
    private ApiPolicy policy;
    private IEvaluationStrategy evaluator;

    Scenario(IAppUnderTest app, List<IApi> restrictedApis, int explDepth, ApiPolicy policy,
                     IEvaluationStrategy evaluator) {
        this.app = app;
        this.restrictedApis = restrictedApis;
        this.explDepth = explDepth;
        this.policy = policy;
        this.evaluator = evaluator;
    }

    private void createDir() {
        try {
            String prefix = String.format("%d_", this.explDepth);

            this.dir = Files.createTempDirectory(this.app.getDir(), prefix);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }

        assert this.dir != null;
        assert Files.exists(this.dir);
    }

    private Path getDir() {
        return this.dir;
    }

    private Path createPoliciesFile(List<IApi> restrictedApis){
        StringBuilder data = new StringBuilder();

        restrictedApis.forEach(p ->
                data.append(String.format("%s\t%s\n", p.toString(), this.policy.toString())));

        Path res = this.getDir().resolve(BoxMateConsts.FILE_API_POLICIES);
        try{
            Files.write(res, data.toString().getBytes());
        }
        catch (IOException e){
            logger.error(e.getMessage(), e);
        }

        assert Files.exists(res);

        return res;
    }

    @Override
    public void initialize(){
        this.createDir();
        Path cfgFile = this.createPoliciesFile(this.restrictedApis);
        this.setCfgFile(cfgFile);
    }

    @Override
    public IExplorationResult getResult() {
        return this.result;
    }

    @Override
    public void setResult(IExplorationResult result) {
        Path newResDir = this.copyExplOutputToDir(result);
        this.result = new ExplorationResult(newResDir);
    }

    private Path copyExplOutputToDir(IExplorationResult res) {
        Path src = res.getExplDir();
        Path dst = this.getDir().resolve("output_device1");

        try {
            if (Files.exists(dst))
                FileUtils.cleanDirectory(dst.toFile());
            Files.deleteIfExists(dst);

            FileUtils.copyDirectory(src.toFile(), dst.toFile());

            FileUtils.cleanDirectory(src.toFile());
            Files.deleteIfExists(src);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }

        assert Files.exists(dst);
        return dst;
    }

    @Override
    public int getExplDepth() {
        return this.explDepth;
    }

    @Override
    public Path getCfgFile() {
        return this.cfgFile;
    }

    private void setCfgFile(Path cfgFile) {
        if (cfgFile.getParent().equals(this.getDir()))
            this.cfgFile = cfgFile;
        else {

            String fileName = cfgFile.getFileName().toString();
            this.cfgFile = this.getDir().resolve(fileName);

            try {
                Files.copy(cfgFile, this.cfgFile, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }

        assert this.cfgFile != null;
        assert Files.exists(this.cfgFile);
    }

    @Override
    public Path getInlinedApk() {
        return this.inlinedApk;
    }

    @Override
    public void setInlinedApk(Path inlinedApk) {
        String fileName = inlinedApk.getFileName().toString();
        this.inlinedApk = this.getDir().resolve(fileName);

        try {
            Files.copy(inlinedApk, this.inlinedApk, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }

        assert Files.exists(this.inlinedApk);
    }

    @Override
    public List<IApi> getExploredApiList(){
        if (this.result == null)
            return new ArrayList<>();

        return this.result.getApiList();
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Scenario && this.restrictedApis.equals(((Scenario) other).restrictedApis);
    }

    @Override
    public boolean isValid() {
        return this.evaluator.isValid(this.result);
    }

    @Override
    public List<IApi> getRestrictedApiList(){
        return this.restrictedApis;
    }

    @Override
    public double getDissimilarity(){
        return this.evaluator.getDissimilarity(result);
    }

}
