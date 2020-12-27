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
        PathElementEntitySet containingSet = new PathElementEntitySet(pluginMultiDatastream.MULTI_DATASTREAM, null);

        Entity entity = new DefaultEntity(pluginMultiDatastream.MULTI_DATASTREAM);
        Assert.assertFalse(isEntityComplete(entity, containingSet));

        entity.setProperty(pluginCoreModel.EP_NAME, "Test MultiDatastream");
        Assert.assertFalse(isEntityComplete(entity, containingSet));

        entity.setProperty(pluginCoreModel.EP_DESCRIPTION, "Test Description");
        Assert.assertFalse(isEntityComplete(entity, containingSet));

        List<UnitOfMeasurement> unitOfMeasurements = new ArrayList<>();
        unitOfMeasurements.add(new UnitOfMeasurement().setName("temperature").setDefinition("SomeUrl").setSymbol("degC"));
        entity.setProperty(pluginMultiDatastream.EP_UNITOFMEASUREMENTS, unitOfMeasurements);
        Assert.assertFalse(isEntityComplete(entity, containingSet));

        entity.setProperty(pluginCoreModel.EP_OBSERVATIONTYPE, "http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_ComplexObservation");
        Assert.assertFalse(isEntityComplete(entity, containingSet));

        List<String> multiObservationDataTypes = new ArrayList<>();
        multiObservationDataTypes.add("http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement");
        entity.setProperty(pluginMultiDatastream.EP_MULTIOBSERVATIONDATATYPES, multiObservationDataTypes);
        Assert.assertFalse(isEntityComplete(entity, containingSet));

        entity.setProperty(pluginCoreModel.NP_THING, new DefaultEntity(pluginCoreModel.THING).setId(new IdLong(1)));
        Assert.assertFalse(isEntityComplete(entity, containingSet));

        entity.setProperty(pluginCoreModel.NP_SENSOR, new DefaultEntity(pluginCoreModel.SENSOR).setId(new IdLong(2)));
        Assert.assertFalse(isEntityComplete(entity, containingSet));

        EntitySet observedProperties = new EntitySetImpl(pluginCoreModel.OBSERVED_PROPERTY);
        observedProperties.add(new DefaultEntity(pluginCoreModel.OBSERVED_PROPERTY).setId(new IdLong(3)));
        entity.setProperty(pluginCoreModel.NP_OBSERVEDPROPERTIES, observedProperties);
        Assert.assertTrue(isEntityComplete(entity, containingSet));

        entity.setProperty(pluginCoreModel.NP_THING, null);
        Assert.assertFalse(isEntityComplete(entity, containingSet));
        Assert.assertTrue(isEntityComplete(entity, new PathElementEntitySet(pluginMultiDatastream.MULTI_DATASTREAM, new PathElementEntity(new IdLong(2), pluginCoreModel.THING, null))));

        Assert.assertFalse(isEntityComplete(entity, new PathElementEntitySet(pluginCoreModel.DATASTREAM, null)));

        unitOfMeasurements.add(new UnitOfMeasurement().setName("temperature").setDefinition("SomeUrl").setSymbol("degC"));
        entity.setProperty(pluginMultiDatastream.EP_UNITOFMEASUREMENTS, unitOfMeasurements);
        Assert.assertFalse(isEntityComplete(entity, containingSet));

        multiObservationDataTypes.add("http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement");
        entity.setProperty(pluginMultiDatastream.EP_MULTIOBSERVATIONDATATYPES, multiObservationDataTypes);
        Assert.assertFalse(isEntityComplete(entity, containingSet));

        observedProperties.add(new DefaultEntity(pluginCoreModel.OBSERVED_PROPERTY).setId(new IdLong(3)));
        entity.setProperty(pluginCoreModel.NP_OBSERVEDPROPERTIES, observedProperties);
        Assert.assertTrue(isEntityComplete(entity, containingSet));
    }

    @Test
    public void testObservationComplete() {
        PathElementEntitySet containingSet = new PathElementEntitySet(pluginCoreModel.OBSERVATION, null);
        Entity entity = new DefaultEntity(pluginCoreModel.OBSERVATION);
        Assert.assertFalse(isEntityComplete(entity, containingSet));

        entity.setProperty(pluginCoreModel.EP_RESULT, "result");
        Assert.assertFalse(isEntityComplete(entity, containingSet));

        entity.setProperty(pluginCoreModel.NP_DATASTREAM, new DefaultEntity(pluginCoreModel.DATASTREAM).setId(new IdLong(2)));
        Assert.assertTrue(isEntityComplete(entity, containingSet));

        entity.setProperty(pluginMultiDatastream.NP_MULTIDATASTREAM, new DefaultEntity(pluginMultiDatastream.MULTI_DATASTREAM).setId(new IdLong(2)));
        Assert.assertFalse(isEntityComplete(entity, containingSet));

        entity.setProperty(pluginCoreModel.NP_DATASTREAM, null);
        Assert.assertFalse(isEntityComplete(entity, containingSet));

        entity.setProperty(pluginCoreModel.EP_RESULT, Arrays.asList("result"));
        Assert.assertTrue(isEntityComplete(entity, containingSet));

        Assert.assertFalse(isEntityComplete(entity, new PathElementEntitySet(pluginCoreModel.DATASTREAM, null)));

        entity.setProperty(pluginCoreModel.NP_DATASTREAM, new DefaultEntity(pluginCoreModel.DATASTREAM).setId(new IdLong(2)));
        entity.setProperty(pluginMultiDatastream.NP_MULTIDATASTREAM, null);

        containingSet = new PathElementEntitySet(pluginCoreModel.OBSERVATION, new PathElementEntity(new IdLong(1), pluginCoreModel.DATASTREAM, null));
        entity = new DefaultEntity(pluginCoreModel.OBSERVATION);
        entity.setProperty(pluginCoreModel.EP_RESULT, "result");
        Assert.assertTrue(isEntityComplete(entity, containingSet));

        containingSet = new PathElementEntitySet(pluginCoreModel.OBSERVATION, new PathElementEntity(new IdLong(1), pluginMultiDatastream.MULTI_DATASTREAM, null));
        entity = new DefaultEntity(pluginCoreModel.OBSERVATION);
        entity.setProperty(pluginCoreModel.EP_RESULT, Arrays.asList("result"));
        Assert.assertTrue(isEntityComplete(entity, containingSet));

    }
}
