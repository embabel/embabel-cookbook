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

import com.embabel.agent.api.common.Ai;
import com.embabel.agent.api.common.PromptRunner;
import com.embabel.common.ai.model.LlmOptions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import static org.junit.jupiter.api.Assertions.assertNotNull;

// tag::bootstrap[]
@SpringBootTest(classes = CookbookTestApplication.class)
@ActiveProfiles("cookbook-test")
// Replace Spring's default test listeners so this test keeps dependency injection
// without triggering the broader reset/mock listeners that are noisy here.
@TestExecutionListeners(
        listeners = DependencyInjectionTestExecutionListener.class,
        mergeMode = TestExecutionListeners.MergeMode.REPLACE_DEFAULTS
)
class ObjectCreationTest {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private Ai ai;
    // end::bootstrap[]

    // tag::test[]
    @Test
    void creatingBuildsATravelSummary() {
        logger.info("Running object creation test");

        PromptRunner promptRunner = ai.withLlm(LlmOptions.withDefaultLlm().withTemperature(0.0)); // <1>
        var summary = promptRunner
                .withSystemPrompt("You are a travel assistant.") // <2>
                .creating(TripSummary.class) // <3>
                .withExample(
                        "Weekend in Rome",
                        new TripSummary(
                                "Rome",
                                "plane",
                                "Visit the Colosseum and finish with dinner in Trastevere.",
                                "Colosseum",
                                "150 EUR"
                        )
                ) // <4>
                .withValidation(true) // <5>
                .fromPrompt("""
                        Create a short travel summary for a weekend trip to Paris.
                        Return the destination, the transport, a short itinerary description, one highlight, and a budget.
                        """); // <6>

        logger.info("Created summary: {}", summary);

        assertNotNull(summary);
        assertNotNull(summary.destination());
        assertNotNull(summary.transport());
        assertNotNull(summary.itineraryDescription());
        assertNotNull(summary.highlight());
        assertNotNull(summary.budget());
    }
    // end::test[]
}

record TripSummary(String destination, String transport, String itineraryDescription, String highlight, String budget) {
}
