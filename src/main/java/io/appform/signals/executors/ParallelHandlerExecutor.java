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
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 * An executor that executes handlers in parallel
 * and calls {@link ResponseCombiner#assimilateHandlerResult(Object)} on every response from handler.
 * Null responses are ignored.
 */
@Slf4j
public class ParallelHandlerExecutor<T, R, F extends SignalHandlerBase<T, R>> implements HandlerExecutor<T, R, F> {
    private final ExecutorService executorService;

    public ParallelHandlerExecutor(ExecutorService executorService) {
        this.executorService = executorService;
    }

    @Override
    public R execute(
            Collection<Signal.NamedHandler<F>> handlers,
            T data,
            ResponseCombiner<R> combiner,
            TaskErrorHandler errorHandlingStrategy) {
        val c = new ExecutorCompletionService<R>(executorService);
        val futures = handlers.stream()
                .map(handler -> c.submit(() -> (R) SignalUtils.execute(handler, data, combiner, errorHandlingStrategy)))
                .collect(Collectors.toList());
        //Please do not combine the two by removing the collection, it will serialise the operations
        futures.forEach(f -> result(errorHandlingStrategy, f));
        return combiner.result();
    }

    private void result(TaskErrorHandler errorHandlingStrategy, Future<R> f) {
        try {
            f.get();
        }
        catch (ExecutionException e) {
            errorHandlingStrategy.handle(e);
        }
        catch (InterruptedException e) {
            log.error("Thread has been interrupted...");
            Thread.currentThread().interrupt();
        }
    }
}
