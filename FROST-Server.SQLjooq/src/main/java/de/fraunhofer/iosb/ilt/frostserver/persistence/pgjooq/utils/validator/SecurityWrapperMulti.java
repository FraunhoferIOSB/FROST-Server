/*
 * Copyright (C) 2024 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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
package de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.validator;

import de.fraunhofer.iosb.ilt.configurable.AnnotatedConfigurable;
import de.fraunhofer.iosb.ilt.configurable.annotations.ConfigurableClass;
import de.fraunhofer.iosb.ilt.configurable.annotations.ConfigurableField;
import de.fraunhofer.iosb.ilt.configurable.editor.EditorClass;
import de.fraunhofer.iosb.ilt.configurable.editor.EditorList;
import de.fraunhofer.iosb.ilt.configurable.editor.EditorSubclass;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.JooqPersistenceManager;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.StaMainTable;
import java.util.List;
import org.jooq.Table;

@ConfigurableClass
public class SecurityWrapperMulti implements SecurityTableWrapper {

    @ConfigurableClass
    public static class IfConditionThenWrapper implements AnnotatedConfigurable<Void, Void> {

        @ConfigurableField(editor = EditorSubclass.class, label = "If", description = "The condition in which to apply the Wrapper")
        @EditorSubclass.EdOptsSubclass(iface = UserCondition.class, merge = true, nameField = "@class", shortenClassNames = true)
        private UserCondition condition;

        @ConfigurableField(editor = EditorSubclass.class, label = "Then", description = "The wrapper to use of the condition is valid")
        @EditorSubclass.EdOptsSubclass(iface = SecurityTableWrapper.class, merge = true, nameField = "@class", shortenClassNames = true)
        private SecurityTableWrapper wrapper;

        public UserCondition getCondition() {
            return condition;
        }

        public IfConditionThenWrapper setCondition(UserCondition condition) {
            this.condition = condition;
            return this;
        }

        public SecurityTableWrapper getWrapper() {
            return wrapper;
        }

        public IfConditionThenWrapper setWrapper(SecurityTableWrapper wrapper) {
            this.wrapper = wrapper;
            return this;
        }
    }

    @ConfigurableField(editor = EditorList.class, label = "Wrappers", description = "The wrappers and conditions in which to apply them.")
    @EditorList.EdOptsList(editor = EditorClass.class)
    @EditorClass.EdOptsClass(clazz = IfConditionThenWrapper.class)
    private List<IfConditionThenWrapper> wrappers;

    @Override
    public Table wrap(StaMainTable table, JooqPersistenceManager pm) {
        for (IfConditionThenWrapper ifThen : wrappers) {
            if (ifThen.getCondition().isValid(pm)) {
                return ifThen.getWrapper().wrap(table, pm);
            }
        }
        return table;
    }

    public List<IfConditionThenWrapper> getWrappers() {
        return wrappers;
    }

    public SecurityWrapperMulti setWrappers(List<IfConditionThenWrapper> wrappers) {
        this.wrappers = wrappers;
        return this;
    }

}
