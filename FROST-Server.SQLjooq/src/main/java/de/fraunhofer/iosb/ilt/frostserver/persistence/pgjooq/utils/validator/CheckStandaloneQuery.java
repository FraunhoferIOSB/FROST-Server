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
package de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.validator;

import de.fraunhofer.iosb.ilt.configurable.annotations.ConfigurableField;
import de.fraunhofer.iosb.ilt.configurable.editor.EditorString;
import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.model.core.EntitySet;
import de.fraunhofer.iosb.ilt.frostserver.parser.query.QueryParser;
import de.fraunhofer.iosb.ilt.frostserver.path.PathElementEntitySet;
import de.fraunhofer.iosb.ilt.frostserver.path.ResourcePath;
import de.fraunhofer.iosb.ilt.frostserver.path.Version;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.JooqPersistenceManager;
import de.fraunhofer.iosb.ilt.frostserver.query.Query;
import de.fraunhofer.iosb.ilt.frostserver.query.QueryDefaults;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.DynamicContext;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import de.fraunhofer.iosb.ilt.frostserver.util.user.PrincipalExtended;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A check that runs a stand-alone query.
 */
public class CheckStandaloneQuery implements ValidationCheck, UserCondition {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(CheckStandaloneQuery.class);

    @ConfigurableField(editor = EditorString.class,
            label = "EntityType", description = "The EntityType to query.")
    @EditorString.EdOptsString
    private String entityTypeName;

    @ConfigurableField(editor = EditorString.class,
            label = "Query", description = "The Query runs without direct context.")
    @EditorString.EdOptsString
    private String query;

    private ResourcePath path;
    private EntityType entityType;
    private DynamicContext context;
    private Query parsedQuery;

    @Override
    public boolean check(JooqPersistenceManager pm, Entity contextEntity) {
        return isValid(pm);
    }

    private void init(JooqPersistenceManager pm) {
        entityType = pm.getCoreSettings().getModelRegistry().getEntityTypeForName(entityTypeName, true);
        final CoreSettings coreSettings = pm.getCoreSettings();
        final QueryDefaults queryDefaults = coreSettings.getQueryDefaults();
        path = new ResourcePath(queryDefaults.getServiceRootUrl(), Version.V_1_1, "/" + entityType.plural)
                .addPathElement(new PathElementEntitySet(entityType));
        context = new DynamicContext();
        parsedQuery = QueryParser.parseQuery(getQuery(), coreSettings, path, PrincipalExtended.INTERNAL_ADMIN_PRINCIPAL, context)
                .validate(entityType);
        LOGGER.info("Initialised check on {}", entityType);
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getEntityTypeName() {
        return entityTypeName;
    }

    public void setEntityTypeName(String entityTypeName) {
        this.entityTypeName = entityTypeName;
    }

    @Override
    public String toString() {
        return "CheckQuery: " + entityType + ": " + query;
    }

    @Override
    public boolean isValid(JooqPersistenceManager pm) {
        if (parsedQuery == null) {
            init(pm);
        }
        try {
            final PrincipalExtended userPrincipal = PrincipalExtended.getLocalPrincipal();
            context.setUser(userPrincipal);
            // Run the Query as Admin user.
            PrincipalExtended.setLocalPrincipal(PrincipalExtended.INTERNAL_ADMIN_PRINCIPAL);
            EntitySet result = pm.get(path, parsedQuery, EntitySet.class);
            // Restore the non-admin user as current user.
            PrincipalExtended.setLocalPrincipal(userPrincipal);
            final boolean valid = result != null && !result.isEmpty();
            LOGGER.debug("  Check on {}: {}", entityType, valid);
            return valid;
        } finally {
            context.clear();
        }
    }

}
