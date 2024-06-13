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
import de.fraunhofer.iosb.ilt.configurable.editor.EditorBoolean;
import de.fraunhofer.iosb.ilt.configurable.editor.EditorString;
import de.fraunhofer.iosb.ilt.frostserver.model.loader.DefNavigationProperty;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.JooqPersistenceManager;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.relations.RelationManyToManyOrdered;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.StaLinkTable;
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
public class FieldMapperManyToManyOrdered extends FieldMapperAbstractNp {

    /**
     * The name of the field in "my" table.
     */
    @ConfigurableField(editor = EditorString.class,
            label = "Field", description = "The database field to use in 'my' table.")
    @EditorString.EdOptsString()
    private String field;

    /**
     * Name of the link table.
     */
    @ConfigurableField(editor = EditorString.class,
            label = "LinkTable", description = "Name of the link table.")
    @EditorString.EdOptsString()
    private String linkTable;

    /**
     * Name of "my" field in the link table.
     */
    @ConfigurableField(editor = EditorString.class,
            label = "OurLinkField", description = "Name of 'my' field in the link table.")
    @EditorString.EdOptsString()
    private String linkOurField;

    /**
     * Name of the other table's field in the link table.
     */
    @ConfigurableField(editor = EditorString.class,
            label = "OtherLinkField", description = "Name of the other table's field in the link table.")
    @EditorString.EdOptsString()
    private String linkOtherField;

    /**
     * Name of the sorting field in the link table.
     */
    @ConfigurableField(editor = EditorString.class,
            label = "RankLinkField", description = "Name of the rank field in the link table.")
    @EditorString.EdOptsString()
    private String linkRankField;

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

    /**
     * Flag indicating duplicates should be removed when following the relation
     * over a navigationLink.
     */
    @ConfigurableField(editor = EditorBoolean.class,
            label = "Distinct", description = "Flag indicating duplicates should be removed when following the relation over a navigationLink.")
    @EditorBoolean.EdOptsBool()
    private boolean distinct = false;

    /**
     * Flag indicating duplicates should be removed when following the inverse
     * relation over a navigationLink.
     */
    @ConfigurableField(editor = EditorBoolean.class,
            label = "DistinctInverse", description = "Flag indicating duplicates should be removed when following the inverse relation over a navigationLink.")
    @EditorBoolean.EdOptsBool()
    private boolean distinctInverse = false;

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
        final int fieldIdxOther = getOrRegisterField(otherField, dbTableOther, staTableOther);

        final StaLinkTable staTableLink = ppm.getOrCreateLinkTable(linkTable);
        final Table dbTableLink = ppm.getDbTable(linkTable);
        final int fieldIdxLinkOur = getOrRegisterField(linkOurField, dbTableLink, staTableLink);
        final int fieldIdxLinkOther = getOrRegisterField(linkOtherField, dbTableLink, staTableLink);
        final int fieldIdxLinkRank = getOrRegisterField(linkRankField, dbTableLink, staTableLink);

        final NavigationPropertyMain navProp = getParent().getNavigationProperty();
        final PropertyFieldRegistry<T> pfReg = staTable.getPropertyFieldRegistry();
        pfReg.addEntry(navProp, t -> t.field(fieldIdx));

        staTable.registerRelation(new RelationManyToManyOrdered(navProp, staTable, staTableLink, staTableOther, true)
                .setAlwaysDistinct(distinct)
                .setOrderFieldAcc(t -> (TableField) t.field(fieldIdxLinkRank))
                .setSourceFieldAcc(t -> (TableField) t.field(fieldIdx))
                .setSourceLinkFieldAcc(t -> (TableField) t.field(fieldIdxLinkOur))
                .setTargetLinkFieldAcc(t -> (TableField) t.field(fieldIdxLinkOther))
                .setTargetFieldAcc(t -> (TableField) t.field(fieldIdxOther)));

