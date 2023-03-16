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

    public static final String USER_NAME_ANONYMOUS = "anonymous";

    @ConfigurableClass
    public static class SecurityEntry {

        @ConfigurableField(editor = EditorString.class,
                label = "Table Name", description = "The name of the table to apply this wrapper to.")
        @EditorString.EdOptsString()
        private String tableName;

        @ConfigurableField(editor = EditorSubclass.class,
                label = "Wrapper", description = "The wrapper to apply to this table.")
        @EditorSubclass.EdOptsSubclass(iface = SecurityWrapper.class, merge = true, nameField = "@class")
        private SecurityWrapper wrapper;

        public String getTableName() {
            return tableName;
        }

        public void setTableName(String tableName) {
            this.tableName = tableName;
        }

        public SecurityWrapper getWrapper() {
            return wrapper;
        }

        public void setWrapper(SecurityWrapper wrapper) {
            this.wrapper = wrapper;
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
