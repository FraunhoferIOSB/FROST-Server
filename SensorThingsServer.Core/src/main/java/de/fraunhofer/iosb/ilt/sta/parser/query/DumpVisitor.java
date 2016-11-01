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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DumpVisitor implements ParserVisitor {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(DumpVisitor.class);
    private int indent = 0;

    private String indentString() {
        StringBuilder sb = new StringBuilder();
        boolean line = true;
        for (int i = 0; i < indent; ++i) {
            if (line) {
                sb.append("| ");
            } else {
                sb.append("  ");
            }
            line = !line;
        }
        return sb.toString();
    }

    public Object defltAction(SimpleNode node, Object data) {
        if (node.value == null) {
            LOGGER.info(indentString() + node);
        } else {
            LOGGER.info(indentString() + node + " : (" + node.value.getClass().getName() + ") " + node.value);
        }
        ++indent;
        node.childrenAccept(this, data);
        --indent;
        return data;
    }

    @Override
    public Object visit(SimpleNode node, Object data) {
        LOGGER.info("{}{}: acceptor not implemented in subclass?", indentString(), node);
        ++indent;
        node.childrenAccept(this, data);
        --indent;
        return data;
    }

    @Override
    public Object visit(ASTStart node, Object data) {
        LOGGER.info(indentString() + node);
        ++indent;
        node.childrenAccept(this, data);
        --indent;
        return data;
    }

    @Override
    public Object visit(ASTOptions node, Object data) {
        return defltAction(node, data);
    }

    @Override
    public Object visit(ASTOption node, Object data) {
        return defltAction(node, data);
    }

    @Override
    public Object visit(ASTOrderBys node, Object data) {
        return defltAction(node, data);
    }

    @Override
    public Object visit(ASTOrderBy node, Object data) {
        return defltAction(node, data);
    }

    @Override
    public Object visit(ASTIdentifiers node, Object data) {
        return defltAction(node, data);
    }

    @Override
    public Object visit(ASTPlainPaths node, Object data) {
        return defltAction(node, data);
    }

    @Override
    public Object visit(ASTPlainPath node, Object data) {
        return defltAction(node, data);
    }

    @Override
    public Object visit(ASTPathElement node, Object data) {
        return defltAction(node, data);
    }

    @Override
    public Object visit(ASTFilteredPaths node, Object data) {
        return defltAction(node, data);
    }

    @Override
    public Object visit(ASTFilteredPath node, Object data) {
        return defltAction(node, data);
    }

    @Override
    public Object visit(ASTIdentifiedPaths node, Object data) {
        return defltAction(node, data);
    }

    @Override
    public Object visit(ASTIdentifiedPath node, Object data) {
        return defltAction(node, data);
    }

    @Override
    public Object visit(ASTFilter node, Object data) {
        return defltAction(node, data);
    }

    @Override
    public Object visit(ASTFormat node, Object data) {
        return defltAction(node, data);
    }

    @Override
    public Object visit(ASTLogicalOr node, Object data) {
        return defltAction(node, data);
    }

    @Override
    public Object visit(ASTLogicalAnd node, Object data) {
        return defltAction(node, data);
    }

    @Override
    public Object visit(ASTNot node, Object data) {
        return defltAction(node, data);
    }

    @Override
    public Object visit(ASTBooleanFunction node, Object data) {
        return defltAction(node, data);
    }

    @Override
    public Object visit(ASTComparison node, Object data) {
        return defltAction(node, data);
    }

    @Override
    public Object visit(ASTPlusMin node, Object data) {
        return defltAction(node, data);
    }

    @Override
    public Object visit(ASTOperator node, Object data) {
        return defltAction(node, data);
    }

    @Override
    public Object visit(ASTMulDiv node, Object data) {
        return defltAction(node, data);
    }

    @Override
    public Object visit(ASTValueNode node, Object data) {
        return defltAction(node, data);
    }

    @Override
    public Object visit(ASTBool node, Object data) {
        return defltAction(node, data);
    }

    @Override
    public Object visit(ASTFunction node, Object data) {
        return defltAction(node, data);
    }

    @Override
    public Object visit(ASTGeoStringLit node, Object data) {
        return defltAction(node, data);
    }

}
