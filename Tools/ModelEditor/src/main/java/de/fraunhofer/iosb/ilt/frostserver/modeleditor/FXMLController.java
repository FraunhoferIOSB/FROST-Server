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

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import de.fraunhofer.iosb.ilt.configurable.ConfigEditor;
import de.fraunhofer.iosb.ilt.configurable.ConfigEditors;
import de.fraunhofer.iosb.ilt.configurable.editor.EditorNull;
import de.fraunhofer.iosb.ilt.frostserver.model.loader.DefModel;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import org.apache.commons.io.FileUtils;
import org.slf4j.LoggerFactory;

public class FXMLController implements Initializable {

    /**
     * The logger for this class.
     */
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(FXMLController.class);
    @FXML
    private AnchorPane paneRoot;
    @FXML
    private ScrollPane paneConfig;
    @FXML
    private Button buttonLoad;
    @FXML
    private Button buttonSave;
    @FXML
    private Label labelFile;

    private final EditorNull editorNull = new EditorNull();
    private ConfigEditor<?> configEditorModel;
    private final FileChooser fileChooser = new FileChooser();

    private final ExecutorService executor = Executors.newFixedThreadPool(1);

    private void setCurrentFile(File file) {
        File currentFile = file;
        if (currentFile != null) {
            labelFile.setText(currentFile.getAbsolutePath());
            fileChooser.setInitialDirectory(currentFile.getParentFile());
            fileChooser.setInitialFileName(currentFile.getName());
        }
    }

    @FXML
    private void actionLoad(ActionEvent event) {
        loadFromFile("Load Model");
    }

    private void loadFromFile(String title) {
        fileChooser.setTitle(title);
        File file = fileChooser.showOpenDialog(paneConfig.getScene().getWindow());
        loadFromFile(file);
    }

    private void loadFromFile(File file) {
        if (file == null) {
            return;
        }
        try {
            String config = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
            loadJson(config);
            setCurrentFile(file);
        } catch (IOException ex) {
            LOGGER.error("Failed to read file", ex);
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("failed to read file");
            alert.setContentText(ex.getLocalizedMessage());
            alert.showAndWait();
        }
    }

    private void loadJson(String config) {
        JsonElement json = JsonParser.parseString(config);
        if (json == null) {
            return;
        }
        configEditorModel = ConfigEditors
                .buildEditorFromClass(DefModel.class, null, null)
                .orElse(editorNull);
        configEditorModel.setConfig(json);
        replaceEditor();
    }

    @FXML
    private void actionSave(ActionEvent event) {
        saveModel();
    }

    private void saveModel() {
        JsonElement json = configEditorModel.getConfig();
        saveToFile(json, "Save Model");
    }

    private void saveToFile(JsonElement json, String title) {
        String config = new GsonBuilder().setPrettyPrinting().create().toJson(json);
        fileChooser.setTitle(title);
        File file = fileChooser.showSaveDialog(paneConfig.getScene().getWindow());
        if (file == null) {
            return;
        }
        setCurrentFile(file);
        try {
            FileUtils.writeStringToFile(file, config, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            LOGGER.error("Failed to write file.", ex);
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("failed to write file");
            alert.setContentText(ex.getLocalizedMessage());
            alert.showAndWait();
        }
    }

    private void replaceEditor() {
        paneConfig.setContent(configEditorModel.getGuiFactoryFx().getNode());
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configEditorModel = ConfigEditors
                .buildEditorFromClass(DefModel.class, null, null)
                .orElse(editorNull);
        paneRoot.setOnDragOver(event -> {
            if (event.getGestureSource() != paneRoot && event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY);
            }
            event.consume();
        });
        paneRoot.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasFiles()) {
                List<File> files = db.getFiles();
                loadFromFile(files.get(0));
                success = true;
            }
            event.setDropCompleted(success);
            event.consume();
        });
        replaceEditor();
    }

    public void close() {
        LOGGER.info("Received close, shutting down executor.");
        List<Runnable> remaining = executor.shutdownNow();
        LOGGER.info("Remaining threads: {}", remaining.size());
    }
}
