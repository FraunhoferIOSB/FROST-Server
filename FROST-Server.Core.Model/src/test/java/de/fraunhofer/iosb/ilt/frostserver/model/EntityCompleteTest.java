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

import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.model.core.IdLong;
import de.fraunhofer.iosb.ilt.frostserver.path.PathElementEntity;
import de.fraunhofer.iosb.ilt.frostserver.path.PathElementEntitySet;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.IncompleteEntityException;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author scf
 */
public class EntityCompleteTest {

    private static ModelRegistry modelRegistry;
    private static TestModel testModel;

    @BeforeClass
    public static void beforeClass() {
        modelRegistry = new ModelRegistry();
        testModel = new TestModel();
        testModel.initModel(modelRegistry);
        modelRegistry.initFinalise();
    }

    private boolean isEntityComplete(Entity entity, PathElementEntitySet containingSet) {
        try {
            entity.complete(containingSet);
            return true;
        } catch (IncompleteEntityException | IllegalArgumentException e) {
            return false;
        }
    }

    @Test
    public void testEntityComplete() {
        PathElementEntitySet containingSet = new PathElementEntitySet(testModel.ET_ROOM);
        Entity entity = new DefaultEntity(testModel.ET_ROOM);
        Assert.assertFalse(isEntityComplete(entity, containingSet));

        entity.setProperty(testModel.EP_NAME, "name");
        Assert.assertFalse(isEntityComplete(entity, containingSet));

        entity.setProperty(testModel.NP_HOUSE_ROOM, new DefaultEntity(testModel.ET_HOUSE).setId(new IdLong(2)));
        Assert.assertTrue("Entity not complete: " + entity, isEntityComplete(entity, containingSet));

        entity = new DefaultEntity(testModel.ET_ROOM);
        entity.setProperty(testModel.EP_NAME, "Name");
        containingSet = new PathElementEntitySet(testModel.ET_ROOM);
        Assert.assertFalse(isEntityComplete(entity, containingSet));
        containingSet = new PathElementEntitySet(testModel.NP_BATHROOMS_HOUSE, new PathElementEntity(testModel.ET_ROOM, null).setId(new IdLong(1)));
        Assert.assertFalse(isEntityComplete(entity, containingSet));
        containingSet = new PathElementEntitySet(testModel.NP_ROOMS_HOUSE, new PathElementEntity(testModel.ET_HOUSE, null).setId(new IdLong(1)));
        Assert.assertTrue(isEntityComplete(entity, containingSet));

    }
}
