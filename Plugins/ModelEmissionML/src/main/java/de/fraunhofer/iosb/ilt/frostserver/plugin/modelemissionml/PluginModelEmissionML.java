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
package de.fraunhofer.iosb.ilt.frostserver.plugin.modelemissionml;

import static de.fraunhofer.iosb.ilt.frostserver.property.PropertyAbstract.Option.CUSTOM_PROPS;
import static de.fraunhofer.iosb.ilt.frostserver.property.PropertyAbstract.Option.NULLABLE;
import static de.fraunhofer.iosb.ilt.frostserver.property.PropertyAbstract.Option.REQUIRED;
import static de.fraunhofer.iosb.ilt.frostserver.property.PropertyAbstract.Option.SERIALISE_NULLS;
import static de.fraunhofer.iosb.ilt.frostserver.property.type.TypeSimplePrimitive.EDM_DATETIMEOFFSET;
import static de.fraunhofer.iosb.ilt.frostserver.property.type.TypeSimplePrimitive.EDM_DECIMAL;
import static de.fraunhofer.iosb.ilt.frostserver.property.type.TypeSimplePrimitive.EDM_STRING;
import static de.fraunhofer.iosb.ilt.frostserver.property.type.TypeSimplePrimitive.EDM_UNTYPED;
import static de.fraunhofer.iosb.ilt.frostserver.service.InitResult.INIT_DELAY;

import de.fraunhofer.iosb.ilt.frostserver.model.ComplexValueImpl;
import de.fraunhofer.iosb.ilt.frostserver.model.ModelRegistry;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.TimeInstant;
import de.fraunhofer.iosb.ilt.frostserver.plugin.coremodel.PluginCoreModel;
import de.fraunhofer.iosb.ilt.frostserver.plugin.coremodelv2.PluginCoreModelV2;
import de.fraunhofer.iosb.ilt.frostserver.plugin.modelloader.PluginModelLoader;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.property.type.TypeComplex;
import de.fraunhofer.iosb.ilt.frostserver.service.InitResult;
import de.fraunhofer.iosb.ilt.frostserver.service.Plugin;
import de.fraunhofer.iosb.ilt.frostserver.service.PluginManager;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import de.fraunhofer.iosb.ilt.settings.ConfigProvider;
import de.fraunhofer.iosb.ilt.settings.Settings;
import de.fraunhofer.iosb.ilt.settings.annotation.DefaultValueBoolean;
import java.math.BigDecimal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.core.TreeNode;

/**
 * Plugin loader for the Observations and Measurements Model plugin.
 */
public class PluginModelEmissionML extends ConfigProvider<PluginModelEmissionML> implements Plugin {

    private static final Logger LOGGER = LoggerFactory.getLogger(PluginModelEmissionML.class.getName());

    public static enum EmissionIntent {
        INTENTIONAL("intentional"),
        UNINTENTIONAL("unintentional"),
        UNKNOWN("unknown");

        private final String label;

        private EmissionIntent(String label) {
            this.label = label;
        }

        public final String getLabel() {
            return label;
        }

        @Override
        public String toString() {
            return getLabel();
        }

    }

    public static final String EM_NAME_DEFINITION = "definition";
    public static final String EM_NAME_LABEL = "label";
    public static final String EM_NAME_SYMBOL = "symbol";
    public static final String EM_NAME_VALUE = "value";
    public static final String EM_NAME_QUALITY = "quality";
    public static final String EM_NAME_UNIT = "unit";
    public static final String EM_NAME_TIME = "time";

    public static final EntityPropertyMain<String> EM_EP_UOM_DEFINITION = new EntityPropertyMain<>(EM_NAME_DEFINITION, EDM_STRING, NULLABLE, SERIALISE_NULLS);
    public static final EntityPropertyMain<String> EM_EP_UOM_LABEL = new EntityPropertyMain<>(EM_NAME_LABEL, EDM_STRING, NULLABLE, SERIALISE_NULLS);
    public static final EntityPropertyMain<String> EM_EP_UOM_SYMBOL = new EntityPropertyMain<>(EM_NAME_SYMBOL, EDM_STRING, NULLABLE, SERIALISE_NULLS);

