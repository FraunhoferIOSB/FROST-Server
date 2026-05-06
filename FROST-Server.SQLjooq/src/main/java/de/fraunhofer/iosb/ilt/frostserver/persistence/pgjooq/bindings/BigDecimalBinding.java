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
package de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.bindings;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Types;
import org.jooq.Binding;
import org.jooq.BindingGetResultSetContext;
import org.jooq.BindingGetSQLInputContext;
import org.jooq.BindingGetStatementContext;
import org.jooq.BindingRegisterContext;
import org.jooq.BindingSQLContext;
import org.jooq.BindingSetSQLOutputContext;
import org.jooq.BindingSetStatementContext;
import org.jooq.Converter;
import org.jooq.DataType;
import org.jooq.conf.ParamType;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;

/**
 * JOOQ binding for BigDecimal from NUMBER columns. By default, number columns
 * without scale and precision are turned into BigIntegers.
 */
public class BigDecimalBinding implements Binding<BigDecimal, BigDecimal> {

    private static final BigDecimalBinding INSTANCE = new BigDecimalBinding();
    private static final Converter<BigDecimal, BigDecimal> CONVERTER_INSTANCE = new Converter<BigDecimal, BigDecimal>() {
        @Override
        public BigDecimal from(BigDecimal databaseObject) {
            return databaseObject;
        }

        @Override
        public BigDecimal to(BigDecimal userObject) {
            return userObject;
        }

        @Override
        public Class<BigDecimal> fromType() {
            return BigDecimal.class;
        }

        @Override
        public Class<BigDecimal> toType() {
            return BigDecimal.class;
        }
    };
    private static final DataType<BigDecimal> DATA_TYPE = SQLDataType.NUMERIC.asConvertedDataType(INSTANCE);

    public static BigDecimalBinding instance() {
        return INSTANCE;
    }

    public static DataType<BigDecimal> dataType() {
        return DATA_TYPE;
    }

    private BigDecimalBinding() {
        // Use instance()
    }

    @Override
    public Converter<BigDecimal, BigDecimal> converter() {
        return CONVERTER_INSTANCE;
    }

    @Override
    public void sql(BindingSQLContext<BigDecimal> ctx) throws SQLException {
        if (ctx.render().paramType() == ParamType.INLINED) {
            ctx.render().visit(DSL.inline(ctx.convert(converter()).value()));
        } else {
            ctx.render().sql(ctx.variable());
        }
    }

    @Override
    public void register(BindingRegisterContext<BigDecimal> ctx) throws SQLException {
        ctx.statement().registerOutParameter(ctx.index(), Types.NUMERIC);
    }

    @Override
    public void set(BindingSetStatementContext<BigDecimal> ctx) throws SQLException {
        ctx.statement().setBigDecimal(ctx.index(), ctx.convert(converter()).value());
    }

    @Override
    public void get(BindingGetResultSetContext<BigDecimal> ctx) throws SQLException {
        ctx.convert(converter()).value(ctx.resultSet().getBigDecimal(ctx.index()));
    }

    @Override
    public void get(BindingGetStatementContext<BigDecimal> ctx) throws SQLException {
        ctx.convert(converter()).value(ctx.statement().getBigDecimal(ctx.index()));
    }

    @Override
    public void set(BindingSetSQLOutputContext<BigDecimal> ctx) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void get(BindingGetSQLInputContext<BigDecimal> ctx) throws SQLException {
        throw new SQLFeatureNotSupportedException();

    }
}
