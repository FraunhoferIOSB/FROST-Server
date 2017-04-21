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
package de.fraunhofer.iosb.ilt.sta.parser;

import de.fraunhofer.iosb.ilt.sta.model.id.LongId;
import de.fraunhofer.iosb.ilt.sta.parser.path.PathParser;
import de.fraunhofer.iosb.ilt.sta.path.CustomPropertyArrayIndex;
import de.fraunhofer.iosb.ilt.sta.path.CustomPropertyPathElement;
import de.fraunhofer.iosb.ilt.sta.path.EntityPathElement;
import de.fraunhofer.iosb.ilt.sta.path.EntityProperty;
import de.fraunhofer.iosb.ilt.sta.path.EntitySetPathElement;
import de.fraunhofer.iosb.ilt.sta.path.EntityType;
import de.fraunhofer.iosb.ilt.sta.path.Property;
import de.fraunhofer.iosb.ilt.sta.path.PropertyPathElement;
import de.fraunhofer.iosb.ilt.sta.path.ResourcePath;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author jab
 */
public class PathParserTest {

    public PathParserTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testParsePath_setThings() {
        String path = "/Things";
        ResourcePath result = PathParser.parsePath("", path);

        ResourcePath expResult = new ResourcePath("", path);
        EntitySetPathElement espe = new EntitySetPathElement(EntityType.Thing, null);
        expResult.addPathElement(espe, true, false);
        expResult.setMainElement(espe);

        assert (result.equals(expResult));
    }

    @Test
    public void testParsePath_setThingsRef() {
        String path = "/Things/$ref";
        ResourcePath result = PathParser.parsePath("", path);

        ResourcePath expResult = new ResourcePath("", path);
        EntitySetPathElement espe = new EntitySetPathElement(EntityType.Thing, null);
        expResult.addPathElement(espe, true, false);
        expResult.setMainElement(espe);
        expResult.setRef(true);

        assert (result.equals(expResult));
    }

    private void testThing(long id) {
        String path = "/Things(" + id + ")";
        ResourcePath result = PathParser.parsePath("", path);

        ResourcePath expResult = new ResourcePath("", path);
        EntitySetPathElement espe = new EntitySetPathElement(EntityType.Thing, null);
        expResult.addPathElement(espe, false, false);
        EntityPathElement epe = new EntityPathElement(new LongId(id), EntityType.Thing, espe);
        expResult.addPathElement(epe, true, true);

        assert (result.equals(expResult));
    }

    @Test
    public void testParsePath_entityThing() {
        testThing(0);
        testThing(1);
        testThing(-1);
        testThing(Long.MAX_VALUE);
        testThing(Long.MIN_VALUE);
    }

    @Test
    public void testParsePath_entityProperty() {
        for (EntityType entityType : EntityType.values()) {
            for (Property property : entityType.getPropertySet()) {
                if (property instanceof EntityProperty) {
                    EntityProperty entityProperty = (EntityProperty) property;

                    String path = "/" + entityType.plural + "(1)/" + property.getName();
                    ResourcePath result = PathParser.parsePath("", path);
                    ResourcePath expResult = new ResourcePath("", path);
                    EntitySetPathElement espe = new EntitySetPathElement(entityType, null);
                    expResult.addPathElement(espe, false, false);
                    EntityPathElement epe = new EntityPathElement(new LongId(1), entityType, espe);
                    expResult.addPathElement(epe, true, true);
                    PropertyPathElement ppe = new PropertyPathElement(entityProperty, epe);
                    expResult.addPathElement(ppe, false, false);

                    assert (result.equals(expResult));
                }
            }
        }
    }

