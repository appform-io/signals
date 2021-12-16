/*
 * Copyright 2021. Santanu Sinha
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
 *  compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and limitations
 * under the License.
 */

package io.appform.signals.signals;

import io.appform.signals.CountingConsumer;
import lombok.val;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static io.appform.signals.TestingUtils.loop;
import static io.appform.signals.TestingUtils.printTime;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

/**
 *
 */
class ConsumingSyncSignalTest {

    @Test
    void testDefault() {
        val s = new ConsumingSyncSignal<Integer>();
        sumTest(s);
    }

    @Test
    void testBuilder() {
        val s = ConsumingSyncSignal.<Integer>builder().build();
        sumTest(s);
    }

    @Test
    void testBuilderConsumer() {
        val combiner = new CountingConsumer();
        val s = ConsumingSyncSignal.<Integer>builder()
                .combiner(combiner).build();
        sumTest(s);
        assertEquals(200, combiner.getHandlerCount().get());
        assertEquals(20, combiner.getGroupCount().get());
    }

    @Test
    void testBuilderErrorHandler() {
        final int[] errorCounter = {0};
        val s = ConsumingSyncSignal.<Integer>builder()
                .errorHandler(e -> errorCounter[0]++)
                .build();
        loop(10).forEach(i -> s.connect(x -> {
            if (x % 2 == 0) {
                throw new IllegalStateException();
            }
        }));
        printTime(() -> loop(20).forEach(s::dispatch));
        assertEquals(100, errorCounter[0]); // Five times per dispatch
    }

    @Test
    void testNamedHandlers() {
        val s = new ConsumingSyncSignal<Void>();
        val ctr = new AtomicInteger();

        val handlerName = "testHandler";
        s.connect(handlerName, v -> ctr.incrementAndGet());
        loop(10).forEach(i -> s.dispatch(null));
        assertEquals(10, ctr.get());
        s.disconnect(handlerName);
        assertEquals(10, ctr.get()); // No change as handler is disconnected
    }

    @Test
    void testNamedHandlersFail() {
        val s = new ConsumingSyncSignal<Void>();

        try {
            s.connect(null, v -> {});
            fail("Should have thrown IllegalArgument exception");
        } catch (IllegalArgumentException e) {
            assertEquals("Handler can neither be null, nor empty", e.getMessage());
        }

        try {
            s.connect("", v -> {});
            fail("Should have thrown IllegalArgument exception");
        } catch (IllegalArgumentException e) {
            assertEquals("Handler can neither be null, nor empty", e.getMessage());
        }

    }

    private void sumTest(ConsumingSyncSignal<Integer> s) {
        final int[] sum = {0};
        loop(10).forEach(i -> s.connect(x -> sum[0] += x));
        printTime(() -> loop(20).forEach(s::dispatch));
        assertEquals(2100, sum[0]);
    }

}