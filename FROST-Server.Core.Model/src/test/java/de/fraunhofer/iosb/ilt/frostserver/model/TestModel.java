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
package de.fraunhofer.iosb.ilt.frostserver.model;

import static de.fraunhofer.iosb.ilt.frostserver.property.PropertyAbstract.Option.REQUIRED;
import static de.fraunhofer.iosb.ilt.frostserver.property.SpecialNames.AT_IOT_ID;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.fraunhofer.iosb.ilt.frostserver.model.core.EntitySetImpl;
import de.fraunhofer.iosb.ilt.frostserver.model.core.PkValue;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.TimeInstant;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.TimeValue;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain.NavigationPropertyEntity;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain.NavigationPropertyEntitySet;
import de.fraunhofer.iosb.ilt.frostserver.property.Property;
import de.fraunhofer.iosb.ilt.frostserver.property.StandardProperties;
import de.fraunhofer.iosb.ilt.frostserver.property.type.TypeSimplePrimitive;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.constant.IntegerConstant;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Assertions;

public class TestModel {

    public final EntityPropertyMain<String> epName = new EntityPropertyMain<>("name", TypeSimplePrimitive.EDM_STRING, REQUIRED);
    public final EntityPropertyMain<Number> epValue = new EntityPropertyMain<>("value", TypeSimplePrimitive.EDM_DECIMAL);
    public final EntityPropertyMain<TimeValue> epTime = new EntityPropertyMain<>("time", TypeSimplePrimitive.EDM_DATETIMEOFFSET);

    public final NavigationPropertyEntity npRoomHouse = new NavigationPropertyEntity("House", REQUIRED);
    public final NavigationPropertyEntitySet npHouseRooms = new NavigationPropertyEntitySet("Rooms");

    public final NavigationPropertyEntity npRoomBathroom = new NavigationPropertyEntity("BathroomFor");
    public final NavigationPropertyEntitySet npHouseBathrooms = new NavigationPropertyEntitySet("Bathrooms");

    public final NavigationPropertyEntitySet npHouseStreets = new NavigationPropertyEntitySet("Streets");
    public final NavigationPropertyEntitySet npStreetHouses = new NavigationPropertyEntitySet("Houses");

    public final EntityType etStreet = new EntityType("Street", "Streets");
    public final EntityType etHouse = new EntityType("House", "Houses");
    public final EntityType etRoom = new EntityType("Room", "Rooms");

    public void initModel(ModelRegistry modelRegistry, String idType) {
        npHouseBathrooms.setInverses(npRoomBathroom);
        npHouseBathrooms.setEntityType(etRoom);
        npRoomBathroom.setEntityType(etHouse);
        npRoomHouse.setInverses(npHouseRooms);
        npHouseStreets.setInverses(npHouseStreets);

        modelRegistry.registerEntityType(etStreet);
        etStreet.registerProperty(new EntityPropertyMain<>(AT_IOT_ID, modelRegistry.getPropertyType(idType)).setAliases("id"))
                .registerProperty(epName)
                .registerProperty(npStreetHouses);
        modelRegistry.registerEntityType(etHouse);
        etHouse.registerProperty(new EntityPropertyMain<>(AT_IOT_ID, modelRegistry.getPropertyType(idType)).setAliases("id"))
                .registerProperty(epName)
                .registerProperty(epValue)
                .registerProperty(StandardProperties.EP_PROPERTIES)
                .registerProperty(npHouseStreets)
                .registerProperty(npHouseRooms)
                .registerProperty(npHouseBathrooms);
        modelRegistry.registerEntityType(etRoom);
        etRoom.registerProperty(new EntityPropertyMain<>(AT_IOT_ID, modelRegistry.getPropertyType(idType)).setAliases("id"))
                .registerProperty(epName)
                .registerProperty(epValue)
                .registerProperty(epTime)
                .registerProperty(StandardProperties.EP_PROPERTIES)
                .registerProperty(npRoomHouse)
                .registerProperty(npRoomBathroom);
    }