    @Test
    public void testParsePath_entityThingPropertyValue() {
        String path = "/Things(1)/properties/$value";
        ResourcePath result = PathParser.parsePath("", path);

        ResourcePath expResult = new ResourcePath("", path);
        EntitySetPathElement espe = new EntitySetPathElement(EntityType.Thing, null);
        expResult.addPathElement(espe, false, false);
        EntityPathElement epe = new EntityPathElement(new LongId(1), EntityType.Thing, espe);
        expResult.addPathElement(epe, true, true);
        PropertyPathElement ppe = new PropertyPathElement(EntityProperty.Properties, epe);
        expResult.addPathElement(ppe, false, false);
        expResult.setValue(true);

        assert (result.equals(expResult));
    }

    @Test
    public void testParsePath_entityThingSubProperty() {
        String path = "/Things(1)/properties/property1";
        ResourcePath result = PathParser.parsePath("", path);

        ResourcePath expResult = new ResourcePath("", path);
        EntitySetPathElement espe = new EntitySetPathElement(EntityType.Thing, null);
        expResult.addPathElement(espe, false, false);
        EntityPathElement epe = new EntityPathElement(new LongId(1), EntityType.Thing, espe);
        expResult.addPathElement(epe, true, true);
        PropertyPathElement ppe = new PropertyPathElement(EntityProperty.Properties, epe);
        expResult.addPathElement(ppe, false, false);
        CustomPropertyPathElement cppe = new CustomPropertyPathElement("property1", ppe);
        expResult.addPathElement(cppe, false, false);
        assert (result.equals(expResult));

        path = "/Things(1)/properties/property1[2]";
        result = PathParser.parsePath("", path);
        expResult = new ResourcePath("", path);
        espe = new EntitySetPathElement(EntityType.Thing, null);
        expResult.addPathElement(espe, false, false);
        epe = new EntityPathElement(new LongId(1), EntityType.Thing, espe);
        expResult.addPathElement(epe, true, true);
        ppe = new PropertyPathElement(EntityProperty.Properties, epe);
        expResult.addPathElement(ppe, false, false);
        cppe = new CustomPropertyPathElement("property1", ppe);
        expResult.addPathElement(cppe, false, false);
        CustomPropertyArrayIndex cpai = new CustomPropertyArrayIndex(2, cppe);
        expResult.addPathElement(cpai, false, false);
        assert (result.equals(expResult));

        path = "/Things(1)/properties/property1[2][3]";
        result = PathParser.parsePath("", path);
        expResult = new ResourcePath("", path);
        espe = new EntitySetPathElement(EntityType.Thing, null);
        expResult.addPathElement(espe, false, false);
        epe = new EntityPathElement(new LongId(1), EntityType.Thing, espe);
        expResult.addPathElement(epe, true, true);
        ppe = new PropertyPathElement(EntityProperty.Properties, epe);
        expResult.addPathElement(ppe, false, false);
        cppe = new CustomPropertyPathElement("property1", ppe);
        expResult.addPathElement(cppe, false, false);
        cpai = new CustomPropertyArrayIndex(2, cppe);
        expResult.addPathElement(cpai, false, false);
        cpai = new CustomPropertyArrayIndex(3, cpai);
        expResult.addPathElement(cpai, false, false);
        assert (result.equals(expResult));

        path = "/Things(1)/properties/property1[2]/deep[3]";
        result = PathParser.parsePath("", path);
        expResult = new ResourcePath("", path);
        espe = new EntitySetPathElement(EntityType.Thing, null);
        expResult.addPathElement(espe, false, false);
        epe = new EntityPathElement(new LongId(1), EntityType.Thing, espe);
        expResult.addPathElement(epe, true, true);
        ppe = new PropertyPathElement(EntityProperty.Properties, epe);
        expResult.addPathElement(ppe, false, false);
        cppe = new CustomPropertyPathElement("property1", ppe);
        expResult.addPathElement(cppe, false, false);
        cpai = new CustomPropertyArrayIndex(2, cppe);
        expResult.addPathElement(cpai, false, false);
        cppe = new CustomPropertyPathElement("deep", cpai);
        expResult.addPathElement(cppe, false, false);
        cpai = new CustomPropertyArrayIndex(3, cppe);
        expResult.addPathElement(cpai, false, false);
        assert (result.equals(expResult));

    }

