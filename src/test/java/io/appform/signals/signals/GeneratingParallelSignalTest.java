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

import io.appform.signals.Adder;
import lombok.val;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static io.appform.signals.TestingUtils.loop;
import static io.appform.signals.TestingUtils.printTime;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 *
 */
class GeneratingParallelSignalTest {

    @Test
    void testDefault() {
        val s = new GeneratingParallelSignal<Integer, Integer>();
        IntStream.rangeClosed(1, 10).forEach(i -> s.connect(x -> x));
        printTime(() -> assertEquals(210, IntStream.rangeClosed(1, 20).map(s::dispatch).sum()));
        // 210 -> sum of 1..20 as each time combiner returns only the index passed in that dispatch
    }

    @Test
    void testBuilder() {
        val s = GeneratingParallelSignal.<Integer, Integer>builder()
                .combiner(new Adder())
                .build();
        testRun(s);
    }

    @Test
    void testBuilderExec() {
        val s = GeneratingParallelSignal.<Integer, Integer>builder()
                .combiner(new Adder())
                .executorService(Executors.newSingleThreadExecutor())
                .build();
        testRun(s);
    }

    @Test
    void testBuilderException() {
        val errorCounter = new AtomicInteger();
        val s = GeneratingParallelSignal.<Integer, Integer>builder()
                .errorHandler(e -> errorCounter.incrementAndGet())
                .build();
        loop(10).forEach(i -> s.connect(x -> {
            if (x % 2 == 0) {
                throw new IllegalStateException();
            }
            return x;
        }));
        printTime(() -> loop(20).forEach(s::dispatch));
        assertEquals(100, errorCounter.get()); // Five times per dispatch
    }

    private void testRun(GeneratingParallelSignal<Integer, Integer> s) {
        loop(10).forEach(i -> s.connect(x -> x)); //Each step will be 10 * step index
        printTime(() -> assertEquals(15400, IntStream.rangeClosed(1, 20).map(s::dispatch).sum()));
    }

}