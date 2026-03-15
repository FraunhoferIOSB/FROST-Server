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

import de.fraunhofer.iosb.ilt.frostserver.plugin.coremodelv2.swecommon.AbstractDataComponent;
import de.fraunhofer.iosb.ilt.frostserver.plugin.coremodelv2.swecommon.util.NillValue;
import java.util.List;
import java.util.Objects;

/**
 * SWE AbstractSimpleComponent class.
 *
 * @param <T> The type of the extending class.
 * @param <V> The type of the Value field.
 */
public abstract class AbstractSimpleComponent<T extends AbstractSimpleComponent<T, V>, V> extends AbstractDataComponent<T, V> {

    /**
     * Axis ID.
     *
     * A string that uniquely identifies one of the reference frame’s axes along
     * which the coordinate value is given.
     */
    private String axisID;

    /**
     * Reference Frame.
     *
     * The reference frame relative to which the coordinate value is given.
     * Commonly an EPSG identifier.
     */
    private String referenceFrame;

    /**
     * NilValues.
     *
     * A list (one or more) of NIL values.
     */
    private List<NillValue> nilValues;

    /**
     * Quality.
     *
     * 8.2.15. Quality Union. A union of the Quantity, QuantityRange, Category
     * and Text classes.
     */
    private Quality quality;

    public String getReferenceFrame() {
        return referenceFrame;
    }

    public T setReferenceFrame(String referenceFrame) {
        this.referenceFrame = referenceFrame;
        return self();
    }

    public String getAxisID() {
        return axisID;
    }

    public T setAxisID(String axisID) {
        this.axisID = axisID;
        return self();
    }

    public List<NillValue> getNilValues() {
        return nilValues;
    }

    public T setNilValues(List<NillValue> nilValues) {
        this.nilValues = nilValues;
        return self();
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 47 * hash + Objects.hashCode(this.axisID);
        hash = 47 * hash + Objects.hashCode(this.referenceFrame);
        hash = 47 * hash + Objects.hashCode(this.nilValues);
        hash = 47 * hash + Objects.hashCode(this.quality);
        hash = 47 * hash + super.hashCode();
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
        final AbstractSimpleComponent other = (AbstractSimpleComponent) obj;
        if (!Objects.equals(this.axisID, other.axisID)) {
            return false;
        }
        if (!Objects.equals(this.referenceFrame, other.referenceFrame)) {
            return false;
        }
        if (!Objects.equals(this.nilValues, other.nilValues)) {
            return false;
        }
        if (!Objects.equals(this.quality, other.quality)) {
            return false;
        }
        return super.equals(obj);
    }

}
