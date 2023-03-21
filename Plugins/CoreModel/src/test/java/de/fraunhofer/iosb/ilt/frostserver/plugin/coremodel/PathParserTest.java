/*
 * Copyright (C) 2023 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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
package de.fraunhofer.iosb.ilt.frostserver.plugin.coremodel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.ModelRegistry;
import de.fraunhofer.iosb.ilt.frostserver.model.core.IdLong;
import de.fraunhofer.iosb.ilt.frostserver.model.core.IdString;
import de.fraunhofer.iosb.ilt.frostserver.parser.path.PathParser;
import de.fraunhofer.iosb.ilt.frostserver.path.PathElementArrayIndex;
import de.fraunhofer.iosb.ilt.frostserver.path.PathElementCustomProperty;
import de.fraunhofer.iosb.ilt.frostserver.path.PathElementEntity;
import de.fraunhofer.iosb.ilt.frostserver.path.PathElementEntitySet;
import de.fraunhofer.iosb.ilt.frostserver.path.PathElementProperty;
import de.fraunhofer.iosb.ilt.frostserver.path.ResourcePath;
import de.fraunhofer.iosb.ilt.frostserver.path.Version;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.property.Property;
import de.fraunhofer.iosb.ilt.frostserver.query.QueryDefaults;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import de.fraunhofer.iosb.ilt.frostserver.util.Constants;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 *
 * @author jab
 */
class PathParserTest {

    private static CoreSettings coreSettings;
    private static QueryDefaults queryDefaults;
    private static ModelRegistry modelRegistry;
    private static PluginCoreModel pluginCoreModel;

    private static CoreSettings coreSettingsString;
    private static QueryDefaults queryDefaultsString;
    private static ModelRegistry modelRegistryString;
    private static PluginCoreModel pluginCoreModelString;

    @BeforeAll
    public static void beforeClass() {
        coreSettings = new CoreSettings();
        modelRegistry = coreSettings.getModelRegistry();
        queryDefaults = coreSettings.getQueryDefaults();
        queryDefaults.setUseAbsoluteNavigationLinks(false);
        pluginCoreModel = new PluginCoreModel();
        pluginCoreModel.init(coreSettings);
        coreSettings.getPluginManager().initPlugins(null);

        coreSettingsString = new CoreSettings();
        coreSettingsString.getPluginSettings().set("coreModel.idType", Constants.VALUE_ID_TYPE_STRING);
        modelRegistryString = coreSettingsString.getModelRegistry();
        queryDefaultsString = coreSettingsString.getQueryDefaults();
        queryDefaultsString.setUseAbsoluteNavigationLinks(false);
        pluginCoreModelString = new PluginCoreModel();
        pluginCoreModelString.init(coreSettingsString);
        coreSettingsString.getPluginManager().initPlugins(null);
    }

    @Test
    void testPathsetThings() {
        String path = "/Things";
        ResourcePath result = PathParser.parsePath(modelRegistry, "", Version.V_1_1, path);

        ResourcePath expResult = new ResourcePath("", Version.V_1_1, path);
        PathElementEntitySet espe = new PathElementEntitySet(pluginCoreModel.etThing);
        expResult.addPathElement(espe, true, false);
        expResult.setMainElement(espe);

        assertEquals(expResult, result);
    }

    @Test
    void testPathThing() {
        assertThrows(IllegalArgumentException.class, () -> {
            String path = "/Thing";
            PathParser.parsePath(modelRegistry, "", Version.V_1_1, path);
        });
    }

    @Test
    void testPathsetThingsRef() {
        String path = "/Things/$ref";
        ResourcePath result = PathParser.parsePath(modelRegistry, "", Version.V_1_1, path);

        ResourcePath expResult = new ResourcePath("", Version.V_1_1, path);
        PathElementEntitySet espe = new PathElementEntitySet(pluginCoreModel.etThing);
        expResult.addPathElement(espe, true, false);
        expResult.setMainElement(espe);
        expResult.setRef(true);

        assertEquals(expResult, result);
    }

