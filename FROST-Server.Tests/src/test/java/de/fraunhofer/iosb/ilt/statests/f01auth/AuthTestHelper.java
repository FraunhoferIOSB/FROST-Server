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

import de.fraunhofer.iosb.ilt.sta.ServiceFailureException;
import de.fraunhofer.iosb.ilt.sta.StatusCodeException;
import de.fraunhofer.iosb.ilt.sta.dao.BaseDao;
import de.fraunhofer.iosb.ilt.sta.model.Entity;
import de.fraunhofer.iosb.ilt.sta.service.SensorThingsService;
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

    public <T extends Entity<T>> void createForOk(SensorThingsService service, T entity, String failMessage, BaseDao<T> validateDoa, List<T> expected) {
        try {
            service.create(entity);
        } catch (ServiceFailureException ex) {
            LOGGER.error(failMessage, ex);
            fail(failMessage);
        }
        EntityUtils.testFilterResults(validateDoa, "", expected);
    }

    public <T extends Entity<T>> void createForFail(SensorThingsService service, T entity, String failMessage, BaseDao<T> validateDoa, List<T> expected, int... expectedCodes) {
        try {
            service.create(entity);
            fail(failMessage);
        } catch (ServiceFailureException ex) {
            expectStatusCodeException(failMessage, ex, expectedCodes);
        }
        EntityUtils.testFilterResults(validateDoa, "", expected);
    }

    public <T extends Entity<T>> void updateForOk(SensorThingsService service, T entity, String failMessage, BaseDao<T> validateDoa, List<T> expected) {
        try {
            service.update(entity);
        } catch (ServiceFailureException ex) {
            LOGGER.error(failMessage, ex);
            fail(failMessage);
        }
        EntityUtils.testFilterResults(validateDoa, "", expected);
    }

    public <T extends Entity<T>> void updateForFail(SensorThingsService service, T entity, String failMessage, BaseDao<T> validateDoa, List<T> expected, int... expectedCodes) {
        try {
            service.update(entity);
            fail(failMessage);
        } catch (ServiceFailureException ex) {
            expectStatusCodeException(failMessage, ex, expectedCodes);
        }
        EntityUtils.testFilterResults(validateDoa, "", expected);
    }

    public <T extends Entity<T>> void deleteForOk(SensorThingsService service, T entity, String failMessage, BaseDao<T> validateDoa, List<T> expected) {
        try {
            service.delete(entity);
        } catch (ServiceFailureException ex) {
            LOGGER.error(failMessage, ex);
            fail(failMessage);
        }
        EntityUtils.testFilterResults(validateDoa, "", expected);
    }

    public <T extends Entity<T>> void deleteForFail(SensorThingsService service, T entity, String failMessage, BaseDao<T> validateDoa, List<T> expected, int... expectedCodes) {
        try {
            service.delete(entity);
            fail(failMessage);
        } catch (ServiceFailureException ex) {
            expectStatusCodeException(failMessage, ex, expectedCodes);
        }
        EntityUtils.testFilterResults(validateDoa, "", expected);
    }

    public void expectStatusCodeException(String failMessage, Exception ex, int... expected) {
        if (ex instanceof StatusCodeException) {
            StatusCodeException scex = (StatusCodeException) ex;
            int got = scex.getStatusCode();
            for (int want : expected) {
                if (got == want) {
                    return;
                }
            }
        }
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
