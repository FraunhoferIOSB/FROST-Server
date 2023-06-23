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
package de.fraunhofer.iosb.ilt.frostserver.util;

import de.fraunhofer.iosb.ilt.configurable.annotations.ConfigurableClass;
import de.fraunhofer.iosb.ilt.configurable.annotations.ConfigurableField;
import de.fraunhofer.iosb.ilt.configurable.editor.EditorClass;
import de.fraunhofer.iosb.ilt.configurable.editor.EditorList;
import de.fraunhofer.iosb.ilt.configurable.editor.EditorString;
import de.fraunhofer.iosb.ilt.configurable.editor.EditorSubclass;
import java.util.List;

/**
 * A list of security definitions.
 *
 * @author hylke
 */
@ConfigurableClass
public class SecurityModel {

    @ConfigurableClass
    public static class SecurityEntry {

        @ConfigurableField(editor = EditorString.class,
                label = "Table Name", description = "The name of the table to apply this wrapper to.")
        @EditorString.EdOptsString()
        private String tableName;

        @ConfigurableField(editor = EditorList.class,
                label = "Wrappers", description = "The wrappers to apply to this table.")
        @EditorList.EdOptsList(editor = EditorSubclass.class)
        @EditorSubclass.EdOptsSubclass(iface = SecurityWrapper.class, merge = true, nameField = "@class")
        private List<SecurityWrapper> wrappers;

        public String getTableName() {
            return tableName;
        }

        public SecurityEntry setTableName(String tableName) {
            this.tableName = tableName;
            return this;
        }

        public List<SecurityWrapper> getWrappers() {
            return wrappers;
        }

        public SecurityEntry setWrappers(List<SecurityWrapper> wrappers) {
            this.wrappers = wrappers;
            return this;
        }

    }

    @ConfigurableField(editor = EditorList.class,
            label = "Definitions", description = "The security definitions")
    @EditorList.EdOptsList(editor = EditorClass.class)
    @EditorClass.EdOptsClass(clazz = SecurityEntry.class)
    private List<SecurityEntry> entries;

    public List<SecurityEntry> getEntries() {
        return entries;
    }

    public void setEntries(List<SecurityEntry> entries) {
        this.entries = entries;
    }

}
