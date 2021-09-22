/*
 * Copyright (C) 2021 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 */
package de.fraunhofer.iosb.ilt.frostserver.parser.query;

/**
 *
 * @author hylke
 */
public class ASTStringValueNode extends SimpleNode {

    public ASTStringValueNode(int id) {
        super(id);
    }

    public ASTStringValueNode(Parser p, int id) {
        super(p, id);
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getValue() {
        return (String) value;
    }

}
