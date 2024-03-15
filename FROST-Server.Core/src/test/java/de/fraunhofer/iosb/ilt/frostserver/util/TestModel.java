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
package de.fraunhofer.iosb.ilt.frostserver.util;

import static de.fraunhofer.iosb.ilt.frostserver.property.SpecialNames.AT_IOT_ID;

import de.fraunhofer.iosb.ilt.frostserver.model.DefaultEntity;
import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.ModelRegistry;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.model.core.EntitySetImpl;
import de.fraunhofer.iosb.ilt.frostserver.model.core.PkValue;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.TimeInstant;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.TimeValue;
import de.fraunhofer.iosb.ilt.frostserver.path.Version;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain.NavigationPropertyEntity;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain.NavigationPropertyEntitySet;
import de.fraunhofer.iosb.ilt.frostserver.property.Property;
import de.fraunhofer.iosb.ilt.frostserver.property.type.TypeSimplePrimitive;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.constant.IntegerConstant;
import de.fraunhofer.iosb.ilt.frostserver.service.PluginService;
import de.fraunhofer.iosb.ilt.frostserver.service.Service;
import de.fraunhofer.iosb.ilt.frostserver.service.ServiceRequest;
import de.fraunhofer.iosb.ilt.frostserver.service.ServiceResponse;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author hylke
 */
public class TestModel implements PluginService {

    public final EntityPropertyMain<String> EP_NAME = new EntityPropertyMain<>("name", TypeSimplePrimitive.EDM_STRING, true, false);
    public final EntityPropertyMain<Number> EP_VALUE = new EntityPropertyMain<>("value", TypeSimplePrimitive.EDM_DECIMAL);
    public final EntityPropertyMain<TimeValue> EP_TIME = new EntityPropertyMain<>("time", TypeSimplePrimitive.EDM_DATETIMEOFFSET);

    public final NavigationPropertyEntity NP_ROOM_HOUSE = new NavigationPropertyEntity("House", true);
    public final NavigationPropertyEntitySet NP_HOUSE_ROOMS = new NavigationPropertyEntitySet("Rooms", NP_ROOM_HOUSE);

    public final NavigationPropertyEntitySet NP_HOUSES = new NavigationPropertyEntitySet("Houses");

    public final EntityType ET_HOUSE = new EntityType("House", "Houses");
    public final EntityType ET_ROOM = new EntityType("Room", "Rooms");

    public void initModel(ModelRegistry modelRegistry, String idType) {
        modelRegistry.registerEntityType(ET_HOUSE);
        ET_HOUSE.registerProperty(new EntityPropertyMain<>(AT_IOT_ID, modelRegistry.getPropertyType(idType)).setAliases("id"))
                .registerProperty(EP_NAME)
                .registerProperty(EP_VALUE)
                .registerProperty(ModelRegistry.EP_PROPERTIES)
                .registerProperty(NP_HOUSE_ROOMS);
        modelRegistry.registerEntityType(ET_ROOM);
        ET_ROOM.registerProperty(new EntityPropertyMain<>(AT_IOT_ID, modelRegistry.getPropertyType(idType)).setAliases("id"))
                .registerProperty(EP_NAME)
                .registerProperty(EP_VALUE)
                .registerProperty(EP_TIME)
                .registerProperty(ModelRegistry.EP_PROPERTIES)
                .registerProperty(NP_HOUSE_ROOMS)
                .registerProperty(NP_ROOM_HOUSE);
    }

    public Entity createHouse(long id, String name, double value) {
        return new DefaultEntity(ET_HOUSE, PkValue.of(id))
                .setProperty(EP_NAME, name)
                .setProperty(EP_VALUE, value);
    }

    public Entity createRoom(long id, String name, double value) {
        return new DefaultEntity(ET_HOUSE, PkValue.of(id))
                .setProperty(EP_NAME, name)
                .setProperty(EP_VALUE, value);
    }

    public Entity createRoom(long id, String name, double value, TimeValue time) {
        return createRoom(id, name, value)
                .setProperty(EP_TIME, time);
    }

    public Map<EntityType, Map<Property, Object>> getTestPropertyValues(ModelRegistry modelRegistry) {
        Map<EntityType, Map<Property, Object>> propertyValues = new HashMap<>();
        Map<Property, Object> propertyValuesHouse = new HashMap<>();
        Map<Property, Object> propertyValuesRoom = new HashMap<>();
        propertyValues.put(ET_HOUSE, propertyValuesHouse);
        propertyValues.put(ET_ROOM, propertyValuesRoom);

        propertyValuesHouse.put(ET_HOUSE.getPrimaryKey().getKeyProperties().get(0), new IntegerConstant(1));
        propertyValuesRoom.put(ET_ROOM.getPrimaryKey().getKeyProperties().get(0), new IntegerConstant(1));
        propertyValuesHouse.put(EP_NAME, "myName");
        propertyValuesRoom.put(EP_NAME, "myName");
        propertyValuesHouse.put(EP_VALUE, 6);
        propertyValuesRoom.put(EP_VALUE, 7);
        propertyValuesRoom.put(EP_TIME, TimeInstant.now());

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("key1", "value1");
        parameters.put("key2", 2);
        propertyValuesHouse.put(ModelRegistry.EP_PROPERTIES, parameters);
        propertyValuesRoom.put(ModelRegistry.EP_PROPERTIES, parameters);
        propertyValuesHouse.put(ModelRegistry.EP_SELFLINK, "http://my.self/link");
        propertyValuesRoom.put(ModelRegistry.EP_SELFLINK, "http://my.self/link");

        long nextId = 100;
        propertyValuesRoom.put(NP_ROOM_HOUSE, new DefaultEntity(ET_HOUSE, PkValue.of(nextId++)));

        EntitySetImpl rooms = new EntitySetImpl(ET_ROOM);
        rooms.add(new DefaultEntity(ET_ROOM, PkValue.of(nextId++)));
        rooms.add(new DefaultEntity(ET_ROOM, PkValue.of(nextId++)));
        propertyValuesHouse.put(NP_HOUSE_ROOMS, rooms);

        EntitySetImpl houses = new EntitySetImpl(ET_HOUSE);
        houses.add(new DefaultEntity(ET_HOUSE, PkValue.of(nextId++)));
        houses.add(new DefaultEntity(ET_HOUSE, PkValue.of(nextId++)));
        propertyValuesRoom.put(NP_HOUSE_ROOMS, houses);
        return propertyValues;
    }

    @Override
    public Collection<Version> getVersions() {
        return Arrays.asList(Version.V_1_0, Version.V_1_1);
    }

    @Override
    public boolean definesVersions() {
        return true;
    }

    @Override
    public Collection<String> getVersionedUrlPaths() {
        return Collections.emptyList();
    }

    @Override
    public Collection<String> getRequestTypes() {
        return Collections.emptyList();
    }

    @Override
    public String getRequestTypeFor(Version version, String path, HttpMethod method, String contentType) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ServiceResponse execute(Service mainService, ServiceRequest request, ServiceResponse response) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void init(CoreSettings settings) {
        settings.getPluginManager().registerPlugin(this);
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
