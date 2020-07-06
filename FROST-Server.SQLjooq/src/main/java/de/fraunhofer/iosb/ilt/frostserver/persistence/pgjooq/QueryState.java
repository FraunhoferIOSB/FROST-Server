/*
 * Copyright (C) 2020 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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
package de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq;

import static de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.QueryBuilder.ALIAS_PREFIX;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.StaMainTable;
import java.util.Set;
import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Table;
import org.jooq.impl.DSL;

/**
 *
 * @author hylke
 * @param <J> The id class.
 */
public class QueryState<J extends Comparable> {

    private Set<Field> sqlSelectFields;
    private Field<J> sqlMainIdField;
    private Table sqlFrom;
    private Condition sqlWhere = DSL.trueCondition();
    private Utils.SortSelectFields sqlSortFields;

    private boolean distinctRequired = false;
    private boolean isFilter = false;

    private int aliasNr = 0;

    public void startQuery(StaMainTable<J> table, Set<Field> sqlSelectFields) {
        this.sqlSelectFields = sqlSelectFields;
        sqlFrom = table;
        sqlMainIdField = table.getId();
    }

    public String getNextAlias() {
        return ALIAS_PREFIX + (++aliasNr);
    }

    public boolean isSqlSortFieldsSet() {
        if (sqlSortFields == null) {
            return false;
        }
        return !sqlSortFields.getSqlSortFields().isEmpty();
    }

    public Utils.SortSelectFields getSqlSortFields() {
        if (sqlSortFields == null) {
            sqlSortFields = new Utils.SortSelectFields();
        }
        return sqlSortFields;
    }

    /**
     * @return the sqlSelectFields
     */
    public Set<Field> getSqlSelectFields() {
        return sqlSelectFields;
    }

    /**
     * @param sqlSelectFields the sqlSelectFields to set
     */
    public void setSqlSelectFields(Set<Field> sqlSelectFields) {
        this.sqlSelectFields = sqlSelectFields;
    }

    /**
     * @return the sqlMainIdField
     */
    public Field<J> getSqlMainIdField() {
        return sqlMainIdField;
    }

    /**
     * @param sqlMainIdField the sqlMainIdField to set
     */
    public void setSqlMainIdField(Field<J> sqlMainIdField) {
        this.sqlMainIdField = sqlMainIdField;
    }

    /**
     * @return the sqlFrom
     */
    public Table<Record> getSqlFrom() {
        return sqlFrom;
    }

    /**
     * @param sqlFrom the sqlFrom to set
     */
    public void setSqlFrom(Table sqlFrom) {
        this.sqlFrom = sqlFrom;
    }

    /**
     * @return the sqlWhere
     */
    public Condition getSqlWhere() {
        return sqlWhere;
    }

    /**
     * @param sqlWhere the sqlWhere to set
     */
    public void setSqlWhere(Condition sqlWhere) {
        this.sqlWhere = sqlWhere;
    }

    /**
     * @param sqlSortFields the sqlSortFields to set
     */
    public void setSqlSortFields(Utils.SortSelectFields sqlSortFields) {
        this.sqlSortFields = sqlSortFields;
    }

    /**
     * @return the distinctRequired
     */
    public boolean isDistinctRequired() {
        return distinctRequired;
    }

    /**
     * @param distinctRequired the distinctRequired to set
     */
    public void setDistinctRequired(boolean distinctRequired) {
        this.distinctRequired = distinctRequired;
    }

    /**
     * @return Flag indicating the query is a filter query.
     */
    public boolean isFilter() {
        return isFilter;
    }

    /**
     * @param isFilter Flag indicating the query is a filter query.
     */
    public void setFilter(boolean isFilter) {
        this.isFilter = isFilter;
    }

}
