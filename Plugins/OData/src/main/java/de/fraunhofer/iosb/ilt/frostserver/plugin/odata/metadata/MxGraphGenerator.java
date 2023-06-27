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
package de.fraunhofer.iosb.ilt.frostserver.plugin.odata.metadata;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import de.fraunhofer.iosb.ilt.frostserver.json.serialize.MomentSerializer;
import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.ModelRegistry;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationProperty;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.time4j.Moment;
import org.apache.commons.lang3.StringUtils;

/**
 * Generates an MxGraph from the data model.
 */
public class MxGraphGenerator {

    private static final String STYLE_LIST = "swimlane;fontStyle=0;childLayout=stackLayout;horizontal=1;startSize=30;horizontalStack=0;resizeParent=1;resizeParentMax=0;resizeLast=0;collapsible=1;marginBottom=0;whiteSpace=wrap;html=1;fillColor=#98D095;strokeColor=#82b366;swimlaneFillColor=#E3F7E2;";
    private static final String STYLE_LIST_ITEM = "text;strokeColor=none;fillColor=none;align=left;verticalAlign=middle;spacingLeft=4;spacingRight=4;overflow=hidden;points=[[0,0.5],[1,0.5]];portConstraint=eastwest;rotatable=0;whiteSpace=wrap;html=1;";
    private static final String STYLE_CONNECTOR = "endArrow=classic;startArrow=classic;html=1;rounded=0;edgeStyle=orthogonalEdgeStyle;";
    private static final String STYLE_LABEL = "edgeLabel;html=1;align=center;verticalAlign=middle;resizable=0;points=[];labelBackgroundColor=none;fontSize=10;spacingLeft=1;spacing=3;spacingRight=2;";

    private static final String AS_SOURCEPOINT = "sourcePoint";
    private static final String AS_TARGETPOINT = "targetPoint";
    private static final String AS_OFFSET = "offset";
    private static final String AS_GEOMETRY = "geometry";
    private static final int BOX_WIDTH = 140;
    private static final int BOX_HEIGHT_BASE = 30;
    private static final int BOX_HEIGHT_ITEM = 16;
    private static final int DISTANCE = 100;

    private final Map<EntityType, MxCell> typeCells = new HashMap<>();
    private int globalX = -1;
    private int globalY = 0;
    private int rowHeight = 30;
    private int maxWidth = 1;
    private int etCount = 0;

    public void generate(Writer writer, ModelRegistry model) throws IOException {
        generate(writer, model, false);
    }

