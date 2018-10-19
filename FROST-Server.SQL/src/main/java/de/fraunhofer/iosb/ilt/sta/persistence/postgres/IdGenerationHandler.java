/*
 * Copyright (C) 2016 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
 * Karlsruhe, Germany.
 * Copyright (C) 2018 KIT TECO, Vincenz-Prießnitz-Str. 1, D 76131
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
package de.fraunhofer.iosb.ilt.sta.persistence.postgres;

import de.fraunhofer.iosb.ilt.sta.model.core.Entity;
import de.fraunhofer.iosb.ilt.sta.util.IncompleteEntityException;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * Class for handling the persistence setting "idGenerationMode".
 *
 * @author koepke
 */
public abstract class IdGenerationHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(IdGenerationHandler.class);

    /**
     * The possible id generation modes.
     */
    private enum IdGenerationType {
        SERVER_GENERATED_ONLY,
        SERVER_AND_CLIENT_GENERATED,
        CLIENT_GENERATED_ONLY;

        private static final Map<String, IdGenerationType> aliases = new HashMap<>();

        public static IdGenerationType findType(String input) {
            if (aliases.isEmpty()) {
                init();
            }
            return aliases.get(input.toLowerCase());
        }

        private static void init() {
            for (IdGenerationType type : IdGenerationType.values()) {
                aliases.put(type.name(), type);
                aliases.put(type.name().replace("_", "").toLowerCase(), type);
            }
        }
    }

    private static IdGenerationType idGenerationMode = IdGenerationType.SERVER_GENERATED_ONLY;

    private final Entity entity;

    /**
     * Constructor for IdGenerationHandler.
     *
     * @param entity Entity for which idGenerationMode should be
     * checked/applied.
     */
    public IdGenerationHandler(Entity entity) {
        this.entity = entity;
    }

    /**
     *
     * Sets the idGenerationMode for all IdGenerationHandler instances.
     *
     * @param mode String that contains the idGenerationMode.
     * @throws IllegalArgumentException Will be thrown if given idGenerationMode
     * is not supported.
     */
    public static void setIdGenerationMode(String mode) throws IllegalArgumentException {
        idGenerationMode = IdGenerationType.findType(mode);
        if (idGenerationMode == null) {
            // not a valid generation mode
            String error = "Unknown idGenerationMode: " + mode + ".";
            LOGGER.error(error);
            throw new IllegalArgumentException(error);
        }
    }

    protected Entity getEntity() {
        return entity;
    }

    /**
     *
     * Returns the idvalue of the entity, which was used to create the instance
     * of IdGeneration Handler.
     *
     * @return Value of the entity id. Can be null.
     */
    public Object getIdValue() {
        if (entity.getId() == null) {
            return null;
        } else {
            // keep null pointer for error handling
            return entity.getId().getValue();
        }
    }

    /**
     *
     * Modify the entity id.
     *
     */
    public abstract void modifyClientSuppliedId();

    /**
     *
     * Checks if a client generated id can/should be used with respect to the
     * idGenerationMode.
     *
     * @return true if a valid client id can be used.
     * @throws IncompleteEntityException Will be thrown if @iot.id is missing
     * for client generated ids.
     * @throws IllegalArgumentException Will be thrown if idGenerationMode is
     * not supported.
     */
    public boolean useClientSuppliedId() throws IncompleteEntityException, IllegalArgumentException {
        switch (idGenerationMode) {
            case SERVER_GENERATED_ONLY:
                if (getIdValue() == null) {
                    LOGGER.trace("Using server generated id.");
                    return false;
                } else {
                    LOGGER.warn("idGenerationMode is '{}' but @iot.id '{}' is present. Ignoring @iot.id.", idGenerationMode, getIdValue());
                    return false;
                }

            case SERVER_AND_CLIENT_GENERATED:
                if (!validateClientSuppliedId()) {
                    LOGGER.debug("No valid @iot.id. Using server generated id.");
                    return false;
                }
                break;

            case CLIENT_GENERATED_ONLY:
                if (!validateClientSuppliedId()) {
                    LOGGER.error("No @iot.id and idGenerationMode is '{}'", idGenerationMode);
                    throw new IncompleteEntityException("Error: no @iot.id");
                }
                break;

            default:
                // not a valid generation mode
                LOGGER.error("idGenerationMode '{}' is not implemented.", idGenerationMode);
                throw new IllegalArgumentException("idGenerationMode '" + idGenerationMode.toString() + "' is not implemented.");
        }

        LOGGER.info("Using client generated id.");
        return true;
    }

    /**
     *
     * Checks if a client generated id is valid.
     *
     * @return true if client generated id is valid.
     */
    protected abstract boolean validateClientSuppliedId();
}
