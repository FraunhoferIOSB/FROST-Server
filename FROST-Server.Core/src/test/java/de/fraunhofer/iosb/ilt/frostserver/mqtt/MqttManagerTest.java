/*
 * Copyright (C) 2016 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
 * Karlsruhe, Germany.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.fraunhofer.iosb.ilt.frostserver.mqtt;

import de.fraunhofer.iosb.ilt.frostserver.settings.UnknownVersionException;
import de.fraunhofer.iosb.ilt.frostserver.settings.Version;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author jab
 */
public class MqttManagerTest {

    @Test
    public void testVersionParse() throws UnknownVersionException {
        Assert.assertEquals(Version.V_1_0, MqttManager.getVersionFromTopic("v1.0/Observations"));
        Assert.assertEquals(Version.V_1_1, MqttManager.getVersionFromTopic("v1.1/Observations"));
    }

    @Test(expected = UnknownVersionException.class)
    public void testVersionParseFail() throws UnknownVersionException {
        MqttManager.getVersionFromTopic("v1.9/Observations");
    }

}
