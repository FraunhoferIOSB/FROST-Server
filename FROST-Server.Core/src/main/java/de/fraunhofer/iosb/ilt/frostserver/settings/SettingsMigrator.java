/*
 * Copyright (C) 2021 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 */
package de.fraunhofer.iosb.ilt.frostserver.settings;

import static de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings.PREFIX_PERSISTENCE;
import static de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings.PREFIX_PLUGINS;
import de.fraunhofer.iosb.ilt.frostserver.util.Constants;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Converts old settings to new settings.
 *
 * @author hylke
 */
public class SettingsMigrator {

    private static final Logger LOGGER = LoggerFactory.getLogger(SettingsMigrator.class.getName());

    private final Map<String, Map<String, ReplaceList>> valueChanges = new HashMap<>();
    private final Map<String, String> keyChanges = new HashMap<>();

    private Map<String, ReplaceList> getReplaceValue(String key) {
        return valueChanges.computeIfAbsent(key, t -> new TreeMap<>());
    }

    public SettingsMigrator() {
        String valuePpm = "de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.PostgresPersistenceManager";
        final String keyCoreModelidType = PREFIX_PLUGINS + "coreModel.idType";
        final String keyPersistenceImpCls = PREFIX_PERSISTENCE + PersistenceSettings.TAG_IMPLEMENTATION_CLASS;
        ReplaceList ppm = ReplaceList.make().put(keyPersistenceImpCls, valuePpm);
        ReplaceList ppmLong = ppm.copy().put(keyCoreModelidType, Constants.VALUE_ID_TYPE_LONG);
        ReplaceList ppmStrg = ppm.copy().put(keyCoreModelidType, Constants.VALUE_ID_TYPE_STRING);
        ReplaceList ppmUuid = ppm.copy().put(keyCoreModelidType, Constants.VALUE_ID_TYPE_UUID);

        Map<String, ReplaceList> pmc = getReplaceValue(keyPersistenceImpCls);
        pmc.put("de.fraunhofer.iosb.ilt.sta.persistence.postgres.PostgresPersistenceManager", ppmLong);
        pmc.put("de.fraunhofer.iosb.ilt.sta.persistence.postgres.longid.PostgresPersistenceManagerLong", ppmLong);
        pmc.put("de.fraunhofer.iosb.ilt.sta.persistence.postgres.stringid.PostgresPersistenceManagerString", ppmStrg);
        pmc.put("de.fraunhofer.iosb.ilt.sta.persistence.postgres.uuidid.PostgresPersistenceManagerUuid", ppmUuid);
        pmc.put("de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.imp.PostgresPersistenceManagerLong", ppmLong);
        pmc.put("de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.imp.PostgresPersistenceManagerString", ppmStrg);
        pmc.put("de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.imp.PostgresPersistenceManagerUuid", ppmUuid);
        pmc.put("de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.imp.PostgresPersistenceManagerLong", ppmLong);
        pmc.put("de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.imp.PostgresPersistenceManagerString", ppmStrg);
        pmc.put("de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.imp.PostgresPersistenceManagerUuid", ppmUuid);

        keyChanges.put("enableActuation", "plugins.actuation.enable");
        keyChanges.put("enableMultiDatastream", "plugins.multiDatastream.enable");
        keyChanges.put("persistence.alwaysOrderbyId", "alwaysOrderbyId");
    }

    public void migrateOldSettings(Properties properties) {
        for (Map.Entry<String, String> change : keyChanges.entrySet()) {
            migrateOldSettings(properties, change.getKey(), change.getValue());
        }
        for (Map.Entry<String, Map<String, ReplaceList>> change : valueChanges.entrySet()) {
            migrateOldSettings(properties, change.getKey(), change.getValue());
        }
    }

    private void migrateOldSettings(Properties properties, String key, Map<String, ReplaceList> replaces) {
        String oldValue = properties.getProperty(key);
        ReplaceList newValues = replaces.get(oldValue);
        if (newValues != null) {
            LOGGER.warn("Converting settings of key '{}' with old value '{}'", key, oldValue);
            properties.remove(key);
            for (Map.Entry<String, String> entry : newValues.getItems().entrySet()) {
                final String newKey = entry.getKey();
                final String newVal = entry.getValue();
                LOGGER.warn("                Adding key '{}' : '{}'", newKey, newVal);
                properties.put(newKey, newVal);
            }
        }
    }

    private void migrateOldSettings(Properties properties, String oldKey, String newKey) {
        Object oldValue = properties.get(oldKey);
        if (oldValue != null) {
            LOGGER.warn("Converting setting with old key: {} to new key: {} with value: {}", oldKey, newKey, oldValue);
            properties.remove(oldKey);
            properties.put(newKey, oldValue);
        }
    }

    public static void migrate(Properties properties) {
        new SettingsMigrator().migrateOldSettings(properties);
    }

    private static class ReplaceList {

        private final Map<String, String> items = new HashMap<>();

        static ReplaceList make() {
            return new ReplaceList();
        }

        public Map<String, String> getItems() {
            return items;
        }

        public ReplaceList put(String key, String value) {
            items.put(key, value);
            return this;
        }

        public ReplaceList copy() {
            ReplaceList copy = make();
            copy.items.putAll(items);
            return copy;
        }

    }

}
