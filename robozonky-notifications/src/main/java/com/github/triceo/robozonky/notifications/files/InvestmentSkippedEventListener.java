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

package com.github.triceo.robozonky.notifications.files;

import com.github.triceo.robozonky.api.notifications.InvestmentSkippedEvent;

final class InvestmentSkippedEventListener extends AbstractFileStoringListener<InvestmentSkippedEvent> {

    protected InvestmentSkippedEventListener(final ListenerSpecificNotificationProperties properties) {
        super(properties);
    }

    @Override
    int getLoanId(final InvestmentSkippedEvent event) {
        return event.getRecommendation().getLoanDescriptor().getLoan().getId();
    }

    @Override
    int getAmount(final InvestmentSkippedEvent event) {
        return event.getRecommendation().getRecommendedInvestmentAmount();
    }

    @Override
    String getSuffix(InvestmentSkippedEvent event) {
        return "skipped";
    }
}
