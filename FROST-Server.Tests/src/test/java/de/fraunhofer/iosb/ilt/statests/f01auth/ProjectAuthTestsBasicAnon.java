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

import de.fraunhofer.iosb.ilt.frostclient.SensorThingsService;
import de.fraunhofer.iosb.ilt.statests.ServerVersion;
import de.fraunhofer.iosb.ilt.statests.TestSuite;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.commons.io.IOUtils;

/**
 * Runs the Project Auth Tests using BasicAuth.
 */
public class ProjectAuthTestsBasicAnon extends ProjectAuthTests {

    private static final Map<String, String> SERVER_PROPERTIES = new LinkedHashMap<>();

    static {
        ProjectAuthTests.addCommonProperties(SERVER_PROPERTIES);
        SERVER_PROPERTIES.put("auth.provider", "de.fraunhofer.iosb.ilt.frostserver.auth.basic.BasicAuthProvider");
        SERVER_PROPERTIES.put("auth.authenticateOnly", "true");
        SERVER_PROPERTIES.put("auth.allowAnonymousRead", "true");
        SERVER_PROPERTIES.put("auth.autoUpdateDatabase", "true");
        final String dbName = "projectAuthBasicAnon";
        final String dbDriver = "org.postgresql.Driver";
        SERVER_PROPERTIES.put("auth.db.url", TestSuite.createDbUrl(dbDriver, dbName));
        SERVER_PROPERTIES.put("auth.db.driver", dbDriver);
        SERVER_PROPERTIES.put("auth.db.username", TestSuite.VAL_PG_USER);
        SERVER_PROPERTIES.put("auth.db.password", TestSuite.VAL_PG_PASS);
        SERVER_PROPERTIES.put("auth.plainTextPassword", "false");
        SERVER_PROPERTIES.put(KEY_DB_NAME, dbName);

    }

    public ProjectAuthTestsBasicAnon(ServerVersion version) {
        super(version, SERVER_PROPERTIES, true);
    }

    @Override
    public String getBatchPostData() throws IOException {
        return IOUtils.resourceToString("projects/dataBatchPost.json", StandardCharsets.UTF_8, getClass().getClassLoader());
    }

    @Override
    public void createServices() {
        serviceAdmin = AuthTestHelper.setAuthBasic(createService(), "admin", "admin");
        serviceWrite = AuthTestHelper.setAuthBasic(createService(), "write", "write");
        serviceRead = AuthTestHelper.setAuthBasic(createService(), "read", "read");
        serviceGlObsCr = AuthTestHelper.setAuthBasic(createService(), "GlobalObsCreater", "GlobalObsCreater");
        serviceGlObsPropCr = AuthTestHelper.setAuthBasic(createService(), "GlobalObsPropCreater", "GlobalObsPropCreater");
        serviceAnon = createService();
        serviceAdminProject1 = AuthTestHelper.setAuthBasic(createService(), "AdminProject1", "AdminProject1");
        serviceAdminProject2 = AuthTestHelper.setAuthBasic(createService(), "AdminProject2", "AdminProject2");
        serviceObsCreaterProject1 = AuthTestHelper.setAuthBasic(createService(), "ObsCreaterProject1", "ObsCreaterProject1");
        serviceObsCreaterProject2 = AuthTestHelper.setAuthBasic(createService(), "ObsCreaterProject2", "ObsCreaterProject2");
    }

    @Override
    protected SensorThingsService createService() {
        try {
            if (!baseService.isBaseUrlSet()) {
                baseService.setBaseUrl(new URI(serverSettings.getServiceUrl(version)))
                        .init();
            }
            return new SensorThingsService(baseService.getModelRegistry())
                    .setBaseUrl(new URI(serverSettings.getServiceUrl(version)))
                    .setVersion(baseService.getVersion())
                    .init();
        } catch (URISyntaxException | MalformedURLException ex) {
            throw new IllegalArgumentException("Serversettings contains malformed URL.", ex);
        }
    }

}
