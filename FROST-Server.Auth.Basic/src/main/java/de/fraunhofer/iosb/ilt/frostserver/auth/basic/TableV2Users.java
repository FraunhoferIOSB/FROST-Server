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
package de.fraunhofer.iosb.ilt.frostserver.auth.basic;

import org.jooq.Field;
import org.jooq.Name;
import org.jooq.Record2;
import org.jooq.TableField;
import org.jooq.impl.DSL;
import org.jooq.impl.TableImpl;

/**
 * The users table for basic auth.
 */
public class TableV2Users extends TableImpl<Record2<String, String>> {

    private static final long serialVersionUID = 1713698749;
    private static final String TABLE_NAME = "users";

    /**
     * The reference instance of <code>public.USERS</code>
     */
    public static final TableV2Users USERS = new TableV2Users();

    /**
     * The column <code>public.USERS.USER_NAME</code>.
     */
    public final TableField<Record2<String, String>, String> userName = createField(DSL.name("user_name"), org.jooq.impl.SQLDataType.CLOB);
    /**
     * The column <code>public.USERS.USER_PASS</code>.
     */
    public final TableField<Record2<String, String>, String> userPass = createField(DSL.name("user_pass"), org.jooq.impl.SQLDataType.CLOB);

    public TableV2Users() {
        this(DSL.name(TABLE_NAME), null);
    }

    protected TableV2Users(Name alias, TableV2Users aliased) {
        this(alias, aliased, null);
    }

    protected TableV2Users(Name alias, TableV2Users aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TableV2Users as(String alias) {
        return new TableV2Users(DSL.name(alias), this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TableV2Users as(Name alias) {
        return new TableV2Users(alias, this);
    }

    @Override
    public boolean equals(Object that) {
        // No extra checks needed, since fields must not be included.
        return super.equals(that);
    }

    @Override
    public int hashCode() {
        // No extra checks needed, since fields must not be included.
        return super.hashCode();
    }

}
