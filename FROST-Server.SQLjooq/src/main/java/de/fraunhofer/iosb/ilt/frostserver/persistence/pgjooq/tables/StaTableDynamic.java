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
import de.fraunhofer.iosb.ilt.frostserver.model.core.PrimaryKey;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.JooqPersistenceManager;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.factories.EntityFactories;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.validator.SecurityTableWrapper;
import de.fraunhofer.iosb.ilt.frostserver.util.user.PrincipalExtended;
import java.util.ArrayList;
import java.util.List;
import org.jooq.Binding;
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
    private int[] pkFieldIdx;
    private List<Field> pkFields;

    public StaTableDynamic(Name tableName, EntityType entityType, DataType<?> idType) {
        super(idType, tableName, null, null);
        this.tableName = tableName;
        this.entityType = entityType;
        this.pkFieldIdx = new int[entityType.getPrimaryKey().size()];
    }

    private StaTableDynamic(Name alias, StaTableDynamic aliased, int[] idFieldIdx) {
        this(alias, aliased, aliased, idFieldIdx);
    }

    private StaTableDynamic(Name alias, StaTableDynamic aliased, Table updatedSql, int[] idFieldIdx) {
        super(aliased.getIdType(), alias, aliased, updatedSql);
        this.tableName = aliased.getTableName();
        this.pkFieldIdx = idFieldIdx;
        this.entityType = aliased.getEntityType();
    }

    public Name getTableName() {
        return tableName;
    }

    @Override
    public final int registerField(Name name, DataType type, Binding binding) {
        int fieldId = super.registerField(name, type, binding);
        PrimaryKey primaryKey = entityType.getPrimaryKey();
        for (int idx = 0; idx < primaryKey.size(); idx++) {
            if (primaryKey.getKeyProperty(idx).getName().equals(name.toString())) {
                pkFieldIdx[idx] = fieldId;
            }
        }
        return fieldId;
    }

    @Override
    public StaTableDynamic as(Name as) {
        return new StaTableDynamic(as, this, pkFieldIdx).initCustomFields();
    }

    @Override
    public StaTableDynamic asSecure(String name, JooqPersistenceManager pm) {
        final SecurityTableWrapper securityWrapper = getSecurityWrapper();
        if (securityWrapper == null || PrincipalExtended.getLocalPrincipal().isAdmin()) {
            return as(name);
        }
        final Table wrappedTable = securityWrapper.wrap(this, pm);
        return new StaTableDynamic(DSL.name(name), this, wrappedTable, pkFieldIdx).initCustomFields();
    }

    @Override
    public List<Field> getPkFields() {
        if (pkFields == null) {
            pkFields = new ArrayList<>();
            for (int fieldId : pkFieldIdx) {
                pkFields.add(field(fieldId));
            }
        }
        return pkFields;
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
