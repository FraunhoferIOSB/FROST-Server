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
package de.fraunhofer.iosb.ilt.frostserver.modeleditor;

import de.fraunhofer.iosb.ilt.frostserver.model.loader.DefEntityProperty;
import de.fraunhofer.iosb.ilt.frostserver.model.loader.DefModel;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.fieldmapper.FieldMapper;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.fieldmapper.FieldMapperId;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.fieldmapper.FieldMapperManyToMany;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.fieldmapper.FieldMapperManyToManyOrdered;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.fieldmapper.FieldMapperOneToMany;
import de.fraunhofer.iosb.ilt.frostserver.util.StringHelper;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.CaseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hylke
 */
public class LiquibaseTemplates {

    private static final Logger LOGGER = LoggerFactory.getLogger(LiquibaseTemplates.class.getName());

    public static final String VAR_NAME_BLOCK_CHANGESETS = "BLOCK_CHANGESETS";
    public static final String VAR_NAME_BLOCK_COLUMNS = "BLOCK_COLUMNS";
    public static final String VAR_NAME_BLOCK_COPYRIGHT = "BLOCK_COPYRIGHT";
    public static final String VAR_NAME_BLOCK_PROPERTIES = "BLOCK_PROPERTIES";
    public static final String VAR_NAME_BLOCK_INCLUDES = "BLOCK_INCLUDES";
    public static final String VAR_NAME_CHANGELOG_AUTHOR = "CHANGELOG_AUTHOR";
    public static final String VAR_NAME_CHANGELOG_DATE = "CHANGELOG_DATE";
    public static final String VAR_NAME_COLUMN_NAME = "COLUMN_NAME";
    public static final String VAR_NAME_COLUMN_NAME_1 = "COLUMN_NAME_1";
    public static final String VAR_NAME_COLUMN_NAME_2 = "COLUMN_NAME_2";
    public static final String VAR_NAME_COLUMN_NAME_OTHER = "COLUMN_NAME_OTHER";
    public static final String VAR_NAME_COLUMN_TYPE = "COLUMN_TYPE";
    public static final String VAR_NAME_COLUMN_TYPE_1 = "COLUMN_TYPE_1";
    public static final String VAR_NAME_COLUMN_TYPE_2 = "COLUMN_TYPE_2";
    public static final String VAR_NAME_ENTITY_NAME = "ENTITY_NAME";
    public static final String VAR_NAME_TABLE_NAME = "TABLE_NAME";
    public static final String VAR_NAME_TABLE_NAME_OTHER = "TABLE_NAME_OTHER";
    public static final String VAR_NAME_TESTCOLUMN_NAME = "TEST_COLUMN_NAME";

    private static final String S_NAME_BLOCK_CHANGESETS = "§{" + VAR_NAME_BLOCK_CHANGESETS + '}';
    private static final String S_NAME_BLOCK_COLUMNS = "§{" + VAR_NAME_BLOCK_COLUMNS + '}';
    private static final String S_NAME_BLOCK_COPYRIGHT = "§{" + VAR_NAME_BLOCK_COPYRIGHT + '}';
    private static final String S_NAME_BLOCK_PROPERTIES = "§{" + VAR_NAME_BLOCK_PROPERTIES + '}';
    private static final String S_NAME_BLOCK_INCLUDES = "§{" + VAR_NAME_BLOCK_INCLUDES + '}';
    private static final String S_NAME_CHANGELOG_AUTHOR = "§{" + VAR_NAME_CHANGELOG_AUTHOR + '}';
    private static final String S_NAME_CHANGELOG_DATE = "§{" + VAR_NAME_CHANGELOG_DATE + '}';
    private static final String S_NAME_COLUMN_NAME = "§{" + VAR_NAME_COLUMN_NAME + '}';
    private static final String S_NAME_COLUMN_NAME_1 = "§{" + VAR_NAME_COLUMN_NAME_1 + '}';
    private static final String S_NAME_COLUMN_NAME_2 = "§{" + VAR_NAME_COLUMN_NAME_2 + '}';
    private static final String S_NAME_COLUMN_NAME_OTHER = "§{" + VAR_NAME_COLUMN_NAME_OTHER + '}';
    private static final String S_NAME_COLUMN_TYPE = "§{" + VAR_NAME_COLUMN_TYPE + '}';
    private static final String S_NAME_COLUMN_TYPE_1 = "§{" + VAR_NAME_COLUMN_TYPE_1 + '}';
    private static final String S_NAME_COLUMN_TYPE_2 = "§{" + VAR_NAME_COLUMN_TYPE_2 + '}';
    private static final String S_NAME_ENTITY_NAME = "§{" + VAR_NAME_ENTITY_NAME + '}';
    private static final String S_NAME_TABLE_NAME = "§{" + VAR_NAME_TABLE_NAME + '}';
    private static final String S_NAME_TABLE_NAME_OTHER = "§{" + VAR_NAME_TABLE_NAME_OTHER + '}';
    private static final String S_NAME_TESTCOLUMN_NAME = "§{" + VAR_NAME_TESTCOLUMN_NAME + '}';

