package org.droidmate.analyzer.exploration;

import org.droidmate.analyzer.IAppUnderTest;
import org.droidmate.analyzer.api.IApi;
import org.droidmate.analyzer.evaluation.EvaluationStrategyBuilder;
import org.droidmate.analyzer.evaluation.IEvaluationStrategy;
import org.droidmate.apis.ApiPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class DefaultExplorationStrategy implements IExplorationStrategy {
    private static final Logger logger = LoggerFactory.getLogger(DefaultExplorationStrategy.class);
    private ApiPolicy policy;
    private EvaluationStrategyBuilder evaluatorBuilder;

    public DefaultExplorationStrategy(ApiPolicy policy, EvaluationStrategyBuilder evaluatorBuilder) {
        this.policy = policy;
        this.evaluatorBuilder = evaluatorBuilder;
    }

    private List<IScenario> getValidScenarios(IAppUnderTest app) {
        return app
                .getScenarios(app.getCurrExplDepth() - 1)
                .stream()
                .filter(IScenario::isValid)
                .collect(Collectors.toList());
    }

    private IEvaluationStrategy getEvaluationStrategy(IAppUnderTest app) {
        IScenario initialExpl = app.getInitialExpl();
        return this.evaluatorBuilder.build(initialExpl);
    }

    private List<IScenario> generateSimpleScenarios(IAppUnderTest app) {
        logger.info("Generating simple scenarios");

        List<IScenario> scenarios = new ArrayList<>();

        // Don't continue the experiment if the initial exploration is not valid
        if (app.getInitialExpl().isValid()) {

            // filter privacy sensitive APIs (unique)
            List<IApi> apiStream = app.getInitialMonitoredApiList();

            apiStream.forEach(api ->
            {
                Scenario scenario = Scenario.build(app, Collections.singletonList(api), app.getCurrExplDepth(),
                        this.policy, this.getEvaluationStrategy(app));

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

    private List<IScenario> generateCompositeScenarios(IAppUnderTest app) {
        logger.info("Generating composite scenarios");

        List<IScenario> scenarios = new ArrayList<>();
        List<IScenario> lastScenarios = this.getValidScenarios(app);

        for(IScenario s1 : lastScenarios)
            for(IScenario s2 : lastScenarios)
                if (!s1.equals(s2)){
                    Scenario newScenario = Scenario.build(s1, s2, app.getCurrExplDepth());

                    if (!scenarios.contains(newScenario)) {
                        newScenario.initialize();
                        scenarios.add(newScenario);
                    }
                }
        return scenarios;
    }

    private List<IScenario> generateInitialExpl(IAppUnderTest app){
        Scenario s = Scenario.build(app, null, app.getCurrExplDepth(), this.policy,
                this.getEvaluationStrategy(app));
        s.initialize();
        return Collections.singletonList(s);
    }

    @Override
    public List<IScenario> generateScenarios(IAppUnderTest app) {
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
