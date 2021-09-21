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

import io.appform.signals.signals.GeneratingSyncSignal;
import lombok.val;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 *
 */
class GroupingTest {
    @Test
    void testSignal() {
        val s = GeneratingSyncSignal.<String, String>builder()
                .combiner(new ResponseCombiner<String>() {
                    String value = "";

                    @Override
                    public void assimilateHandlerResult(String data) {
                        value += data;
                        System.out.println(value);
                    }

                    @Override
                    public void assimilateGroupResult(String data) {

                    }

                    @Override
                    public String result() {
                        return value;
                    }
                })
                .build();
        s.connect(v -> v + " 1-1 ")
                .connect(v -> v + " 1-2 ")
                .connect(1, v -> v + "2-1")
                .connect(1, v -> v + "2-2");
        assertEquals("test 1-1 test 1-2 test2-1test2-2", s.dispatch("test"));
    }
}
