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

package io.appform.signals;

import io.appform.signals.signals.*;
import lombok.val;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 *
 */
class SignalTest {

    @Test
    void testSyncConsumer() {
        val s = ConsumingSyncSignal.<Integer>builder()
                .build();
        final int[] sum = {0};
        IntStream.rangeClosed(1, 10).forEach(i -> s.connect(x -> sum[0] += x));
        TestingUtils.printTime(() -> IntStream.rangeClosed(1, 20).forEach(s::dispatch));
        assertEquals(2100, sum[0]);
    }

    @Test
    void testParallelConsumer() {
        val s = ConsumingParallelSignal.<Integer>builder()
                .executorService(Executors.newFixedThreadPool(2))
                .build();
        val sum = new AtomicInteger();
        IntStream.rangeClosed(1, 10)
                .forEach(i -> s.connect(sum::addAndGet));
/*        IntStream.rangeClosed(1, 10)
                .forEach(i -> s.connect(x -> System.out.println(i + "->" + x)));*/
        TestingUtils.printTime(() -> IntStream.rangeClosed(1, 20).forEach(s::dispatch));
        assertEquals(2100, sum.get());
    }

    @Test
    void testFireForgetConsumer() {
        val s = new ConsumingFireForgetSignal<Integer>();
        val sum = new AtomicInteger();
        IntStream.rangeClosed(1, 10)
                .forEach(i -> s.connect(sum::addAndGet));
/*        IntStream.rangeClosed(1, 10)
                .forEach(i -> s.connect(x -> System.out.println(i + "->" + x)));*/
        TestingUtils.printTime(() -> IntStream.rangeClosed(1, 20).forEach(s::dispatch));
        Awaitility.await()
                .timeout(3, TimeUnit.SECONDS)
                .until(() -> sum.get() == 2100);
        assertEquals(2100, sum.get());
    }

    @Test
    void testGenericSyncSignal() {
        val s = GeneratingSyncSignal.<Integer, Integer>builder()
                .combiner(new SimpleAdder())
                .build();
        IntStream.rangeClosed(1, 10).forEach(i -> s.connect(x -> x));
        TestingUtils.printTime(() -> assertEquals(83881000, IntStream.rangeClosed(1, 20).map(s::dispatch).sum()));
    }

    @Test
    void testGenericParallelSignal() {
        val s = GeneratingParallelSignal.<Integer, Integer>builder()
                .combiner(new Adder())
                .build();
        IntStream.rangeClosed(1, 10).forEach(i -> s.connect(x -> x));
        TestingUtils.printTime(() -> assertEquals(83881000, IntStream.rangeClosed(1, 20).map(s::dispatch).sum()));
    }

    @Test
    void testGenericParallelSignalExec() {
        val s = GeneratingParallelSignal.<Integer, Integer>builder()
                .executorService(Executors.newSingleThreadExecutor())
                .combiner(new Adder())
                .build();
        IntStream.rangeClosed(1, 10).forEach(i -> s.connect(x -> x));
        TestingUtils.printTime(() -> assertEquals(83881000, IntStream.rangeClosed(1, 20).map(s::dispatch).sum()));
    }

}