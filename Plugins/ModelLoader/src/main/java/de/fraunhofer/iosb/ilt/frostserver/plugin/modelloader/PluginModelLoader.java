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
package de.fraunhofer.iosb.ilt.frostserver.plugin.modelloader;

import static de.fraunhofer.iosb.ilt.frostserver.plugin.modelloader.ModelLoaderSettings.PLUGIN_NAME;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.fraunhofer.iosb.ilt.frostserver.model.ModelRegistry;
import de.fraunhofer.iosb.ilt.frostserver.model.loader.DefEntityProperty;
import de.fraunhofer.iosb.ilt.frostserver.model.loader.DefEntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.loader.DefModel;
import de.fraunhofer.iosb.ilt.frostserver.persistence.PersistenceManager;
import de.fraunhofer.iosb.ilt.frostserver.persistence.PersistenceManagerFactory;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.PostgresPersistenceManager;
import de.fraunhofer.iosb.ilt.frostserver.plugin.coremodel.CoreModelSettings;
import de.fraunhofer.iosb.ilt.frostserver.plugin.coremodel.PluginCoreModel;
import de.fraunhofer.iosb.ilt.frostserver.service.PluginModel;
import de.fraunhofer.iosb.ilt.frostserver.service.PluginRootDocument;
import de.fraunhofer.iosb.ilt.frostserver.service.Service;
import de.fraunhofer.iosb.ilt.frostserver.service.ServiceRequest;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import de.fraunhofer.iosb.ilt.frostserver.settings.Settings;
import de.fraunhofer.iosb.ilt.frostserver.util.LiquibaseUser;
import de.fraunhofer.iosb.ilt.frostserver.util.SecurityModel;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.UpgradeFailedException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author scf
 */
public class PluginModelLoader implements PluginRootDocument, PluginModel, LiquibaseUser {

    private static final Logger LOGGER = LoggerFactory.getLogger(PluginModelLoader.class.getName());

    private CoreSettings settings;
    private boolean enabled;
    private boolean fullyInitialised;
    private String idTypeDefault;
    private List<DefModel> modelDefinitions = new ArrayList<>();
    private Map<String, DefEntityProperty> primaryKeys;

    private String liquibasePath;
    private String modelPath;
    private String securityPath;
    private final List<String> conformance = new ArrayList<>();
    private final List<String> liquibaseFiles = new ArrayList<>();
    private final List<String> securityFiles = new ArrayList<>();

    public PluginModelLoader() {
        LOGGER.info("Creating new ModelLoader Plugin.");
    }

    @Override
    public void init(CoreSettings settings) {
        this.settings = settings;
        Settings pluginSettings = settings.getPluginSettings();
        enabled = pluginSettings.getBoolean(ModelLoaderSettings.TAG_ENABLE_MODELLOADER, ModelLoaderSettings.class);
        if (enabled) {
            idTypeDefault = pluginSettings.get(CoreModelSettings.TAG_ID_TYPE_DEFAULT, CoreModelSettings.class).toUpperCase();
            settings.getPluginManager().registerPlugin(this);

            String liquibaseString = pluginSettings.get(ModelLoaderSettings.TAG_LIQUIBASE_FILES, ModelLoaderSettings.class);
            liquibaseFiles.addAll(Arrays.asList(StringUtils.split(liquibaseString.trim(), ", ")));
            liquibasePath = pluginSettings.get(ModelLoaderSettings.TAG_LIQUIBASE_PATH, ModelLoaderSettings.class);

            modelPath = pluginSettings.get(ModelLoaderSettings.TAG_MODEL_PATH, ModelLoaderSettings.class);
            String modelFilesString = pluginSettings.get(ModelLoaderSettings.TAG_MODEL_FILES, ModelLoaderSettings.class);
            String[] modelFiles = StringUtils.split(modelFilesString.trim(), ", ");

            primaryKeys = new HashMap<>();
            for (String fileName : modelFiles) {
                loadModelFile(fileName);
            }
            for (Entry<String, DefEntityProperty> entry : primaryKeys.entrySet()) {
                String typeName = entry.getKey();
                primaryKeys.get(typeName).setType(getTypeFor(settings, typeName));
            }

            securityPath = pluginSettings.get(ModelLoaderSettings.TAG_SECURITY_PATH, ModelLoaderSettings.class);
            String securityFilesString = pluginSettings.get(ModelLoaderSettings.TAG_SECURITY_FILES, ModelLoaderSettings.class);
            securityFiles.addAll(Arrays.asList(StringUtils.split(securityFilesString.trim(), ", ")));
        }
    }

