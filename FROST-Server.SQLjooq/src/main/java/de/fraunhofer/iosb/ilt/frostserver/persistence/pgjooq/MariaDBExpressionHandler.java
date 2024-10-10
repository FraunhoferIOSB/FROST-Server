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
import de.fraunhofer.iosb.ilt.frostserver.query.expression.constant.GeoJsonConstant;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.constant.LineStringConstant;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.constant.PointConstant;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.constant.PolygonConstant;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import org.geojson.GeoJsonObject;
import org.geolatte.geom.Geometry;
import org.geolatte.geom.codec.Wkt;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Hylke van der Schaaf
 */
public class MariaDBExpressionHandler extends ExpressionHandler {

    private static final String ST_GeomFromText = "ST_GeomFromText(?)";

    private static final Logger LOGGER = LoggerFactory.getLogger(MariaDBExpressionHandler.class);

    public MariaDBExpressionHandler(CoreSettings settings, QueryBuilder queryBuilder) {
        super(settings, queryBuilder);
    }

    @Override
    public FieldWrapper visit(LineStringConstant node) {
        Geometry geom = fromGeoJsonConstant(node);
        return new SimpleFieldWrapper(DSL.field(ST_GeomFromText, PostGisGeometryBinding.dataType(), geom.asText()));
    }

    @Override
    public FieldWrapper visit(PointConstant node) {
        Geometry geom = fromGeoJsonConstant(node);
        return new SimpleFieldWrapper(DSL.field(ST_GeomFromText, PostGisGeometryBinding.dataType(), geom.asText()));
    }

    @Override
    public FieldWrapper visit(PolygonConstant node) {
        Geometry geom = fromGeoJsonConstant(node);
        return new SimpleFieldWrapper(DSL.field(ST_GeomFromText, PostGisGeometryBinding.dataType(), geom.asText()));
    }

    public Geometry fromGeoJsonConstant(GeoJsonConstant<? extends GeoJsonObject> node) {
        if (node.getValue().getCrs() == null) {
            return Wkt.fromWkt(node.getSource());
        }
        return Wkt.fromWkt(node.getSource());
    }
}
