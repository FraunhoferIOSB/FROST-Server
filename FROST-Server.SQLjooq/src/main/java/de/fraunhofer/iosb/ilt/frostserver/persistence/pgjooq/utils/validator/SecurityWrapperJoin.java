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

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.fraunhofer.iosb.ilt.configurable.AnnotatedConfigurable;
import de.fraunhofer.iosb.ilt.configurable.annotations.ConfigurableClass;
import de.fraunhofer.iosb.ilt.configurable.annotations.ConfigurableField;
import de.fraunhofer.iosb.ilt.configurable.editor.EditorBoolean;
import de.fraunhofer.iosb.ilt.configurable.editor.EditorClass;
import de.fraunhofer.iosb.ilt.configurable.editor.EditorEnum;
import de.fraunhofer.iosb.ilt.configurable.editor.EditorList;
import de.fraunhofer.iosb.ilt.configurable.editor.EditorString;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.JooqPersistenceManager;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.StaMainTable;
import de.fraunhofer.iosb.ilt.frostserver.util.StringHelper;
import de.fraunhofer.iosb.ilt.frostserver.util.user.PrincipalExtended;
import java.security.Principal;
import java.util.List;
import org.jooq.Record;
import org.jooq.Record1;
import org.jooq.SQL;
import org.jooq.SelectConditionStep;
import org.jooq.SelectJoinStep;
import org.jooq.Table;
import org.jooq.impl.DSL;

@ConfigurableClass
public class SecurityWrapperJoin implements SecurityTableWrapper {

    @ConfigurableClass
    public static class TableJoin implements AnnotatedConfigurable<Void, Void> {

        public enum JoinType {
            INNER,
            LEFT,
            RIGHT
        }

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

        @ConfigurableField(editor = EditorBoolean.class, optional = true,
                label = "Username", description = "Flag indicating there is a parameter :username in the SQL.")
        @EditorBoolean.EdOptsBool()
        public boolean usernameParameter;

        @ConfigurableField(editor = EditorBoolean.class, optional = true,
                label = "Groups", description = "Flag indicating there is a parameter :usergroups in the SQL.")
        @EditorBoolean.EdOptsBool()
        public boolean groupSetParameter;

        @ConfigurableField(editor = EditorEnum.class,
                label = "Join Type", description = "The type of join to use.")
        @EditorEnum.EdOptsEnum(sourceType = JoinType.class, dflt = "INNER")
        public JoinType joinType = JoinType.INNER;

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
            label = "Joins", description = "The list of joins to apply. The main table is aliassed to 't'.")
    @EditorList.EdOptsList(editor = EditorClass.class)
    @EditorClass.EdOptsClass(clazz = TableJoin.class)
    private List<TableJoin> joins;

    @ConfigurableField(editor = EditorString.class, optional = true,
            label = "Where", description = "A final where for the query. The main table is aliassed to 't'.")
    @EditorString.EdOptsString()
    private String where;

    @ConfigurableField(editor = EditorBoolean.class, optional = true,
            label = "Username", description = "Flag indicating there is a parameter ? in the SQL that should be replaced with the principal.")
    @EditorBoolean.EdOptsBool()
    private boolean usernameParameter;

    @ConfigurableField(editor = EditorBoolean.class, optional = true,
            label = "Groups", description = "Flag indicating there is a parameter ? in the SQL that should be replaces with the group-set.")
    @EditorBoolean.EdOptsBool()
    private boolean groupSetParameter;

    @Override
    public Table wrap(StaMainTable table, JooqPersistenceManager pm) {
        final Principal principal = PrincipalExtended.getLocalPrincipal();
        final StaMainTable tableIn = table.as("t");
        SelectJoinStep<Record1<Integer>> exists = DSL.select(DSL.one()).from(tableIn);
        for (TableJoin join : joins) {
            SQL joinPart;
            if (join.usernameParameter) {
                joinPart = DSL.sql(join.joinOnSql, principal.getName());
            } else if (join.groupSetParameter) {
                final String[] groups;
                groups = ((PrincipalExtended) principal).getRoles().toArray(String[]::new);
                joinPart = DSL.sql(join.joinOnSql, (Object) groups);
            } else {
                joinPart = DSL.sql(join.joinOnSql);
            }
            switch (join.joinType) {
                case LEFT:
                    exists = exists.leftJoin(join.getTargetTable()).on(joinPart);
                    break;

                case RIGHT:
                    exists = exists.rightJoin(join.getTargetTable()).on(joinPart);
                    break;

                case INNER:
                default:
                    exists = exists.innerJoin(join.getTargetTable()).on(joinPart);
                    break;
            }
        }
        if (!StringHelper.isNullOrEmpty(where)) {
            if (usernameParameter) {
                SelectConditionStep<Record1<Integer>> finalExists = exists.where(table.getId().eq(tableIn.getId())).and(where, principal.getName());
                return table.whereExists(finalExists).asTable("tOut");
            }
            SelectConditionStep<Record1<Integer>> finalExists = exists.where(table.getId().eq(tableIn.getId())).and(where);
            return table.whereExists(finalExists).asTable("tOut");
        }

        SelectConditionStep<Record1<Integer>> finalExists = exists.where(table.getId().eq(tableIn.getId()));
        return table.whereExists(finalExists).asTable("tOut");
    }

    public List<TableJoin> getJoins() {
        return joins;
    }

    public SecurityWrapperJoin setJoins(List<TableJoin> joins) {
        this.joins = joins;
        return this;
    }

    public String getWhere() {
        return where;
    }

    public SecurityWrapperJoin setWhere(String where) {
        this.where = where;
        return this;
    }

    public boolean isUsernameParameter() {
        return usernameParameter;
    }

    public SecurityWrapperJoin setUsernameParameter(boolean usernameParameter) {
        this.usernameParameter = usernameParameter;
        return this;
    }

    public boolean isGroupSetParameter() {
        return groupSetParameter;
    }

    public SecurityWrapperJoin setGroupSetParameter(boolean groupSetParameter) {
        this.groupSetParameter = groupSetParameter;
        return this;
    }

}
