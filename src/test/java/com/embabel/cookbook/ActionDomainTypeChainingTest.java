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
import com.embabel.agent.api.common.Ai;
import com.embabel.agent.api.invocation.AgentInvocation;
import com.embabel.agent.core.AgentPlatform;
import com.embabel.agent.domain.io.UserInput;
import com.embabel.cookbook.travel.domain.Flight;
import com.embabel.cookbook.travel.domain.FlightRequest;
import com.embabel.cookbook.travel.domain.Itinerary;
import com.embabel.cookbook.travel.domain.ItineraryRequest;
import com.embabel.cookbook.travel.domain.TravelActivity;
import com.embabel.cookbook.travel.domain.TravelRecommendation;
import com.embabel.cookbook.travel.domain.TravelRequest;
import com.embabel.cookbook.travel.domain.TravelRequestType;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertFalse;

// tag::bootstrap[]
@SpringBootTest(classes = CookbookTestApplication.class)
@ActiveProfiles({"cookbook-test", "action-domain-type-chaining"})
class ActionDomainTypeChainingTest {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private AgentPlatform agentPlatform;
    // end::bootstrap[]

    // tag::test[]
    // tag::flight-request-test[]
    @Test
    void flightRequestChainsThroughFlightActivity() {
        logger.info("Running flight request domain type chaining test");

        var recommendation = AgentInvocation.create(agentPlatform, TravelRecommendation.class) // <1>
                .invoke(new UserInput("Find me a flight from New York to London"));

        logger.info("Flight recommendation: {}", recommendation.text());
        assertNotNull(recommendation);
        assertFalse(recommendation.text().isBlank());
    }
    // end::flight-request-test[]

    // tag::itinerary-request-test[]
    @Test
    void itineraryRequestChainsThroughItineraryActivity() {
        logger.info("Running itinerary request domain type chaining test");

        var recommendation = AgentInvocation.create(agentPlatform, TravelRecommendation.class) // <1>
                .invoke(new UserInput("Plan a day in London around museums and food"));

        logger.info("Itinerary recommendation: {}", recommendation.text());
        assertNotNull(recommendation);
        assertFalse(recommendation.text().isBlank());
    }
    // end::itinerary-request-test[]
    // end::test[]

}

// tag::agent[]
@Agent(description = "Help the user generate an itinerary or find a flight") // <1>
@Profile("action-domain-type-chaining")
class TravelAgent {

    @Autowired
    private Ai ai; // <2>

    // tag::classify-request[]
    @Action(description = "Classify the user input as a flight or itinerary request") // <3>
    TravelRequest classifyRequest(UserInput userInput) {
        var requestType = ai.withDefaultLlm()
                .createObject("""
                        You are a helpful travel assistant classifying whether the request needs a flight or an itinerary.

                        # User input
                        %s
                        """.formatted(userInput.getContent()).trim(), TravelRequestType.class); // <4>

        return switch (requestType) {
            case FLIGHT -> new FlightRequest(userInput);
            case ITINERARY -> new ItineraryRequest(userInput);
        };
    }
    // end::classify-request[]

    // tag::find-flight[]
    @Action(description = "Find a flight based on user input") // <5>
    Flight findFlight(FlightRequest request) {
        return ai.withDefaultLlm()
                .createObject("""
                        You are a travel assistant. Always recommend a specific flight to go to. Use airport codes.

                        # User input
                        %s
                        """.formatted(request.userInput().getContent()).trim(), Flight.class);
    }
    // end::find-flight[]

    // tag::build-itinerary[]
    @Action(description = "Build a travel itinerary based on user input") // <6>
    Itinerary buildItinerary(ItineraryRequest request) {
        return ai.withDefaultLlm()
                .createObject("""
                        You are a travel assistant, so build a concise itinerary based on the user input.

                        # User input
                        %s
                        """.formatted(request.userInput().getContent()).trim(), Itinerary.class);
    }
    // end::build-itinerary[]

    // tag::summarize[]
    @AchievesGoal(description = "The user has received a travel recommendation") // <7>
    @Action(description = "Summarize the selected travel activity")
    TravelRecommendation summarize(TravelActivity activity) {
        return ai.withDefaultLlm()
                .createObject("""
                        Summarize this travel recommendation for the user.

                        # Recommendation
                        %s
                        """.formatted(activity.recommendation()).trim(), TravelRecommendation.class);
    }
    // end::summarize[]
}
// end::agent[]