    @Test
    void testIdentifiers() {
        testThing(0);
        testThing(1);
        testThing(-1);
        testThing(Long.MAX_VALUE);
        testThing(Long.MIN_VALUE);
        testThing("a String Id");
    }

    @Test
    void testPathEntityProperty() {
        for (EntityType entityType : modelRegistry.getEntityTypes()) {
            for (Property property : entityType.getPropertySet()) {
                if (property instanceof EntityPropertyMain) {
                    EntityPropertyMain entityProperty = (EntityPropertyMain) property;

                    String path = "/" + entityType.plural + "(1)/" + property.getName();
                    ResourcePath result = PathParser.parsePath(modelRegistry, "", Version.V_1_1, path);
                    ResourcePath expResult = new ResourcePath("", Version.V_1_1, path);
                    PathElementEntitySet espe = new PathElementEntitySet(entityType);
                    expResult.addPathElement(espe, false, false);
                    PathElementEntity epe = new PathElementEntity(new IdLong(1), entityType, espe);
                    expResult.addPathElement(epe, true, true);
                    PathElementProperty ppe = new PathElementProperty(entityProperty, epe);
                    expResult.addPathElement(ppe, false, false);

                    assertEquals(expResult, result, "Failed on " + entityType + " - " + property);
                }
            }
        }
    }

    @Test
    void testPathEntityThingPropertyValue() {
        String path = "/Things(1)/properties/$value";
        ResourcePath result = PathParser.parsePath(modelRegistry, "", Version.V_1_1, path);

        ResourcePath expResult = new ResourcePath("", Version.V_1_1, path);
        PathElementEntitySet espe = new PathElementEntitySet(pluginCoreModel.etThing);
        expResult.addPathElement(espe, false, false);
        PathElementEntity epe = new PathElementEntity(new IdLong(1), pluginCoreModel.etThing, espe);
        expResult.addPathElement(epe, true, true);
        PathElementProperty ppe = new PathElementProperty(ModelRegistry.EP_PROPERTIES, epe);
        expResult.addPathElement(ppe, false, false);
        expResult.setValue(true);

        assertEquals(expResult, result);
    }