    public static final EntityPropertyMain<BigDecimal> EM_EP_VALUE = new EntityPropertyMain<>(EM_NAME_VALUE, EDM_DECIMAL, REQUIRED);
    public static final EntityPropertyMain<TreeNode> EM_EP_QUALITY = new EntityPropertyMain<>(EM_NAME_QUALITY, EDM_UNTYPED, NULLABLE, CUSTOM_PROPS);
    public static final EntityPropertyMain<TimeInstant> EM_EP_TIME = new EntityPropertyMain<>(EM_NAME_TIME, EDM_DATETIMEOFFSET, REQUIRED);

    public static final TypeComplex EM_TYPE_UOM = new TypeComplex("UnitOfMeasurement", "The Unit Of Measurement Type", false)
            .registerProperty(EM_EP_UOM_LABEL)
            .registerProperty(EM_EP_UOM_DEFINITION)
            .registerProperty(EM_EP_UOM_SYMBOL);
    public static final EntityPropertyMain<ComplexValueImpl> EM_EP_UNIT = new EntityPropertyMain<>(EM_NAME_UNIT, EM_TYPE_UOM, REQUIRED);

    public static final TypeComplex EM_TYPE_EMISSION_QUANTITY = new TypeComplex("EmissionQuantity", "The EmissionML EmissionQuantity Type", false)
            .registerProperty(EM_EP_VALUE)
            .registerProperty(EM_EP_QUALITY)
            .registerProperty(EM_EP_UNIT);
    public static final TypeComplex EM_TYPE_TEMPORALBOUND = new TypeComplex("TemporalBound", "The EmissionML TemporalBound Type", false)
            .registerProperty(EM_EP_TIME)
            .registerProperty(EM_EP_QUALITY);

    public static final String EM_NAMESPACE = "modelEmissionML.";
    @DefaultValueBoolean(false)
    public static final String TAG_ENABLE_EMML = "enable";

    private boolean enabled;

    @Override
    public InitResult init(CoreSettings settings) {
        Settings pluginSettings = settings.getPluginSettings();
        setSettings(pluginSettings.getSubSettings(EM_NAMESPACE));

        enabled = getBoolean(TAG_ENABLE_EMML);
        if (enabled) {
            final ModelRegistry mr = settings.getModelRegistry();
            final PluginManager pluginManager = settings.getPluginManager();
            boolean modelV1 = pluginManager.isPluginEnabled(PluginCoreModel.class);
            boolean modelV2 = pluginManager.isPluginEnabled(PluginCoreModelV2.class);
            PluginModelLoader pml = pluginManager.getPlugin(PluginModelLoader.class);
            if (pml == null || !pml.isEnabled()) {
                LOGGER.warn("PluginModelLoader must be enabled before the EmissionML plugin, delaying initialisation...");
                return INIT_DELAY;
            }
            if (!modelV1 && !modelV2) {
                LOGGER.warn("PluginCoreModel or PluginCoreModelV2 must be enabled before the EmissionML plugin, delaying initialisation...");
                return INIT_DELAY;
            }
            if (modelV2) {
                mr.registerPropertyType(EM_TYPE_UOM);
                mr.registerPropertyType(EM_TYPE_TEMPORALBOUND);
                mr.registerPropertyType(EM_TYPE_EMISSION_QUANTITY);
                pml.addLiquibaseFile("pluginModelEmissionml/v2/liquibase/tables.xml");
                pml.addModelFile("pluginModelEmissionml/v2/model/Mechanism.json");
                pml.addModelFile("pluginModelEmissionml/v2/model/DeterminationMethod.json");
                pml.addModelFile("pluginModelEmissionml/v2/model/EmissionEvent.json");
            }
            pluginManager.registerPlugin(this);
        }
        return InitResult.INIT_OK;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

}
