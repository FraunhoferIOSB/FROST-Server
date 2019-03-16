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
package de.fraunhofer.iosb.ilt.sta.persistence.pgjooq;

import de.fraunhofer.iosb.ilt.sta.path.EntityType;
import java.util.EnumMap;
import java.util.Map;
import org.jooq.Field;
import org.jooq.Table;

/**
 * A class that keeps track of the latest table that was joined.
 *
 * @param <J> The type of the ID fields.
 */
public class TableRef<J extends Comparable> {

    private final EntityType type;
    private final Table table;
    private final Field<J> idField;
    private final Map<EntityType, TableRef<J>> joins = new EnumMap(EntityType.class);

    public TableRef(EntityType type, Table table, Field<J> idField) {
        this.type = type;
        this.table = table;
        this.idField = idField;
    }

    public EntityType getType() {
        return type;
    }

    public boolean isEmpty() {
        return type == null && table == null;
    }

    public Table getTable() {
        return table;
    }

    public Field<J> getIdField() {
        return idField;
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
}
