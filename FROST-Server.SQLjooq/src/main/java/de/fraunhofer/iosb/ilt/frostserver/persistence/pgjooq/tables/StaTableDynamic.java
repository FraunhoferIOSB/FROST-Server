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
package de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables;

import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.factories.EntityFactories;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.validator.SecurityTableWrapper;
import org.jooq.DataType;
import org.jooq.Field;
import org.jooq.Name;
import org.jooq.Table;
import org.jooq.impl.DSL;

/**
 *
 * @author hylke
 */
public final class StaTableDynamic extends StaTableAbstract<StaTableDynamic> {

    private final Name tableName;
    private final transient EntityType entityType;
    private int idFieldIdx;

    public StaTableDynamic(Name tableName, EntityType entityType, DataType<?> idType) {
        super(idType, tableName, null, null);
        this.tableName = tableName;
        this.entityType = entityType;
    }

    private StaTableDynamic(Name alias, StaTableDynamic aliased, int idFieldIdx) {
        this(alias, aliased, aliased, idFieldIdx);
    }

    private StaTableDynamic(Name alias, StaTableDynamic aliased, Table updatedSql, int idFieldIdx) {
        super(aliased.getIdType(), alias, aliased, updatedSql);
        this.tableName = aliased.getTableName();
        this.idFieldIdx = idFieldIdx;
        this.entityType = aliased.getEntityType();
    }

    public Name getTableName() {
        return tableName;
    }

    public final int registerIdField(String name, DataType<?> type) {
        return registerIdField(DSL.name(name), type);
    }

    public final int registerIdField(Name name, DataType<?> type) {
        idFieldIdx = registerField(name, type);
        return idFieldIdx;
    }

    @Override
    public StaTableDynamic as(Name as) {
        return new StaTableDynamic(as, this, idFieldIdx).initCustomFields();
    }

    @Override
    public StaTableDynamic asSecure(String name) {
        final SecurityTableWrapper securityWrapper = getSecurityWrapper();
        if (securityWrapper == null) {
            return as(name);
        }
        final Table wrappedTable = securityWrapper.wrap(this);
        return new StaTableDynamic(DSL.name(name), this, wrappedTable, idFieldIdx).initCustomFields();
    }

    @Override
    public Field<?> getId() {
        return field(idFieldIdx);
    }

    @Override
    public void initRelations() {
        // Not needed for this implementation, since it happens externally.
    }

    @Override
    public void initProperties(EntityFactories entityFactories) {
        // Not needed for this implementation, since it happens externally.
    }

    @Override
    public EntityType getEntityType() {
        return entityType;
    }

    @Override
    public StaTableDynamic getThis() {
        return this;
    }

}
