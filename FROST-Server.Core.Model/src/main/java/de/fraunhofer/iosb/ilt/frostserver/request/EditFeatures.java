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
package de.fraunhofer.iosb.ilt.frostserver.request;

/**
 * How updates/inserts should behave.
 *
 * @see <a
 * href="https://docs.oasis-open.org/odata/odata/v4.01/odata-v4.01-part1-protocol.html#sec_UpdateRelatedEntitiesWhenUpdatinganE">OData
 * v4.01 Part 1</a>
 *
 */
public class EditFeatures {

    public static final EditFeatures NONE = new EditFeatures(false, false, false);

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

    public EditFeatures(boolean createNew, boolean remove, boolean deepUpdate) {
        this.createAndLinkNew = createNew;
        this.removeMissing = remove;
        this.deepUpdate = deepUpdate;
    }

}
