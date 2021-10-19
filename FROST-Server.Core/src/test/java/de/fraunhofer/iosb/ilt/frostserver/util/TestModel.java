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

import de.fraunhofer.iosb.ilt.frostserver.model.DefaultEntity;
import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.ModelRegistry;
import de.fraunhofer.iosb.ilt.frostserver.model.core.EntitySetImpl;
import de.fraunhofer.iosb.ilt.frostserver.model.core.IdLong;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.TimeInstant;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.TimeValue;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.property.Property;
import de.fraunhofer.iosb.ilt.frostserver.property.type.TypeSimplePrimitive;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author hylke
 */
public class TestModel {

    public final EntityPropertyMain<String> EP_NAME = new EntityPropertyMain<>("name", TypeSimplePrimitive.EDM_STRING);
    public final EntityPropertyMain<Number> EP_VALUE = new EntityPropertyMain<>("value", TypeSimplePrimitive.EDM_DECIMAL);
    public final EntityPropertyMain<TimeValue> EP_TIME = new EntityPropertyMain<>("time", TypeSimplePrimitive.EDM_DATETIMEOFFSET);

    public final NavigationPropertyMain.NavigationPropertyEntity NP_HOUSE = new NavigationPropertyMain.NavigationPropertyEntity("House");
    public final NavigationPropertyMain.NavigationPropertyEntitySet NP_HOUSES = new NavigationPropertyMain.NavigationPropertyEntitySet("Houses");
    public final NavigationPropertyMain.NavigationPropertyEntity NP_ROOM = new NavigationPropertyMain.NavigationPropertyEntity("Room");
    public final NavigationPropertyMain.NavigationPropertyEntitySet NP_ROOMS = new NavigationPropertyMain.NavigationPropertyEntitySet("Rooms");

    public final EntityType ET_HOUSE = new EntityType("House", "Houses");
    public final EntityType ET_ROOM = new EntityType("Room", "Rooms");

    public void initModel(ModelRegistry modelRegistry) {
        modelRegistry.registerEntityType(ET_HOUSE);
        ET_HOUSE.registerProperty(ModelRegistry.EP_ID_LONG, false)
                .registerProperty(ModelRegistry.EP_SELFLINK, false)
                .registerProperty(EP_NAME, true)
                .registerProperty(EP_VALUE, false)
                .registerProperty(ModelRegistry.EP_PROPERTIES, false)
                .registerProperty(NP_ROOMS, false);
        modelRegistry.registerEntityType(ET_ROOM);
        ET_ROOM.registerProperty(ModelRegistry.EP_ID_LONG, false)
                .registerProperty(ModelRegistry.EP_SELFLINK, false)
                .registerProperty(EP_NAME, true)
                .registerProperty(EP_VALUE, false)
                .registerProperty(EP_TIME, false)
                .registerProperty(ModelRegistry.EP_PROPERTIES, false)
                .registerProperty(NP_ROOMS, false)
                .registerProperty(NP_HOUSE, true);
    }

    public Map<EntityType, Map<Property, Object>> getTestPropertyValues(ModelRegistry modelRegistry) {
        Map<EntityType, Map<Property, Object>> propertyValues = new HashMap<>();
        Map<Property, Object> propertyValuesHouse = new HashMap<>();
        Map<Property, Object> propertyValuesRoom = new HashMap<>();
        propertyValues.put(ET_HOUSE, propertyValuesHouse);
        propertyValues.put(ET_ROOM, propertyValuesRoom);

        propertyValuesHouse.put(ModelRegistry.EP_ID_LONG, new IdLong(1));
        propertyValuesRoom.put(ModelRegistry.EP_ID_LONG, new IdLong(1));
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

        int nextId = 100;
        propertyValuesRoom.put(NP_HOUSE, new DefaultEntity(ET_HOUSE, new IdLong(nextId++)));

        EntitySetImpl rooms = new EntitySetImpl(ET_ROOM);
        rooms.add(new DefaultEntity(ET_ROOM, new IdLong(nextId++)));
        rooms.add(new DefaultEntity(ET_ROOM, new IdLong(nextId++)));
        propertyValuesHouse.put(NP_ROOMS, rooms);

        EntitySetImpl houses = new EntitySetImpl(ET_HOUSE);
        houses.add(new DefaultEntity(ET_HOUSE, new IdLong(nextId++)));
        houses.add(new DefaultEntity(ET_HOUSE, new IdLong(nextId++)));
        propertyValuesRoom.put(NP_ROOMS, houses);
        return propertyValues;
    }
}
