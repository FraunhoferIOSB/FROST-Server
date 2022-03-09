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
package de.fraunhofer.iosb.ilt.frostserver.plugin.multidatastream;

import de.fraunhofer.iosb.ilt.frostserver.model.DefaultEntity;
import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.ModelRegistry;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.model.core.EntitySet;
import de.fraunhofer.iosb.ilt.frostserver.model.core.EntitySetImpl;
import de.fraunhofer.iosb.ilt.frostserver.model.core.IdLong;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.UnitOfMeasurement;
import de.fraunhofer.iosb.ilt.frostserver.path.PathElementEntity;
import de.fraunhofer.iosb.ilt.frostserver.path.PathElementEntitySet;
import de.fraunhofer.iosb.ilt.frostserver.plugin.coremodel.PluginCoreModel;
import static de.fraunhofer.iosb.ilt.frostserver.plugin.multidatastream.MdsModelSettings.TAG_ENABLE_MDS_MODEL;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain.NavigationPropertyEntity;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain.NavigationPropertyEntitySet;
import de.fraunhofer.iosb.ilt.frostserver.query.QueryDefaults;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.IncompleteEntityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

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
    private static NavigationPropertyEntitySet npObservationsMds;
    private static NavigationPropertyEntitySet npMultiDatastreamsSensor;
    private static NavigationPropertyEntitySet npMultiDatastreamsObsProp;
    private static NavigationPropertyEntitySet npMultiDatastreamsThing;

    @BeforeAll
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
            npObservationsMds = (NavigationPropertyEntitySet) etMultiDatastream.getNavigationProperty("Observations");

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
    void testMultiDatastreamComplete() {
        PathElementEntitySet containingSet = new PathElementEntitySet(etMultiDatastream);

        Entity entity = new DefaultEntity(etMultiDatastream);
        assertFalse(isEntityComplete(entity, containingSet));

        entity.setProperty(pluginCoreModel.epName, "Test MultiDatastream");
        assertFalse(isEntityComplete(entity, containingSet));

        entity.setProperty(pluginCoreModel.epDescription, "Test Description");
        assertFalse(isEntityComplete(entity, containingSet));

        List<UnitOfMeasurement> unitOfMeasurements = new ArrayList<>();
        unitOfMeasurements.add(new UnitOfMeasurement().setName("temperature").setDefinition("SomeUrl").setSymbol("degC"));
        entity.setProperty(epUnitOfMeasurements, unitOfMeasurements);
        assertFalse(isEntityComplete(entity, containingSet));

        entity.setProperty(pluginCoreModel.epObservationType, "http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_ComplexObservation");
        assertFalse(isEntityComplete(entity, containingSet));

        List<String> multiObservationDataTypes = new ArrayList<>();
        multiObservationDataTypes.add("http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement");
        entity.setProperty(epMultiObservationDataTypes, multiObservationDataTypes);
        assertFalse(isEntityComplete(entity, containingSet));

        entity.setProperty(npThingMds, new DefaultEntity(pluginCoreModel.etThing).setId(new IdLong(1)));
        assertFalse(isEntityComplete(entity, containingSet));

        entity.setProperty(npSensorMds, new DefaultEntity(pluginCoreModel.etSensor).setId(new IdLong(2)));
        assertFalse(isEntityComplete(entity, containingSet));

        EntitySet observedProperties = new EntitySetImpl(pluginCoreModel.etObservedProperty);
        observedProperties.add(new DefaultEntity(pluginCoreModel.etObservedProperty).setId(new IdLong(3)));
        entity.setProperty(npObservedPropertiesMds, observedProperties);
        assertTrue(isEntityComplete(entity, containingSet));

        entity.setProperty(npThingMds, null);
        assertFalse(isEntityComplete(entity, containingSet));
        assertTrue(isEntityComplete(entity, new PathElementEntitySet(npMultiDatastreamsThing, new PathElementEntity(new IdLong(2), pluginCoreModel.etThing, null))));

        assertFalse(isEntityComplete(entity, new PathElementEntitySet(pluginCoreModel.etDatastream)));

        unitOfMeasurements.add(new UnitOfMeasurement().setName("temperature").setDefinition("SomeUrl").setSymbol("degC"));
        entity.setProperty(epUnitOfMeasurements, unitOfMeasurements);
        assertFalse(isEntityComplete(entity, containingSet));

        multiObservationDataTypes.add("http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement");
        entity.setProperty(epMultiObservationDataTypes, multiObservationDataTypes);
        assertFalse(isEntityComplete(entity, containingSet));

        observedProperties.add(new DefaultEntity(pluginCoreModel.etObservedProperty).setId(new IdLong(3)));
        entity.setProperty(npObservedPropertiesMds, observedProperties);
        assertTrue(isEntityComplete(entity, containingSet));
    }

    @Test
    void testObservationComplete() {
        PathElementEntitySet containingSet = new PathElementEntitySet(pluginCoreModel.etObservation);
        Entity entity = new DefaultEntity(pluginCoreModel.etObservation);
        assertFalse(isEntityComplete(entity, containingSet));

        entity.setProperty(pluginCoreModel.epResult, "result");
        assertFalse(isEntityComplete(entity, containingSet));

        entity.setProperty(pluginCoreModel.npDatastreamObservation, new DefaultEntity(pluginCoreModel.etDatastream).setId(new IdLong(2)));
        assertTrue(isEntityComplete(entity, containingSet));

        entity.setProperty(npMultiDatastreamObservation, new DefaultEntity(etMultiDatastream).setId(new IdLong(2)));
        assertFalse(isEntityComplete(entity, containingSet));

        entity.setProperty(pluginCoreModel.npDatastreamObservation, null);
        assertFalse(isEntityComplete(entity, containingSet));

        entity.setProperty(pluginCoreModel.epResult, Arrays.asList("result"));
        assertTrue(isEntityComplete(entity, containingSet));

        assertFalse(isEntityComplete(entity, new PathElementEntitySet(pluginCoreModel.etDatastream)));

        entity.setProperty(pluginCoreModel.npDatastreamObservation, new DefaultEntity(pluginCoreModel.etDatastream).setId(new IdLong(2)));
        entity.setProperty(npMultiDatastreamObservation, null);

        containingSet = new PathElementEntitySet(pluginCoreModel.npObservationsDatastream, new PathElementEntity(new IdLong(1), pluginCoreModel.etDatastream, null));
        entity = new DefaultEntity(pluginCoreModel.etObservation);
        entity.setProperty(pluginCoreModel.epResult, "result");
        assertTrue(isEntityComplete(entity, containingSet));

        containingSet = new PathElementEntitySet(npObservationsMds, new PathElementEntity(new IdLong(1), etMultiDatastream, null));
        entity = new DefaultEntity(pluginCoreModel.etObservation);
        entity.setProperty(pluginCoreModel.epResult, Arrays.asList("result"));
        assertTrue(isEntityComplete(entity, containingSet));

    }
}
