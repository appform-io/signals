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

import io.appform.signals.HandlerExecutor;
import io.appform.signals.ResponseCombiner;
import io.appform.signals.SignalHandlerBase;
import io.appform.signals.TaskErrorHandler;
import io.appform.signals.utils.SignalUtils;

import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * A {@link HandlerExecutor} that does not respond back.
 * However, all responses are provided to {@link io.appform.signals.ResponseCombiner#assimilateHandlerResult(Object)}
 */
public class FireForgetHandlerExecutor<T, R, F extends SignalHandlerBase<T, R>> implements HandlerExecutor<T, R, F> {

    private final ExecutorService executorService;

    public FireForgetHandlerExecutor(
            ExecutorService executorService) {
        this.executorService = executorService;
    }

    @Override
    public R execute(
            List<F> handlers,
            T data,
            ResponseCombiner<R> combiner,
            TaskErrorHandler errorHandlingStrategy) {
        handlers.forEach(handler -> executorService.execute(
                () -> SignalUtils.execute(handler, data, combiner, errorHandlingStrategy)));
        return null;
    }
}
