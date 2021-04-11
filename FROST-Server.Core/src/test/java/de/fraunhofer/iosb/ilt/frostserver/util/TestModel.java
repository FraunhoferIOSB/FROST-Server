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
import de.fraunhofer.iosb.ilt.frostserver.model.ext.TimeValue;
import static de.fraunhofer.iosb.ilt.frostserver.model.ext.TypeReferencesHelper.TYPE_REFERENCE_NUMBER;
import static de.fraunhofer.iosb.ilt.frostserver.model.ext.TypeReferencesHelper.TYPE_REFERENCE_STRING;
import static de.fraunhofer.iosb.ilt.frostserver.model.ext.TypeReferencesHelper.TYPE_REFERENCE_TIMEVALUE;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain.NavigationPropertyEntity;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain.NavigationPropertyEntitySet;
import de.fraunhofer.iosb.ilt.frostserver.property.Property;
import java.util.HashMap;
import java.util.Map;
import org.junit.Assert;

/**
 *
 * @author hylke
 */
public class TestModel {

    public final EntityPropertyMain<String> epName = new EntityPropertyMain<>("Name", TYPE_REFERENCE_STRING);
    public final EntityPropertyMain<Number> epValue = new EntityPropertyMain<>("Value", TYPE_REFERENCE_NUMBER);
    public final EntityPropertyMain<TimeValue> epTime = new EntityPropertyMain<>("Time", TYPE_REFERENCE_TIMEVALUE);

    public final NavigationPropertyEntity npHouse = new NavigationPropertyEntity("House");
    public final NavigationPropertyEntitySet npHouses = new NavigationPropertyEntitySet("Houses");
    public final NavigationPropertyEntity npRoom = new NavigationPropertyEntity("Room");
    public final NavigationPropertyEntitySet npRooms = new NavigationPropertyEntitySet("Rooms");

    public final EntityType etHouse = new EntityType("House", "Houses");
    public final EntityType etRoom = new EntityType("Room", "Rooms");

    public void initModel(ModelRegistry modelRegistry) {
        modelRegistry.registerEntityProperty(ModelRegistry.EP_ID);
        modelRegistry.registerEntityProperty(epName);
        modelRegistry.registerEntityProperty(epValue);
        modelRegistry.registerEntityProperty(epTime);
        modelRegistry.registerEntityProperty(ModelRegistry.EP_PROPERTIES);
        modelRegistry.registerEntityProperty(ModelRegistry.EP_SELFLINK);

        modelRegistry.registerNavProperty(npHouse);
        modelRegistry.registerNavProperty(npHouses);
        modelRegistry.registerNavProperty(npRoom);
        modelRegistry.registerNavProperty(npRooms);

        modelRegistry.registerEntityType(etHouse)
                .registerProperty(ModelRegistry.EP_ID, false)
                .registerProperty(ModelRegistry.EP_SELFLINK, false)
                .registerProperty(epName, true)
                .registerProperty(epValue, false)
                .registerProperty(ModelRegistry.EP_PROPERTIES, false)
                .registerProperty(npRooms, false);
        modelRegistry.registerEntityType(etRoom)
                .registerProperty(ModelRegistry.EP_ID, false)
                .registerProperty(ModelRegistry.EP_SELFLINK, false)
                .registerProperty(epName, true)
                .registerProperty(epValue, false)
                .registerProperty(epTime, false)
                .registerProperty(ModelRegistry.EP_PROPERTIES, false)
                .registerProperty(npHouse, true)
                .registerProperty(npHouses, false);
    }

    public Map<Property, Object> getTextPropertyValues(ModelRegistry modelRegistry) {
        Map<Property, Object> propertyValues = new HashMap<>();
        propertyValues.put(ModelRegistry.EP_ID, new IdLong(1));
        propertyValues.put(epName, "myName");
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("key1", "value1");
        parameters.put("key2", 2);
        propertyValues.put(ModelRegistry.EP_PROPERTIES, parameters);
        propertyValues.put(ModelRegistry.EP_SELFLINK, "http://my.self/link");

        for (EntityPropertyMain ep : modelRegistry.getEntityProperties()) {
            Assert.assertTrue("Missing value for " + ep, propertyValues.containsKey(ep));
        }

        int nextId = 100;
        propertyValues.put(npHouse, new DefaultEntity(etHouse, new IdLong(nextId++)));
        propertyValues.put(npRoom, new DefaultEntity(etRoom, new IdLong(nextId++)));

        EntitySetImpl rooms = new EntitySetImpl(etRoom);
        rooms.add(new DefaultEntity(etRoom, new IdLong(nextId++)));
        rooms.add(new DefaultEntity(etRoom, new IdLong(nextId++)));
        propertyValues.put(npRooms, rooms);

        EntitySetImpl houses = new EntitySetImpl(etHouse);
        houses.add(new DefaultEntity(etHouse, new IdLong(nextId++)));
        houses.add(new DefaultEntity(etHouse, new IdLong(nextId++)));
        propertyValues.put(npHouses, houses);
        return propertyValues;
    }
}
