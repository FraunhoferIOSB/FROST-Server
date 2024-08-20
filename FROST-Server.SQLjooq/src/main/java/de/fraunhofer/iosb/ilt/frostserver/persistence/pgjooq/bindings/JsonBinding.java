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

import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.factories.EntityFactories;
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
 * JOOQ Binding for JsonValue.
 *
 * @author scf
 */
public class JsonBinding implements Binding<Object, JsonValue> {

    private static final JsonBinding INSTANCE = new JsonBinding();
    private static final Converter<Object, JsonValue> CONVERTER_INSTANCE = new Converter<Object, JsonValue>() {
        @Override
        public JsonValue from(Object databaseObject) {
            if (databaseObject == null) {
                return new JsonValue((String) null);
            }
            return new JsonValue(databaseObject.toString());
        }

        @Override
        public Object to(JsonValue userObject) {
            return EntityFactories.objectToJson(userObject);
        }

        @Override
        public Class<Object> fromType() {
            return Object.class;
        }

        @Override
        public Class<JsonValue> toType() {
            return JsonValue.class;
        }
    };
    private static final DataType<JsonValue> DATA_TYPE = SQLDataType.CLOB.asConvertedDataType(INSTANCE);

    public static Converter<Object, JsonValue> getConverterInstance() {
        return CONVERTER_INSTANCE;
    }

    public static JsonBinding instance() {
        return INSTANCE;
    }

    public static DataType<JsonValue> dataType() {
        return DATA_TYPE;
    }

    private JsonBinding() {
        // use instance()
    }

    @Override
    public Converter<Object, JsonValue> converter() {
        return CONVERTER_INSTANCE;
    }

    @Override
    public void sql(BindingSQLContext<JsonValue> ctx) throws SQLException {
        if (ctx.render().paramType() == ParamType.INLINED) {
            ctx.render().visit(DSL.inline(ctx.convert(converter()).value())).sql("::json");
        } else {
            ctx.render().sql("?::json");
        }
    }

    @Override
    public void register(BindingRegisterContext<JsonValue> ctx) throws SQLException {
        ctx.statement().registerOutParameter(ctx.index(), Types.VARCHAR);
    }

    @Override
    public void set(BindingSetStatementContext<JsonValue> ctx) throws SQLException {
        ctx.statement().setObject(ctx.index(), ctx.convert(converter()).value(), java.sql.Types.VARCHAR);
    }

    @Override
    public void set(BindingSetSQLOutputContext<JsonValue> ctx) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void get(BindingGetResultSetContext<JsonValue> ctx) throws SQLException {
        ctx.convert(converter()).value(ctx.resultSet().getString(ctx.index()));
    }

    @Override
    public void get(BindingGetStatementContext<JsonValue> ctx) throws SQLException {
        ctx.convert(converter()).value(ctx.statement().getString(ctx.index()));
    }

    @Override
    public void get(BindingGetSQLInputContext<JsonValue> ctx) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

}
