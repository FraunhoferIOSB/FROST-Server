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

import de.fraunhofer.iosb.ilt.frostserver.model.loader.DefModel;
import de.fraunhofer.iosb.ilt.frostserver.util.SecurityModel;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.AnchorPane;
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
    @FXML
    private ScrollPane paneConfigSec;
    @FXML
    private Button buttonLoadSec;
    @FXML
    private Button buttonSaveSec;
    @FXML
    private Label labelFileSec;

    private ConfigFileEditor cfeDataModel;
    private ConfigFileEditor cfeSecurity;

    @FXML
    private void actionLoad(ActionEvent event) {
        cfeDataModel.loadFromFile("Load Data Model");
    }

    @FXML
    private void actionSave(ActionEvent event) {
        cfeDataModel.saveModel("Save Data Model");
    }

    @FXML
    private void actionLoadSec(ActionEvent event) {
        cfeSecurity.loadFromFile("Load Security Model");
    }

    @FXML
    private void actionSaveSec(ActionEvent event) {
        cfeSecurity.saveModel("Save Security Model");
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cfeDataModel = new ConfigFileEditor(DefModel.class, paneConfig, labelFile);
        cfeDataModel.initialize();
        cfeSecurity = new ConfigFileEditor(SecurityModel.class, paneConfigSec, labelFileSec);
        cfeSecurity.initialize();
    }

    public void close() {
        LOGGER.info("Received close, shutting down.");
    }
}
