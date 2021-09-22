/*
 * Copyright (C) 2020 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 */
package de.fraunhofer.iosb.ilt.frostserver.model;

import de.fraunhofer.iosb.ilt.frostserver.model.core.Id;
import static de.fraunhofer.iosb.ilt.frostserver.model.ext.TypeReferencesHelper.TYPE_REFERENCE_ID;
import static de.fraunhofer.iosb.ilt.frostserver.model.ext.TypeReferencesHelper.TYPE_REFERENCE_MAP;
import static de.fraunhofer.iosb.ilt.frostserver.model.ext.TypeReferencesHelper.TYPE_REFERENCE_STRING;
import de.fraunhofer.iosb.ilt.frostserver.path.ParserHelper;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyMain;
import static de.fraunhofer.iosb.ilt.frostserver.property.SpecialNames.AT_IOT_ID;
import static de.fraunhofer.iosb.ilt.frostserver.property.SpecialNames.AT_IOT_SELF_LINK;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hylke
 */
public class ModelRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(ModelRegistry.class.getName());

    /**
     * The global EntityProperty @iot.id.
     */
    public static final EntityPropertyMain<Id> EP_ID = new EntityPropertyMain<>(AT_IOT_ID, TYPE_REFERENCE_ID, "id");
    /**
     * The global EntityProperty SelfLink.
     */
    public static final EntityPropertyMain<String> EP_SELFLINK = new EntityPropertyMain<>(AT_IOT_SELF_LINK, TYPE_REFERENCE_STRING, "selfLink");
    /**
     * The global EntityProperty properties.
     */
    public static final EntityPropertyMain<Map<String, Object>> EP_PROPERTIES = new EntityPropertyMain<>("properties", TYPE_REFERENCE_MAP, true, false);
    /**
     * The global EntityProperty encodingType.
     */
    public static final EntityPropertyMain<String> EP_ENCODINGTYPE = new EntityPropertyMain<>("encodingType", TYPE_REFERENCE_STRING);

    private final Map<String, EntityType> typesByName = new HashMap<>();
    private final Set<EntityType> types = new LinkedHashSet<>();

    private Class<? extends Id> idClass;

    private ParserHelper parserHelper;

    /**
     * Entities need queries, even when sent through messages.
     */
    private final EntityChangedMessage.QueryGenerator messageQueryGenerator = new EntityChangedMessage.QueryGenerator();

    public final EntityType registerEntityType(EntityType type) {
        if (typesByName.containsKey(type.entityName)) {
            throw new IllegalArgumentException("An entity type named " + type.entityName + " is already registered");
        }
        typesByName.put(type.entityName, type);
        typesByName.put(type.plural, type);
        types.add(type);
        type.setModelRegistry(this);
        return type;
    }

    public final EntityType getEntityTypeForName(String typeName) {
        return typesByName.get(typeName);
    }

    public final Set<EntityType> getEntityTypes() {
        return types;
    }

    public Class<? extends Id> getIdClass() {
        return idClass;
    }

    public void setIdClass(Class<? extends Id> idClass) {
        this.idClass = idClass;
    }

    public EntityChangedMessage.QueryGenerator getMessageQueryGenerator() {
        return messageQueryGenerator;
    }

    public synchronized void initFinalise() {
        LOGGER.info("Finalising {} EntityTypes.", types.size());
        for (EntityType type : types) {
            type.init();
        }
    }

    public ParserHelper getParserHelper() {
        if (parserHelper == null) {
            parserHelper = new ParserHelper(this);
        }
        return parserHelper;
    }
}
