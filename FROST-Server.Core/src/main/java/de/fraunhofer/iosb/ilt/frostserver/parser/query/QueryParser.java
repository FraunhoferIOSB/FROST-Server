/*
 * Copyright (C) 2016 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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
package de.fraunhofer.iosb.ilt.frostserver.parser.query;

import de.fraunhofer.iosb.ilt.frostserver.property.EntityProperty;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationProperty;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyCustom;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.property.Property;
import de.fraunhofer.iosb.ilt.frostserver.query.Expand;
import de.fraunhofer.iosb.ilt.frostserver.query.OrderBy;
import de.fraunhofer.iosb.ilt.frostserver.query.Query;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import de.fraunhofer.iosb.ilt.frostserver.util.ParserHelper;
import de.fraunhofer.iosb.ilt.frostserver.util.StringHelper;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QueryParser extends AbstractParserVisitor {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(QueryParser.class);

    private static final String OP_TOP = "top";
    private static final String OP_SKIP = "skip";
    private static final String OP_COUNT = "count";
    private static final String OP_SELECT = "select";
    private static final String OP_EXPAND = "expand";
    private static final String OP_FILTER = "filter";
    private static final String OP_FORMAT = "resultformat";
    private static final String OP_ORDER_BY = "orderby";

    private final CoreSettings settings;
    private final boolean customLinksEnabled;

    public QueryParser(CoreSettings settings) {
        this.settings = settings;
        customLinksEnabled = settings.getExperimentalSettings().getBoolean(CoreSettings.TAG_ENABLE_CUSTOM_LINKS, CoreSettings.class);
    }

    public static Query parseQuery(String query, CoreSettings settings) {
        return parseQuery(query, StringHelper.UTF8, settings);
    }

    public static Query parseQuery(String query, Charset encoding, CoreSettings settings) {
        if (query == null || query.isEmpty()) {
            return new Query(settings);
        }

        InputStream is = new ByteArrayInputStream(query.getBytes(encoding));
        Parser t = new Parser(is, StringHelper.UTF8.name());
        try {
            ASTStart n = t.Start();
            QueryParser v = new QueryParser(settings);
            return v.visit(n, null);
        } catch (ParseException | TokenMgrError | IllegalArgumentException ex) {
            LOGGER.error("Exception parsing: {}", StringHelper.cleanForLogging(query));
            throw new IllegalArgumentException("Query is not valid: " + ex.getMessage(), ex);
        }
    }

    @Override
    public Query visit(ASTStart node, Object data) {
        if (node.jjtGetNumChildren() != 1 || !(node.jjtGetChild(0) instanceof ASTOptions)) {
            throw new IllegalArgumentException("query start node must have exactly one child of type Options");
        }
        return visit((ASTOptions) node.jjtGetChild(0), data);
    }

    @Override
    public Query visit(ASTOptions node, Object data) {
        Query result = new Query(settings);
        node.childrenAccept(this, result);
        return result;
    }

    @Override
    public Object visit(ASTOption node, Object data) {
        Query query = (Query) data;
        String operator = node.getType().toLowerCase().trim();
        switch (operator) {
            case OP_TOP:
                int top = Math.toIntExact((long) ((ASTValueNode) node.jjtGetChild(0)).jjtGetValue());
                query.setTop(top);
                break;

            case OP_SKIP:
                query.setSkip(Math.toIntExact((long) ((ASTValueNode) node.jjtGetChild(0)).jjtGetValue()));
                break;

            case OP_COUNT:
                query.setCount(((ASTBool) node.jjtGetChild(0)).getValue());
                break;

            case OP_SELECT:
                if (node.jjtGetNumChildren() != 1 || !(node.jjtGetChild(0) instanceof ASTIdentifiers)) {
                    throw new IllegalArgumentException("ASTOption(select) must have exactly one child node of type ASTIdentifiers");
                }
                query.setSelect(visit((ASTIdentifiers) node.jjtGetChild(0), data));
                break;

            case OP_EXPAND:
                if (node.jjtGetNumChildren() != 1 || !(node.jjtGetChild(0) instanceof ASTFilteredPaths)) {
                    throw new IllegalArgumentException("ASTOption(expand) must have exactly one child node of type ASTFilteredPaths");
                }
                query.setExpand(visit(((ASTFilteredPaths) node.jjtGetChild(0)), data));
                break;

            case OP_FILTER:
                if (node.jjtGetNumChildren() != 1) {
                    throw new IllegalArgumentException("ASTOption(filter) must have exactly one child node");
                }
                query.setFilter(ExpressionParser.parseExpression(node.jjtGetChild(0)));
                break;

            case OP_FORMAT:
                query.setFormat(((ASTFormat) node.jjtGetChild(0)).getValue());
                break;

            case OP_ORDER_BY:
                if (node.jjtGetNumChildren() != 1 || !(node.jjtGetChild(0) instanceof ASTOrderBys)) {
                    throw new IllegalArgumentException("ASTOption(orderby) must have exactly one child node of type ASTOrderBys");
                }
                query.setOrderBy(visit((ASTOrderBys) node.jjtGetChild(0), data));
                break;

            default:
                // ignore or throw exception?
                throw new IllegalArgumentException("unknow query option '" + operator + "'");
        }
        return data;
    }

    @Override
    public List<Expand> visit(ASTFilteredPaths node, Object data) {
        List<Expand> result = new ArrayList<>();
        for (int i = 0; i < node.jjtGetNumChildren(); i++) {
            if (!(node.jjtGetChild(i) instanceof ASTFilteredPath)) {
                throw new IllegalArgumentException("ASTFilteredPaths can only have instance of ASTFilteredPath as childs");
            }
            result.add(visit((ASTFilteredPath) node.jjtGetChild(i), data));
        }
        return result;
    }

    @Override
    public Expand visit(ASTFilteredPath node, Object data) {
        // ASTOptions is not another child but rather a child of last ASTPathElement
        Expand result = new Expand();
        Expand current = null;
        int numChildren = node.jjtGetNumChildren();
        for (int i = 0; i < numChildren; i++) {
            if (current == null) {
                current = result;
            } else {
                Expand temp = new Expand();
                if (current.getSubQuery() == null) {
                    current.setSubQuery(new Query(settings));
                }
                current.getSubQuery().addExpand(temp);
                current = temp;
            }
            Node childNode = node.jjtGetChild(i);
            if (!(childNode instanceof ASTPathElement)) {
                throw new IllegalArgumentException("ASTFilteredPaths can only have instances of ASTPathElement as childs");
            }
            String name = ((ASTPathElement) childNode).getName();
            NavigationProperty property;
            try {
                property = NavigationPropertyMain.fromString(name);
            } catch (IllegalArgumentException ex) {
                EntityProperty entityProp = EntityProperty.fromString(name);
                if (!entityProp.hasCustomProperties) {
                    throw new IllegalArgumentException("Only Entity Properties of JSON type allowed in expand paths.");
                }
                if (!customLinksEnabled) {
                    throw new IllegalArgumentException("Custom links not allowed.");
                }
                NavigationPropertyCustom customProperty = new NavigationPropertyCustom(entityProp);
                while (++i < numChildren) {
                    Node subChildNode = node.jjtGetChild(i);
                    if (!(subChildNode instanceof ASTPathElement)) {
                        throw new IllegalArgumentException("ASTFilteredPaths can only have instances of ASTPathElement as childs");
                    }
                    String subName = ((ASTPathElement) subChildNode).getName();
                    customProperty.addToSubPath(subName);
                }
                i--;
                property = customProperty;
            }

            current.setPath(property);
            // look at children of child
            if (node.jjtGetChild(i).jjtGetNumChildren() > 1) {
                throw new IllegalArgumentException("ASTFilteredPath can at most have one child");
            }
            if (node.jjtGetChild(i).jjtGetNumChildren() == 1) {
                if (!(node.jjtGetChild(i).jjtGetChild(0) instanceof ASTOptions) || current.getSubQuery() != null) {
                    throw new IllegalArgumentException("there is only one subquery allowed per expand path");
                }
                current.setSubQuery(visit(((ASTOptions) node.jjtGetChild(i).jjtGetChild(0)), data));
            }
        }
        return result;
    }

    @Override
    public List<Property> visit(ASTIdentifiers node, Object data) {
        List<Property> result = new ArrayList<>();
        for (int i = 0; i < node.jjtGetNumChildren(); i++) {
            Property property = visit((ASTPathElement) node.jjtGetChild(i), data);
            result.add(property);
        }
        return result;
    }

    @Override
    public Property visit(ASTPathElement node, Object data) {
        if (node.getIdentifier() != null && !node.getIdentifier().isEmpty()) {
            throw new IllegalArgumentException("no identified paths are allowed inside select");
        }
        Property previous = null;
        if (data instanceof Property) {
            previous = (Property) data;
        }
        return ParserHelper.parseProperty(node.getName(), previous);
    }

    @Override
    public List<OrderBy> visit(ASTOrderBys node, Object data) {
        List<OrderBy> result = new ArrayList<>();
        for (int i = 0; i < node.jjtGetNumChildren(); i++) {
            result.add(visit((ASTOrderBy) node.jjtGetChild(i), data));
        }
        return result;
    }

    @Override
    public OrderBy visit(ASTOrderBy node, Object data) {
        if (node.jjtGetNumChildren() != 1) {
            throw new IllegalArgumentException("ASTOrderBy node must have exactly one child");
        }
        return new OrderBy(
                ExpressionParser.parseExpression(node.jjtGetChild(0)),
                node.isAscending() ? OrderBy.OrderType.ASCENDING : OrderBy.OrderType.DESCENDING);
    }
}
