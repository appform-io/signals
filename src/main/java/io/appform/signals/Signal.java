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
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ExecutorService;

import static io.appform.signals.utils.SignalUtils.isEmpty;

/**
 * Top level signal abstraction. The actual classes are derived from this class.
 */
@Slf4j
public abstract class Signal<T, R, F extends SignalHandlerBase<T, R>> {
    private static final int DEFAULT_GROUP = 0;

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

    /**
     * Connect a handler of type {@link SignalHandlerBase} to this signal. All handlers connected by this method get
     * allocated to the first group (0).
     *
     * @param handler A signal handler
     * @return This same signal, for chaining
     */
    public final Signal<T, R, F> connect(final F handler) {
        return connect(DEFAULT_GROUP, handler);
    }

    /**
     * Connect a handler to this signal at a specific grouping. Grouping can be used to order between multiple sets of
     * handlers that can be executed in parallel. Execution of groups is ordered by the group id. Multiple calls with
     * same grouping id will add the handlers to the same group.
     *
     * @param groupId Group id to be assigned to.
     * @param handler A signal handler
     * @return This same signal, for chaining
     */
    public final synchronized Signal<T, R, F> connect(int groupId, final F handler) {
        handlers.computeIfAbsent(groupId, g -> new HandlerGroup<>(groupId, new ArrayList<>()))
                .add(UUID.randomUUID().toString(), handler);
        return this;
    }

    /**
     * Connect a handler to this signal at a specific grouping. Grouping can be used to order between multiple sets of
     * handlers that can be executed in parallel. Execution of groups is ordered by the group id. Multiple calls with
     * same grouping id will add the handlers to the same group.
     *
     * @param handlerName Name to identify the handler
     * @param handler A signal handler
     * @return This same signal, for chaining
     */
    public final synchronized Signal<T, R, F> connect(final String handlerName, final F handler) {
        return connect(DEFAULT_GROUP, handlerName, handler);
    }

    /**
     * Connect a handler to this signal at a specific grouping. Grouping can be used to order between multiple sets of
     * handlers that can be executed in parallel. Execution of groups is ordered by the group id. Multiple calls with
     * same grouping id will add the handlers to the same group.
     *
     * @param groupId Group id to be assigned to.
     * @param handler A signal handler
     * @return This same signal, for chaining
     */
    public final synchronized Signal<T, R, F> connect(int groupId, final String handlerName, final F handler) {
        checkHandlerName(handlerName);
        handlers.computeIfAbsent(groupId, g -> new HandlerGroup<>(groupId, new ArrayList<>()))
                .add(handlerName, handler);
        return this;
    }

    /**
     * Disconnect a named handler from the signal from the default group
     * @param handlerName Name that identifies a handler
     * @return This same signal, for chaining
     */
    public final synchronized Signal<T,R,F> disconnect(final String handlerName) {
        return disconnect(DEFAULT_GROUP, handlerName);
    }

    /**
     * Disconnect a named handler from the signal.
     * @param handlerName Name that identifies a handler
     * @return This same signal, for chaining
     */
    public final synchronized Signal<T,R,F> disconnect(int groupId, final String handlerName) {
        handlers.computeIfPresent(groupId, (k,v) -> {
            v.remove(handlerName);
            return v;
        });
        return this;
    }

    /**
     * Trigger the signal with the data. Handlers will get called according to how they have been connected and how the
     * executors are being setup.
     *
     * @param data The data to be passed to the signal handler
     * @return Response from calling the handlers after they pass through the combiner
     */
    public final R dispatch(final T data) {
        handlers.values()
                .stream()
                .map(group -> executor.execute(group.getHandlers(), data, combiner, errorHandlingStrategy))
                .forEach(combiner::assimilateGroupResult);
        return combiner.result();
    }

    /**
     * Base class for providing a builder for configuring subtypes of the Signal class.
     * @param <T> Type of parameter to handler
     * @param <R> Return type from handler
     * @param <F> Type fo signal handler
     * @param <C> Combiner used to combine the results
     * @param <S> Subtype for the signal class
     */
    protected abstract static class BuilderBase<
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

    @Value
    public static class NamedHandler<F> {
        String name;
        F handler;
    }

    /**
     * A group of handlers. All handlers in a group are considered to be equivalent and might be executed in parallel
     * depending on the executor implementation provided.
     */
    @Value
    private static class HandlerGroup<T, R, F extends SignalHandlerBase<T, R>> {
        int id;
        List<NamedHandler<F>> handlers;

        void add(String handlerName, F function) {
            checkHandlerName(handlerName);
            handlers.add(new NamedHandler<>(handlerName, function));
        }

        void remove(final String handlerName) {
            checkHandlerName(handlerName);
            handlers.removeIf(t -> t.getName().equals(handlerName));
        }
    }

    /**
     * Ensures handler name is not null or empty
     * @param handlerName The name ot identify the handler
     */
    private static void checkHandlerName(String handlerName) {
        if(isEmpty(handlerName)) {
            throw new IllegalArgumentException("Handler can neither be null, nor empty");
        }
    }
}