    @Test
    void testPathEntityThingSubProperty() {
        {
            String path = "/Things(1)/properties/property1";
            ResourcePath result = PathParser.parsePath(modelRegistry, "", Version.V_1_1, path);
            ResourcePath expResult = new ResourcePath("", Version.V_1_1, path);
            PathElementEntitySet espe = new PathElementEntitySet(pluginCoreModel.etThing);
            expResult.addPathElement(espe, false, false);
            PathElementEntity epe = new PathElementEntity(new IdLong(1), pluginCoreModel.etThing, espe);
            expResult.addPathElement(epe, true, true);
            PathElementProperty ppe = new PathElementProperty(ModelRegistry.EP_PROPERTIES, epe);
            expResult.addPathElement(ppe, false, false);
            PathElementCustomProperty cppe = new PathElementCustomProperty("property1", ppe);
            expResult.addPathElement(cppe, false, false);
            assertEquals(expResult, result);
        }
        {
            String path = "/Things(1)/properties/name_two";
            ResourcePath result = PathParser.parsePath(modelRegistry, "", Version.V_1_1, path);
            ResourcePath expResult = new ResourcePath("", Version.V_1_1, path);
            PathElementEntitySet espe = new PathElementEntitySet(pluginCoreModel.etThing);
            expResult.addPathElement(espe, false, false);
            PathElementEntity epe = new PathElementEntity(new IdLong(1), pluginCoreModel.etThing, espe);
            expResult.addPathElement(epe, true, true);
            PathElementProperty ppe = new PathElementProperty(ModelRegistry.EP_PROPERTIES, epe);
            expResult.addPathElement(ppe, false, false);
            PathElementCustomProperty cppe = new PathElementCustomProperty("name_two", ppe);
            expResult.addPathElement(cppe, false, false);
            assertEquals(expResult, result);
        }
        {
            String path = "/Things(1)/properties/property1[2]";
            ResourcePath result = PathParser.parsePath(modelRegistry, "", Version.V_1_1, path);
            ResourcePath expResult = new ResourcePath("", Version.V_1_1, path);
            PathElementEntitySet espe = new PathElementEntitySet(pluginCoreModel.etThing);
            expResult.addPathElement(espe, false, false);
            PathElementEntity epe = new PathElementEntity(new IdLong(1), pluginCoreModel.etThing, espe);
            expResult.addPathElement(epe, true, true);
            PathElementProperty ppe = new PathElementProperty(ModelRegistry.EP_PROPERTIES, epe);
            expResult.addPathElement(ppe, false, false);
            PathElementCustomProperty cppe = new PathElementCustomProperty("property1", ppe);
            expResult.addPathElement(cppe, false, false);
            PathElementArrayIndex cpai = new PathElementArrayIndex(2, cppe);
            expResult.addPathElement(cpai, false, false);
            assertEquals(expResult, result);
        }
        {
            String path = "/Things(1)/properties/property1[2][3]";
            ResourcePath result = PathParser.parsePath(modelRegistry, "", Version.V_1_1, path);
            ResourcePath expResult = new ResourcePath("", Version.V_1_1, path);
            PathElementEntitySet espe = new PathElementEntitySet(pluginCoreModel.etThing);
            expResult.addPathElement(espe, false, false);
            PathElementEntity epe = new PathElementEntity(new IdLong(1), pluginCoreModel.etThing, espe);
            expResult.addPathElement(epe, true, true);
            PathElementProperty ppe = new PathElementProperty(ModelRegistry.EP_PROPERTIES, epe);
            expResult.addPathElement(ppe, false, false);
            PathElementCustomProperty cppe = new PathElementCustomProperty("property1", ppe);
            expResult.addPathElement(cppe, false, false);
            PathElementArrayIndex cpai = new PathElementArrayIndex(2, cppe);
            expResult.addPathElement(cpai, false, false);
            cpai = new PathElementArrayIndex(3, cpai);
            expResult.addPathElement(cpai, false, false);
            assertEquals(expResult, result);
        }
        {
            String path = "/Things(1)/properties/property1[2]/deep[3]";
            ResourcePath result = PathParser.parsePath(modelRegistry, "", Version.V_1_1, path);
            ResourcePath expResult = new ResourcePath("", Version.V_1_1, path);
            PathElementEntitySet espe = new PathElementEntitySet(pluginCoreModel.etThing);
            expResult.addPathElement(espe, false, false);
            PathElementEntity epe = new PathElementEntity(new IdLong(1), pluginCoreModel.etThing, espe);
            expResult.addPathElement(epe, true, true);
            PathElementProperty ppe = new PathElementProperty(ModelRegistry.EP_PROPERTIES, epe);
            expResult.addPathElement(ppe, false, false);
            PathElementCustomProperty cppe = new PathElementCustomProperty("property1", ppe);
            expResult.addPathElement(cppe, false, false);
            PathElementArrayIndex cpai = new PathElementArrayIndex(2, cppe);
            expResult.addPathElement(cpai, false, false);
            cppe = new PathElementCustomProperty("deep", cpai);
            expResult.addPathElement(cppe, false, false);
            cpai = new PathElementArrayIndex(3, cppe);
            expResult.addPathElement(cpai, false, false);
            assertEquals(expResult, result);
        }
    }

