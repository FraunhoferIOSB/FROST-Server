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
package de.fraunhofer.iosb.ilt.frostserver.mcp.subscription;

import de.fraunhofer.iosb.ilt.frostserver.model.DefaultEntity;
import de.fraunhofer.iosb.ilt.frostserver.model.EntityChangedMessage;
import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.model.core.PkValue;
import de.fraunhofer.iosb.ilt.frostserver.path.PathElementEntity;
import de.fraunhofer.iosb.ilt.frostserver.path.ResourcePath;
import de.fraunhofer.iosb.ilt.frostserver.persistence.PersistenceManager;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.query.Query;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.FunctionRegistry;
import de.fraunhofer.iosb.ilt.frostserver.request.EditFeatures;
import de.fraunhofer.iosb.ilt.frostserver.service.InitResult;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.IncompleteEntityException;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.NoSuchEntityException;
import jakarta.json.JsonPatch;
import java.security.Principal;
import java.util.Collections;
import java.util.List;

/**
 * A minimal persistence manager for the MCP subscription unit tests:
 * entities with id &gt;= 0 exist, others do not. Path-based queries always
 * return null, so match-expression based subscriptions never match; only
 * the cheap matcher paths are exercised in these tests.
 */
public class TestPmMcp implements PersistenceManager {

    private CoreSettings coreSettings;

    @Override
    public void addExpressionHandlers(FunctionRegistry fr) {
        // Mock does not do anything.
    }

    @Override
    public boolean validatePath(ResourcePath path) {
        return true;
    }

    @Override
    public boolean insert(Entity entity, EditFeatures updateMode) throws NoSuchEntityException, IncompleteEntityException {
        return true;
    }

    @Override
    public Entity get(EntityType entityType, PkValue id) {
        if (id == null || id.get(0) == null || ((Number) id.get(0)).longValue() < 0) {
            return null;
        }
        return new DefaultEntity(entityType, id);
    }

    @Override
    public Object get(ResourcePath path, Query query) {
        // The dummy has no stored entities, so path-based lookups find nothing.
        return null;
    }

    @Override
    public boolean delete(PathElementEntity pathElement) throws NoSuchEntityException {
        return true;
    }

    @Override
    public void delete(ResourcePath path, Query query) throws NoSuchEntityException {
        // Mock does not do anything.
    }

    @Override
    public void addRelation(PathElementEntity source, NavigationPropertyMain.NavigationPropertyEntitySet np, Entity target) {
        // Mock does not do anything.
    }

    @Override
    public void setRelation(PathElementEntity source, NavigationPropertyMain.NavigationPropertyEntity np, Entity target) throws NoSuchEntityException {
        // Mock does not do anything.
    }

    @Override
    public void setRelation(PathElementEntity source, NavigationPropertyMain.NavigationPropertyEntitySet np, List<Entity> targets) throws NoSuchEntityException {
        // Mock does not do anything.
    }

    @Override
    public void deleteRelation(PathElementEntity source, NavigationPropertyMain np, PathElementEntity target) {
        // Mock does not do anything.
    }

    @Override
    public boolean update(PathElementEntity pathElement, Entity entity, EditFeatures updateMode) throws NoSuchEntityException, IncompleteEntityException {
        return true;
    }

    @Override
    public boolean update(PathElementEntity pathElement, JsonPatch patch) throws NoSuchEntityException, IncompleteEntityException {
        return true;
    }

    @Override
    public List<EntityChangedMessage> getEntityChangedMessages() {
        return Collections.emptyList();
    }

    @Override
    public InitResult init(CoreSettings settings) {
        coreSettings = settings;
        return InitResult.INIT_OK;
    }

    @Override
    public CoreSettings getCoreSettings() {
        return coreSettings;
    }

    @Override
    public void setRole(Principal user) {
        // Mock does not do anything.
    }

    @Override
    public void commit() {
        // Mock does not do anything.
    }

    @Override
    public void rollback() {
        // Mock does not do anything.
    }

    @Override
    public void close() {
        // Mock does not do anything.
    }

}
