package org.droidmate.analyzer.exploration;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.io.FileUtils;
import org.droidmate.analyzer.AppUnderTest;
import org.droidmate.analyzer.ResourceManager;
import org.droidmate.analyzer.api.Api;
import org.droidmate.apis.ApiPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import static java.nio.file.Files.readAllLines;

/**
 * Scenario (to be) explored
 */
public class Scenario {
    private static final Logger logger = LoggerFactory.getLogger(Scenario.class);

    private ExplorationResult result;
    private List<Api> restrictedApis;
    private int explDepth;
    private Path dir;
    private Path cfgFile;
    private Path inlinedApk;
    private AppUnderTest app;
    private ApiPolicy policy;

    private void applyRestriction(JsonObject api, Api restriction) {
        if (restriction.getURI().length() > 0) {
            // Currently the URIs are called "uri", so no fancy logic was developed
            String newRestriction = String.format("%s.toString().equals(\"%s\")",
                    restriction.getURIParamName(), restriction.getURI());

            String currRestriction = api.get("customPolicyConstraint").getAsString();

            // is already restricted, add extra condition
            if (currRestriction.length() > 0)
                currRestriction = String.format("(%s) && (%s)", currRestriction, newRestriction);
            else
                currRestriction = newRestriction;

            api.remove("customPolicyConstraint");
            api.addProperty("customPolicyConstraint", currRestriction);
        }

        api.remove("policy");
        api.addProperty("policy", this.policy.toString());
    }

    private Path writeNewMonitoredApisFile(JsonObject jsonApiList) throws IOException {
        Path newFile = Paths.get(this.getDir().toString(), "monitored_apis.json");
        Files.write(newFile, jsonApiList.toString().getBytes());

        return newFile;
    }

    private Path createCfgFile(List<Api> restrictedApis) {

        Path defaultFile = new ResourceManager().getDefaultMonitoredApisFile();

        // Initial exploration
        if (restrictedApis.size() == 0)
            return defaultFile;

        try {
            String fileData = String.join("\n", Files.readAllLines(defaultFile));
            JsonObject jsonApiList = new JsonParser().parse(fileData).getAsJsonObject();

            JsonElement apis = jsonApiList.get("apis");

            ((JsonArray) apis).forEach(item ->
            {
                Api api = Api.build((JsonObject) item);

                // Check if APi is being restricted
                if ((restrictedApis.contains(api)) && (api.hasRestriction())) {
                    this.applyRestriction((JsonObject) item, api.getRestriction());
                }
            });

            return this.writeNewMonitoredApisFile(jsonApiList);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }

        return null;
    }

    static Scenario build(AppUnderTest app, List<Api> restrictedApis, int explDepth, ApiPolicy policy) {
        if (restrictedApis == null)
            restrictedApis = new ArrayList<>();

        return new Scenario(app, restrictedApis, explDepth, policy);
    }

    private Scenario(AppUnderTest app, List<Api> restrictedApis, int explDepth, ApiPolicy policy) {
        this.app = app;
        this.restrictedApis = restrictedApis;
        this.explDepth = explDepth;
        this.policy = policy;

        this.createDir();
        Path cfgFile = this.createCfgFile(restrictedApis);
        this.setCfgFile(cfgFile);
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

    private void setCfgFile(Path cfgFile) {
        if (cfgFile.getParent().equals(this.getDir()))
            this.cfgFile = cfgFile;
        else {

            String fileName = cfgFile.getFileName().toString();
            this.cfgFile = Paths.get(this.getDir().toString(), fileName);

            try {
                Files.copy(cfgFile, this.cfgFile, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }

        assert this.cfgFile != null;
        assert Files.exists(this.cfgFile);
    }

    private Path getDir() {
        return this.dir;
    }

    public ExplorationResult getResult() {
        return this.result;
    }

    private Path copyExplOutputToDir(ExplorationResult res) {
        Path src = res.getExplDir();
        Path dst = Paths.get(this.getDir().toString(), "output_device1");

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

    public void setResult(ExplorationResult result) {
        Path newResDir = this.copyExplOutputToDir(result);
        this.result = new ExplorationResult(newResDir);
    }

    public int getExplDepth() {
        return this.explDepth;
    }

    public Path getCfgFile() {
        return this.cfgFile;
    }

    public Path getInlinedApk() {
        return this.inlinedApk;
    }

    public void setInlinedApk(Path inlinedApk) {
        String fileName = inlinedApk.getFileName().toString();
        this.inlinedApk = Paths.get(this.getDir().toString(), fileName);

        try {
            Files.copy(inlinedApk, this.inlinedApk, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }

        assert Files.exists(this.inlinedApk);
    }

    public List<Api> getRestrictedApis(){
        return this.restrictedApis;
    }
}