    @Test
    void testPathEntityObservation() {
        String path = "/Observations(1)/parameters/property1";
        ResourcePath result = PathParser.parsePath(modelRegistry, "", Version.V_1_1, path);

        ResourcePath expResult = new ResourcePath("", Version.V_1_1, path);
        PathElementEntitySet espe = new PathElementEntitySet(pluginCoreModel.etObservation);
        expResult.addPathElement(espe, false, false);
        PathElementEntity epe = new PathElementEntity(new IdLong(1), pluginCoreModel.etObservation, espe);
        expResult.addPathElement(epe, true, true);
        PathElementProperty ppe = new PathElementProperty(pluginCoreModel.epParameters, epe);
        expResult.addPathElement(ppe, false, false);
        PathElementCustomProperty cppe = new PathElementCustomProperty("property1", ppe);
        expResult.addPathElement(cppe, false, false);
        assertEquals(expResult, result);

        path = "/Observations(1)/parameters/property1[2]";
        result = PathParser.parsePath(modelRegistry, "", Version.V_1_1, path);
        expResult = new ResourcePath("", Version.V_1_1, path);
        espe = new PathElementEntitySet(pluginCoreModel.etObservation);
        expResult.addPathElement(espe, false, false);
        epe = new PathElementEntity(new IdLong(1), pluginCoreModel.etObservation, espe);
        expResult.addPathElement(epe, true, true);
        ppe = new PathElementProperty(pluginCoreModel.epParameters, epe);
        expResult.addPathElement(ppe, false, false);
        cppe = new PathElementCustomProperty("property1", ppe);
        expResult.addPathElement(cppe, false, false);
        PathElementArrayIndex cpai = new PathElementArrayIndex(2, cppe);
        expResult.addPathElement(cpai, false, false);
        assertEquals(expResult, result);

        path = "/Observations(1)/parameters/property1[2][3]";
        result = PathParser.parsePath(modelRegistry, "", Version.V_1_1, path);
        expResult = new ResourcePath("", Version.V_1_1, path);
        espe = new PathElementEntitySet(pluginCoreModel.etObservation);
        expResult.addPathElement(espe, false, false);
        epe = new PathElementEntity(new IdLong(1), pluginCoreModel.etObservation, espe);
        expResult.addPathElement(epe, true, true);
        ppe = new PathElementProperty(pluginCoreModel.epParameters, epe);
        expResult.addPathElement(ppe, false, false);
        cppe = new PathElementCustomProperty("property1", ppe);
        expResult.addPathElement(cppe, false, false);
        cpai = new PathElementArrayIndex(2, cppe);
        expResult.addPathElement(cpai, false, false);
        cpai = new PathElementArrayIndex(3, cpai);
        expResult.addPathElement(cpai, false, false);
        assertEquals(expResult, result);

        path = "/Observations(1)/parameters/property1[2]/deep[3]";
        result = PathParser.parsePath(modelRegistry, "", Version.V_1_1, path);
        expResult = new ResourcePath("", Version.V_1_1, path);
        espe = new PathElementEntitySet(pluginCoreModel.etObservation);
        expResult.addPathElement(espe, false, false);
        epe = new PathElementEntity(new IdLong(1), pluginCoreModel.etObservation, espe);
        expResult.addPathElement(epe, true, true);
        ppe = new PathElementProperty(pluginCoreModel.epParameters, epe);
        expResult.addPathElement(ppe, false, false);
        cppe = new PathElementCustomProperty("property1", ppe);
        expResult.addPathElement(cppe, false, false);
        cpai = new PathElementArrayIndex(2, cppe);
        expResult.addPathElement(cpai, false, false);
        cppe = new PathElementCustomProperty("deep", cpai);
        expResult.addPathElement(cppe, false, false);
        cpai = new PathElementArrayIndex(3, cppe);
        expResult.addPathElement(cpai, false, false);
        assertEquals(expResult, result);

        path = "/Observations(1)/result/property1";
        result = PathParser.parsePath(modelRegistry, "", Version.V_1_1, path);

        expResult = new ResourcePath("", Version.V_1_1, path);
        espe = new PathElementEntitySet(pluginCoreModel.etObservation);
        expResult.addPathElement(espe, false, false);
        epe = new PathElementEntity(new IdLong(1), pluginCoreModel.etObservation, espe);
        expResult.addPathElement(epe, true, true);
        ppe = new PathElementProperty(pluginCoreModel.epResult, epe);
        expResult.addPathElement(ppe, false, false);
        cppe = new PathElementCustomProperty("property1", ppe);
        expResult.addPathElement(cppe, false, false);
        assertEquals(expResult, result);

        path = "/Observations(1)/result[2]";
        result = PathParser.parsePath(modelRegistry, "", Version.V_1_1, path);
        expResult = new ResourcePath("", Version.V_1_1, path);
        espe = new PathElementEntitySet(pluginCoreModel.etObservation);
        expResult.addPathElement(espe, false, false);
        epe = new PathElementEntity(new IdLong(1), pluginCoreModel.etObservation, espe);
        expResult.addPathElement(epe, true, true);
        ppe = new PathElementProperty(pluginCoreModel.epResult, epe);
        expResult.addPathElement(ppe, false, false);
        cpai = new PathElementArrayIndex(2, ppe);
        expResult.addPathElement(cpai, false, false);
        assertEquals(expResult, result);

    }

