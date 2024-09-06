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
package de.fraunhofer.iosb.ilt.frostserver.plugin.modelloader;

import static de.fraunhofer.iosb.ilt.frostserver.model.ext.TypeReferencesHelper.TYPE_REFERENCE_MAP;
import static de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.LiquibaseHelper.CHANGE_SET_NAME;
import static de.fraunhofer.iosb.ilt.frostserver.plugin.modelloader.ModelLoaderSettings.PLUGIN_NAME;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.fraunhofer.iosb.ilt.frostserver.model.ModelRegistry;
import de.fraunhofer.iosb.ilt.frostserver.model.loader.DefEntityProperty;
import de.fraunhofer.iosb.ilt.frostserver.model.loader.DefEntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.loader.DefModel;
import de.fraunhofer.iosb.ilt.frostserver.persistence.PersistenceManager;
import de.fraunhofer.iosb.ilt.frostserver.persistence.PersistenceManagerFactory;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.JooqPersistenceManager;
import de.fraunhofer.iosb.ilt.frostserver.plugin.coremodel.CoreModelSettings;
import de.fraunhofer.iosb.ilt.frostserver.plugin.coremodel.PluginCoreModel;
import de.fraunhofer.iosb.ilt.frostserver.service.InitResult;
import de.fraunhofer.iosb.ilt.frostserver.service.PluginModel;
import de.fraunhofer.iosb.ilt.frostserver.service.PluginRootDocument;
import de.fraunhofer.iosb.ilt.frostserver.service.Service;
import de.fraunhofer.iosb.ilt.frostserver.service.ServiceRequest;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import de.fraunhofer.iosb.ilt.frostserver.settings.Settings;
import de.fraunhofer.iosb.ilt.frostserver.util.LiquibaseUser;
import de.fraunhofer.iosb.ilt.frostserver.util.SecurityModel;
import de.fraunhofer.iosb.ilt.frostserver.util.StringHelper;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.UpgradeFailedException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.apache.commons.io.FileUtils;
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
    private final Map<String, DefEntityProperty> primaryKeys = new HashMap<>();

    private String modelPath;
    private final List<String> modelFiles = new ArrayList<>();

<<<<<<< HEAD
    private String modelPath;

=======
>>>>>>> v2.x
    private String liquibasePath;
    private final List<String> liquibaseFiles = new ArrayList<>();

    private String securityPath;
    private final List<String> securityFiles = new ArrayList<>();

    private String metadataPath;
    private String metadataStringData;
    private final List<String> metadataFiles = new ArrayList<>();
    private Map<String, Object> metadataExtra;
    private final List<String> conformance = new ArrayList<>();
<<<<<<< HEAD

    public PluginModelLoader() {
        LOGGER.info("Creating new ModelLoader Plugin.");
    }
