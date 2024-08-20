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

import de.fraunhofer.iosb.ilt.frostserver.modelextractor.FXMLController.TableChoice;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class TableDataController implements Initializable {

    @FXML
    private ChoiceBox<TableChoice> choiceBoxTableType;

    @FXML
    private Label labelTableName;

    @FXML
    private TextField textFieldPlural;

    @FXML
    private TextField textFieldSingular;

    public String getPlural() {
        return textFieldPlural.getText();
    }

    public void setPlural(String plural) {
        textFieldPlural.setText(plural);
    }

    public String getSingular() {
        return textFieldSingular.getText();
    }

    public void setSingular(String singular) {
        textFieldSingular.setText(singular);
    }

    public TableChoice getChoice() {
        return choiceBoxTableType.getValue();
    }

    public void setChoice(TableChoice choice) {
        choiceBoxTableType.setValue(choice);
    }

    public void setTableName(String tableName) {
        labelTableName.setText(tableName);
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        choiceBoxTableType.setItems(FXCollections.observableArrayList(TableChoice.values()));
        choiceBoxTableType.getSelectionModel().selectedItemProperty().addListener((ov, o, n) -> {
            textFieldSingular.setVisible(n == TableChoice.ENTITY_TYPE);
            textFieldPlural.setVisible(n == TableChoice.ENTITY_TYPE);
        });
    }

}
