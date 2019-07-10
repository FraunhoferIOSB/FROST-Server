/*
 * Copyright 2016 Open Geospatial Consortium.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.fraunhofer.iosb.ilt.statests.util;

import de.fraunhofer.iosb.ilt.sta.ServiceFailureException;
import de.fraunhofer.iosb.ilt.sta.dao.BaseDao;
import de.fraunhofer.iosb.ilt.sta.model.Entity;
import de.fraunhofer.iosb.ilt.sta.model.ext.EntityList;
import de.fraunhofer.iosb.ilt.sta.service.SensorThingsService;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jab
 */
public class Utils {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(Utils.class);

    private Utils() {
    }

    public static String urlEncode(String link) {
        try {
            return URLEncoder.encode(link, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException ex) {
        }
        return link;
    }

    /**
     * Quote the ID for use in json, if needed.
     *
     * @param id The id to quote.
     * @return The quoted id.
     */
    public static String quoteIdForJson(Object id) {
        if (id instanceof Number) {
            return id.toString();
        }
        return "\"" + id + "\"";
    }

    /**
     * Quote the ID for use in URLs, if needed.
     *
     * @param id The id to quote.
     * @return The quoted id.
     */
    public static String quoteIdForUrl(Object id) {
        if (id instanceof Number) {
            return id.toString();
        }
        return "'" + id + "'";
    }

    public static Object idObjectFromPostResult(String postResultLine) {
        int pos1 = postResultLine.lastIndexOf("(") + 1;
        int pos2 = postResultLine.lastIndexOf(")");
        String part = postResultLine.substring(pos1, pos2);
        try {
            return Long.parseLong(part);
        } catch (NumberFormatException exc) {
            // Id was not a long, thus a String.
            if (!part.startsWith("'") || !part.endsWith("'")) {
                throw new IllegalArgumentException("Strings in urls must be quoted with single quotes.");
            }
            return part.substring(1, part.length() - 1);
        }
    }

    public static void deleteAll(SensorThingsService sts) throws ServiceFailureException {
        deleteAll(sts.things());
        deleteAll(sts.locations());
        deleteAll(sts.sensors());
        deleteAll(sts.featuresOfInterest());
        deleteAll(sts.observedProperties());
        deleteAll(sts.observations());
    }

    public static <T extends Entity<T>> void deleteAll(BaseDao<T> doa) throws ServiceFailureException {
        boolean more = true;
        int count = 0;
        while (more) {
            EntityList<T> entities = doa.query().count().list();
            if (entities.getCount() > 0) {
                LOGGER.debug("{} to go.", entities.getCount());
            } else {
                more = false;
            }
            for (T entity : entities) {
                doa.delete(entity);
                count++;
            }
        }
        LOGGER.debug("Deleted {} using {}.", count, doa.getClass().getName());
    }

    public static <T extends Entity<T>> List<T> getFromList(List<T> list, int... ids) {
        List<T> result = new ArrayList<>();
        for (int i : ids) {
            result.add(list.get(i));
        }
        return result;
    }

    public static <T extends Entity<T>> List<T> getFromListExcept(List<T> list, int... ids) {
        List<T> result = new ArrayList<>(list);
        for (int i : ids) {
            result.remove(list.get(i));
        }
        return result;
    }

    public static <T extends Entity<T>> List<T> removeFromList(List<T> sourceList, List<T> remaining, int... ids) {
        for (int i : ids) {
            remaining.remove(sourceList.get(i));
        }
        return remaining;
    }

}
