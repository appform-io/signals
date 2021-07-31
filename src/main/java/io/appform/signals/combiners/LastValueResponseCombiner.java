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

package io.appform.signals.combiners;

import io.appform.signals.ResponseCombiner;

/**
 * A {@link ResponseCombiner} that stores the last value it encounters. This is the default consumer for Generating Signals.
 */
public class LastValueResponseCombiner<R> implements ResponseCombiner<R> {
    private R current;

    public LastValueResponseCombiner() {
        this(null);
    }

    public LastValueResponseCombiner(R current) {
        this.current = current;
    }

    @Override
    public void assimilate(R data) {
        this.current = data;
    }

    @Override
    public R result() {
        return current;
    }
}