    public static List<ChangeLogBuilder> CreateChangeLogsFor(List<DefModel> models, String date, String author) {
        List<ChangeLogBuilder> clBuilders = new ArrayList<>();
        if (StringHelper.isNullOrEmpty(date)) {
            date = DateTimeFormatter.ISO_LOCAL_DATE.format(ZonedDateTime.now());
        }
        TableChangelogBuilder clForeignKeys = TableChangelogBuilder.start(date)
                .setAuthor(author)
                .setFileName("foreignKeys.xml");
        MainChangeLogBuilder clMain = MainChangeLogBuilder.start();
        for (var model : models) {
            createChangelogFor(model, clMain, date, author, clBuilders, clForeignKeys);
        }
        if (!clForeignKeys.isEmpty()) {
            clBuilders.add(clForeignKeys);
            clMain.addFile(clForeignKeys.getFileName());
        }
        clBuilders.add(clMain);
        return clBuilders;
    }

    private static void createChangelogFor(DefModel model, MainChangeLogBuilder clMain, String date, String author, List<ChangeLogBuilder> clBuilders, TableChangelogBuilder clForeignKeys) {
        for (var et : model.getEntityTypes()) {
            final String etName = et.getName();
            final String tableName = et.getTable();
            final String fileName = "table" + et.getPlural() + ".xml";
            clMain.addFile(fileName);
            clMain.addPropertyIdType(etName);
            TableChangelogBuilder clEntityType = TableChangelogBuilder.start(date, tableName)
                    .setAuthor(author)
                    .setFileName(fileName);
            clBuilders.add(clEntityType);
            ChangesetColumnsBuilder csColumns = ChangesetColumnsBuilder.start();
            String idField = null;
            for (var ep : et.getEntityProperties()) {
                idField = handleEntityProperty(ep, idField, clEntityType, etName, csColumns);
            }
            clEntityType.addChangesetColumnsBuilder(csColumns);

            for (var ep : et.getNavigationProperties()) {
                String otherEntityType = ep.getEntityType();
                for (var handler : ep.getHandlers()) {
                    if (handler instanceof FieldMapperOneToMany fm) {
                        String ourColumn = fm.getField();
                        String otherColumn = fm.getOtherField();
                        String otherTable = fm.getOtherTable();
                        if (ourColumn.equals(idField)) {
                            clEntityType.addChangesetColumnsBuilder(
                                    ChangesetColumnsBuilder.start()
                                            .setTableName(otherTable)
                                            .setTestColumnName(otherColumn)
                                            .prependColumn(otherColumn, idColumnType(etName), !ep.getInverse().isRequired()));
                            clForeignKeys.addChangsetForeignKey(otherTable, otherColumn, tableName, ourColumn);
                            clEntityType.addChangsetIndex(otherTable, otherColumn);
                        } else {
                            csColumns.prependColumn(ourColumn, idColumnType(otherEntityType), !ep.isRequired());
                            if (!csColumns.isTestColumnNameSet()) {
                                csColumns.setTestColumnName(ourColumn);
                            }
                            clForeignKeys.addChangsetForeignKey(tableName, ourColumn, otherTable, otherColumn);
                            clEntityType.addChangsetIndex(ourColumn);
                        }
                    } else if (handler instanceof FieldMapperManyToMany fm) {
                        String ourField = fm.getField();
                        String ourType = idColumnType(etName);
                        String otherType = idColumnType(otherEntityType);
                        String linkTableOurField = fm.getLinkOurField();
                        String linkTable = fm.getLinkTable();
                        String linkTableOtherField = fm.getLinkOtherField();
                        String otherField = fm.getOtherField();
                        String otherTable = fm.getOtherTable();
                        TableChangelogBuilder clLinkTable = TableChangelogBuilder.start(date)
                                .setAuthor(author)
                                .setFileName("table" + CaseUtils.toCamelCase(linkTable, true, '_') + ".xml")
                                .addChangsetLinkTable(linkTable, linkTableOurField, ourType, linkTableOtherField, otherType);
                        clBuilders.add(clLinkTable);
                        clForeignKeys.addChangsetForeignKey(linkTable, linkTableOurField, tableName, ourField);
                        clForeignKeys.addChangsetForeignKey(linkTable, linkTableOtherField, otherTable, otherField);
                        clMain.addFile(clLinkTable.getFileName());
                    } else if (handler instanceof FieldMapperManyToManyOrdered fm) {
                        LOGGER.warn("Unknown Handler Type: {}", handler);
                    }
                }
            }
        }
    }

