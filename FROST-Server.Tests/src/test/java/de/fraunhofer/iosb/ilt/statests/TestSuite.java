/*
 * Copyright (C) 2024 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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
package de.fraunhofer.iosb.ilt.statests;

import static de.fraunhofer.iosb.ilt.statests.TestCore.getInstance;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import de.fraunhofer.iosb.ilt.statests.TestSuite.SuiteFinaliser1;
import de.fraunhofer.iosb.ilt.statests.TestSuite.SuiteFinaliser2;
import de.fraunhofer.iosb.ilt.statests.c01sensingcore.Capability1CoreOnlyTests10;
import de.fraunhofer.iosb.ilt.statests.c01sensingcore.Capability1CoreOnlyTests11;
import de.fraunhofer.iosb.ilt.statests.c01sensingcore.Capability1Tests10;
import de.fraunhofer.iosb.ilt.statests.c01sensingcore.Capability1Tests11;
import de.fraunhofer.iosb.ilt.statests.c02cud.AdditionalTests10;
import de.fraunhofer.iosb.ilt.statests.c02cud.AdditionalTests11;
import de.fraunhofer.iosb.ilt.statests.c02cud.Capability2Tests10;
import de.fraunhofer.iosb.ilt.statests.c02cud.Capability2Tests11;
import de.fraunhofer.iosb.ilt.statests.c02cud.DeleteFilterTests10;
import de.fraunhofer.iosb.ilt.statests.c02cud.DeleteFilterTests11;
import de.fraunhofer.iosb.ilt.statests.c02cud.JsonPatchTests10;
import de.fraunhofer.iosb.ilt.statests.c02cud.JsonPatchTests11;
import de.fraunhofer.iosb.ilt.statests.c02cud.ResultTypesTests10;
import de.fraunhofer.iosb.ilt.statests.c02cud.ResultTypesTests11;
import de.fraunhofer.iosb.ilt.statests.c03filtering.Capability3Tests10;
import de.fraunhofer.iosb.ilt.statests.c03filtering.Capability3Tests11;
import de.fraunhofer.iosb.ilt.statests.c03filtering.DateTimeTests10;
import de.fraunhofer.iosb.ilt.statests.c03filtering.DateTimeTests11;
import de.fraunhofer.iosb.ilt.statests.c03filtering.FilterTests10;
import de.fraunhofer.iosb.ilt.statests.c03filtering.FilterTests11;
import de.fraunhofer.iosb.ilt.statests.c03filtering.GeoTests10;
import de.fraunhofer.iosb.ilt.statests.c03filtering.GeoTests11;
import de.fraunhofer.iosb.ilt.statests.c03filtering.JsonPropertiesTests10;
import de.fraunhofer.iosb.ilt.statests.c03filtering.JsonPropertiesTests11;
import de.fraunhofer.iosb.ilt.statests.c04batch.BatchTests10;
import de.fraunhofer.iosb.ilt.statests.c04batch.BatchTests11;
import de.fraunhofer.iosb.ilt.statests.c05multidatastream.MdDateTimeTests10;
import de.fraunhofer.iosb.ilt.statests.c05multidatastream.MdDateTimeTests11;
import de.fraunhofer.iosb.ilt.statests.c05multidatastream.MultiDatastreamObsPropTests10;
import de.fraunhofer.iosb.ilt.statests.c05multidatastream.MultiDatastreamObsPropTests11;
import de.fraunhofer.iosb.ilt.statests.c05multidatastream.MultiDatastreamTests10;
import de.fraunhofer.iosb.ilt.statests.c05multidatastream.MultiDatastreamTests11;
import de.fraunhofer.iosb.ilt.statests.c06dataarrays.DataArrayTests10;
import de.fraunhofer.iosb.ilt.statests.c06dataarrays.DataArrayTests11;
import de.fraunhofer.iosb.ilt.statests.c07mqttcreate.Capability7Tests10;
import de.fraunhofer.iosb.ilt.statests.c07mqttcreate.Capability7Tests11;
import de.fraunhofer.iosb.ilt.statests.c08mqttsubscribe.MqttCoreTests11;
import de.fraunhofer.iosb.ilt.statests.c08mqttsubscribe.MqttExtraTests11;
import de.fraunhofer.iosb.ilt.statests.f01auth.BasicAuthAnonReadTests10;
import de.fraunhofer.iosb.ilt.statests.f01auth.BasicAuthAnonReadTests11;
import de.fraunhofer.iosb.ilt.statests.f01auth.BasicAuthCryptPwTests10;
import de.fraunhofer.iosb.ilt.statests.f01auth.BasicAuthCryptPwTests11;
import de.fraunhofer.iosb.ilt.statests.f01auth.BasicAuthTests10;
import de.fraunhofer.iosb.ilt.statests.f01auth.BasicAuthTests11;
import de.fraunhofer.iosb.ilt.statests.f01auth.FineGrainedAuthTestsBasic11;
import de.fraunhofer.iosb.ilt.statests.f01auth.FineGrainedAuthTestsKeycloak11;
import de.fraunhofer.iosb.ilt.statests.f01auth.FineGrainedAuthTestsKeycloakAnon11;
import de.fraunhofer.iosb.ilt.statests.f01auth.KeyCloakAnonReadTests10;
import de.fraunhofer.iosb.ilt.statests.f01auth.KeyCloakAnonReadTests11;
import de.fraunhofer.iosb.ilt.statests.f01auth.KeyCloakTests10;
import de.fraunhofer.iosb.ilt.statests.f01auth.KeyCloakTests11;
import de.fraunhofer.iosb.ilt.statests.f01auth.ProjectAuthTestsBasic11;
import de.fraunhofer.iosb.ilt.statests.f01auth.ProjectAuthTestsBasicAnon11;
import de.fraunhofer.iosb.ilt.statests.f02customlinks.CustomLinksTests10;
import de.fraunhofer.iosb.ilt.statests.f02customlinks.CustomLinksTests11;
import de.fraunhofer.iosb.ilt.statests.f03metadata.MetadataTests10;
import de.fraunhofer.iosb.ilt.statests.f03metadata.MetadataTests11;
import de.fraunhofer.iosb.ilt.statests.util.HTTPMethods;
import de.fraunhofer.iosb.ilt.statests.v2cud.GeoTests20;
import de.fraunhofer.iosb.ilt.statests.v2cud.MqttCoreTests20;
import de.fraunhofer.iosb.ilt.statests.v2cud.MqttRequestResponse20;
import de.fraunhofer.iosb.ilt.statests.v2cud.ReferenceTests20;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Update the nested SuiteFinaliser classes when updating or adding tests.
 */
