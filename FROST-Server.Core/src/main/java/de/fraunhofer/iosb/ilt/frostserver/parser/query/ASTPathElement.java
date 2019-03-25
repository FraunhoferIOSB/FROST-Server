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
 * An ID.
 */
public class ASTPathElement extends SimpleNode {

    private String name;
    private String identifier;

    /**
     * Constructor.
     *
     * @param id the id
     */
    public ASTPathElement(int id) {
        super(id);
    }

    /**
     * Set the name.
     *
     * @param n the name
     */
    public void setName(String n) {
        name = n;
    }

    public void setIdentifier(String identifier) {
        if (identifier == null) {
            return;
        }
        this.identifier = identifier;
    }

    @Override
    public String toString() {
        if (identifier == null) {
            return "Element: " + name;
        }
        return "Element: " + name + " '" + identifier + "'";
    }

    @Override
    public Object jjtAccept(ParserVisitor visitor, Object data) {
        return visitor.visit(this, data);
    }

    public String getName() {
        return name;
    }

    public String getIdentifier() {
        return identifier;
    }

}
