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
import com.embabel.agent.api.streaming.StreamingPromptRunnerBuilder;
import com.embabel.cookbook.travel.domain.Itinerary;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

// tag::bootstrap[]
@SpringBootTest(classes = CookbookTestApplication.class)
@ActiveProfiles("cookbook-test")
// Replace Spring's default test listeners so this test keeps dependency injection
// without triggering the broader reset/mock listeners that are noisy here.
@TestExecutionListeners(
        listeners = DependencyInjectionTestExecutionListener.class,
        mergeMode = TestExecutionListeners.MergeMode.REPLACE_DEFAULTS
)
class StreamingTest {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private Ai ai;
    // end::bootstrap[]

    // tag::test[]
    @Test
    void streamingTopThreeItineraries() {
        logger.info("Running streaming test");

        PromptRunner runner = ai.withDefaultLlm(); // <1>
        assertTrue(runner.supportsStreaming(), "Expected the prompt runner to support streaming"); // <2>

        List<Itinerary> itineraries = new CopyOnWriteArrayList<>(); // <3>
        AtomicReference<Throwable> errorOccurred = new AtomicReference<>(); // <4>
        AtomicBoolean completionCalled = new AtomicBoolean(false); // <5>

        new StreamingPromptRunnerBuilder(runner) // <6>
                .streaming()
                .withPrompt("""
                        Return exactly three itinerary options for a three-day trip from London to Paris.
                        Emit each itinerary as a separate JSON object with a single text field.
                        Keep the options distinct and concise.
                        """) // <7>
                .createObjectStream(Itinerary.class) // <8>
                .timeout(Duration.ofSeconds(240))
                .doOnNext(itinerary -> {
                    itineraries.add(itinerary);
                    logger.info("Streamed itinerary: {}", itinerary.text());
                })
                .doOnError(error -> errorOccurred.set(error))
                .doOnComplete(() -> completionCalled.set(true))
                .blockLast(Duration.ofSeconds(240)); // <9>

        logger.info("Streamed itineraries: {}", itineraries);

        assertNull(errorOccurred.get());
        assertNotNull(itineraries);
        assertTrue(completionCalled.get(), "Expected the streaming flux to complete");
        assertEquals(3, itineraries.size());
    }
    // end::test[]
}
