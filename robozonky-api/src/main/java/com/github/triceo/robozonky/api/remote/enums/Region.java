/*
 * Copyright 2016 Lukáš Petrovický
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

package com.github.triceo.robozonky.api.remote.enums;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link #UNKNOWN} must always come last - it is an internal value, not in the Zonky API, and therefore must only get
 * its integer ID after all other values already got one.
 */
@JsonDeserialize(using = Region.RegionDeserializer.class)
public enum Region {

    PRAHA, STREDOCESKY, JIHOCESKY, PLZENSKY, KARLOVARSKY, USTECKY, LIBERECKY, KRALOVEHRADECKY, PARDUBICKY, VYSOCINA,
    JIHOMORAVSKY, OLOMOUCKY, MORAVSKOSLEZSKY, ZLINSKY, UNKNOWN;

    private static final Logger LOGGER = LoggerFactory.getLogger(Region.class);

    static class RegionDeserializer extends JsonDeserializer<Region> {

        @Override
        public Region deserialize(final JsonParser jsonParser, final DeserializationContext deserializationContext)
                throws IOException {
            final String id = jsonParser.getText();
            try {
                final int actualId = Integer.parseInt(id) - 1; // regions in Zonky API are indexed from 1
                return Region.values()[actualId];
            } catch (final RuntimeException ex) { // whatever went wrong, don't fail on this unimportant enum
                Region.LOGGER.warn("Unknown value '{}', API may be incomplete.", id);
                return Region.UNKNOWN;
            }
        }

    }

}
