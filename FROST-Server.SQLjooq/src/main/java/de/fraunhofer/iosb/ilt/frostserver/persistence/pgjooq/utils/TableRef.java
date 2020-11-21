/*
 * Copyright (C) 2018 Fraunhofer IOSB.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library. If not, see <http://www.gnu.org/licenses/>.
 */
package de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils;

import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.StaMainTable;
import java.util.HashMap;
import java.util.Map;

/**
 * A class that keeps track of the latest table that was joined.
 *
 * @param <J> The type of the ID fields.
 */
public class TableRef<J extends Comparable> {

    private final EntityType type;
    private final StaMainTable<J, ?> table;
    private final Map<EntityType, TableRef<J>> joins = new HashMap<>();

    public TableRef(EntityType type, StaMainTable<J, ?> table) {
        this.type = type;
        this.table = table;
    }

    public EntityType getType() {
        return type;
    }

    public boolean isEmpty() {
        return type == null && table == null;
    }

    public StaMainTable<J, ?> getTable() {
        return table;
    }

    public void addJoin(EntityType link, TableRef<J> joinedTable) {
        joins.put(link, joinedTable);
    }

    public TableRef<J> getJoin(EntityType link) {
        return joins.get(link);
    }

    public void clearJoins() {
        joins.clear();
    }

    public TableRef<J> createJoin(String name, QueryState<J, ?> queryState) {
        return table.createJoin(name, queryState, this);
    }
}
