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
import com.embabel.agent.api.common.PlannerType;
import com.embabel.agent.api.invocation.AgentInvocation;
import com.embabel.agent.core.ActionInvocation;
import com.embabel.agent.core.AgentPlatform;
import com.embabel.agent.core.AgentProcessStatusCode;
import com.embabel.agent.domain.io.UserInput;
import com.embabel.cookbook.travel.domain.TravelRecommendation;
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
@ActiveProfiles({"cookbook-test", "action-heuristics"})
// Replace Spring's default test listeners so this test keeps dependency injection
// without triggering the broader reset/mock listeners that are noisy here.
@TestExecutionListeners(
        listeners = DependencyInjectionTestExecutionListener.class,
        mergeMode = TestExecutionListeners.MergeMode.REPLACE_DEFAULTS
)
class ActionHeuristicsTest {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private AgentPlatform agentPlatform;
    // end::bootstrap[]

    // tag::test[]
    @Test
    void plannerChoosesCheapestAction() {
        logger.info("Running action heuristics test");

        var process = AgentInvocation.create(agentPlatform, TravelRecommendation.class)
                .runAsync(new UserInput("Choose the best travel recommendation"))
                .join();

        logger.info("Heuristics blackboard: {}", process.getBlackboard().infoString(true, 1));
        logger.info("Heuristics history: {}", process.getHistory().stream().map(ActionInvocation::getActionName).toList());

        assertEquals(AgentProcessStatusCode.COMPLETED, process.getStatus());
        assertEquals(1, process.getHistory().size());
        assertEquals("com.embabel.cookbook.ActionHeuristicsAgent.buildCheapestRecommendation", process.getHistory().getFirst().getActionName());
        var recommendation = process.last(TravelRecommendation.class);
        assertNotNull(recommendation);
        assertEquals("Cheapest travel recommendation selected.", recommendation.text());
    }
    // end::test[]
}

// tag::agent[]
@Agent(description = "Show how action cost steers the planner", planner = PlannerType.GOAP)
@Profile("action-heuristics")
class ActionHeuristicsAgent {

    @AchievesGoal(description = "The user has received a travel recommendation")
    @Action(description = "Build the cheapest recommendation", cost = 0.1)
    TravelRecommendation buildCheapestRecommendation(UserInput userInput) {
        return new TravelRecommendation("Cheapest travel recommendation selected.");
    }

    @AchievesGoal(description = "The user has received a travel recommendation")
    @Action(description = "Build the premium recommendation", cost = 0.9)
    TravelRecommendation buildPremiumRecommendation(UserInput userInput) {
        return new TravelRecommendation("Premium travel recommendation selected.");
    }
}
// end::agent[]
