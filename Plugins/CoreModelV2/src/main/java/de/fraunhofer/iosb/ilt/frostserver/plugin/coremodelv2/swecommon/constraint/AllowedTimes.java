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

import de.fraunhofer.iosb.ilt.frostserver.plugin.coremodelv2.swecommon.util.UnitOfMeasurement;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SWE Class AllowedTimes constraint implementation.
 */
public class AllowedTimes extends AbstractConstraint<AllowedTimes, String> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AllowedTimes.class.getName());

    /**
     * Intervals
     *
     * The intervals that the values must fall in.
     */
    private List<List<String>> intervals;

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 11 * hash + Objects.hashCode(this.intervals);
        hash = 11 * hash + Objects.hashCode(this.significantFigures);
        hash = 11 * hash + super.hashCode();
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
        final AllowedTimes other = (AllowedTimes) obj;
        if (!Objects.equals(this.intervals, other.intervals)) {
            return false;
        }
        return Objects.equals(this.significantFigures, other.significantFigures);
    }

    /**
     * Significant Figures
     *
     * The number of significant figures.
     */
    private Integer significantFigures;

    public List<List<String>> getIntervals() {
        return intervals;
    }

    public AllowedTimes setIntervals(List<List<String>> intervals) {
        this.intervals = intervals;
        return this;
    }

    public Integer getSignificantFigures() {
        return significantFigures;
    }

    public AllowedTimes setSignificantFigures(Integer significantFigures) {
        this.significantFigures = significantFigures;
        return this;
    }

    public boolean isValid(String input, UnitOfMeasurement uom) {
        LOGGER.warn("isValid not implemented yet on AllowedTimes, input: {}, {}", input, uom);
        return true;
    }

    @Override
    protected AllowedTimes self() {
        return this;
    }

}
