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
package de.fraunhofer.iosb.ilt.statests.f01auth;

import static de.fraunhofer.iosb.ilt.statests.TestSuite.KEY_DB_NAME;
import static de.fraunhofer.iosb.ilt.statests.f01auth.FineGrainedAuthTests.serviceAdminProject1;
import static de.fraunhofer.iosb.ilt.statests.f01auth.FineGrainedAuthTests.serviceAdminProject2;
import static de.fraunhofer.iosb.ilt.statests.f01auth.FineGrainedAuthTests.serviceObsCreaterProject1;
import static de.fraunhofer.iosb.ilt.statests.f01auth.FineGrainedAuthTests.serviceObsCreaterProject2;
import static de.fraunhofer.iosb.ilt.statests.f01auth.FineGrainedAuthTests.serviceRead;
import static de.fraunhofer.iosb.ilt.statests.f01auth.FineGrainedAuthTests.serviceWrite;
import static de.fraunhofer.iosb.ilt.statests.f01auth.SensorThingsUserModel.EP_USERNAME;
import static de.fraunhofer.iosb.ilt.statests.f01auth.SensorThingsUserModel.EP_USERPASS;
import static de.fraunhofer.iosb.ilt.statests.util.EntityUtils.testFilterResults;
import static org.junit.jupiter.api.Assertions.fail;

import de.fraunhofer.iosb.ilt.frostclient.SensorThingsService;
import de.fraunhofer.iosb.ilt.frostclient.exception.ServiceFailureException;
import de.fraunhofer.iosb.ilt.frostclient.model.Entity;
import de.fraunhofer.iosb.ilt.statests.ServerVersion;
import de.fraunhofer.iosb.ilt.statests.TestSuite;
import de.fraunhofer.iosb.ilt.statests.util.Utils;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Runs the FineGrained Auth Tests using BasicAuth.
 */
public class FineGrainedAuthTestsBasic extends FineGrainedAuthTests {

    private static final Logger LOGGER = LoggerFactory.getLogger(FineGrainedAuthTestsBasic.class.getName());
    private static final Map<String, String> SERVER_PROPERTIES = new LinkedHashMap<>();

    static {
        FineGrainedAuthTests.addCommonProperties(SERVER_PROPERTIES);
        SERVER_PROPERTIES.put("auth.provider", "de.fraunhofer.iosb.ilt.frostserver.auth.basic.BasicAuthProvider");
        SERVER_PROPERTIES.put("auth.authenticateOnly", "true");
        SERVER_PROPERTIES.put("auth.allowAnonymousRead", "false");
        SERVER_PROPERTIES.put("auth.autoUpdateDatabase", "true");
        final String dbName = "fineGrainedAuthBasic";
        final String dbDriver = "org.postgresql.Driver";
        SERVER_PROPERTIES.put("auth.db.url", TestSuite.createDbUrl(dbDriver, dbName));
        SERVER_PROPERTIES.put("auth.db.driver", dbDriver);
        SERVER_PROPERTIES.put("auth.db.username", TestSuite.VAL_PG_USER);
        SERVER_PROPERTIES.put("auth.db.password", TestSuite.VAL_PG_PASS);
        SERVER_PROPERTIES.put("auth.plainTextPassword", "false");
        SERVER_PROPERTIES.put(KEY_DB_NAME, dbName);

    }

    public FineGrainedAuthTestsBasic(ServerVersion version) {
        super(version, SERVER_PROPERTIES, false);
    }

    @Override
    public String getBatchPostData() throws IOException {
        String batchPostData = IOUtils.resourceToString("finegrainedsecurity/dataBatchPost.json", StandardCharsets.UTF_8, FineGrainedAuthTests.class.getClassLoader());
        return batchPostData;
    }

