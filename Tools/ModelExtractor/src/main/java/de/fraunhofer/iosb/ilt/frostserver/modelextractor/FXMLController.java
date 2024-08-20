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
package de.fraunhofer.iosb.ilt.frostserver.modelextractor;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import de.fraunhofer.iosb.ilt.configurable.ConfigEditor;
import de.fraunhofer.iosb.ilt.configurable.ConfigEditors;
import de.fraunhofer.iosb.ilt.configurable.ConfigurationException;
import de.fraunhofer.iosb.ilt.configurable.ContentConfigEditor;
import de.fraunhofer.iosb.ilt.configurable.editor.EditorNull;
import de.fraunhofer.iosb.ilt.frostserver.model.loader.DefEntityProperty;
import de.fraunhofer.iosb.ilt.frostserver.model.loader.DefEntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.loader.DefModel;
import de.fraunhofer.iosb.ilt.frostserver.model.loader.DefNavigationProperty;
import de.fraunhofer.iosb.ilt.frostserver.model.loader.DefNavigationProperty.Inverse;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.fieldmapper.FieldMapperBigDecimal;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.fieldmapper.FieldMapperBoolean;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.fieldmapper.FieldMapperGeometry;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.fieldmapper.FieldMapperId;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.fieldmapper.FieldMapperJson;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.fieldmapper.FieldMapperManyToMany;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.fieldmapper.FieldMapperOneToMany;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.fieldmapper.FieldMapperString;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.fieldmapper.FieldMapperTimeInstant;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.fieldmapper.FieldMapperTimeInterval;
import de.fraunhofer.iosb.ilt.frostserver.property.type.TypeComplex;
import de.fraunhofer.iosb.ilt.frostserver.property.type.TypeSimplePrimitive;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeMap;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.util.Callback;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.CaseUtils;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.Meta;
import org.jooq.SQLDialect;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.UniqueKey;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FXMLController implements Initializable {

    public enum TableChoice {
        IGNORE("-"),
        ENTITY_TYPE("Entity"),
        LINK_TABLE("Link");

        private final String label;

        private TableChoice(String label) {
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }

    }

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(FXMLController.class);
    private static final String FAILED_TO_GENERATE_JSON = "Failed to generate JSON.";
    private static final String FAILED_TO_LOAD_DB_DRIVER = "Failed to load DB driver.";
    private static final String FAILED_TO_LOAD_JSON = "Failed to load JSON.";
    private static final String FAILED_TO_WRITE_FILE = "Failed to write file.";
    private static final String SELECT_TARGET_FILE_OR_DIRECTORY = "Select target file or directory";

    @FXML
    private AnchorPane paneRoot;
    @FXML
    private ScrollPane paneConfig;
    @FXML
    private Button buttonSave;
    @FXML
    private Button buttonRead;
    @FXML
    private Button buttonSelectDir;
    @FXML
    private Button buttonSelectFile;
    @FXML
    private Button buttonGenerate;
    @FXML
    private TextField textFieldDbUrl;
    @FXML
    private TextField textFieldDriver;
    @FXML
    private TextField textFieldUsername;
    @FXML
    private TextField textFieldPassword;
    @FXML
    private TextField textFieldSetPostfix;
    @FXML
    private TextField textFieldSchemas;
    @FXML
    private ListView<TableData> listViewFound;
    private ObservableList<TableData> tableList;
    @FXML
    private Label labelFile;

    private final EditorNull editorNull = new EditorNull();

    private final FileChooser fileChooser = new FileChooser();
    private final DirectoryChooser dirChooser = new DirectoryChooser();
    private File currentFile = null;

    private ObjectMapper objectMapper;

    private String setPostFix;

    private final Map<String, TableData> tables = new HashMap<>();
    private final Map<String, ConfigEditor<DefModel>> editors = new TreeMap<>();

    private final Set<String> includeSchemas = new HashSet<>();

    private ObjectMapper getObjectMapper() {
        if (objectMapper == null) {
            objectMapper = new ObjectMapper()
                    .setSerializationInclusion(JsonInclude.Include.NON_DEFAULT)
                    .enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS);
        }
        return objectMapper;
    }

    private void setCurrentFile(File file) {
        currentFile = file;
        buttonSave.setDisable(currentFile == null);
        if (currentFile != null) {
            labelFile.setText(currentFile.getAbsolutePath());
            if (currentFile.isDirectory()) {
                dirChooser.setInitialDirectory(currentFile);
                fileChooser.setInitialDirectory(currentFile);
            } else {
                dirChooser.setInitialDirectory(currentFile.getParentFile());
                fileChooser.setInitialDirectory(currentFile.getParentFile());
                fileChooser.setInitialFileName(currentFile.getName());
            }
        }
    }

    @FXML
    private void actionRead(ActionEvent event) {
        setPostFix = textFieldSetPostfix.getText();
        includeSchemas.clear();
        final String[] schemas = StringUtils.split(textFieldSchemas.getText(), ',');
        for (String schema : schemas) {
            includeSchemas.add(schema.trim());
        }
        readFromDatabase();
    }

    private TableData getTable(String tableName) {
        return tables.computeIfAbsent(tableName, t -> new TableData(tableName, setPostFix));
    }

    private void readFromDatabase() {
        listViewFound.getItems().clear();
        tables.clear();
        DSLContext dslContext = getDslContext();
        if (dslContext == null) {
            return;
        }

        Meta meta = dslContext.meta();
        List<Table<?>> dbTableList = meta.getTables();
        for (Table<?> table : dbTableList) {
            if (!includeSchemas.contains(table.getSchema().getName())) {
                continue;
            }
            TableData tableData = getTable(table.getName());
            final UniqueKey primaryKey = table.getPrimaryKey();
            if (primaryKey != null) {
                final List<Field> fields = primaryKey.getFields();
                tableData.setPkSize(fields.size());
                tableData.addFields(fields, true);
            }

            tableData.addFields(Arrays.asList(table.fields()), false);

            List<ForeignKey<?, ?>> references = (List<ForeignKey<?, ?>>) table.getReferences();
            for (ForeignKey reference : references) {
                TableField[] otherFields = reference.getKeyFieldsArray();
                if (otherFields.length != 1) {
                    LOGGER.warn("      Multi-key FK: {}", reference.getName());
                    continue;
                }
                TableField otherField = otherFields[0];
                String otherTableName = otherField.getTable().getName();

                TableField[] myFields = reference.getFieldsArray();
                if (myFields.length != 1) {
                    LOGGER.warn("      Multi-source-field FK: {}", reference.getName());
                    continue;
                }
                FieldData fieldMine = tableData.getField(myFields[0].getName());
                fieldMine.setFk(true);

                ForeignKeyData fk = new ForeignKeyData(tableData.tableName, otherTableName)
                        .setFieldMine(fieldMine)
                        .setFieldTheirs(FieldData.from(otherField, false));
                tableData.addReferenceToOther(fk);
                getTable(otherTableName).addReferenceFromOther(fk);

            }
            this.tableList.add(tableData);
        }

        for (TableData tableData : tables.values()) {
            LOGGER.info("-> {}", tableData.tableName);
            LOGGER.info("  -> Fields");
            for (FieldData fieldData : tableData.getFields().values()) {
                LOGGER.info("    -> {}", fieldData);
            }
            LOGGER.info("  -> Relations");
            for (List<ForeignKeyData> fkList : tableData.refsToOther.values()) {
                for (ForeignKeyData fk : fkList) {
                    LOGGER.info("    -> {}", fk);
                }
            }

            tableData.analyse();
        }
        buttonGenerate.setDisable(false);
    }

    private DSLContext getDslContext() {
        try {
            Class.forName(textFieldDriver.getText());
        } catch (ClassNotFoundException ex) {
            LOGGER.error(FAILED_TO_LOAD_DB_DRIVER);
            alertError(FAILED_TO_LOAD_DB_DRIVER, ex);
            return null;
        }
        BasicDataSource ds = new BasicDataSource();
        ds.setUrl(textFieldDbUrl.getText());
        ds.setUsername(textFieldUsername.getText());
        ds.setPassword(textFieldPassword.getText());
        return DSL.using(ds, SQLDialect.POSTGRES);
    }

    private void generate() {
        editors.clear();
        Map<String, DefEntityType> entityTypes = new TreeMap<>();
        DefModel fullModel = new DefModel();

        generateDataColumns(entityTypes, fullModel);

        generateRelations(entityTypes);
        for (DefEntityType defEt : fullModel.getEntityTypes()) {
            editors.put(defEt.getTable(), createEditorForModel(new DefModel().addEntityType(defEt)));
        }
    }

    private void generateRelations(Map<String, DefEntityType> entityTypes) {
        for (TableData tableData : tables.values()) {
            if (!tableData.isEntityType()) {
                continue;
            }
            // Direct links from this entity to other entities.
            DefEntityType entityType = entityTypes.get(tableData.tableName);
            generateRelationsDirect(tableData, entityTypes, entityType);
            generateRelationsLinktable(tableData, entityTypes, entityType);
        }
    }

    private void generateRelationsLinktable(TableData tableData, Map<String, DefEntityType> entityTypes, DefEntityType entityType) {
        for (List<ForeignKeyData> fkList : tableData.refsFromOther.values()) {
            if (fkList.size() == 2
                    && fkList.get(0).myTableName.equals(fkList.get(1).myTableName)
                    && fkList.get(0).otherTableName.equals(fkList.get(1).otherTableName)
                    && fkList.get(0).fieldTheirs.name.equals(fkList.get(1).fieldTheirs.name)
                    && tables.get(fkList.get(0).myTableName).isLinkTable()) {
                generateSelfRelationLinkTable(fkList, entityType, tableData);
            } else {
                for (ForeignKeyData fk : fkList) {
                    final TableData linkTableData = tables.get(fk.myTableName);
                    if (linkTableData.isLinkTable()) {
                        final String otherTableName = linkTableData.refsToOther.keySet().stream().findFirst().orElse("");
                        final TableData otherTableData = tables.get(otherTableName);
                        if (otherTableData == null) {
                            LOGGER.warn("Ignoring fk over linkTable {}; No data on other table {}", linkTableData.tableName, otherTableName);
                            continue;
                        }
                        if (otherTableData == tableData) {
                            // Other side handles this link.
                        } else {
                            generateRelationLinkTable(linkTableData, otherTableName, entityTypes, otherTableData, fk, entityType);
                        }
                    } else {
                        LOGGER.info("Ignoring fk from {} to {} ({}, not a link table)", tableData.tableName, linkTableData.tableName, linkTableData.getTableType());
                    }
                }
            }
        }
    }

    private void generateSelfRelationLinkTable(List<ForeignKeyData> fkList, DefEntityType entityType, TableData tableData) {
        // This is a self-link
        ForeignKeyData fk1 = fkList.get(0);
        ForeignKeyData fk2 = fkList.get(1);
        final TableData linkTableData = tables.get(fk1.myTableName);
        DefNavigationProperty defNp = new DefNavigationProperty()
                .setName(entityType.getPlural() + "_" + fk1.fieldMine.name)
                .setEntityType(entityType.getName())
                .setEntitySet(true)
                .addHandler(new FieldMapperManyToMany()
                        .setField(fk1.fieldTheirs.name)
                        .setLinkTable(linkTableData.tableName)
                        .setLinkOurField(fk1.fieldMine.name)
                        .setLinkOtherField(fk2.fieldMine.name)
                        .setOtherTable(tableData.tableName)
                        .setOtherField(fk2.fieldTheirs.name))
                .setInverse(new Inverse()
                        .setName(entityType.getPlural() + "_" + fk2.fieldMine.name)
                        .setEntitySet(true));
        entityType.addNavigationProperty(defNp);
    }

    private void generateRelationLinkTable(final TableData linkTableData, final String otherTableName, Map<String, DefEntityType> entityTypes, final TableData otherTableData, ForeignKeyData fk, DefEntityType entityType) {
        List<ForeignKeyData> fk2List = linkTableData.refsToOther.get(otherTableName);
        if (fk2List.size() > 1) {
            LOGGER.warn("Found multiple links from {} to link table {}", otherTableName, linkTableData.tableName);
        }
        final ForeignKeyData fk2 = fk2List.get(0);
        DefEntityType otherEntityType = entityTypes.get(otherTableData.tableName);
        DefNavigationProperty defNp = new DefNavigationProperty()
                .setName(otherEntityType.getPlural())
                .setEntityType(otherEntityType.getName())
                .setEntitySet(true)
                .addHandler(new FieldMapperManyToMany()
                        .setField(fk.fieldTheirs.name)
                        .setLinkTable(linkTableData.tableName)
                        .setLinkOurField(fk.fieldMine.name)
                        .setLinkOtherField(fk2.fieldMine.name)
                        .setOtherTable(otherTableData.tableName)
                        .setOtherField(fk2.fieldTheirs.name))
                .setInverse(new Inverse()
                        .setName(entityType.getPlural())
                        .setEntitySet(true));
        entityType.addNavigationProperty(defNp);
    }

    private void generateRelationsDirect(TableData tableData, Map<String, DefEntityType> entityTypes, DefEntityType entityType) {
        for (List<ForeignKeyData> fkList : tableData.refsToOther.values()) {
            for (ForeignKeyData fk : fkList) {
                TableData otherTableData = tables.get(fk.otherTableName);
                if (otherTableData.isEntityType()) {
                    DefEntityType otherEntityType = entityTypes.get(otherTableData.tableName);
                    String myRelationName = otherEntityType.getName();
                    String otherRelationName = entityType.getPlural();
                    if (entityType.equals(otherEntityType)) {
                        myRelationName = otherEntityType.getName() + "Parent";
                        otherRelationName = entityType.getPlural() + "Children";
                    }
                    DefNavigationProperty defNp = new DefNavigationProperty()
                            .setName(myRelationName)
                            .setEntityType(otherEntityType.getName())
                            .setRequired(true)
                            .addHandler(new FieldMapperOneToMany()
                                    .setField(fk.fieldMine.name)
                                    .setOtherField(fk.fieldTheirs.name)
                                    .setOtherTable(fk.otherTableName))
                            .setInverse(new Inverse()
                                    .setName(otherRelationName)
                                    .setEntitySet(true));
                    entityType.addNavigationProperty(defNp);
                } else {
                    LOGGER.info("Ignoring fk from {} to {} ({})", tableData.tableName, otherTableData.tableName, otherTableData.getTableType());
                }
            }
        }
    }

    private void generateDataColumns(Map<String, DefEntityType> entityTypes, DefModel fullModel) {
        // First pass, gather non-fk columns.
        for (TableData tableData : tables.values()) {
            if (tableData.isEntityType()) {
                DefEntityType defType = new DefEntityType()
                        .setName(tableData.getEntityName())
                        .setPlural(tableData.getEntityPlural())
                        .setTable(tableData.tableName);

                for (FieldData field : tableData.getFields().values()) {
                    DefEntityProperty defEp = new DefEntityProperty();
                    defEp.setName(CaseUtils.toCamelCase(field.name, false, '_'));
                    if (field.pk) {
                        defEp.addAlias("@iot.id");
                    }
                    if (fieldMapperFromFieldData(field, defEp, tableData)) {
                        defType.addEntityProperty(defEp);
                    }
                }

                entityTypes.put(tableData.tableName, defType);
                fullModel.addEntityType(defType);
            }
        }
    }

    private ConfigEditor<DefModel> createEditorForModel(DefModel model) {
        try {
            String stringData = getObjectMapper().writeValueAsString(model);
            JsonElement json = JsonParser.parseString(stringData);
            if (json == null) {
                return null;
            }
            ConfigEditor<DefModel> configEditor = (ConfigEditor<DefModel>) ConfigEditors
                    .buildEditorFromClass(DefModel.class, null, null)
                    .orElse(editorNull);
            configEditor.setConfig(json);
            return configEditor;
        } catch (JsonProcessingException ex) {
            LOGGER.error(FAILED_TO_GENERATE_JSON, ex);
            alertError(FAILED_TO_GENERATE_JSON, ex);
        }
        return null;
    }

    private boolean fieldMapperFromFieldData(FieldData fieldData, DefEntityProperty defEp, TableData tableData) {
        if (fieldData.pk) {
            defEp.addHandler(new FieldMapperId().setField(fieldData.name))
                    .setType("Id");
            return true;
        }
        if (fieldData.fk) {
            return false;
        }
        switch (fieldData.typeName.toLowerCase()) {
            case "boolean":
                defEp.addHandler(new FieldMapperBoolean().setField(fieldData.name))
                        .setType(TypeSimplePrimitive.EDM_BOOLEAN_NAME);
                return true;

            case "clob":
            case "varchar":
            case "uuid":
                defEp.addHandler(new FieldMapperString().setField(fieldData.name))
                        .setType(TypeSimplePrimitive.EDM_STRING_NAME);
                return true;

            case "smallint":
            case "integer":
            case "bigint":
                defEp.addHandler(new FieldMapperBigDecimal().setField(fieldData.name))
                        .setType(TypeSimplePrimitive.EDM_INT64_NAME);
                return true;

            case "float":
            case "double":
            case "decimal":
                defEp.addHandler(new FieldMapperBigDecimal().setField(fieldData.name))
                        .setType(TypeSimplePrimitive.EDM_DECIMAL_NAME);
                return true;

            case "geometry":
                defEp.addHandler(new FieldMapperGeometry().setFieldGeom(fieldData.name))
                        .setType(TypeSimplePrimitive.EDM_GEOMETRY_NAME);
                return true;

            case "json":
            case "jsonb":
                defEp.addHandler(new FieldMapperJson().setField(fieldData.name)
                        .setIsMap(false))
                        .setType(TypeComplex.STA_MAP_NAME);
                return true;

            case "timestamp with time zone":
                final String fnl = fieldData.name.toLowerCase();
                if (fnl.endsWith("_end") && tableData.hasField(fnl.substring(0, fnl.length() - 3) + "start")) {
                    // Start+End time combo, end already handled by start.
                    return false;
                }
                final String endName = fnl.length() > 6 ? fnl.substring(0, fnl.length() - 5) + "end" : "";
                if (fnl.endsWith("_start") && tableData.hasField(endName)) {
                    FieldData fieldDataEnd = tableData.getField(endName);
                    defEp.setType("TimeInterval")
                            .setName(defEp.getName().substring(0, defEp.getName().length() - 5))
                            .addHandler(new FieldMapperTimeInterval()
                                    .setFieldStart(fieldData.name)
                                    .setFieldEnd(fieldDataEnd.name));
                    return true;
                }
                defEp.addHandler(new FieldMapperTimeInstant().setField(fieldData.name))
                        .setType(TypeSimplePrimitive.EDM_DATETIMEOFFSET_NAME);
                return true;

            default:
                LOGGER.error("Unknown field type: {}", fieldData.typeName);
                return true;
        }
    }

    @FXML
    private void actionSelectFile(ActionEvent event) {
        fileChooser.setTitle(SELECT_TARGET_FILE_OR_DIRECTORY);
        File file = fileChooser.showOpenDialog(paneConfig.getScene().getWindow());
        if (file == null) {
            return;
        }
        setCurrentFile(file);
    }

    @FXML
    private void actionSelectDir(ActionEvent event) {
        fileChooser.setTitle(SELECT_TARGET_FILE_OR_DIRECTORY);
        File file = dirChooser.showDialog(paneConfig.getScene().getWindow());
        if (file == null) {
            return;
        }
        setCurrentFile(file);
    }

    @FXML
    private void actionGenerate(ActionEvent event) {
        generate();
        listViewFound.getSelectionModel().clearSelection();
        listViewFound.getSelectionModel().selectFirst();
    }

    @FXML
    private void actionSave(ActionEvent event) {
        if (currentFile.isDirectory()) {
            for (ConfigEditor<DefModel> editor : editors.values()) {
                try {
                    DefModel subModel = new DefModel();
                    ((ContentConfigEditor) editor).setContentsOn(subModel);
                    String typeName = subModel.getEntityTypes().get(0).getName();
                    saveToFile(editor.getConfig(), new File(currentFile, typeName + ".json"));
                } catch (ConfigurationException ex) {
                    LOGGER.error(FAILED_TO_LOAD_JSON, ex);
                    alertError(FAILED_TO_LOAD_JSON, ex);
                }
            }
        } else {
            DefModel composite = new DefModel();
            for (ConfigEditor<DefModel> editor : editors.values()) {
                try {
                    DefModel subModel = new DefModel();
                    ((ContentConfigEditor) editor).setContentsOn(subModel);
                    composite.getEntityTypes().addAll(subModel.getEntityTypes());
                    composite.getConformance().addAll(subModel.getConformance());
                    composite.getSimplePropertyTypes().addAll(subModel.getSimplePropertyTypes());
                } catch (ConfigurationException ex) {
                    LOGGER.error(FAILED_TO_LOAD_JSON, ex);
                    alertError(FAILED_TO_LOAD_JSON, ex);
                }
            }

            try {
                String stringData = getObjectMapper().writeValueAsString(composite);
                JsonElement json = JsonParser.parseString(stringData);
                saveToFile(json, currentFile);
            } catch (JsonProcessingException ex) {
                LOGGER.error(FAILED_TO_GENERATE_JSON, ex);
                alertError(FAILED_TO_GENERATE_JSON, ex);
            }
        }
    }

    private void saveToFile(JsonElement json, File file) {
        if (file == null) {
            return;
        }
        String config = new GsonBuilder().setPrettyPrinting().create().toJson(json);
        try {
            FileUtils.writeStringToFile(file, config, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            LOGGER.error(FAILED_TO_WRITE_FILE, ex);
            alertError(FAILED_TO_WRITE_FILE, ex);
        }
    }

    private void showModel(TableData table) {
        if (table == null) {
            paneConfig.setContent(null);
            return;
        }
        LOGGER.info("Selected: {}", table);
        ConfigEditor<?> model = editors.get(table.tableName);
        replaceEditor(model);
    }

    private void replaceEditor(ConfigEditor<?> editor) {
        if (editor == null) {
            paneConfig.setContent(null);
        } else {
            paneConfig.setContent(editor.getGuiFactoryFx().getNode());
        }
    }

    private void alertError(String text, Exception ex) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(text);
        alert.setContentText(ex.getLocalizedMessage());
        alert.showAndWait();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        tableList = FXCollections.observableArrayList();
        listViewFound.setItems(tableList);
        listViewFound.getSelectionModel().selectedItemProperty().addListener((ov, oldItem, newItem) -> showModel(newItem));
        listViewFound.setCellFactory(new TableDataCellFactory());
    }

    public static class TableDataCellFactory implements Callback<ListView<TableData>, ListCell<TableData>> {

        @Override
        public ListCell<TableData> call(ListView<TableData> list) {
            return new ListCell<>() {
                @Override
                public void updateItem(TableData tableData, boolean empty) {
                    super.updateItem(tableData, empty);
                    if (empty) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        setText(null);
                        setGraphic(tableData.getNode());
                    }
                }
            };
        }
    }
}
