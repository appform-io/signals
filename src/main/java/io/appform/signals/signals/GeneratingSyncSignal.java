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

import io.appform.signals.ResponseCombiner;
import io.appform.signals.Signal;
import io.appform.signals.TaskErrorHandler;
import io.appform.signals.combiners.LastValueResponseCombiner;
import io.appform.signals.errorhandlers.LoggingTaskErrorHandler;
import io.appform.signals.executors.SameThreadHandlerExecutor;
import io.appform.signals.signalhandlers.SignalHandler;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import static io.appform.signals.utils.SignalUtils.requireNonNullElse;

/**
 * A Generating {@link Signal} that fires handlers in the calling thread and waits for them to complete.
 * Generating signals accept a {@link SignalHandler} as handler and every handler invocation is expected to produce a response.
 * Responses from the handlers are sent to {@link io.appform.signals.ResponseCombiner#assimilateHandlerResult(Object)}.
 * Every {@link io.appform.signals.Signal.HandlerGroup} response is also sent to combiner.
 * Any errors are handled by the provided {@link TaskErrorHandler}.
 * Defaults:
 *  - Executor Service - Single thread executor
 *  - Combiner - {@link LastValueResponseCombiner}
 *  - Error Handler - {@link LoggingTaskErrorHandler}
 * For normal usage, use the default constructor. Use the builder to customise.
 */
public class GeneratingSyncSignal<T, R> extends Signal<T, R, SignalHandler<T, R>> {

    public GeneratingSyncSignal() {
        super(new SameThreadHandlerExecutor<>(), new LastValueResponseCombiner<>(), new LoggingTaskErrorHandler());
    }

    public GeneratingSyncSignal(ResponseCombiner<R> combiner, TaskErrorHandler errorHandlingStrategy) {
        super(new SameThreadHandlerExecutor<>(), combiner, errorHandlingStrategy);
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Builder<T, R> extends BuilderBase<T, R, SignalHandler<T, R>, ResponseCombiner<R>, GeneratingSyncSignal<T, R>> {


        public Builder<T, R> combiner(final ResponseCombiner<R> combiner) {
            this.combiner = combiner;
            return this;
        }

        public Builder<T, R> errorHandler(final TaskErrorHandler errorHandler) {
            this.errorHandler = errorHandler;
            return this;
        }

        @Override
        public GeneratingSyncSignal<T, R> build() {

            return new GeneratingSyncSignal<>(
                    requireNonNullElse(combiner, new LastValueResponseCombiner<>()),
                    requireNonNullElse(errorHandler, new LoggingTaskErrorHandler()));
        }
    }

    public static <T, R> Builder<T, R> builder() {
        return new Builder<>();
    }
}
