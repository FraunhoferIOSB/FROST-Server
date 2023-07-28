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
import java.sql.Types;
import org.geolatte.geom.Geometry;
import org.jooq.Binding;
import org.jooq.BindingGetResultSetContext;
import org.jooq.BindingGetSQLInputContext;
import org.jooq.BindingGetStatementContext;
import org.jooq.BindingRegisterContext;
import org.jooq.BindingSQLContext;
import org.jooq.BindingSetSQLOutputContext;
import org.jooq.BindingSetStatementContext;
import org.jooq.Converter;

/**
 *
 * @author scf
 */
public class PostGisGeometryBinding implements Binding<Object, Geometry> {

    private static final Converter<Object, Geometry> CONVERTER_INSTANCE = new Converter<Object, Geometry>() {
        @Override
        public Geometry from(Object databaseObject) {
            // We do not actually support requesting a bare Geometry object from the database.
            return null;
        }

        @Override
        public Object to(Geometry userObject) {
            return userObject.asText();
        }

        @Override
        public Class<Object> fromType() {
            return Object.class;
        }

        @Override
        public Class<Geometry> toType() {
            return Geometry.class;
        }
    };

    @Override
    public Converter<Object, Geometry> converter() {
        return CONVERTER_INSTANCE;
    }

    @Override
    public void sql(BindingSQLContext<Geometry> ctx) throws SQLException {
        ctx.render().sql("ST_GeomFromEWKT(?)");
    }

    @Override
    public void register(BindingRegisterContext<Geometry> ctx) throws SQLException {
        ctx.statement().registerOutParameter(ctx.index(), Types.VARCHAR);
    }

    @Override
    public void set(BindingSetStatementContext<Geometry> ctx) throws SQLException {
        ctx.statement().setObject(ctx.index(), ctx.convert(converter()).value(), Types.VARCHAR);
    }

    @Override
    public void set(BindingSetSQLOutputContext<Geometry> ctx) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void get(BindingGetResultSetContext<Geometry> ctx) throws SQLException {
        ctx.convert(converter()).value(ctx.resultSet().getBytes(ctx.index()));
    }

    @Override
    public void get(BindingGetStatementContext<Geometry> ctx) throws SQLException {
        ctx.convert(converter()).value(ctx.statement().getBytes(ctx.index()));
    }

    @Override
    public void get(BindingGetSQLInputContext<Geometry> ctx) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

}
