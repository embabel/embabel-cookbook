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
import com.embabel.common.core.thinking.ThinkingException;
import com.embabel.common.core.thinking.ThinkingResponse;
import com.embabel.cookbook.travel.domain.TravelPlan;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import static org.junit.jupiter.api.Assertions.*;

// tag::bootstrap[]
@SpringBootTest(classes = CookbookTestApplication.class)
@ActiveProfiles("cookbook-test")
// Replace Spring's default test listeners so this test keeps dependency injection
// without triggering the broader reset/mock listeners that are noisy here.
@TestExecutionListeners(
    listeners = DependencyInjectionTestExecutionListener.class,
    mergeMode = TestExecutionListeners.MergeMode.REPLACE_DEFAULTS
)
class ThinkingTest {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private Ai ai;
    // end::bootstrap[]

    // tag::positive[]
    @Test
    void createObjectWithThinkingResult() {
        logger.info("Running thinking positive test");

        PromptRunner runner = ai.withDefaultLlm(); // <1>
        assertTrue(runner.supportsThinking(), "Expected the prompt runner to support thinking"); // <2>

        ThinkingResponse<TravelPlan> response = runner
            .withSystemPrompt( // <3>
                """
                     You are travel assistant.
                     You MUST:
                   1. Provide reasoning inside <decision_reasoning>...</decision_reasoning>
                   2. Keep reasoning concise (3-5 bullet points)
                   3. Explain why the itinerary is optimal.

                   <decision_reasoning>
                   Explain:
                   - time constraint
                   - risk of being late
                   - trade-offs
                   </decision_reasoning>
                   """)
            .thinking() // <4>
            .createObject("""
                Create a short travel plan for a three-day trip from London to Paris.
                Include major landmarks.
                Make the most optimal and balanced itinerary.
                """, TravelPlan.class); // <5>

        logger.info("Positive thinking response: {}", response);
        logger.info("Positive thinking content: {}", response.getThinkingContent());

        assertNotNull(response);
        assertTrue(response.hasResult());
        assertNotNull(response.getResult());
        assertTrue(response.hasThinking()); // <5>
    }
    // end::positive[]

    // tag::nullable[]
    @Test
    void createObjectIfPossibleWithThinkingResult() {
        logger.info("Running thinking nullable test");

        PromptRunner runner = ai.withDefaultLlm(); // <1>
        assertTrue(runner.supportsThinking(), "Expected the prompt runner to support thinking"); // <2>

        ThinkingResponse<TravelPlan> response = runner
            .withSystemPrompt( // <3>
                """
                     You are travel assistant.
                     You MUST:
                   1. Provide reasoning inside <decision_reasoning>...</decision_reasoning>
                   2. Keep reasoning concise (3-5 bullet points)
                   3. Explain why the itinerary is optimal.

                   <decision_reasoning>
                   Explain:
                   - time constraint
                   - risk of being late
                   - trade-offs
                   </decision_reasoning>
                   """)
            .thinking() // <4>
            .createObjectIfPossible("""
                Create a short travel plan for a half-day trip from New York to Sydney.
                Include major landmarks.
                Make the most optimal and balanced itinerary.
                """, TravelPlan.class); // <5>

        logger.info("Nullable thinking response: {}", response);
        logger.info("Nullable thinking content: {}", response.getThinkingContent());
        logger.info("Nullable thinking exception: {}", response.getException() == null ? null : response.getException().getMessage());

        assertNotNull(response);
        assertNull(response.getResult());
        assertNotNull(response.getException());
        assertInstanceOf(ThinkingException.class, response.getException());
        assertTrue(response.getException().getMessage().contains("Impossible: half-day from New York to Sydney"));
    }
    // end::nullable[]
}
