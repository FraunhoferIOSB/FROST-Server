/*
 * Copyright (C) 2021 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 */
package de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.fieldmapper;

import de.fraunhofer.iosb.ilt.frostserver.persistence.IdManager;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.PostgresPersistenceManager;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.relations.RelationOneToMany;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.StaMainTable;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.StaTableDynamic;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.PropertyFieldRegistry;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.property.Property;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.Table;
import org.jooq.TableField;

/**
 * The field mapper for Id fields.
 *
 * @author hylke
 */
public class FieldMapperOneToMany extends FieldMapperAbstract {

    /**
     * The name of the field in "my" table.
     */
    private String field;
    /**
     * The name of the other table we link to.
     */
    private String otherTable;
    /**
     * The field in the other table that is the key in the relation.
     */
    private String otherField;

    private int fieldIdx;
    private int fieldIdxOther;

    @Override
    public void registerField(PostgresPersistenceManager ppm, StaTableDynamic staTable, Property property) {
        final Name tableName = staTable.getQualifiedName();
        Table dbTable = ppm.getDbTable(tableName);
        fieldIdx = getOrRegisterField(field, dbTable, staTable);
    }

    @Override
    public <J extends Comparable<J>> void registerMapping(PostgresPersistenceManager ppm, StaTableDynamic<J> staTable, Property property) {
        StaMainTable staTableOther = (StaMainTable) ppm.getTableCollection().getTableForName(otherTable);
        Table dbTableOther = ppm.getDbTable(otherTable);
        fieldIdxOther = getOrRegisterField(otherField, dbTableOther, staTableOther);

        if (!(property instanceof NavigationPropertyMain)) {
            throw new IllegalArgumentException("Property must be a NavigationPropertyMain, got: " + property);
        }
        NavigationPropertyMain navigationProperty = (NavigationPropertyMain) property;
        PropertyFieldRegistry<J, StaTableDynamic<J>> pfReg = staTable.getPropertyFieldRegistry();
        IdManager idManager = ppm.getIdManager();
        pfReg.addEntry(navigationProperty, t -> t.field(fieldIdx), idManager);

        staTable.registerRelation(new RelationOneToMany<>(staTable, staTableOther, navigationProperty.getEntityType(), navigationProperty.isEntitySet())
                .setSourceFieldAccessor(t -> (TableField<Record, J>) t.field(fieldIdx))
                .setTargetFieldAccessor(t -> (TableField<Record, J>) t.field(fieldIdxOther))
        );
    }

    /**
     * The name of the field in "my" table.
     *
     * @return the field
     */
    public String getField() {
        return field;
    }

    /**
     * The name of the field in "my" table.
     *
     * @param field the field to set
     */
    public void setField(String field) {
        this.field = field;
    }

    /**
     * The name of the other table we link to.
     *
     * @return the otherTable
     */
    public String getOtherTable() {
        return otherTable;
    }

    /**
     * The name of the other table we link to.
     *
     * @param otherTable the otherTable to set
     */
    public void setOtherTable(String otherTable) {
        this.otherTable = otherTable;
    }

    /**
     * The field in the other table that is the key in the relation.
     *
     * @return the otherField
     */
    public String getOtherField() {
        return otherField;
    }

    /**
     * The field in the other table that is the key in the relation.
     *
     * @param otherField the otherField to set
     */
    public void setOtherField(String otherField) {
        this.otherField = otherField;
    }
}
