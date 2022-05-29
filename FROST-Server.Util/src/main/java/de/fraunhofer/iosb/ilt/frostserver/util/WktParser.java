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

import de.fraunhofer.iosb.ilt.frostserver.util.wktparser.Node;
import de.fraunhofer.iosb.ilt.frostserver.util.wktparser.Node.Visitor;
import de.fraunhofer.iosb.ilt.frostserver.util.wktparser.ParseException;
import de.fraunhofer.iosb.ilt.frostserver.util.wktparser.WParser;
import de.fraunhofer.iosb.ilt.frostserver.util.wktparser.nodes.COMMA;
import de.fraunhofer.iosb.ilt.frostserver.util.wktparser.nodes.Coords2;
import de.fraunhofer.iosb.ilt.frostserver.util.wktparser.nodes.Coords3;
import de.fraunhofer.iosb.ilt.frostserver.util.wktparser.nodes.DOUBLE;
import de.fraunhofer.iosb.ilt.frostserver.util.wktparser.nodes.GEOMETRYCOLLECTION;
import de.fraunhofer.iosb.ilt.frostserver.util.wktparser.nodes.LB;
import de.fraunhofer.iosb.ilt.frostserver.util.wktparser.nodes.LINESTRING;
import de.fraunhofer.iosb.ilt.frostserver.util.wktparser.nodes.LinearRing;
import de.fraunhofer.iosb.ilt.frostserver.util.wktparser.nodes.MULTILINESTRING;
import de.fraunhofer.iosb.ilt.frostserver.util.wktparser.nodes.MULTIPOINT;
import de.fraunhofer.iosb.ilt.frostserver.util.wktparser.nodes.MULTIPOLYGON;
import de.fraunhofer.iosb.ilt.frostserver.util.wktparser.nodes.POINT;
import de.fraunhofer.iosb.ilt.frostserver.util.wktparser.nodes.POLYGON;
import de.fraunhofer.iosb.ilt.frostserver.util.wktparser.nodes.POLYHEDRALSURFACE;
import de.fraunhofer.iosb.ilt.frostserver.util.wktparser.nodes.RB;
import de.fraunhofer.iosb.ilt.frostserver.util.wktparser.nodes.Start;
import de.fraunhofer.iosb.ilt.frostserver.util.wktparser.nodes.TIN;
import de.fraunhofer.iosb.ilt.frostserver.util.wktparser.nodes.TRIANGLE;
import de.fraunhofer.iosb.ilt.frostserver.util.wktparser.nodes.WktLineString;
import de.fraunhofer.iosb.ilt.frostserver.util.wktparser.nodes.WktMultiPoint;
import de.fraunhofer.iosb.ilt.frostserver.util.wktparser.nodes.WktPoint;
import de.fraunhofer.iosb.ilt.frostserver.util.wktparser.nodes.WktPolygon;
import de.fraunhofer.iosb.ilt.frostserver.util.wktparser.nodes.ZaM;
import de.fraunhofer.iosb.ilt.frostserver.util.wktparser.nodes.ZoM;
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
public class WktParser extends Visitor {

    private WktParser() {
        // Not for public consumption
    }

    private GeoJsonObject result;
    private LngLatAlt lastLngLatAlt;
    private List<LngLatAlt> lastLinearRing;
    private Polygon lastPolygon;
    private LineString lastLineString;
    private MultiPoint lastMultiPoint;
    private Point lastPoint;

