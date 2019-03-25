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
package de.fraunhofer.iosb.ilt.frostserver.util;

import de.fraunhofer.iosb.ilt.frostserver.model.FeatureOfInterest;
import de.fraunhofer.iosb.ilt.frostserver.model.builder.ObservationBuilder;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Id;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.TimeInstant;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.TimeInterval;
import de.fraunhofer.iosb.ilt.frostserver.persistence.IdManager;
import de.fraunhofer.iosb.ilt.frostserver.persistence.PersistenceManagerFactory;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author scf
 */
public class ArrayValueHandlers {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ArrayValueHandlers.class);

    public interface ArrayValueHandler {

        public void handle(Object value, ObservationBuilder target);
    }
    /**
     * Our default handlers.
     */
    private static final Map<String, ArrayValueHandler> HANDLERS = new HashMap<>();

    public static ArrayValueHandler getHandler(String component) {
        if (HANDLERS.isEmpty()) {
            createDefaults();
        }
        return HANDLERS.get(component);
    }

    private static synchronized void createDefaults() {
        if (!HANDLERS.isEmpty()) {
            return;
        }

        final IdManager idManager = PersistenceManagerFactory.getInstance().getIdManager();
        ArrayValueHandler idHandler = (Object value, ObservationBuilder target) -> target.setId(idManager.parseId(value.toString()));
        HANDLERS.put("id", idHandler);
        HANDLERS.put("@iot.id", idHandler);
        HANDLERS.put(
                "result",
                (Object value, ObservationBuilder target) -> target.setResult(value)
        );
        HANDLERS.put(
                "resultQuality",
                (Object value, ObservationBuilder target) -> target.setResultQuality(value)
        );
        HANDLERS.put("parameters", (Object value, ObservationBuilder target) -> {
            if (value instanceof Map) {
                target.setParameters((Map<String, Object>) value);
                return;
            }
            throw new IllegalArgumentException("parameters has to be a map.");
        });
        HANDLERS.put("phenomenonTime", (Object value, ObservationBuilder target) -> {
            try {
                TimeInstant time = TimeInstant.parse(value.toString());
                target.setPhenomenonTime(time);
                return;
            } catch (Exception e) {
                LOGGER.trace("Not a time instant: {}.", value);
            }
            try {
                TimeInterval time = TimeInterval.parse(value.toString());
                target.setPhenomenonTime(time);
                return;
            } catch (Exception e) {
                LOGGER.trace("Not a time interval: {}.", value);
            }
            throw new IllegalArgumentException("phenomenonTime could not be parsed as time instant or time interval.");
        });
        HANDLERS.put("resultTime", (Object value, ObservationBuilder target) -> {
            try {
                TimeInstant time = TimeInstant.parse(value.toString());
                target.setResultTime(time);
            } catch (Exception e) {
                throw new IllegalArgumentException("resultTime could not be parsed as time instant or time interval.", e);
            }
        });
        HANDLERS.put("validTime", (Object value, ObservationBuilder target) -> {
            try {
                TimeInterval time = TimeInterval.parse(value.toString());
                target.setValidTime(time);
            } catch (Exception e) {
                throw new IllegalArgumentException("resultTime could not be parsed as time instant or time interval.", e);
            }
        });
        HANDLERS.put("FeatureOfInterest/id", (Object value, ObservationBuilder target) -> {
            Id foiId = idManager.parseId(value.toString());
            target.setFeatureOfInterest(new FeatureOfInterest(foiId));
        });

    }
}
