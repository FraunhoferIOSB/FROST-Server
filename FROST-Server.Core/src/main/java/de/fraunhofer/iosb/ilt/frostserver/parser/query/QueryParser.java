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
package de.fraunhofer.iosb.ilt.frostserver.parser.query;

import de.fraunhofer.iosb.ilt.frostserver.model.ModelRegistry;
import de.fraunhofer.iosb.ilt.frostserver.path.ResourcePath;
import de.fraunhofer.iosb.ilt.frostserver.query.Expand;
import de.fraunhofer.iosb.ilt.frostserver.query.Metadata;
import de.fraunhofer.iosb.ilt.frostserver.query.OrderBy;
import de.fraunhofer.iosb.ilt.frostserver.query.PropertyPlaceholder;
import de.fraunhofer.iosb.ilt.frostserver.query.Query;
import de.fraunhofer.iosb.ilt.frostserver.query.QueryDefaults;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.Expression;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import de.fraunhofer.iosb.ilt.frostserver.util.StringHelper;
import de.fraunhofer.iosb.ilt.frostserver.util.queryparser.Node;
import de.fraunhofer.iosb.ilt.frostserver.util.queryparser.Node.Visitor;
import de.fraunhofer.iosb.ilt.frostserver.util.queryparser.ParseException;
import de.fraunhofer.iosb.ilt.frostserver.util.queryparser.QParser;
import de.fraunhofer.iosb.ilt.frostserver.util.queryparser.Token;
import de.fraunhofer.iosb.ilt.frostserver.util.queryparser.nodes.P_ExpandItem;
import de.fraunhofer.iosb.ilt.frostserver.util.queryparser.nodes.P_Option;
import de.fraunhofer.iosb.ilt.frostserver.util.queryparser.nodes.P_OrderBy;
import de.fraunhofer.iosb.ilt.frostserver.util.queryparser.nodes.P_PlainPath;
import de.fraunhofer.iosb.ilt.frostserver.util.queryparser.nodes.P_Ref;
import de.fraunhofer.iosb.ilt.frostserver.util.queryparser.nodes.Start;
import de.fraunhofer.iosb.ilt.frostserver.util.queryparser.nodes.T_ARRAYINDEX;
import de.fraunhofer.iosb.ilt.frostserver.util.queryparser.nodes.T_BOOL;
import de.fraunhofer.iosb.ilt.frostserver.util.queryparser.nodes.T_CHARSEQ_FORMAT;
import de.fraunhofer.iosb.ilt.frostserver.util.queryparser.nodes.T_CHARSEQ_METADATA;
import de.fraunhofer.iosb.ilt.frostserver.util.queryparser.nodes.T_DISTINCT;
import de.fraunhofer.iosb.ilt.frostserver.util.queryparser.nodes.T_LONG;
import de.fraunhofer.iosb.ilt.frostserver.util.queryparser.nodes.T_O_COUNT;
import de.fraunhofer.iosb.ilt.frostserver.util.queryparser.nodes.T_O_DESC;
import de.fraunhofer.iosb.ilt.frostserver.util.queryparser.nodes.T_O_EXPAND;
import de.fraunhofer.iosb.ilt.frostserver.util.queryparser.nodes.T_O_FILTER;
import de.fraunhofer.iosb.ilt.frostserver.util.queryparser.nodes.T_O_FORMAT;
import de.fraunhofer.iosb.ilt.frostserver.util.queryparser.nodes.T_O_METADATA;
import de.fraunhofer.iosb.ilt.frostserver.util.queryparser.nodes.T_O_ORDERBY;
import de.fraunhofer.iosb.ilt.frostserver.util.queryparser.nodes.T_O_SELECT;
import de.fraunhofer.iosb.ilt.frostserver.util.queryparser.nodes.T_O_SKIP;
import de.fraunhofer.iosb.ilt.frostserver.util.queryparser.nodes.T_O_SKIPFILTER;
import de.fraunhofer.iosb.ilt.frostserver.util.queryparser.nodes.T_O_TOP;
import de.fraunhofer.iosb.ilt.frostserver.util.queryparser.nodes.T_PATH_SEPARATOR;
import de.fraunhofer.iosb.ilt.frostserver.util.queryparser.nodes.T_STRING;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QueryParser extends Visitor {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(QueryParser.class);

    private final QueryDefaults queryDefaults;
    private final ModelRegistry modelRegistry;
    private final ResourcePath path;
    private ExpressionParser expressionParser;
    private Query currentQuery;
    private P_Option currentOption;

    public QueryParser(QueryDefaults queryDefaults, ModelRegistry modelRegistry, ResourcePath path) {
        this.queryDefaults = queryDefaults;
        this.modelRegistry = modelRegistry;
        this.path = path;
    }

    private Query handle(Start node) {
        Query query = new Query(modelRegistry, queryDefaults, path);
        for (P_Ref child : node.childrenOfType(P_Ref.class)) {
            handle(child, query);
        }
        for (P_Option child : node.childrenOfType(P_Option.class)) {
            handle(child, query);
        }
        return query;
    }

    private void handle(P_Ref ref, Query query) {
        List<T_STRING> children = ref.childrenOfType(T_STRING.class);
        if (children.size() != 1) {
            throw new IllegalArgumentException("$id must be followed by a string");
        }
        query.setId(children.get(0).getImage());
    }

    private void handle(P_Option option, Query query) {
        Query lastQuery = currentQuery;
        P_Option lastOption = currentOption;

        Node type = option.getFirstChild();
        currentQuery = query;
        currentOption = option;
        visit(type);

        currentQuery = lastQuery;
        currentOption = lastOption;
    }

    void visit(T_O_COUNT node) {
        List<T_BOOL> values = currentOption.childrenOfType(T_BOOL.class);
        if (values.isEmpty()) {
            return;
        }
        currentQuery.setCount(Boolean.valueOf(values.get(0).getImage()));
    }

    void visit(T_O_SKIP node) {
        List<T_LONG> values = currentOption.childrenOfType(T_LONG.class);
        if (values.isEmpty()) {
            return;
        }
        currentQuery.setSkip(Integer.parseInt(values.get(0).getImage()));
    }

    void visit(T_O_TOP node) {
        List<T_LONG> values = currentOption.childrenOfType(T_LONG.class);
        if (values.isEmpty()) {
            return;
        }
        String image = values.get(0).getImage();
        currentQuery.setTop(Integer.parseInt(image));
    }

    void visit(T_O_FORMAT node) {
        List<T_CHARSEQ_FORMAT> values = currentOption.childrenOfType(T_CHARSEQ_FORMAT.class);
        if (values.isEmpty()) {
            return;
        }
        currentQuery.setFormat(values.get(0).getImage());
    }

    void visit(T_O_METADATA node) {
        List<T_CHARSEQ_METADATA> values = currentOption.childrenOfType(T_CHARSEQ_METADATA.class);
        if (values.isEmpty()) {
            return;
        }
        currentQuery.setMetadata(Metadata.lookup(values.get(0).getImage()));
    }

    void visit(T_O_SELECT node) {
        if (currentOption.getChild(1) instanceof T_DISTINCT) {
            currentQuery.setSelectDistinct(true);
        }
        List<P_PlainPath> values = currentOption.childrenOfType(P_PlainPath.class);
        if (values.isEmpty()) {
            return;
        }
        for (P_PlainPath pp : values) {
            PropertyPlaceholder property = handle(pp);
            currentQuery.addSelect(property);
        }
    }

    PropertyPlaceholder handle(P_PlainPath pp) {
        List<Token> children = pp.childrenOfType(Token.class);
        PropertyPlaceholder property = new PropertyPlaceholder(children.get(0).getImage());
        for (int i = 1; i < children.size(); i++) {
            final Token child = children.get(i);
            if (child instanceof T_PATH_SEPARATOR) {
                continue;
            }
            final String img = child.getImage();
            if (child instanceof T_ARRAYINDEX) {
                property.addToSubPath(img.substring(1, img.length() - 1));
            } else {
                property.addToSubPath(img);
            }
        }
        return property;
    }

    void visit(T_O_ORDERBY node) {
        List<P_OrderBy> values = currentOption.childrenOfType(P_OrderBy.class);
        if (values.isEmpty()) {
            return;
        }
        for (P_OrderBy orderby : values) {
            QueryParser.this.handle(orderby);
        }
    }

    void handle(P_OrderBy node) {
        var dir = OrderBy.OrderType.ASCENDING;
        if (node.getChildCount() == 2 && node.getChild(1) instanceof T_O_DESC) {
            dir = OrderBy.OrderType.DESCENDING;
        }
        Expression expression = getExpressionParser().parseExpression(node.getChild(0));
        currentQuery.addOrderBy(new OrderBy(expression, dir));
    }

    void visit(T_O_FILTER node) {
        currentQuery.setFilter(getExpressionParser().parseExpression(currentOption.getChild(1)));
    }

    void visit(T_O_SKIPFILTER node) {
        currentQuery.setSkipFilter(getExpressionParser().parseExpression(currentOption.getChild(1)));
    }

    void visit(T_O_EXPAND node) {
        List<P_ExpandItem> values = currentOption.childrenOfType(P_ExpandItem.class);
        if (values.isEmpty()) {
            return;
        }
        for (P_ExpandItem expand : values) {
            handle(expand);
        }
    }

    void handle(P_ExpandItem expandItem) {
        Expand expand = new Expand(modelRegistry);
        List<P_PlainPath> paths = expandItem.childrenOfType(P_PlainPath.class);
        if (paths.isEmpty()) {
            return;
        }
        PropertyPlaceholder property = handle(paths.get(0));
        expand.addToRawPath(property.getName());
        for (String item : property.getSubPath()) {
            expand.addToRawPath(item);
        }
        List<P_Option> subOptions = expandItem.childrenOfType(P_Option.class);
        if (!subOptions.isEmpty()) {
            Query subQuery = new Query(modelRegistry, queryDefaults, path);
            for (P_Option subOption : subOptions) {
                handle(subOption, subQuery);
            }
            expand.setSubQuery(subQuery);
        }
        currentQuery.addExpand(expand);
    }

    private ExpressionParser getExpressionParser() {
        if (expressionParser == null) {
            expressionParser = new ExpressionParser(this);
        }
        return expressionParser;
    }

    public static Query parseQuery(String query, CoreSettings settings, ResourcePath path) {
        return parseQuery(query, StringHelper.UTF8, settings.getQueryDefaults(), settings.getModelRegistry(), path);
    }

    public static Query parseQuery(String query, QueryDefaults queryDefaults, ModelRegistry modelRegistry, ResourcePath path) {
        return parseQuery(query, StringHelper.UTF8, queryDefaults, modelRegistry, path);
    }

    public static Query parseQuery(String query, Charset encoding, QueryDefaults queryDefaults, ModelRegistry modelRegistry, ResourcePath path) {
        if (query == null || query.isEmpty()) {
            return new Query(modelRegistry, queryDefaults, path);
        }
        LOGGER.debug("Parsing: {}", query);

        InputStream is = new ByteArrayInputStream(query.getBytes(encoding));
        QParser t = new QParser(is);
        try {
            Start start = t.Start();
            QueryParser v = new QueryParser(queryDefaults, modelRegistry, path);
            return v.handle(start);
        } catch (ParseException | IllegalArgumentException ex) {
            LOGGER.error("Exception parsing: {}", StringHelper.cleanForLogging(query));
            throw new IllegalArgumentException("Query is not valid: " + ex.getMessage(), ex);
        }
    }

}
