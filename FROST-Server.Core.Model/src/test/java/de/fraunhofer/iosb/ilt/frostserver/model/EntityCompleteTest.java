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
import java.util.Arrays;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author scf
 */
public class EntityCompleteTest {

    private static ModelRegistry modelRegistry;

    @BeforeClass
    public static void beforeClass() {
        modelRegistry = new ModelRegistry();
        modelRegistry.initDefaultTypes();
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
    public void testObservationComplete() {
        PathElementEntitySet containingSet = new PathElementEntitySet(modelRegistry.OBSERVATION, null);
        Entity entity = new DefaultEntity(modelRegistry.OBSERVATION);
        Assert.assertFalse(isEntityComplete(entity, containingSet));

        entity.setProperty(modelRegistry.EP_RESULT, "result");
        Assert.assertFalse(isEntityComplete(entity, containingSet));

        entity.setProperty(modelRegistry.NP_DATASTREAM, new DefaultEntity(modelRegistry.DATASTREAM).setId(new IdLong(2)));
        Assert.assertTrue(isEntityComplete(entity, containingSet));

        entity.setProperty(modelRegistry.EP_RESULT, Arrays.asList("result"));
        Assert.assertTrue(isEntityComplete(entity, containingSet));

        Assert.assertFalse(isEntityComplete(entity, new PathElementEntitySet(modelRegistry.DATASTREAM, null)));

        entity.setProperty(modelRegistry.NP_DATASTREAM, new DefaultEntity(modelRegistry.DATASTREAM).setId(new IdLong(2)));

        containingSet = new PathElementEntitySet(modelRegistry.OBSERVATION, new PathElementEntity(new IdLong(1), modelRegistry.DATASTREAM, null));
        entity = new DefaultEntity(modelRegistry.OBSERVATION);
        entity.setProperty(modelRegistry.EP_RESULT, "result");
        Assert.assertTrue(isEntityComplete(entity, containingSet));

    }
}
