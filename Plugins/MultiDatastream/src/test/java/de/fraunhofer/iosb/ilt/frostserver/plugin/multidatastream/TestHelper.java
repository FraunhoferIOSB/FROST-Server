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
import de.fraunhofer.iosb.ilt.frostserver.model.core.EntitySetImpl;
import de.fraunhofer.iosb.ilt.frostserver.model.core.IdLong;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.TimeInstant;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.TimeInterval;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.UnitOfMeasurement;
import de.fraunhofer.iosb.ilt.frostserver.plugin.coremodel.PluginCoreModel;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.property.Property;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.geojson.Feature;
import org.geojson.GeoJsonObject;
import org.geojson.LineString;
import org.geojson.LngLatAlt;
import org.geojson.Point;
import org.geojson.Polygon;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Helper class for testing JSON de-/serialization.
 *
 * @author jab
 */
public class TestHelper {

    private TestHelper() {
        // Utility class, not to be instantiated.
    }

    public static <T extends Number> Polygon getPolygon(int dimensions, T... values) {
        return new Polygon(getPointList(dimensions, values));
    }

    public static <T extends Number> Point getPoint(T... values) {
        if (values == null || values.length < 2 || values.length > 3) {
            throw new IllegalArgumentException("values must have a length of 2 or 3.");
        }
        if (values.length == 2) {
            return new Point(values[0].doubleValue(), values[1].doubleValue());
        }
        return new Point(values[0].doubleValue(), values[1].doubleValue(), values[2].doubleValue());
    }

    public static <T extends Number> List<LngLatAlt> getPointList(int dimensions, T... values) {
        if (dimensions < 2 || dimensions > 3) {
            throw new IllegalArgumentException("PointList requires 'demensions' to be 2 or 3.");
        }
        if (values == null || values.length % dimensions != 0) {
            throw new IllegalArgumentException("The number of values " + Arrays.toString(values) + " does not fit the dimensions " + dimensions);
        }
        List<LngLatAlt> points = new ArrayList<>(values.length / dimensions);
        for (int i = 0; i < values.length; i += dimensions) {
            if (dimensions == 2) {
                points.add(new LngLatAlt(values[i].doubleValue(), values[i + 1].doubleValue()));
            } else {
                points.add(new LngLatAlt(values[i].doubleValue(), values[i + 1].doubleValue(), values[i + 2].doubleValue()));
            }
        }
        return points;
    }

    public static <T extends Number> LineString getLine(T[]... values) {
        if (values == null || values.length < 2 || values.length > 3) {
            throw new IllegalArgumentException("values must have a length of 2 or 3.");
        }
        return new LineString(Arrays.asList(values).stream().map(x -> getPoint(x).getCoordinates()).toArray(size -> new LngLatAlt[size]));
    }

    public static <T extends Number> Feature getFeatureWithPoint(T... values) {
        return getFeatureWithGeometry(getPoint(values));
    }

    public static Feature getFeatureWithGeometry(GeoJsonObject geometry) {
        if (geometry == null) {
            throw new IllegalArgumentException("geometry must be non-null");
        }

        Feature result = new Feature();
        result.setGeometry(geometry);
        return result;
    }

    public static TimeInstant createTimeInstant(int year, int month, int day, int hour, int minute, int second, DateTimeZone timeZoneInput, DateTimeZone timeZoneOutput) {
        return TimeInstant.create(new DateTime(year, month, day, hour, minute, second, timeZoneInput).getMillis(), timeZoneOutput);
    }

    public static TimeInstant createTimeInstantUTC(int year, int month, int day, int hour, int minute, int second) {
        return createTimeInstant(year, month, day, hour, minute, second, DateTimeZone.UTC, DateTimeZone.UTC);
    }

    public static TimeInstant createTimeInstant(int year, int month, int day, int hour, int minute, int second) {
        return TimeInstant.create(new DateTime(year, month, day, hour, minute, second).getMillis());
    }

    public static TimeInterval createTimeInterval(int year1, int month1, int day1, int hour1, int minute1, int second1,
            int year2, int month2, int day2, int hour2, int minute2, int second2, DateTimeZone timeZone) {
        return TimeInterval.create(
                new DateTime(year1, month1, day1, hour1, minute1, second1, timeZone).getMillis(),
                new DateTime(year2, month2, day2, hour2, minute2, second2, timeZone).getMillis(),
                timeZone);
    }

