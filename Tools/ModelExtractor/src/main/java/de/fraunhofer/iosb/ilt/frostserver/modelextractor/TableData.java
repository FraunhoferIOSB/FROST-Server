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
package de.fraunhofer.iosb.ilt.frostserver.modelextractor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import org.apache.commons.text.CaseUtils;
import org.jooq.Field;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TableData {

    private static final Logger LOGGER = LoggerFactory.getLogger(TableData.class.getName());
    final String tableName;
    final Map<String, List<ForeignKeyData>> refsToOther = new TreeMap<>();
    final Map<String, List<ForeignKeyData>> refsFromOther = new TreeMap<>();
    private final Map<String, FieldData> fields = new TreeMap<>();
    private int pkSize;
    private Node node;
    private TableDataController controller;
    private final String setPostFix;

    public TableData(String tableName, String setPostFix) {
        this.tableName = tableName;
        this.setPostFix = setPostFix;
        getNode();
    }

    public Node getNode() {
        if (node == null) {
            try {
                FXMLLoader loader = new FXMLLoader(TableDataController.class.getResource("/fxml/TableData.fxml"));
                node = (Pane) loader.load();
                controller = loader.getController();
                controller.setTableName(tableName);
                String prettyName = CaseUtils.toCamelCase(tableName, true, '_');
                controller.setSingular(prettyName);
                controller.setPlural(prettyName + setPostFix);
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

    public FXMLController.TableChoice getTableType() {
        return controller.getChoice();
    }

    public boolean isEntityType() {
        return controller.getChoice() == FXMLController.TableChoice.ENTITY_TYPE;
    }

    public boolean isLinkTable() {
        return controller.getChoice() == FXMLController.TableChoice.LINK_TABLE;
    }

    public boolean isIgnored() {
        return controller.getChoice() == FXMLController.TableChoice.IGNORE;
    }

    public void addReferenceToOther(ForeignKeyData fk) {
        refsToOther.computeIfAbsent(fk.otherTableName, t -> new ArrayList<>()).add(fk);
    }

    public void addReferenceFromOther(ForeignKeyData fk) {
        refsFromOther.computeIfAbsent(fk.myTableName, t -> new ArrayList<>()).add(fk);
    }

    public void setPkSize(int pkSize) {
        this.pkSize = pkSize;
    }

    public void analyse() {
        if ((pkSize == 0 || pkSize > 1) && refsToOther.size() >= 1) {
            controller.setChoice(FXMLController.TableChoice.LINK_TABLE);
            return;
        }
        if (pkSize == 1 && refsFromOther.size() + refsToOther.size() > 0) {
            controller.setChoice(FXMLController.TableChoice.ENTITY_TYPE);
            return;
        }
        controller.setChoice(FXMLController.TableChoice.IGNORE);
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
