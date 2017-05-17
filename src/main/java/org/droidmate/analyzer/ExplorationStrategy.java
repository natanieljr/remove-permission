package org.droidmate.analyzer;

import org.droidmate.analyzer.tools.Api;
import org.droidmate.analyzer.tools.Scenario;
import org.droidmate.apis.ApiPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

/**
 * Define how scenarios are generated and combined
 */
class ExplorationStrategy
{
  private static final Logger logger = LoggerFactory.getLogger(ExplorationStrategy.class);
  private ApiPolicy    policy;

  ExplorationStrategy(ApiPolicy policy){
    this.policy = policy;
  }

  private List<Scenario> generateSimpleScenarios(AppUnderTest app)
  {
    logger.info("Generating simple scenarios");

    List<Scenario> scenarios = new ArrayList<>();

    // filter privacy sensitive APIs
    Stream<Api> apiStream = app.getInitialExpl().getResult()
            .getApiList().stream().filter(Api::hasRestriction);

    apiStream.forEachOrdered(api ->
    {
      Scenario scenario = Scenario.build(app, Collections.singletonList(api), app.getCurrExplDepth(), this.policy);
      scenarios.add(scenario);
    });

    return scenarios;
  }

  private List<Scenario> generateCompositeScenarios(AppUnderTest app)
  {
    return null;
  }

  List<Scenario> generateScenarios(AppUnderTest app)
  {
    // Initial exploration
    if (app.getCurrExplDepth() == 0)
    {
      Scenario s = Scenario.build(app,null, app.getCurrExplDepth(), this.policy);
      return Collections.singletonList(s);
    }

    // First generation is simple scenarios
    if (app.getCurrExplDepth() == 1)
      return this.generateSimpleScenarios(app);
    else
      return this.generateCompositeScenarios(app);
  }
}
