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

import de.fraunhofer.iosb.ilt.frostserver.path.ResourcePath;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyMain;
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
    private final ResourcePath path;
    private final boolean customLinksEnabled;

    public QueryParser(CoreSettings settings, ResourcePath path) {
        this.settings = settings;
        this.path = path;
        customLinksEnabled = settings.getExperimentalSettings().getBoolean(CoreSettings.TAG_ENABLE_CUSTOM_LINKS, CoreSettings.class);
    }

    public static Query parseQuery(String query, CoreSettings settings, ResourcePath path) {
        return parseQuery(query, StringHelper.UTF8, settings, path);
    }

    public static Query parseQuery(String query, Charset encoding, CoreSettings settings, ResourcePath path) {
        if (query == null || query.isEmpty()) {
            return new Query(settings.getQueryDefaults(), path);
        }

        InputStream is = new ByteArrayInputStream(query.getBytes(encoding));
        Parser t = new Parser(is, StringHelper.UTF8.name());
        try {
            ASTStart n = t.Start();
            QueryParser v = new QueryParser(settings, path);
            return v.visit(n, null);
        } catch (ParseException | TokenMgrError | IllegalArgumentException ex) {
            LOGGER.error("Exception parsing: {}", StringHelper.cleanForLogging(query));
            throw new IllegalArgumentException("Query is not valid: " + ex.getMessage(), ex);
        }
    }

    @Override
    public Query visit(ASTStart node, Object data) {
        if (node.jjtGetNumChildren() != 1) {
            throw new IllegalArgumentException("query start node must have exactly one child of type Options");
        }
        ASTOptions options = getChildOfType(node, 0, ASTOptions.class);
        return visit(options, data);
    }

    @Override
    public Query visit(ASTOptions node, Object data) {
        Query result = new Query(settings.getQueryDefaults(), path);
        node.childrenAccept(this, result);
        return result;
    }

    @Override
    public Object visit(ASTOption node, Object data) {
        if (node.jjtGetNumChildren() != 1) {
            throw new IllegalArgumentException("Query options must have exactly one child node.");
        }
        Query query = (Query) data;
        String operator = node.getType().toLowerCase().trim();
        switch (operator) {
            case OP_TOP:
                handleTop(node, query);
                break;

            case OP_SKIP:
                handleSkip(node, query);
                break;

            case OP_COUNT:
                handleCount(node, query);
                break;

            case OP_SELECT:
                handleSelect(node, query, data);
                break;

            case OP_EXPAND:
                handleExpand(node, query, data);
                break;

            case OP_FILTER:
                query.setFilter(ExpressionParser.parseExpression(node.jjtGetChild(0)));
                break;

            case OP_FORMAT:
                handleFromat(node, query);
                break;

            case OP_ORDER_BY:
                handleOrderBy(node, query, data);
                break;

            default:
                // ignore or throw exception?
                throw new IllegalArgumentException("unknow query option '" + operator + "'");
        }
        return data;
    }

    private void handleOrderBy(ASTOption node, Query query, Object data) {
        ASTOrderBys child = getChildOfType(node, 0, ASTOrderBys.class);
        query.setOrderBy(visit(child, data));
    }

    private void handleFromat(ASTOption node, Query query) {
        ASTFormat child = getChildOfType(node, 0, ASTFormat.class);
        query.setFormat(child.getValue());
    }

    private void handleExpand(ASTOption node, Query query, Object data) {
        ASTFilteredPaths child = getChildOfType(node, 0, ASTFilteredPaths.class);
        query.setExpand(visit(child, data));
    }

    private void handleSelect(ASTOption node, Query query, Object data) {
        ASTIdentifiers child = getChildOfType(node, 0, ASTIdentifiers.class);
        query.addSelect(visit(child, data));
    }

    private void handleCount(ASTOption node, Query query) {
        ASTBool child = getChildOfType(node, 0, ASTBool.class);
        query.setCount(child.getValue());
    }

    private void handleSkip(ASTOption node, Query query) {
        ASTValueNode child = getChildOfType(node, 0, ASTValueNode.class);
        query.setSkip(Math.toIntExact((long) child.jjtGetValue()));
    }

    private void handleTop(ASTOption node, Query query) {
        ASTValueNode child = getChildOfType(node, 0, ASTValueNode.class);
        int top = Math.toIntExact((long) child.jjtGetValue());
        query.setTop(top);
    }

    @Override
    public List<Expand> visit(ASTFilteredPaths node, Object data) {
        List<Expand> result = new ArrayList<>();
        for (int i = 0; i < node.jjtGetNumChildren(); i++) {
            final ASTFilteredPath childNode = getChildOfType(node, i, ASTFilteredPath.class);
            result.add(visit(childNode, data));
        }
        return result;
    }

    @Override
    public Expand visit(ASTFilteredPath node, Object data) {
        // ASTOptions is not another child but rather a child of last ASTPathElement
        Expand resultExpand = new Expand();
        Expand currentExpand = null;
        int numChildren = node.jjtGetNumChildren();
        int i = 0;
        while (i < numChildren) {
            currentExpand = prepareCurrentExpand(currentExpand, resultExpand);
            i = handleChild(node, i, currentExpand, data);
            i++;
        }
        return resultExpand;
    }

    private Expand prepareCurrentExpand(Expand current, Expand result) {
        if (current == null) {
            return result;
        } else {
            Expand temp = new Expand();
            if (!current.hasSubQuery()) {
                current.setSubQuery(new Query(settings.getQueryDefaults(), path));
            }
            current.getSubQuery().addExpand(temp);
            return temp;
        }
    }

    private int handleChild(ASTFilteredPath node, int start, Expand currentExpand, Object data) {
        int idx = start;
        ASTPathElement childNode = getChildOfType(node, idx, ASTPathElement.class);
        String name = childNode.getName();
        NavigationProperty property;
        try {
            property = NavigationPropertyMain.fromString(name);
        } catch (IllegalArgumentException ex) {
            EntityPropertyMain entityProp = EntityPropertyMain.fromString(name);
            if (!entityProp.hasCustomProperties) {
                throw new IllegalArgumentException("Only Entity Properties of JSON type allowed in expand paths.");
            }
            if (!customLinksEnabled) {
                throw new IllegalArgumentException("Custom links not allowed.");
            }
            NavigationPropertyCustom customProperty = new NavigationPropertyCustom(entityProp);
            int numChildren = node.jjtGetNumChildren();
            while (++idx < numChildren) {
                childNode = getChildOfType(node, idx, ASTPathElement.class);
                customProperty.addToSubPath(childNode.getName());
            }
            property = customProperty;
        }
        currentExpand.setPath(property);
        // look at children of child
        if (childNode.jjtGetNumChildren() > 1) {
            throw new IllegalArgumentException("ASTFilteredPath can at most have one child");
        }
        if (childNode.jjtGetNumChildren() == 1) {
            if (currentExpand.hasSubQuery()) {
                throw new IllegalArgumentException("there is only one subquery allowed per expand path");
            }
            ASTOptions subChildNode = getChildOfType(childNode, 0, ASTOptions.class);
            currentExpand.setSubQuery(visit(subChildNode, data));
        }
        return idx;
    }

    @Override
    public List<Property> visit(ASTIdentifiers node, Object data) {
        List<Property> result = new ArrayList<>();
        for (int i = 0; i < node.jjtGetNumChildren(); i++) {
            ASTPathElement child = getChildOfType(node, i, ASTPathElement.class);
            Property property = visit(child, data);
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
            ASTOrderBy child = getChildOfType(node, i, ASTOrderBy.class);
            result.add(visit(child, data));
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

    private static <T extends Node> T getChildOfType(SimpleNode parent, int index, Class<T> expectedType) {
        Node childNode = parent.jjtGetChild(index);
        if (!(expectedType.isAssignableFrom(childNode.getClass()))) {
            throw new IllegalArgumentException(parent.getClass().getName() + " expected to have child of type " + expectedType.getName());
        }
        return (T) childNode;
    }
}
