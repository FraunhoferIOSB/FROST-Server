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
import de.fraunhofer.iosb.ilt.frostserver.plugin.coremodel.PluginCoreModel;
import de.fraunhofer.iosb.ilt.frostserver.plugin.multidatastream.PluginMultiDatastream;
import de.fraunhofer.iosb.ilt.frostserver.query.QueryDefaults;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.IncompleteEntityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author scf
 */
public class EntityCompleteTest {

    private static CoreSettings coreSettings;
    private static QueryDefaults queryDefaults;
    private static ModelRegistry modelRegistry;
    private static PluginCoreModel pluginCoreModel;
    private static PluginMultiDatastream pluginMultiDatastream;

    @BeforeClass
    public static void beforeClass() {
        if (queryDefaults == null) {
            coreSettings = new CoreSettings();
            modelRegistry = coreSettings.getModelRegistry();
            queryDefaults = coreSettings.getQueryDefaults();
            queryDefaults.setUseAbsoluteNavigationLinks(false);
            pluginCoreModel = new PluginCoreModel();
            pluginCoreModel.init(coreSettings);
            pluginMultiDatastream = new PluginMultiDatastream();
            pluginMultiDatastream.init(coreSettings);
            coreSettings.getPluginManager().registerPlugin(pluginMultiDatastream);
            coreSettings.getPluginManager().initPlugins(null);
        }
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
    public void testMultiDatastreamComplete() {
        PathElementEntitySet containingSet = new PathElementEntitySet(pluginMultiDatastream.etMultiDatastream, null);

        Entity entity = new DefaultEntity(pluginMultiDatastream.etMultiDatastream);
        Assert.assertFalse(isEntityComplete(entity, containingSet));

        entity.setProperty(pluginCoreModel.epName, "Test MultiDatastream");
        Assert.assertFalse(isEntityComplete(entity, containingSet));

        entity.setProperty(pluginCoreModel.epDescription, "Test Description");
        Assert.assertFalse(isEntityComplete(entity, containingSet));

        List<UnitOfMeasurement> unitOfMeasurements = new ArrayList<>();
        unitOfMeasurements.add(new UnitOfMeasurement().setName("temperature").setDefinition("SomeUrl").setSymbol("degC"));
        entity.setProperty(pluginMultiDatastream.epUnitOfMeasurements, unitOfMeasurements);
        Assert.assertFalse(isEntityComplete(entity, containingSet));

        entity.setProperty(pluginCoreModel.epObservationType, "http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_ComplexObservation");
        Assert.assertFalse(isEntityComplete(entity, containingSet));

        List<String> multiObservationDataTypes = new ArrayList<>();
        multiObservationDataTypes.add("http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement");
        entity.setProperty(pluginMultiDatastream.epMultiObservationDataTypes, multiObservationDataTypes);
        Assert.assertFalse(isEntityComplete(entity, containingSet));

        entity.setProperty(pluginCoreModel.npThing, new DefaultEntity(pluginCoreModel.etThing).setId(new IdLong(1)));
        Assert.assertFalse(isEntityComplete(entity, containingSet));

        entity.setProperty(pluginCoreModel.npSensor, new DefaultEntity(pluginCoreModel.etSensor).setId(new IdLong(2)));
        Assert.assertFalse(isEntityComplete(entity, containingSet));

        EntitySet observedProperties = new EntitySetImpl(pluginCoreModel.etObservedProperty);
        observedProperties.add(new DefaultEntity(pluginCoreModel.etObservedProperty).setId(new IdLong(3)));
        entity.setProperty(pluginCoreModel.npObservedProperties, observedProperties);
        Assert.assertTrue(isEntityComplete(entity, containingSet));

        entity.setProperty(pluginCoreModel.npThing, null);
        Assert.assertFalse(isEntityComplete(entity, containingSet));
        Assert.assertTrue(isEntityComplete(entity, new PathElementEntitySet(pluginMultiDatastream.etMultiDatastream, new PathElementEntity(new IdLong(2), pluginCoreModel.etThing, null))));

        Assert.assertFalse(isEntityComplete(entity, new PathElementEntitySet(pluginCoreModel.etDatastream, null)));

        unitOfMeasurements.add(new UnitOfMeasurement().setName("temperature").setDefinition("SomeUrl").setSymbol("degC"));
        entity.setProperty(pluginMultiDatastream.epUnitOfMeasurements, unitOfMeasurements);
        Assert.assertFalse(isEntityComplete(entity, containingSet));

        multiObservationDataTypes.add("http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement");
        entity.setProperty(pluginMultiDatastream.epMultiObservationDataTypes, multiObservationDataTypes);
        Assert.assertFalse(isEntityComplete(entity, containingSet));

        observedProperties.add(new DefaultEntity(pluginCoreModel.etObservedProperty).setId(new IdLong(3)));
        entity.setProperty(pluginCoreModel.npObservedProperties, observedProperties);
        Assert.assertTrue(isEntityComplete(entity, containingSet));
    }

    @Test
    public void testObservationComplete() {
        PathElementEntitySet containingSet = new PathElementEntitySet(pluginCoreModel.etObservation, null);
        Entity entity = new DefaultEntity(pluginCoreModel.etObservation);
        Assert.assertFalse(isEntityComplete(entity, containingSet));

        entity.setProperty(pluginCoreModel.epResult, "result");
        Assert.assertFalse(isEntityComplete(entity, containingSet));

        entity.setProperty(pluginCoreModel.npDatastream, new DefaultEntity(pluginCoreModel.etDatastream).setId(new IdLong(2)));
        Assert.assertTrue(isEntityComplete(entity, containingSet));

        entity.setProperty(pluginMultiDatastream.npMultiDatastream, new DefaultEntity(pluginMultiDatastream.etMultiDatastream).setId(new IdLong(2)));
        Assert.assertFalse(isEntityComplete(entity, containingSet));

        entity.setProperty(pluginCoreModel.npDatastream, null);
        Assert.assertFalse(isEntityComplete(entity, containingSet));

        entity.setProperty(pluginCoreModel.epResult, Arrays.asList("result"));
        Assert.assertTrue(isEntityComplete(entity, containingSet));

        Assert.assertFalse(isEntityComplete(entity, new PathElementEntitySet(pluginCoreModel.etDatastream, null)));

        entity.setProperty(pluginCoreModel.npDatastream, new DefaultEntity(pluginCoreModel.etDatastream).setId(new IdLong(2)));
        entity.setProperty(pluginMultiDatastream.npMultiDatastream, null);

        containingSet = new PathElementEntitySet(pluginCoreModel.etObservation, new PathElementEntity(new IdLong(1), pluginCoreModel.etDatastream, null));
        entity = new DefaultEntity(pluginCoreModel.etObservation);
        entity.setProperty(pluginCoreModel.epResult, "result");
        Assert.assertTrue(isEntityComplete(entity, containingSet));

        containingSet = new PathElementEntitySet(pluginCoreModel.etObservation, new PathElementEntity(new IdLong(1), pluginMultiDatastream.etMultiDatastream, null));
        entity = new DefaultEntity(pluginCoreModel.etObservation);
        entity.setProperty(pluginCoreModel.epResult, Arrays.asList("result"));
        Assert.assertTrue(isEntityComplete(entity, containingSet));

    }
}