    private static String handleEntityProperty(DefEntityProperty ep, String idField, TableChangelogBuilder clEntityType, final String etName, ChangesetColumnsBuilder csColumns) {
        for (var handler : ep.getHandlers()) {
            if (handler instanceof FieldMapperId fm) {
                idField = fm.getField();
                clEntityType.addChangeLogsIds(idField, etName);
            } else if (handler instanceof FieldMapper fm) {
                for (var entry : fm.getFieldTypes().entrySet()) {
                    final String colName = entry.getKey();
                    final String colType = entry.getValue();
                    csColumns.addColumn(colName, colType, true);
                    if (!csColumns.isTestColumnNameSet()) {
                        csColumns.setTestColumnName(colName);
                    }
                }
            } else {
                LOGGER.warn("Unknown Handler Type: {}", handler);
            }
        }
        return idField;
    }

    public static interface ChangeLogBuilder {

        public String getFileName();

        public String build();
    }

    public static interface ChangeSetBuilder {

        public boolean isEmpty();

        public String build();
    }

    public static class WrappedSetBuilder implements ChangeSetBuilder {

        private final String data;

        public WrappedSetBuilder(String data) {
            this.data = data;
        }

        @Override
        public boolean isEmpty() {
            return StringUtils.isAllBlank(data);
        }

        @Override
        public String build() {
            return data;
        }

    }

    public static class MainChangeLogBuilder implements ChangeLogBuilder {

        private String fileName = "tables.xml";
        private final List<String> files = new ArrayList<>();
        private final StringBuilder properties = new StringBuilder();

        public static MainChangeLogBuilder start() {
            return new MainChangeLogBuilder();
        }

        public static MainChangeLogBuilder start(String fileName) {
            return new MainChangeLogBuilder()
                    .setFileName(fileName);
        }

        public void addFile(String fileName) {
            files.add(fileName);
        }

        public void addPropertyIdType(String entityTypeName) {
            properties.append("    <property name=\"id-").append(entityTypeName).append("\" value=\"LONG\" />\n");
            properties.append("    <property name=\"idType-").append(entityTypeName).append("\" value=\"BIGINT\" />\n");
        }

