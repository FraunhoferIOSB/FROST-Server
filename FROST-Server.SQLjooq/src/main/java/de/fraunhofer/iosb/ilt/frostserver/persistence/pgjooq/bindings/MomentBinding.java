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
package de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.bindings;

import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Timestamp;
import java.sql.Types;
import net.time4j.Moment;
import net.time4j.sql.JDBCAdapter;
import org.jooq.Binding;
import org.jooq.BindingGetResultSetContext;
import org.jooq.BindingGetSQLInputContext;
import org.jooq.BindingGetStatementContext;
import org.jooq.BindingRegisterContext;
import org.jooq.BindingSQLContext;
import org.jooq.BindingSetSQLOutputContext;
import org.jooq.BindingSetStatementContext;
import org.jooq.Converter;
import org.jooq.conf.ParamType;
import org.jooq.impl.DSL;

/**
 *
 * @author hylke
 */
public class MomentBinding implements Binding<Timestamp, Moment> {

    private static final Converter<Timestamp, Moment> INSTANCE = new Converter<Timestamp, Moment>() {
        @Override
        public Moment from(Timestamp databaseObject) {
            if (databaseObject == null) {
                return null;
            }
            return JDBCAdapter.SQL_TIMESTAMP_WITH_ZONE.translate(databaseObject);
        }

        @Override
        public Timestamp to(Moment userObject) {
            if (userObject == null) {
                return null;
            }
            return JDBCAdapter.SQL_TIMESTAMP_WITH_ZONE.from(userObject);
        }

        @Override
        public Class<Timestamp> fromType() {
            return Timestamp.class;
        }

        @Override
        public Class<Moment> toType() {
            return Moment.class;
        }
    };

    @Override
    public Converter<Timestamp, Moment> converter() {
        return INSTANCE;
    }

    @Override
    public void sql(BindingSQLContext<Moment> ctx) throws SQLException {
        if (ctx.render().paramType() == ParamType.INLINED) {
            ctx.render().visit(DSL.inline(ctx.convert(converter()).value()));
        } else {
            ctx.render().sql(ctx.variable());
        }
    }

    // Registering VARCHAR types for JDBC CallableStatement OUT parameters
    @Override
    public void register(BindingRegisterContext<Moment> ctx) throws SQLException {
        ctx.statement().registerOutParameter(ctx.index(), Types.TIMESTAMP_WITH_TIMEZONE);
    }

    // Converting the JsonElement to a String value and setting that on a JDBC PreparedStatement
    @Override
    public void set(BindingSetStatementContext<Moment> ctx) throws SQLException {
        ctx.statement().setTimestamp(ctx.index(), ctx.convert(converter()).value());
    }

    // Getting a String value from a JDBC ResultSet and converting that to a JsonElement
    @Override
    public void get(BindingGetResultSetContext<Moment> ctx) throws SQLException {
        ctx.convert(converter()).value(ctx.resultSet().getTimestamp(ctx.index()));
    }

    // Getting a String value from a JDBC CallableStatement and converting that to a JsonElement
    @Override
    public void get(BindingGetStatementContext<Moment> ctx) throws SQLException {
        ctx.convert(converter()).value(ctx.statement().getTimestamp(ctx.index()));
    }

    // Setting a value on a JDBC SQLOutput (useful for Oracle OBJECT types)
    @Override
    public void set(BindingSetSQLOutputContext<Moment> ctx) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    // Getting a value from a JDBC SQLInput (useful for Oracle OBJECT types)
    @Override
    public void get(BindingGetSQLInputContext<Moment> ctx) throws SQLException {
        throw new SQLFeatureNotSupportedException();

    }
}
