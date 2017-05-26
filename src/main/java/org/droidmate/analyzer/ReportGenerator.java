package org.droidmate.analyzer;

import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.droidmate.analyzer.api.IApi;
import org.droidmate.analyzer.exploration.IExplorationResult;
import org.droidmate.analyzer.exploration.IScenario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Produce application report
 */
class ReportGenerator {
    private static final Logger logger = LoggerFactory.getLogger(ReportGenerator.class);

    private List<ImmutableTriple<IAppUnderTest, Date, Date>> experiments;

    ReportGenerator(){
        this.experiments = new ArrayList<>();
    }

    void addApp(IAppUnderTest app, Date startTime, Date endTime){
        ImmutableTriple<IAppUnderTest, Date, Date> triple = new ImmutableTriple<>(app, startTime, endTime);
        this.experiments.add(triple);
    }

    private String createReportHeader(Triple<IAppUnderTest, Date, Date> experiment){
        IAppUnderTest app = experiment.getLeft();
        Date start = experiment.getMiddle();
        Date end = experiment.getRight();

        List<IScenario> scenarios = app.getScenarios();
        int nrScenarios = scenarios.size();
        int nrScenariosSucc = app.getSuccessfulScenarios().size();
        int nrScenariosFail = app.getFailScenarios().size();
        int nrApis = app.getInitialMonitoredApiList().size();

        String headerFormat = "APP:\t\t%s\n" +
                "START:\t\t%s\n" +
                "END:\t\t%s\n" +
                "APIS:\t\t%d\n" +
                "SCENARIOS:\t%d\n" +
                "  SUCCESS:\t%d\n" +
                "  FAIL:\t\t%d\n\n\n";

        return String.format(headerFormat,
                             app.toString(),
                             ReportFormatter.formatDate(start),
                             ReportFormatter.formatDate(end),
                             nrApis,
                             nrScenarios,
                             nrScenariosSucc,
                             nrScenariosFail);
    }

    private String createScenarios(Triple<IAppUnderTest, Date, Date> experiment){
        IAppUnderTest app = experiment.getLeft();
        List<IScenario> scenarios = app.getScenarios();

        String scenarioFormat = "SCENARIO:\t\t\t%s\n" +
                "VALID:\t\t\t\t%s\n" +
                "DISSIMILARITY:\t\t%.6f\n" +
                "WIDGETS:\n" +
                "  OBSERVED:\t\t\t%d\n" +
                "  EXPLORED:\t\t\t%d\n" +
                "RESTRICTED APIS:\t%d\n" +
                "%s" +
                "EXPLORED APIS:\n" +
                "%s\n";

        StringBuilder b = new StringBuilder();

        scenarios.forEach(p -> {
            List<IApi> restricted = p.getRestrictedApiList();
            String restrictedStr = ReportFormatter.formatApiList(restricted);
            List<IApi> explored = p.getExploredApiList();
            String exploredStr = ReportFormatter.formatApiList(explored);

            IExplorationResult result = p.getResult();
            int nrWidgetsExpl = result.getNrWidgetsExplored();
            int nrWidgetsObs = result.getNrWidgetsObserved();
            String dirName = result.getExplDir().getParent().getFileName().toString();

            b.append(String.format(scenarioFormat,
                    dirName,
                    p.isValid() + "",
                    p.getDissimilarity(),
                    nrWidgetsObs,
                    nrWidgetsExpl,
                    restricted.size(),
                    restrictedStr,
                    exploredStr
                    ));
        });

        return b.toString();
    }

    private String createSummary(Triple<IAppUnderTest, Date, Date> experiment){
        IAppUnderTest app = experiment.getLeft();

        IScenario firstError = null;
        IScenario lastSuccess = null;

        for(IScenario scenario: app.getScenarios()){
            // updated first error
            if ((firstError == null) && (!scenario.isValid()))
                firstError = scenario;

            // update last success
            if (scenario.isValid()){
                if (lastSuccess != null){
                    int maxBlockedApis = lastSuccess.getRestrictedApiList().size();
                    int currBlockedApis = scenario.getRestrictedApiList().size();

                    if (currBlockedApis > maxBlockedApis)
                        lastSuccess = scenario;
                }
                else{
                    lastSuccess = scenario;
                }
            }
        }

        double nrApis = app.getInitialMonitoredApiList().size();
        int nrApisBeforreError = (int) nrApis;
        int nrApisBlocked = 0;
        String firstErrorStr = "NO ERROR";
        String lastSuccessStr = "NO SUCCESS";

        if (firstError != null) {
            nrApisBeforreError = Math.max(firstError.getRestrictedApiList().size() - 1, 0);
            firstErrorStr = firstError.getResult().getExplDir().getParent().getFileName().toString();
        }

        if (lastSuccess != null) {
            nrApisBlocked = lastSuccess.getRestrictedApiList().size();
            lastSuccessStr = lastSuccess.getResult().getExplDir().getParent().getFileName().toString();
        }

        String summaryFormat = //"VALID SCENARIOS (%):\t%.2f\n" +
                "FIRST ERROR (scenario):\t%s\n" +
                "LAST SUCCESS (scenario):\t%s\n" +
                "MAX APIS BEFORE ERROR:\t%d\n" +
                "MAX APIS BLOCKED:\t%d\n" +
                "MAX APIS BEFORE ERROR (PERC):\t%.2f\n" +
                "MAX APIS BLOCKED (PERC):\t%.2f\n";


        return String.format(summaryFormat,
                             firstErrorStr,
                             lastSuccessStr,
                             nrApisBeforreError,
                             nrApisBlocked,
                             (nrApisBeforreError/nrApis)*100,
                             (nrApisBlocked/nrApis)*100
                            );
    }

    private void createReport(Triple<IAppUnderTest, Date, Date> experiment){
        String header = this.createReportHeader(experiment);
        String scenarios = this.createScenarios(experiment);
        String summary = this.createSummary(experiment);

        String fileData = String.format("%s\n%s\n%s", header, scenarios, summary);
        IAppUnderTest app = experiment.getLeft();
        Path reportFile = app.getDir().resolve("report.txt");
        try{
            Files.write(reportFile, fileData.getBytes());
        }
        catch(IOException e){
            logger.error(e.getMessage(), e);
        }

        assert Files.exists(reportFile);
    }

    void generateReport(){
        this.experiments.forEach(this::createReport);
    }
}
