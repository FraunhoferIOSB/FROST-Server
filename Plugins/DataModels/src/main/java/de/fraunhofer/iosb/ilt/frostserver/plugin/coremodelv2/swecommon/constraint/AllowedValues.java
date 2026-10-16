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
package de.fraunhofer.iosb.ilt.frostserver.plugin.coremodelv2.swecommon.constraint;

import static de.fraunhofer.iosb.ilt.frostserver.util.StringHelper.isNullOrEmpty;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

/**
 * SWE Class AllowedValues constraint implementation.
 */
public class AllowedValues extends AbstractConstraint<AllowedValues, BigDecimal> {

    /**
     * Intervals
     *
     * The intervals that the values must fall in.
     */
    private List<List<BigDecimal>> intervals;

    /**
     * Significant Figures
     *
     * The number of significant figures.
     */
    private Integer significantFigures;

    public List<List<BigDecimal>> getIntervals() {
        return intervals;
    }

    public AllowedValues setIntervals(List<List<BigDecimal>> intervals) {
        this.intervals = intervals;
        return this;
    }

    public Integer getSignificantFigures() {
        return significantFigures;
    }

    public AllowedValues setSignificantFigures(Integer significantFigures) {
        this.significantFigures = significantFigures;
        return this;
    }

    public boolean isValid(BigDecimal input) {
        List<BigDecimal> values = getValues();
        if (isNullOrEmpty(values) && isNullOrEmpty(intervals) && significantFigures == 0) {
            // This constraint is empty
            return true;
        }
        if (values != null) {
            for (BigDecimal item : values) {
                if (item.compareTo(input) == 0) {
                    return true;
                }
            }
        }
        if (intervals != null) {
            for (List<BigDecimal> range : intervals) {
                if (range.get(0).compareTo(input) < 0 && range.get(1).compareTo(input) > 0) {
                    return true;
                }
            }
        }
        // TODO: validate significantFigues
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 17 * hash + Objects.hashCode(this.intervals);
        hash = 17 * hash + Objects.hashCode(this.significantFigures);
        hash = 17 * hash + super.hashCode();
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        final AllowedValues other = (AllowedValues) obj;
        if (!Objects.equals(this.intervals, other.intervals)) {
            return false;
        }
        return Objects.equals(this.significantFigures, other.significantFigures);
    }

    @Override
    protected AllowedValues self() {
        return this;
    }

}
