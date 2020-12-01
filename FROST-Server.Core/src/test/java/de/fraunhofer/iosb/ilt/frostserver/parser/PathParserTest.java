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
package de.fraunhofer.iosb.ilt.frostserver.parser;

import de.fraunhofer.iosb.ilt.frostserver.json.deserialize.JsonReader;
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
import de.fraunhofer.iosb.ilt.frostserver.persistence.IdManagerLong;
import de.fraunhofer.iosb.ilt.frostserver.persistence.IdManagerString;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.property.Property;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author jab
 */
public class PathParserTest {

    private static ModelRegistry modelRegistry;
    private static JsonReader entityParser;

    @BeforeClass
    public static void beforeClass() {
        modelRegistry = new ModelRegistry();
        modelRegistry.initDefaultTypes();
        modelRegistry.initFinalise();
        modelRegistry.setIdClass(IdLong.class);
        entityParser = new JsonReader(modelRegistry);
    }

    @Test
    public void testPathsetThings() {
        String path = "/Things";
        ResourcePath result = PathParser.parsePath(modelRegistry, "", Version.V_1_1, path);

        ResourcePath expResult = new ResourcePath("", Version.V_1_1, path);
        PathElementEntitySet espe = new PathElementEntitySet(modelRegistry.THING, null);
        expResult.addPathElement(espe, true, false);
        expResult.setMainElement(espe);

        Assert.assertEquals(expResult, result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPathThing() {
        String path = "/Thing";
        PathParser.parsePath(modelRegistry, "", Version.V_1_1, path);
    }

    @Test
    public void testPathsetThingsRef() {
        String path = "/Things/$ref";
        ResourcePath result = PathParser.parsePath(modelRegistry, "", Version.V_1_1, path);

        ResourcePath expResult = new ResourcePath("", Version.V_1_1, path);
        PathElementEntitySet espe = new PathElementEntitySet(modelRegistry.THING, null);
        expResult.addPathElement(espe, true, false);
        expResult.setMainElement(espe);
        expResult.setRef(true);

        Assert.assertEquals(expResult, result);
    }

    @Test
    public void testIdentifiers() {
        testThing(0);
        testThing(1);
        testThing(-1);
        testThing(Long.MAX_VALUE);
        testThing(Long.MIN_VALUE);
        testThing("a String Id");
    }

    @Test
    public void testPathEntityProperty() {
        for (EntityType entityType : modelRegistry.getEntityTypes()) {
            for (Property property : entityType.getPropertySet()) {
                if (property instanceof EntityPropertyMain) {
                    EntityPropertyMain entityProperty = (EntityPropertyMain) property;

                    String path = "/" + entityType.plural + "(1)/" + property.getName();
                    ResourcePath result = PathParser.parsePath(modelRegistry, "", Version.V_1_1, path);
                    ResourcePath expResult = new ResourcePath("", Version.V_1_1, path);
                    PathElementEntitySet espe = new PathElementEntitySet(entityType, null);
                    expResult.addPathElement(espe, false, false);
                    PathElementEntity epe = new PathElementEntity(new IdLong(1), entityType, espe);
                    expResult.addPathElement(epe, true, true);
                    PathElementProperty ppe = new PathElementProperty(entityProperty, epe);
                    expResult.addPathElement(ppe, false, false);

                    Assert.assertEquals("Failed on " + entityType + " - " + property, expResult, result);
                }
            }
        }
    }

    @Test
    public void testPathEntityThingPropertyValue() {
        String path = "/Things(1)/properties/$value";
        ResourcePath result = PathParser.parsePath(modelRegistry, "", Version.V_1_1, path);

        ResourcePath expResult = new ResourcePath("", Version.V_1_1, path);
        PathElementEntitySet espe = new PathElementEntitySet(modelRegistry.THING, null);
        expResult.addPathElement(espe, false, false);
        PathElementEntity epe = new PathElementEntity(new IdLong(1), modelRegistry.THING, espe);
        expResult.addPathElement(epe, true, true);
        PathElementProperty ppe = new PathElementProperty(modelRegistry.EP_PROPERTIES, epe);
        expResult.addPathElement(ppe, false, false);
        expResult.setValue(true);

        Assert.assertEquals(expResult, result);
    }

    @Test
    public void testPathEntityThingSubProperty() {
        {
            String path = "/Things(1)/properties/property1";
            ResourcePath result = PathParser.parsePath(modelRegistry, "", Version.V_1_1, path);
            ResourcePath expResult = new ResourcePath("", Version.V_1_1, path);
            PathElementEntitySet espe = new PathElementEntitySet(modelRegistry.THING, null);
            expResult.addPathElement(espe, false, false);
            PathElementEntity epe = new PathElementEntity(new IdLong(1), modelRegistry.THING, espe);
            expResult.addPathElement(epe, true, true);
            PathElementProperty ppe = new PathElementProperty(modelRegistry.EP_PROPERTIES, epe);
            expResult.addPathElement(ppe, false, false);
            PathElementCustomProperty cppe = new PathElementCustomProperty("property1", ppe);
            expResult.addPathElement(cppe, false, false);
            Assert.assertEquals(expResult, result);
        }
        {
            String path = "/Things(1)/properties/name_two";
            ResourcePath result = PathParser.parsePath(modelRegistry, "", Version.V_1_1, path);
            ResourcePath expResult = new ResourcePath("", Version.V_1_1, path);
            PathElementEntitySet espe = new PathElementEntitySet(modelRegistry.THING, null);
            expResult.addPathElement(espe, false, false);
            PathElementEntity epe = new PathElementEntity(new IdLong(1), modelRegistry.THING, espe);
            expResult.addPathElement(epe, true, true);
            PathElementProperty ppe = new PathElementProperty(modelRegistry.EP_PROPERTIES, epe);
            expResult.addPathElement(ppe, false, false);
            PathElementCustomProperty cppe = new PathElementCustomProperty("name_two", ppe);
            expResult.addPathElement(cppe, false, false);
            Assert.assertEquals(expResult, result);
        }
        {
            String path = "/Things(1)/properties/property1[2]";
            ResourcePath result = PathParser.parsePath(modelRegistry, "", Version.V_1_1, path);
            ResourcePath expResult = new ResourcePath("", Version.V_1_1, path);
            PathElementEntitySet espe = new PathElementEntitySet(modelRegistry.THING, null);
            expResult.addPathElement(espe, false, false);
            PathElementEntity epe = new PathElementEntity(new IdLong(1), modelRegistry.THING, espe);
            expResult.addPathElement(epe, true, true);
            PathElementProperty ppe = new PathElementProperty(modelRegistry.EP_PROPERTIES, epe);
            expResult.addPathElement(ppe, false, false);
            PathElementCustomProperty cppe = new PathElementCustomProperty("property1", ppe);
            expResult.addPathElement(cppe, false, false);
            PathElementArrayIndex cpai = new PathElementArrayIndex(2, cppe);
            expResult.addPathElement(cpai, false, false);
            Assert.assertEquals(expResult, result);
        }
        {
            String path = "/Things(1)/properties/property1[2][3]";
            ResourcePath result = PathParser.parsePath(modelRegistry, "", Version.V_1_1, path);
            ResourcePath expResult = new ResourcePath("", Version.V_1_1, path);
            PathElementEntitySet espe = new PathElementEntitySet(modelRegistry.THING, null);
            expResult.addPathElement(espe, false, false);
            PathElementEntity epe = new PathElementEntity(new IdLong(1), modelRegistry.THING, espe);
            expResult.addPathElement(epe, true, true);
            PathElementProperty ppe = new PathElementProperty(modelRegistry.EP_PROPERTIES, epe);
            expResult.addPathElement(ppe, false, false);
            PathElementCustomProperty cppe = new PathElementCustomProperty("property1", ppe);
            expResult.addPathElement(cppe, false, false);
            PathElementArrayIndex cpai = new PathElementArrayIndex(2, cppe);
            expResult.addPathElement(cpai, false, false);
            cpai = new PathElementArrayIndex(3, cpai);
            expResult.addPathElement(cpai, false, false);
            Assert.assertEquals(expResult, result);
        }
        {
            String path = "/Things(1)/properties/property1[2]/deep[3]";
            ResourcePath result = PathParser.parsePath(modelRegistry, "", Version.V_1_1, path);
            ResourcePath expResult = new ResourcePath("", Version.V_1_1, path);
            PathElementEntitySet espe = new PathElementEntitySet(modelRegistry.THING, null);
            expResult.addPathElement(espe, false, false);
            PathElementEntity epe = new PathElementEntity(new IdLong(1), modelRegistry.THING, espe);
            expResult.addPathElement(epe, true, true);
            PathElementProperty ppe = new PathElementProperty(modelRegistry.EP_PROPERTIES, epe);
            expResult.addPathElement(ppe, false, false);
            PathElementCustomProperty cppe = new PathElementCustomProperty("property1", ppe);
            expResult.addPathElement(cppe, false, false);
            PathElementArrayIndex cpai = new PathElementArrayIndex(2, cppe);
            expResult.addPathElement(cpai, false, false);
            cppe = new PathElementCustomProperty("deep", cpai);
            expResult.addPathElement(cppe, false, false);
            cpai = new PathElementArrayIndex(3, cppe);
            expResult.addPathElement(cpai, false, false);
            Assert.assertEquals(expResult, result);
        }
    }

    @Test
    public void testPathEntityObservation() {
        String path = "/Observations(1)/parameters/property1";
        ResourcePath result = PathParser.parsePath(modelRegistry, "", Version.V_1_1, path);

        ResourcePath expResult = new ResourcePath("", Version.V_1_1, path);
        PathElementEntitySet espe = new PathElementEntitySet(modelRegistry.OBSERVATION, null);
        expResult.addPathElement(espe, false, false);
        PathElementEntity epe = new PathElementEntity(new IdLong(1), modelRegistry.OBSERVATION, espe);
        expResult.addPathElement(epe, true, true);
        PathElementProperty ppe = new PathElementProperty(modelRegistry.EP_PARAMETERS, epe);
        expResult.addPathElement(ppe, false, false);
        PathElementCustomProperty cppe = new PathElementCustomProperty("property1", ppe);
        expResult.addPathElement(cppe, false, false);
        Assert.assertEquals(expResult, result);

        path = "/Observations(1)/parameters/property1[2]";
        result = PathParser.parsePath(modelRegistry, "", Version.V_1_1, path);
        expResult = new ResourcePath("", Version.V_1_1, path);
        espe = new PathElementEntitySet(modelRegistry.OBSERVATION, null);
        expResult.addPathElement(espe, false, false);
        epe = new PathElementEntity(new IdLong(1), modelRegistry.OBSERVATION, espe);
        expResult.addPathElement(epe, true, true);
        ppe = new PathElementProperty(modelRegistry.EP_PARAMETERS, epe);
        expResult.addPathElement(ppe, false, false);
        cppe = new PathElementCustomProperty("property1", ppe);
        expResult.addPathElement(cppe, false, false);
        PathElementArrayIndex cpai = new PathElementArrayIndex(2, cppe);
        expResult.addPathElement(cpai, false, false);
        Assert.assertEquals(expResult, result);

        path = "/Observations(1)/parameters/property1[2][3]";
        result = PathParser.parsePath(modelRegistry, "", Version.V_1_1, path);
        expResult = new ResourcePath("", Version.V_1_1, path);
        espe = new PathElementEntitySet(modelRegistry.OBSERVATION, null);
        expResult.addPathElement(espe, false, false);
        epe = new PathElementEntity(new IdLong(1), modelRegistry.OBSERVATION, espe);
        expResult.addPathElement(epe, true, true);
        ppe = new PathElementProperty(modelRegistry.EP_PARAMETERS, epe);
        expResult.addPathElement(ppe, false, false);
        cppe = new PathElementCustomProperty("property1", ppe);
        expResult.addPathElement(cppe, false, false);
        cpai = new PathElementArrayIndex(2, cppe);
        expResult.addPathElement(cpai, false, false);
        cpai = new PathElementArrayIndex(3, cpai);
        expResult.addPathElement(cpai, false, false);
        Assert.assertEquals(expResult, result);

        path = "/Observations(1)/parameters/property1[2]/deep[3]";
        result = PathParser.parsePath(modelRegistry, "", Version.V_1_1, path);
        expResult = new ResourcePath("", Version.V_1_1, path);
        espe = new PathElementEntitySet(modelRegistry.OBSERVATION, null);
        expResult.addPathElement(espe, false, false);
        epe = new PathElementEntity(new IdLong(1), modelRegistry.OBSERVATION, espe);
        expResult.addPathElement(epe, true, true);
        ppe = new PathElementProperty(modelRegistry.EP_PARAMETERS, epe);
        expResult.addPathElement(ppe, false, false);
        cppe = new PathElementCustomProperty("property1", ppe);
        expResult.addPathElement(cppe, false, false);
        cpai = new PathElementArrayIndex(2, cppe);
        expResult.addPathElement(cpai, false, false);
        cppe = new PathElementCustomProperty("deep", cpai);
        expResult.addPathElement(cppe, false, false);
        cpai = new PathElementArrayIndex(3, cppe);
        expResult.addPathElement(cpai, false, false);
        Assert.assertEquals(expResult, result);

        path = "/Observations(1)/result/property1";
        result = PathParser.parsePath(modelRegistry, "", Version.V_1_1, path);

        expResult = new ResourcePath("", Version.V_1_1, path);
        espe = new PathElementEntitySet(modelRegistry.OBSERVATION, null);
        expResult.addPathElement(espe, false, false);
        epe = new PathElementEntity(new IdLong(1), modelRegistry.OBSERVATION, espe);
        expResult.addPathElement(epe, true, true);
        ppe = new PathElementProperty(modelRegistry.EP_RESULT, epe);
        expResult.addPathElement(ppe, false, false);
        cppe = new PathElementCustomProperty("property1", ppe);
        expResult.addPathElement(cppe, false, false);
        Assert.assertEquals(expResult, result);

        path = "/Observations(1)/result[2]";
        result = PathParser.parsePath(modelRegistry, "", Version.V_1_1, path);
        expResult = new ResourcePath("", Version.V_1_1, path);
        espe = new PathElementEntitySet(modelRegistry.OBSERVATION, null);
        expResult.addPathElement(espe, false, false);
        epe = new PathElementEntity(new IdLong(1), modelRegistry.OBSERVATION, espe);
        expResult.addPathElement(epe, true, true);
        ppe = new PathElementProperty(modelRegistry.EP_RESULT, epe);
        expResult.addPathElement(ppe, false, false);
        cpai = new PathElementArrayIndex(2, ppe);
        expResult.addPathElement(cpai, false, false);
        Assert.assertEquals(expResult, result);

    }

    @Test
    public void testPathdeep1() {
        String path = "/Things(1)/Locations(2)/HistoricalLocations(3)/Thing/Datastreams(5)/Sensor/Datastreams(6)/ObservedProperty/Datastreams(7)/Observations(8)/FeatureOfInterest";
        ResourcePath result = PathParser.parsePath(modelRegistry, "", Version.V_1_1, path);

        ResourcePath expResult = new ResourcePath("", Version.V_1_1, path);

        PathElementEntitySet espe = new PathElementEntitySet(modelRegistry.THING, null);
        expResult.addPathElement(espe, false, false);
        PathElementEntity epe = new PathElementEntity(new IdLong(1), modelRegistry.THING, espe);
        expResult.addPathElement(epe, false, false);

        espe = new PathElementEntitySet(modelRegistry.LOCATION, epe);
        expResult.addPathElement(espe, false, false);
        epe = new PathElementEntity(new IdLong(2), modelRegistry.LOCATION, espe);
        expResult.addPathElement(epe, false, false);

        espe = new PathElementEntitySet(modelRegistry.HISTORICAL_LOCATION, epe);
        expResult.addPathElement(espe, false, false);
        epe = new PathElementEntity(new IdLong(3), modelRegistry.HISTORICAL_LOCATION, espe);
        expResult.addPathElement(epe, false, false);

        epe = new PathElementEntity(null, modelRegistry.THING, epe);
        expResult.addPathElement(epe, false, false);

        espe = new PathElementEntitySet(modelRegistry.DATASTREAM, epe);
        expResult.addPathElement(espe, false, false);
        epe = new PathElementEntity(new IdLong(5), modelRegistry.DATASTREAM, espe);
        expResult.addPathElement(epe, false, false);

        epe = new PathElementEntity(null, modelRegistry.SENSOR, epe);
        expResult.addPathElement(epe, false, false);

        espe = new PathElementEntitySet(modelRegistry.DATASTREAM, epe);
        expResult.addPathElement(espe, false, false);
        epe = new PathElementEntity(new IdLong(6), modelRegistry.DATASTREAM, espe);
        expResult.addPathElement(epe, false, false);

        epe = new PathElementEntity(null, modelRegistry.OBSERVED_PROPERTY, epe);
        expResult.addPathElement(epe, false, false);

        espe = new PathElementEntitySet(modelRegistry.DATASTREAM, epe);
        expResult.addPathElement(espe, false, false);
        epe = new PathElementEntity(new IdLong(7), modelRegistry.DATASTREAM, espe);
        expResult.addPathElement(epe, false, false);

        espe = new PathElementEntitySet(modelRegistry.OBSERVATION, epe);
        expResult.addPathElement(espe, false, false);
        epe = new PathElementEntity(new IdLong(8), modelRegistry.OBSERVATION, espe);
        expResult.addPathElement(epe, false, true);

        epe = new PathElementEntity(null, modelRegistry.FEATURE_OF_INTEREST, epe);
        expResult.addPathElement(epe, true, false);

        Assert.assertEquals(expResult, result);
    }

    @Test
    public void testPathdeep2() {
        String path = "/FeaturesOfInterest(1)/Observations(2)/Datastream/Thing/HistoricalLocations(3)/Locations(4)/Things(1)/properties/property1";
        ResourcePath result = PathParser.parsePath(modelRegistry, "", Version.V_1_1, path);

        ResourcePath expResult = new ResourcePath("", Version.V_1_1, path);

        PathElementEntitySet espe = new PathElementEntitySet(modelRegistry.FEATURE_OF_INTEREST, null);
        expResult.addPathElement(espe, false, false);
        PathElementEntity epe = new PathElementEntity(new IdLong(1), modelRegistry.FEATURE_OF_INTEREST, espe);
        expResult.addPathElement(epe, false, false);

        espe = new PathElementEntitySet(modelRegistry.OBSERVATION, epe);
        expResult.addPathElement(espe, false, false);
        epe = new PathElementEntity(new IdLong(2), modelRegistry.OBSERVATION, espe);
        expResult.addPathElement(epe, false, false);

        epe = new PathElementEntity(null, modelRegistry.DATASTREAM, epe);
        expResult.addPathElement(epe, false, false);

        epe = new PathElementEntity(null, modelRegistry.THING, epe);
        expResult.addPathElement(epe, false, false);

        espe = new PathElementEntitySet(modelRegistry.HISTORICAL_LOCATION, epe);
        expResult.addPathElement(espe, false, false);
        epe = new PathElementEntity(new IdLong(3), modelRegistry.HISTORICAL_LOCATION, espe);
        expResult.addPathElement(epe, false, false);

        espe = new PathElementEntitySet(modelRegistry.LOCATION, epe);
        expResult.addPathElement(espe, false, false);
        epe = new PathElementEntity(new IdLong(4), modelRegistry.LOCATION, espe);
        expResult.addPathElement(epe, false, false);

        espe = new PathElementEntitySet(modelRegistry.THING, epe);
        expResult.addPathElement(espe, false, false);
        epe = new PathElementEntity(new IdLong(1), modelRegistry.THING, espe);
        expResult.addPathElement(epe, true, true);
        PathElementProperty ppe = new PathElementProperty(modelRegistry.EP_PROPERTIES, epe);
        expResult.addPathElement(ppe, false, false);
        PathElementCustomProperty cppe = new PathElementCustomProperty("property1", ppe);
        expResult.addPathElement(cppe, false, false);

        Assert.assertEquals(expResult, result);
    }

    @Test
    public void testPathdeep3() {
        String path = "/Things(1)/Locations(2)/HistoricalLocations(3)/Thing/MultiDatastreams(5)/Sensor/MultiDatastreams(6)/ObservedProperties(7)/MultiDatastreams(8)/Observations(9)/FeatureOfInterest";
        ResourcePath result = PathParser.parsePath(modelRegistry, "", Version.V_1_1, path);

        ResourcePath expResult = new ResourcePath("", Version.V_1_1, path);

        PathElementEntitySet espe = new PathElementEntitySet(modelRegistry.THING, null);
        expResult.addPathElement(espe, false, false);
        PathElementEntity epe = new PathElementEntity(new IdLong(1), modelRegistry.THING, espe);
        expResult.addPathElement(epe, false, false);

        espe = new PathElementEntitySet(modelRegistry.LOCATION, epe);
        expResult.addPathElement(espe, false, false);
        epe = new PathElementEntity(new IdLong(2), modelRegistry.LOCATION, espe);
        expResult.addPathElement(epe, false, false);

        espe = new PathElementEntitySet(modelRegistry.HISTORICAL_LOCATION, epe);
        expResult.addPathElement(espe, false, false);
        epe = new PathElementEntity(new IdLong(3), modelRegistry.HISTORICAL_LOCATION, espe);
        expResult.addPathElement(epe, false, false);

        epe = new PathElementEntity(null, modelRegistry.THING, epe);
        expResult.addPathElement(epe, false, false);

        espe = new PathElementEntitySet(modelRegistry.MULTI_DATASTREAM, epe);
        expResult.addPathElement(espe, false, false);
        epe = new PathElementEntity(new IdLong(5), modelRegistry.MULTI_DATASTREAM, espe);
        expResult.addPathElement(epe, false, false);

        epe = new PathElementEntity(null, modelRegistry.SENSOR, epe);
        expResult.addPathElement(epe, false, false);

        espe = new PathElementEntitySet(modelRegistry.MULTI_DATASTREAM, epe);
        expResult.addPathElement(espe, false, false);
        epe = new PathElementEntity(new IdLong(6), modelRegistry.MULTI_DATASTREAM, espe);
        expResult.addPathElement(epe, false, false);

        espe = new PathElementEntitySet(modelRegistry.OBSERVED_PROPERTY, epe);
        expResult.addPathElement(espe, false, false);
        epe = new PathElementEntity(new IdLong(7), modelRegistry.OBSERVED_PROPERTY, espe);
        expResult.addPathElement(epe, false, false);

        espe = new PathElementEntitySet(modelRegistry.MULTI_DATASTREAM, epe);
        expResult.addPathElement(espe, false, false);
        epe = new PathElementEntity(new IdLong(8), modelRegistry.MULTI_DATASTREAM, espe);
        expResult.addPathElement(epe, false, false);

        espe = new PathElementEntitySet(modelRegistry.OBSERVATION, epe);
        expResult.addPathElement(espe, false, false);
        epe = new PathElementEntity(new IdLong(9), modelRegistry.OBSERVATION, espe);
        expResult.addPathElement(epe, false, true);

        epe = new PathElementEntity(null, modelRegistry.FEATURE_OF_INTEREST, epe);
        expResult.addPathElement(epe, true, false);

        Assert.assertEquals(expResult, result);
    }

    @Test
    public void testPathdeep4() {
        String path = "/FeaturesOfInterest(1)/Observations(2)/MultiDatastream/Thing/HistoricalLocations(3)/Locations(4)/Things(1)/properties/property1/subproperty2";
        ResourcePath result = PathParser.parsePath(modelRegistry, "", Version.V_1_1, path);

        ResourcePath expResult = new ResourcePath("", Version.V_1_1, path);

        PathElementEntitySet espe = new PathElementEntitySet(modelRegistry.FEATURE_OF_INTEREST, null);
        expResult.addPathElement(espe, false, false);
        PathElementEntity epe = new PathElementEntity(new IdLong(1), modelRegistry.FEATURE_OF_INTEREST, espe);
        expResult.addPathElement(epe, false, false);

        espe = new PathElementEntitySet(modelRegistry.OBSERVATION, epe);
        expResult.addPathElement(espe, false, false);
        epe = new PathElementEntity(new IdLong(2), modelRegistry.OBSERVATION, espe);
        expResult.addPathElement(epe, false, false);

        epe = new PathElementEntity(null, modelRegistry.MULTI_DATASTREAM, epe);
        expResult.addPathElement(epe, false, false);

        epe = new PathElementEntity(null, modelRegistry.THING, epe);
        expResult.addPathElement(epe, false, false);

        espe = new PathElementEntitySet(modelRegistry.HISTORICAL_LOCATION, epe);
        expResult.addPathElement(espe, false, false);
        epe = new PathElementEntity(new IdLong(3), modelRegistry.HISTORICAL_LOCATION, espe);
        expResult.addPathElement(epe, false, false);

        espe = new PathElementEntitySet(modelRegistry.LOCATION, epe);
        expResult.addPathElement(espe, false, false);
        epe = new PathElementEntity(new IdLong(4), modelRegistry.LOCATION, espe);
        expResult.addPathElement(epe, false, false);

        espe = new PathElementEntitySet(modelRegistry.THING, epe);
        expResult.addPathElement(espe, false, false);
        epe = new PathElementEntity(new IdLong(1), modelRegistry.THING, espe);
        expResult.addPathElement(epe, true, true);
        PathElementProperty ppe = new PathElementProperty(modelRegistry.EP_PROPERTIES, epe);
        expResult.addPathElement(ppe, false, false);
        PathElementCustomProperty cppe = new PathElementCustomProperty("property1", ppe);
        expResult.addPathElement(cppe, false, false);
        cppe = new PathElementCustomProperty("subproperty2", cppe);
        expResult.addPathElement(cppe, false, false);

        Assert.assertEquals(expResult, result);
    }

    @Test
    public void testPathdeepCompressed1() {
        String path = "/Observations(11)/Datastream/Thing";
        ResourcePath result = PathParser.parsePath(modelRegistry, "", Version.V_1_1, path);
        result.compress();

        ResourcePath expResult = new ResourcePath("", Version.V_1_1, path);
        PathElementEntitySet espe = new PathElementEntitySet(modelRegistry.OBSERVATION, null);
        expResult.addPathElement(espe, false, false);
        PathElementEntity epe = new PathElementEntity(new IdLong(11), modelRegistry.OBSERVATION, espe);
        expResult.addPathElement(epe, false, true);

        epe = new PathElementEntity(null, modelRegistry.DATASTREAM, epe);
        expResult.addPathElement(epe, false, false);

        epe = new PathElementEntity(null, modelRegistry.THING, epe);
        expResult.addPathElement(epe, true, false);

        Assert.assertEquals(expResult, result);
    }

    @Test
    public void testPathdeepCompressed2() {
        String path = "/Datastreams(5)/Observations(11)/Datastream/Thing";
        ResourcePath result = PathParser.parsePath(modelRegistry, "", Version.V_1_1, path);
        result.compress();

        ResourcePath expResult = new ResourcePath("", Version.V_1_1, path);
        PathElementEntitySet espe = new PathElementEntitySet(modelRegistry.OBSERVATION, null);
        expResult.addPathElement(espe, false, false);
        PathElementEntity epe = new PathElementEntity(new IdLong(11), modelRegistry.OBSERVATION, espe);
        expResult.addPathElement(epe, false, true);

        epe = new PathElementEntity(null, modelRegistry.DATASTREAM, epe);
        expResult.addPathElement(epe, false, false);

        epe = new PathElementEntity(null, modelRegistry.THING, epe);
        expResult.addPathElement(epe, true, false);

        Assert.assertEquals(expResult, result);
    }

    private void testThing(long id) {
        String path = "/Things(" + id + ")";
        ResourcePath result = PathParser.parsePath(modelRegistry, new IdManagerLong(), "", Version.V_1_1, path);

        ResourcePath expResult = new ResourcePath("", Version.V_1_1, path);
        PathElementEntitySet espe = new PathElementEntitySet(modelRegistry.THING, null);
        expResult.addPathElement(espe, false, false);
        PathElementEntity epe = new PathElementEntity(new IdLong(id), modelRegistry.THING, espe);
        expResult.addPathElement(epe, true, true);

        Assert.assertEquals(expResult, result);
    }

    private void testThing(String id) {
        String path = "/Things('" + id + "')";
        ResourcePath result = PathParser.parsePath(modelRegistry, new IdManagerString(), "", Version.V_1_1, path);

        ResourcePath expResult = new ResourcePath("", Version.V_1_1, path);
        PathElementEntitySet espe = new PathElementEntitySet(modelRegistry.THING, null);
        expResult.addPathElement(espe, false, false);
        PathElementEntity epe = new PathElementEntity(new IdString(id), modelRegistry.THING, espe);
        expResult.addPathElement(epe, true, true);

        Assert.assertEquals(expResult, result);
    }

}
