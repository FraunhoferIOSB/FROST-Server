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
package de.fraunhofer.iosb.ilt.frostserver.plugin.projects;

import static de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.Utils.getFieldOrNull;
import static de.fraunhofer.iosb.ilt.frostserver.service.InitResult.INIT_DELAY;

import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.ModelRegistry;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.model.core.PkValue;
import de.fraunhofer.iosb.ilt.frostserver.persistence.PersistenceManager;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.JooqPersistenceManager;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.StaMainTable;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.StaTable;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.TableCollection;
import de.fraunhofer.iosb.ilt.frostserver.plugin.actuation.PluginActuation;
import de.fraunhofer.iosb.ilt.frostserver.plugin.coremodel.PluginCoreModel;
import de.fraunhofer.iosb.ilt.frostserver.plugin.coremodel.TableImpFeatures;
import de.fraunhofer.iosb.ilt.frostserver.plugin.coremodel.TableImpLocations;
import de.fraunhofer.iosb.ilt.frostserver.plugin.coremodelv2.PluginCoreModelV2;
import de.fraunhofer.iosb.ilt.frostserver.plugin.modelloader.PluginModelLoader;
import de.fraunhofer.iosb.ilt.frostserver.plugin.multidatastream.PluginMultiDatastream;
import de.fraunhofer.iosb.ilt.frostserver.service.InitResult;
import de.fraunhofer.iosb.ilt.frostserver.service.PluginManager;
import de.fraunhofer.iosb.ilt.frostserver.service.PluginModel;
import de.fraunhofer.iosb.ilt.frostserver.service.UpdateMode;
import de.fraunhofer.iosb.ilt.frostserver.settings.ConfigDefaults;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import de.fraunhofer.iosb.ilt.frostserver.settings.Settings;
import de.fraunhofer.iosb.ilt.frostserver.settings.annotation.DefaultValueBoolean;
import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record2;
import org.jooq.Result;
import org.jooq.TableField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Plugin loader for the Projects plugin.
 */
public class PluginProjects implements PluginModel, ConfigDefaults {

    private static final Logger LOGGER = LoggerFactory.getLogger(PluginProjects.class.getName());

    @DefaultValueBoolean(false)
    public static final String TAG_ENABLE_PROJECTS = "projects.enable";

    @DefaultValueBoolean(true)
    public static final String TAG_DEFAULT_RULES = "projects.enableDefaultRules";

    @DefaultValueBoolean(false)
    public static final String TAG_UPDATE_FEATURE_WITH_LOCATION = "projects.updateFeatureWithLocation";

    private CoreSettings coreSettings;

    private boolean enabled;
    private boolean autoUpdateFeatures;
    private boolean fullyInitialised;

    @Override
    public InitResult init(CoreSettings coreSettings) {
        this.coreSettings = coreSettings;
        Settings pluginSettings = coreSettings.getPluginSettings();
        enabled = pluginSettings.getBoolean(TAG_ENABLE_PROJECTS, PluginProjects.class);
        if (enabled) {
            boolean rules = pluginSettings.getBoolean(TAG_DEFAULT_RULES, PluginProjects.class);
            final PluginManager pluginManager = coreSettings.getPluginManager();
            PluginModelLoader pml = pluginManager.getPlugin(PluginModelLoader.class);
            boolean pCoreModelV1 = pluginManager.isPluginEnabled(PluginCoreModel.class);
            boolean pCoreModelV2 = pluginManager.isPluginEnabled(PluginCoreModelV2.class);
            boolean multiDatastream = pluginManager.isPluginEnabled(PluginMultiDatastream.class);
            boolean actuation = pluginManager.isPluginEnabled(PluginActuation.class);
            if (pml == null || !pml.isEnabled()) {
                LOGGER.warn("PluginModelLoader must be enabled before the Projects plugin, delaying initialisation...");
                return INIT_DELAY;
            }
            if (!pCoreModelV1) {
                LOGGER.warn("PluginCoreModelV1 must be enabled before the Projects plugin, delaying initialisation...");
                return INIT_DELAY;
            }
            if (pCoreModelV1 && !pCoreModelV2) {
                // Dealing with Model v1.1
                fixSettings(pluginSettings, coreSettings);
                initForCoreModelV1(pml, multiDatastream, actuation, rules, pluginSettings);
            } else if (!pCoreModelV1 && pCoreModelV2) {
                // Dealing with Model v2.0
                LOGGER.error("Projects plugin is not ready for V2 yet!");
                return InitResult.INIT_FAILED;
            } else {
                LOGGER.warn("Either CoreModel or CoreModelV2 must be enabled, delaying initialisation...");
                return INIT_DELAY;
            }
            pluginManager.registerPlugin(this);
        }
        return InitResult.INIT_OK;
    }

