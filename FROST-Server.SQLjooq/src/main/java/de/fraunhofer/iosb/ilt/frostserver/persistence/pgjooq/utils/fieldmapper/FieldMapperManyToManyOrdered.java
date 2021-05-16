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

import de.fraunhofer.iosb.ilt.frostserver.model.loader.DefNavigationProperty;
import de.fraunhofer.iosb.ilt.frostserver.persistence.IdManager;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.PostgresPersistenceManager;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.relations.RelationManyToManyOrdered;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.StaLinkTable;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.StaMainTable;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.PropertyFieldRegistry;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain;
import org.jooq.Name;
import org.jooq.Table;
import org.jooq.TableField;

/**
 * The field mapper for Id fields.
 *
 * @author hylke
 */
public class FieldMapperManyToManyOrdered extends FieldMapperAbstract {

    /**
     * The name of the field in "my" table.
     */
    private String field;
    /**
     * Name of the link table.
     */
    private String linkTable;
    /**
     * Name of "my" field in the link table.
     */
    private String linkOurField;
    /**
     * Name of the other table's field in the link table.
     */
    private String linkOtherField;
    /**
     * Name of the sorting field in the link table.
     */
    private String linkRankField;
    /**
     * The name of the other table we link to.
     */
    private String otherTable;
    /**
     * The field in the other table that is the key in the relation.
     */
    private String otherField;
    /**
     * Flag indicating duplicates should be removed when following the relation
     * over a navigationLink.
     */
    private boolean distinct = false;
    /**
     * Flag indicating duplicates should be removed when following the inverse
     * relation over a navigationLink.
     */
    private boolean distinctInverse = false;

    private int fieldIdx;
    private int fieldIdxLinkOur;
    private int fieldIdxLinkOther;
    private int fieldIdxLinkRank;
    private int fieldIdxOther;

    private DefNavigationProperty parent;

    @Override
    public void setParent(DefNavigationProperty parent) {
        this.parent = parent;
    }

    @Override
    public void registerField(PostgresPersistenceManager ppm, StaMainTable staTable) {
        final Name tableName = staTable.getQualifiedName();
        final Table dbTable = ppm.getDbTable(tableName);
        fieldIdx = getOrRegisterField(field, dbTable, staTable);
    }

    @Override
    public <J extends Comparable<J>, T extends StaMainTable<J, T>> void registerMapping(PostgresPersistenceManager ppm, T staTable) {
        final StaMainTable staTableOther = (StaMainTable) ppm.getTableCollection().getTableForName(otherTable);
        final Table dbTableOther = ppm.getDbTable(otherTable);
        fieldIdxOther = getOrRegisterField(otherField, dbTableOther, staTableOther);

        final StaLinkTable staTableLink = ppm.getOrCreateLinkTable(linkTable);
        final Table dbTableLink = ppm.getDbTable(linkTable);
        fieldIdxLinkOur = getOrRegisterField(linkOurField, dbTableLink, staTableLink);
        fieldIdxLinkOther = getOrRegisterField(linkOtherField, dbTableLink, staTableLink);
        fieldIdxLinkRank = getOrRegisterField(linkRankField, dbTableLink, staTableLink);

        final NavigationPropertyMain navProp = parent.getNavigationProperty();
        final PropertyFieldRegistry<J, T> pfReg = staTable.getPropertyFieldRegistry();
        final IdManager idManager = ppm.getIdManager();
        pfReg.addEntry(navProp, t -> t.field(fieldIdx), idManager);

        staTable.registerRelation(new RelationManyToManyOrdered(navProp, staTable, staTableLink, staTableOther)
                .setAlwaysDistinct(distinct)
                .setOrderFieldAcc(t -> (TableField) t.field(fieldIdxLinkRank))
                .setSourceFieldAcc(t -> (TableField) t.field(fieldIdx))
                .setSourceLinkFieldAcc(t -> (TableField) t.field(fieldIdxLinkOur))
                .setTargetLinkFieldAcc(t -> (TableField) t.field(fieldIdxLinkOther))
                .setTargetFieldAcc(t -> (TableField) t.field(fieldIdxOther))
        );

        final DefNavigationProperty.Inverse inverse = parent.getInverse();
        if (inverse != null) {
            final NavigationPropertyMain navPropInverse = parent.getNavigationPropertyInverse();
            final PropertyFieldRegistry<?, ?> pfRegOther = staTableOther.getPropertyFieldRegistry();
            pfRegOther.addEntry(navPropInverse, t -> t.field(fieldIdxOther), idManager);
            staTableOther.registerRelation(new RelationManyToManyOrdered(navPropInverse, staTableOther, staTableLink, staTable)
                    .setAlwaysDistinct(distinctInverse)
                    .setOrderFieldAcc(t -> (TableField) t.field(fieldIdxLinkRank))
                    .setSourceFieldAcc(t -> (TableField) t.field(fieldIdxOther))
                    .setSourceLinkFieldAcc(t -> (TableField) t.field(fieldIdxLinkOther))
                    .setTargetLinkFieldAcc(t -> (TableField) t.field(fieldIdxLinkOur))
                    .setTargetFieldAcc(t -> (TableField) t.field(fieldIdx))
            );
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
     */
    public void setField(String field) {
        this.field = field;
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
     */
    public void setLinkTable(String linkTable) {
        this.linkTable = linkTable;
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
     */
    public void setLinkOurField(String linkOurField) {
        this.linkOurField = linkOurField;
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
     */
    public void setLinkOtherField(String linkOtherField) {
        this.linkOtherField = linkOtherField;
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
     * @param linkOrderField the linkOrderField to set
     */
    public void setLinkRankField(String linkRankField) {
        this.linkRankField = linkRankField;
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
     */
    public void setDistinct(boolean distinct) {
        this.distinct = distinct;
    }

}
