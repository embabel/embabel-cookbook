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

import com.embabel.agent.api.annotation.LlmTool;
import com.embabel.agent.api.common.Ai;
import com.embabel.agent.api.common.PromptRunner;
import com.embabel.agent.api.tool.callback.LogLevel;
import com.embabel.agent.api.tool.callback.ToolCallLoggingInspector;
import com.embabel.cookbook.travel.domain.TravelPlan;
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
@TestExecutionListeners(
        listeners = DependencyInjectionTestExecutionListener.class,
        mergeMode = TestExecutionListeners.MergeMode.REPLACE_DEFAULTS
)
class ToolCallTest {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private Ai ai;
    // end::bootstrap[]

    // tag::tooling[]
    static class TravelTooling {

        @LlmTool(description = "Get current weather for a destination.")
        public String getWeather(String destination) {
            return "Sunny, 24°C, low humidity — ideal for outdoor sightseeing in " + destination + ".";
        }

        @LlmTool(description = "Get top attractions for a destination.")
        public String getTopAttractions(String destination) {
            return "Top attractions in " + destination + ": Eiffel Tower, Louvre Museum, Montmartre, Seine River cruise.";
        }
    }
    // end::tooling[]

    // tag::test[]
    @Test
    void planTripWithTools() {
        PromptRunner runner = ai.withDefaultLlm()
                .withToolObject(new TravelTooling()) // <1>
                .withToolCallInspectors(new ToolCallLoggingInspector(LogLevel.INFO, logger)); // <2>

        TravelPlan plan = runner
                .creating(TravelPlan.class) // <3>
                .fromPrompt("""
                        Plan a 3-day trip to Paris for a traveler from London.
                        Use tools to check the weather and top attractions.
                        """); // <4>

        logger.info("Created travel plan: {}", plan);

        assertNotNull(plan.destination());
        assertNotNull(plan.itineraryDescription());
        assertNotNull(plan.highlight());
    }
    // end::test[]
}
