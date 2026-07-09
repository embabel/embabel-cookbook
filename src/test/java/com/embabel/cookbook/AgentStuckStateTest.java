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
import com.embabel.agent.api.common.OperationContext;
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
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

// tag::bootstrap[]
@SpringBootTest(classes = CookbookTestApplication.class)
@ActiveProfiles({"cookbook-test", "action-stuck-state"})
@TestExecutionListeners(
        listeners = DependencyInjectionTestExecutionListener.class,
        mergeMode = TestExecutionListeners.MergeMode.REPLACE_DEFAULTS
)
class AgentStuckStateTest {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private AgentPlatform agentPlatform;
    // end::bootstrap[]

    // tag::test[]
    // tag::travel-test[]
    @Test
    void travelRequestCompletes() {
        logger.info("Running travel-request stuck-state test");

        var process = AgentInvocation.create(agentPlatform, TravelRecommendation.class)
                .runAsync(new UserInput("Plan a travel itinerary from London to Paris"))
                .join();

        logger.info("Travel blackboard: {}", process.getBlackboard().infoString(true, 1));
        logger.info("Travel history: {}", process.getHistory().stream().map(ActionInvocation::getActionName).toList());

        assertEquals(AgentProcessStatusCode.COMPLETED, process.getStatus());
        var recommendation = process.last(TravelRecommendation.class);
        assertNotNull(recommendation);
        assertFalse(recommendation.text().isBlank());
    }
    // end::travel-test[]

    // tag::dog-test[]
    @Test
    void dogRequestStaysStuck() {
        logger.info("Running dog stuck-state test");

        var process = AgentInvocation.create(agentPlatform, TravelRecommendation.class) // <1>
                .runAsync(new UserInput("Tell me about my dog"))
                .join();

        logger.info("Dog blackboard: {}", process.getBlackboard().infoString(true, 1));
        logger.info("Dog history: {}", process.getHistory().stream().map(ActionInvocation::getActionName).toList());

        assertEquals(AgentProcessStatusCode.STUCK, process.getStatus());
        assertEquals(1, process.getHistory().size());
        assertEquals("com.embabel.cookbook.ActionStuckStateAgent.classifyRequest", process.getHistory().getFirst().getActionName());
    }
    // end::dog-test[]
    // end::test[]

}

// tag::agent[]
@Agent(description = "Show how a process becomes stuck when no action is eligible")
@Profile("action-stuck-state")
class ActionStuckStateAgent {

    private static final Logger logger = LoggerFactory.getLogger(ActionStuckStateAgent.class);

    // tag::classify-request[]
    @Action(description = "Classify whether the request is travel-related")
    RequestClassification classifyRequest(UserInput userInput, OperationContext context) {
        var request = isTravelRequest(userInput.getContent())
                ? new TravelInquiry(userInput)
                : new NonTravelInquiry(userInput);
        logger.info("Stuck-state blackboard: {}", context.getObjects());
        return request;
    }
    // end::classify-request[]

    // tag::build-travel-recommendation[]
    @Action(description = "Build a travel recommendation")
    @AchievesGoal(description = "The user has received a travel recommendation")
    TravelRecommendation buildTravelRecommendation(TravelInquiry request, OperationContext context) {
        return context.ai()
                .withDefaultLlm()
                .createObject("""
                        You are a travel assistant. Write a short travel recommendation.

                        # User input
                        %s
                        """.formatted(request.userInput().getContent()).trim(), TravelRecommendation.class);
    }
    // end::build-travel-recommendation[]

    private static boolean isTravelRequest(String text) {
        var normalized = text.toLowerCase();
        return normalized.contains("travel")
                || normalized.contains("trip")
                || normalized.contains("itinerary")
                || normalized.contains("flight")
                || normalized.contains("train")
                || normalized.contains("hotel");
    }
}
// end::agent[]

// tag::request-classification[]
interface RequestClassification {
}

record TravelInquiry(UserInput userInput) implements RequestClassification {
}

record NonTravelInquiry(UserInput userInput) implements RequestClassification {
}
// end::request-classification[]
