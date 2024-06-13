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
package de.fraunhofer.iosb.ilt.frostserver.modelextractor;

public class ForeignKeyData {

    final String myTableName;
    final String otherTableName;
    FieldData fieldMine;
    FieldData fieldTheirs;

    public ForeignKeyData(String myTableName, String otherTableName) {
        this.myTableName = myTableName;
        this.otherTableName = otherTableName;
    }

    public ForeignKeyData setFieldMine(FieldData fieldMine) {
        this.fieldMine = fieldMine;
        return this;
    }

    public ForeignKeyData setFieldTheirs(FieldData fieldTheirs) {
        this.fieldTheirs = fieldTheirs;
        return this;
    }

    @Override
    public String toString() {
        return myTableName + "." + fieldMine.name + " -> " + otherTableName + "." + fieldTheirs.name + "";
    }

}
