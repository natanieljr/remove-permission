package org.droidmate.analyzer.exploration;

import org.droidmate.analyzer.api.Api;
import org.droidmate.analyzer.AppUnderTest;
import org.droidmate.analyzer.evaluation.IScenarioEvaluationStrategy;
import org.droidmate.analyzer.evaluation.InitialExplStrategy;
import org.droidmate.apis.ApiPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ExplorationStrategy implements IExplorationStrategy{
    private static final Logger logger = LoggerFactory.getLogger(ExplorationStrategy.class);
    private ApiPolicy policy;
    private IScenarioEvaluationStrategy evaluator;

    public ExplorationStrategy(ApiPolicy policy, IScenarioEvaluationStrategy evaluator) {
        this.policy = policy;
        this.evaluator = evaluator;
    }

    private List<Scenario> getValidScenarios(AppUnderTest app){
        return app
                .getScenarios(app.getCurrExplDepth() - 1)
                .stream()
                .filter(Scenario::isValid)
                .collect(Collectors.toList());
    }

    private List<Scenario> generateSimpleScenarios(AppUnderTest app) {
        logger.info("Generating simple scenarios");

        List<Scenario> scenarios = new ArrayList<>();

        // Don't continue the experiment if the initial exploration is not valid
        if (app.getInitialExpl().isValid()) {

            // filter privacy sensitive APIs (unique)
            List<Api> apiStream = app.getInitialMonitoredApiList();

            apiStream.forEach(api ->
            {
                Scenario scenario = Scenario.build(app, Collections.singletonList(api), app.getCurrExplDepth(),
                        this.policy, this.evaluator);

                // Somehow, the distinct operation of the stream class does not work and
                // the java.net.URL->openConnection() appears multiple times
                if (!scenarios.contains(scenario)) {
                    scenario.initialize();
                    scenarios.add(scenario);
                }
            });
        }

        return scenarios;
    }

    private List<Scenario> generateCompositeScenarios(AppUnderTest app) {
        logger.info("Generating composite scenarios");

        List<Scenario> scenarios = new ArrayList<>();
        List<Scenario> lastScenarios = this.getValidScenarios(app);

        for(Scenario s1 : lastScenarios)
            for(Scenario s2 : lastScenarios)
                if (!s1.equals(s2)){
                    Scenario newScenario = Scenario.build(s1, s2, app.getCurrExplDepth());

                    if (!lastScenarios.contains(newScenario)) {
                        newScenario.initialize();
                        scenarios.add(newScenario);
                    }
                }

        return scenarios;
    }

    private List<Scenario> generateInitialExpl(AppUnderTest app){
        Scenario s = Scenario.build(app, null, app.getCurrExplDepth(), this.policy,
                new InitialExplStrategy());
        s.initialize();
        return Collections.singletonList(s);
    }

    @Override
    public List<Scenario> generateScenarios(AppUnderTest app) {
        int currDepth = app.getCurrExplDepth();
        logger.info(String.format("Generating scenarios of depth %d", currDepth));

        // Initial exploration
        if (currDepth == 0)
            return this.generateInitialExpl(app);
        // First generation is simple scenarios
        else if (currDepth == 1)
            return this.generateSimpleScenarios(app);
        else
            return this.generateCompositeScenarios(app);
    }
}
