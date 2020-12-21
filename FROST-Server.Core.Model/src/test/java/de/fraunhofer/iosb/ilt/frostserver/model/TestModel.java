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
package de.fraunhofer.iosb.ilt.frostserver.model;

import de.fraunhofer.iosb.ilt.frostserver.model.core.EntitySetImpl;
import de.fraunhofer.iosb.ilt.frostserver.model.core.IdLong;
import static de.fraunhofer.iosb.ilt.frostserver.model.ext.TypeReferencesHelper.TYPE_REFERENCE_STRING;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.property.Property;
import java.util.HashMap;
import java.util.Map;
import org.junit.Assert;

/**
 *
 * @author hylke
 */
public class TestModel {

    public final EntityPropertyMain<String> EP_NAME = new EntityPropertyMain<>("Name", TYPE_REFERENCE_STRING);

    public final NavigationPropertyMain.NavigationPropertyEntity NP_HOUSE = new NavigationPropertyMain.NavigationPropertyEntity("House");
    public final NavigationPropertyMain.NavigationPropertyEntitySet NP_HOUSES = new NavigationPropertyMain.NavigationPropertyEntitySet("Houses");
    public final NavigationPropertyMain.NavigationPropertyEntity NP_ROOM = new NavigationPropertyMain.NavigationPropertyEntity("Room");
    public final NavigationPropertyMain.NavigationPropertyEntitySet NP_ROOMS = new NavigationPropertyMain.NavigationPropertyEntitySet("Rooms");

    public final EntityType HOUSE = new EntityType("House", "Houses");
    public final EntityType ROOM = new EntityType("Room", "Rooms");

    public void initModel(ModelRegistry modelRegistry) {
        modelRegistry.registerEntityProperty(ModelRegistry.EP_ID);
        modelRegistry.registerEntityProperty(EP_NAME);
        modelRegistry.registerEntityProperty(ModelRegistry.EP_PROPERTIES);
        modelRegistry.registerEntityProperty(ModelRegistry.EP_SELFLINK);

        modelRegistry.registerNavProperty(NP_HOUSE);
        modelRegistry.registerNavProperty(NP_HOUSES);
        modelRegistry.registerNavProperty(NP_ROOM);
        modelRegistry.registerNavProperty(NP_ROOMS);

        modelRegistry.registerEntityType(HOUSE)
                .registerProperty(ModelRegistry.EP_ID, false)
                .registerProperty(ModelRegistry.EP_SELFLINK, false)
                .registerProperty(EP_NAME, true)
                .registerProperty(ModelRegistry.EP_PROPERTIES, false)
                .registerProperty(NP_ROOMS, false);
        modelRegistry.registerEntityType(ROOM)
                .registerProperty(ModelRegistry.EP_ID, false)
                .registerProperty(ModelRegistry.EP_SELFLINK, false)
                .registerProperty(EP_NAME, true)
                .registerProperty(ModelRegistry.EP_PROPERTIES, false)
                .registerProperty(NP_HOUSE, true);
    }

    public Map<Property, Object> getTextPropertyValues(ModelRegistry modelRegistry) {
        Map<Property, Object> propertyValues = new HashMap<>();
        propertyValues.put(ModelRegistry.EP_ID, new IdLong(1));
        propertyValues.put(EP_NAME, "myName");
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("key1", "value1");
        parameters.put("key2", 2);
        propertyValues.put(ModelRegistry.EP_PROPERTIES, parameters);
        propertyValues.put(ModelRegistry.EP_SELFLINK, "http://my.self/link");

        for (EntityPropertyMain ep : modelRegistry.getEntityProperties()) {
            Assert.assertTrue("Missing value for " + ep, propertyValues.containsKey(ep));
        }

        int nextId = 100;
        propertyValues.put(NP_HOUSE, new DefaultEntity(HOUSE, new IdLong(nextId++)));
        propertyValues.put(NP_ROOM, new DefaultEntity(ROOM, new IdLong(nextId++)));

        EntitySetImpl rooms = new EntitySetImpl(ROOM);
        rooms.add(new DefaultEntity(ROOM, new IdLong(nextId++)));
        rooms.add(new DefaultEntity(ROOM, new IdLong(nextId++)));
        propertyValues.put(NP_ROOMS, rooms);

        EntitySetImpl houses = new EntitySetImpl(HOUSE);
        houses.add(new DefaultEntity(HOUSE, new IdLong(nextId++)));
        houses.add(new DefaultEntity(HOUSE, new IdLong(nextId++)));
        propertyValues.put(NP_HOUSES, houses);
        return propertyValues;
    }
}