    @Test
    void testPathdeep0() {
        String path = "/ObservedProperties(1)/Datastreams(2)/Observations";
        ResourcePath result = PathParser.parsePath(modelRegistry, "", Version.V_1_1, path);

        ResourcePath expResult = new ResourcePath("", Version.V_1_1, path);
        PathElementEntitySet espe = new PathElementEntitySet(pluginCoreModel.etObservedProperty);
        expResult.addPathElement(espe, false, false);
        PathElementEntity epe = new PathElementEntity(new IdLong(1), pluginCoreModel.etObservedProperty, espe);
        expResult.addPathElement(epe, false, true);
        espe = new PathElementEntitySet(pluginCoreModel.npDatastreamsObsProp, epe);
        expResult.addPathElement(espe, false, false);
        epe = new PathElementEntity(new IdLong(2), pluginCoreModel.etDatastream, espe);
        expResult.addPathElement(epe, false, true);
        espe = new PathElementEntitySet(pluginCoreModel.npObservationsDatastream, epe);
        expResult.addPathElement(espe, false, false);

        assertEquals(expResult, result);
    }

    @Test
    void testPathdeep1() {
        String path = "/Things(1)/Locations(2)/HistoricalLocations(3)/Thing/Datastreams(5)/Sensor/Datastreams(6)/ObservedProperty/Datastreams(7)/Observations(8)/FeatureOfInterest";
        ResourcePath result = PathParser.parsePath(modelRegistry, "", Version.V_1_1, path);

        ResourcePath expResult = new ResourcePath("", Version.V_1_1, path);

        PathElementEntitySet espe = new PathElementEntitySet(pluginCoreModel.etThing);
        expResult.addPathElement(espe, false, false);
        PathElementEntity epe = new PathElementEntity(new IdLong(1), pluginCoreModel.etThing, espe);
        expResult.addPathElement(epe, false, false);

        espe = new PathElementEntitySet(pluginCoreModel.npLocationsThing, epe);
        expResult.addPathElement(espe, false, false);
        epe = new PathElementEntity(new IdLong(2), pluginCoreModel.etLocation, espe);
        expResult.addPathElement(epe, false, false);

        espe = new PathElementEntitySet(pluginCoreModel.npHistoricalLocationsLocation, epe);
        expResult.addPathElement(espe, false, false);
        epe = new PathElementEntity(new IdLong(3), pluginCoreModel.etHistoricalLocation, espe);
        expResult.addPathElement(epe, false, false);

        epe = new PathElementEntity(pluginCoreModel.npThingHistLoc, epe);
        expResult.addPathElement(epe, false, false);

        espe = new PathElementEntitySet(pluginCoreModel.npDatastreamsThing, epe);
        expResult.addPathElement(espe, false, false);
        epe = new PathElementEntity(new IdLong(5), pluginCoreModel.etDatastream, espe);
        expResult.addPathElement(epe, false, false);

        epe = new PathElementEntity(pluginCoreModel.npSensorDatastream, epe);
        expResult.addPathElement(epe, false, false);

        espe = new PathElementEntitySet(pluginCoreModel.npDatastreamsSensor, epe);
        expResult.addPathElement(espe, false, false);
        epe = new PathElementEntity(new IdLong(6), pluginCoreModel.etDatastream, espe);
        expResult.addPathElement(epe, false, false);

        epe = new PathElementEntity(pluginCoreModel.npObservedPropertyDatastream, epe);
        expResult.addPathElement(epe, false, false);

        espe = new PathElementEntitySet(pluginCoreModel.npDatastreamsObsProp, epe);
        expResult.addPathElement(espe, false, false);
        epe = new PathElementEntity(new IdLong(7), pluginCoreModel.etDatastream, espe);
        expResult.addPathElement(epe, false, false);

        espe = new PathElementEntitySet(pluginCoreModel.npObservationsDatastream, epe);
        expResult.addPathElement(espe, false, false);
        epe = new PathElementEntity(new IdLong(8), pluginCoreModel.etObservation, espe);
        expResult.addPathElement(epe, false, true);

        epe = new PathElementEntity(pluginCoreModel.npFeatureOfInterestObservation, epe);
        expResult.addPathElement(epe, true, false);

        assertEquals(expResult, result);
    }

