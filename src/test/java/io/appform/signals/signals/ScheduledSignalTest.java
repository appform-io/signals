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

import io.appform.signals.errorhandlers.LoggingTaskErrorHandler;
import lombok.SneakyThrows;
import lombok.val;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 */
class ScheduledSignalTest {

    @Test
    @SneakyThrows
    void testSchedSigDefCons() {
        val s = new ScheduledSignal(Duration.ofSeconds(1));
        val ctr = new AtomicInteger();
        val startTime = new Date();
        s.connect(date -> ctr.getAndIncrement());
        Awaitility.await()
                .timeout(10, TimeUnit.SECONDS)
                .until(() -> ctr.get() > 5);
        val delay = new Date().getTime() - startTime.getTime();
        assertTrue(delay > 5_000 && delay < 7_000);
        s.close();
    }

    @Test
    @SneakyThrows
    void testSchedSig() {
        val s = ScheduledSignal.builder()
                .interval(Duration.ofSeconds(1))
                .errorHandler(new LoggingTaskErrorHandler())
                .build();
        val ctr = new AtomicInteger();
        val startTime = new Date();
        s.connect(date -> ctr.getAndIncrement());
        Awaitility.await()
                .timeout(10, TimeUnit.SECONDS)
                .until(() -> ctr.get() > 5);
        val delay = new Date().getTime() - startTime.getTime();
        assertTrue(delay > 5_000 && delay < 7_000);
        s.close();
    }

    @Test
    void testInitConstructor() {
        val s = new ScheduledSignal(Duration.ofSeconds(1), Duration.ofSeconds(1));
        val ctr = new AtomicInteger();
        val startTime = new Date();
        s.connect(date -> ctr.getAndIncrement());
        Awaitility.await()
                .timeout(10, TimeUnit.SECONDS)
                .until(() -> ctr.get() > 5);
        val delay = new Date().getTime() - startTime.getTime();
        assertTrue(delay > 6_000 && delay < 8_000);
        s.close();
    }

    @Test
    void testInitDelay() {
        val s = ScheduledSignal.builder()
                .initialDelay(Duration.ofSeconds(1))
                .interval(Duration.ofSeconds(1))
                .build();
        val ctr = new AtomicInteger();
        val startTime = new Date();
        s.connect(date -> ctr.getAndIncrement());
        Awaitility.await()
                .timeout(10, TimeUnit.SECONDS)
                .until(() -> ctr.get() > 5);
        val delay = new Date().getTime() - startTime.getTime();
        assertTrue(delay > 6_000 && delay < 8_000);
        s.close();
    }

    @Test
    @SneakyThrows
    void testSchedSigWithProcessingMoreThanInterval() {
        val s = ScheduledSignal.builder()
                .interval(Duration.ofSeconds(1))
                .build();
        val ctr = new AtomicInteger();
        val startTime = new Date();
        s.connect(date -> {
            ctr.getAndIncrement();
            try {
                Thread.sleep(1500);
            }
            catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        Awaitility.await()
                .timeout(15, TimeUnit.SECONDS)
                .until(() -> ctr.get() > 5);
        val delay = new Date().getTime() - startTime.getTime();
        assertTrue(delay > 8_500 && delay < 15_000); //Takes more time
        s.close();
    }

    @Test
    void testNoInterval() {
        try {
            val s = ScheduledSignal.builder()
                    .build();
        } catch (NullPointerException e) {
            assertEquals("Interval is needed for building scheduled signal", e.getMessage());
            return;
        }
        fail("Should have thrown NPE");
    }

    @Test
    void testNoInit() {
        try {
            val s = new ScheduledSignal(new LoggingTaskErrorHandler(), null, Duration.ofSeconds(1));
        } catch (NullPointerException e) {
            assertEquals("Initial delay is needed for building scheduled signal", e.getMessage());
            return;
        }
        fail("Should have thrown NPE");
    }
}