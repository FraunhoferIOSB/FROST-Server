/*
 * Copyright (C) 2017 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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
package de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq;

/**
 * A wrapper for the size so it can be passed to, and changed by, the Factories.
 */
public class DataSize {

    private long size;

    /**
     * @return the DataSize
     */
    public long getDataSize() {
        return size;
    }

    /**
     * @param dataSize the DataSize to set
     */
    public void setDataSize(long dataSize) {
        this.size = dataSize;
    }

    /**
     * Increases the size with the given amount.
     *
     * @param amount the amount to increase the size with.
     */
    public void increase(long amount) {
        size += amount;
    }

}