    @Test
    public void testParsePath_entityObservation() {
        String path = "/Observations(1)/parameters/property1";
        ResourcePath result = PathParser.parsePath("", path);

        ResourcePath expResult = new ResourcePath("", path);
        EntitySetPathElement espe = new EntitySetPathElement(EntityType.Observation, null);
        expResult.addPathElement(espe, false, false);
        EntityPathElement epe = new EntityPathElement(new LongId(1), EntityType.Observation, espe);
        expResult.addPathElement(epe, true, true);
        PropertyPathElement ppe = new PropertyPathElement(EntityProperty.Parameters, epe);
        expResult.addPathElement(ppe, false, false);
        CustomPropertyPathElement cppe = new CustomPropertyPathElement("property1", ppe);
        expResult.addPathElement(cppe, false, false);
        assert (result.equals(expResult));

        path = "/Observations(1)/parameters/property1[2]";
        result = PathParser.parsePath("", path);
        expResult = new ResourcePath("", path);
        espe = new EntitySetPathElement(EntityType.Observation, null);
        expResult.addPathElement(espe, false, false);
        epe = new EntityPathElement(new LongId(1), EntityType.Observation, espe);
        expResult.addPathElement(epe, true, true);
        ppe = new PropertyPathElement(EntityProperty.Parameters, epe);
        expResult.addPathElement(ppe, false, false);
        cppe = new CustomPropertyPathElement("property1", ppe);
        expResult.addPathElement(cppe, false, false);
        CustomPropertyArrayIndex cpai = new CustomPropertyArrayIndex(2, cppe);
        expResult.addPathElement(cpai, false, false);
        assert (result.equals(expResult));

        path = "/Observations(1)/parameters/property1[2][3]";
        result = PathParser.parsePath("", path);
        expResult = new ResourcePath("", path);
        espe = new EntitySetPathElement(EntityType.Observation, null);
        expResult.addPathElement(espe, false, false);
        epe = new EntityPathElement(new LongId(1), EntityType.Observation, espe);
        expResult.addPathElement(epe, true, true);
        ppe = new PropertyPathElement(EntityProperty.Parameters, epe);
        expResult.addPathElement(ppe, false, false);
        cppe = new CustomPropertyPathElement("property1", ppe);
        expResult.addPathElement(cppe, false, false);
        cpai = new CustomPropertyArrayIndex(2, cppe);
        expResult.addPathElement(cpai, false, false);
        cpai = new CustomPropertyArrayIndex(3, cpai);
        expResult.addPathElement(cpai, false, false);
        assert (result.equals(expResult));

        path = "/Observations(1)/parameters/property1[2]/deep[3]";
        result = PathParser.parsePath("", path);
        expResult = new ResourcePath("", path);
        espe = new EntitySetPathElement(EntityType.Observation, null);
        expResult.addPathElement(espe, false, false);
        epe = new EntityPathElement(new LongId(1), EntityType.Observation, espe);
        expResult.addPathElement(epe, true, true);
        ppe = new PropertyPathElement(EntityProperty.Parameters, epe);
        expResult.addPathElement(ppe, false, false);
        cppe = new CustomPropertyPathElement("property1", ppe);
        expResult.addPathElement(cppe, false, false);
        cpai = new CustomPropertyArrayIndex(2, cppe);
        expResult.addPathElement(cpai, false, false);
        cppe = new CustomPropertyPathElement("deep", cpai);
        expResult.addPathElement(cppe, false, false);
        cpai = new CustomPropertyArrayIndex(3, cppe);
        expResult.addPathElement(cpai, false, false);
        assert (result.equals(expResult));

        path = "/Observations(1)/result/property1";
        result = PathParser.parsePath("", path);

        expResult = new ResourcePath("", path);
        espe = new EntitySetPathElement(EntityType.Observation, null);
        expResult.addPathElement(espe, false, false);
        epe = new EntityPathElement(new LongId(1), EntityType.Observation, espe);
        expResult.addPathElement(epe, true, true);
        ppe = new PropertyPathElement(EntityProperty.Result, epe);
        expResult.addPathElement(ppe, false, false);
        cppe = new CustomPropertyPathElement("property1", ppe);
        expResult.addPathElement(cppe, false, false);
        assert (result.equals(expResult));

        path = "/Observations(1)/result[2]";
        result = PathParser.parsePath("", path);
        expResult = new ResourcePath("", path);
        espe = new EntitySetPathElement(EntityType.Observation, null);
        expResult.addPathElement(espe, false, false);
        epe = new EntityPathElement(new LongId(1), EntityType.Observation, espe);
        expResult.addPathElement(epe, true, true);
        ppe = new PropertyPathElement(EntityProperty.Result, epe);
        expResult.addPathElement(ppe, false, false);
        cpai = new CustomPropertyArrayIndex(2, ppe);
        expResult.addPathElement(cpai, false, false);
        assert (result.equals(expResult));

    }

