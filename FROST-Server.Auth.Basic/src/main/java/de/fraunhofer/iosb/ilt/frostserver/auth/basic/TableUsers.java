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
public class TableUsers extends TableImpl<Record2> {

    private static final long serialVersionUID = 1713698749;
    private static final String TABLE_NAME = "USERS";

    /**
     * The reference instance of <code>public.USERS</code>
     */
    public static final TableUsers USERS = new TableUsers();

    /**
     * The column <code>public.USERS.USER_NAME</code>.
     */
    public final TableField<Record2, String> userName = createField("USER_NAME", org.jooq.impl.SQLDataType.CLOB);
    /**
     * The column <code>public.USERS.USER_PASS</code>.
     */
    public final TableField<Record2, String> userPass = createField("USER_PASS", org.jooq.impl.SQLDataType.CLOB);

    public TableUsers() {
        this(DSL.name(TABLE_NAME), null);
    }

    protected TableUsers(Name alias, TableUsers aliased) {
        this(alias, aliased, null);
    }

    protected TableUsers(Name alias, TableUsers aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TableUsers as(String alias) {
        return new TableUsers(DSL.name(alias), this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TableUsers as(Name alias) {
        return new TableUsers(alias, this);
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
        final TableUsers other = (TableUsers) obj;
        if (!Objects.equals(this.userName, other.userName)) {
            return false;
        }
        if (!Objects.equals(this.userPass, other.userPass)) {
            return false;
        }
        return super.equals(obj);
    }

}
