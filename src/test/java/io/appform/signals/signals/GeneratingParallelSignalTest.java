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
        printTime(() -> assertEquals(83881000, IntStream.rangeClosed(1, 20).map(s::dispatch).sum()));

        /*
        83881000 = sum of the following values from each step
        1 2 3 4 5 6 7 8 9 10 20 = 20
        22 24 26 28 30 32 34 36 38 40 80 = 80
        83 86 89 92 95 98 101 104 107 110 220 = 220
        224 228 232 236 240 244 248 252 256 260 520 = 520
        525 530 535 540 545 550 555 560 565 570 1140 = 1140
        1146 1152 1158 1164 1170 1176 1182 1188 1194 1200 2400 = 2400
        2407 2414 2421 2428 2435 2442 2449 2456 2463 2470 4940 = 4940
        4948 4956 4964 4972 4980 4988 4996 5004 5012 5020 10040 = 10040
        10049 10058 10067 10076 10085 10094 10103 10112 10121 10130 20260 = 20260
        20270 20280 20290 20300 20310 20320 20330 20340 20350 20360 40720 = 40720
        40731 40742 40753 40764 40775 40786 40797 40808 40819 40830 81660 = 81660
        81672 81684 81696 81708 81720 81732 81744 81756 81768 81780 163560 = 163560
        163573 163586 163599 163612 163625 163638 163651 163664 163677 163690 327380 = 327380
        327394 327408 327422 327436 327450 327464 327478 327492 327506 327520 655040 = 655040
        655055 655070 655085 655100 655115 655130 655145 655160 655175 655190 1310380 = 1310380
        1310396 1310412 1310428 1310444 1310460 1310476 1310492 1310508 1310524 1310540 2621080 = 2621080
        2621097 2621114 2621131 2621148 2621165 2621182 2621199 2621216 2621233 2621250 5242500 = 5242500
        5242518 5242536 5242554 5242572 5242590 5242608 5242626 5242644 5242662 5242680 10485360 = 10485360
        10485379 10485398 10485417 10485436 10485455 10485474 10485493 10485512 10485531 10485550 20971100 = 20971100
        20971120 20971140 20971160 20971180 20971200 20971220 20971240 20971260 20971280 20971300 41942600 = 41942600
         */
    }

}