    @Override
    public void createServices() {
        serviceAdmin = AuthTestHelper.setAuthBasic(createService(), "admin", "admin");
        serviceWrite = AuthTestHelper.setAuthBasic(createService(), "write", "write");
        serviceRead = AuthTestHelper.setAuthBasic(createService(), "read", "read");
        serviceAnon = createService();
        serviceAdminProject1 = AuthTestHelper.setAuthBasic(createService(), "AdminProject1", "AdminProject1");
        serviceAdminProject2 = AuthTestHelper.setAuthBasic(createService(), "AdminProject2", "AdminProject2");
        serviceObsCreaterProject1 = AuthTestHelper.setAuthBasic(createService(), "ObsCreaterProject1", "ObsCreaterProject1");
        serviceObsCreaterProject2 = AuthTestHelper.setAuthBasic(createService(), "ObsCreaterProject2", "ObsCreaterProject2");
    }

    @Override
    protected SensorThingsService createService() {
        if (!baseService.isBaseUrlSet()) {
            try {
                baseService.setBaseUrl(new URI(serverSettings.getServiceUrl(version)))
                        .init();
            } catch (URISyntaxException | MalformedURLException ex) {
                throw new IllegalArgumentException("Serversettings contains malformed URL.", ex);
            }
        }
        try {
            return new SensorThingsService(baseService.getModelRegistry())
                    .setBaseUrl(new URI(serverSettings.getServiceUrl(version)))
                    .init();
        } catch (URISyntaxException | MalformedURLException ex) {
            throw new IllegalArgumentException("Serversettings contains malformed URL.", ex);
        }
    }

    @Test
    void test_99_ChangePassword() {
        LOGGER.info("  test_04c_ChangePassword");
        FineGrainedAuthTests.EntityCreator changed = (user) -> USERS.stream().filter(t -> t.getProperty(EP_USERNAME).equals(user)).findFirst().get()
                .setProperty(EP_USERPASS, user + "2");
        FineGrainedAuthTests.EntityCreator changedCopy = (user) -> USERS.stream().filter(t -> t.getProperty(EP_USERNAME).equals(user)).findFirst().get()
                .withOnlyPk()
                .setProperty(EP_USERPASS, user + "2");

        serviceRead = testChangePassword(READ, serviceRead, changed, Utils.getFromList(USERS, 5));
        serviceWrite = testChangePassword(WRITE, serviceWrite, changed, Utils.getFromList(USERS, 6));
        serviceAdminProject1 = testChangePassword(ADMIN_P1, serviceAdminProject1, changed, USERS);
        serviceAdminProject2 = testChangePassword(ADMIN_P2, serviceAdminProject2, changed, USERS);
        serviceObsCreaterProject1 = testChangePassword(OBS_CREATE_P1, serviceObsCreaterProject1, changed, Utils.getFromList(USERS, 3));
        serviceObsCreaterProject2 = testChangePassword(OBS_CREATE_P2, serviceObsCreaterProject2, changed, Utils.getFromList(USERS, 4));

        testChangePasswordFail(WRITE, serviceWrite, changedCopy, READ);
        testChangePasswordFail(ADMIN_P1, serviceAdminProject1, changedCopy, OBS_CREATE_P1);
    }

    private void testChangePasswordFail(String user, SensorThingsService service, FineGrainedAuthTests.EntityCreator creator, String user2) {
        LOGGER.debug("    {}", user);
        try {
            service.update(creator.create(user2));
            String failMessage = "User " + user + " should NOT be able to update password for user " + user2 + ".";
            LOGGER.error(failMessage);
            fail(failMessage);
        } catch (ServiceFailureException ex) {
            // Good!
        }
    }

    private SensorThingsService testChangePassword(String user, SensorThingsService service, FineGrainedAuthTests.EntityCreator creator, List<Entity> entityList) {
        LOGGER.debug("    {}", user);
        final Entity userEntity = creator.create(user);
        try {
            service.update(userEntity);
        } catch (ServiceFailureException ex) {
            String failMessage = "User " + user + " should be able to update password. Got " + ex.getMessage();
            LOGGER.error(failMessage, ex);
            fail(failMessage);
        }
        SensorThingsService newService = AuthTestHelper.setAuthBasic(createService(), user, userEntity.getProperty(EP_USERPASS));
        testFilterResults(user, newService, mdlUsers.etUser, "", entityList);
        return newService;
    }

}
