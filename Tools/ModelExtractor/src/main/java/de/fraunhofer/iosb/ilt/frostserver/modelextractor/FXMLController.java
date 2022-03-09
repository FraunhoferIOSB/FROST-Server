/*
 * Copyright (C) 2017 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
 * Karlsruhe, Germany.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
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
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.fieldmapper.FieldMapperId;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.fieldmapper.FieldMapperJson;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.fieldmapper.FieldMapperManyToMany;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.fieldmapper.FieldMapperOneToMany;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.fieldmapper.FieldMapperString;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.fieldmapper.FieldMapperTimeInstant;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.fieldmapper.FieldMapperTimeInterval;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.TreeMap;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.util.Callback;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.io.FileUtils;
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
    private ListView<TableData> listViewFound;
    private ObservableList<TableData> tableList;
    @FXML
    private Label labelFile;

    private final EditorNull editorNull = new EditorNull();

    private final FileChooser fileChooser = new FileChooser();
    private final DirectoryChooser dirChooser = new DirectoryChooser();
    private File currentFile = null;

    private ObjectMapper objectMapper;

    private final Map<String, TableData> tables = new HashMap<>();
    private final Map<String, ConfigEditor<DefModel>> models = new TreeMap<>();

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
        readFromDatabase();
    }

    private static class FieldData {

        final String name;
        boolean pk;
        boolean fk;
        String typeName;
        String castTypeName;
        String comment;

        public static FieldData from(Field field, boolean pk) {
            return new FieldData(field.getName())
                    .setPk(pk)
                    .setTypeName(field.getDataType().getTypeName())
                    .setCastTypeName(field.getDataType().getCastTypeName())
                    .setComment(field.getComment());

        }

        public FieldData(String name) {
            this.name = name;
        }

        public FieldData setPk(boolean pk) {
            this.pk = pk;
            return this;
        }

        public FieldData setFk(boolean fk) {
            this.fk = fk;
            return this;
        }

        public FieldData setTypeName(String typeName) {
            this.typeName = typeName;
            return this;
        }

        public FieldData setCastTypeName(String castTypeName) {
            this.castTypeName = castTypeName;
            return this;
        }

        public FieldData setComment(String comment) {
            this.comment = comment;
            return this;
        }

        @Override
        public String toString() {
            return name + "(" + typeName + " / " + castTypeName + ") " + (pk ? "PK" : "") + (fk ? "FK" : "");
        }
    }

    private static class ForeignKeyData {

        final String myTableName;
        final String otherTableName;
        FieldData fieldMine;
        FieldData fieldTheirs;

        public ForeignKeyData(String myTableName, String otherTableName) {
            this.myTableName = myTableName;
            this.otherTableName = otherTableName;
        }

        public ForeignKeyData setFieldMine(FieldData fieldMine) {
            this.fieldMine = fieldMine;
            return this;
        }

        public ForeignKeyData setFieldTheirs(FieldData fieldTheirs) {
            this.fieldTheirs = fieldTheirs;
            return this;
        }

        @Override
        public String toString() {
            return myTableName + "." + fieldMine.name + " -> " + otherTableName + "." + fieldTheirs.name + "";
        }

    }

    static class TableData {

        private final String tableName;
        private final Map<String, ForeignKeyData> refsToOther = new TreeMap<>();
        private final Map<String, ForeignKeyData> refsFromOther = new TreeMap<>();
        private final Map<String, FieldData> fields = new TreeMap<>();
        private int pkSize;
        private Node node;
        private TableDataController controller;

        public TableData(String tableName) {
            this.tableName = tableName;
            getNode();
        }

        public Node getNode() {
            if (node == null) {
                try {
                    FXMLLoader loader = new FXMLLoader(TableDataController.class.getResource("/fxml/TableData.fxml"));
                    node = (Pane) loader.load();
                    controller = loader.<TableDataController>getController();
                    controller.setTableName(tableName);
                    String prettyName = CaseUtils.toCamelCase(tableName, true, '_');
                    controller.setSingular(prettyName);
                    controller.setPlural(prettyName + "Set");
                } catch (IOException ex) {
                    LOGGER.error("Failed to load FXML", ex);
                }
            }
            return node;
        }

        public String getTableName() {
            return tableName;
        }

        public String getEntityName() {
            return controller.getSingular();
        }

        public String getEntityPlural() {
            return controller.getPlural();
        }

        public TableChoice getTableType() {
            return controller.getChoice();
        }

        public boolean isEntityType() {
            return controller.getChoice() == TableChoice.ENTITY_TYPE;
        }

        public boolean isLinkTable() {
            return controller.getChoice() == TableChoice.LINK_TABLE;
        }

        public boolean isIgnored() {
            return controller.getChoice() == TableChoice.IGNORE;
        }

        public void addReferenceToOther(ForeignKeyData fk) {
            refsToOther.put(fk.otherTableName, fk);
        }

        public void addReferenceFromOther(ForeignKeyData fk) {
            refsFromOther.put(fk.myTableName, fk);
        }

        public void setPkSize(int pkSize) {
            this.pkSize = pkSize;
        }

        public void analyse() {
            if ((pkSize == 0 || pkSize > 1) && refsToOther.size() == 2) {
                controller.setChoice(TableChoice.LINK_TABLE);
                return;
            }
            if (pkSize == 1 && refsFromOther.size() + refsToOther.size() > 0) {
                controller.setChoice(TableChoice.ENTITY_TYPE);
                return;
            }
            controller.setChoice(TableChoice.IGNORE);
        }

        public Map<String, FieldData> getFields() {
            return fields;
        }

        public boolean hasField(String fieldName) {
            return fields.containsKey(fieldName.toLowerCase());
        }

        public FieldData getField(String fieldName) {
            return fields.get(fieldName.toLowerCase());
        }

        public void addFields(List<Field> tableFields, boolean pks) {
            for (Field field : tableFields) {
                final String fieldName = field.getName();
                if (fields.containsKey(fieldName.toLowerCase())) {
                    continue;
                }
                final FieldData fieldData = FieldData.from(field, pks);
                fields.put(fieldData.name.toLowerCase(), fieldData);
            }
        }

        @Override
        public String toString() {
            return tableName;
        }

    }

    private TableData getTable(String tableName) {
        return tables.computeIfAbsent(tableName, TableData::new);
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
            for (ForeignKeyData fk : tableData.refsToOther.values()) {
                LOGGER.info("    -> {}", fk);
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
        models.clear();
        Map<String, DefEntityType> entityTypes = new TreeMap<>();
        DefModel fullModel = new DefModel();
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
        for (TableData tableData : tables.values()) {
            if (tableData.isEntityType()) {
                DefEntityType entityType = entityTypes.get(tableData.tableName);
                for (ForeignKeyData fk : tableData.refsToOther.values()) {
                    TableData otherTableData = tables.get(fk.otherTableName);
                    if (otherTableData.isEntityType()) {
                        DefEntityType otherEntityType = entityTypes.get(otherTableData.tableName);
                        DefNavigationProperty defNp = new DefNavigationProperty()
                                .setName(otherEntityType.getName())
                                .setEntityType(otherEntityType.getName())
                                .setRequired(true)
                                .addHandler(new FieldMapperOneToMany()
                                        .setField(fk.fieldMine.name)
                                        .setOtherField(fk.fieldTheirs.name)
                                        .setOtherTable(fk.otherTableName))
                                .setInverse(new Inverse()
                                        .setName(entityType.getPlural())
                                        .setEntitySet(true));
                        entityType.addNavigationProperty(defNp);
                    } else {
                        LOGGER.info("Ignoring fk from {} to {} ({})", tableData.tableName, otherTableData.tableName, otherTableData.getTableType());
                    }
                }
                for (ForeignKeyData fk : tableData.refsFromOther.values()) {
                    TableData otherTableData = tables.get(fk.myTableName);
                    if (otherTableData.isLinkTable()) {
                        TableData linkTableData = otherTableData;
                        String otherTableName = linkTableData.refsToOther.keySet().stream().findFirst().orElse("");
                        otherTableData = tables.get(otherTableName);
                        if (otherTableData == null || otherTableData == tableData) {
                            LOGGER.warn("Ignoring fk over linkTable for now {}, {}", linkTableData.tableName, otherTableName);
                            continue;
                        }
                        ForeignKeyData fk2 = linkTableData.refsToOther.get(otherTableName);
                        DefEntityType otherEntityType = entityTypes.get(otherTableData.tableName);
                        DefNavigationProperty defNp = new DefNavigationProperty()
                                .setName(otherEntityType.getPlural())
                                .setEntityType(otherEntityType.getName())
                                .addHandler(new FieldMapperManyToMany()
                                        .setField(fk.fieldMine.name)
                                        .setLinkTable(linkTableData.tableName)
                                        .setLinkOurField(fk.fieldTheirs.name)
                                        .setLinkOtherField(fk2.fieldTheirs.name)
                                        .setOtherTable(otherTableData.tableName)
                                        .setOtherField(fk2.fieldMine.name))
                                .setInverse(new Inverse()
                                        .setName(entityType.getPlural())
                                        .setEntitySet(true));
                        entityType.addNavigationProperty(defNp);
                    } else {
                        LOGGER.info("Ignoring fk from {} to {} ({})", tableData.tableName, otherTableData.tableName, otherTableData.getTableType());
                    }
                }
            }
        }
        for (DefEntityType defEt : fullModel.getEntityTypes()) {
            models.put(defEt.getTable(), createEditorForModel(new DefModel().addEntityType(defEt)));
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
            case "clob":
            case "varchar":
            case "uuid":
                defEp.addHandler(new FieldMapperString().setField(fieldData.name))
                        .setType("String");
                return true;

            case "smallint":
            case "bigint":
            case "float":
                defEp.addHandler(new FieldMapperBigDecimal().setField(fieldData.name))
                        .setType("BigDecimal");
                return true;

            case "jsonb":
                defEp.addHandler(new FieldMapperJson().setField(fieldData.name)
                        .setIsMap(false))
                        .setType("Object");
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
                        .setType("TimeInstant");
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
            for (ConfigEditor<DefModel> editor : models.values()) {
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
            for (ConfigEditor<DefModel> editor : models.values()) {
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
        ConfigEditor<?> model = models.get(table.tableName);
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
        listViewFound.getSelectionModel().selectedItemProperty().addListener((ov, oldItem, newItem) -> {
            showModel(newItem);
        });
        listViewFound.setCellFactory(new TableDataCellFactory());
    }

    public class TableDataCellFactory implements Callback<ListView<TableData>, ListCell<TableData>> {

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