        @Override
        public String build() {
            StringBuilder includes = new StringBuilder();
            for (String file : files) {
                includes.append("    <include relativeToChangelogFile=\"true\" file=\"").append(file).append("\" />\n");
            }
            String[] searchList = new String[]{
                S_NAME_BLOCK_COPYRIGHT,
                S_NAME_BLOCK_PROPERTIES,
                S_NAME_BLOCK_INCLUDES
            };
            String[] replacementList = new String[]{
                BLOCK_COPYRIGHT.trim(),
                properties.toString().trim(),
                includes.toString().trim()
            };
            return StringUtils.replaceEach(BLOCK_MAIN_FILE, searchList, replacementList);
        }

        @Override
        public String getFileName() {
            return fileName;
        }

        public MainChangeLogBuilder setFileName(String fileName) {
            this.fileName = fileName;
            return this;
        }

    }

    public static class TableChangelogBuilder implements ChangeLogBuilder {

        private final List<ChangeSetBuilder> changesets = new ArrayList<>();
        private final String base;
        private final String date;
        private final String tableName;
        private String author = "generated";
        private String fileName;

        private TableChangelogBuilder(String date, String tableName) {
            this.date = date;
            this.tableName = tableName;
            String[] searchList = new String[]{
                S_NAME_BLOCK_COPYRIGHT
            };
            String[] replacementList = new String[]{
                BLOCK_COPYRIGHT.trim()
            };
            base = StringUtils.replaceEach(BLOCK_CHANGLOG, searchList, replacementList);
        }

        public static TableChangelogBuilder start(String date) {
            return new TableChangelogBuilder(date, null);
        }

        public static TableChangelogBuilder start(String date, String tableName) {
            return new TableChangelogBuilder(date, tableName);
        }

        @Override
        public String getFileName() {
            return fileName;
        }

        public TableChangelogBuilder setAuthor(String author) {
            this.author = author;
            return this;
        }

        public TableChangelogBuilder setFileName(String fileName) {
            this.fileName = fileName;
            return this;
        }

        public TableChangelogBuilder addChangeLogsIds(String entityName) {
            if (StringHelper.isNullOrEmpty(tableName)) {
                throw new IllegalArgumentException("Table must be set!");
            }
            return addChangeLogsIds(tableName, "id", entityName);
        }

        public TableChangelogBuilder addChangeLogsIds(String columnName, String entityName) {
            if (StringHelper.isNullOrEmpty(tableName)) {
                throw new IllegalArgumentException("Table must be set!");
            }
            return addChangeLogsIds(tableName, columnName, entityName);
        }

        public TableChangelogBuilder addChangeLogsIds(String tableName, String columnName, String entityName) {
            addChangeset(createChangsetLongId(author, date, tableName, columnName, entityName));
            addChangeset(createChangsetStringId(author, date, tableName, columnName, entityName));
            return this;
        }

        public TableChangelogBuilder addChangesetColumnsBuilder(ChangesetColumnsBuilder builder) {
            builder.setAuthor(author);
            builder.setDate(date);
            if (!builder.isTableNameSet()) {
                builder.setTableName(tableName);
            }
            return addChangeset(builder);
        }

        public TableChangelogBuilder addChangsetIndex(String columnName) {
            return addChangsetIndex(tableName, columnName);
        }

        public TableChangelogBuilder addChangsetIndex(String tableName, String columnName) {
            return addChangeset(createChangsetIndex(author, date, tableName, columnName));
        }

        public TableChangelogBuilder addChangsetLinkTable(String columnName1, String columnType1, String columnName2, String columnType2) {
            return addChangsetLinkTable(tableName, columnName1, columnType1, columnName2, columnType2);
        }

        public TableChangelogBuilder addChangsetLinkTable(String tableName, String columnName1, String columnType1, String columnName2, String columnType2) {
            return addChangeset(createChangsetLinkTable(author, date, tableName, columnName1, columnType1, columnName2, columnType2));
        }

        public TableChangelogBuilder addChangsetForeignKey(String columnName, String otherTableName, String otherColumnName) {
            return addChangsetForeignKey(tableName, columnName, otherTableName, otherColumnName);
        }

