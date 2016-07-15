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
package de.fraunhofer.iosb.ilt.sta.parser.query;

import de.fraunhofer.iosb.ilt.sta.Constants;
import de.fraunhofer.iosb.ilt.sta.path.NavigationProperty;
import de.fraunhofer.iosb.ilt.sta.path.Property;
import de.fraunhofer.iosb.ilt.sta.query.Expand;
import de.fraunhofer.iosb.ilt.sta.query.OrderBy;
import de.fraunhofer.iosb.ilt.sta.query.Query;
import de.fraunhofer.iosb.ilt.sta.util.ParserHelper;
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
    private static final Charset ENCODING = Charset.forName("UTF-8");

    public static Query parseQuery(String query) {
        return parseQuery(query, ENCODING);
    }

    public static Query parseQuery(String query, Charset encoding) {
        if (query == null) {
            Query result = new Query();
            return result;
        }
        LOGGER.debug("Parsing: {}", query);
        InputStream is = new ByteArrayInputStream(query.getBytes(encoding));
        Parser t = new Parser(is, ENCODING.name());
        try {
            ASTStart n = t.Start();
            QueryParser v = new QueryParser();
            return v.visit(n, null);
        } catch (ParseException | TokenMgrError | IllegalArgumentException ex) {
            LOGGER.error("Failed to parse because (Set loglevel to trace for stack): {}", ex.getMessage());
            LOGGER.trace("Exception: ", ex);
            throw new IllegalArgumentException("Query is not valid.", ex);
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
        Query result = new Query();
        node.childrenAccept(this, result);
        return result;
    }

    private static final String OP_TOP = "top";
    private static final String OP_SKIP = "skip";
    private static final String OP_COUNT = "count";
    private static final String OP_SELECT = "select";
    private static final String OP_EXPAND = "expand";
    private static final String OP_FILTER = "filter";
    private static final String OP_ORDER_BY = "orderby";

    @Override
    public Object visit(ASTOption node, Object data) {
        Query query = (Query) data;
        String operator = node.getType().toLowerCase().trim();
        switch (operator) {
            case OP_TOP: {
                int top = Math.toIntExact((long) ((ASTValueNode) node.jjtGetChild(0)).jjtGetValue());
                query.setTop(Math.min(top, Constants.DEFAULT_MAX_TOP));
                break;
            }
            case OP_SKIP: {
                query.setSkip(Math.toIntExact((long) ((ASTValueNode) node.jjtGetChild(0)).jjtGetValue()));
                break;
            }
            case OP_COUNT: {
                query.setCount(((ASTBool) node.jjtGetChild(0)).getValue());
                break;
            }
            case OP_SELECT: {
                if (node.jjtGetNumChildren() != 1 || !(node.jjtGetChild(0) instanceof ASTIdentifiers)) {
                    throw new IllegalArgumentException("ASTOption(select) must have exactly one child node of type ASTIdentifiers");
                }
                query.setSelect(visit((ASTIdentifiers) node.jjtGetChild(0), data));
                break;
            }
            case OP_EXPAND: {
                if (node.jjtGetNumChildren() != 1 || !(node.jjtGetChild(0) instanceof ASTFilteredPaths)) {
                    throw new IllegalArgumentException("ASTOption(expand) must have exactly one child node of type ASTFilteredPaths");
                }
                query.setExpand(visit(((ASTFilteredPaths) node.jjtGetChild(0)), data));
                break;
            }
            case OP_FILTER: {
                if (node.jjtGetNumChildren() != 1) {
                    throw new IllegalArgumentException("ASTOption(filter) must have exactly one child node");
                }
                query.setFilter(ExpressionParser.parseExpression(node.jjtGetChild(0)));
                break;
            }
            case OP_ORDER_BY: {
                if (node.jjtGetNumChildren() != 1 || !(node.jjtGetChild(0) instanceof ASTOrderBys)) {
                    throw new IllegalArgumentException("ASTOption(orderby) must have exactly one child node of type ASTOrderBys");
                }
                query.setOrderBy(visit((ASTOrderBys) node.jjtGetChild(0), data));
                break;
            }

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
        for (int i = 0; i < node.jjtGetNumChildren(); i++) {
            if (!(node.jjtGetChild(i) instanceof ASTPathElement)) {
                throw new IllegalArgumentException("ASTFilteredPaths can only have instances of ASTPathElement as childs");

            }
            result.getPath().add(NavigationProperty.fromString(((ASTPathElement) node.jjtGetChild(i)).getName()));
            // look at children of child
            if (node.jjtGetChild(i).jjtGetNumChildren() > 1) {
                throw new IllegalArgumentException("ASTFilteredPath can at most have one child");
            }
            if (node.jjtGetChild(i).jjtGetNumChildren() == 1) {
                if (!(node.jjtGetChild(i).jjtGetChild(0) instanceof ASTOptions) || result.getSubQuery() != null) {
                    throw new IllegalArgumentException("there is only one subquery allowed per expand path");
                }
                result.setSubQuery(visit(((ASTOptions) node.jjtGetChild(i).jjtGetChild(0)), data));
            }
        }
        return result;
    }

    @Override
    public List<Property> visit(ASTIdentifiers node, Object data) {
        List<Property> result = new ArrayList<>();
        for (int i = 0; i < node.jjtGetNumChildren(); i++) {
            result.add(visit((ASTPathElement) node.jjtGetChild(i), data));
        }
        return result;
    }

    @Override
    public Property visit(ASTPathElement node, Object data) {
        if (node.getIdentifier() != null && !node.getIdentifier().isEmpty()) {
            throw new IllegalArgumentException("no identified paths are allowed inside select");
        }
        return ParserHelper.parseProperty(node.getName());
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
                node.isAscending()
                        ? OrderBy.OrderType.Ascending
                        : OrderBy.OrderType.Descending);
    }
}
