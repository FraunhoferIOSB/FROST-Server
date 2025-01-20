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
package de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.validator;

import de.fraunhofer.iosb.ilt.configurable.annotations.ConfigurableField;
import de.fraunhofer.iosb.ilt.configurable.editor.EditorString;
import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.ModelRegistry;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
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
 * A check that runs a query against the context entity.
 */
public class CheckEntityQuery implements ValidationCheck {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(CheckEntityQuery.class);

    @ConfigurableField(editor = EditorString.class,
            label = "Query", description = "The Query runs against the entity being edited.")
    @EditorString.EdOptsString
    private String query;

    private EntityType entityType;
    private DynamicContext context;
    private Query parsedQuery;

    @Override
    public boolean check(JooqPersistenceManager pm, Entity contextEntity) {
        if (parsedQuery == null) {
            init(contextEntity, pm);
        }
        final PrincipalExtended localPrincipal = PrincipalExtended.getLocalPrincipal();
        try {
            context.setEntity(contextEntity);
            context.setUser(localPrincipal);

            // Run the actual query as admin, but with the user in the context.
            PrincipalExtended.setLocalPrincipal(PrincipalExtended.INTERNAL_ADMIN_PRINCIPAL);
            final Entity result = pm.get(entityType, contextEntity.getPrimaryKeyValues(), parsedQuery);
            PrincipalExtended.setLocalPrincipal(localPrincipal);

            final boolean valid = result != null;
            LOGGER.debug("  Check on {}: {}", entityType, valid);
            return valid;
        } finally {
            // Ensure the user is re-set, in case of an exception.
            PrincipalExtended.setLocalPrincipal(localPrincipal);
            context.clear();
        }
    }

    private synchronized void init(Entity contextEntity, JooqPersistenceManager pm) {
        if (parsedQuery != null) {
            return;
        }
        try {
            entityType = contextEntity.getEntityType();
            final CoreSettings coreSettings = pm.getCoreSettings();
            final ResourcePath path = new ResourcePath("", Version.V_1_1, '/' + entityType.plural)
                    .addPathElement(new PathElementEntitySet(entityType));
            context = new DynamicContext();
            final QueryDefaults queryDefaults = coreSettings.getQueryDefaults();
            final ModelRegistry modelRegistry = coreSettings.getModelRegistry();
            parsedQuery = QueryParser.parseQuery(getQuery(), queryDefaults, modelRegistry, path, PrincipalExtended.INTERNAL_ADMIN_PRINCIPAL, context)
                    .validate(null, entityType);
            LOGGER.info("Initialised check on {}", entityType);
        } catch (RuntimeException ex) {
            LOGGER.error("Failed to initialise check.", ex);
            throw new IllegalStateException("Failed to initialise Check Query.");
        }
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    @Override
    public String toString() {
        return "CheckQuery: " + entityType + ": " + query;
    }

}