=======
>>>>>>> v2.x

    @Override
    public InitResult init(CoreSettings settings) {
        this.settings = settings;
        Settings pluginSettings = settings.getPluginSettings();
        enabled = pluginSettings.getBoolean(ModelLoaderSettings.TAG_ENABLE_MODELLOADER, ModelLoaderSettings.class);
        if (enabled) {
            idTypeDefault = pluginSettings.get(CoreModelSettings.TAG_ID_TYPE_DEFAULT, CoreModelSettings.class).toUpperCase();
            settings.getPluginManager().registerPlugin(this);

            liquibasePath = pluginSettings.get(ModelLoaderSettings.TAG_LIQUIBASE_PATH, ModelLoaderSettings.class);
            String liquibaseString = pluginSettings.get(ModelLoaderSettings.TAG_LIQUIBASE_FILES, ModelLoaderSettings.class);
            liquibaseFiles.addAll(Arrays.asList(StringUtils.split(liquibaseString.trim(), ", ")));

            modelPath = pluginSettings.get(ModelLoaderSettings.TAG_MODEL_PATH, ModelLoaderSettings.class);
            String modelFilesString = pluginSettings.get(ModelLoaderSettings.TAG_MODEL_FILES, ModelLoaderSettings.class);
            for (var modelFile : StringUtils.split(modelFilesString.trim(), ", ")) {
                addModelFileWithPath(modelFile);
            }

            securityPath = pluginSettings.get(ModelLoaderSettings.TAG_SECURITY_PATH, ModelLoaderSettings.class);
            String securityFilesString = pluginSettings.get(ModelLoaderSettings.TAG_SECURITY_FILES, ModelLoaderSettings.class);
            securityFiles.addAll(Arrays.asList(StringUtils.split(securityFilesString.trim(), ", ")));

            metadataStringData = pluginSettings.get(ModelLoaderSettings.TAG_METADATA_DATA, ModelLoaderSettings.class);
            metadataPath = pluginSettings.get(ModelLoaderSettings.TAG_METADATA_PATH, ModelLoaderSettings.class);
            String metadataFilesString = pluginSettings.get(ModelLoaderSettings.TAG_METADATA_FILES, ModelLoaderSettings.class);
            metadataFiles.addAll(Arrays.asList(StringUtils.split(metadataFilesString.trim(), ", ")));
<<<<<<< HEAD
=======
        }
        return InitResult.INIT_OK;
    }

    private void loadModelFiles() {
        for (String fileName : modelFiles) {
            loadModelFile(fileName);
        }
        for (Entry<String, DefEntityProperty> entry : primaryKeys.entrySet()) {
            String typeName = entry.getKey();
            primaryKeys.get(typeName).setType(getTypeFor(settings, typeName));
>>>>>>> v2.x
        }
    }

    private void addModelFileWithPath(String fileName) {
        final File fullFile;
        if (StringHelper.isNullOrEmpty(modelPath)) {
            fullFile = new File(fileName);
        } else {
            fullFile = new File(modelPath, fileName);
        }
        modelFiles.add(fullFile.toString());
    }

    public void addModelFile(String filename) {
        modelFiles.add(filename);
    }

    public void addSecurityFile(String filename) {
        securityFiles.add(filename);
    }

    public void addLiquibaseFile(String filename) {
        liquibaseFiles.add(filename);
    }

    public void loadModelFile(String fullPathString) {
        File fullFile = new File(fullPathString);
        LOGGER.info("Loading model definition from {}", fullFile.toString());
        String data;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            DefModel modelDefinition;
            if (fullFile.exists()) {
                data = FileUtils.readFileToString(fullFile, StandardCharsets.UTF_8);
                modelDefinition = objectMapper.readValue(data, DefModel.class);
            } else {
                InputStream stream = getClass().getClassLoader().getResourceAsStream(fullFile.toString());
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
        if (pm instanceof JooqPersistenceManager) {
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
        final File fullFile = new File(securityPath, fileName);
        LOGGER.info("Loading security definition from {}", fullFile.toString());
        String data;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            SecurityModel securityModel;
            if (fullFile.exists()) {
                data = FileUtils.readFileToString(fullFile, StandardCharsets.UTF_8);
                securityModel = objectMapper.readValue(data, SecurityModel.class);
            } else {
                InputStream stream = getClass().getClassLoader().getResourceAsStream(fullFile.toString());
                if (stream == null) {
                    LOGGER.info("  Not found: {}", fullFile);
                    return null;
                }
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
        if (metadataExtra == null) {
            metadataExtra = loadExtraMetadata();
        }
        Set<String> extensionList = (Set<String>) serverSettings.get(Service.KEY_CONFORMANCE_LIST);
        extensionList.addAll(conformance);
        mergeJson(serverSettings, metadataExtra);
    }

    private Map<String, Object> loadExtraMetadata() {
        final ObjectMapper objectMapper = new ObjectMapper();
        final Map<String, Object> extraMetadata = new LinkedHashMap<>();

        // local copy for thread safety
        final String localMetadataStringData = metadataStringData;
        if (!StringHelper.isNullOrEmpty(localMetadataStringData)) {
            try {
                mergeJson(extraMetadata, objectMapper.readValue(localMetadataStringData, TYPE_REFERENCE_MAP));
            } catch (JsonProcessingException ex) {
                LOGGER.error("Failed to parse extra metadata.", ex);
            }
            // Free global String data.
            metadataStringData = null;
        }
        for (String fileName : metadataFiles) {
            mergeJson(extraMetadata, loadExtraMetadataFile(fileName));
        }
        return extraMetadata;
    }

    private void mergeJson(Map<String, Object> target, Map<String, Object> toMerge) {
        if (toMerge == null) {
            return;
        }
        for (var entry : toMerge.entrySet()) {
            String name = entry.getKey();
            Object valueToMerge = entry.getValue();
            Object valueTarget = target.get(name);
            if (valueTarget == null) {
                target.put(name, valueToMerge);
            } else if (valueTarget instanceof Map toMap && valueToMerge instanceof Map mergeMap) {
                mergeJson(toMap, mergeMap);
            } else if (valueTarget instanceof List toList && valueToMerge instanceof List mergeList) {
                toList.addAll(mergeList);
            } else if (valueTarget instanceof Set toSet && valueToMerge instanceof List mergeList) {
                toSet.addAll(mergeList);
            } else {
                LOGGER.warn("Keeping value for {}, target already contains a value: {}; Ignoring {}", name, valueTarget, valueToMerge);
            }
        }
    }

    private Map<String, Object> loadExtraMetadataFile(String fileName) {
        final File fullFile = new File(metadataPath, fileName);
        LOGGER.info("Loading extra landing page meta data from {}", fullFile.toString());
        String data;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            if (fullFile.exists()) {
                data = FileUtils.readFileToString(fullFile, StandardCharsets.UTF_8);
                return objectMapper.readValue(data, TYPE_REFERENCE_MAP);
            } else {
                InputStream stream = getClass().getClassLoader().getResourceAsStream(fullFile.toString());
                if (stream == null) {
                    LOGGER.info("  Not found: {}", fullFile);
                } else {
                    return objectMapper.readValue(stream, TYPE_REFERENCE_MAP);
                }
            }
        } catch (IOException ex) {
            LOGGER.error("Failed to load extra metadata", ex);
        }
        return Collections.emptyMap();
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
        loadModelFiles();

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

    public Map<String, Object> createLiqibaseParams(JooqPersistenceManager jpm, Map<String, Object> target) {
        if (target == null) {
            target = new LinkedHashMap<>();
        }
        PluginCoreModel pCoreModel = settings.getPluginManager().getPlugin(PluginCoreModel.class);
        if (pCoreModel != null) {
            pCoreModel.createLiqibaseParams(jpm, target);
        }
        for (Entry<String, DefEntityProperty> entry : primaryKeys.entrySet()) {
            String typeName = entry.getKey();
            jpm.generateLiquibaseVariables(target, typeName, getTypeFor(settings, typeName));
        }
        return target;
    }

    @Override
    public String checkForUpgrades() {
        if (!isFullyInitialised()) {
            return "ModelLoader not fully initialised yet.";
        }
        try (PersistenceManager pm = PersistenceManagerFactory.getInstance(settings).create()) {
            if (pm instanceof JooqPersistenceManager jpm) {
                StringBuilder result = new StringBuilder();
                for (String file : liquibaseFiles) {
                    final Map<String, Object> liquibaseParams = createLiqibaseParams(jpm, null);
                    liquibaseParams.put(CHANGE_SET_NAME, file);
                    liquibaseParams.put("searchPath", liquibasePath);
                    result.append(jpm.checkForUpgrades(file, liquibaseParams));
                }
                return result.toString();
            } else {
                return "Unknown persistence manager class";
            }
        }
    }

    @Override
    public boolean doUpgrades(Writer out) throws UpgradeFailedException, IOException {
        if (!isFullyInitialised()) {
            out.append("ModelLoader not fully initialised yet.");
            return false;
        }
        try (PersistenceManager pm = PersistenceManagerFactory.getInstance(settings).create()) {
            if (pm instanceof JooqPersistenceManager jpm) {
                for (String file : liquibaseFiles) {
                    final Map<String, Object> liquibaseParams = createLiqibaseParams(jpm, null);
                    liquibaseParams.put("searchPath", liquibasePath);
                    if (!jpm.doUpgrades(file, liquibaseParams, out)) {
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
