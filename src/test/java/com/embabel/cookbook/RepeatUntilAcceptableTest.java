/*
 * Copyright 2024-2026 Embabel Pty Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.embabel.cookbook;

import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.common.ActionContext;
import com.embabel.agent.api.common.workflow.loop.RepeatUntilAcceptableBuilder;
import com.embabel.agent.api.common.workflow.loop.TextFeedback;
import com.embabel.agent.api.invocation.AgentInvocation;
import com.embabel.agent.core.ActionInvocation;
import com.embabel.agent.core.AgentPlatform;
import com.embabel.agent.core.AgentProcessStatusCode;
import com.embabel.agent.domain.io.UserInput;
import com.embabel.cookbook.travel.domain.Itinerary;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

// tag::bootstrap[]
@SpringBootTest(classes = CookbookTestApplication.class)
@ActiveProfiles({"cookbook-test", "repeat-until-acceptable"})
// Replace Spring's default test listeners so this test keeps dependency injection
// without triggering the broader reset/mock listeners that are noisy here.
@TestExecutionListeners(
        listeners = DependencyInjectionTestExecutionListener.class,
        mergeMode = TestExecutionListeners.MergeMode.REPLACE_DEFAULTS
)
class RepeatUntilAcceptableTest {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private AgentPlatform agentPlatform;
    // end::bootstrap[]

    // tag::test[]
    @Test
    void loopRetriesUntilTheResultIsAcceptable() {
        logger.info("Running repeat-until acceptable test");

        var process = AgentInvocation.create(agentPlatform, Itinerary.class) // <1>
                .runAsync(new UserInput("Plan a 3-day itinerary from London to Paris"))
                .join();

        logger.info("Loop blackboard: {}", process.getBlackboard().infoString(true, 1));
        logger.info("Loop history: {}", process.getHistory().stream().map(ActionInvocation::getActionName).toList());

        assertEquals(AgentProcessStatusCode.COMPLETED, process.getStatus());
        assertEquals(1, process.getHistory().size());
        assertEquals("com.embabel.cookbook.RepeatUntilAcceptableAgent.reviseUntilAcceptable",
                process.getHistory().getFirst().getActionName());
        var itinerary = process.last(Itinerary.class);
        assertNotNull(itinerary);
        assertEquals("""
                Day 1: Depart from London and arrive in Paris.
                Day 2: Visit the Eiffel Tower and the Louvre.
                Day 3: Take a Seine cruise and depart.
                """.strip(), itinerary.text());
    }
    // end::test[]
}

// tag::agent[]
@Agent(description = "Show how repeat-until acceptable retries a result until it passes the score threshold")
@Profile("repeat-until-acceptable")
class RepeatUntilAcceptableAgent {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @AchievesGoal(description = "The user has received an acceptable itinerary")
    @Action(description = "Revise the itinerary until it is acceptable")
    Itinerary reviseUntilAcceptable(UserInput userInput, ActionContext actionContext) { // <1>
        return RepeatUntilAcceptableBuilder
                .returning(Itinerary.class) // <2>
                .withMaxIterations(3) // <3>
                .withScoreThreshold(0.8) // <4>
                .repeating(context -> {
                    var lastAttempt = context.lastAttempt();
                    var itineraryText = lastAttempt == null
                            ? """
                            Day 1: Depart from London and arrive in Paris.
                            """.strip()
                            : """
                            Day 1: Depart from London and arrive in Paris.
                            Day 2: Visit the Eiffel Tower and the Louvre.
                            Day 3: Take a Seine cruise and depart.
                            """.strip();
                    logger.info("Repeat-until attempt: {}", itineraryText);
                    return new Itinerary(itineraryText);
                }) // <5>
                .withEvaluator(context -> {
                    var text = context.getResultToEvaluate().text();
                    var score = text.contains("Day 2") && text.contains("Day 3") ? 0.9 : 0.2;
                    var feedback = score >= 0.8 ? "Three-day itinerary accepted" : "Needs the full three-day itinerary";
                    logger.info("Repeat-until evaluation: score={} feedback={}", score, feedback);
                    return new TextFeedback(score, feedback);
                }) // <6>
                .build() // <7>
                .asSubProcess(actionContext, Itinerary.class); // <8>
    }
}
// end::agent[]