        final DefNavigationProperty.Inverse inverse = getParent().getInverse();
        if (inverse != null) {
            final NavigationPropertyMain navPropInverse = getParent().getNavigationPropertyInverse();
            final PropertyFieldRegistry<?> pfRegOther = staTableOther.getPropertyFieldRegistry();
            pfRegOther.addEntry(navPropInverse, t -> t.field(fieldIdxOther));
            staTableOther.registerRelation(new RelationManyToManyOrdered(navPropInverse, staTableOther, staTableLink, staTable, false)
                    .setAlwaysDistinct(distinctInverse)
                    .setOrderFieldAcc(t -> (TableField) t.field(fieldIdxLinkRank))
                    .setSourceFieldAcc(t -> (TableField) t.field(fieldIdxOther))
                    .setSourceLinkFieldAcc(t -> (TableField) t.field(fieldIdxLinkOther))
                    .setTargetLinkFieldAcc(t -> (TableField) t.field(fieldIdxLinkOur))
                    .setTargetFieldAcc(t -> (TableField) t.field(fieldIdx)));
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
    public FieldMapperManyToManyOrdered setField(String field) {
        this.field = field;
        return this;
    }

    /**
     * Name of the link table.
     *
     * @return the linkTable
     */
    public String getLinkTable() {
        return linkTable;
    }

    /**
     * Name of the link table.
     *
     * @param linkTable the linkTable to set
     * @return this.
     */
    public FieldMapperManyToManyOrdered setLinkTable(String linkTable) {
        this.linkTable = linkTable;
        return this;
    }

    /**
     * Name of "my" field in the link table.
     *
     * @return the linkOurField
     */
    public String getLinkOurField() {
        return linkOurField;
    }

    /**
     * Name of "my" field in the link table.
     *
     * @param linkOurField the linkOurField to set
     * @return this.
     */
    public FieldMapperManyToManyOrdered setLinkOurField(String linkOurField) {
        this.linkOurField = linkOurField;
        return this;
    }

    /**
     * Name of the other table's field in the link table.
     *
     * @return the linkOtherField
     */
    public String getLinkOtherField() {
        return linkOtherField;
    }

    /**
     * Name of the other table's field in the link table.
     *
     * @param linkOtherField the linkOtherField to set
     * @return this.
     */
    public FieldMapperManyToManyOrdered setLinkOtherField(String linkOtherField) {
        this.linkOtherField = linkOtherField;
        return this;
    }

    /**
     * Name of the sorting field in the link table.
     *
     * @return the linkOrderField
     */
    public String getLinkRankField() {
        return linkRankField;
    }

    /**
     * Name of the sorting field in the link table.
     *
     * @param linkRankField the linkOrderField to set
     * @return this.
     */
    public FieldMapperManyToManyOrdered setLinkRankField(String linkRankField) {
        this.linkRankField = linkRankField;
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
    public FieldMapperManyToManyOrdered setOtherTable(String otherTable) {
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
    public FieldMapperManyToManyOrdered setOtherField(String otherField) {
        this.otherField = otherField;
        return this;
    }

    /**
     * Flag indicating duplicates should be removed when following the relation
     * over a navigationLink.
     *
     * @return the distinct
     */
    public boolean isDistinct() {
        return distinct;
    }

    /**
     * Flag indicating duplicates should be removed when following the relation
     * over a navigationLink.
     *
     * @param distinct the distinct to set
     * @return this.
     */
    public FieldMapperManyToManyOrdered setDistinct(boolean distinct) {
        this.distinct = distinct;
        return this;
    }

    /**
     * Flag indicating duplicates should be removed when following the inverse
     * relation over a navigationLink.
     *
     * @return the distinctInverse
     */
    public boolean isDistinctInverse() {
        return distinctInverse;
    }

    /**
     * Flag indicating duplicates should be removed when following the inverse
     * relation over a navigationLink.
     *
     * @param distinctInverse the distinctInverse to set
     * @return this.
     */
    public FieldMapperManyToManyOrdered setDistinctInverse(boolean distinctInverse) {
        this.distinctInverse = distinctInverse;
        return this;
    }

    @Override
    public Map<String, String> getFieldTypes() {
        return Collections.emptyMap();
    }

}