    @Test
    public void testParsePath_deep1() {
        String path = "/Things(1)/Locations(2)/HistoricalLocations(3)/Thing/Datastreams(5)/Sensor/Datastreams(6)/ObservedProperty/Datastreams(7)/Observations(8)/FeatureOfInterest";
        ResourcePath result = PathParser.parsePath("", path);

        ResourcePath expResult = new ResourcePath("", path);

        EntitySetPathElement espe = new EntitySetPathElement(EntityType.Thing, null);
        expResult.addPathElement(espe, false, false);
        EntityPathElement epe = new EntityPathElement(new LongId(1), EntityType.Thing, espe);
        expResult.addPathElement(epe, false, false);

        espe = new EntitySetPathElement(EntityType.Location, epe);
        expResult.addPathElement(espe, false, false);
        epe = new EntityPathElement(new LongId(2), EntityType.Location, espe);
        expResult.addPathElement(epe, false, false);

        espe = new EntitySetPathElement(EntityType.HistoricalLocation, epe);
        expResult.addPathElement(espe, false, false);
        epe = new EntityPathElement(new LongId(3), EntityType.HistoricalLocation, espe);
        expResult.addPathElement(epe, false, false);

        epe = new EntityPathElement(null, EntityType.Thing, epe);
        expResult.addPathElement(epe, false, false);

        espe = new EntitySetPathElement(EntityType.Datastream, epe);
        expResult.addPathElement(espe, false, false);
        epe = new EntityPathElement(new LongId(5), EntityType.Datastream, espe);
        expResult.addPathElement(epe, false, false);

        epe = new EntityPathElement(null, EntityType.Sensor, epe);
        expResult.addPathElement(epe, false, false);

        espe = new EntitySetPathElement(EntityType.Datastream, epe);
        expResult.addPathElement(espe, false, false);
        epe = new EntityPathElement(new LongId(6), EntityType.Datastream, espe);
        expResult.addPathElement(epe, false, false);

        epe = new EntityPathElement(null, EntityType.ObservedProperty, epe);
        expResult.addPathElement(epe, false, false);

        espe = new EntitySetPathElement(EntityType.Datastream, epe);
        expResult.addPathElement(espe, false, false);
        epe = new EntityPathElement(new LongId(7), EntityType.Datastream, espe);
        expResult.addPathElement(epe, false, false);

        espe = new EntitySetPathElement(EntityType.Observation, epe);
        expResult.addPathElement(espe, false, false);
        epe = new EntityPathElement(new LongId(8), EntityType.Observation, espe);
        expResult.addPathElement(epe, false, true);

        epe = new EntityPathElement(null, EntityType.FeatureOfInterest, epe);
        expResult.addPathElement(epe, true, false);

        assert (result.equals(expResult));
    }

