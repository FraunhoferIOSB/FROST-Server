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
package de.fraunhofer.iosb.ilt.frostserver.modelextractor;

import org.jooq.Field;

public class FieldData {

    final String name;
    boolean pk;
    boolean fk;
    String typeName;
    String castTypeName;
    String comment;

    public static FieldData from(Field field, boolean pk) {
        return new FieldData(field.getName()).setPk(pk).setTypeName(field.getDataType().getTypeName()).setCastTypeName(field.getDataType().getCastTypeName()).setComment(field.getComment());
    }

    public FieldData(String name) {
        this.name = name;
    }

    public FieldData setPk(boolean pk) {
        this.pk = pk;
        return this;
    }

    public FieldData setFk(boolean fk) {
        this.fk = fk;
        return this;
    }

    public FieldData setTypeName(String typeName) {
        this.typeName = typeName;
        return this;
    }

    public FieldData setCastTypeName(String castTypeName) {
        this.castTypeName = castTypeName;
        return this;
    }

    public FieldData setComment(String comment) {
        this.comment = comment;
        return this;
    }

    @Override
    public String toString() {
        return name + "(" + typeName + " / " + castTypeName + ") " + (pk ? "PK" : "") + (fk ? "FK" : "");
    }

}