        public TableChangelogBuilder addChangsetForeignKey(String tableName, String columnName, String otherTableName, String otherColumnName) {
            return addChangeset(createChangsetForeignKey(author, date, tableName, columnName, otherTableName, otherColumnName));
        }

        public TableChangelogBuilder addChangeset(ChangeSetBuilder changeset) {
            changesets.add(changeset);
            return this;
        }

        private TableChangelogBuilder addChangeset(String changeset) {
            changesets.add(new WrappedSetBuilder(changeset));
            return this;
        }

        public boolean isEmpty() {
            return changesets.isEmpty();
        }

        @Override
        public String build() {
            StringBuilder changeSetString = new StringBuilder();
            for (var cs : changesets) {
                if (!cs.isEmpty()) {
                    changeSetString.append(cs.build()).append('\n');
                }
            }
            return StringUtils.replace(base, S_NAME_BLOCK_CHANGESETS, changeSetString.toString().trim());
        }
    }

    public static String createChangsetLongId(String author, String date, String tableName, String columnName, String entityName) {
        String[] searchList = new String[]{
            S_NAME_CHANGELOG_AUTHOR,
            S_NAME_CHANGELOG_DATE,
            S_NAME_TABLE_NAME,
            S_NAME_COLUMN_NAME,
            S_NAME_ENTITY_NAME
        };
        String[] replacementList = new String[]{
            author,
            date,
            tableName,
            columnName,
            entityName
        };
        return StringUtils.replaceEach(BLOCK_CHANGESET_IDLONG, searchList, replacementList);
    }

    public static String createChangsetStringId(String author, String date, String tableName, String columnName, String entityName) {
        String[] searchList = new String[]{
            S_NAME_CHANGELOG_AUTHOR,
            S_NAME_CHANGELOG_DATE,
            S_NAME_TABLE_NAME,
            S_NAME_COLUMN_NAME,
            S_NAME_ENTITY_NAME
        };
        String[] replacementList = new String[]{
            author,
            date,
            tableName,
            columnName,
            entityName
        };
        return StringUtils.replaceEach(BLOCK_CHANGESET_IDSTRING, searchList, replacementList);
    }

    public static class ChangesetColumnsBuilder implements ChangeSetBuilder {

        private final StringBuilder preColumns = new StringBuilder();
        private final StringBuilder columns = new StringBuilder();
        String author;
        String date;
        String tableName;
        String testColumnName;

        public static ChangesetColumnsBuilder start() {
            return new ChangesetColumnsBuilder(null, null, null);
        }

        public static ChangesetColumnsBuilder start(String date, String tableName, String testColumnName) {
            return new ChangesetColumnsBuilder(date, tableName, testColumnName);
        }

        private ChangesetColumnsBuilder(String date, String tableName, String testColumnName) {
            this.date = date;
            this.tableName = tableName;
            this.testColumnName = testColumnName;
        }

        public ChangesetColumnsBuilder setAuthor(String author) {
            this.author = author;
            return this;
        }

        public ChangesetColumnsBuilder setDate(String date) {
            this.date = date;
            return this;
        }

        public boolean isTestColumnNameSet() {
            return !StringHelper.isNullOrEmpty(testColumnName);
        }

        public ChangesetColumnsBuilder setTestColumnName(String testColumnName) {
            this.testColumnName = testColumnName;
            return this;
        }

        public boolean isTableNameSet() {
            return !StringHelper.isNullOrEmpty(tableName);
        }

        public ChangesetColumnsBuilder setTableName(String tableName) {
            this.tableName = tableName;
            return this;
        }

        public ChangesetColumnsBuilder prependColumn(String columnName, String columnType, boolean nullable) {
            preColumns.append(createBlockColumn(columnName, columnType, nullable));
            return this;
        }

        public ChangesetColumnsBuilder addColumn(String columnName, String columnType, boolean nullable) {
            columns.append(createBlockColumn(columnName, columnType, nullable));
            return this;
        }

        public boolean isEmpty() {
            return preColumns.isEmpty() && columns.isEmpty();
        }