    @Test
    void testPathdeep2() {
        String path = "/FeaturesOfInterest(1)/Observations(2)/Datastream/Thing/HistoricalLocations(3)/Locations(4)/Things(1)/properties/property1";
        ResourcePath result = PathParser.parsePath(modelRegistry, "", Version.V_1_1, path);

        ResourcePath expResult = new ResourcePath("", Version.V_1_1, path);

        PathElementEntitySet espe = new PathElementEntitySet(pluginCoreModel.etFeatureOfInterest);
        expResult.addPathElement(espe, false, false);
        PathElementEntity epe = new PathElementEntity(new IdLong(1), pluginCoreModel.etFeatureOfInterest, espe);
        expResult.addPathElement(epe, false, false);

        espe = new PathElementEntitySet(pluginCoreModel.npObservationsFeature, epe);
        expResult.addPathElement(espe, false, false);
        epe = new PathElementEntity(new IdLong(2), pluginCoreModel.etObservation, espe);
        expResult.addPathElement(epe, false, false);

        epe = new PathElementEntity(pluginCoreModel.npDatastreamObservation, epe);
        expResult.addPathElement(epe, false, false);

        epe = new PathElementEntity(pluginCoreModel.npThingDatasteam, epe);
        expResult.addPathElement(epe, false, false);

        espe = new PathElementEntitySet(pluginCoreModel.npHistoricalLocationsThing, epe);
        expResult.addPathElement(espe, false, false);
        epe = new PathElementEntity(new IdLong(3), pluginCoreModel.etHistoricalLocation, espe);
        expResult.addPathElement(epe, false, false);

        espe = new PathElementEntitySet(pluginCoreModel.npLocationsHistLoc, epe);
        expResult.addPathElement(espe, false, false);
        epe = new PathElementEntity(new IdLong(4), pluginCoreModel.etLocation, espe);
        expResult.addPathElement(epe, false, false);

        espe = new PathElementEntitySet(pluginCoreModel.npThingsLocation, epe);
        expResult.addPathElement(espe, false, false);
        epe = new PathElementEntity(new IdLong(1), pluginCoreModel.etThing, espe);
        expResult.addPathElement(epe, true, true);
        PathElementProperty ppe = new PathElementProperty(ModelRegistry.EP_PROPERTIES, epe);
        expResult.addPathElement(ppe, false, false);
        PathElementCustomProperty cppe = new PathElementCustomProperty("property1", ppe);
        expResult.addPathElement(cppe, false, false);

        assertEquals(expResult, result);
    }

