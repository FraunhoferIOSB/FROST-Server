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
package de.fraunhofer.iosb.ilt.frostserver.util;

import static de.fraunhofer.iosb.ilt.frostserver.property.PropertyAbstract.Option.REQUIRED;
import static de.fraunhofer.iosb.ilt.frostserver.property.SpecialNames.AT_IOT_ID;

import de.fraunhofer.iosb.ilt.frostserver.model.DefaultEntity;
import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.ModelRegistry;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.model.core.EntitySetImpl;
import de.fraunhofer.iosb.ilt.frostserver.model.core.PkValue;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.TimeInstant;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.TimeValue;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain.NavigationPropertyEntity;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain.NavigationPropertyEntitySet;
import de.fraunhofer.iosb.ilt.frostserver.property.Property;
import de.fraunhofer.iosb.ilt.frostserver.property.StandardProperties;
import de.fraunhofer.iosb.ilt.frostserver.property.type.TypeSimplePrimitive;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.constant.IntegerConstant;
import de.fraunhofer.iosb.ilt.frostserver.request.ServiceRequest;
import de.fraunhofer.iosb.ilt.frostserver.request.Version;
import de.fraunhofer.iosb.ilt.frostserver.service.InitResult;
import de.fraunhofer.iosb.ilt.frostserver.service.PluginService;
import de.fraunhofer.iosb.ilt.frostserver.service.Service;
import de.fraunhofer.iosb.ilt.frostserver.service.ServiceResponse;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Assertions;

public class TestModel implements PluginService {

    public final EntityPropertyMain<String> epName = new EntityPropertyMain<>("name", TypeSimplePrimitive.EDM_STRING, REQUIRED);
    public final EntityPropertyMain<Number> epValue = new EntityPropertyMain<>("value", TypeSimplePrimitive.EDM_DECIMAL);
    public final EntityPropertyMain<TimeValue> epTime = new EntityPropertyMain<>("time", TypeSimplePrimitive.EDM_DATETIMEOFFSET);

    public final NavigationPropertyEntity npRoomHouse = new NavigationPropertyEntity("House", REQUIRED);
    public final NavigationPropertyEntitySet npHouseRooms = new NavigationPropertyEntitySet("Rooms", npRoomHouse);

    public final NavigationPropertyEntitySet npHouses = new NavigationPropertyEntitySet("Houses");

    public final EntityType etHouse = new EntityType("House", "Houses");
    public final EntityType etRoom = new EntityType("Room", "Rooms");

    public void initModel(ModelRegistry modelRegistry, String idType) {
        modelRegistry.registerEntityType(etHouse);
        etHouse.registerProperty(new EntityPropertyMain<>(AT_IOT_ID, modelRegistry.getPropertyType(idType)).setAliases("id"))
                .registerProperty(epName)
                .registerProperty(epValue)
                .registerProperty(StandardProperties.EP_PROPERTIES)
                .registerProperty(npHouseRooms);
        modelRegistry.registerEntityType(etRoom);
        etRoom.registerProperty(new EntityPropertyMain<>(AT_IOT_ID, modelRegistry.getPropertyType(idType)).setAliases("id"))
                .registerProperty(epName)
                .registerProperty(epValue)
                .registerProperty(epTime)
                .registerProperty(StandardProperties.EP_PROPERTIES)
                .registerProperty(npHouseRooms)
                .registerProperty(npRoomHouse);
    }

    public Entity createHouse(long id, String name, double value) {
        return new DefaultEntity(etHouse, PkValue.of(id))
                .setProperty(epName, name)
                .setProperty(epValue, value);
    }

    public Entity createRoom(long id, String name, double value) {
        return new DefaultEntity(etHouse, PkValue.of(id))
                .setProperty(epName, name)
                .setProperty(epValue, value);
    }

    public Entity createRoom(long id, String name, double value, TimeValue time) {
        return createRoom(id, name, value)
                .setProperty(epTime, time);
    }

    public Map<EntityType, Map<Property, Object>> getTestPropertyValues() {
        Map<EntityType, Map<Property, Object>> propertyValues = new HashMap<>();
        Map<Property, Object> propertyValuesHouse = new HashMap<>();
        Map<Property, Object> propertyValuesRoom = new HashMap<>();
        propertyValues.put(etHouse, propertyValuesHouse);
        propertyValues.put(etRoom, propertyValuesRoom);

        propertyValuesHouse.put(etHouse.getPrimaryKey().getKeyProperties().get(0), new IntegerConstant(1));
        propertyValuesRoom.put(etRoom.getPrimaryKey().getKeyProperties().get(0), new IntegerConstant(1));
        propertyValuesHouse.put(epName, "myName");
        propertyValuesRoom.put(epName, "myName");
        propertyValuesHouse.put(epValue, 6);
        propertyValuesRoom.put(epValue, 7);
        propertyValuesRoom.put(epTime, TimeInstant.now());

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("key1", "value1");
        parameters.put("key2", 2);
        propertyValuesHouse.put(StandardProperties.EP_PROPERTIES, parameters);
        propertyValuesRoom.put(StandardProperties.EP_PROPERTIES, parameters);
        propertyValuesHouse.put(StandardProperties.EP_SELFLINK, "http://my.self/link");
        propertyValuesRoom.put(StandardProperties.EP_SELFLINK, "http://my.self/link");

        long nextId = 100;
        propertyValuesRoom.put(npRoomHouse, new DefaultEntity(etHouse, PkValue.of(nextId++)));

        EntitySetImpl rooms = new EntitySetImpl(etRoom);
        rooms.add(new DefaultEntity(etRoom, PkValue.of(nextId++)));
        rooms.add(new DefaultEntity(etRoom, PkValue.of(nextId++)));
        propertyValuesHouse.put(npHouseRooms, rooms);

        EntitySetImpl houses = new EntitySetImpl(etHouse);
        houses.add(new DefaultEntity(etHouse, PkValue.of(nextId++)));
        houses.add(new DefaultEntity(etHouse, PkValue.of(nextId++)));
        propertyValuesRoom.put(npHouseRooms, houses);

        Assertions.assertEquals(105, nextId);
        return propertyValues;
    }

    @Override
    public Collection<Version> getVersions() {
        return Arrays.asList(Version.INTERNAL);
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
    public InitResult init(CoreSettings settings) {
        settings.getPluginManager().registerPlugin(this);
        return InitResult.INIT_OK;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
