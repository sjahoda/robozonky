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

package com.github.triceo.robozonky.notifications.email;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.triceo.robozonky.api.Refreshable;
import com.github.triceo.robozonky.api.ReturnCode;
import com.github.triceo.robozonky.api.notifications.Event;
import com.github.triceo.robozonky.api.notifications.EventListener;
import com.github.triceo.robozonky.api.notifications.ExecutionStartedEvent;
import com.github.triceo.robozonky.api.notifications.InvestmentDelegatedEvent;
import com.github.triceo.robozonky.api.notifications.InvestmentMadeEvent;
import com.github.triceo.robozonky.api.notifications.InvestmentRejectedEvent;
import com.github.triceo.robozonky.api.notifications.InvestmentSkippedEvent;
import com.github.triceo.robozonky.api.notifications.RemoteOperationFailedEvent;
import com.github.triceo.robozonky.api.notifications.RoboZonkyCrashedEvent;
import com.github.triceo.robozonky.api.notifications.RoboZonkyDaemonFailedEvent;
import com.github.triceo.robozonky.api.notifications.RoboZonkyEndingEvent;
import com.github.triceo.robozonky.api.notifications.RoboZonkyInitializedEvent;
import com.github.triceo.robozonky.api.notifications.RoboZonkyTestingEvent;
import com.github.triceo.robozonky.api.remote.entities.Investment;
import com.github.triceo.robozonky.api.remote.entities.Loan;
import com.github.triceo.robozonky.api.remote.enums.Rating;
import com.github.triceo.robozonky.api.strategies.LoanDescriptor;
import com.github.triceo.robozonky.api.strategies.Recommendation;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Mockito;

@RunWith(Parameterized.class)
public abstract class AbstractListenerTest {

    private static EmailNotificationProperties getNotificationProperties() {
        System.setProperty(RefreshableEmailNotificationProperties.CONFIG_FILE_LOCATION_PROPERTY,
                EmailNotificationPropertiesTest.class.getResource("notifications-enabled.cfg").toString());
        final Refreshable<EmailNotificationProperties> r = new RefreshableEmailNotificationProperties();
        r.run();
        final Optional<EmailNotificationProperties> p = r.getLatest();
        System.clearProperty(RefreshableEmailNotificationProperties.CONFIG_FILE_LOCATION_PROPERTY);
        return p.get();
    }

    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> getListeners() {
        // prepare data
        final Loan loan = Mockito.mock(Loan.class);
        Mockito.when(loan.getId()).thenReturn(66666);
        Mockito.when(loan.getDatePublished()).thenReturn(OffsetDateTime.now());
        Mockito.when(loan.getRemainingInvestment()).thenReturn(2000.0);
        Mockito.when(loan.getAmount()).thenReturn(100000.0);
        Mockito.when(loan.getRating()).thenReturn(Rating.AAAAA);
        Mockito.when(loan.getTermInMonths()).thenReturn(25);
        final LoanDescriptor loanDescriptor = new LoanDescriptor(loan);
        final Recommendation recommendation = loanDescriptor.recommend(1200, false).get();
        final Investment i = new Investment(loan, 1000);
        final EmailNotificationProperties properties = AbstractListenerTest.getNotificationProperties();
        // create events for listeners
        final Map<SupportedListener, Event> events = new HashMap<>(SupportedListener.values().length);
        events.put(SupportedListener.INVESTMENT_DELEGATED,
                new InvestmentDelegatedEvent(recommendation, 200, "random"));
        events.put(SupportedListener.INVESTMENT_MADE, new InvestmentMadeEvent(i, 200, true));
        events.put(SupportedListener.INVESTMENT_SKIPPED, new InvestmentSkippedEvent(recommendation));
        events.put(SupportedListener.INVESTMENT_REJECTED, new InvestmentRejectedEvent(recommendation, 200, "random"));
        events.put(SupportedListener.BALANCE_ON_TARGET, new ExecutionStartedEvent("", Collections.emptyList(), 200));
        events.put(SupportedListener.BALANCE_UNDER_MINIMUM, new ExecutionStartedEvent("", Collections.emptyList(), 199));
        events.put(SupportedListener.CRASHED, new RoboZonkyCrashedEvent(ReturnCode.ERROR_UNEXPECTED, new RuntimeException()));
        events.put(SupportedListener.REMOTE_OPERATION_FAILED, new RemoteOperationFailedEvent(new RuntimeException()));
        events.put(SupportedListener.DAEMON_FAILED, new RoboZonkyDaemonFailedEvent(new RuntimeException()));
        events.put(SupportedListener.INITIALIZED, new RoboZonkyInitializedEvent(""));
        events.put(SupportedListener.ENDING, new RoboZonkyEndingEvent(""));
        events.put(SupportedListener.TESTING, new RoboZonkyTestingEvent());
        // create the listeners
        return Stream.of(SupportedListener.values())
                .map(s -> new Object[] {s, s.getListener(properties), events.get(s)})
                .collect(Collectors.toList());
    }

    @Parameterized.Parameter(1)
    public EventListener<Event> listener;
    @Parameterized.Parameter(2)
    public Event event;
    // only exists so that the parameter can have a nice constant description. otherwise PIT will report 0 coverage.
    @Parameterized.Parameter
    public SupportedListener listenerType;

}
