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

package com.github.triceo.robozonky.app.investing;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.triceo.robozonky.api.Refreshable;
import com.github.triceo.robozonky.api.marketplaces.ExpectedTreatment;
import com.github.triceo.robozonky.api.marketplaces.Marketplace;
import com.github.triceo.robozonky.api.remote.ZonkyApi;
import com.github.triceo.robozonky.api.remote.entities.Investment;
import com.github.triceo.robozonky.api.remote.entities.Loan;
import com.github.triceo.robozonky.api.strategies.InvestmentStrategy;
import com.github.triceo.robozonky.api.strategies.LoanDescriptor;
import com.github.triceo.robozonky.api.strategies.Recommendation;
import com.github.triceo.robozonky.app.ShutdownEnabler;
import com.github.triceo.robozonky.app.authentication.AuthenticationHandler;
import com.github.triceo.robozonky.common.remote.ApiProvider;
import com.github.triceo.robozonky.common.secrets.SecretProvider;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.slf4j.LoggerFactory;

@RunWith(Parameterized.class)
public class DaemonInvestmentModeTest extends AbstractInvestingTest {

    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> getParameters() {
        return Stream.of(ExpectedTreatment.values())
                .map(v -> new Object[] {v})
                .collect(Collectors.toList());
    }

    private static final class TestMarketplace implements Marketplace {

        private Consumer<Collection<Loan>> listener;
        private final Collection<Loan> toReturn;
        private final ExpectedTreatment treatment;
        private boolean closed = false;

        public TestMarketplace(final Collection<Loan> toReturn, final ExpectedTreatment treatment) {
            this.toReturn = toReturn;
            this.treatment = treatment;
        }

        @Override
        public boolean registerListener(final Consumer<Collection<Loan>> listener) {
            this.listener = listener;
            return true;
        }

        @Override
        public ExpectedTreatment specifyExpectedTreatment() {
            return this.treatment;
        }

        public boolean isClosed() {
            return closed;
        }

        @Override
        public void run() {
            this.listener.accept(toReturn);
        }

        @Override
        public void close() throws Exception {
            this.closed = true;
        }
    }

    @Rule
    public final RestoreSystemProperties propertyRestore = new RestoreSystemProperties();

    @Parameterized.Parameter
    public ExpectedTreatment treatment;

    @Before
    public void setupApp() {
        ShutdownEnabler.DAEMON_ALLOWED_TO_TERMINATE.release(); // make sure the daemon is allowed to quit
        DaemonInvestmentMode.BLOCK_UNTIL_RELEASED.release();
    }

    @Test(timeout = 10000)
    public void standardDryRun() throws Exception {
        System.setProperty("robozonky.default.dry_run_balance", String.valueOf(1000)); // no need to mock wallet
        final Loan l = new Loan(1, 10000, OffsetDateTime.now());
        final LoanDescriptor ld = new LoanDescriptor(l);
        final Recommendation r = ld.recommend(200, false).get();
        final DaemonInvestmentModeTest.TestMarketplace m =
                new DaemonInvestmentModeTest.TestMarketplace(Collections.singletonList(l), treatment);
        final InvestmentStrategy ms = Mockito.mock(InvestmentStrategy.class);
        Mockito.when(ms.recommend(ArgumentMatchers.anyCollection(), ArgumentMatchers.any()))
                .thenReturn(Collections.singletonList(r));
        final ApiProvider p = Mockito.mock(ApiProvider.class);
        final ZonkyApi z = Mockito.mock(ZonkyApi.class);
        Mockito.when(z.getLoan(ArgumentMatchers.eq(l.getId()))).thenReturn(l);
        Mockito.when(p.authenticated(ArgumentMatchers.any()))
                .thenReturn(new ApiProvider.ApiWrapper<>(ZonkyApi.class, z));
        Mockito.when(p.oauth()).thenReturn(Mockito.mock(ApiProvider.ApiWrapper.class));
        final Refreshable<InvestmentStrategy> s = Refreshable.createImmutable(ms);
        s.run();
        try (final DaemonInvestmentMode mode = new DaemonInvestmentMode(
                AuthenticationHandler.passwordBased(SecretProvider.fallback("username", new char[0])),
                new ZonkyProxy.Builder().asDryRun(), false, m, s)) {
            final Future<Boolean> wasLockedByUser = Executors.newScheduledThreadPool(1).schedule(() -> {
                LoggerFactory.getLogger(DaemonInvestmentModeTest.class).info("Sending request to terminate.");
                final boolean result = DaemonInvestmentMode.BLOCK_UNTIL_RELEASED.hasQueuedThreads();
                DaemonInvestmentMode.BLOCK_UNTIL_RELEASED.release();
                return result;
            }, 1, TimeUnit.SECONDS);
            final Optional<Collection<Investment>> result = mode.execute(p);
            Assertions.assertThat(wasLockedByUser.get()).isTrue();
            Assertions.assertThat(result).matches(o -> o.map(c -> c.size() == 1).orElse(false));
        } finally {
            Assertions.assertThat(m.isClosed()).isTrue();
        }
    }

}