        @Override
        public String build() {
            String data = preColumns.toString() + columns.toString();
            if (StringHelper.isNullOrEmpty(date)) {
                throw new IllegalArgumentException("Date must be set!");
            }
            if (StringHelper.isNullOrEmpty(tableName)) {
                throw new IllegalArgumentException("Table must be set!");
            }
            String[] searchList = new String[]{
                S_NAME_CHANGELOG_AUTHOR,
                S_NAME_CHANGELOG_DATE,
                S_NAME_TABLE_NAME,
                S_NAME_TESTCOLUMN_NAME,
                S_NAME_BLOCK_COLUMNS
            };
            String[] replacementList = new String[]{
                author,
                date,
                tableName,
                testColumnName,
                data.trim()
            };
            return StringUtils.replaceEach(BLOCK_CHANGESET_NORMAL_COLUMNS, searchList, replacementList);
        }
    }

    public static String createBlockColumn(String columnName, String columnType, boolean nullable) {
        if (nullable) {
            return createBlockColumn(columnName, columnType);
        } else {
            return createBlockColumnNonNull(columnName, columnType);
        }
    }

    public static String createBlockColumn(String columnName, String columnType) {
        String[] searchList = new String[]{
            S_NAME_COLUMN_NAME,
            S_NAME_COLUMN_TYPE
        };
        String[] replacementList = new String[]{
            columnName,
            columnType
        };
        return StringUtils.replaceEach(BLOCK_CHANGESET_COLUMN, searchList, replacementList);
    }

    public static String createBlockColumnNonNull(String columnName, String columnType) {
        String[] searchList = new String[]{
            S_NAME_COLUMN_NAME,
            S_NAME_COLUMN_TYPE
        };
        String[] replacementList = new String[]{
            columnName,
            columnType
        };
        return StringUtils.replaceEach(BLOCK_CHANGESET_COLUMN_NONNULL, searchList, replacementList);
    }

    public static String createChangsetIndex(String author, String date, String tableName, String columnName) {
        String[] searchList = new String[]{
            S_NAME_CHANGELOG_AUTHOR,
            S_NAME_CHANGELOG_DATE,
            S_NAME_TABLE_NAME,
            S_NAME_COLUMN_NAME
        };
        String[] replacementList = new String[]{
            author,
            date,
            tableName,
            columnName
        };
        return StringUtils.replaceEach(BLOCK_CHANGESET_INDEX, searchList, replacementList);
    }

    public static String createChangsetLinkTable(String author, String date, String tableName, String columnName1, String columnType1, String columnName2, String columnType2) {
        String[] searchList = new String[]{
            S_NAME_CHANGELOG_AUTHOR,
            S_NAME_CHANGELOG_DATE,
            S_NAME_TABLE_NAME,
            S_NAME_COLUMN_NAME_1,
            S_NAME_COLUMN_TYPE_1,
            S_NAME_COLUMN_NAME_2,
            S_NAME_COLUMN_TYPE_2
        };
        String[] replacementList = new String[]{
            author,
            date,
            tableName,
            columnName1,
            columnType1,
            columnName2,
            columnType2
        };
        return StringUtils.replaceEach(BLOCK_CHANGESET_LINKTABLE, searchList, replacementList);
    }

    public static String createChangsetForeignKey(String author, String date, String tableName, String columnName, String otherTableName, String otherColumnName) {
        String[] searchList = new String[]{
            S_NAME_CHANGELOG_AUTHOR,
            S_NAME_CHANGELOG_DATE,
            S_NAME_TABLE_NAME,
            S_NAME_COLUMN_NAME,
            S_NAME_TABLE_NAME_OTHER,
            S_NAME_COLUMN_NAME_OTHER
        };
        String[] replacementList = new String[]{
            author,
            date,
            tableName,
            columnName,
            otherTableName,
            otherColumnName
        };
        return StringUtils.replaceEach(BLOCK_CHANGESET_FKEY, searchList, replacementList);
    }

