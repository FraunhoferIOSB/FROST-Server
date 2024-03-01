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

import static de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.QueryBuilder.ALIAS_PREFIX;

import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.model.core.EntitySet;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.EntitySetJooqCurser;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.JooqPersistenceManager;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.ResultBuilder;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.StaMainTable;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.PropertyFieldRegistry.ExpressionFactory;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.PropertyFieldRegistry.PropertyFields;
import java.util.HashSet;
import java.util.Set;
import org.jooq.Condition;
import org.jooq.Cursor;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Table;
import org.jooq.impl.DSL;

/**
 *
 * @author hylke
 * @param <T> The type of the main table for the query.
 */
public class QueryState<T extends StaMainTable<T>> {

    public static final String ALIAS_ROOT = "_ROOT";

    private final JooqPersistenceManager persistenceManager;
    private Set<PropertyFields<T>> selectedProperties;
    private Set<Field> sqlSelectFields;
    private final T mainTable;
    private final Field<?> sqlMainIdField;
    private Table sqlFrom;
    private Condition sqlWhere = DSL.noCondition();
    private Condition sqlSkipWhere;
    private Utils.SortSelectFields sqlSortFields;

    private boolean distinctRequired = false;
    private boolean isFilter = false;

    private int aliasNr = 0;
    private QueryState parent;
    private String staAlias;

    /**
     * The table reference for the main table of the request.
     */
    private final TableRef tableRef;

    public QueryState(T table, QueryState parent, String staAlias) {
        this(parent.getPersistenceManager(), table, null);
        this.parent = parent;
        this.staAlias = staAlias;
    }

    public QueryState(JooqPersistenceManager pm, T table, Set<PropertyFields<T>> sqlSelectFields) {
        this.persistenceManager = pm;
        this.selectedProperties = sqlSelectFields;
        sqlFrom = table;
        mainTable = table;
        sqlMainIdField = table.getId();
        tableRef = new TableRef(table);
        staAlias = ALIAS_ROOT;
    }

    public JooqPersistenceManager getPersistenceManager() {
        return persistenceManager;
    }

    public T getMainTable() {
        return mainTable;
    }

    public TableRef getTableRef() {
        return tableRef;
    }

    public Entity entityFromQuery(Record tuple, DataSize dataSize) {
        return mainTable.entityFromQuery(tuple, this, dataSize);
    }

    public EntitySet createSetFromRecords(Cursor<Record> tuples, ResultBuilder resultBuilder) {
        return new EntitySetJooqCurser(mainTable.getEntityType(), tuples, this, resultBuilder);
    }

    public QueryState findStateForAlias(String alias) {
        if (staAlias.equalsIgnoreCase(alias)) {
            return this;
        }
        if (parent == null) {
            return this;
        }
        return parent.findStateForAlias(alias);
    }

    public String getNextAlias() {
        if (parent == null) {
            return ALIAS_PREFIX + (++aliasNr);
        }
        return parent.getNextAlias();
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
     * @return the selectedProperties
     */
    public Set<Field> getSqlSelectFields() {
        if (sqlSelectFields == null) {
            sqlSelectFields = new HashSet<>();
            for (PropertyFields<?> sp : selectedProperties) {
                for (ExpressionFactory f : sp.fields.values()) {
                    sqlSelectFields.add(f.get(mainTable));
                }
            }
        }
        return sqlSelectFields;
    }

    public Set<PropertyFields<T>> getSelectedProperties() {
        return selectedProperties;
    }

    /**
     * @param sqlSelectFields the selectedProperties to set
     */
    public void setSelectedProperties(Set<PropertyFields<T>> sqlSelectFields) {
        this.selectedProperties = sqlSelectFields;
        this.sqlSelectFields = null;
    }

    /**
     * @return the sqlMainIdField
     */
    public Field<?> getSqlMainIdField() {
        return sqlMainIdField;
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
     * Get the SQL Where clause, including skipFilter conditions.
     *
     * @return the sql where clause with skipFilter.
     */
    public Condition getFullSqlWhere() {
        if (sqlSkipWhere == null) {
            return sqlWhere;
        }
        return sqlWhere.and(sqlSkipWhere);
    }

    /**
     * Get the SQL Where clause, excluding skipFilter conditions.
     *
     * @return the sql where clause without skipFilter.
     */
    public Condition getSqlWhere() {
        return sqlWhere;
    }

    /**
     * Set the SQL Where clause, excluding skipFilter conditions.
     *
     * @param sqlWhere the sql where clause without skipFilter.
     */
    public void setSqlWhere(Condition sqlWhere) {
        this.sqlWhere = sqlWhere;
    }

    /**
     * Get the SQL Where clause, for the skipFilter conditions.
     *
     * @return the sql where clause for the skipFilter.
     */
    public Condition getSqlSkipWhere() {
        return sqlSkipWhere == null ? DSL.noCondition() : sqlSkipWhere;
    }

    /**
     * Set the SQL Where clause, for the skipFilter conditions.
     *
     * @param sqlSkipWhere the sql where clause for the skipFilter.
     */
    public void setSqlSkipWhere(Condition sqlSkipWhere) {
        this.sqlSkipWhere = sqlSkipWhere;
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
