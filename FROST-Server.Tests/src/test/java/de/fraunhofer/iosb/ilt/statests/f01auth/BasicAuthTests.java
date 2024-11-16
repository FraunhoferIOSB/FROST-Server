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
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for access rights checking with Basic Authentication.
 *
 * @author Hylke van der Schaaf
 */
public abstract class BasicAuthTests extends AbstractAuthTests {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(BasicAuthTests.class);

    private static final Map<String, String> SERVER_PROPERTIES = new LinkedHashMap<>();

    static {
        SERVER_PROPERTIES.put("auth.provider", "de.fraunhofer.iosb.ilt.frostserver.auth.basic.BasicAuthProvider");
        SERVER_PROPERTIES.put("auth.allowAnonymousRead", "false");
        SERVER_PROPERTIES.put("auth.autoUpdateDatabase", "true");
        final String dbName = "basicauth";
        final String dbDriver = "org.postgresql.Driver";
        SERVER_PROPERTIES.put("auth.db.url", TestSuite.createDbUrl(dbDriver, dbName));
        SERVER_PROPERTIES.put("auth.db.driver", dbDriver);
        SERVER_PROPERTIES.put("auth.db.username", TestSuite.VAL_PG_USER);
        SERVER_PROPERTIES.put("auth.db.password", TestSuite.VAL_PG_PASS);
        SERVER_PROPERTIES.put(KEY_DB_NAME, dbName);
    }

    public BasicAuthTests(ServerVersion version) {
        super(version, SERVER_PROPERTIES, false);
    }

    @Override
    protected void setUpVersion() {
        LOGGER.info("Setting up for version {}.", version.urlPart);
        super.setUpVersion();
    }

    @Override
    public SensorThingsService getServiceAdmin() {
        return AuthTestHelper.setAuthBasic(createService(), "admin", "admin");
    }

    @Override
    public SensorThingsService getServiceWrite() {
        return AuthTestHelper.setAuthBasic(createService(), "write", "write");
    }

    @Override
    public SensorThingsService getServiceRead() {
        return AuthTestHelper.setAuthBasic(createService(), "read", "read");
    }

    @Override
    public SensorThingsService getServiceAnonymous() {
        return createService();
    }

}
