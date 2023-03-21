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
package de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables;

import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.ModelRegistry;
import de.fraunhofer.iosb.ilt.frostserver.model.loader.DefModel;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.factories.EntityFactories;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author scf
 */
public class TableCollection {

    private ModelRegistry modelRegistry;
    private boolean initialised = false;

    /**
     * The model definition, stored here as long as the PersistenceManager has
     * not initialised itself using it.
     */
    private List<DefModel> modelDefinitions;

    private final Map<EntityType, StaMainTable<?>> tablesByType = new LinkedHashMap<>();
    private final Map<Class<?>, StaTable<?>> tablesByClass = new LinkedHashMap<>();
    private final Map<String, StaTable<?>> tablesByName = new LinkedHashMap<>();

    public TableCollection setModelRegistry(ModelRegistry modelRegistry) {
        this.modelRegistry = modelRegistry;
        return this;
    }

    public StaMainTable<?> getTableForType(EntityType type) {
        return tablesByType.get(type);
    }

    public <T extends StaTable<T>> T getTableForClass(Class<T> clazz) {
        return (T) tablesByClass.get(clazz);
    }

    public StaTable<?> getTableForName(String name) {
        return tablesByName.get(name);
    }

    public Collection<StaMainTable<?>> getAllTables() {
        return tablesByType.values();
    }

    public void registerTable(EntityType type, StaTableAbstract<?> table) {
        tablesByType.put(type, table);
        tablesByClass.put(table.getClass(), table);
        tablesByName.put(table.getName(), table);
        table.init(modelRegistry, this);
    }

    public void registerTable(StaLinkTable<?> table) {
        tablesByClass.put(table.getClass(), table);
        tablesByName.put(table.getName(), table);
    }

    public void init(EntityFactories entityFactories) {
        if (initialised) {
            return;
        }
        synchronized (this) {
            if (!initialised) {
                initialised = true;
                for (StaMainTable<?> table : getAllTables()) {
                    table.initProperties(entityFactories);
                    table.initRelations();
                }
            }
        }
    }

    /**
     * @return the tablesByType
     */
    public Map<EntityType, StaMainTable<?>> getTablesByType() {
        return tablesByType;
    }

    /**
     * The model definitions, stored here as long as the PersistenceManager has
     * not initialised itself using them.
     *
     * @return the modelDefinitions
     */
    public List<DefModel> getModelDefinitions() {
        if (modelDefinitions == null) {
            modelDefinitions = new ArrayList<>();
        }
        return modelDefinitions;
    }

    /**
     * clears the list of model definitions, and makes it immutable.
     */
    public void clearModelDefinitions() {
        this.modelDefinitions = Collections.emptyList();
    }

}
