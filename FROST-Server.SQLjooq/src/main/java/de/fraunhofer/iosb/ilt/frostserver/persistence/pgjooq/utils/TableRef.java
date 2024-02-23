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
package de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils;

import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.StaMainTable;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationProperty;
import java.util.HashMap;
import java.util.Map;
import org.jooq.Field;

/**
 * A class that keeps track of the latest table that was joined.
 */
public class TableRef {

    private final EntityType type;
    private final StaMainTable<?> table;
    private final Map<NavigationProperty, TableRef> joins = new HashMap<>();
    private Map<Field, Field> joinEquals;

    public TableRef(EntityType type, StaMainTable<?> table) {
        this.type = type;
        this.table = table;
    }

    public EntityType getType() {
        return type;
    }

    public boolean isEmpty() {
        return type == null && table == null;
    }

    public StaMainTable<?> getTable() {
        return table;
    }

    public void addJoin(NavigationProperty link, TableRef joinedTable) {
        joins.put(link, joinedTable);
    }

    public TableRef getJoin(NavigationProperty link) {
        return joins.get(link);
    }

    public void clearJoins() {
        joins.clear();
    }

    public TableRef createJoin(String name, QueryState<?> queryState) {
        return table.createJoin(name, queryState, this);
    }

    public TableRef setJoinEquals(Map<Field, Field> joinEquals) {
        this.joinEquals = joinEquals;
        return this;
    }

    public Field getJoinEqual(Field requested) {
        if (joinEquals == null) {
            return requested;
        }
        Field found = joinEquals.get(requested);
        if (found == null) {
            return requested;
        }
        return found;
    }

}
