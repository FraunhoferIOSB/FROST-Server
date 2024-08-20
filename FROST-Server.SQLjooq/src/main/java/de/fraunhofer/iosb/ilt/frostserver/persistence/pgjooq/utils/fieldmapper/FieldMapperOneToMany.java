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
package de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.fieldmapper;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.fraunhofer.iosb.ilt.configurable.annotations.ConfigurableField;
import de.fraunhofer.iosb.ilt.configurable.editor.EditorString;
import de.fraunhofer.iosb.ilt.frostserver.model.loader.DefNavigationProperty;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.JooqPersistenceManager;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.relations.RelationOneToMany;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.StaMainTable;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.PropertyFieldRegistry;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain;
import java.util.Collections;
import java.util.Map;
import org.jooq.Name;
import org.jooq.Table;
import org.jooq.TableField;

/**
 * The field mapper for Id fields.
 *
 * @author hylke
 */
public class FieldMapperOneToMany extends FieldMapperAbstractNp {

    /**
     * The name of the field in "my" table.
     */
    @ConfigurableField(editor = EditorString.class,
            label = "Field", description = "The database field to use in 'my' table.")
    @EditorString.EdOptsString()
    private String field;

    /**
     * The name of the other table we link to.
     */
    @ConfigurableField(editor = EditorString.class,
            label = "OtherTable", description = "The name of the other table we link to.")
    @EditorString.EdOptsString()
    private String otherTable;

    /**
     * The field in the other table that is the key in the relation.
     */
    @ConfigurableField(editor = EditorString.class,
            label = "OtherField", description = "The field in the other table that is the key in the relation.")
    @EditorString.EdOptsString()
    private String otherField;

    @JsonIgnore
    private int fieldIdx;

    @Override
    public void registerField(JooqPersistenceManager ppm, StaMainTable staTable) {
        final Name tableName = staTable.getQualifiedName();
        final Table dbTable = ppm.getDbTable(tableName);
        fieldIdx = getOrRegisterField(field, dbTable, staTable);
    }

    @Override
    public <T extends StaMainTable<T>> void registerMapping(JooqPersistenceManager ppm, T staTable) {
        final StaMainTable staTableOther = (StaMainTable) ppm.getTableCollection().getTableForName(otherTable);
        final Table dbTableOther = ppm.getDbTable(otherTable);
        final NavigationPropertyMain navProp = getParent().getNavigationProperty();

        PropertyFieldRegistry<T> pfReg = staTable.getPropertyFieldRegistry();
        pfReg.addEntry(navProp, t -> t.field(fieldIdx));

        final int fieldIdxOther = getOrRegisterField(otherField, dbTableOther, staTableOther);
        staTable.registerRelation(new RelationOneToMany(navProp, staTable, staTableOther)
                .setSourceFieldAccessor(t -> (TableField) t.field(fieldIdx))
                .setTargetFieldAccessor(t -> (TableField) t.field(fieldIdxOther)));

        final DefNavigationProperty.Inverse inverse = getParent().getInverse();
        if (inverse != null) {
            final NavigationPropertyMain navPropInverse = getParent().getNavigationPropertyInverse();
            final PropertyFieldRegistry<T> pfRegOther = staTableOther.getPropertyFieldRegistry();
            pfRegOther.addEntry(navPropInverse, t -> t.field(fieldIdxOther));
            staTableOther.registerRelation(new RelationOneToMany(navPropInverse, staTableOther, staTable)
                    .setSourceFieldAccessor(t -> (TableField) t.field(fieldIdxOther))
                    .setTargetFieldAccessor(t -> (TableField) t.field(fieldIdx)));
        }
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
     * @return this.
     */
    public FieldMapperOneToMany setField(String field) {
        this.field = field;
        return this;
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
     * @return this.
     */
    public FieldMapperOneToMany setOtherTable(String otherTable) {
        this.otherTable = otherTable;
        return this;
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
     * @return this.
     */
    public FieldMapperOneToMany setOtherField(String otherField) {
        this.otherField = otherField;
        return this;
    }

    @Override
    public Map<String, String> getFieldTypes() {
        return Collections.emptyMap();
    }

}
