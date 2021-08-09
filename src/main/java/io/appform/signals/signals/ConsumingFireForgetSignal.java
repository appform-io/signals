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

import io.appform.signals.Signal;
import io.appform.signals.combiners.ConsumingCombiner;
import io.appform.signals.signalhandlers.SignalConsumer;
import io.appform.signals.TaskErrorHandler;
import io.appform.signals.combiners.ConsumingNoOpCombiner;
import io.appform.signals.errorhandlers.LoggingTaskErrorHandler;
import io.appform.signals.executors.FireForgetHandlerExecutor;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;
import java.util.concurrent.*;

/**
 * A Consuming {@link Signal} that fires handlers in parallel and does not wait for their response.
 * Consuming signals accept a {@link SignalConsumer} as handler and do not respond back with any responses.
 * All {@link io.appform.signals.ResponseCombiner#assimilateHandlerResult(Object)} invocations will receive null.
 * Any errors are handled by the provided {@link TaskErrorHandler}.
 * Defaults:
 *  - Executor Service - Single thread executor
 *  - Combiner - {@link ConsumingNoOpCombiner}
 *  - Error Handler - {@link LoggingTaskErrorHandler}
 * For normal usage, use the default constructor. Use the builder to customise.
 */
@Slf4j
public class ConsumingFireForgetSignal<T> extends Signal<T, Void, SignalConsumer<T>> {
    public ConsumingFireForgetSignal() {
        this(Executors.newSingleThreadExecutor(), new ConsumingNoOpCombiner(), new LoggingTaskErrorHandler());
    }


    public ConsumingFireForgetSignal(
            ExecutorService executorService,
            final ConsumingCombiner combiner,
            final TaskErrorHandler errorHandlingStrategy) {
        super(new FireForgetHandlerExecutor<>(executorService), combiner, errorHandlingStrategy);
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Builder<T> extends BuilderBase<T, Void, SignalConsumer<T>, ConsumingCombiner, ConsumingFireForgetSignal<T>> {

        public Builder<T> executorService(final ExecutorService executorService) {
            this.executorService = executorService;
            return this;
        }

        public Builder<T> combiner(final ConsumingCombiner combiner) {
            this.combiner = combiner;
            return this;
        }

        public Builder<T> errorHandler(final TaskErrorHandler errorHandler) {
            this.errorHandler = errorHandler;
            return this;
        }

        @Override
        public ConsumingFireForgetSignal<T> build() {
            return new ConsumingFireForgetSignal<>(
                    Objects.requireNonNullElse(executorService, Executors.newCachedThreadPool()),
                    Objects.requireNonNullElse(combiner, new ConsumingNoOpCombiner()),
                    Objects.requireNonNullElse(errorHandler, new LoggingTaskErrorHandler()));
        }
    }

    public static <T> Builder<T> builder() {
        return new Builder<>();
    }
}
