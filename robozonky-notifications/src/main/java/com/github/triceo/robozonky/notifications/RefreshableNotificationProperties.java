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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.github.triceo.robozonky.api.Refreshable;
import com.github.triceo.robozonky.internal.api.Defaults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class RefreshableNotificationProperties<N extends NotificationProperties> extends Refreshable<N> {

    private static String readUrl(final URL url) throws IOException {
        try (final BufferedReader r = new BufferedReader(new InputStreamReader(url.openStream(), Defaults.CHARSET))) {
            return r.lines().collect(Collectors.joining(System.lineSeparator()));
        }
    }

    protected final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    abstract protected N newNotificationProperties(final Properties properties);

    abstract protected String getConfigFileLocationPropertyName();

    abstract protected File getDefaultConfigFileLocation();

    @Override
    public Optional<N> transform(final String source) {
        try (final ByteArrayInputStream baos = new ByteArrayInputStream(source.getBytes(Defaults.CHARSET))) {
            final Properties p = new Properties();
            p.load(baos);
            return Optional.of(this.newNotificationProperties(p));
        } catch (final IOException ex) {
            LOGGER.warn("Failed transforming source.", ex);
            return Optional.empty();
        }
    }

    @Override
    public Supplier<Optional<String>> getLatestSource() {
        return this::getPropertiesContents;
    }

    private Optional<String> getPropertiesContents() {
        final String propValue = System.getProperty(this.getConfigFileLocationPropertyName());
        if (propValue != null) { // attempt to read from the URL specified by the property
            LOGGER.debug("Reading e-mail notification configuration from {}.", propValue);
            try {
                return Optional.of(RefreshableNotificationProperties.readUrl(new URL(propValue)));
            } catch (final IOException ex) {
                // fall back to the property file
                LOGGER.debug("Failed reading configuration from {}.", propValue);
            }
        }
        final File defaultConfigFile = this.getDefaultConfigFileLocation();
        if (defaultConfigFile.canRead()) {
            try {
                LOGGER.debug("Read config file {}.", defaultConfigFile.getAbsolutePath());
                final URL u = defaultConfigFile.toURI().toURL();
                final String content = RefreshableNotificationProperties.readUrl(u);
                return Optional.of(content);
            } catch (final IOException ex) {
                LOGGER.debug("Failed reading configuration file {}.", defaultConfigFile, ex);
                return Optional.empty();
            }
        } else {
            LOGGER.debug("No configuration file found.");
            return Optional.empty();
        }
    }

    @Override
    public Optional<Refreshable<?>> getDependedOn() {
        return Optional.empty();
    }

}
