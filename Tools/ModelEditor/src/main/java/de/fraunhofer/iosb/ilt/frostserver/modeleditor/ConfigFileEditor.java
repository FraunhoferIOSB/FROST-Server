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

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import de.fraunhofer.iosb.ilt.configurable.ConfigEditor;
import de.fraunhofer.iosb.ilt.configurable.ConfigEditors;
import de.fraunhofer.iosb.ilt.configurable.editor.EditorNull;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import javafx.scene.control.Alert;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hylke
 */
public class ConfigFileEditor {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigFileEditor.class.getName());

    private final EditorNull editorNull = new EditorNull();
    private ConfigEditor<?> configEditorModel;
    private final FileChooser fileChooser = new FileChooser();
    private final Class<?> editorClass;
    private File currentFile;

    public ConfigFileEditor(Class<?> editorClass) {
        this.editorClass = editorClass;
    }

    public File getCurrentFile() {
        return currentFile;
    }

    public void setCurrentFile(File file) {
        currentFile = file;
        if (currentFile != null) {
            fileChooser.setInitialDirectory(currentFile.getParentFile());
            fileChooser.setInitialFileName(currentFile.getName());
        }
    }

    public void loadFromFileWithChooser(String title, Window window) {
        fileChooser.setTitle(title);
        File file = fileChooser.showOpenDialog(window);
        loadFromFile(file);
    }

    public void loadFromFile(File file) {
        setCurrentFile(file);
        loadFromCurrentFile();
    }

    public void loadFromCurrentFile() {
        if (currentFile == null) {
            return;
        }
        try {
            String config = FileUtils.readFileToString(currentFile, StandardCharsets.UTF_8);
            loadJson(config);
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
    }

    public void saveModelWithChooser(String title, Window window) {
        JsonElement json = configEditorModel.getConfig();
        saveToFileWithChooser(json, title, window);
    }

    public void saveToFileWithChooser(JsonElement json, String title, Window window) {
        String config = new GsonBuilder().setPrettyPrinting().create().toJson(json);
        fileChooser.setTitle(title);
        File file = fileChooser.showSaveDialog(window);
        if (file == null) {
            return;
        }
        setCurrentFile(file);
        saveToCurrentFile(config);
    }

    public String saveToCurrentFile() {
        JsonElement json = configEditorModel.getConfig();
        String config = new GsonBuilder().setPrettyPrinting().create().toJson(json);
        saveToCurrentFile(config);
        return config;
    }

    private void saveToCurrentFile(String config) {
        if (currentFile == null) {
            throw new IllegalArgumentException("No current file to save to!");
        }
        try {
            FileUtils.writeStringToFile(currentFile, config, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            LOGGER.error("Failed to write file.", ex);
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("failed to write file");
            alert.setContentText(ex.getLocalizedMessage());
            alert.showAndWait();
        }
    }

    public ConfigEditor<?> getConfigEditor() {
        return configEditorModel;
    }

    public void initialize() {
        configEditorModel = ConfigEditors
                .buildEditorFromClass(editorClass, null, null)
                .orElse(editorNull);
    }
}
