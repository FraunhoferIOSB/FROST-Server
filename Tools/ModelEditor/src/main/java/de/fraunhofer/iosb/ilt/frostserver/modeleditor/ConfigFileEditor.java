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
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.stage.FileChooser;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hylke
 */
public class ConfigFileEditor {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigFileEditor.class.getName());

    private final ScrollPane paneConfig;
    private final Label labelFile;

    private final EditorNull editorNull = new EditorNull();
    private ConfigEditor<?> configEditorModel;
    private final FileChooser fileChooser = new FileChooser();
    private final Class<?> editorClass;

    public ConfigFileEditor(Class<?> editorClass, ScrollPane paneConfig, Label labelFile) {
        this.paneConfig = paneConfig;
        this.labelFile = labelFile;
        this.editorClass = editorClass;
    }

    public void setCurrentFile(File file) {
        File currentFile = file;
        if (currentFile != null) {
            labelFile.setText(currentFile.getAbsolutePath());
            fileChooser.setInitialDirectory(currentFile.getParentFile());
            fileChooser.setInitialFileName(currentFile.getName());
        }
    }

    public void loadFromFile(String title) {
        fileChooser.setTitle(title);
        File file = fileChooser.showOpenDialog(paneConfig.getScene().getWindow());
        loadFromFile(file);
    }

    public void loadFromFile(File file) {
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

    public void loadJson(String config) {
        JsonElement json = JsonParser.parseString(config);
        if (json == null) {
            return;
        }
        configEditorModel = ConfigEditors
                .buildEditorFromClass(editorClass, null, null)
                .orElse(editorNull);
        configEditorModel.setConfig(json);
        replaceEditor();
    }

    public void saveModel(String title) {
        JsonElement json = configEditorModel.getConfig();
        saveToFile(json, title);
    }

    public void saveToFile(JsonElement json, String title) {
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

    public void replaceEditor() {
        paneConfig.setContent(configEditorModel.getGuiFactoryFx().getNode());
    }

    public void initialize() {
        configEditorModel = ConfigEditors
                .buildEditorFromClass(editorClass, null, null)
                .orElse(editorNull);
        paneConfig.setOnDragOver(event -> {
            if (event.getGestureSource() != paneConfig && event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY);
            }
            event.consume();
        });
        paneConfig.setOnDragDropped(event -> {
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
}
