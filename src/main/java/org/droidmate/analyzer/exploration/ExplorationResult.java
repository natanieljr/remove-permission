package org.droidmate.analyzer.exploration;

import org.droidmate.analyzer.api.Api;
import org.droidmate.analyzer.api.IApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class ExplorationResult implements IExplorationResult {
    private static final Logger logger = LoggerFactory.getLogger(ExplorationResult.class);

    private Path explDir;
    private boolean crashed;
    private int nrWidgetsObs;
    private int nrWidgetsExpl;
    private List<IApi> apiList;

    public ExplorationResult(Path explDir) {
        logger.debug(String.format("Reading exploration results in %s", explDir.toString()));
        this.apiList = new ArrayList<>();
        this.explDir = explDir;

        this.tryReadStats();
        this.readSummary();
    }

    @Override
    public Path getExplDir() {
        return this.explDir;
    }

    @Override
    public boolean hasCrashed() {
        return this.crashed;
    }

    private Path getReportFolder() {
        return Paths.get(this.explDir.toString(), "report");
    }

    private void readStatsFile(Path statsFile) {
        try {
            // First line is header
            List<String> data = Files.readAllLines(statsFile);

            if ((data.size() > 0) && (data.get(1).trim().contains("\t"))) {
                String[] lineData = data.get(1).trim().split("\t");

                this.nrWidgetsObs = Integer.parseInt(lineData[5]);
                this.nrWidgetsExpl = Integer.parseInt(lineData[6]);

                this.crashed = !lineData[lineData.length - 1].equals("N/A (lack of DeviceException)");
            }
            else
                this.createErrorData();
        } catch (Exception e) {
            this.createErrorData();
            logger.error(e.getMessage(), e);
        }
    }

    private void createErrorData() {
        this.crashed = true;
        this.nrWidgetsObs = 0;
        this.nrWidgetsExpl = 0;
    }

    private void tryReadStats() {
        Path statsFile = Paths.get(this.getReportFolder().toString(), "aggregate_stats.txt");

        if (!Files.exists(statsFile))
            this.createErrorData();
        else
            this.readStatsFile(statsFile);
    }

    @Override
    public List<IApi> getApiList(){
        return this.apiList;
    }

    private void readSummary() {
        Path summaryFile = Paths.get(this.getReportFolder().toString(), "summary.txt");

        if (!Files.exists(summaryFile)) {
            this.createErrorData();
            return;
        }

        try {
            List<String> lines = Files.readAllLines(summaryFile);

            int l = lines.size();
            int i = 0;

            while (i < l) {
                String line = lines.get(i);
                // Api list
                if (line.contains("pairs count observed")) {
                    // Jump to start of the list
                    i += 3;
                    line = lines.get(i).trim();

                    while ((i < l) && (!line.contains("==================")) && (line.length() > 0)) {
                        line = lines.get(i).trim();

                        String[] data = line.split(" ");
                        String methodSignature, className, uri;

                        // Has Uri, must load
                        if (data[data.length - 2].contains("uri:")) {
                            uri = data[data.length - 1];
                            methodSignature = data[data.length - 3];
                            className = data[data.length - 5];
                        } else {
                            uri = "";
                            methodSignature = data[data.length - 1];
                            className = data[data.length - 3];
                        }

                        String params = Api.getParamsFromMethodSignature(methodSignature);
                        String methodName = Api.getMethodNameFromSignature(methodSignature);

                        // remove : from method name
                        className = className.replace(":", "");

                        i += 1;
                        IApi api = Api.build(className, methodName, params, uri);
                        logger.debug(String.format("Identified API %s", api.toString()));
                        this.apiList.add(api);
                    }
                }
                ++i;
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    public double getSize(){
        return Math.sqrt(Math.pow(this.nrWidgetsExpl, 2) + Math.pow(this.nrWidgetsObs, 2));
    }

    @Override
    public String toBrackedNotation(){
        StringBuilder b = new StringBuilder("root{");
        this.apiList.forEach(p -> b.append(String.format("{%s}", p.toString())));
        b.append("}");

        return b.toString();
    }

    @Override
    public int getNrWidgetsExplored(){
        return this.nrWidgetsExpl;
    }

    @Override
    public int getNrWidgetsObserved(){
        return this.nrWidgetsObs;
    }
}
