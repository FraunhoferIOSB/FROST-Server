package de.fraunhofer.iosb.ilt.statests.util;

import org.joda.time.format.ISODateTimeFormat;

/**
 * Provides list of sample values for each property of each entity for testing
 * $filter. These values are the same as the properties of one of the entities
 * that is created in pre-test.
 */
public class EntityPropertiesSampleValue {

    /**
     * Sample properties for Thing entity.
     */
    public static final String[] THING_PROPERTIES_Values = {"'thing 1'", "'thing 1'"};
    /**
     * Sample properties for Location entity.
     */
    public static final String[] LOCATION_PROPERTIES_Values = {"'location 2'", "'location 2'", "'application/vnd.geo+json'", "location"};
    /**
     * Sample properties for HistoricalLocation entity.
     */
    public static final Comparable[] HISTORICAL_LOCATION_PROPERTIES_Values = {ISODateTimeFormat.dateTime().parseDateTime("2015-10-14T21:30:00.104Z")};
    /**
     * Sample properties for Datastream entity.
     */
    public static final String[] DATASTREAM_PROPERTIES_Values = {"'datastream 1'", "'datastream 1'", "unitOfMeasurement", "'http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement'"};
    /**
     * Sample properties for Sensor entity.
     */
    public static final String[] SENSOR_PROPERTIES_Values = {"'sensor 1'", "'sensor 1'", "'application/pdf'", "'Light flux sensor'"};
    /**
     * Sample properties for ObservedProperty entity.
     */
    public static final String[] OBSERVED_PROPETY_PROPERTIES_Values = {"'Luminous Flux'", "'http://www.qudt.org/qudt/owl/1.0.0/quantity/Instances.html/LuminousFlux'", "'observedProperty 1'"};
    /**
     * Sample properties for Observation entity.
     */
    public static final Comparable[] OBSERVATION_PROPERTIES_Values = {
        ISODateTimeFormat.dateTime().parseDateTime("2015-03-02T00:00:00.000Z"),
        Integer.valueOf(2),
        ISODateTimeFormat.dateTime().parseDateTime("2015-03-02T00:00:00.000Z")};
    /**
     * Sample properties for FeatureOfInterest entity.
     */
    public static final String[] FEATURE_OF_INTEREST_PROPERTIES_Values = {"'Generated using location details: location 1'", "'Generated using location details: location 1'", "'application/vnd.geo+json'", "feature"};

    /**
     * Returning the sample property value for the properties[index] of the
     * entityType
     *
     * @param entityType The type of the entity in the format of EntityType enum
     * @param index      The index of the requested properties in the properties list
     *                   of the entityType
     * @return The sample value from the properties list of the given
     * "entityType" positioned in location "index" in the list
     */
    public static Comparable getPropertyValueFor(EntityType entityType, int index) {
        try {
            switch (entityType) {
                case THING:
                    return THING_PROPERTIES_Values[index];
                case LOCATION:
                    return LOCATION_PROPERTIES_Values[index];
                case FEATURE_OF_INTEREST:
                    return FEATURE_OF_INTEREST_PROPERTIES_Values[index];
                case OBSERVED_PROPERTY:
                    return OBSERVED_PROPETY_PROPERTIES_Values[index];
                case HISTORICAL_LOCATION:
                    return HISTORICAL_LOCATION_PROPERTIES_Values[index];
                case SENSOR:
                    return SENSOR_PROPERTIES_Values[index];
                case DATASTREAM:
                    return DATASTREAM_PROPERTIES_Values[index];
                case OBSERVATION:
                    return OBSERVATION_PROPERTIES_Values[index];
                default:
                    break;
            }
        } catch (IndexOutOfBoundsException e) {
            // No sample value for this property...
        }
        return null;
    }
}
