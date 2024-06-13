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
package de.fraunhofer.iosb.ilt.frostserver.serialise;

import static de.fraunhofer.iosb.ilt.frostserver.property.SpecialNames.AT_IOT_ID;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.fraunhofer.iosb.ilt.frostserver.model.DefaultEntity;
import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.ModelRegistry;
import de.fraunhofer.iosb.ilt.frostserver.model.core.EntitySetImpl;
import de.fraunhofer.iosb.ilt.frostserver.model.core.PkValue;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.TimeInstant;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.TimeValue;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain.NavigationPropertyEntity;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain.NavigationPropertyEntitySet;
import de.fraunhofer.iosb.ilt.frostserver.property.Property;
import de.fraunhofer.iosb.ilt.frostserver.property.type.TypeSimplePrimitive;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.constant.IntegerConstant;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author hylke
 */
public class TestModel {

    public final EntityPropertyMain<String> EP_NAME = new EntityPropertyMain<>("name", TypeSimplePrimitive.EDM_STRING, true, false);
    public final EntityPropertyMain<Number> EP_VALUE = new EntityPropertyMain<>("value", TypeSimplePrimitive.EDM_DECIMAL);
    public final EntityPropertyMain<TimeValue> EP_TIME = new EntityPropertyMain<>("time", TypeSimplePrimitive.EDM_DATETIMEOFFSET);

    public final NavigationPropertyEntity NP_HOUSE_ROOM = new NavigationPropertyEntity("House", true);
    public final NavigationPropertyEntitySet NP_ROOMS_HOUSE = new NavigationPropertyEntitySet("Rooms");

    public final NavigationPropertyEntity NP_BATHROOMFOR_ROOM = new NavigationPropertyEntity("BathroomFor", false);
    public final NavigationPropertyEntitySet NP_BATHROOMS_HOUSE = new NavigationPropertyEntitySet("Bathrooms");

    public final NavigationPropertyEntitySet NP_STREETS_HOUSE = new NavigationPropertyEntitySet("Streets");
    public final NavigationPropertyEntitySet NP_HOUSES_STREET = new NavigationPropertyEntitySet("Houses");

    public final EntityType ET_STREET = new EntityType("Street", "Streets");
    public final EntityType ET_HOUSE = new EntityType("House", "Houses");
    public final EntityType ET_ROOM = new EntityType("Room", "Rooms");

    public void initModel(ModelRegistry modelRegistry, String idType) {
        NP_BATHROOMS_HOUSE.setInverses(NP_BATHROOMFOR_ROOM);
        NP_BATHROOMS_HOUSE.setEntityType(ET_ROOM);
        NP_BATHROOMFOR_ROOM.setEntityType(ET_HOUSE);
        NP_HOUSE_ROOM.setInverses(NP_ROOMS_HOUSE);
        NP_STREETS_HOUSE.setInverses(NP_STREETS_HOUSE);

        modelRegistry.registerEntityType(ET_STREET);
        ET_STREET.registerProperty(new EntityPropertyMain<>(AT_IOT_ID, modelRegistry.getPropertyType(idType)).setAliases("id"))
                .registerProperty(EP_NAME)
                .registerProperty(NP_HOUSES_STREET);
        modelRegistry.registerEntityType(ET_HOUSE);
        ET_HOUSE.registerProperty(new EntityPropertyMain<>(AT_IOT_ID, modelRegistry.getPropertyType(idType)).setAliases("id"))
                .registerProperty(EP_NAME)
                .registerProperty(EP_VALUE)
                .registerProperty(ModelRegistry.EP_PROPERTIES)
                .registerProperty(NP_STREETS_HOUSE)
                .registerProperty(NP_ROOMS_HOUSE)
                .registerProperty(NP_BATHROOMS_HOUSE);
        modelRegistry.registerEntityType(ET_ROOM);
        ET_ROOM.registerProperty(new EntityPropertyMain<>(AT_IOT_ID, modelRegistry.getPropertyType(idType)).setAliases("id"))
                .registerProperty(EP_NAME)
                .registerProperty(EP_VALUE)
                .registerProperty(EP_TIME)
                .registerProperty(ModelRegistry.EP_PROPERTIES)
                .registerProperty(NP_HOUSE_ROOM)
                .registerProperty(NP_BATHROOMFOR_ROOM);
    }

