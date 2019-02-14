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
import org.jooq.Field;
import org.jooq.Table;
import org.jooq.impl.TableImpl;

/**
 * A class that keeps track of the latest table that was joined.
 *
 * @param <J> The type of the ID fields.
 */
public class TableRef<J extends Comparable> {

    private EntityType type;
    private Table table;
    private Field<J> idField;

    public TableRef() {
    }

    public TableRef(TableRef<J> source) {
        type = source.type;
        table = source.table;
        idField = source.idField;
    }

    public EntityType getType() {
        return type;
    }

    public void setType(EntityType type) {
        this.type = type;
    }

    public void clear() {
        type = null;
        table = null;
        idField = null;
    }

    public TableRef copy() {
        return new TableRef(this);
    }

    public boolean isEmpty() {
        return type == null && table == null;
    }

    public Table getTable() {
        return table;
    }

    public void setTable(TableImpl<?> table) {
        this.table = table;
    }

    public Field<J> getIdField() {
        return idField;
    }

    public void setIdField(Field<J> idField) {
        this.idField = idField;
    }

}