    @Test
    void testPathdeep3() {
        String path = "/Things(1)/Locations(2)/HistoricalLocations(3)/Thing/Datastreams(5)/Sensor/Datastreams(6)/ObservedProperty/Datastreams(8)/Observations(9)/FeatureOfInterest";
        ResourcePath result = PathParser.parsePath(modelRegistry, "", Version.V_1_1, path);

        ResourcePath expResult = new ResourcePath("", Version.V_1_1, path);

        PathElementEntitySet espe = new PathElementEntitySet(pluginCoreModel.etThing);
        expResult.addPathElement(espe, false, false);
        PathElementEntity epe = new PathElementEntity(new IdLong(1), pluginCoreModel.etThing, espe);
        expResult.addPathElement(epe, false, false);

        espe = new PathElementEntitySet(pluginCoreModel.npLocationsThing, epe);
        expResult.addPathElement(espe, false, false);
        epe = new PathElementEntity(new IdLong(2), pluginCoreModel.etLocation, espe);
        expResult.addPathElement(epe, false, false);

        espe = new PathElementEntitySet(pluginCoreModel.npHistoricalLocationsLocation, epe);
        expResult.addPathElement(espe, false, false);
        epe = new PathElementEntity(new IdLong(3), pluginCoreModel.etHistoricalLocation, espe);
        expResult.addPathElement(epe, false, false);

        epe = new PathElementEntity(pluginCoreModel.npThingHistLoc, epe);
        expResult.addPathElement(epe, false, false);

        espe = new PathElementEntitySet(pluginCoreModel.npDatastreamsThing, epe);
        expResult.addPathElement(espe, false, false);
        epe = new PathElementEntity(new IdLong(5), pluginCoreModel.etDatastream, espe);
        expResult.addPathElement(epe, false, false);

        epe = new PathElementEntity(pluginCoreModel.npSensorDatastream, epe);
        expResult.addPathElement(epe, false, false);

        espe = new PathElementEntitySet(pluginCoreModel.npDatastreamsSensor, epe);
        expResult.addPathElement(espe, false, false);
        epe = new PathElementEntity(new IdLong(6), pluginCoreModel.etDatastream, espe);
        expResult.addPathElement(epe, false, false);

        epe = new PathElementEntity(pluginCoreModel.npObservedPropertyDatastream, epe);
        expResult.addPathElement(epe, false, false);

        espe = new PathElementEntitySet(pluginCoreModel.npDatastreamsObsProp, epe);
        expResult.addPathElement(espe, false, false);
        epe = new PathElementEntity(new IdLong(8), pluginCoreModel.etDatastream, espe);
        expResult.addPathElement(epe, false, false);

        espe = new PathElementEntitySet(pluginCoreModel.npObservationsDatastream, epe);
        expResult.addPathElement(espe, false, false);
        epe = new PathElementEntity(new IdLong(9), pluginCoreModel.etObservation, espe);
        expResult.addPathElement(epe, false, true);

        epe = new PathElementEntity(pluginCoreModel.npFeatureOfInterestObservation, epe);
        expResult.addPathElement(epe, true, false);

        assertEquals(expResult, result);
    }

    @Test
    void testPathdeep4() {
        String path = "/FeaturesOfInterest(1)/Observations(2)/Datastream/Thing/HistoricalLocations(3)/Locations(4)/Things(1)/properties/property1/subproperty2";
        ResourcePath result = PathParser.parsePath(modelRegistry, "", Version.V_1_1, path);

        ResourcePath expResult = new ResourcePath("", Version.V_1_1, path);

        PathElementEntitySet espe = new PathElementEntitySet(pluginCoreModel.etFeatureOfInterest);
        expResult.addPathElement(espe, false, false);
        PathElementEntity epe = new PathElementEntity(new IdLong(1), pluginCoreModel.etFeatureOfInterest, espe);
        expResult.addPathElement(epe, false, false);

        espe = new PathElementEntitySet(pluginCoreModel.npObservationsFeature, epe);
        expResult.addPathElement(espe, false, false);
        epe = new PathElementEntity(new IdLong(2), pluginCoreModel.etObservation, espe);
        expResult.addPathElement(epe, false, false);

        epe = new PathElementEntity(pluginCoreModel.npDatastreamObservation, epe);
        expResult.addPathElement(epe, false, false);

        epe = new PathElementEntity(pluginCoreModel.npThingDatasteam, epe);
        expResult.addPathElement(epe, false, false);

        espe = new PathElementEntitySet(pluginCoreModel.npHistoricalLocationsThing, epe);
        expResult.addPathElement(espe, false, false);
        epe = new PathElementEntity(new IdLong(3), pluginCoreModel.etHistoricalLocation, espe);
        expResult.addPathElement(epe, false, false);

        espe = new PathElementEntitySet(pluginCoreModel.npLocationsHistLoc, epe);
        expResult.addPathElement(espe, false, false);
        epe = new PathElementEntity(new IdLong(4), pluginCoreModel.etLocation, espe);
        expResult.addPathElement(epe, false, false);

        espe = new PathElementEntitySet(pluginCoreModel.npThingsLocation, epe);
        expResult.addPathElement(espe, false, false);
        epe = new PathElementEntity(new IdLong(1), pluginCoreModel.etThing, espe);
        expResult.addPathElement(epe, true, true);
        PathElementProperty ppe = new PathElementProperty(ModelRegistry.EP_PROPERTIES, epe);
        expResult.addPathElement(ppe, false, false);
        PathElementCustomProperty cppe = new PathElementCustomProperty("property1", ppe);
        expResult.addPathElement(cppe, false, false);
        cppe = new PathElementCustomProperty("subproperty2", cppe);
        expResult.addPathElement(cppe, false, false);

        assertEquals(expResult, result);
    }

