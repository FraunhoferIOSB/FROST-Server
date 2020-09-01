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

import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.relations.Relation;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.PropertyFieldRegistry;
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
 * @param <T> The exact type of the implementing class.
 */
public abstract class StaTableAbstract<J extends Comparable, T extends StaMainTable<J, T>> extends TableImpl<Record> implements StaMainTable<J, T> {

    public static final String TYPE_JSONB = "\"pg_catalog\".\"jsonb\"";
    public static final String TYPE_GEOMETRY = "\"public\".\"geometry\"";

    private transient TableCollection<J> tables;
    private transient Map<String, Relation<J>> relations;
    protected PropertyFieldRegistry<J, T> pfReg;

    protected StaTableAbstract() {
        this(DSL.name("THINGS"), null);
    }

    protected StaTableAbstract(Name alias, StaTableAbstract<J, T> aliased) {
        this(alias, aliased, null);
    }

    protected StaTableAbstract(Name alias, StaTableAbstract<J, T> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""));
    }

    public StaTableAbstract(Name name, Schema schema, StaTableAbstract<J, T> aliased, Field<?>[] parameters, Comment comment) {
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
    public PropertyFieldRegistry<J, T> getPropertyFieldRegistry() {
        if (pfReg == null) {
            initProperties();
        }
        return pfReg;
    }

    @Override
    public abstract StaTableAbstract<J, T> as(Name as);

    @Override
    public abstract StaTableAbstract<J, T> as(String alias);

    public final TableCollection<J> getTables() {
        return tables;
    }

    public final void setTables(TableCollection<J> tables) {
        this.tables = tables;
    }

}