    public Map<EntityType, Map<Property, Object>> getTestPropertyValues(ModelRegistry modelRegistry) {
        Map<EntityType, Map<Property, Object>> propertyValues = new HashMap<>();
        Map<Property, Object> propertyValuesStreet = new HashMap<>();
        Map<Property, Object> propertyValuesHouse = new HashMap<>();
        Map<Property, Object> propertyValuesRoom = new HashMap<>();
        propertyValues.put(ET_STREET, propertyValuesStreet);
        propertyValues.put(ET_HOUSE, propertyValuesHouse);
        propertyValues.put(ET_ROOM, propertyValuesRoom);

        propertyValuesStreet.put(ET_STREET.getPrimaryKey().getKeyProperties().get(0), new IntegerConstant(1));
        propertyValuesHouse.put(ET_HOUSE.getPrimaryKey().getKeyProperties().get(0), new IntegerConstant(1));
        propertyValuesRoom.put(ET_ROOM.getPrimaryKey().getKeyProperties().get(0), new IntegerConstant(1));
        propertyValuesStreet.put(EP_NAME, "StreetName");
        propertyValuesHouse.put(EP_NAME, "HouseName");
        propertyValuesRoom.put(EP_NAME, "RoomName");
        propertyValuesHouse.put(EP_VALUE, 6);
        propertyValuesRoom.put(EP_VALUE, 7);
        propertyValuesRoom.put(EP_TIME, TimeInstant.now());

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("key1", "value1");
        parameters.put("key2", 2);
        propertyValuesHouse.put(ModelRegistry.EP_PROPERTIES, parameters);
        propertyValuesRoom.put(ModelRegistry.EP_PROPERTIES, parameters);
        propertyValuesStreet.put(ModelRegistry.EP_SELFLINK, "http://my.self/link");
        propertyValuesHouse.put(ModelRegistry.EP_SELFLINK, "http://my.self/link");
        propertyValuesRoom.put(ModelRegistry.EP_SELFLINK, "http://my.self/link");

        long nextId = 100;
        propertyValuesRoom.put(NP_BATHROOMFOR_ROOM, new DefaultEntity(ET_HOUSE, PkValue.of(nextId++)));
        propertyValuesRoom.put(NP_HOUSE_ROOM, new DefaultEntity(ET_HOUSE, PkValue.of(nextId++)));

        EntitySetImpl rooms = new EntitySetImpl(ET_ROOM);
        rooms.add(new DefaultEntity(ET_ROOM, PkValue.of(nextId++)));
        rooms.add(new DefaultEntity(ET_ROOM, PkValue.of(nextId++)));
        propertyValuesHouse.put(NP_ROOMS_HOUSE, rooms);
        propertyValuesHouse.put(NP_BATHROOMS_HOUSE, rooms);

        EntitySetImpl houses = new EntitySetImpl(ET_HOUSE);
        houses.add(new DefaultEntity(ET_HOUSE, PkValue.of(nextId++)));
        houses.add(new DefaultEntity(ET_HOUSE, PkValue.of(nextId++)));
        propertyValuesStreet.put(NP_HOUSES_STREET, houses);

        EntitySetImpl streets = new EntitySetImpl(ET_STREET);
        streets.add(new DefaultEntity(ET_STREET, PkValue.of(nextId++)));
        streets.add(new DefaultEntity(ET_STREET, PkValue.of(nextId++)));
        propertyValuesHouse.put(NP_STREETS_HOUSE, streets);

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

        return propertyValues;
    }
}
