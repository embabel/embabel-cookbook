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
import com.embabel.cookbook.travel.domain.ItineraryRequest;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

// tag::bootstrap[]
@SpringBootTest(classes = CookbookTestApplication.class)
@ActiveProfiles("cookbook-test")
// Replace Spring's default test listeners so this test keeps dependency injection
// without triggering the broader reset/mock listeners that are noisy here.
@TestExecutionListeners(
        listeners = DependencyInjectionTestExecutionListener.class,
        mergeMode = TestExecutionListeners.MergeMode.REPLACE_DEFAULTS
)
class CreateObjectIfPossibleTest {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private Ai ai;
    // end::bootstrap[]

    // tag::test[]
    @Test
    void createObjectIfPossibleReturnsItineraryRequestWhenPromptIsSufficient() {
        logger.info("Running createObjectIfPossible positive test");

        var request = ai.withDefaultLlm() // <1>
                .createObjectIfPossible("""
                        Plan a three-day itinerary from London to Paris for Friday to Sunday.
                        Include Eurostar travel, the Eiffel Tower, the Louvre, and a Seine river walk.
                        """, ItineraryRequest.class); // <2>

        logger.info("Positive request: {}", request);

        assertNotNull(request);
    }

    @Test
    void createObjectIfPossibleReturnsNullWhenPromptIsInsufficient() {
        logger.info("Running createObjectIfPossible negative test");

        var request = ai.withDefaultLlm() // <3>
                .createObjectIfPossible("""
                        Plan a trip.
                        """, ItineraryRequest.class); // <4>

        logger.info("Negative request: {}", request);

        assertNull(request);
    }
    // end::test[]
}
