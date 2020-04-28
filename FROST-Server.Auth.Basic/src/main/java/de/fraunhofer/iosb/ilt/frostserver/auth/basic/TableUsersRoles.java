package de.fraunhofer.iosb.ilt.frostserver.auth.basic;

import java.util.Objects;
import org.jooq.Field;
import org.jooq.Name;
import org.jooq.Record2;
import org.jooq.TableField;
import org.jooq.impl.DSL;
import org.jooq.impl.TableImpl;

/**
 * The roles table for basic auth.
 */
public class TableUsersRoles extends TableImpl<Record2<String, String>> {

    private static final long serialVersionUID = 1713698749;
    private static final String TABLE_NAME = "USER_ROLES";

    /**
     * The reference instance of <code>public.USERS</code>
     */
    public static final TableUsersRoles USER_ROLES = new TableUsersRoles();

    /**
     * The column <code>public.USERS.USER_NAME</code>.
     */
    public final TableField<Record2<String, String>, String> userName = createField(DSL.name("USER_NAME"), org.jooq.impl.SQLDataType.CLOB);
    /**
     * The column <code>public.USERS.USER_PASS</code>.
     */
    public final TableField<Record2<String, String>, String> roleName = createField(DSL.name("ROLE_NAME"), org.jooq.impl.SQLDataType.CLOB);

    public TableUsersRoles() {
        this(DSL.name(TABLE_NAME), null);
    }

    protected TableUsersRoles(Name alias, TableUsersRoles aliased) {
        this(alias, aliased, null);
    }

    protected TableUsersRoles(Name alias, TableUsersRoles aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TableUsersRoles as(String alias) {
        return new TableUsersRoles(DSL.name(alias), this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TableUsersRoles as(Name alias) {
        return new TableUsersRoles(alias, this);
    }

    @Override
    public int hashCode() {
        int hash = super.hashCode();
        hash = 89 * hash + Objects.hashCode(this.userName);
        hash = 89 * hash + Objects.hashCode(this.roleName);
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
        final TableUsersRoles other = (TableUsersRoles) obj;
        if (!Objects.equals(this.userName, other.userName)) {
            return false;
        }
        if (!Objects.equals(this.roleName, other.roleName)) {
            return false;
        }
        return super.equals(obj);
    }

}
