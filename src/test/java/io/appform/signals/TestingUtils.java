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

import lombok.experimental.UtilityClass;
import lombok.val;

import java.util.stream.IntStream;

/**
 * Small utilities for testing
 */
@UtilityClass
public class TestingUtils {
    public static void printTime(Runnable runnable) {
        val sw = System.nanoTime();
        runnable.run();
        final double elapsedTime = (System.nanoTime() - sw) / (1e6);
        val e = Thread.currentThread().getStackTrace()[2];
        val className = e.getClassName();
        System.out.printf("Time taken for dispatch in %s::%s : %f ms\n",
                          className.substring(className.lastIndexOf('.') + 1),
                          e.getMethodName(),
                          elapsedTime);
    }

    public static IntStream loop(int i) {
        return IntStream.rangeClosed(1, i);
    }
}
