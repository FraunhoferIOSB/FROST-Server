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
package de.fraunhofer.iosb.ilt.sta.model;

import de.fraunhofer.iosb.ilt.sta.deserialize.EntityParser;
import de.fraunhofer.iosb.ilt.sta.model.builder.DatastreamBuilder;
import de.fraunhofer.iosb.ilt.sta.model.builder.MultiDatastreamBuilder;
import de.fraunhofer.iosb.ilt.sta.model.builder.ObservedPropertyBuilder;
import de.fraunhofer.iosb.ilt.sta.model.builder.SensorBuilder;
import de.fraunhofer.iosb.ilt.sta.model.builder.ThingBuilder;
import de.fraunhofer.iosb.ilt.sta.model.builder.UnitOfMeasurementBuilder;
import de.fraunhofer.iosb.ilt.sta.model.core.Entity;
import de.fraunhofer.iosb.ilt.sta.model.core.EntitySet;
import de.fraunhofer.iosb.ilt.sta.model.core.EntitySetImpl;
import de.fraunhofer.iosb.ilt.sta.model.ext.UnitOfMeasurement;
import de.fraunhofer.iosb.ilt.sta.model.id.LongId;
import de.fraunhofer.iosb.ilt.sta.path.EntityPathElement;
import de.fraunhofer.iosb.ilt.sta.path.EntitySetPathElement;
import de.fraunhofer.iosb.ilt.sta.path.EntityType;
import de.fraunhofer.iosb.ilt.sta.util.IncompleteEntityException;
import java.util.ArrayList;
import java.util.List;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 *
 * @author scf
 */
public class EntityCompleteTest {

    private EntityParser entityParser;

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Before
    public void setUp() {
        entityParser = new EntityParser(LongId.class);
    }

    @After
    public void tearDown() {
    }

    private boolean isEntityComplete(Entity entity, EntitySetPathElement containingSet) {
        try {
            entity.complete(containingSet);
            return true;
        } catch (IncompleteEntityException | IllegalStateException e) {
            return false;
        }
    }

    @Test
    public void testMultiDatastreamComplete() {
        EntitySetPathElement containingSet = new EntitySetPathElement(EntityType.MultiDatastream, null);

        MultiDatastream entity = new MultiDatastream();
        Assert.assertFalse(isEntityComplete(entity, containingSet));

        entity.setName("Test MultiDatastream");
        Assert.assertFalse(isEntityComplete(entity, containingSet));

        entity.setDescription("Test Description");
        Assert.assertFalse(isEntityComplete(entity, containingSet));

        List<UnitOfMeasurement> unitOfMeasurements = new ArrayList<>();
        unitOfMeasurements.add(new UnitOfMeasurementBuilder().setName("temperature").setDefinition("SomeUrl").setSymbol("degC").build());
        entity.setUnitOfMeasurements(unitOfMeasurements);
        Assert.assertFalse(isEntityComplete(entity, containingSet));

        entity.setObservationType("http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_ComplexObservation");
        Assert.assertFalse(isEntityComplete(entity, containingSet));

        List<String> multiObservationDataTypes = new ArrayList<>();
        multiObservationDataTypes.add("http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement");
        entity.setMultiObservationDataTypes(multiObservationDataTypes);
        Assert.assertFalse(isEntityComplete(entity, containingSet));

        entity.setThing(new ThingBuilder().setId(new LongId(1)).build());
        Assert.assertFalse(isEntityComplete(entity, containingSet));

        entity.setSensor(new SensorBuilder().setId(new LongId(2)).build());
        Assert.assertFalse(isEntityComplete(entity, containingSet));

        EntitySet<ObservedProperty> observedProperties = new EntitySetImpl<>(EntityType.ObservedProperty);
        observedProperties.add(new ObservedPropertyBuilder().setId(new LongId(3)).build());
        entity.setObservedProperties(observedProperties);
        Assert.assertTrue(isEntityComplete(entity, containingSet));

        entity.setThing(null);
        Assert.assertFalse(isEntityComplete(entity, containingSet));
        Assert.assertTrue(isEntityComplete(entity, new EntitySetPathElement(EntityType.MultiDatastream, new EntityPathElement(new LongId(2), EntityType.Thing, null))));

        Assert.assertFalse(isEntityComplete(entity, new EntitySetPathElement(EntityType.Datastream, null)));

        unitOfMeasurements.add(new UnitOfMeasurementBuilder().setName("temperature").setDefinition("SomeUrl").setSymbol("degC").build());
        entity.setUnitOfMeasurements(unitOfMeasurements);
        Assert.assertFalse(isEntityComplete(entity, containingSet));

        multiObservationDataTypes.add("http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement");
        entity.setMultiObservationDataTypes(multiObservationDataTypes);
        Assert.assertFalse(isEntityComplete(entity, containingSet));

        observedProperties.add(new ObservedPropertyBuilder().setId(new LongId(3)).build());
        entity.setObservedProperties(observedProperties);
        Assert.assertTrue(isEntityComplete(entity, containingSet));
    }

    @Test
    public void testObservationComplete() {
        EntitySetPathElement containingSet = new EntitySetPathElement(EntityType.Observation, null);
        Observation entity = new Observation();
        Assert.assertFalse(isEntityComplete(entity, containingSet));

        entity.setResult("result");
        Assert.assertFalse(isEntityComplete(entity, containingSet));

        entity.setDatastream(new DatastreamBuilder().setId(new LongId(2)).build());
        Assert.assertTrue(isEntityComplete(entity, containingSet));

        entity.setMultiDatastream(new MultiDatastreamBuilder().setId(new LongId(2)).build());
        Assert.assertFalse(isEntityComplete(entity, containingSet));

        entity.setDatastream(null);
        Assert.assertTrue(isEntityComplete(entity, containingSet));

        Assert.assertFalse(isEntityComplete(entity, new EntitySetPathElement(EntityType.Datastream, null)));

        containingSet = new EntitySetPathElement(EntityType.Observation, new EntityPathElement(new LongId(1), EntityType.Datastream, null));
        entity = new Observation();
        entity.setResult("result");
        Assert.assertTrue(isEntityComplete(entity, containingSet));

        containingSet = new EntitySetPathElement(EntityType.Observation, new EntityPathElement(new LongId(1), EntityType.MultiDatastream, null));
        entity = new Observation();
        entity.setResult("result");
        Assert.assertTrue(isEntityComplete(entity, containingSet));

    }
}