    public void fixSettings(Settings pluginSettings, CoreSettings coreSettings1) {
        if (!pluginSettings.containsName("modelLoader.idType.Role")) {
            pluginSettings.set("modelLoader.idType.Role", "STRING");
        }
        if (!pluginSettings.containsName("modelLoader.idType.User")) {
            pluginSettings.set("modelLoader.idType.User", "STRING");
        }
        final Settings settings = coreSettings1.getSettings();
        if (!settings.containsName("persistence.idGenerationMode.Role")) {
            settings.set("persistence.idGenerationMode.Role", "ClientGeneratedOnly");
        }
        if (!settings.containsName("persistence.idGenerationMode.User")) {
            settings.set("persistence.idGenerationMode.User", "ClientGeneratedOnly");
        }
    }

    public void initForCoreModelV1(PluginModelLoader pml, boolean multiDatastream, boolean actuation, boolean rules, Settings pluginSettings) {
        pml.addLiquibaseFile("pluginprojects/sta1/liquibase/tables.xml");

        pml.addModelFile("pluginprojects/sta1/model/Project.json");
        pml.addModelFile("pluginprojects/sta1/model/Restricted.json");
        pml.addModelFile("pluginprojects/sta1/model/Role.json");
        pml.addModelFile("pluginprojects/sta1/model/User.json");
        pml.addModelFile("pluginprojects/sta1/model/UserProjectRole.json");
        if (multiDatastream) {
            pml.addModelFile("pluginprojects/sta1/model/RestrictedMd.json");
        }
        if (actuation) {
            pml.addModelFile("pluginprojects/sta1/model/ProjectActuation.json");
            pml.addModelFile("pluginprojects/sta1/model/RestrictedActuation.json");
        }

        if (rules) {
            pml.addSecurityFile("pluginprojects/sta1/security/secDatastream.json");
            pml.addSecurityFile("pluginprojects/sta1/security/secFeature.json");
            pml.addSecurityFile("pluginprojects/sta1/security/secHistoricalLocation.json");
            pml.addSecurityFile("pluginprojects/sta1/security/secLocation.json");
            if (multiDatastream) {
                pml.addSecurityFile("pluginprojects/sta1/security/secMultiDatastream.json");
            }
            pml.addSecurityFile("pluginprojects/sta1/security/secObservation.json");
            pml.addSecurityFile("pluginprojects/sta1/security/secObservedProperty.json");
            pml.addSecurityFile("pluginprojects/sta1/security/secProject.json");
            pml.addSecurityFile("pluginprojects/sta1/security/secRole.json");
            pml.addSecurityFile("pluginprojects/sta1/security/secSensor.json");
            pml.addSecurityFile("pluginprojects/sta1/security/secThing.json");
            pml.addSecurityFile("pluginprojects/sta1/security/secUser.json");
            pml.addSecurityFile("pluginprojects/sta1/security/secUserProjectRole.json");
            if (actuation) {
                pml.addSecurityFile("pluginprojects/sta1/security/.json");
                pml.addSecurityFile("pluginprojects/sta1/security/.json");
                pml.addSecurityFile("pluginprojects/sta1/security/.json");
            }
        }
        autoUpdateFeatures = pluginSettings.getBoolean(TAG_UPDATE_FEATURE_WITH_LOCATION, PluginProjects.class);
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void registerEntityTypes() {
        // Nothing to do here.
    }

    @Override
    public boolean linkEntityTypes(PersistenceManager pm) {
        if (autoUpdateFeatures) {
            final PluginCoreModel pluginCoreModel = coreSettings.getPluginManager().getPlugin(PluginCoreModel.class);
            if (pluginCoreModel == null || !pluginCoreModel.isFullyInitialised()) {
                return false;
            }
            if (pm instanceof JooqPersistenceManager jpm) {
                StaMainTable<?> tableLocations = jpm.getTableCollection().getTableForType(pluginCoreModel.etLocation);
                tableLocations.registerHookPostUpdate(
                        11.1,
                        (JooqPersistenceManager jpml, Entity entity, PkValue entityId, UpdateMode updateMode) -> relinkProjectsToFeature(jpml, entity));
            }
        }
        fullyInitialised = true;
        return true;
    }

    public static void relinkProjectsToFeature(JooqPersistenceManager jpm, Entity entity) {
        Object genFoiId = null;
        try {
            final TableCollection tableCollection = jpm.getTableCollection();
            final ModelRegistry mr = jpm.getCoreSettings().getModelRegistry();

            EntityType etProject = mr.getEntityTypeForName("Project", true);
            if (etProject == null) {
                LOGGER.error("No Project entity type found!");
                return;
            }
            TableImpLocations ql = tableCollection.getTableForClass(TableImpLocations.class);
            Field<?> lRestricted = ql.field("RESTRICTED");
            final Object locationId = entity.getPrimaryKeyValues().get(0);
            Record2<?, ?> fetchAny = jpm.getDslContext()
                    .select(ql.colGenFoiId, lRestricted)
                    .from(ql)
                    .where(((TableField) ql.getId()).equal(locationId))
                    .fetchAny();
            if (fetchAny == null) {
                LOGGER.error("Location not found in table for id {}", entity.getPrimaryKeyValues());
                return;
            }
            genFoiId = fetchAny.component1();
            if (genFoiId == null) {
                // Location has no genFoiID
                return;
            }

            TableImpFeatures tf = tableCollection.getTableForClass(TableImpFeatures.class);
            TableField fRestricted = (TableField) tf.field("RESTRICTED");
            int updated = jpm.getDslContext()
                    .update(tf)
                    .set(fRestricted, fetchAny.component2())
                    .where(((TableField) tf.getId()).eq(genFoiId))
                    .execute();
            if (updated != 1) {
                LOGGER.warn("Update of generated FoI resulted in {} changed rows!", updated);
            }

            StaTable tfp = tableCollection.getTableForName("FEATURE_PROJECTS");
            TableField tfpPid = (TableField) tfp.field("PROJECT_ID");
            TableField tfpFid = (TableField) tfp.field("FEATURE_ID");
            StaTable tlp = tableCollection.getTableForName("LOCATION_PROJECTS");
            TableField tlpPid = (TableField) tlp.field("PROJECT_ID");
            TableField tlpLid = (TableField) tlp.field("LOCATION_ID");
            if (tfpPid == null || tfpFid == null || tlpPid == null || tlpLid == null) {
                LOGGER.error("Failed to link Projects to generated FoI, linktables do not have correct columns.");
                return;
            }
            int unlinked = jpm.getDslContext()
                    .deleteFrom(tfp)
                    .where(tfpFid.eq(genFoiId))
                    .execute();
            LOGGER.debug("Unlinked {} projects from Feature {}", unlinked, genFoiId);
            Result<Record1<Object>> projectIds = jpm.getDslContext()
                    .select(tlpPid)
                    .from(tlp)
                    .where(tlpLid.eq(locationId))
                    .fetch();
            int linked = 0;
            for (org.jooq.Record pidTuple : projectIds) {
                Object projectId = getFieldOrNull(pidTuple, tlpPid);
                jpm.getDslContext()
                        .insertInto(tfp)
                        .columns(tfpFid, tfpPid)
                        .values(genFoiId, projectId)
                        .execute();
                linked++;
            }
            LOGGER.debug("Linked {} projects from Feature {}", linked, genFoiId);

        } catch (RuntimeException ex) {
            LOGGER.error("Failed to update Projects for generated FoI {} from Location {}.", genFoiId, entity, ex);
        }
    }

    @Override
    public boolean isFullyInitialised() {
        return fullyInitialised;
    }

}
