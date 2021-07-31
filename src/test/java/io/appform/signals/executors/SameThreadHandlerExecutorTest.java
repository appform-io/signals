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

package io.appform.signals.executors;

import io.appform.signals.combiners.ConsumingNoOpCombiner;
import io.appform.signals.errorhandlers.LoggingTaskErrorHandler;
import io.appform.signals.signalhandlers.SignalConsumer;
import lombok.val;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

import static io.appform.signals.TestingUtils.loop;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 *
 */
class SameThreadHandlerExecutorTest {

    @Test
    void testExecutor() {
        val e = new SameThreadHandlerExecutor<Integer, Void, SignalConsumer<Integer>>();
        val sum = new AtomicInteger();
        loop(10)
                .forEach(i -> e.execute(
                        Collections.singletonList(sum::addAndGet),
                        i,
                        new ConsumingNoOpCombiner(),
                        new LoggingTaskErrorHandler()));
        assertEquals(55, sum.get());
    }

}