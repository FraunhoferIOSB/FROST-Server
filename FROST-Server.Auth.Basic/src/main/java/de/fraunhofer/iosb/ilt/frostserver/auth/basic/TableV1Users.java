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

import java.util.Objects;
import org.jooq.Field;
import org.jooq.Name;
import org.jooq.Record2;
import org.jooq.TableField;
import org.jooq.impl.DSL;
import org.jooq.impl.TableImpl;

/**
 * The users table for basic auth.
 */
public class TableV1Users extends TableImpl<Record2<String, String>> {

    private static final long serialVersionUID = 1713698749;
    private static final String TABLE_NAME = "USERS";

    /**
     * The reference instance of <code>public.USERS</code>
     */
    public static final TableV1Users USERS = new TableV1Users();

    /**
     * The column <code>public.USERS.USER_NAME</code>.
     */
    public final TableField<Record2<String, String>, String> userName = createField(DSL.name("USER_NAME"), org.jooq.impl.SQLDataType.CLOB);
    /**
     * The column <code>public.USERS.USER_PASS</code>.
     */
    public final TableField<Record2<String, String>, String> userPass = createField(DSL.name("USER_PASS"), org.jooq.impl.SQLDataType.CLOB);

    public TableV1Users() {
        this(DSL.name(TABLE_NAME), null);
    }

    protected TableV1Users(Name alias, TableV1Users aliased) {
        this(alias, aliased, null);
    }

    protected TableV1Users(Name alias, TableV1Users aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TableV1Users as(String alias) {
        return new TableV1Users(DSL.name(alias), this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TableV1Users as(Name alias) {
        return new TableV1Users(alias, this);
    }

    @Override
    public int hashCode() {
        int hash = super.hashCode();
        hash = 37 * hash + Objects.hashCode(this.userName);
        hash = 37 * hash + Objects.hashCode(this.userPass);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TableV1Users other = (TableV1Users) obj;
        if (!Objects.equals(this.userName, other.userName)) {
            return false;
        }
        if (!Objects.equals(this.userPass, other.userPass)) {
            return false;
        }
        return super.equals(obj);
    }

}