    public Map<EntityType, Map<Property, Object>> getTestPropertyValues(ModelRegistry modelRegistry) {
        Map<EntityType, Map<Property, Object>> propertyValues = new HashMap<>();
        Map<Property, Object> propertyValuesStreet = new HashMap<>();
        Map<Property, Object> propertyValuesHouse = new HashMap<>();
        Map<Property, Object> propertyValuesRoom = new HashMap<>();
        propertyValues.put(etStreet, propertyValuesStreet);
        propertyValues.put(etHouse, propertyValuesHouse);
        propertyValues.put(etRoom, propertyValuesRoom);

        propertyValuesStreet.put(etStreet.getPrimaryKey().getKeyProperties().get(0), new IntegerConstant(1));
        propertyValuesHouse.put(etHouse.getPrimaryKey().getKeyProperties().get(0), new IntegerConstant(1));
        propertyValuesRoom.put(etRoom.getPrimaryKey().getKeyProperties().get(0), new IntegerConstant(1));
        propertyValuesStreet.put(epName, "StreetName");
        propertyValuesHouse.put(epName, "HouseName");
        propertyValuesRoom.put(epName, "RoomName");
        propertyValuesHouse.put(epValue, 6);
        propertyValuesRoom.put(epValue, 7);
        propertyValuesRoom.put(epTime, TimeInstant.now());

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("key1", "value1");
        parameters.put("key2", 2);
        propertyValuesHouse.put(StandardProperties.EP_PROPERTIES, parameters);
        propertyValuesRoom.put(StandardProperties.EP_PROPERTIES, parameters);
        propertyValuesStreet.put(StandardProperties.EP_SELFLINK, "http://my.self/link");
        propertyValuesHouse.put(StandardProperties.EP_SELFLINK, "http://my.self/link");
        propertyValuesRoom.put(StandardProperties.EP_SELFLINK, "http://my.self/link");

        long nextId = 100;
        propertyValuesRoom.put(npRoomBathroom, new DefaultEntity(etHouse, PkValue.of(nextId++)));
        propertyValuesRoom.put(npRoomHouse, new DefaultEntity(etHouse, PkValue.of(nextId++)));

        EntitySetImpl rooms = new EntitySetImpl(etRoom);
        rooms.add(new DefaultEntity(etRoom, PkValue.of(nextId++)));
        rooms.add(new DefaultEntity(etRoom, PkValue.of(nextId++)));
        propertyValuesHouse.put(npHouseRooms, rooms);
        propertyValuesHouse.put(npHouseBathrooms, rooms);

        EntitySetImpl houses = new EntitySetImpl(etHouse);
        houses.add(new DefaultEntity(etHouse, PkValue.of(nextId++)));
        houses.add(new DefaultEntity(etHouse, PkValue.of(nextId++)));
        propertyValuesStreet.put(npStreetHouses, houses);

        EntitySetImpl streets = new EntitySetImpl(etStreet);
        streets.add(new DefaultEntity(etStreet, PkValue.of(nextId++)));
        streets.add(new DefaultEntity(etStreet, PkValue.of(nextId++)));
        propertyValuesHouse.put(npHouseStreets, streets);

        for (EntityType et : modelRegistry.getEntityTypes()) {
            assertTrue(propertyValues.containsKey(et), "Missing values for " + et);
            final Map<Property, Object> propertValuesEt = propertyValues.get(et);
            for (EntityPropertyMain ep : et.getEntityProperties()) {
                assertTrue(propertValuesEt.containsKey(ep), "Missing value for " + et + "/" + ep);
            }
            for (NavigationPropertyMain np : et.getNavigationProperties()) {
                assertTrue(propertValuesEt.containsKey(np), "Missing value for " + et + "/" + np);
            }
        }

        Assertions.assertEquals(108, nextId);
        return propertyValues;
    }
}
