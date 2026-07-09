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
import com.embabel.agent.api.annotation.Condition;
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
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

// tag::bootstrap[]
@SpringBootTest(classes = CookbookTestApplication.class)
@ActiveProfiles({"cookbook-test", "action-condition"})
class ActionConditionTest {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private AgentPlatform agentPlatform;
    // end::bootstrap[]

    // tag::test[]
    // tag::direct-flight-test[]
    @Test
    void directFlightOnlyCompletes() {
        logger.info("Running direct-flight condition test");

        var process = AgentInvocation.create(agentPlatform, TravelRecommendation.class) // <1>
                .runAsync(new UserInput("Find me a direct flight from New York to London"))
                .join();

        logger.info("Direct-flight blackboard: {}", process.getBlackboard().infoString(true, 1));
        logger.info("Direct-flight history: {}", process.getHistory().stream().map(ActionInvocation::getActionName).toList());

        assertEquals(AgentProcessStatusCode.COMPLETED, process.getStatus());
        var recommendation = process.last(TravelRecommendation.class);
        assertNotNull(recommendation);
        assertFalse(recommendation.text().isBlank());
    }
    // end::direct-flight-test[]

    // tag::flight-with-stops-test[]
    @Test
    void flightWithStopsCompletes() {
        logger.info("Running with-stops condition test");

        var process = AgentInvocation.create(agentPlatform, TravelRecommendation.class)
                .runAsync(new UserInput("Find me a flight from New York to London with stops"))
                .join();

        logger.info("With-stops blackboard: {}", process.getBlackboard().infoString(true, 1));
        logger.info("With-stops history: {}", process.getHistory().stream().map(ActionInvocation::getActionName).toList());

        assertEquals(AgentProcessStatusCode.COMPLETED, process.getStatus());
        var recommendation = process.last(TravelRecommendation.class);
        assertNotNull(recommendation);
        assertFalse(recommendation.text().isBlank());
    }
    // end::flight-with-stops-test[]
    // end::test[]

}

// tag::agent[]
@Agent(description = "Help the user choose between direct and with-stops flights") // <1>
@Profile("action-condition")
class FlightConditionAgent {

    private static final Logger logger = LoggerFactory.getLogger(FlightConditionAgent.class);

    @Action(description = "Analyze the flight request", post = {"directFlightRequested", "stopsAllowed"}) // <2>
    // tag::analyze-request[]
    FlightRoutingRequest analyzeFlightRequest(UserInput userInput, OperationContext context) {
        var request = new FlightRoutingRequest(userInput, isDirectFlightOnly(userInput.getContent()));
        context.setCondition("directFlightRequested", request.directFlightOnly());
        context.setCondition("stopsAllowed", request.stopsAllowed());
        return request;
    }
    // end::analyze-request[]

    // tag::build-direct-flight[]
    @Action(description = "Build a direct flight", pre = {"directFlightRequested"})
    FlightPlan buildDirectFlight(FlightRoutingRequest request, OperationContext context) {
        return context.ai()
                .withDefaultLlm()
                .createObject("""
                        You are a travel assistant. Recommend a specific direct flight.
                        Do not include any stopover or connection city.
                        Use airport codes.

                        # User input
                        %s
                        """.formatted(request.userInput().getContent()).trim(), DirectFlightPlan.class);
    }
    // end::build-direct-flight[]

    // tag::build-flight-with-stops[]
    @Action(description = "Build a flight with stops", pre = {"stopsAllowed"})
    FlightPlan buildFlightWithStops(FlightRoutingRequest request, OperationContext context) {
        return context.ai()
                .withDefaultLlm()
                .createObject("""
                        You are a travel assistant. Recommend a flight with at least one stop.
                        Include exactly one connection city.
                        Do not recommend a nonstop or direct flight.
                        Use airport codes.

                        # User input
                        %s
                        """.formatted(request.userInput().getContent()).trim(), ConnectingFlightPlan.class);
    }
    // end::build-flight-with-stops[]

    // tag::summarize[]
    @Action(description = "Summarize the flight")
    @AchievesGoal(description = "The user has received a flight recommendation")
    TravelRecommendation summarize(FlightPlan flightPlan, OperationContext context) {
        return new TravelRecommendation(flightPlan.recommendation());
    }
    // end::summarize[]

    // tag::direct-flight-request[]
    @Condition // <3>
    boolean directFlightRequested(FlightRoutingRequest request, OperationContext context) {
        logger.info("Evaluating direct-flight request against blackboard: {}", context.getObjects());
        return request.directFlightOnly();
    }
    // end::direct-flight-request[]

    // tag::stops-allowed[]
    @Condition // <4>
    boolean stopsAllowed(FlightRoutingRequest request, OperationContext context) {
        logger.info("Evaluating with-stops request against blackboard: {}", context.getObjects());
        return request.stopsAllowed();
    }
    // end::stops-allowed[]

    private static boolean isDirectFlightOnly(String text) {
        var normalized = text.toLowerCase();
        return normalized.contains("direct") || normalized.contains("nonstop");
    }
}
// end::agent[]

// tag::flight-routing-request[]
record FlightRoutingRequest(UserInput userInput, boolean directFlightOnly) {

    boolean stopsAllowed() {
        return !directFlightOnly;
    }
}
// end::flight-routing-request[]

// tag::flight-plan[]
sealed interface FlightPlan permits DirectFlightPlan, ConnectingFlightPlan {

    String recommendation();
}
// end::flight-plan[]

// tag::direct-flight-plan[]
record DirectFlightPlan(String flightNumber, String departure, String arrival) implements FlightPlan {

    @Override
    public String recommendation() {
        return "Direct flight %s departing from %s and arriving at %s.".formatted(
                flightNumber,
                departure,
                arrival
        );
    }
}
// end::direct-flight-plan[]

// tag::connecting-flight-plan[]
record ConnectingFlightPlan(String flightNumber, String departure, String connectionCity, String arrival)
        implements FlightPlan {

    @Override
    public String recommendation() {
        return "Connecting flight %s departing from %s, stopping in %s, and arriving at %s.".formatted(
                flightNumber,
                departure,
                connectionCity,
                arrival
        );
    }
}
// end::connecting-flight-plan[]
