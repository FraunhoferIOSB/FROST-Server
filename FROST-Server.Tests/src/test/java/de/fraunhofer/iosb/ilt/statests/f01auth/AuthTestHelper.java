/*
 * Copyright (C) 2023 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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

import static org.junit.jupiter.api.Assertions.fail;

import de.fraunhofer.iosb.ilt.frostclient.SensorThingsService;
import de.fraunhofer.iosb.ilt.frostclient.dao.Dao;
import de.fraunhofer.iosb.ilt.frostclient.exception.ServiceFailureException;
import de.fraunhofer.iosb.ilt.frostclient.exception.StatusCodeException;
import de.fraunhofer.iosb.ilt.frostclient.model.Entity;
import de.fraunhofer.iosb.ilt.frostclient.model.property.EntityPropertyMain;
import de.fraunhofer.iosb.ilt.frostclient.model.property.NavigationPropertyEntity;
import de.fraunhofer.iosb.ilt.statests.ServerSettings;
import de.fraunhofer.iosb.ilt.statests.util.EntityUtils;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hylke
 */
public class AuthTestHelper {

    public static final int HTTP_CODE_200_OK = 200;
    public static final int HTTP_CODE_401_UNAUTHORIZED = 401;
    public static final int HTTP_CODE_403_FORBIDDEN = 403;
    public static final int HTTP_CODE_404_NOT_FOUND = 404;

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthTestHelper.class.getName());

    private final ServerSettings serverSettings;

    public AuthTestHelper(ServerSettings serverSettings) {
        this.serverSettings = serverSettings;
    }

    public void getDatabaseStatusIndirect(SensorThingsService service, int... expectedResponse) throws IOException {
        getDatabaseStatus(service, service.getEndpoint() + "../DatabaseStatus", expectedResponse);
    }

    public void getDatabaseStatus(SensorThingsService service, int... expectedResponse) throws IOException {
        getDatabaseStatus(service, serverSettings.getServiceRootUrl() + "/DatabaseStatus", expectedResponse);
    }

    public void getDatabaseStatus(SensorThingsService service, String url, int... expectedResponse) throws IOException {
        HttpGet getUpdateDb = new HttpGet(url);
        CloseableHttpResponse response = service.execute(getUpdateDb);
        int code = response.getStatusLine().getStatusCode();
        for (int expected : expectedResponse) {
            if (expected == code) {
                return;
            }
        }
        LOGGER.info("Failed response: {}", org.apache.http.util.EntityUtils.toString(response.getEntity()));
        fail("Unexpected return code: " + code + ", expected one of " + Arrays.toString(expectedResponse));
    }

    public void createForOk(String user, SensorThingsService service, Entity entity, Dao validateDoa, List<Entity> expected) {
        try {
            service.create(entity);
        } catch (StatusCodeException ex) {
            String failMessage = "User " + user + " should be able to create " + entity.getEntityType() + ". Response:\n" + ex.getReturnedContent();
            LOGGER.error(failMessage, ex);
            fail(failMessage);
        } catch (ServiceFailureException ex) {
            String failMessage = "User " + user + " should be able to create " + entity.getEntityType();
            LOGGER.error(failMessage, ex);
            fail(failMessage);
        }
        EntityUtils.testFilterResults(validateDoa, "", expected);
    }

    public void createForFail(String user, SensorThingsService service, Entity entity, Dao validateDoa, List<Entity> expected, int... expectedCodes) {
        String failMessage = "User " + user + " should NOT be able to create " + entity.getEntityType();
        try {
            service.create(entity);
            fail(failMessage);
        } catch (ServiceFailureException ex) {
            expectStatusCodeException(failMessage, ex, expectedCodes);
        }
        EntityUtils.testFilterResults(validateDoa, "", expected);
    }

    public void updateForOk(String user, SensorThingsService service, Entity entity, NavigationPropertyEntity property) {
        try {
            service.update(entity);
        } catch (ServiceFailureException ex) {
            String failMessage = "User " + user + " should be able to update " + entity.getEntityType() + " got " + ex.getMessage();
            LOGGER.error(failMessage, ex);
            fail(failMessage);
        }
        EntityUtils.compareEntityWithRemote(service, entity, property);
    }

    public void updateForOk(String user, SensorThingsService service, Entity entity, EntityPropertyMain... properties) {
        try {
            service.update(entity);
        } catch (ServiceFailureException ex) {
            String failMessage = "User " + user + " should be able to update " + entity.getEntityType() + " got " + ex.getMessage();
            LOGGER.error(failMessage, ex);
            fail(failMessage);
        }
        EntityUtils.compareEntityWithRemote(service, entity, properties);
    }

    public void updateForFail(String user, SensorThingsService service, Entity entity, SensorThingsService validator, Entity original, int... expectedCodes) {
        String failMessage = "User " + user + " should NOT be able to update " + entity.getEntityType();
        try {
            service.update(entity);
            fail(failMessage);
        } catch (ServiceFailureException ex) {
            expectStatusCodeException(failMessage, ex, expectedCodes);
        }
        EntityUtils.compareEntityWithRemote(validator, original);
    }

    public void deleteForOk(String user, SensorThingsService service, Entity entity, Dao validateDoa, List<Entity> expected) {
        try {
            service.delete(entity);
        } catch (ServiceFailureException ex) {
            String failMessage = "User " + user + " should be able to delete " + entity.getEntityType();
            LOGGER.error(failMessage, ex);
            fail(failMessage);
        }
        EntityUtils.testFilterResults(validateDoa, "", expected);
    }

    public void deleteForFail(String user, SensorThingsService service, Entity entity, Dao validateDoa, List<Entity> expected, int... expectedCodes) {
        String failMessage = "User " + user + " should NOT be able to delete " + entity.getEntityType();
        try {
            service.delete(entity);
            fail(failMessage);
        } catch (ServiceFailureException ex) {
            expectStatusCodeException(failMessage, ex, expectedCodes);
        }
        EntityUtils.testFilterResults(validateDoa, "", expected);
    }

    public void expectStatusCodeException(String failMessage, Exception ex, int... expected) {
        int got = -1;
        if (ex instanceof StatusCodeException) {
            StatusCodeException scex = (StatusCodeException) ex;
            got = scex.getStatusCode();
            for (int want : expected) {
                if (got == want) {
                    return;
                }
            }
        }
        failMessage += " expected one of: " + Arrays.toString(expected) + " got " + got;
        LOGGER.error(failMessage, ex);
        fail(failMessage);
    }

    public static SensorThingsService setAuthBasic(SensorThingsService service, String username, String password) {
        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        URL url = service.getEndpoint();

        credsProvider.setCredentials(
                new AuthScope(url.getHost(), url.getPort()),
                new UsernamePasswordCredentials(username, password));

        service.getClientBuilder()
                .setDefaultCredentialsProvider(credsProvider);

        service.rebuildHttpClient();
        return service;
    }

}
