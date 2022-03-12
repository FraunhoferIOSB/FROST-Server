/*
 * Copyright (C) 2022 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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
package de.fraunhofer.iosb.ilt.frostserver.util;

import de.fraunhofer.iosb.ilt.frostserver.util.wktparser.ASTCoords2;
import de.fraunhofer.iosb.ilt.frostserver.util.wktparser.ASTCoords3;
import de.fraunhofer.iosb.ilt.frostserver.util.wktparser.ASTLinearRing;
import de.fraunhofer.iosb.ilt.frostserver.util.wktparser.ASTNumber;
import de.fraunhofer.iosb.ilt.frostserver.util.wktparser.ASTStart;
import de.fraunhofer.iosb.ilt.frostserver.util.wktparser.ASTWktLineString;
import de.fraunhofer.iosb.ilt.frostserver.util.wktparser.ASTWktMultiPoint;
import de.fraunhofer.iosb.ilt.frostserver.util.wktparser.ASTWktPoint;
import de.fraunhofer.iosb.ilt.frostserver.util.wktparser.ASTWktPolygon;
import de.fraunhofer.iosb.ilt.frostserver.util.wktparser.Node;
import de.fraunhofer.iosb.ilt.frostserver.util.wktparser.ParseException;
import de.fraunhofer.iosb.ilt.frostserver.util.wktparser.Parser;
import de.fraunhofer.iosb.ilt.frostserver.util.wktparser.ParserVisitor;
import de.fraunhofer.iosb.ilt.frostserver.util.wktparser.SimpleNode;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.geojson.GeoJsonObject;
import org.geojson.LineString;
import org.geojson.LngLatAlt;
import org.geojson.MultiPoint;
import org.geojson.Point;
import org.geojson.Polygon;

/**
 *
 * @author hylke
 */
public class WktParser implements ParserVisitor {

    private WktParser() {
        // Not for public consumption
    }

    public static GeoJsonObject parseWkt(String wkt) {
        try {
            InputStream is = new ByteArrayInputStream(wkt.getBytes(StandardCharsets.UTF_8));
            Parser t = new Parser(is, StandardCharsets.UTF_8.name());
            ASTStart n = t.Start();
            n.dump("");
            return (GeoJsonObject) n.jjtAccept(new WktParser(), null);
        } catch (ParseException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    @Override
    public Object visit(ASTStart node, Object data) {
        if (node.jjtGetNumChildren() != 1) {
            throw new IllegalArgumentException("Multiple items found, expected only one.");
        }
        return node.jjtGetChild(0).jjtAccept(this, data);
    }

    @Override
    public Object visit(ASTWktPoint node, Object data) {
        if (node.jjtGetNumChildren() != 1) {
            throw new IllegalArgumentException("Multiple items found, expected only one.");
        }
        return new Point((LngLatAlt) node.jjtGetChild(0).jjtAccept(this, null));
    }

    @Override
    public Object visit(ASTWktMultiPoint node, Object data) {
        int childCount = node.jjtGetNumChildren();
        LngLatAlt[] points = new LngLatAlt[childCount];
        for (int i = 0; i < childCount; i++) {
            points[i] = (LngLatAlt) node.jjtGetChild(i).jjtAccept(this, null);
        }
        return new MultiPoint(points);
    }

    @Override
    public Object visit(ASTWktLineString node, Object data) {
        int childCount = node.jjtGetNumChildren();
        LngLatAlt[] points = new LngLatAlt[childCount];
        for (int i = 0; i < childCount; i++) {
            points[i] = (LngLatAlt) node.jjtGetChild(i).jjtAccept(this, null);
        }
        return new LineString(points);
    }

    @Override
    public Object visit(ASTWktPolygon node, Object data) {
        final Polygon polygon = new Polygon();
        polygon.setExteriorRing((List<LngLatAlt>) node.jjtGetChild(0).jjtAccept(this, null));
        int childCount = node.jjtGetNumChildren();
        for (int i = 1; i < childCount; i++) {
            polygon.addInteriorRing((List<LngLatAlt>) node.jjtGetChild(i).jjtAccept(this, null));
        }
        return polygon;
    }

    @Override
    public Object visit(ASTLinearRing node, Object data) {
        final List<LngLatAlt> coordinates = new ArrayList<>();
        int childCount = node.jjtGetNumChildren();
        for (int i = 0; i < childCount; i++) {
            coordinates.add((LngLatAlt) node.jjtGetChild(i).jjtAccept(this, null));
        }
        return coordinates;
    }

    @Override
    public Object visit(ASTCoords2 node, Object data) {
        int childCount = node.jjtGetNumChildren();
        if (childCount == 2) {
            return new LngLatAlt(
                    (Double) getChildOfType(node, 0, ASTNumber.class).jjtGetValue(),
                    (Double) getChildOfType(node, 1, ASTNumber.class).jjtGetValue()
            );
        }
        throw new IllegalArgumentException("Point can not have " + childCount + " coordinates.");
    }

    @Override
    public Object visit(ASTCoords3 node, Object data) {
        int childCount = node.jjtGetNumChildren();
        if (childCount == 3) {
            return new LngLatAlt(
                    (Double) getChildOfType(node, 0, ASTNumber.class).jjtGetValue(),
                    (Double) getChildOfType(node, 1, ASTNumber.class).jjtGetValue(),
                    (Double) getChildOfType(node, 2, ASTNumber.class).jjtGetValue()
            );
        }
        throw new IllegalArgumentException("Point can not have " + childCount + " coordinates.");
    }

    @Override
    public Object visit(ASTNumber node, Object data) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Object visit(SimpleNode node, Object data) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private static <T extends Node> T getChildOfType(SimpleNode parent, int index, Class<T> expectedType) {
        Node childNode = parent.jjtGetChild(index);
        if (!(expectedType.isAssignableFrom(childNode.getClass()))) {
            throw new IllegalArgumentException(parent.getClass().getName() + " expected to have child of type " + expectedType.getName());
        }
        return (T) childNode;
    }

}
