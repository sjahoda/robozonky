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

package com.github.triceo.robozonky.installer.panels;

import java.io.File;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.triceo.robozonky.common.extensions.Checker;
import com.github.triceo.robozonky.notifications.email.RefreshableEmailNotificationProperties;
import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.installer.DataValidator;

public class EmailSettingsValidator implements DataValidator {

    private static final Logger LOGGER = Logger.getLogger(EmailSettingsValidator.class.getCanonicalName());

    @Override
    public DataValidator.Status validateData(final InstallData installData) {
        try {
            // configure e-mail notification properties
            final Properties emailConfig = Util.configureEmailNotifications(installData);
            final File emailConfigTarget = File.createTempFile("robozonky-", ".cfg");
            Util.writeOutProperties(emailConfig, emailConfigTarget);
            System.setProperty(RefreshableEmailNotificationProperties.CONFIG_FILE_LOCATION_PROPERTY,
                    emailConfigTarget.toURI().toURL().toExternalForm());
            return Checker.notifications() ? DataValidator.Status.OK : DataValidator.Status.ERROR;
        } catch (final Exception ex) {
            EmailSettingsValidator.LOGGER.log(Level.WARNING, "Failed sending e-mail.", ex);
            return DataValidator.Status.WARNING;
        } finally {
            System.clearProperty(RefreshableEmailNotificationProperties.CONFIG_FILE_LOCATION_PROPERTY);
        }
    }

    @Override
    public String getErrorMessageId() {
        return "Konfigurace se nezdařila. Pravděpodobně se jedná o chybu v RoboZonky.";
    }

    @Override
    public String getWarningMessageId() {
        return "Došlo k chybě při komunikaci s SMTP " +
                "E-mailové notifikace nemusí fungovat správně.";
    }

    @Override
    public boolean getDefaultAnswer() {
        return false;
    }
}