    public static void generateDefaultValues(Map<Property, Object> propertyValues, PluginCoreModel pluginCoreModel, ModelRegistry modelRegistry) {
        EntityType etMultiDatastream = modelRegistry.getEntityTypeForName("MultiDatastream");
        EntityPropertyMain epMultiObservationDataTypes = etMultiDatastream.getEntityProperty("multiObservationDataTypes");
        EntityPropertyMain epUnitOfMeasurements = etMultiDatastream.getEntityProperty("unitOfMeasurements");
        NavigationPropertyMain npMultiDatastream = (NavigationPropertyMain) pluginCoreModel.etObservation.getNavigationProperty("MultiDatastream");
        NavigationPropertyMain npMultiDatastreams = (NavigationPropertyMain) pluginCoreModel.etThing.getNavigationProperty("MultiDatastreams");

        propertyValues.put(pluginCoreModel.epCreationTime, TimeInstant.now());
        propertyValues.put(pluginCoreModel.epDefinition, "MyDefinition");
        propertyValues.put(pluginCoreModel.epDescription, "My description");
        propertyValues.put(ModelRegistry.EP_ENCODINGTYPE, "My EncodingType");
        propertyValues.put(pluginCoreModel.epFeature, new Point(8, 42));
        propertyValues.put(pluginCoreModel.etThing.getPrimaryKey(), new IdLong(1));
        propertyValues.put(pluginCoreModel.epLocation, new Point(9, 43));
        propertyValues.put(pluginCoreModel.epMetadata, "my meta data");
        propertyValues.put(epMultiObservationDataTypes, Arrays.asList("Type 1", "Type 2"));
        propertyValues.put(pluginCoreModel.epName, "myName");
        propertyValues.put(pluginCoreModel.epObservationType, "my Type");
        propertyValues.put(pluginCoreModel.epObservedArea, new Polygon(new LngLatAlt(0, 0), new LngLatAlt(1, 0), new LngLatAlt(1, 1)));
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("key1", "value1");
        parameters.put("key2", 2);
        propertyValues.put(pluginCoreModel.epParameters, parameters);
        propertyValues.put(pluginCoreModel.epPhenomenonTime, TimeInstant.now());
        propertyValues.put(pluginCoreModel.epPhenomenonTimeDs, TimeInterval.parse("2014-03-02T13:00:00Z/2014-05-11T15:30:00Z"));
        propertyValues.put(ModelRegistry.EP_PROPERTIES, parameters);
        propertyValues.put(pluginCoreModel.epResult, 42);
        propertyValues.put(pluginCoreModel.epResultQuality, "myQuality");
        propertyValues.put(pluginCoreModel.epResultTime, TimeInstant.now());
        propertyValues.put(pluginCoreModel.epResultTimeDs, TimeInterval.parse("2014-03-01T13:00:00Z/2014-05-11T15:30:00Z"));
        propertyValues.put(ModelRegistry.EP_SELFLINK, "http://my.self/link");
        propertyValues.put(pluginCoreModel.epTime, TimeInstant.now());
        UnitOfMeasurement unit1 = new UnitOfMeasurement("unitName", "unitSymbol", "unitDefinition");
        UnitOfMeasurement unit2 = new UnitOfMeasurement("unitName2", "unitSymbol2", "unitDefinition2");
        propertyValues.put(pluginCoreModel.getEpUnitOfMeasurement(), unit1);
        propertyValues.put(epUnitOfMeasurements, Arrays.asList(unit1, unit2));
        propertyValues.put(pluginCoreModel.epValidTime, TimeInterval.parse("2014-03-01T13:00:00Z/2015-05-11T15:30:00Z"));

        int nextId = 100;
        final DefaultEntity datastream1 = new DefaultEntity(pluginCoreModel.etDatastream, new IdLong(nextId++));
        propertyValues.put(pluginCoreModel.npDatastreamObservation, datastream1);
        final DefaultEntity foi1 = new DefaultEntity(pluginCoreModel.etFeatureOfInterest, new IdLong(nextId++));
        propertyValues.put(pluginCoreModel.npFeatureOfInterestObservation, foi1);
        final DefaultEntity histLoc1 = new DefaultEntity(pluginCoreModel.etHistoricalLocation, new IdLong(nextId++));
        propertyValues.put(pluginCoreModel.npHistoricalLocationsLocation, histLoc1);
        propertyValues.put(pluginCoreModel.npHistoricalLocationsThing, histLoc1);
        final DefaultEntity location1 = new DefaultEntity(pluginCoreModel.etLocation, new IdLong(nextId++));
        propertyValues.put(pluginCoreModel.npLocationsHistLoc, location1);
        propertyValues.put(pluginCoreModel.npLocationsThing, location1);
        propertyValues.put(npMultiDatastream, new DefaultEntity(etMultiDatastream, new IdLong(nextId++)));
        final DefaultEntity observation1 = new DefaultEntity(pluginCoreModel.etObservation, new IdLong(nextId++));
        propertyValues.put(pluginCoreModel.npObservationsDatastream, observation1);
        propertyValues.put(pluginCoreModel.npObservationsFeature, observation1);
        final DefaultEntity obsProp1 = new DefaultEntity(pluginCoreModel.etObservedProperty, new IdLong(nextId++));
        propertyValues.put(pluginCoreModel.npObservedPropertyDatastream, obsProp1);
        final DefaultEntity sensor1 = new DefaultEntity(pluginCoreModel.etSensor, new IdLong(nextId++));
        propertyValues.put(pluginCoreModel.npSensorDatastream, sensor1);
        final DefaultEntity thing1 = new DefaultEntity(pluginCoreModel.etThing, new IdLong(nextId++));
        propertyValues.put(pluginCoreModel.npThingDatasteam, thing1);
        propertyValues.put(pluginCoreModel.npThingHistLoc, thing1);
        propertyValues.put(pluginCoreModel.npThingsLocation, thing1);

        EntitySetImpl datastreams = new EntitySetImpl(pluginCoreModel.etDatastream);
        datastreams.add(new DefaultEntity(pluginCoreModel.etDatastream, new IdLong(nextId++)));
        datastreams.add(new DefaultEntity(pluginCoreModel.etDatastream, new IdLong(nextId++)));
        propertyValues.put(pluginCoreModel.npDatastreamsObsProp, datastreams);
        propertyValues.put(pluginCoreModel.npDatastreamsSensor, datastreams);
        propertyValues.put(pluginCoreModel.npDatastreamsThing, datastreams);

        EntitySetImpl histLocations = new EntitySetImpl(pluginCoreModel.etHistoricalLocation);
        histLocations.add(new DefaultEntity(pluginCoreModel.etHistoricalLocation, new IdLong(nextId++)));
        histLocations.add(new DefaultEntity(pluginCoreModel.etHistoricalLocation, new IdLong(nextId++)));
        propertyValues.put(pluginCoreModel.npHistoricalLocationsLocation, histLocations);
        propertyValues.put(pluginCoreModel.npHistoricalLocationsThing, histLocations);

        EntitySetImpl locations = new EntitySetImpl(pluginCoreModel.etLocation);
        locations.add(new DefaultEntity(pluginCoreModel.etLocation, new IdLong(nextId++)));
        locations.add(new DefaultEntity(pluginCoreModel.etLocation, new IdLong(nextId++)));
        propertyValues.put(pluginCoreModel.npLocationsHistLoc, locations);
        propertyValues.put(pluginCoreModel.npLocationsThing, locations);

        EntitySetImpl multiDatastreams = new EntitySetImpl(etMultiDatastream);
        multiDatastreams.add(new DefaultEntity(etMultiDatastream, new IdLong(nextId++)));
        multiDatastreams.add(new DefaultEntity(etMultiDatastream, new IdLong(nextId++)));
        propertyValues.put(npMultiDatastreams, multiDatastreams);

        EntitySetImpl observations = new EntitySetImpl(pluginCoreModel.etObservation);
        observations.add(new DefaultEntity(pluginCoreModel.etObservation, new IdLong(nextId++)));
        observations.add(new DefaultEntity(pluginCoreModel.etObservation, new IdLong(nextId++)));
        propertyValues.put(pluginCoreModel.npObservationsDatastream, observations);
        propertyValues.put(pluginCoreModel.npObservationsFeature, observations);

        EntitySetImpl things = new EntitySetImpl(pluginCoreModel.etThing);
        things.add(new DefaultEntity(pluginCoreModel.etThing, new IdLong(nextId++)));
        things.add(new DefaultEntity(pluginCoreModel.etThing, new IdLong(nextId++)));
        propertyValues.put(pluginCoreModel.npThingsLocation, things);

        for (EntityType entityType : modelRegistry.getEntityTypes()) {
            for (EntityPropertyMain ep : entityType.getEntityProperties()) {
                assertTrue(propertyValues.containsKey(ep), "Missing value for " + ep);
            }
        }

        for (EntityType entityType : modelRegistry.getEntityTypes()) {
            for (NavigationPropertyMain np : entityType.getNavigationEntities()) {
                assertTrue(propertyValues.containsKey(np), "Missing value for " + np);
            }
        }
    }

}