    public static String idColumnType(String entityType) {
        return "${idType-" + entityType + "}";
    }

    public static final String BLOCK_COPYRIGHT = """
                <!--
                 Copyright (C) 2024 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
                 Karlsruhe, Germany.

                 This program is free software: you can redistribute it and/or modify
                 it under the terms of the GNU Lesser General Public License as published by
                 the Free Software Foundation, either version 3 of the License, or
                 (at your option) any later version.

                 This program is distributed in the hope that it will be useful,
                 but WITHOUT ANY WARRANTY; without even the implied warranty of
                 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
                 GNU Lesser General Public License for more details.

                 You should have received a copy of the GNU Lesser General Public License
                 along with this program.  If not, see <http://www.gnu.org/licenses/>.
                -->
            """;

    public static final String BLOCK_CHANGLOG = """
            <?xml version="1.1" encoding="UTF-8"?>
            <databaseChangeLog
                xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-3.1.xsd">
                §{BLOCK_COPYRIGHT}

                §{BLOCK_CHANGESETS}

            </databaseChangeLog>

            """;

    public static final String BLOCK_CHANGESET_IDLONG = """
                <changeSet author="§{CHANGELOG_AUTHOR}" id="§{CHANGELOG_DATE}-§{TABLE_NAME}-1" objectQuotingStrategy="QUOTE_ALL_OBJECTS">
                    <preConditions onFail="MARK_RAN">
                        <changeLogPropertyDefined property="id-§{ENTITY_NAME}" value="LONG" />
                        <not>
                            <tableExists tableName="§{TABLE_NAME}" />
                        </not>
                    </preConditions>
                    <createTable tableName="§{TABLE_NAME}">
                        <column name="§{COLUMN_NAME}" type="${idTypeLong}" autoIncrement="true">
                            <constraints primaryKey="true" primaryKeyName="pk_§{TABLE_NAME}" />
                        </column>
                    </createTable>
                </changeSet>
            """;

    public static final String BLOCK_CHANGESET_IDSTRING = """
                <changeSet author="§{CHANGELOG_AUTHOR}" id="§{CHANGELOG_DATE}-§{TABLE_NAME}-2" objectQuotingStrategy="QUOTE_ALL_OBJECTS">
                    <preConditions onFail="MARK_RAN">
                        <or>
                            <changeLogPropertyDefined property="id-§{ENTITY_NAME}" value="STRING" />
                            <changeLogPropertyDefined property="id-§{ENTITY_NAME}" value="UUID" />
                        </or>
                        <not>
                            <tableExists tableName="§{TABLE_NAME}" />
                        </not>
                    </preConditions>
                    <createTable tableName="§{TABLE_NAME}">
                        <column name="§{COLUMN_NAME}" type="${idType-§{ENTITY_NAME}}" defaultValueComputed="${defaultValueComputed-§{ENTITY_NAME}}">
                            <constraints primaryKey="true" primaryKeyName="pk_§{TABLE_NAME}"/>
                        </column>
                    </createTable>
                </changeSet>
            """;

    public static final String BLOCK_CHANGESET_NORMAL_COLUMNS = """
                <changeSet author="§{CHANGELOG_AUTHOR}" id="§{CHANGELOG_DATE}-§{TABLE_NAME}-3" objectQuotingStrategy="QUOTE_ALL_OBJECTS">
                    <preConditions onFail="MARK_RAN">
                        <not>
                            <columnExists columnName="§{TEST_COLUMN_NAME}" tableName="§{TABLE_NAME}" />
                        </not>
                    </preConditions>
                    <addColumn tableName="§{TABLE_NAME}">
                        §{BLOCK_COLUMNS}
                    </addColumn>
                </changeSet>
            """;

    public static final String BLOCK_CHANGESET_COLUMN = """
                        <column name="§{COLUMN_NAME}" type="§{COLUMN_TYPE}"/>
            """;

