/*
 * Copyright (C) 2024 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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
package de.fraunhofer.iosb.ilt.statests.util;

import static de.fraunhofer.iosb.ilt.frostclient.models.CommonProperties.EP_DEFINITION;
import static de.fraunhofer.iosb.ilt.frostclient.models.CommonProperties.EP_DESCRIPTION;
import static de.fraunhofer.iosb.ilt.frostclient.models.CommonProperties.EP_ENCODINGTYPE;
import static de.fraunhofer.iosb.ilt.frostclient.models.CommonProperties.EP_NAME;
import static de.fraunhofer.iosb.ilt.frostclient.models.CommonProperties.EP_PROPERTIES;
import static de.fraunhofer.iosb.ilt.frostclient.models.CommonProperties.NAME_DATASTREAM;
import static de.fraunhofer.iosb.ilt.frostclient.models.CommonProperties.NAME_FEATURE;
import static de.fraunhofer.iosb.ilt.frostclient.models.CommonProperties.NAME_HISTORICALLOCATION;
import static de.fraunhofer.iosb.ilt.frostclient.models.CommonProperties.NAME_LOCATION;
import static de.fraunhofer.iosb.ilt.frostclient.models.CommonProperties.NAME_OBSERVATION;
import static de.fraunhofer.iosb.ilt.frostclient.models.CommonProperties.NAME_OBSERVEDPROPERTY;
import static de.fraunhofer.iosb.ilt.frostclient.models.CommonProperties.NAME_SENSOR;
import static de.fraunhofer.iosb.ilt.frostclient.models.CommonProperties.NAME_THING;
import static de.fraunhofer.iosb.ilt.frostclient.models.SensorThingsV11Sensing.EP_FEATURE;
import static de.fraunhofer.iosb.ilt.frostclient.models.SensorThingsV11Sensing.EP_LOCATION;
import static de.fraunhofer.iosb.ilt.frostclient.models.SensorThingsV11Sensing.EP_METADATA;
import static de.fraunhofer.iosb.ilt.frostclient.models.SensorThingsV11Sensing.EP_PHENOMENONTIME;
import static de.fraunhofer.iosb.ilt.frostclient.models.SensorThingsV11Sensing.EP_RESULT;
import static de.fraunhofer.iosb.ilt.frostclient.models.SensorThingsV11Sensing.EP_RESULTTIME;
import static de.fraunhofer.iosb.ilt.frostclient.models.SensorThingsV11Sensing.EP_TIME;
import static de.fraunhofer.iosb.ilt.frostclient.models.SensorThingsV11Sensing.EP_VALIDTIME;
import static de.fraunhofer.iosb.ilt.frostclient.models.SensorThingsV20Core.EP_PHENOMENONTIMEDS;
import static de.fraunhofer.iosb.ilt.frostclient.models.SensorThingsV20Core.EP_RESULTTIMEDS;
import static de.fraunhofer.iosb.ilt.frostclient.models.SensorThingsV20Core.EP_RESULTTYPE;
import static de.fraunhofer.iosb.ilt.frostclient.utils.CollectionsHelper.propertiesBuilder;

import de.fraunhofer.iosb.ilt.frostclient.SensorThingsService;
import de.fraunhofer.iosb.ilt.frostclient.exception.ServiceFailureException;
import de.fraunhofer.iosb.ilt.frostclient.model.Entity;
import de.fraunhofer.iosb.ilt.frostclient.models.SensorThingsV20Core;
import de.fraunhofer.iosb.ilt.frostclient.models.ext.TimeInstant;
import de.fraunhofer.iosb.ilt.frostclient.models.ext.TimeInterval;
import de.fraunhofer.iosb.ilt.frostclient.models.ext.TimeValue;
import de.fraunhofer.iosb.ilt.frostclient.models.swecommon.simple.Quantity;
import de.fraunhofer.iosb.ilt.frostclient.models.swecommon.util.UnitOfMeasurement;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import org.geojson.Point;

public class EntityHelper20 extends EntityHelperAbstract {

    private final SensorThingsV20Core sMdl;

    public EntityHelper20(SensorThingsService sSrvc) {
        super(sSrvc);
        sMdl = sSrvc.getModel(SensorThingsV20Core.class);
    }

    @Override
    public final List<String> changeEntity(Entity original) throws IllegalArgumentException {
        switch (original.getType().getName()) {
            case NAME_THING:
                original.setProperty(EP_NAME, "UpdatedName")
                        .setProperty(EP_DESCRIPTION, "Updated Description");
                return Arrays.asList(
                        EP_NAME.getName(),
                        EP_DESCRIPTION.getName());

            case NAME_SENSOR:
                original.setProperty(EP_NAME, "UpdatedName")
                        .setProperty(EP_DESCRIPTION, "Updated Description")
                        .setProperty(EP_ENCODINGTYPE, "Updated Encoding")
                        .setProperty(EP_METADATA, "Updated Metadata");
                return Arrays.asList(
                        EP_NAME.getName(),
                        EP_DESCRIPTION.getName(),
                        EP_ENCODINGTYPE.getName(),
                        EP_METADATA.getName());

            case NAME_LOCATION:
                original.setProperty(EP_NAME, "UpdatedName")
                        .setProperty(EP_DESCRIPTION, "Updated Description")
                        .setProperty(EP_ENCODINGTYPE, "Updated Encoding")
                        .setProperty(EP_LOCATION, "Updated Location");
                return Arrays.asList(
                        EP_NAME.getName(),
                        EP_DESCRIPTION.getName(),
                        EP_ENCODINGTYPE.getName(),
                        EP_LOCATION.getName());

            case NAME_OBSERVEDPROPERTY:
                original.setProperty(EP_NAME, "UpdatedName")
                        .setProperty(EP_DESCRIPTION, "Updated Description")
                        .setProperty(EP_DEFINITION, "Updated Definition");
                return Arrays.asList(
                        EP_NAME.getName(),
                        EP_DESCRIPTION.getName(),
                        EP_DEFINITION.getName());

            case NAME_FEATURE:
                original.setProperty(EP_NAME, "UpdatedName")
                        .setProperty(EP_DESCRIPTION, "Updated Description")
                        .setProperty(EP_ENCODINGTYPE, "Updated Encoding")
                        .setProperty(EP_FEATURE, "Updated Feature");
                return Arrays.asList(
                        EP_NAME.getName(),
                        EP_DESCRIPTION.getName(),
                        EP_ENCODINGTYPE.getName(),
                        EP_FEATURE.getName());

            case NAME_DATASTREAM:
                final UnitOfMeasurement uom = new UnitOfMeasurement().setLabel("Celcius").setSymbol("degC").setHref("http://qudt.org/vocab/unit#DegreeCelsius");
                final String definition = getCache(sMdl.etObservedProperty, 0).getSelfLink(false);
                original.setProperty(EP_NAME, "UpdatedName")
                        .setProperty(EP_DESCRIPTION, "Updated Description")
                        .setProperty(EP_RESULTTYPE, new Quantity().setDefinition(definition).setUom(uom));
                return Arrays.asList(
                        EP_NAME.getName(),
                        EP_DESCRIPTION.getName());

            case NAME_OBSERVATION:
                original.setProperty(EP_RESULT, 43)
                        .setProperty(EP_PHENOMENONTIME, TimeValue.create(Instant.parse("2016-03-01T00:40:00.000Z")))
                        .setProperty(EP_RESULTTIME, TimeInstant.parse("2016-03-01T00:40:00.000Z"))
                        .setProperty(EP_VALIDTIME, TimeInterval.create(
                                Instant.parse("2017-01-01T02:01:01+01:00"),
                                Instant.parse("2017-01-02T00:59:59+01:00")));
                return Arrays.asList(
                        EP_RESULT.getName(),
                        EP_PHENOMENONTIME.getName(),
                        EP_RESULTTIME.getName(),
                        EP_VALIDTIME.getName());

            case NAME_HISTORICALLOCATION:
                original.setProperty(EP_TIME, TimeInstant.parse("2016-03-01T00:40:00.000Z"));
                return Arrays.asList(
                        EP_TIME.getName());

            default:
                throw new IllegalArgumentException("Don't know how to patch a " + original.getType());

        }
    }

    @Override
    public Entity newObservation(Entity datastream) {
        var list = getCache(sMdl.etObservation);
        Entity obs = sMdl.newObservation(list.size())
                .setProperty(sMdl.npObservationDatastream, datastream)
                .setProperty(EP_PHENOMENONTIME, TimeValue.create(Instant.parse("2015-03-01T00:40:00.000Z")))
                .setProperty(EP_RESULTTIME, TimeInstant.parse("2015-03-01T00:40:00.000Z"))
                .setProperty(EP_VALIDTIME, TimeInterval.create(
                        Instant.parse("2016-01-01T02:01:01+01:00"),
                        Instant.parse("2016-01-02T00:59:59+01:00")))
                .setProperty(EP_PROPERTIES, propertiesBuilder()
                        .addItem("param1", "some value1")
                        .addItem("param2", "some value2")
                        .build());
        list.add(obs);
        return obs;
    }

    @Override
    public Entity createObservation(Entity datastream) throws ServiceFailureException {
        Entity obs = newObservation(datastream);
        sSrvc.create(obs);
        return obs;
    }

    @Override
    public Entity newObservation(Entity datastream, Entity feature) {
        return newObservation(datastream)
                .setProperty(sMdl.npObservationProximateFoi, feature);
    }

    @Override
    public Entity createObservation(Entity datastream, Entity feature) throws ServiceFailureException {
        Entity obs = newObservation(datastream, feature);
        sSrvc.create(obs);
        return obs;
    }

    @Override
    public Entity newThing() {
        final Entity newThing = sMdl.newThing("Test Thing", "This is a Test Thing");
        getCache(sMdl.etThing).add(newThing);
        return newThing;
    }

    @Override
    public Entity createThing() throws ServiceFailureException {
        Entity newThing = newThing();
        sSrvc.create(newThing);
        return newThing;
    }

    @Override
    public Entity newSensor() {
        final Entity newSensor = sMdl.newSensor(
                "Fuguro Barometer 1",
                "Our first Fuguro Barometer",
                "http://schema.org/description",
                "Barometer");
        getCache(sMdl.etSensor).add(newSensor);
        return newSensor;
    }

    @Override
    public Entity createSensor() throws ServiceFailureException {
        Entity newSensor = newSensor();
        sSrvc.create(newSensor);
        return newSensor;
    }

    @Override
    public Entity newLocation() {
        final Entity newLocation = sMdl.newLocation(
                "Rhine",
                "The river Thine",
                new Point(-32.01, 50.05));
        getCache(sMdl.etLocation).add(newLocation);
        return newLocation;
    }

    @Override
    public Entity newLocation(Entity thing) {
        return newLocation()
                .addNavigationEntity(sMdl.npLocationThings, thing);
    }

    @Override
    public Entity createLocation() throws ServiceFailureException {
        Entity newLocation = newLocation();
        sSrvc.create(newLocation);
        return newLocation;
    }

    @Override
    public Entity createLocation(Entity thing) throws ServiceFailureException {
        Entity newLocation = newLocation(thing);
        sSrvc.create(newLocation);
        return newLocation;
    }

    @Override
    public Entity newObservedProperty() {
        final Entity newObservedProperty = sMdl.newObservedProperty(
                "Dewpoint temperature",
                "http://dbpedia.org/page/Dew_point",
                "The dewpoint temperature is the temperature to which the air must be cooled, at constant pressure, for dew to form.");
        getCache(sMdl.etObservedProperty).add(newObservedProperty);
        return newObservedProperty;
    }

    @Override
    public Entity createObservedProperty() throws ServiceFailureException {
        Entity newObservedProperty = newObservedProperty();
        sSrvc.create(newObservedProperty);
        return newObservedProperty;
    }

    @Override
    public Entity newFeatureOfInterest(int idx) {
        final Entity newFeatureOfInterest = sMdl.newFeature(
                "Weather Station " + idx,
                "Weather station " + idx + " in my garden.",
                new Point(10.0 + 1 * idx, 10.0 + 1 * idx));
        getCache(sMdl.etFeature).add(newFeatureOfInterest);
        return newFeatureOfInterest;
    }

    @Override
    public Entity createFeatureOfInterest(int idx) throws ServiceFailureException {
        Entity newFeatureOfInterest = newFeatureOfInterest(idx);
        sSrvc.create(newFeatureOfInterest);
        return newFeatureOfInterest;
    }

    @Override
    public Entity newDatastream(Entity observedProperty, Entity sensor) {
        final Entity newDatastream = sMdl.newDatastream(
                "test datastream",
                "A datatream for testing",
                observedProperty.getSelfLink(false),
                new UnitOfMeasurement().setLabel("Celcius").setSymbol("degC").setHref("http://qudt.org/vocab/unit#DegreeCelsius"))
                .setProperty(EP_PHENOMENONTIMEDS, TimeInterval.parse("2014-03-01T13:00:00Z/2015-05-11T15:30:00Z"))
                .setProperty(EP_RESULTTIMEDS, TimeInterval.parse("2014-03-01T13:00:00Z/2015-05-11T15:30:00Z"))
                .setProperty(sMdl.npDatastreamSensor, sensor);
        getCache(sMdl.etDatastream).add(newDatastream);
        return newDatastream;
    }

    @Override
    public Entity newDatastream(Entity thing, Entity observedProperty, Entity sensor) {
        return newDatastream(observedProperty, sensor)
                .setProperty(sMdl.npDatastreamThing, thing);
    }

    @Override
    public Entity createDatastream(Entity thing, Entity observedProperty, Entity sensor) throws ServiceFailureException {
        final Entity newDatastream = newDatastream(thing, observedProperty, sensor);
        sSrvc.create(newDatastream);
        return newDatastream;
    }

    @Override
    public Entity newHistoricalLocation(Entity thing, Entity location) {
        final Entity newHistoricalLocation = sMdl.newHistoricalLocation()
                .setProperty(EP_TIME, TimeInstant.parse("2015-03-01T00:40:00.000Z"))
                .setProperty(sMdl.npHistlocThing, thing)
                .addNavigationEntity(sMdl.npHistlocLocations, location);
        getCache(sMdl.etHistoricalLocation).add(newHistoricalLocation);
        return newHistoricalLocation;
    }

    @Override
    public Entity createHistoricalLocation(Entity thing, Entity location) throws ServiceFailureException {
        Entity newHistoricalLocation = newHistoricalLocation(thing, location);
        sSrvc.create(newHistoricalLocation);
        return newHistoricalLocation;
    }

}
