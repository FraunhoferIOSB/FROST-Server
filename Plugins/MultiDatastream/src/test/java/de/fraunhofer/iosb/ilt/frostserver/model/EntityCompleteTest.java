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
import static de.fraunhofer.iosb.ilt.frostserver.plugin.multidatastream.PluginMultiDatastream.TAG_ENABLE_MDS_MODEL;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain.NavigationPropertyEntity;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain.NavigationPropertyEntitySet;
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
    private static EntityType etMultiDatastream;
    private static EntityPropertyMain epMultiObservationDataTypes;
    private static EntityPropertyMain epUnitOfMeasurements;
    private static NavigationPropertyEntity npMultiDatastreamObservation;
    private static NavigationPropertyEntity npThingMds;
    private static NavigationPropertyEntity npSensorMds;
    private static NavigationPropertyEntitySet npObservedPropertiesMds;
    private static NavigationPropertyEntitySet npMultiDatastreamsSensor;
    private static NavigationPropertyEntitySet npMultiDatastreamsObsProp;
    private static NavigationPropertyEntitySet npMultiDatastreamsThing;

    @BeforeClass
    public static void beforeClass() {
        if (queryDefaults == null) {
            coreSettings = new CoreSettings();
            coreSettings.getSettings().getProperties().put("plugins." + TAG_ENABLE_MDS_MODEL, "true");
            modelRegistry = coreSettings.getModelRegistry();
            queryDefaults = coreSettings.getQueryDefaults();
            queryDefaults.setUseAbsoluteNavigationLinks(false);
            pluginCoreModel = new PluginCoreModel();
            pluginCoreModel.init(coreSettings);
            pluginMultiDatastream = new PluginMultiDatastream();
            pluginMultiDatastream.init(coreSettings);
            coreSettings.getPluginManager().initPlugins(null);
            etMultiDatastream = modelRegistry.getEntityTypeForName("MultiDatastream");
            epMultiObservationDataTypes = etMultiDatastream.getEntityProperty("multiObservationDataTypes");
            epUnitOfMeasurements = etMultiDatastream.getEntityProperty("unitOfMeasurements");

            npThingMds = (NavigationPropertyEntity) etMultiDatastream.getNavigationProperty("Thing");
            npSensorMds = (NavigationPropertyEntity) etMultiDatastream.getNavigationProperty("Sensor");
            npObservedPropertiesMds = (NavigationPropertyEntitySet) etMultiDatastream.getNavigationProperty("ObservedProperties");

            npMultiDatastreamObservation = (NavigationPropertyEntity) pluginCoreModel.etObservation.getNavigationProperty("MultiDatastream");
            npMultiDatastreamsThing = (NavigationPropertyEntitySet) pluginCoreModel.etThing.getNavigationProperty("MultiDatastreams");
            npMultiDatastreamsSensor = (NavigationPropertyEntitySet) pluginCoreModel.etSensor.getNavigationProperty("MultiDatastreams");
            npMultiDatastreamsObsProp = (NavigationPropertyEntitySet) pluginCoreModel.etObservedProperty.getNavigationProperty("MultiDatastreams");
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
        PathElementEntitySet containingSet = new PathElementEntitySet(etMultiDatastream, null);

        Entity entity = new DefaultEntity(etMultiDatastream);
        Assert.assertFalse(isEntityComplete(entity, containingSet));

        entity.setProperty(pluginCoreModel.epName, "Test MultiDatastream");
        Assert.assertFalse(isEntityComplete(entity, containingSet));

        entity.setProperty(pluginCoreModel.epDescription, "Test Description");
        Assert.assertFalse(isEntityComplete(entity, containingSet));

        List<UnitOfMeasurement> unitOfMeasurements = new ArrayList<>();
        unitOfMeasurements.add(new UnitOfMeasurement().setName("temperature").setDefinition("SomeUrl").setSymbol("degC"));
        entity.setProperty(epUnitOfMeasurements, unitOfMeasurements);
        Assert.assertFalse(isEntityComplete(entity, containingSet));

        entity.setProperty(pluginCoreModel.epObservationType, "http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_ComplexObservation");
        Assert.assertFalse(isEntityComplete(entity, containingSet));

        List<String> multiObservationDataTypes = new ArrayList<>();
        multiObservationDataTypes.add("http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement");
        entity.setProperty(epMultiObservationDataTypes, multiObservationDataTypes);
        Assert.assertFalse(isEntityComplete(entity, containingSet));

        entity.setProperty(npThingMds, new DefaultEntity(pluginCoreModel.etThing).setId(new IdLong(1)));
        Assert.assertFalse(isEntityComplete(entity, containingSet));

        entity.setProperty(npSensorMds, new DefaultEntity(pluginCoreModel.etSensor).setId(new IdLong(2)));
        Assert.assertFalse(isEntityComplete(entity, containingSet));

        EntitySet observedProperties = new EntitySetImpl(pluginCoreModel.etObservedProperty);
        observedProperties.add(new DefaultEntity(pluginCoreModel.etObservedProperty).setId(new IdLong(3)));
        entity.setProperty(npObservedPropertiesMds, observedProperties);
        Assert.assertTrue(isEntityComplete(entity, containingSet));

        entity.setProperty(npThingMds, null);
        Assert.assertFalse(isEntityComplete(entity, containingSet));
        Assert.assertTrue(isEntityComplete(entity, new PathElementEntitySet(etMultiDatastream, new PathElementEntity(new IdLong(2), pluginCoreModel.etThing, null))));

        Assert.assertFalse(isEntityComplete(entity, new PathElementEntitySet(pluginCoreModel.etDatastream, null)));

        unitOfMeasurements.add(new UnitOfMeasurement().setName("temperature").setDefinition("SomeUrl").setSymbol("degC"));
        entity.setProperty(epUnitOfMeasurements, unitOfMeasurements);
        Assert.assertFalse(isEntityComplete(entity, containingSet));

        multiObservationDataTypes.add("http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement");
        entity.setProperty(epMultiObservationDataTypes, multiObservationDataTypes);
        Assert.assertFalse(isEntityComplete(entity, containingSet));

        observedProperties.add(new DefaultEntity(pluginCoreModel.etObservedProperty).setId(new IdLong(3)));
        entity.setProperty(npObservedPropertiesMds, observedProperties);
        Assert.assertTrue(isEntityComplete(entity, containingSet));
    }

    @Test
    public void testObservationComplete() {
        PathElementEntitySet containingSet = new PathElementEntitySet(pluginCoreModel.etObservation, null);
        Entity entity = new DefaultEntity(pluginCoreModel.etObservation);
        Assert.assertFalse(isEntityComplete(entity, containingSet));

        entity.setProperty(pluginCoreModel.epResult, "result");
        Assert.assertFalse(isEntityComplete(entity, containingSet));

        entity.setProperty(pluginCoreModel.npDatastreamObservation, new DefaultEntity(pluginCoreModel.etDatastream).setId(new IdLong(2)));
        Assert.assertTrue(isEntityComplete(entity, containingSet));

        entity.setProperty(npMultiDatastreamObservation, new DefaultEntity(etMultiDatastream).setId(new IdLong(2)));
        Assert.assertFalse(isEntityComplete(entity, containingSet));

        entity.setProperty(pluginCoreModel.npDatastreamObservation, null);
        Assert.assertFalse(isEntityComplete(entity, containingSet));

        entity.setProperty(pluginCoreModel.epResult, Arrays.asList("result"));
        Assert.assertTrue(isEntityComplete(entity, containingSet));

        Assert.assertFalse(isEntityComplete(entity, new PathElementEntitySet(pluginCoreModel.etDatastream, null)));

        entity.setProperty(pluginCoreModel.npDatastreamObservation, new DefaultEntity(pluginCoreModel.etDatastream).setId(new IdLong(2)));
        entity.setProperty(npMultiDatastreamObservation, null);

        containingSet = new PathElementEntitySet(pluginCoreModel.etObservation, new PathElementEntity(new IdLong(1), pluginCoreModel.etDatastream, null));
        entity = new DefaultEntity(pluginCoreModel.etObservation);
        entity.setProperty(pluginCoreModel.epResult, "result");
        Assert.assertTrue(isEntityComplete(entity, containingSet));

        containingSet = new PathElementEntitySet(pluginCoreModel.etObservation, new PathElementEntity(new IdLong(1), etMultiDatastream, null));
        entity = new DefaultEntity(pluginCoreModel.etObservation);
        entity.setProperty(pluginCoreModel.epResult, Arrays.asList("result"));
        Assert.assertTrue(isEntityComplete(entity, containingSet));

    }
}