    public static final String BLOCK_CHANGESET_COLUMN_NONNULL = """
                        <column name="§{COLUMN_NAME}" type="§{COLUMN_TYPE}">
                            <constraints nullable="false"/>
                        </column>
            """;

    public static final String BLOCK_CHANGESET_INDEX = """
                <changeSet author="§{CHANGELOG_AUTHOR}" id="§{CHANGELOG_DATE}-§{TABLE_NAME}-idx-§{COLUMN_NAME}" objectQuotingStrategy="QUOTE_ALL_OBJECTS">
                    <preConditions onFail="MARK_RAN">
                        <not>
                            <indexExists tableName="§{TABLE_NAME}" indexName="§{TABLE_NAME}_§{COLUMN_NAME}" />
                        </not>
                    </preConditions>
                    <createIndex tableName="§{TABLE_NAME}" indexName="§{TABLE_NAME}_§{COLUMN_NAME}">
                        <column name="§{COLUMN_NAME}" />
                    </createIndex>
                </changeSet>
            """;

    public static final String BLOCK_CHANGESET_LINKTABLE = """
                <changeSet author="§{CHANGELOG_AUTHOR}" id="§{CHANGELOG_DATE}-§{TABLE_NAME}" objectQuotingStrategy="QUOTE_ALL_OBJECTS">
                    <preConditions onFail="MARK_RAN">
                        <not>
                            <tableExists tableName="§{TABLE_NAME}" />
                        </not>
                    </preConditions>
                    <createTable tableName="§{TABLE_NAME}">
                        <column name="§{COLUMN_NAME_1}" type="§{COLUMN_TYPE_1}">
                            <constraints nullable="false"/>
                        </column>
                        <column name="§{COLUMN_NAME_2}" type="§{COLUMN_TYPE_2}">
                            <constraints nullable="false"/>
                        </column>
                    </createTable>
                    <addPrimaryKey columnNames="§{COLUMN_NAME_1}, §{COLUMN_NAME_2}" constraintName="pk_§{TABLE_NAME}" tableName="§{TABLE_NAME}"/>
                    <createIndex tableName="§{TABLE_NAME}" indexName="§{TABLE_NAME}_§{COLUMN_NAME_1}">
                        <column name="§{COLUMN_NAME_1}" />
                    </createIndex>
                    <createIndex tableName="§{TABLE_NAME}" indexName="§{TABLE_NAME}_§{COLUMN_NAME_2}">
                        <column name="§{COLUMN_NAME_2}" />
                    </createIndex>
                </changeSet>
            """;

    public static final String BLOCK_CHANGESET_FKEY = """
                <changeSet author="§{CHANGELOG_AUTHOR}" id="§{CHANGELOG_DATE}-fk_§{TABLE_NAME}_§{COLUMN_NAME}" objectQuotingStrategy="QUOTE_ALL_OBJECTS">
                    <preConditions onFail="MARK_RAN">
                        <not>
                            <foreignKeyConstraintExists foreignKeyName="fk_§{TABLE_NAME}_§{COLUMN_NAME}" foreignKeyTableName="§{TABLE_NAME}" />
                        </not>
                    </preConditions>
                    <addForeignKeyConstraint
                        constraintName="fk_§{TABLE_NAME}_§{COLUMN_NAME}"
                        baseTableName="§{TABLE_NAME}" baseColumnNames="§{COLUMN_NAME}"
                        referencedTableName="§{TABLE_NAME_OTHER}" referencedColumnNames="§{COLUMN_NAME_OTHER}"
                        deferrable="false" initiallyDeferred="false"
                        onDelete="CASCADE" onUpdate="CASCADE"/>
                </changeSet>
            """;

    public static final String BLOCK_MAIN_FILE = """
            <?xml version="1.1" encoding="UTF-8"?>
            <databaseChangeLog
                xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-3.1.xsd">
                §{BLOCK_COPYRIGHT}

                §{BLOCK_PROPERTIES}

                §{BLOCK_INCLUDES}

            </databaseChangeLog>

            """;

    private LiquibaseTemplates() {
        // Utility class
    }

}
