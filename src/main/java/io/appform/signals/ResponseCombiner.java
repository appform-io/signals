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

/**
 * Combines results from signal handlers and groups
 */
public interface ResponseCombiner<R> {
    /**
     * Assimilate result from a call to {@link SignalHandlerBase#handle(Object)}.
     * @param data The result from handler call
     */
    void assimilateHandlerResult(R data);

    /**
     * Signal handlers can be registered in groups. This function is used to assimilate results from the processing
     * of a group.
     * @param data The result from execution of a group oh handlers
     */
    default void assimilateGroupResult(R data) {
        //Do nothing here
    }

    /**
     * Result of the computation. When using Generating Signal, this is the value that gets returned.
     * @return The result of computation implemented in the combiner
     */
    R result();

}
