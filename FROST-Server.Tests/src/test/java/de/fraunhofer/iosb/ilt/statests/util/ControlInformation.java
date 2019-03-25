package de.fraunhofer.iosb.ilt.statests.util;

/**
 * SensorThings common control information
 */
public class ControlInformation {

    /**
     * The system-generated identifier of an entity. It is unique among the
     * entities of the same entity type in a SensorThings service.
     */
    public static final String ID = "@iot.id";
    /**
     * The absolute URL of an entity that is unique among all other entities.
     */
    public static final String SELF_LINK = "@iot.selfLink";
    /**
     * The relative URL that retrieves content of related entities.
     */
    public static final String NAVIGATION_LINK = "@iot.navigationLink";
}
