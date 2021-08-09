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

package io.appform.signals.combiners;

import lombok.val;
import org.junit.jupiter.api.Test;

import static io.appform.signals.TestingUtils.loop;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 *
 */
class ConsumingCombinerTest {

    @Test
    void testCombiner() {
        final int [] sum = new int[1];
        val c = new ConsumingCombiner() {

            @Override
            public void assimilateHandlerResult(Void data) {
                sum[0]++;
            }

            @Override
            public void assimilateGroupResult(Void data) {
                sum[0]++;
            }
        };
        loop(10).forEach(i -> c.assimilateHandlerResult(null));
        assertEquals(10, sum[0]);
    }

}