    private void loadModelFile(String fileName) {
        final Path fullPath = Path.of(modelPath, fileName);
        LOGGER.info("Loading model definition from {}", fullPath.toAbsolutePath());
        String data;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            DefModel modelDefinition;
            if (fullPath.toFile().exists()) {
                data = new String(Files.readAllBytes(fullPath), StandardCharsets.UTF_8);
                modelDefinition = objectMapper.readValue(data, DefModel.class);
            } else {
                InputStream stream = getClass().getClassLoader().getResourceAsStream(fullPath.toString());
                modelDefinition = objectMapper.readValue(stream, DefModel.class);
            }
            modelDefinition.init();
            modelDefinitions.add(modelDefinition);
            for (DefEntityType type : modelDefinition.getEntityTypes()) {
                final DefEntityProperty primaryKey = type.getPrimaryKey();
                if (primaryKey != null) {
                    primaryKeys.put(type.getName(), primaryKey);
                }
            }
            conformance.addAll(modelDefinition.getConformance());
        } catch (IOException ex) {
            LOGGER.error("Failed to load model definition", ex);
        }
    }

    @Override
    public void installSecurityDefinitions(PersistenceManager pm) {
        if (pm instanceof PostgresPersistenceManager) {
            for (String fileName : securityFiles) {
                SecurityModel secModel = loadSecurityFile(fileName);
                if (secModel == null) {
                    continue;
                }

                for (SecurityModel.SecurityEntry secEntry : secModel.getEntries()) {
                    pm.addSecurityDefinition(secEntry);
                }
            }
        }
    }

    private SecurityModel loadSecurityFile(String fileName) {
        final Path fullPath = Path.of(securityPath, fileName);
        LOGGER.info("Loading security definition from {}", fullPath.toAbsolutePath());
        String data;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            SecurityModel securityModel;
            if (fullPath.toFile().exists()) {
                data = new String(Files.readAllBytes(fullPath), StandardCharsets.UTF_8);
                securityModel = objectMapper.readValue(data, SecurityModel.class);
            } else {
                InputStream stream = getClass().getClassLoader().getResourceAsStream(fullPath.toString());
                securityModel = objectMapper.readValue(stream, SecurityModel.class);
            }
            return securityModel;
        } catch (IOException ex) {
            LOGGER.error("Failed to load model definition", ex);
        }
        return null;
    }

    @Override
    public void modifyServiceDocument(ServiceRequest request, Map<String, Object> result) {
        Map<String, Object> serverSettings = (Map<String, Object>) result.get(Service.KEY_SERVER_SETTINGS);
        if (serverSettings == null) {
            // Nothing to add to.
            return;
        }
        Set<String> extensionList = (Set<String>) serverSettings.get(Service.KEY_CONFORMANCE_LIST);
        extensionList.addAll(conformance);
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public boolean isFullyInitialised() {
        return fullyInitialised;
    }

    @Override
    public void registerEntityTypes() {
        ModelRegistry modelRegistry = settings.getModelRegistry();
        for (DefModel modelDefinition : modelDefinitions) {
            modelDefinition.registerEntityTypes(modelRegistry);
        }
    }

    @Override
    public boolean linkEntityTypes(PersistenceManager pm) {
        LOGGER.info("Initialising Model Types...");
        ModelRegistry modelRegistry = settings.getModelRegistry();
        for (DefModel modelDefinition : modelDefinitions) {
            modelDefinition.linkEntityTypes(modelRegistry);
            pm.addModelMapping(modelDefinition);
        }

        // Done, release the model definition.
        modelDefinitions = null;
        fullyInitialised = true;
        return true;
    }

    public String getTypeFor(CoreSettings settings, String entityTypeName) {
        Settings pluginSettings = settings.getPluginSettings();
        return pluginSettings.get(PLUGIN_NAME + ".idType." + entityTypeName, idTypeDefault).toUpperCase();
    }

    public Map<String, Object> createLiqibaseParams(PostgresPersistenceManager ppm, Map<String, Object> target) {
        if (target == null) {
            target = new LinkedHashMap<>();
        }
        PluginCoreModel pCoreModel = settings.getPluginManager().getPlugin(PluginCoreModel.class);
        if (pCoreModel != null) {
            pCoreModel.createLiqibaseParams(ppm, target);
        }
        for (Entry<String, DefEntityProperty> entry : primaryKeys.entrySet()) {
            String typeName = entry.getKey();
            ppm.generateLiquibaseVariables(target, typeName, getTypeFor(settings, typeName));
        }
        return target;
    }

    @Override
    public String checkForUpgrades() {
        try (PersistenceManager pm = PersistenceManagerFactory.getInstance(settings).create()) {
            if (pm instanceof PostgresPersistenceManager) {
                PostgresPersistenceManager ppm = (PostgresPersistenceManager) pm;
                StringBuilder result = new StringBuilder();
                for (String file : liquibaseFiles) {
                    result.append(ppm.checkForUpgrades(liquibasePath + File.separatorChar + file, createLiqibaseParams(ppm, null)));
                }
                return result.toString();
            } else {
                return "Unknown persistence manager class";
            }
        }
    }

    @Override
    public boolean doUpgrades(Writer out) throws UpgradeFailedException, IOException {
        try (PersistenceManager pm = PersistenceManagerFactory.getInstance(settings).create()) {
            if (pm instanceof PostgresPersistenceManager) {
                PostgresPersistenceManager ppm = (PostgresPersistenceManager) pm;
                for (String file : liquibaseFiles) {
                    if (!ppm.doUpgrades(liquibasePath + File.separatorChar + file, createLiqibaseParams(ppm, null), out)) {
                        return false;
                    }
                }
                return true;
            }
            out.append("Unknown persistence manager class");
            return false;
        }
    }

}