@SelectClasses({
    Capability1CoreOnlyTests10.class,
    Capability1CoreOnlyTests11.class,
    Capability1Tests10.class,
    Capability1Tests11.class,
    Capability2Tests10.class,
    Capability2Tests11.class,
    AdditionalTests10.class,
    AdditionalTests11.class,
    DeleteFilterTests10.class,
    DeleteFilterTests11.class,
    JsonPatchTests10.class,
    JsonPatchTests11.class,
    ResultTypesTests10.class,
    ResultTypesTests11.class,
    Capability3Tests10.class,
    Capability3Tests11.class,
    DateTimeTests10.class,
    DateTimeTests11.class,
    FilterTests10.class,
    FilterTests11.class,
    GeoTests10.class,
    GeoTests11.class,
    JsonPropertiesTests10.class,
    JsonPropertiesTests11.class,
    BatchTests10.class,
    BatchTests11.class,
    MultiDatastreamTests10.class,
    MultiDatastreamTests11.class,
    MultiDatastreamObsPropTests10.class,
    MultiDatastreamObsPropTests11.class,
    MdDateTimeTests10.class,
    MdDateTimeTests11.class,
    DataArrayTests10.class,
    DataArrayTests11.class,
    Capability7Tests10.class,
    Capability7Tests11.class,
    MqttCoreTests11.class,
    MqttExtraTests11.class,
    CustomLinksTests10.class,
    CustomLinksTests11.class,
    MetadataTests10.class,
    MetadataTests11.class,
    BasicAuthTests10.class,
    BasicAuthTests11.class,
    BasicAuthAnonReadTests10.class,
    BasicAuthAnonReadTests11.class,
    BasicAuthCryptPwTests10.class,
    BasicAuthCryptPwTests11.class,
    KeyCloakTests10.class,
    KeyCloakTests11.class,
    KeyCloakAnonReadTests10.class,
    KeyCloakAnonReadTests11.class,
    FineGrainedAuthTestsBasic11.class,
    FineGrainedAuthTestsKeycloak11.class,
    FineGrainedAuthTestsKeycloakAnon11.class,
    ProjectAuthTestsBasic11.class,
    ProjectAuthTestsBasicAnon11.class,
    SuiteFinaliser1.class,
    ReferenceTests20.class,
    GeoTests20.class,
    MqttCoreTests20.class,
    MqttRequestResponse20.class,
    SuiteFinaliser2.class
})
@Suite
class TestSuite {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestSuite.class.getName());

    @BeforeAll
    static void setUpClass() {
        LOGGER.info("Starting Servers...");
    }

    @AfterAll
    static void tearDownClass() {
        HTTPMethods.logStats();
        LOGGER.info("Stopping Servers...");
        getInstance().stopAllServers();
    }

    static class SuiteFinaliser1 {

        @Test
        void finalTest() {
            LOGGER.info("Stopping Servers...");
            assertDoesNotThrow(() -> {
                getInstance().stopAllServers();
            });
            HTTPMethods.logStats();
            final int countDelete = HTTPMethods.getCountDelete();
            final int countGet = HTTPMethods.getCountGet();
            final int countPatch = HTTPMethods.getCountPatch();
            final int countPost = HTTPMethods.getCountPost();
            final int countPut = HTTPMethods.getCountPut();
            HTTPMethods.resetStats();
            Assertions.assertEquals(190, countDelete, "Unexpected number of DELETE calls.");
            Assertions.assertEquals(6773, countGet, "Unexpected number of GET calls.");
            Assertions.assertEquals(46, countPatch, "Unexpected number of PATCH calls.");
            Assertions.assertEquals(204, countPost, "Unexpected number of POST calls.");
            Assertions.assertEquals(64, countPut, "Unexpected number of PUT calls.");
        }
    }

    static class SuiteFinaliser2 {

        @Test
        void finalTest() {
            LOGGER.info("Stopping Servers...");
            assertDoesNotThrow(() -> {
                getInstance().stopAllServers();
            });
            HTTPMethods.logStats();
            final int countDelete = HTTPMethods.getCountDelete();
            final int countGet = HTTPMethods.getCountGet();
            final int countPatch = HTTPMethods.getCountPatch();
            final int countPost = HTTPMethods.getCountPost();
            final int countPut = HTTPMethods.getCountPut();
            HTTPMethods.resetStats();
            Assertions.assertEquals(6, countDelete, "Unexpected number of DELETE calls.");
            Assertions.assertEquals(208, countGet, "Unexpected number of GET calls.");
            Assertions.assertEquals(0, countPatch, "Unexpected number of PATCH calls.");
            Assertions.assertEquals(3, countPost, "Unexpected number of POST calls.");
            Assertions.assertEquals(48, countPut, "Unexpected number of PUT calls.");
        }
    }

}