    @Test
    public void testParsePath_deep2() {
        String path = "/FeaturesOfInterest(1)/Observations(2)/Datastream/Thing/HistoricalLocations(3)/Locations(4)/Things(1)/properties/property1";
        ResourcePath result = PathParser.parsePath("", path);

        ResourcePath expResult = new ResourcePath("", path);

        EntitySetPathElement espe = new EntitySetPathElement(EntityType.FeatureOfInterest, null);
        expResult.addPathElement(espe, false, false);
        EntityPathElement epe = new EntityPathElement(new LongId(1), EntityType.FeatureOfInterest, espe);
        expResult.addPathElement(epe, false, false);

        espe = new EntitySetPathElement(EntityType.Observation, epe);
        expResult.addPathElement(espe, false, false);
        epe = new EntityPathElement(new LongId(2), EntityType.Observation, espe);
        expResult.addPathElement(epe, false, false);

        epe = new EntityPathElement(null, EntityType.Datastream, epe);
        expResult.addPathElement(epe, false, false);

        epe = new EntityPathElement(null, EntityType.Thing, epe);
        expResult.addPathElement(epe, false, false);

        espe = new EntitySetPathElement(EntityType.HistoricalLocation, epe);
        expResult.addPathElement(espe, false, false);
        epe = new EntityPathElement(new LongId(3), EntityType.HistoricalLocation, espe);
        expResult.addPathElement(epe, false, false);

        espe = new EntitySetPathElement(EntityType.Location, epe);
        expResult.addPathElement(espe, false, false);
        epe = new EntityPathElement(new LongId(4), EntityType.Location, espe);
        expResult.addPathElement(epe, false, false);

        espe = new EntitySetPathElement(EntityType.Thing, epe);
        expResult.addPathElement(espe, false, false);
        epe = new EntityPathElement(new LongId(1), EntityType.Thing, espe);
        expResult.addPathElement(epe, true, true);
        PropertyPathElement ppe = new PropertyPathElement(EntityProperty.Properties, epe);
        expResult.addPathElement(ppe, false, false);
        CustomPropertyPathElement cppe = new CustomPropertyPathElement("property1", ppe);
        expResult.addPathElement(cppe, false, false);

        assert (result.equals(expResult));
    }

    @Test
    public void testParsePath_deep3() {
        String path = "/Things(1)/Locations(2)/HistoricalLocations(3)/Thing/MultiDatastreams(5)/Sensor/MultiDatastreams(6)/ObservedProperties(7)/MultiDatastreams(8)/Observations(9)/FeatureOfInterest";
        ResourcePath result = PathParser.parsePath("", path);

        ResourcePath expResult = new ResourcePath("", path);

        EntitySetPathElement espe = new EntitySetPathElement(EntityType.Thing, null);
        expResult.addPathElement(espe, false, false);
        EntityPathElement epe = new EntityPathElement(new LongId(1), EntityType.Thing, espe);
        expResult.addPathElement(epe, false, false);

        espe = new EntitySetPathElement(EntityType.Location, epe);
        expResult.addPathElement(espe, false, false);
        epe = new EntityPathElement(new LongId(2), EntityType.Location, espe);
        expResult.addPathElement(epe, false, false);

        espe = new EntitySetPathElement(EntityType.HistoricalLocation, epe);
        expResult.addPathElement(espe, false, false);
        epe = new EntityPathElement(new LongId(3), EntityType.HistoricalLocation, espe);
        expResult.addPathElement(epe, false, false);

        epe = new EntityPathElement(null, EntityType.Thing, epe);
        expResult.addPathElement(epe, false, false);

        espe = new EntitySetPathElement(EntityType.MultiDatastream, epe);
        expResult.addPathElement(espe, false, false);
        epe = new EntityPathElement(new LongId(5), EntityType.MultiDatastream, espe);
        expResult.addPathElement(epe, false, false);

        epe = new EntityPathElement(null, EntityType.Sensor, epe);
        expResult.addPathElement(epe, false, false);

        espe = new EntitySetPathElement(EntityType.MultiDatastream, epe);
        expResult.addPathElement(espe, false, false);
        epe = new EntityPathElement(new LongId(6), EntityType.MultiDatastream, espe);
        expResult.addPathElement(epe, false, false);

        espe = new EntitySetPathElement(EntityType.ObservedProperty, epe);
        expResult.addPathElement(espe, false, false);
        epe = new EntityPathElement(new LongId(7), EntityType.ObservedProperty, espe);
        expResult.addPathElement(epe, false, false);

        espe = new EntitySetPathElement(EntityType.MultiDatastream, epe);
        expResult.addPathElement(espe, false, false);
        epe = new EntityPathElement(new LongId(8), EntityType.MultiDatastream, espe);
        expResult.addPathElement(epe, false, false);

        espe = new EntitySetPathElement(EntityType.Observation, epe);
        expResult.addPathElement(espe, false, false);
        epe = new EntityPathElement(new LongId(9), EntityType.Observation, espe);
        expResult.addPathElement(epe, false, true);

        epe = new EntityPathElement(null, EntityType.FeatureOfInterest, epe);
        expResult.addPathElement(epe, true, false);

        assert (result.equals(expResult));
    }

