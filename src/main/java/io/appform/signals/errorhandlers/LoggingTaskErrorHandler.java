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

package io.appform.signals.errorhandlers;

import io.appform.signals.TaskErrorHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * A simple {@link TaskErrorHandler} that prints the exception and suppresses it.
 */
@Slf4j
public class LoggingTaskErrorHandler implements TaskErrorHandler {

    /**
     * Log and suppress the exception thrown
     * @param e Exception thrown by the handler
     */
    @Override
    public void handle(Exception e) {
        log.error("Task error: ", e);
    }
}
