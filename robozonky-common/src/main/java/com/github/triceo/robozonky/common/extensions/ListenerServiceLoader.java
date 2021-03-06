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

package com.github.triceo.robozonky.common.extensions;

import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.github.triceo.robozonky.api.Refreshable;
import com.github.triceo.robozonky.api.notifications.Event;
import com.github.triceo.robozonky.api.notifications.EventListener;
import com.github.triceo.robozonky.api.notifications.ListenerService;
import com.github.triceo.robozonky.util.Scheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ListenerServiceLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger(ListenerServiceLoader.class);
    private static final ServiceLoader<ListenerService> LOADER =
            ExtensionsManager.INSTANCE.getServiceLoader(ListenerService.class);

    private static <T> Stream<T> iteratorToStream(final Iterable<T> iterable) {
        return StreamSupport.stream(iterable.spliterator(), false);
    }

    static <T extends Event> List<Refreshable<EventListener<T>>> load(final Class<T> eventType,
                                                                      final Iterable<ListenerService> loader,
                                                                      final Scheduler scheduler) {
        return ListenerServiceLoader.iteratorToStream(loader)
                .parallel()
                .peek(s -> ListenerServiceLoader.LOGGER.debug("Processing '{}'.", s.getClass()))
                .map(s -> s.findListener(eventType))
                .peek(scheduler::submit)
                .collect(Collectors.toList());
    }

    public static <T extends Event> List<Refreshable<EventListener<T>>> load(final Class<T> eventType) {
        return ListenerServiceLoader.load(eventType, ListenerServiceLoader.LOADER, Scheduler.BACKGROUND_SCHEDULER);
    }

}