    public static GeoJsonObject parseWkt(String wkt) {
        try {
            InputStream is = new ByteArrayInputStream(wkt.getBytes(StandardCharsets.UTF_8));
            WParser parser = new WParser(is);
            Start start = parser.Start();
            return new WktParser().visit(start);
        } catch (ParseException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    public GeoJsonObject visit(Start node) {
        if (node.getChildCount() != 2) {
            throw new IllegalArgumentException("Multiple items found in WKT, expected only one.");
        }
        visit(node.getChild(0));
        return result;
    }

    public void visit(WktPoint node) {
        if (lastPoint != null) {
            throw new IllegalArgumentException("Previously parsed Point not used.");
        }
        List<Node> children = new ArrayList<>();
        children.addAll(node.childrenOfType(Coords2.class));
        children.addAll(node.childrenOfType(Coords3.class));
        if (children.size() != 1) {
            throw new IllegalArgumentException("Multiple items (" + children.size() + ") found in Point, expected only one.");
        }
        lastLngLatAlt = null;
        visit(children.get(0));
        lastPoint = new Point(lastLngLatAlt);
        result = lastPoint;
    }

    public void visit(WktMultiPoint node) {
        if (lastMultiPoint != null) {
            throw new IllegalArgumentException("Previously parsed MultiPoint not used.");
        }
        lastMultiPoint = new MultiPoint();
        for (Node child : node.children()) {
            lastLngLatAlt = null;
            visit(child);
            if (lastLngLatAlt != null) {
                lastMultiPoint.add(lastLngLatAlt);
            }
        }
        result = lastMultiPoint;
    }

    public void visit(WktLineString node) {
        if (lastLineString != null) {
            throw new IllegalArgumentException("Previously parsed LineString not used.");
        }
        lastLineString = new LineString();
        for (Node child : node.children()) {
            lastLngLatAlt = null;
            visit(child);
            if (lastLngLatAlt != null) {
                lastLineString.add(lastLngLatAlt);
            }
        }
        result = lastLineString;
    }

    public void visit(WktPolygon node) {
        if (lastPolygon != null) {
            throw new IllegalArgumentException("Previously parsed Polygon not used.");
        }
        List<LinearRing> children = node.childrenOfType(LinearRing.class);
        lastPolygon = new Polygon();
        lastLinearRing = null;
        visit(children.get(0));
        lastPolygon.setExteriorRing(lastLinearRing);

        int childCount = children.size();
        for (int i = 1; i < childCount; i++) {
            lastLinearRing = null;
            visit(children.get(i));
            if (lastLinearRing != null) {
                lastPolygon.addInteriorRing(lastLinearRing);
            }
        }
        result = lastPolygon;
    }

    public void visit(LinearRing node) {
        if (lastLinearRing != null) {
            throw new IllegalArgumentException("Previously parsed coordinates not used.");
        }
        lastLinearRing = new ArrayList<>();
        for (Node child : node.children()) {
            lastLngLatAlt = null;
            visit(child);
            if (lastLngLatAlt != null) {
                lastLinearRing.add(lastLngLatAlt);
            }
        }
    }

    public void visit(Coords2 node) {
        if (lastLngLatAlt != null) {
            throw new IllegalArgumentException("Previously parsed LngLatAlt not used.");
        }
        List<DOUBLE> children = node.childrenOfType(DOUBLE.class);
        if (children.size() == 2) {
            lastLngLatAlt = new LngLatAlt(
                    Double.valueOf(children.get(0).getImage()),
                    Double.valueOf(children.get(1).getImage())
            );
        } else {
            throw new IllegalArgumentException("Point can not have " + children.size() + " coordinates.");
        }
    }

    public void visit(Coords3 node) {
        if (lastLngLatAlt != null) {
            throw new IllegalArgumentException("Previously parsed LngLatAlt not used.");
        }
        List<DOUBLE> children = node.childrenOfType(DOUBLE.class);
        if (children.size() == 3) {
            lastLngLatAlt = new LngLatAlt(
                    Double.valueOf(children.get(0).getImage()),
                    Double.valueOf(children.get(1).getImage()),
                    Double.valueOf(children.get(2).getImage())
            );
        } else {
            throw new IllegalArgumentException("Point can not have " + children.size() + " coordinates.");
        }
    }

    public void visit(LB node) {
        // Can be ignored while parsing.
    }

    public void visit(RB node) {
        // Can be ignored while parsing.
    }

    public void visit(COMMA node) {
        // Can be ignored while parsing.
    }

    public void visit(LINESTRING node) {
        // Can be ignored while parsing.
    }

    public void visit(GEOMETRYCOLLECTION node) {
        // Can be ignored while parsing.
    }

    public void visit(MULTILINESTRING node) {
        // Can be ignored while parsing.
    }

    public void visit(MULTIPOINT node) {
        // Can be ignored while parsing.
    }

    public void visit(MULTIPOLYGON node) {
        // Can be ignored while parsing.
    }

    public void visit(POINT node) {
        // Can be ignored while parsing.
    }

    public void visit(POLYGON node) {
        // Can be ignored while parsing.
    }

    public void visit(POLYHEDRALSURFACE node) {
        // Can be ignored while parsing.
    }

    public void visit(TIN node) {
        // Can be ignored while parsing.
    }

    public void visit(TRIANGLE node) {
        // Can be ignored while parsing.
    }

    public void visit(ZaM node) {
        // Can be ignored while parsing.
    }

    public void visit(ZoM node) {
        // Can be ignored while parsing.
    }

}
