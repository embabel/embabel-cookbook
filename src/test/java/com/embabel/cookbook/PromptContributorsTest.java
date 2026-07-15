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
import com.embabel.chat.Message;
import com.embabel.chat.SystemMessage;
import com.embabel.chat.UserMessage;
import com.embabel.cookbook.travel.domain.TravelPlan;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
class PromptContributorsTest {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private Ai ai;
    // end::bootstrap[]

    // tag::test[]
    @Test
    void creatingFromMessagesBuildsTravelPlan() {
        logger.info("Running prompt contributors test");

        PromptRunner runner = ai.withDefaultLlm(); // <1>
        List<Message> messages = List.of( // <2>
                new SystemMessage("You are a travel assistant."),
                new UserMessage("Create a structured travel plan for a three-day trip from London to Paris."),
                new UserMessage("Include Eurostar travel, the Eiffel Tower, the Louvre, and a Seine walk.")
        );

        var plan = runner
                .creating(TravelPlan.class) // <3>
                .fromMessages(messages); // <4>

        logger.info("Created travel plan: {}", plan);

        assertNotNull(plan);
        assertEquals("Paris, France", plan.destination());
        assertNotNull(plan.transport());
        assertNotNull(plan.itineraryDescription());
        assertNotNull(plan.highlight());
    }
    // end::test[]
}
