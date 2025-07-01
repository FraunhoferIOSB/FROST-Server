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
package de.fraunhofer.iosb.ilt.frostserver.plugin.coremodelv2.swecommon.simple;

import de.fraunhofer.iosb.ilt.frostserver.plugin.coremodelv2.swecommon.constraint.AllowedTimes;
import de.fraunhofer.iosb.ilt.frostserver.plugin.coremodelv2.swecommon.util.UnitOfMeasurement;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.databind.JsonNode;

/**
 * SWE Time class.
 */
public class Time extends AbstractSimpleComponent<Time, String> {

    private static final Logger LOGGER = LoggerFactory.getLogger(Time.class.getName());
    /**
     * Reference Time
     *
     * The “referenceTime” attribute is used to specify a different time origin
     * than the one sometimes implied by the “referenceFrame”. This is used to
     * express a time relative to an arbitrary epoch (i.e. different from the
     * origin of a well known reference frame). The new time origin specified by
     * “referenceTime” shall be expressed with respect to the reference frame
     * specified and is of type “DateTime”. This forces the definition of this
     * origin as a calendar date/time combination.
     */
    private String referenceTime;

    /**
     * Local Frame
     *
     * The optional “localFrame” attribute allows for the definition of a local
     * temporal frame of reference through the value of the component (i.e. we
     * are specifying a time origin), as opposed to the referenceFrame which
     * specifies that the value of the component is in reference to this frame.
     */
    private String localFrame;

    /**
     * Unit of Measurement
     *
     * The “uom” attribute is mandatory since time is a continuous property that
     * shall always be expressed in a well defined scale. The only units allowed
     * are obviously time units.
     *
     * Default: http://www.opengis.net/def/uom/ISO‐8601/0/Gregorian
     */
    private UnitOfMeasurement uom;

    /**
     * Constraint.
     *
     * The “constraint” attribute allows further restricting the range of
     * possible time values.
     */
    private AllowedTimes constraint;

    /**
     * Value
     *
     * The “value” attribute (or the corresponding value in out-of-band data) is
     * of type “TimePosition” and must match the constraint.
     */
    private String value;

    public String getReferenceTime() {
        return referenceTime;
    }

    public Time setReferenceTime(String referenceTime) {
        this.referenceTime = referenceTime;
        return this;
    }

    public String getLocalFrame() {
        return localFrame;
    }

    public Time setLocalFrame(String localFrame) {
        this.localFrame = localFrame;
        return this;
    }

    public UnitOfMeasurement getUom() {
        return uom;
    }

    public Time setUom(UnitOfMeasurement uom) {
        this.uom = uom;
        return this;
    }

    public AllowedTimes getConstraint() {
        return constraint;
    }

    public Time setConstraint(AllowedTimes constraint) {
        this.constraint = constraint;
        return this;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public Time setValue(String value) {
        this.value = value;
        return this;
    }

    @Override
    public boolean valueIsValid() {
        return validate(value);
    }

    @Override
    public boolean validate(Object input) {
        if (input == null) {
            return isOptional() || isSecret();
        }
        if (input instanceof JsonNode j) {
            return validate(j);
        }
        if (input instanceof String s) {
            return validate(s);
        }
        LOGGER.debug("Non-String value {} for Text.", input);
        return false;
    }

    @Override
    public boolean validate(JsonNode input) {
        if (input == null) {
            return isOptional() || isSecret();
        }
        if (!input.isTextual()) {
            LOGGER.debug("Non-Text value {} for Text.", input);
            return false;
        }
        return validate(input.asString());
    }

    public boolean validate(String input) {
        if (input == null) {
            return isOptional() || isSecret();
        }
        if (constraint == null) {
            return true;
        }
        return constraint.isValid(input, getUom());
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 19 * hash + Objects.hashCode(this.referenceTime);
        hash = 19 * hash + Objects.hashCode(this.localFrame);
        hash = 19 * hash + Objects.hashCode(this.uom);
        hash = 19 * hash + Objects.hashCode(this.constraint);
        hash = 19 * hash + Objects.hashCode(this.value);
        hash = 19 * hash + super.hashCode();
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Time other = (Time) obj;
        if (!Objects.equals(this.referenceTime, other.referenceTime)) {
            return false;
        }
        if (!Objects.equals(this.localFrame, other.localFrame)) {
            return false;
        }
        if (!Objects.equals(this.uom, other.uom)) {
            return false;
        }
        if (!Objects.equals(this.value, other.value)) {
            return false;
        }
        if (!Objects.equals(this.constraint, other.constraint)) {
            return false;
        }
        return super.equals(obj);
    }

    @Override
    protected Time self() {
        return this;
    }

}
