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

import io.appform.signals.combiners.ConsumingCombiner;
import lombok.val;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static io.appform.signals.TestingUtils.loop;
import static io.appform.signals.TestingUtils.printTime;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 *
 */
class ConsumingParallelSignalTest {

    @Test
    void testDefault() {
        val s = new ConsumingParallelSignal<Integer>();
        testSum(s);
    }

    @Test
    void testBuilder() {
        val s = ConsumingParallelSignal.<Integer>builder().build();
        testSum(s);
    }

    @Test
    void testBuilderExecutorService() {
        val s = ConsumingParallelSignal.<Integer>builder()
                .executorService(Executors.newSingleThreadExecutor())
                .build();
        testSum(s);
    }

    @Test
    void testBuilderConsumer() {
        val count = new AtomicInteger();
        val s = ConsumingParallelSignal.<Integer>builder()
                .combiner(new ConsumingCombiner() {
                    @Override
                    public void assimilate(Void data) {
                        count.incrementAndGet();
                    }
                }).build();
        testSum(s);
        assertEquals(220, count.get());
    }

    @Test
    void testBuilderErrorHandler() {
        val errorCounter = new AtomicInteger();
        val s = ConsumingParallelSignal.<Integer>builder()
                .errorHandler(e -> errorCounter.incrementAndGet())
                .build();
        loop(10).forEach(i -> s.connect(x -> {
            if (x % 2 == 0) {
                throw new IllegalStateException();
            }
        }));
        printTime(() -> loop(20).forEach(s::dispatch));
        assertEquals(100, errorCounter.get()); // Five times per dispatch
    }

    private void testSum(ConsumingParallelSignal<Integer> s) {
        val sum = new AtomicInteger();
        loop(10).forEach(i -> s.connect(sum::addAndGet));
        printTime(() -> loop(20).forEach(s::dispatch));
        assertEquals(2100, sum.get());
    }

}