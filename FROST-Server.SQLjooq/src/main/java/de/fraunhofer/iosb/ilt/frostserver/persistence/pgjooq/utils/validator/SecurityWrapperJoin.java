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
package de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.validator;

import static de.fraunhofer.iosb.ilt.frostserver.query.PrincipalExtended.USER_NAME_ANONYMOUS;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.fraunhofer.iosb.ilt.configurable.AnnotatedConfigurable;
import de.fraunhofer.iosb.ilt.configurable.annotations.ConfigurableClass;
import de.fraunhofer.iosb.ilt.configurable.annotations.ConfigurableField;
import de.fraunhofer.iosb.ilt.configurable.editor.EditorBoolean;
import de.fraunhofer.iosb.ilt.configurable.editor.EditorClass;
import de.fraunhofer.iosb.ilt.configurable.editor.EditorList;
import de.fraunhofer.iosb.ilt.configurable.editor.EditorString;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.StaMainTable;
import de.fraunhofer.iosb.ilt.frostserver.query.PrincipalExtended;
import java.security.Principal;
import java.util.List;
import org.jooq.Record;
import org.jooq.SQL;
import org.jooq.SelectJoinStep;
import org.jooq.Table;
import org.jooq.impl.DSL;

@ConfigurableClass
public class SecurityWrapperJoin implements SecurityTableWrapper {

    @ConfigurableClass
    public static class TableJoin implements AnnotatedConfigurable<Void, Void> {

        @ConfigurableField(editor = EditorString.class,
                label = "Target Table", description = "The name of the target table to join.")
        @EditorString.EdOptsString()
        public String targetTable;

        @ConfigurableField(editor = EditorString.class,
                label = "Target Table Alias", description = "The alias the target table gets when joining. Use this in the Join SQL.")
        @EditorString.EdOptsString()
        public String targetAlias;

        @ConfigurableField(editor = EditorString.class,
                label = "Join SQL", description = "The full SQL join.")
        @EditorString.EdOptsString()
        public String joinOnSql;

        @ConfigurableField(editor = EditorBoolean.class,
                label = "Username", description = "Flag indicating there is a parameter :username in the SQL.")
        @EditorBoolean.EdOptsBool()
        public boolean usernameParameter;

        @ConfigurableField(editor = EditorBoolean.class,
                label = "Groups", description = "Flag indicating there is a parameter :usergroups in the SQL.")
        @EditorBoolean.EdOptsBool()
        public boolean groupSetParameter;

        @JsonIgnore
        public Table<Record> aliassedTable;

        public Table<Record> getTargetTable() {
            if (aliassedTable == null) {
                if (targetAlias == null) {
                    aliassedTable = DSL.table(DSL.name(targetTable));
                } else {
                    aliassedTable = DSL.table(DSL.name(targetTable)).as(targetAlias);
                }
            }
            return aliassedTable;
        }
    }

    @ConfigurableField(editor = EditorList.class,
            label = "Joins", description = "The list of joins to apply.")
    @EditorList.EdOptsList(editor = EditorClass.class)
    @EditorClass.EdOptsClass(clazz = TableJoin.class)
    private List<TableJoin> joins;

    @Override
    public Table wrap(StaMainTable table) {
        final Principal principal = PrincipalExtended.getLocalPrincipal();
        final StaMainTable tableT = table.as("t");
        SelectJoinStep<Record> current = DSL.select(tableT.asterisk()).from(tableT);
        for (TableJoin join : joins) {
            SQL joinPart;
            if (join.usernameParameter) {
                final String name;
                if (principal == null) {
                    name = USER_NAME_ANONYMOUS;
                } else {
                    name = principal.getName();
                }
                joinPart = DSL.sql(join.joinOnSql, name);
            } else if (join.groupSetParameter) {
                final String[] groups;
                if (principal == null) {
                    groups = new String[]{USER_NAME_ANONYMOUS};
                } else {
                    groups = ((PrincipalExtended) principal).getRoles().toArray(String[]::new);
                }
                joinPart = DSL.sql(join.joinOnSql, (Object) groups);
            } else {
                joinPart = DSL.sql(join.joinOnSql);
            }
            current = current.join(join.getTargetTable()).on(joinPart);
        }
        return current.asTable();
    }

    public List<TableJoin> getJoins() {
        return joins;
    }

    public void setJoins(List<TableJoin> joins) {
        this.joins = joins;
    }

}
