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
package de.fraunhofer.iosb.ilt.frostserver.modeleditor;

import com.fasterxml.jackson.core.JsonProcessingException;
import de.fraunhofer.iosb.ilt.configurable.ConfigEditor;
import de.fraunhofer.iosb.ilt.frostserver.json.serialize.JsonWriter;
import de.fraunhofer.iosb.ilt.frostserver.model.loader.DefModel;
import de.fraunhofer.iosb.ilt.frostserver.modeleditor.LiquibaseTemplates.ChangeLogBuilder;
import de.fraunhofer.iosb.ilt.frostserver.util.SecurityModel;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextField;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Window;
import org.apache.commons.io.FileUtils;
import org.slf4j.LoggerFactory;

public class FXMLController implements Initializable {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(FXMLController.class);

    @FXML
    private AnchorPane paneRoot;
    @FXML
    private SplitPane splitPaneModel;
    @FXML
    private ScrollPane paneConfig;
    @FXML
    private BorderPane bpEntityModel;
    @FXML
    private BorderPane bpSecurityModel;
    @FXML
    private Button buttonLoad;
    @FXML
    private Button buttonSave;
    @FXML
    private Button buttonSaveAll;
    @FXML
    private Button buttonClose;
    @FXML
    private TextField textFieldDate;
    @FXML
    private TextField textFieldLiquibasePath;
    @FXML
    private Label labelFile;
    @FXML
    private ScrollPane paneConfigSec;
    @FXML
    private Button buttonLoadSec;
    @FXML
    private Button buttonSaveSec;
    @FXML
    private Label labelFileSec;
    @FXML
    private ListView<FileData> listViewEntityTypes;
    private ObservableList<FileData> entityTypeList;

    private ConfigFileEditor cfeSecurity;
    private File currentPath;

    @FXML
    private void actionLoad(ActionEvent event) {
        loadWithSelector();
    }

    @FXML
    private void actionSave(ActionEvent event) {
        FileData fd = listViewEntityTypes.getSelectionModel().getSelectedItem();
        ConfigFileEditor cfe = fd.getEditor();
        cfe.saveModelWithChooser("Save Data Model", getWindow());
    }

    @FXML
    private void actionSaveAll(ActionEvent event) {
        List<DefModel> models = new ArrayList<>();
        for (FileData fd : listViewEntityTypes.getItems()) {
            ConfigFileEditor cfe = fd.getEditor();
            String data = cfe.saveToCurrentFile();
            try {
                DefModel value = JsonWriter.getObjectMapper().readValue(data, DefModel.class);
                models.add(value);
            } catch (JsonProcessingException ex) {
                LOGGER.error("Failed to instantiate.", ex);
            }
        }
        List<ChangeLogBuilder> liquibaseChangeLogs = LiquibaseTemplates.CreateChangeLogsFor(models, textFieldDate.getText());
        File liquibasePath = new File(currentPath, textFieldLiquibasePath.getText());
        if (!liquibaseChangeLogs.isEmpty()) {
            liquibasePath.mkdirs();
        }
        for (var cl : liquibaseChangeLogs) {
            String data = cl.build();
            String fileName = cl.getFileName();
            if (!fileName.endsWith(".xml")) {
                fileName += ".xml";
            }
            File targetFile = new File(liquibasePath, fileName);
            try {
                FileUtils.writeStringToFile(targetFile, data, StandardCharsets.UTF_8);
            } catch (IOException ex) {
                LOGGER.error("Failed to save liquibase file.", ex);
            }
        }

    }

    @FXML
    private void actionClose(ActionEvent event) {
        FileData fd = listViewEntityTypes.getSelectionModel().getSelectedItem();
        if (fd != null) {
            listViewEntityTypes.getItems().remove(fd);
        }
    }

    private Window getWindow() {
        return paneRoot.getScene().getWindow();
    }

    @FXML
    private void actionLoadSec(ActionEvent event) {
        loadSecWithSelector();
    }

    @FXML
    private void actionSaveSec(ActionEvent event) {
        cfeSecurity.saveModelWithChooser("Save Security Model", getWindow());
        labelFileSec.setText(cfeSecurity.getCurrentFile().getAbsolutePath());
    }

    private void loadWithSelector() {
        ConfigFileEditor cfe = new ConfigFileEditor(DefModel.class);
        cfe.loadFromFileWithChooser("Load Entity Type", getWindow());
        addToList(cfe);
    }

