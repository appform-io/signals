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

import lombok.Value;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;

/**
 *
 */
public class Signal<T, R, F extends SignalHandlerBase<T, R>> {
    private final Map<Integer, HandlerGroup<T, R, F>> handlers;
    private final HandlerExecutor<T, R, F> executor;
    private final ResponseCombiner<R> combiner;
    private final TaskErrorHandler errorHandlingStrategy;

    protected Signal(
            HandlerExecutor<T, R, F> executor,
            ResponseCombiner<R> combiner,
            TaskErrorHandler errorHandlingStrategy) {
        this.errorHandlingStrategy = errorHandlingStrategy;
        this.handlers = new TreeMap<>();
        this.combiner = combiner;
        this.executor = executor;
    }

    public Signal<T, R, F> connect(final F handle) {
        return connect(0, handle);
    }

    public synchronized Signal<T, R, F> connect(int groupId, final F handle) {
        handlers.computeIfAbsent(groupId, g -> new HandlerGroup<>(groupId, new ArrayList<>()))
                .add(handle);
        return this;
    }

    public R dispatch(final T data) {
        handlers.values()
                .stream()
                .map(group -> executor.execute(group.getHandlers(), data, combiner, errorHandlingStrategy))
                .forEach(combiner::assimilate);
        return combiner.result();
    }

    protected abstract static  class BuilderBase<
            T,
            R,
            F extends SignalHandlerBase<T, R>,
            C extends ResponseCombiner<R>,
            S extends Signal<T, R, F>> {
        protected ExecutorService executorService;
        protected C combiner;
        protected TaskErrorHandler errorHandler;

        public abstract S build();
    }

    /**
     *
     */
    @Value
    public static class HandlerGroup<T, R, F extends SignalHandlerBase<T,R>> {
        int id;
        List<F> handlers;

        void add(F function) {
            handlers.add(function);
        }
    }
}
