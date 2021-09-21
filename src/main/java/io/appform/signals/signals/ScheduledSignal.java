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
import io.appform.signals.combiners.ConsumingNoOpCombiner;
import io.appform.signals.errorhandlers.LoggingTaskErrorHandler;
import io.appform.signals.executors.SameThreadHandlerExecutor;
import io.appform.signals.signalhandlers.SignalConsumer;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.Closeable;
import java.time.Duration;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static io.appform.signals.utils.SignalUtils.requireNonNullElse;

/**
 * A Consuming {@link Signal} that fires handlers at fixed interval.
 * What this means is that if the handlers take more than the interval time, or at the next scheduled time, whichever is later.
 * There are provisions to specify an initial delay before the handler starts firing. The handler is called in a different thread.
 * In order to stop the thread, close this signal by calling {@link io.appform.signals.signals.ScheduledSignal#close()}.
 * The {@link io.appform.signals.signalhandlers.SignalConsumer} gets the current date as parameter to the method.
 * Any errors are handled by the provided {@link TaskErrorHandler}.
 * Defaults:
 *  - Error Handler - {@link LoggingTaskErrorHandler}
 * For normal usage, use the default constructor. Use the builder to customise.
 */

@Slf4j
public class ScheduledSignal extends Signal<Date, Void, SignalConsumer<Date>> implements Closeable {
    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    private final ScheduledFuture<?> jobFuture;

    public ScheduledSignal(Duration interval) {
        this(Duration.ZERO, interval);
    }

    public ScheduledSignal(Duration initialDelay, Duration interval) {
        this(new LoggingTaskErrorHandler(), initialDelay, interval);
    }

    public ScheduledSignal(
            TaskErrorHandler errorHandlingStrategy,
            Duration initialDelay, Duration interval) {
        super(new SameThreadHandlerExecutor<>(), new ConsumingNoOpCombiner(), errorHandlingStrategy);
        Objects.requireNonNull(initialDelay, "Initial delay is needed for building scheduled signal");
        Objects.requireNonNull(interval, "Interval is needed for building scheduled signal");
        this.jobFuture = this.executorService.scheduleAtFixedRate(() -> {
            try {
                dispatch(new Date());
            }
            catch (Throwable t) {
                log.error("Error calling dispatch: ", t);
            }
        }, initialDelay.toMillis(), interval.toMillis(), TimeUnit.MILLISECONDS);
    }


    @Override
    public void close() {
        this.jobFuture.cancel(true);
        this.executorService.shutdownNow();
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Builder extends BuilderBase<Date, Void, SignalConsumer<Date>, ResponseCombiner<Void>, ScheduledSignal> {

        private Duration initialDelay;
        private Duration interval;

        public Builder initialDelay(Duration initialDelay) {
            this.initialDelay = initialDelay;
            return this;
        }

        public Builder interval(Duration interval) {
            this.interval = interval;
            return this;
        }

        public Builder errorHandler(final TaskErrorHandler errorHandler) {
            this.errorHandler = errorHandler;
            return this;
        }

        @Override
        public ScheduledSignal build() {
            return new ScheduledSignal(
                    requireNonNullElse(errorHandler, new LoggingTaskErrorHandler()),
                    requireNonNullElse(initialDelay, Duration.ZERO),
                    interval);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

}
