/*
 * Copyright 2017 Lukáš Petrovický
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.triceo.robozonky.notifications;

import java.time.Duration;
import java.util.UUID;

import org.assertj.core.api.Assertions;
import org.junit.Test;

public class CounterTest {

    @Test
    public void testTiming() throws InterruptedException {
        final int seconds = 1;
        final Counter c = new Counter(UUID.randomUUID().toString(), 1, Duration.ofSeconds(seconds));
        Assertions.assertThat(c.allow()).isTrue();
        c.increase();
        Assertions.assertThat(c.allow()).isFalse();
        Thread.sleep(seconds * 5 * 1000);
        Assertions.assertThat(c.allow()).isTrue();
    }

}
