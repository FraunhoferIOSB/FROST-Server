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

/**
 *
 * @author jab
 */
public abstract class AbstractParserVisitor implements ParserVisitor {

    private static final String NOT_SUPPORTED_YET = "Not supported yet.";

    @Override
    public Object visit(SimpleNode node, Object data) {
        throw new UnsupportedOperationException(NOT_SUPPORTED_YET);
    }

    @Override
    public Object visit(ASTStart node, Object data) {
        throw new UnsupportedOperationException(NOT_SUPPORTED_YET);
    }

    @Override
    public Object visit(ASTOptions node, Object data) {
        throw new UnsupportedOperationException(NOT_SUPPORTED_YET);
    }

    @Override
    public Object visit(ASTOption node, Object data) {
        throw new UnsupportedOperationException(NOT_SUPPORTED_YET);
    }

    @Override
    public Object visit(ASTFormat node, Object data) {
        throw new UnsupportedOperationException(NOT_SUPPORTED_YET);
    }

    @Override
    public Object visit(ASTOrderBys node, Object data) {
        throw new UnsupportedOperationException(NOT_SUPPORTED_YET);
    }

    @Override
    public Object visit(ASTOrderBy node, Object data) {
        throw new UnsupportedOperationException(NOT_SUPPORTED_YET);
    }

    @Override
    public Object visit(ASTIdentifiers node, Object data) {
        throw new UnsupportedOperationException(NOT_SUPPORTED_YET);
    }

    @Override
    public Object visit(ASTPlainPaths node, Object data) {
        throw new UnsupportedOperationException(NOT_SUPPORTED_YET);
    }

    @Override
    public Object visit(ASTPlainPath node, Object data) {
        throw new UnsupportedOperationException(NOT_SUPPORTED_YET);
    }

    @Override
    public Object visit(ASTPathElement node, Object data) {
        throw new UnsupportedOperationException(NOT_SUPPORTED_YET);
    }

    @Override
    public Object visit(ASTFilteredPaths node, Object data) {
        throw new UnsupportedOperationException(NOT_SUPPORTED_YET);
    }

    @Override
    public Object visit(ASTFilteredPath node, Object data) {
        throw new UnsupportedOperationException(NOT_SUPPORTED_YET);
    }

    @Override
    public Object visit(ASTIdentifiedPaths node, Object data) {
        throw new UnsupportedOperationException(NOT_SUPPORTED_YET);
    }

    @Override
    public Object visit(ASTIdentifiedPath node, Object data) {
        throw new UnsupportedOperationException(NOT_SUPPORTED_YET);
    }

    @Override
    public Object visit(ASTFilter node, Object data) {
        throw new UnsupportedOperationException(NOT_SUPPORTED_YET);
    }

    @Override
    public Object visit(ASTLogicalOr node, Object data) {
        throw new UnsupportedOperationException(NOT_SUPPORTED_YET);
    }

    @Override
    public Object visit(ASTLogicalAnd node, Object data) {
        throw new UnsupportedOperationException(NOT_SUPPORTED_YET);
    }

    @Override
    public Object visit(ASTNot node, Object data) {
        throw new UnsupportedOperationException(NOT_SUPPORTED_YET);
    }

    @Override
    public Object visit(ASTBooleanFunction node, Object data) {
        throw new UnsupportedOperationException(NOT_SUPPORTED_YET);
    }

    @Override
    public Object visit(ASTComparison node, Object data) {
        throw new UnsupportedOperationException(NOT_SUPPORTED_YET);
    }

    @Override
    public Object visit(ASTPlusMin node, Object data) {
        throw new UnsupportedOperationException(NOT_SUPPORTED_YET);
    }

    @Override
    public Object visit(ASTOperator node, Object data) {
        throw new UnsupportedOperationException(NOT_SUPPORTED_YET);
    }

    @Override
    public Object visit(ASTMulDiv node, Object data) {
        throw new UnsupportedOperationException(NOT_SUPPORTED_YET);
    }

    @Override
    public Object visit(ASTFunction node, Object data) {
        throw new UnsupportedOperationException(NOT_SUPPORTED_YET);
    }

    @Override
    public Object visit(ASTValueNode node, Object data) {
        throw new UnsupportedOperationException(NOT_SUPPORTED_YET);
    }

    @Override
    public Object visit(ASTGeoStringLit node, Object data) {
        throw new UnsupportedOperationException(NOT_SUPPORTED_YET);
    }

    @Override
    public Object visit(ASTBool node, Object data) {
        throw new UnsupportedOperationException(NOT_SUPPORTED_YET);
    }

}
