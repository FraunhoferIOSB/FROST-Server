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
package de.fraunhofer.iosb.ilt.sta;

/**
 *
 * @author scf
 */
public class Settings {

    /**
     * The maximum number of items to return in a collection. TODO: move to some
     * configurable location
     */
    public static final int DEFAULT_MAX_TOP = 100;
    /**
     * The default value of the count toggle. TODO: move to some configurable
     * location
     */
    public static final boolean DEFAULT_COUNT = true;
    /**
     * The default top to use when no specific top is set.
     */
    private int topDefault = DEFAULT_MAX_TOP;
    /**
     * The maximum allowed top.
     */
    private int topMax = DEFAULT_MAX_TOP;
    /**
     * The default count to use when no specific count is set.
     */
    private boolean countDefault = DEFAULT_COUNT;

    /**
     * The default top to use when no specific top is set.
     *
     * @return the topDefault
     */
    public int getTopDefault() {
        return topDefault;
    }

    /**
     * The default top to use when no specific top is set.
     *
     * @param topDefault the topDefault to set
     */
    public void setTopDefault(int topDefault) {
        this.topDefault = topDefault;
    }

    /**
     * The maximum allowed top.
     *
     * @return the topMax
     */
    public int getTopMax() {
        return topMax;
    }

    /**
     * The maximum allowed top.
     *
     * @param topMax the topMax to set
     */
    public void setTopMax(int topMax) {
        this.topMax = topMax;
    }

    /**
     * The default count to use when no specific count is set.
     *
     * @return the countDefault
     */
    public boolean isCountDefault() {
        return countDefault;
    }

    /**
     * The default count to use when no specific count is set.
     *
     * @param countDefault the countDefault to set
     */
    public void setCountDefault(boolean countDefault) {
        this.countDefault = countDefault;
    }

}
