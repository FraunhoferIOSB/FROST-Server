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
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.PostgresPersistenceManager;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.factories.EntityFactories;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.validator.HookValidator;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.validator.SecurityTableWrapper;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author scf
 */
public class TableCollection {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(TableCollection.class);

    private ModelRegistry modelRegistry;
    private boolean initialised = false;

    /**
     * The model definition, stored here as long as the PersistenceManager has
     * not initialised itself using it.
     */
    private List<DefModel> modelDefinitions;
    private Map<String, SecurityTableWrapper> securityDefinitions;
    private Map<String, List<HookValidator>> hookValidators;

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

    /**
     * Initialise the TableCollection.
     *
     * @param ppm The PersistenceManager to initialise the TableCollection for.
     * @return True if the called caused the TableCollection to be initialised,
     * false if the TableCollection was already initialised and the call made no
     * changes.
     */
    public boolean init(PostgresPersistenceManager ppm) {
        if (initialised) {
            return false;
        }
        synchronized (this) {
            if (!initialised) {
                initialised = true;
                final EntityFactories entityFactories = ppm.getEntityFactories();
                for (StaMainTable<?> table : getAllTables()) {
                    LOGGER.info("  Table: {}.", table.getName());
                    table.initProperties(entityFactories);
                    table.initRelations();
                    initSecurityWrapper(table);
                    initHookValidators(table, ppm);
                }
                return true;
            }
            return false;
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

    public void initSecurityWrapper(StaMainTable table) {
        if (securityDefinitions == null) {
            return;
        }
        SecurityTableWrapper stw = securityDefinitions.get(table.getName());
        if (stw == null) {
            return;
        }
        table.setSecurityWrapper(stw);
    }

    public void addSecurityWrapper(String tableName, SecurityTableWrapper w) {
        if (securityDefinitions == null) {
            securityDefinitions = new HashMap<>();
        }
        securityDefinitions.put(tableName, w);
    }

    public void initHookValidators(StaMainTable table, PostgresPersistenceManager ppm) {
        if (hookValidators == null) {
            return;
        }
        final List<HookValidator> hvList = hookValidators.get(table.getName());
        if (hvList == null) {
            return;
        }
        for (HookValidator hv : hvList) {
            hv.registerHooks(table, ppm);
        }
    }

    public void addHookValidator(String tableName, HookValidator hv) {
        if (hookValidators == null) {
            hookValidators = new HashMap<>();
        }
        hookValidators.computeIfAbsent(tableName, tn -> new ArrayList<>())
                .add(hv);
    }
}
