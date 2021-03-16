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
package de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables;

import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.factories.EntityFactories;
import org.jooq.DataType;
import org.jooq.Field;
import org.jooq.Name;
import org.jooq.impl.DSL;

/**
 *
 * @author hylke
 */
public final class StaTableDynamic<J extends Comparable> extends StaTableAbstract<J, StaTableDynamic<J>> {

    private final Name tableName;
    private final EntityType entityType;
    private int idFieldIdx;

    public StaTableDynamic(Name tableName, EntityType entityType, DataType<J> idType) {
        super(idType, tableName, null);
        this.tableName = tableName;
        this.entityType = entityType;
    }

    private StaTableDynamic(Name alias, StaTableDynamic<J> aliased, int idFieldIdx) {
        super(aliased.getIdType(), alias, aliased);
        this.tableName = aliased.getTableName();
        this.idFieldIdx = idFieldIdx;
        this.entityType = aliased.getEntityType();
    }

    public Name getTableName() {
        return tableName;
    }

    public final int registerIdField(String name, DataType<J> type) {
        return registerIdField(DSL.name(name), type);
    }

    public final int registerIdField(Name name, DataType<J> type) {
        idFieldIdx = registerField(name, type);
        return idFieldIdx;
    }

    @Override
    public StaTableDynamic<J> as(Name as) {
        return new StaTableDynamic<>(as, this, idFieldIdx).initCustomFields();
    }

    @Override
    public Field<J> getId() {
        return field(idFieldIdx, getIdType());
    }

    @Override
    public void initRelations() {
        // Not needed for this implementation, since it happens externally.
    }

    @Override
    public void initProperties(EntityFactories<J> entityFactories) {
        // Not needed for this implementation, since it happens externally.
    }

    @Override
    public EntityType getEntityType() {
        return entityType;
    }

    @Override
    public StaTableDynamic<J> getThis() {
        return this;
    }

}
