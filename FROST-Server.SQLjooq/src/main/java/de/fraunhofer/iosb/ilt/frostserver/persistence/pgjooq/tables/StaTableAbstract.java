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
package de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables;

import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.model.core.EntitySet;
import de.fraunhofer.iosb.ilt.frostserver.model.core.EntitySetImpl;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.bindings.JsonBinding;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.bindings.JsonValue;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.fieldwrapper.JsonFieldFactory;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.relations.Relation;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.DataSize;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.PropertyFieldRegistry;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.PropertyFieldRegistry.PropertyFields;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.QueryState;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.Utils;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyCustomSelect;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyMain;
import java.util.HashMap;
import java.util.Map;
import org.jooq.Comment;
import org.jooq.Field;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.Schema;
import org.jooq.impl.DSL;
import org.jooq.impl.TableImpl;

/**
 *
 * @author hylke
 * @param <J> The type of the ID fields.
 * @param <E> The entity type for which the table holds data.
 * @param <T> The exact type of the implementing class.
 */
public abstract class StaTableAbstract<J extends Comparable, E extends Entity<E>, T extends StaMainTable<J, E, T>> extends TableImpl<Record> implements StaMainTable<J, E, T> {

    public static final String TYPE_JSONB = "\"pg_catalog\".\"jsonb\"";
    public static final String TYPE_GEOMETRY = "\"public\".\"geometry\"";

    private transient TableCollection<J> tables;
    private transient Map<String, Relation<J>> relations;
    protected PropertyFieldRegistry<J, E, T> pfReg;
    private transient EntityType entityType;

    protected StaTableAbstract() {
        this(DSL.name("THINGS"), null);
    }

    protected StaTableAbstract(Name alias, StaTableAbstract<J, E, T> aliased) {
        this(alias, aliased, null);
    }

    protected StaTableAbstract(Name alias, StaTableAbstract<J, E, T> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""));
    }

    public StaTableAbstract(Name name, Schema schema, StaTableAbstract<J, E, T> aliased, Field<?>[] parameters, Comment comment) {
        super(name, schema, aliased, parameters, comment);
        if (aliased != null) {
            setTables(aliased.getTables());
            pfReg = new PropertyFieldRegistry<>(getThis(), aliased.getPropertyFieldRegistry());
        }
    }

    protected void registerRelation(Relation<J> relation) {
        if (relations == null) {
            relations = new HashMap<>();
        }
        relations.put(relation.getName(), relation);
    }

    @Override
    public Relation findRelation(String name) {
        if (relations == null) {
            initRelations();
        }
        return relations.get(name);
    }

    @Override
    public PropertyFieldRegistry<J, E, T> getPropertyFieldRegistry() {
        if (pfReg == null) {
            pfReg = new PropertyFieldRegistry<>(getThis());
        }
        return pfReg;
    }

    @Override
    public E entityFromQuery(Record tuple, QueryState<J, E, T> state, DataSize dataSize) {
        E newEntity = newEntity();
        for (PropertyFields<T, E> sp : state.getSelectedProperties()) {
            sp.setter.setOn(state.getMainTable(), tuple, newEntity, dataSize);
        }
        return newEntity;
    }

    @Override
    public EntitySet<E> newSet() {
        if (entityType == null) {
            entityType = newEntity().getEntityType();
        }
        return new EntitySetImpl<>(entityType);
    }

    @Override
    public abstract StaTableAbstract<J, E, T> as(Name as);

    @Override
    public abstract StaTableAbstract<J, E, T> as(String alias);

    public final TableCollection<J> getTables() {
        return tables;
    }

    public final void setTables(TableCollection<J> tables) {
        this.tables = tables;
    }

    @Override
    public PropertyFields<T, E> handleEntityPropertyCustomSelect(final EntityPropertyCustomSelect epCustomSelect) {
        final EntityPropertyMain mainEntityProperty = epCustomSelect.getMainEntityProperty();
        if (mainEntityProperty == EntityPropertyMain.PROPERTIES) {
            PropertyFields<T, E> mainPropertyFields = pfReg.getSelectFieldsForProperty(mainEntityProperty);

            final Field mainField = mainPropertyFields.fields.values().iterator().next().get(getThis());
            JsonFieldFactory jsonFactory = jsonFieldFromPath(mainField, epCustomSelect);

            return propertyFieldForJsonField(jsonFactory, epCustomSelect);
        }
        return null;
    }

    protected static JsonFieldFactory jsonFieldFromPath(final Field mainField, final EntityPropertyCustomSelect epCustomSelect) {
        JsonFieldFactory jsonFactory = new JsonFieldFactory(mainField);
        for (String pathItem : epCustomSelect.getSubPath()) {
            jsonFactory.addToPath(pathItem);
        }
        return jsonFactory;
    }

    protected PropertyFields<T, E> propertyFieldForJsonField(final JsonFieldFactory jsonFactory, final EntityPropertyCustomSelect epCustomSelect) {
        final Field deepField = jsonFactory.build().getJsonExpression();
        PropertyFields<T, E> pfs = new PropertyFields<>(
                epCustomSelect,
                (tbl, tuple, entity, dataSize) -> {
                    final JsonValue jsonValue = JsonBinding.getConverterInstance().from(tuple.get(deepField));
                    dataSize.increase(jsonValue.getStringLength());
                    Object value = jsonValue.getValue(Utils.TYPE_OBJECT);
                    epCustomSelect.setOn(entity, value);
                });
        pfs.addField("1", t -> deepField);
        return pfs;
    }
}
