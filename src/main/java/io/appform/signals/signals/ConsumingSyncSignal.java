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
import io.appform.signals.TaskErrorHandler;
import io.appform.signals.combiners.ConsumingCombiner;
import io.appform.signals.combiners.ConsumingNoOpCombiner;
import io.appform.signals.errorhandlers.LoggingTaskErrorHandler;
import io.appform.signals.executors.SameThreadHandlerExecutor;
import io.appform.signals.signalhandlers.SignalConsumer;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Objects;

/**
 * A Consuming {@link Signal} that fires handlers in the same thread waits for them to complete.
 * Consuming signals accept a {@link SignalConsumer} as handler and do not respond back with any responses.
 * All {@link io.appform.signals.ResponseCombiner#assimilate(Object)} invocations will receive null.
 * Any errors are handled by the provided {@link TaskErrorHandler}.
 * Defaults:
 *  - Combiner - {@link ConsumingNoOpCombiner}
 *  - Error Handler - {@link LoggingTaskErrorHandler}
 * For normal usage, use the default constructor. Use the builder to customise.
 */
public class ConsumingSyncSignal<T> extends Signal<T, Void, SignalConsumer<T>> {
    public ConsumingSyncSignal() {
        this(new ConsumingNoOpCombiner(), new LoggingTaskErrorHandler());
    }

    public ConsumingSyncSignal(final ConsumingCombiner combiner, final TaskErrorHandler errorHandler) {
        super(new SameThreadHandlerExecutor<>(), combiner, errorHandler);
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Builder<T> extends BuilderBase<T,Void, SignalConsumer<T>, ConsumingCombiner, ConsumingSyncSignal<T>> {

        public Builder<T> combiner(final ConsumingCombiner combiner) {
            this.combiner = combiner;
            return this;
        }

        public Builder<T> errorHandler(final TaskErrorHandler errorHandler) {
            this.errorHandler = errorHandler;
            return this;
        }

        @Override
        public ConsumingSyncSignal<T> build() {
            return new ConsumingSyncSignal<>(
                    Objects.requireNonNullElse(combiner, new ConsumingNoOpCombiner()),
                    Objects.requireNonNullElse(errorHandler, new LoggingTaskErrorHandler()));
        }
    }

    public static <T> Builder<T> builder() {
        return new Builder<>();
    }

}
