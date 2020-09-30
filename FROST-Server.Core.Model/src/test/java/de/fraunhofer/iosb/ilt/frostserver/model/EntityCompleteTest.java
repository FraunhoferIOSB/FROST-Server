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
import de.fraunhofer.iosb.ilt.frostserver.model.core.EntitySet;
import de.fraunhofer.iosb.ilt.frostserver.model.core.EntitySetImpl;
import de.fraunhofer.iosb.ilt.frostserver.model.core.IdLong;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.UnitOfMeasurement;
import de.fraunhofer.iosb.ilt.frostserver.path.PathElementEntity;
import de.fraunhofer.iosb.ilt.frostserver.path.PathElementEntitySet;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.IncompleteEntityException;
import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 *
 * @author scf
 */
public class EntityCompleteTest {

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    private boolean isEntityComplete(Entity entity, PathElementEntitySet containingSet) {
        try {
            entity.complete(containingSet);
            return true;
        } catch (IncompleteEntityException | IllegalStateException e) {
            return false;
        }
    }

    @Test
    public void testMultiDatastreamComplete() {
        PathElementEntitySet containingSet = new PathElementEntitySet(EntityType.MULTI_DATASTREAM, null);

        Entity entity = new DefaultEntity(EntityType.MULTI_DATASTREAM);
        Assert.assertFalse(isEntityComplete(entity, containingSet));

        entity.setProperty(EntityPropertyMain.NAME, "Test MultiDatastream");
        Assert.assertFalse(isEntityComplete(entity, containingSet));

        entity.setProperty(EntityPropertyMain.DESCRIPTION, "Test Description");
        Assert.assertFalse(isEntityComplete(entity, containingSet));

        List<UnitOfMeasurement> unitOfMeasurements = new ArrayList<>();
        unitOfMeasurements.add(new UnitOfMeasurement().setName("temperature").setDefinition("SomeUrl").setSymbol("degC"));
        entity.setProperty(EntityPropertyMain.UNITOFMEASUREMENTS, unitOfMeasurements);
        Assert.assertFalse(isEntityComplete(entity, containingSet));

        entity.setProperty(EntityPropertyMain.OBSERVATIONTYPE, "http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_ComplexObservation");
        Assert.assertFalse(isEntityComplete(entity, containingSet));

        List<String> multiObservationDataTypes = new ArrayList<>();
        multiObservationDataTypes.add("http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement");
        entity.setProperty(EntityPropertyMain.MULTIOBSERVATIONDATATYPES, multiObservationDataTypes);
        Assert.assertFalse(isEntityComplete(entity, containingSet));

        entity.setProperty(NavigationPropertyMain.THING, new DefaultEntity(EntityType.THING).setId(new IdLong(1)));
        Assert.assertFalse(isEntityComplete(entity, containingSet));

        entity.setProperty(NavigationPropertyMain.SENSOR, new DefaultEntity(EntityType.SENSOR).setId(new IdLong(2)));
        Assert.assertFalse(isEntityComplete(entity, containingSet));

        EntitySet observedProperties = new EntitySetImpl<>(EntityType.OBSERVED_PROPERTY);
        observedProperties.add(new DefaultEntity(EntityType.OBSERVED_PROPERTY).setId(new IdLong(3)));
        entity.setProperty(NavigationPropertyMain.OBSERVEDPROPERTIES, observedProperties);
        Assert.assertTrue(isEntityComplete(entity, containingSet));

        entity.setProperty(NavigationPropertyMain.THING, null);
        Assert.assertFalse(isEntityComplete(entity, containingSet));
        Assert.assertTrue(isEntityComplete(entity, new PathElementEntitySet(EntityType.MULTI_DATASTREAM, new PathElementEntity(new IdLong(2), EntityType.THING, null))));

        Assert.assertFalse(isEntityComplete(entity, new PathElementEntitySet(EntityType.DATASTREAM, null)));

        unitOfMeasurements.add(new UnitOfMeasurement().setName("temperature").setDefinition("SomeUrl").setSymbol("degC"));
        entity.setProperty(EntityPropertyMain.UNITOFMEASUREMENTS, unitOfMeasurements);
        Assert.assertFalse(isEntityComplete(entity, containingSet));

        multiObservationDataTypes.add("http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement");
        entity.setProperty(EntityPropertyMain.MULTIOBSERVATIONDATATYPES, multiObservationDataTypes);
        Assert.assertFalse(isEntityComplete(entity, containingSet));

        observedProperties.add(new DefaultEntity(EntityType.OBSERVED_PROPERTY).setId(new IdLong(3)));
        entity.setProperty(NavigationPropertyMain.OBSERVEDPROPERTIES, observedProperties);
        Assert.assertTrue(isEntityComplete(entity, containingSet));
    }

    @Test
    public void testObservationComplete() {
        PathElementEntitySet containingSet = new PathElementEntitySet(EntityType.OBSERVATION, null);
        Entity entity = new DefaultEntity(EntityType.OBSERVATION);
        Assert.assertFalse(isEntityComplete(entity, containingSet));

        entity.setProperty(EntityPropertyMain.RESULT, "result");
        Assert.assertFalse(isEntityComplete(entity, containingSet));

        entity.setProperty(NavigationPropertyMain.DATASTREAM, new DefaultEntity(EntityType.DATASTREAM).setId(new IdLong(2)));
        Assert.assertTrue(isEntityComplete(entity, containingSet));

        entity.setProperty(NavigationPropertyMain.MULTIDATASTREAM, new DefaultEntity(EntityType.MULTI_DATASTREAM).setId(new IdLong(2)));
        Assert.assertFalse(isEntityComplete(entity, containingSet));

        entity.setProperty(NavigationPropertyMain.DATASTREAM, null);
        Assert.assertTrue(isEntityComplete(entity, containingSet));

        Assert.assertFalse(isEntityComplete(entity, new PathElementEntitySet(EntityType.DATASTREAM, null)));

        containingSet = new PathElementEntitySet(EntityType.OBSERVATION, new PathElementEntity(new IdLong(1), EntityType.DATASTREAM, null));
        entity = new DefaultEntity(EntityType.OBSERVATION);
        entity.setProperty(EntityPropertyMain.RESULT, "result");
        Assert.assertTrue(isEntityComplete(entity, containingSet));

        containingSet = new PathElementEntitySet(EntityType.OBSERVATION, new PathElementEntity(new IdLong(1), EntityType.MULTI_DATASTREAM, null));
        entity = new DefaultEntity(EntityType.OBSERVATION);
        entity.setProperty(EntityPropertyMain.RESULT, "result");
        Assert.assertTrue(isEntityComplete(entity, containingSet));

    }
}