    @Test
    void testPathdeep5() {
        assertThrows(IllegalArgumentException.class, () -> {
            String path = "/Things(1)/Locations(2)/HistoricalLocations(3)/Thing/Datastreams(5)/Sensor/Datastreams(6)/ObservedProperties/Datastreams(8)/Observations(9)/FeatureOfInterest";
            PathParser.parsePath(modelRegistry, "", Version.V_1_1, path);
        });
    }

    @Test
    void testPathdeepCompressed1() {
        String path = "/Observations(11)/Datastream/Thing";
        ResourcePath result = PathParser.parsePath(modelRegistry, "", Version.V_1_1, path);
        result.compress();

        ResourcePath expResult = new ResourcePath("", Version.V_1_1, path);
        PathElementEntitySet espe = new PathElementEntitySet(pluginCoreModel.etObservation);
        expResult.addPathElement(espe, false, false);
        PathElementEntity epe = new PathElementEntity(new IdLong(11), pluginCoreModel.etObservation, espe);
        expResult.addPathElement(epe, false, true);

        epe = new PathElementEntity(pluginCoreModel.npDatastreamObservation, epe);
        expResult.addPathElement(epe, false, false);

        epe = new PathElementEntity(pluginCoreModel.npThingDatasteam, epe);
        expResult.addPathElement(epe, true, false);

        assertEquals(expResult, result);
    }

    @Test
    void testPathdeepCompressed2() {
        String path = "/Datastreams(5)/Observations(11)/Datastream/Thing";
        ResourcePath result = PathParser.parsePath(modelRegistry, "", Version.V_1_1, path);
        result.compress();

        ResourcePath expResult = new ResourcePath("", Version.V_1_1, path);
        PathElementEntitySet espe = new PathElementEntitySet(pluginCoreModel.npObservationsDatastream, null);
        expResult.addPathElement(espe, false, false);
        PathElementEntity epe = new PathElementEntity(new IdLong(11), pluginCoreModel.etObservation, espe);
        expResult.addPathElement(epe, false, true);

        epe = new PathElementEntity(pluginCoreModel.npDatastreamObservation, epe);
        expResult.addPathElement(epe, false, false);

        epe = new PathElementEntity(pluginCoreModel.npThingDatasteam, epe);
        expResult.addPathElement(epe, true, false);

        assertEquals(expResult, result);
    }

    private void testThing(long id) {
        String path = "/Things(" + id + ")";
        ResourcePath result = PathParser.parsePath(modelRegistry, "", Version.V_1_1, path);

        ResourcePath expResult = new ResourcePath("", Version.V_1_1, path);
        PathElementEntitySet espe = new PathElementEntitySet(pluginCoreModel.etThing);
        expResult.addPathElement(espe, false, false);
        PathElementEntity epe = new PathElementEntity(new IdLong(id), pluginCoreModel.etThing, espe);
        expResult.addPathElement(epe, true, true);

        assertEquals(expResult, result);
    }

    private void testThing(String id) {
        String path = "/Things('" + id + "')";
        ResourcePath result = PathParser.parsePath(modelRegistryString, "", Version.V_1_1, path);

        ResourcePath expResult = new ResourcePath("", Version.V_1_1, path);
        PathElementEntitySet espe = new PathElementEntitySet(pluginCoreModelString.etThing);
        expResult.addPathElement(espe, false, false);
        PathElementEntity epe = new PathElementEntity(new IdString(id), pluginCoreModelString.etThing, espe);
        expResult.addPathElement(epe, true, true);

        assertEquals(expResult, result);
    }

}