    public void generate(Writer writer, ModelRegistry model, boolean isAdmin) throws IOException {
        Root root = new Root();
        MxCell cellZero = new MxCell()
                .setId("0");
        root.addMxCell(cellZero);
        MxCell cellOne = new MxCell()
                .setId("1")
                .setParent(cellZero.getId());
        root.addMxCell(cellOne);

        final Set<EntityType> entityTypes = model.getEntityTypes(isAdmin);
        maxWidth = (int) Math.round(Math.ceil(Math.sqrt(entityTypes.size())));
        for (EntityType et : entityTypes) {
            addEntityType(et, cellOne, root);
        }

        MxGraphModel gm = new MxGraphModel()
                .setRoot(root);
        Diagram diagram = new Diagram("Data Model")
                .setMxGraphModel(gm);
        MxFile mxFile = new MxFile()
                .setDiagram(diagram);
        SimpleModule module = new SimpleModule();
        module.addSerializer(Moment.class, new MomentSerializer());
        ObjectMapper xmlMapper = new XmlMapper()
                .configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true)
                .registerModule(module)
                .configure(SerializationFeature.INDENT_OUTPUT, true)
                .setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        xmlMapper.writeValue(writer, mxFile);
    }

    private void addEntityType(EntityType et, MxCell cellOne, Root root) {
        etCount++;
        globalX++;
        if (globalX >= maxWidth) {
            globalX = 0;
            globalY += rowHeight + DISTANCE;
            rowHeight = DISTANCE;
        }
        Set<EntityPropertyMain> entityProperties = et.getEntityProperties();
        int boxHeight = BOX_HEIGHT_BASE + entityProperties.size() * BOX_HEIGHT_ITEM;
        if (boxHeight > rowHeight) {
            rowHeight = boxHeight;
        }
        MxGeometry cellGeom = new MxGeometry()
                .setWidth(BOX_WIDTH)
                .setHeight(boxHeight)
                .setX(globalX * (BOX_WIDTH + DISTANCE))
                .setY(globalY);
        MxCell typeCell = new MxCell()
                .setValue(et.entityName)
                .setStyle(STYLE_LIST)
                .setParent(cellOne.getId())
                .setVertex(1)
                .setMxGeometry(cellGeom);
        root.addMxCell(typeCell);
        typeCells.put(et, typeCell);
        int listItemY = BOX_HEIGHT_BASE;
        for (EntityPropertyMain ep : entityProperties) {
            addEntityProperty(listItemY, typeCell, ep, root);
            listItemY += BOX_HEIGHT_ITEM;
        }
        for (NavigationProperty np : et.getNavigationEntities()) {
            addNavLink(np, et, typeCell, cellOne, root);
        }
        for (NavigationProperty np : et.getNavigationSets()) {
            addNavLink(np, et, typeCell, cellOne, root);
        }
    }

    private void addEntityProperty(int listItemY, MxCell typeCell, EntityPropertyMain ep, Root root) {
        MxGeometry propGeom = new MxGeometry()
                .setWidth(BOX_WIDTH)
                .setHeight(BOX_HEIGHT_ITEM)
                .setY(listItemY);
        MxCell propCell = new MxCell()
                .setParent(typeCell.getId())
                .setValue(createTextForEp(ep))
                .setStyle(STYLE_LIST_ITEM)
                .setVertex(1)
                .setConnectable(0)
                .setMxGeometry(propGeom);
        root.addMxCell(propCell);
    }

    private String createTextForEp(EntityPropertyMain ep) {
        String name = ep.getName();
        Collection<String> aliases = ep.getAliases();
        if (aliases.size() > 1) {
            // OData: find the first alias that does not start with an @
            for (String alias : aliases) {
                if (!alias.startsWith("@")) {
                    name = alias;
                    break;
                }
            }
        }
        return name + ": " + StringUtils.replace(ep.getType().getName(), "Edm.", "");
    }

    private void addNavLink(NavigationProperty np, EntityType et, MxCell typeCell, MxCell cellOne, Root root) {
        EntityType etTarget = np.getEntityType();
        MxCell targetCell = typeCells.get(etTarget);
        if (etTarget == et) {
            // Avoid adding non-symetrical self-relations twice
            if (np.getName().compareTo(np.getInverse().getName()) >= 0) {
                createLink(typeCell, typeCell, cellOne, root, np);
            }
        } else if (targetCell != null) {
            createLink(typeCell, targetCell, cellOne, root, np);
        }
    }

    private void createLink(MxCell sourceCell, MxCell targetCell, MxCell cellOne, Root root, NavigationProperty np) {
        MxGeometry linkGeom = new MxGeometry()
                .setWidth(50)
                .setHeight(50)
                .setRelative(1)
                .addMxPoint(new MxPoint()
                        .setX(sourceCell.getMxGeometry().getX())
                        .setY(sourceCell.getMxGeometry().getY())
                        .setAs(AS_SOURCEPOINT))
                .addMxPoint(new MxPoint()
                        .setX(targetCell.getMxGeometry().getX())
                        .setY(targetCell.getMxGeometry().getY())
                        .setAs(AS_TARGETPOINT));
        MxCell linkCell = new MxCell()
                .setStyle(STYLE_CONNECTOR)
                .setParent(cellOne.getId())
                .setSource(sourceCell.getId())
                .setTarget(targetCell.getId())
                .setEdge(1)
                .setMxGeometry(linkGeom);
        root.addMxCell(linkCell);
        root.addMxCell(createLabelCell(np.getInverse(), linkCell, false));
        if (np.getInverse() != np) {
            root.addMxCell(createLabelCell(np, linkCell, true));
        }
    }

    private MxCell createLabelCell(NavigationProperty np, MxCell linkCell, boolean target) {
        return new MxCell()
                .setValue(textForNp(np))
                .setStyle(STYLE_LABEL)
                .setParent(linkCell.getId())
                .setVertex(1)
                .setConnectable(0)
                .setMxGeometry(new MxGeometry()
                        .setRelative(1)
                        .setX(target ? 1 : -1)
                        .addMxPoint(new MxPoint()
                                .setAs(AS_OFFSET)));
    }

    public String textForNp(NavigationProperty np) {
        StringBuilder result = new StringBuilder(np.getName())
                .append("<br>");
        if (np.isEntitySet()) {
            if (np.isRequired()) {
                result.append("1..*");
            } else {
                result.append("0..*");
            }
        } else {
            if (np.isRequired()) {
                result.append("1");
            } else {
                result.append("0..1");
            }
        }
        return result.toString();
    }

    @JsonRootName("mxfile")
    private static class MxFile {

        private String host = "Electron";
        private Moment modified = Moment.nowInSystemTime();
        private String agent = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) draw.io/21.2.8 Chrome/112.0.5615.165 Electron/24.2.0 Safari/537.36";
        private UUID etag = UUID.randomUUID();
        private String version = "21.2.8";
        private String type = "device";
        private Diagram diagram;

        @JacksonXmlProperty(isAttribute = true)
        public String getAgent() {
            return agent;
        }

        public void setAgent(String agent) {
            this.agent = agent;
        }

        @JacksonXmlProperty(isAttribute = true)
        public String getHost() {
            return host;
        }

        public MxFile setHost(String host) {
            this.host = host;
            return this;
        }

        @JacksonXmlProperty(isAttribute = true)
        public Moment getModified() {
            return modified;
        }

        public MxFile setModified(Moment modified) {
            this.modified = modified;
            return this;
        }

        @JacksonXmlProperty(isAttribute = true)
        public UUID getEtag() {
            return etag;
        }

        public MxFile setEtag(UUID etag) {
            this.etag = etag;
            return this;
        }

        @JacksonXmlProperty(isAttribute = true)
        public String getVersion() {
            return version;
        }

        public MxFile setVersion(String version) {
            this.version = version;
            return this;
        }

        @JacksonXmlProperty(isAttribute = true)
        public String getType() {
            return type;
        }

        public MxFile setType(String type) {
            this.type = type;
            return this;
        }

        public Diagram getDiagram() {
            return diagram;
        }

        public MxFile setDiagram(Diagram diagram) {
            this.diagram = diagram;
            return this;
        }

    }

    private static class Diagram {

        private String name;
        private UUID id = UUID.randomUUID();
        private MxGraphModel mxGraphModel;

        public Diagram(String name) {
            this.name = name;
        }

        public MxGraphModel getMxGraphModel() {
            return mxGraphModel;
        }

        public Diagram setMxGraphModel(MxGraphModel mxGraphModel) {
            this.mxGraphModel = mxGraphModel;
            return this;
        }

        @JacksonXmlProperty(isAttribute = true)
        public String getName() {
            return name;
        }

        public Diagram setName(String name) {
            this.name = name;
            return this;
        }

        @JacksonXmlProperty(isAttribute = true)
        public UUID getId() {
            return id;
        }

        public Diagram setId(UUID id) {
            this.id = id;
            return this;
        }

    }

    private static class MxGraphModel {

        private int dx = 0;
        private int dy = 0;
        private int grid = 1;
        private int gridSize = 5;
        private int guides = 1;
        private int tooltips = 1;
        private int connect = 1;
        private int arrows = 1;
        private int fold = 1;
        private int page = 1;
        private int pageScale = 1;
        private int pageWidth = 850;
        private int pageHeight = 1100;
        private int math = 0;
        private int shadow = 0;
        private Root root;

        public Root getRoot() {
            return root;
        }

        public MxGraphModel setRoot(Root root) {
            this.root = root;
            return this;
        }

        @JacksonXmlProperty(isAttribute = true)
        public int getDx() {
            return dx;
        }

        public MxGraphModel setDx(int dx) {
            this.dx = dx;
            return this;
        }

        @JacksonXmlProperty(isAttribute = true)
        public int getDy() {
            return dy;
        }

        public MxGraphModel setDy(int dy) {
            this.dy = dy;
            return this;
        }

        @JacksonXmlProperty(isAttribute = true)
        public int getGrid() {
            return grid;
        }

        public MxGraphModel setGrid(int grid) {
            this.grid = grid;
            return this;
        }

        @JacksonXmlProperty(isAttribute = true)
        public int getGridSize() {
            return gridSize;
        }

        public MxGraphModel setGridSize(int gridSize) {
            this.gridSize = gridSize;
            return this;
        }

        @JacksonXmlProperty(isAttribute = true)
        public int getGuides() {
            return guides;
        }

        public MxGraphModel setGuides(int guides) {
            this.guides = guides;
            return this;
        }

        @JacksonXmlProperty(isAttribute = true)
        public int getTooltips() {
            return tooltips;
        }

        public MxGraphModel setTooltips(int tooltips) {
            this.tooltips = tooltips;
            return this;
        }

        @JacksonXmlProperty(isAttribute = true)
        public int getConnect() {
            return connect;
        }

        public MxGraphModel setConnect(int connect) {
            this.connect = connect;
            return this;
        }

        @JacksonXmlProperty(isAttribute = true)
        public int getArrows() {
            return arrows;
        }

        public MxGraphModel setArrows(int arrows) {
            this.arrows = arrows;
            return this;
        }

        @JacksonXmlProperty(isAttribute = true)
        public int getFold() {
            return fold;
        }

        public MxGraphModel setFold(int fold) {
            this.fold = fold;
            return this;
        }

        @JacksonXmlProperty(isAttribute = true)
        public int getPage() {
            return page;
        }

        public MxGraphModel setPage(int page) {
            this.page = page;
            return this;
        }

        @JacksonXmlProperty(isAttribute = true)
        public int getPageScale() {
            return pageScale;
        }

        public MxGraphModel setPageScale(int pageScale) {
            this.pageScale = pageScale;
            return this;
        }

        @JacksonXmlProperty(isAttribute = true)
        public int getPageWidth() {
            return pageWidth;
        }

        public MxGraphModel setPageWidth(int pageWidth) {
            this.pageWidth = pageWidth;
            return this;
        }

        @JacksonXmlProperty(isAttribute = true)
        public int getPageHeight() {
            return pageHeight;
        }

        public MxGraphModel setPageHeight(int pageHeight) {
            this.pageHeight = pageHeight;
            return this;

        }

        @JacksonXmlProperty(isAttribute = true)
        public int getMath() {
            return math;
        }

        public MxGraphModel setMath(int math) {
            this.math = math;
            return this;
        }

        @JacksonXmlProperty(isAttribute = true)
        public int getShadow() {
            return shadow;
        }

        public MxGraphModel setShadow(int shadow) {
            this.shadow = shadow;
            return this;
        }
    }

    private static class Root {

        private List<MxCell> mxCell = new ArrayList<>();

        @JacksonXmlElementWrapper(useWrapping = false)
        public List<MxCell> getMxCell() {
            return mxCell;
        }

        public Root addMxCell(MxCell cell) {
            mxCell.add(cell);
            return this;
        }
    }

    private static class MxCell {

        private String id = UUID.randomUUID().toString();
        private String value;
        private String style;
        private String parent;
        private Integer vertex;
        private Integer connectable;
        private String source;
        private String target;
        private Integer edge;
        private MxGeometry mxGeometry;

        @JacksonXmlProperty(isAttribute = true)
        public String getId() {
            return id;
        }

        public MxCell setId(String id) {
            this.id = id;
            return this;
        }

        @JacksonXmlProperty(isAttribute = true)
        public String getValue() {
            return value;
        }

        public MxCell setValue(String value) {
            this.value = value;
            return this;
        }

        @JacksonXmlProperty(isAttribute = true)
        public String getStyle() {
            return style;
        }

        public MxCell setStyle(String style) {
            this.style = style;
            return this;
        }

        @JacksonXmlProperty(isAttribute = true)
        public String getParent() {
            return parent;
        }

        public MxCell setParent(String parent) {
            this.parent = parent;
            return this;
        }

        @JacksonXmlProperty(isAttribute = true)
        public Integer getVertex() {
            return vertex;
        }

        public MxCell setVertex(Integer vertex) {
            this.vertex = vertex;
            return this;
        }

        @JacksonXmlProperty(isAttribute = true)
        public Integer getConnectable() {
            return connectable;
        }

        public MxCell setConnectable(Integer connectable) {
            this.connectable = connectable;
            return this;
        }

        @JacksonXmlProperty(isAttribute = true)
        public String getSource() {
            return source;
        }

        public MxCell setSource(String source) {
            this.source = source;
            return this;
        }

        @JacksonXmlProperty(isAttribute = true)
        public String getTarget() {
            return target;
        }

        public MxCell setTarget(String target) {
            this.target = target;
            return this;
        }

        @JacksonXmlProperty(isAttribute = true)
        public Integer getEdge() {
            return edge;
        }

        public MxCell setEdge(Integer edge) {
            this.edge = edge;
            return this;
        }

        public MxGeometry getMxGeometry() {
            return mxGeometry;
        }

        public MxCell setMxGeometry(MxGeometry mxGeometry) {
            this.mxGeometry = mxGeometry;
            return this;
        }

    }

    private static class MxGeometry {

        private final String as = AS_GEOMETRY;
        private Integer x;
        private Integer y;
        private Integer width;
        private Integer height;
        private Integer relative;

        private final List<MxPoint> mxPoint = new ArrayList<>();
        private Array array;

        @JacksonXmlProperty(isAttribute = true)
        public String getAs() {
            return as;
        }

        @JacksonXmlProperty(isAttribute = true)
        public Integer getX() {
            return x;
        }

        public MxGeometry setX(Integer x) {
            this.x = x;
            return this;
        }

        @JacksonXmlProperty(isAttribute = true)
        public Integer getY() {
            return y;
        }

        public MxGeometry setY(Integer y) {
            this.y = y;
            return this;
        }

        @JacksonXmlProperty(isAttribute = true)
        public Integer getWidth() {
            return width;
        }

        public MxGeometry setWidth(Integer width) {
            this.width = width;
            return this;
        }

        @JacksonXmlProperty(isAttribute = true)
        public Integer getHeight() {
            return height;
        }

        public MxGeometry setHeight(Integer height) {
            this.height = height;
            return this;
        }

        @JacksonXmlProperty(isAttribute = true)
        public Integer getRelative() {
            return relative;
        }

        public MxGeometry setRelative(Integer relative) {
            this.relative = relative;
            return this;
        }

        @JacksonXmlElementWrapper(useWrapping = false)
        public List<MxPoint> getMxPoint() {
            return mxPoint;
        }

        public MxGeometry addMxPoint(MxPoint point) {
            mxPoint.add(point);
            return this;
        }

        @JsonProperty("Array")
        public Array getArray() {
            return array;
        }

        public MxGeometry addArrayPoint(MxPoint point) {
            if (array == null) {
                array = new Array();
            }
            array.addMxPoint(point);
            return this;
        }
    }

    private static class MxPoint {

        private Integer x;
        private Integer y;
        private String as = "sourcePoint";

        @JacksonXmlProperty(isAttribute = true)
        public Integer getX() {
            return x;
        }

        public MxPoint setX(Integer x) {
            this.x = x;
            return this;
        }

        @JacksonXmlProperty(isAttribute = true)
        public Integer getY() {
            return y;
        }

        public MxPoint setY(Integer y) {
            this.y = y;
            return this;
        }

        @JacksonXmlProperty(isAttribute = true)
        public String getAs() {
            return as;
        }

        public MxPoint setAs(String as) {
            this.as = as;
            return this;
        }
    }

    private static class Array {

        private final String as = "points";
        private final List<MxPoint> mxPoint = new ArrayList<>();

        @JacksonXmlProperty(isAttribute = true)
        public String getAs() {
            return as;
        }

        public List<MxPoint> getMxPoint() {
            return mxPoint;
        }

        public Array addMxPoint(MxPoint point) {
            mxPoint.add(point);
            return this;
        }
    }

}