    private void loadFromFile(File file) {
        ConfigFileEditor cfe = new ConfigFileEditor(DefModel.class);
        cfe.loadFromFile(file);
        addToList(cfe);
    }

    private void loadSecWithSelector() {
        cfeSecurity.loadFromFileWithChooser("Load Security Model", getWindow());
        labelFileSec.setText(cfeSecurity.getCurrentFile().getAbsolutePath());
        replaceSecEditor(cfeSecurity.getConfigEditor());
    }

    private void loadSecFromFile(File file) {
        cfeSecurity.loadFromFile(file);
        labelFileSec.setText(cfeSecurity.getCurrentFile().getAbsolutePath());
        replaceSecEditor(cfeSecurity.getConfigEditor());
    }

    private void addToList(ConfigFileEditor cfe) {
        FileData fd = new FileData();
        fd.setEditor(cfe);
        fd.updateFileName();
        entityTypeList.add(fd);
        listViewEntityTypes.getSelectionModel().select(fd);
    }

    private void showModel(FileData file) {
        if (file == null) {
            paneConfig.setContent(null);
            return;
        }
        LOGGER.info("Selected: {}", file);
        replaceEditor(file.editor);
        labelFile.setText(file.getCurrentFilePath());
    }

    private void replaceEditor(ConfigFileEditor editor) {
        if (editor == null) {
            paneConfig.setContent(null);
        } else {
            replaceEditor(editor.getConfigEditor());
            File currentFile = editor.getCurrentFile();
            if (currentFile != null) {
                currentPath = currentFile.getParentFile();
            }
        }
    }

    private void replaceEditor(ConfigEditor<?> editor) {
        if (editor == null) {
            paneConfig.setContent(null);
        } else {
            paneConfig.setContent(editor.getGuiFactoryFx().getNode());
        }
    }

    private void replaceSecEditor(ConfigEditor<?> editor) {
        if (editor == null) {
            paneConfigSec.setContent(null);
        } else {
            paneConfigSec.setContent(editor.getGuiFactoryFx().getNode());
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        SplitPane.setResizableWithParent(listViewEntityTypes, Boolean.FALSE);
        cfeSecurity = new ConfigFileEditor(SecurityModel.class);
        cfeSecurity.initialize();
        entityTypeList = FXCollections.observableArrayList();
        listViewEntityTypes.setItems(entityTypeList);
        listViewEntityTypes.getSelectionModel().selectedItemProperty().addListener((ov, oldItem, newItem) -> showModel(newItem));
        makeDropTarget(bpEntityModel, this::loadFromFile);
        makeDropTarget(bpSecurityModel, this::loadSecFromFile);
        textFieldDate.setText(DateTimeFormatter.ISO_LOCAL_DATE.format(ZonedDateTime.now()));
        textFieldLiquibasePath.setText("../liquibase");
    }

    public static void makeDropTarget(Node node, FileAction action) {
        node.setOnDragOver(event -> {
            if (event.getGestureSource() != node && event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY);
            }
            event.consume();
        });
        node.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasFiles()) {
                List<File> files = db.getFiles();
                for (var file : files) {
                    action.call(file);
                }
                success = true;
            }
            event.setDropCompleted(success);
            event.consume();
        });
    }

    public void close() {
        LOGGER.info("Received close, shutting down.");
    }

    public static class FileData {

        private String fileName;
        private ConfigFileEditor editor;

        public String getFileName() {
            return fileName;
        }

        public File getCurrentFile() {
            return editor.getCurrentFile();
        }

        public String getCurrentFilePath() {
            File currentFile = editor.getCurrentFile();
            if (currentFile == null) {
                return "No file selected";
            }
            return currentFile.getAbsolutePath();
        }

        public ConfigFileEditor getEditor() {
            return editor;
        }

        public FileData setEditor(ConfigFileEditor editor) {
            this.editor = editor;
            return this;
        }

        public FileData updateFileName() {
            File currentFile = editor.getCurrentFile();
            if (currentFile == null) {
                fileName = "No File";
            } else {
                fileName = currentFile.getName();
            }
            return this;
        }

        @Override
        public String toString() {
            return fileName;
        }

    }

    public static interface FileAction {

        abstract public void call(File file);
    }
}
