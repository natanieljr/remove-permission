package org.droidmate.analyzer.exploration;

import org.droidmate.analyzer.IAppUnderTest;
import org.droidmate.analyzer.api.IApi;
import org.droidmate.analyzer.evaluation.IEvaluationStrategy;
import org.droidmate.apis.ApiPolicy;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Builder pattern to create IScenario objects
 */
public class ScenarioBuilder {

    IScenario build(IAppUnderTest app, List<IApi> restrictedApis, int explDepth, ApiPolicy policy,
                          IEvaluationStrategy evaluator) {
        if (restrictedApis == null)
            restrictedApis = new ArrayList<>();

        return new Scenario(app, restrictedApis, explDepth, policy, evaluator);
    }

    IScenario build(IScenario s1, IScenario s2, IAppUnderTest app, int explDepth, ApiPolicy policy,
                    IEvaluationStrategy evaluator) {
        List<IApi> restrictedApis = new ArrayList<>();
        Scenario scenario1 = (Scenario)s1;
        restrictedApis.addAll(scenario1.getRestrictedApiList());

        for(IApi api : ((Scenario)s2).getRestrictedApiList())
            if (!restrictedApis.contains(api))
                restrictedApis.add(api);

        restrictedApis.sort(Comparator.comparing(IApi::toString));

        return this.build(app, restrictedApis, explDepth, policy, evaluator);
    }

}