    @Test
    public void testParsePath_deep4() {
        String path = "/FeaturesOfInterest(1)/Observations(2)/MultiDatastream/Thing/HistoricalLocations(3)/Locations(4)/Things(1)/properties/property1/subproperty2";
        ResourcePath result = PathParser.parsePath("", path);

        ResourcePath expResult = new ResourcePath("", path);

        EntitySetPathElement espe = new EntitySetPathElement(EntityType.FeatureOfInterest, null);
        expResult.addPathElement(espe, false, false);
        EntityPathElement epe = new EntityPathElement(new LongId(1), EntityType.FeatureOfInterest, espe);
        expResult.addPathElement(epe, false, false);

        espe = new EntitySetPathElement(EntityType.Observation, epe);
        expResult.addPathElement(espe, false, false);
        epe = new EntityPathElement(new LongId(2), EntityType.Observation, espe);
        expResult.addPathElement(epe, false, false);

        epe = new EntityPathElement(null, EntityType.MultiDatastream, epe);
        expResult.addPathElement(epe, false, false);

        epe = new EntityPathElement(null, EntityType.Thing, epe);
        expResult.addPathElement(epe, false, false);

        espe = new EntitySetPathElement(EntityType.HistoricalLocation, epe);
        expResult.addPathElement(espe, false, false);
        epe = new EntityPathElement(new LongId(3), EntityType.HistoricalLocation, espe);
        expResult.addPathElement(epe, false, false);

        espe = new EntitySetPathElement(EntityType.Location, epe);
        expResult.addPathElement(espe, false, false);
        epe = new EntityPathElement(new LongId(4), EntityType.Location, espe);
        expResult.addPathElement(epe, false, false);

        espe = new EntitySetPathElement(EntityType.Thing, epe);
        expResult.addPathElement(espe, false, false);
        epe = new EntityPathElement(new LongId(1), EntityType.Thing, espe);
        expResult.addPathElement(epe, true, true);
        PropertyPathElement ppe = new PropertyPathElement(EntityProperty.Properties, epe);
        expResult.addPathElement(ppe, false, false);
        CustomPropertyPathElement cppe = new CustomPropertyPathElement("property1", ppe);
        expResult.addPathElement(cppe, false, false);
        cppe = new CustomPropertyPathElement("subproperty2", cppe);
        expResult.addPathElement(cppe, false, false);

        assert (result.equals(expResult));
    }
}
