/*
 * Copyright (C) 2023 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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
package de.fraunhofer.iosb.ilt.frostserver.service;

/**
 * How updates should behave.
 *
 * {@linkplain https://docs.oasis-open.org/odata/odata/v4.01/odata-v4.01-part1-protocol.html#sec_UpdateRelatedEntitiesWhenUpdatinganE}
 *
 */
public class UpdateMode {

    public static final UpdateMode INSERT_ODATA_40 = new UpdateMode(true, false, false);
    public static final UpdateMode UPDATE_ODATA_40 = new UpdateMode(false, false, false);
    public static final UpdateMode INSERT_ODATA_401 = INSERT_ODATA_40;
    public static final UpdateMode UPDATE_ODATA_401 = new UpdateMode(true, true, true);
    public static final UpdateMode INSERT_STA_11 = INSERT_ODATA_40;
    public static final UpdateMode UPDATE_STA_11 = UPDATE_ODATA_40;

    /**
     * Should Updates only add nested entities to related sets, or can they also
     * remove relations.
     */
    public final boolean linkExisting = true;

    /**
     * Can an update create new related entities and link them.
     */
    public final boolean createAndLinkNew;

    /**
     * Does an update remove entities missing from the collection.
     */
    public final boolean removeMissing;

    /**
     * Can nested entities be edited.
     */
    public final boolean deepUpdate;

    public UpdateMode(boolean createNew, boolean remove, boolean deepUpdate) {
        this.createAndLinkNew = createNew;
        this.removeMissing = remove;
        this.deepUpdate = deepUpdate;
    }

}
