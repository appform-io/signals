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

import io.appform.signals.*;
import io.appform.signals.utils.SignalUtils;

import java.util.Collection;

/**
 * An executor that executes handlers in the caller thread
 * and calls {@link ResponseCombiner#assimilateHandlerResult(Object)} on every response from handler.
 * Null responses are ignored.
 */
public class SameThreadHandlerExecutor<T, R, F extends SignalHandlerBase<T, R>> implements HandlerExecutor<T, R, F> {
    @Override
    public R execute(
            Collection<Signal.NamedHandler<F>> handlers,
            T data,
            ResponseCombiner<R> combiner,
            TaskErrorHandler errorHandlingStrategy) {
        handlers.forEach(handler -> SignalUtils.execute(handler, data, combiner, errorHandlingStrategy));
        return combiner.result();
    }
}
