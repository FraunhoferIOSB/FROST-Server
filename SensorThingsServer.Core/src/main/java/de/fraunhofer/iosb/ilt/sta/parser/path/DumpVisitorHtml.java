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
package de.fraunhofer.iosb.ilt.sta.parser.path;

import java.io.PrintWriter;

import de.fraunhofer.iosb.ilt.sta.path.ResourcePath;

public class DumpVisitorHtml extends DumpVisitor {

    private int addOl = 0;
    private final PrintWriter out;

    public DumpVisitorHtml(PrintWriter out) {
        this.out = out;
    }

    private String prependString() {
        StringBuilder sb = new StringBuilder();
        while (addOl > 0) {
            sb.append("<ul>");
            addOl--;
        }
        sb.append("<li>");
        return sb.toString();
    }

    private String appendString() {
        StringBuilder sb = new StringBuilder();
        while (addOl < 0) {
            sb.append("</ul>");
            addOl++;
        }
        sb.append("</li>");
        return sb.toString();
    }

    @Override
    public ResourcePath defltAction(SimpleNode node, ResourcePath data) {
        if (node.value == null) {
            out.println(prependString() + node);
        } else {
            out.println(prependString() + node.toString() + " : (" + node.value.getClass().getSimpleName() + ") " + node.value);
        }
        ++addOl;
        node.childrenAccept(this, data);
        --addOl;
        out.println(appendString());
        return data;
    }

    @Override
    public ResourcePath visit(ASTStart node, ResourcePath data) {
        out.println("<ul>");
        ResourcePath o = defltAction(node, data);
        out.println("</ul>");
        return o;
    }

    @Override
    public ResourcePath visit(SimpleNode node, ResourcePath data) {
        out.println(prependString() + node + ": acceptor not implemented in subclass?{}");
        ++addOl;
        node.childrenAccept(this, data);
        --addOl;
        out.println(appendString());
        return data;
    }

}
