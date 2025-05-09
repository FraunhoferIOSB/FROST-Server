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
package de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq;

import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.bindings.PostGisGeometryBinding;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.fieldwrapper.FieldWrapper;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.fieldwrapper.SimpleFieldWrapper;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.constant.LineStringConstant;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.constant.PointConstant;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.constant.PolygonConstant;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import org.jooq.impl.DSL;

/**
 * Handles translation of OData expressions to MariaDB expressions.
 */
public class MariadbExpressionHandler extends ExpressionHandler {

    private static final String ST_GEOMFROMTEXT = "ST_GeomFromText(?)";

    public MariadbExpressionHandler(CoreSettings settings, QueryBuilder queryBuilder) {
        super(settings, queryBuilder);
    }

    @Override
    public FieldWrapper visit(LineStringConstant node) {
        return new SimpleFieldWrapper(DSL.field(ST_GEOMFROMTEXT, PostGisGeometryBinding.dataType(), node.getSource()));
    }

    @Override
    public FieldWrapper visit(PointConstant node) {
        return new SimpleFieldWrapper(DSL.field(ST_GEOMFROMTEXT, PostGisGeometryBinding.dataType(), node.getSource()));
    }

    @Override
    public FieldWrapper visit(PolygonConstant node) {
        return new SimpleFieldWrapper(DSL.field(ST_GEOMFROMTEXT, PostGisGeometryBinding.dataType(), node.getSource()));
    }

}
