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

package io.appform.signals.utils;

import io.appform.signals.ResponseCombiner;
import io.appform.signals.SignalHandlerBase;
import io.appform.signals.TaskErrorHandler;
import lombok.experimental.UtilityClass;
import lombok.val;

/**
 *
 */
@UtilityClass
public class SignalUtils {
    public static <T, R, F extends SignalHandlerBase<T, R>> R execute(
            final F handler,
            T data,
            ResponseCombiner<R> combiner,
            TaskErrorHandler errorHandlingStrategy) {
        try {
            val response = handler.handle(data);
            combiner.assimilateHandlerResult(response);
            return response;
        }
        catch (Exception e) {
            errorHandlingStrategy.handle(e);
        }
        return null;
    }

    public static <T> T requireNonNullElse(T original, T defaultValue) {
        return null == original ? defaultValue : original;
    }
}
