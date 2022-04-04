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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq;

import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.model.core.EntitySet;
import de.fraunhofer.iosb.ilt.frostserver.path.UrlHelper;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.DataSize;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.QueryState;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.query.Query;
import java.util.Iterator;
import java.util.NoSuchElementException;
import org.jooq.Cursor;
import org.jooq.Record;
import org.jooq.exception.DataAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hylke
 */
public class EntitySetJooqCurser implements EntitySet {

    private static final Logger LOGGER = LoggerFactory.getLogger(EntitySetJooqCurser.class.getName());

    private final Query staQuery;
    private final QueryState queryState;
    private final Cursor<Record> results;
    private final DataSize size;
    private final ResultBuilder resultBuilder;

    private String nextLink;
    private long count = -1;
    private int maxFetch;

    private final EntityType type;
    private NavigationPropertyMain.NavigationPropertyEntitySet navigationProperty;

    private CursorIterator iterator;

    public EntitySetJooqCurser(EntityType type, Cursor<Record> results, QueryState queryState, ResultBuilder resultBuilder) {
        this.type = type;
        this.staQuery = resultBuilder.getStaQuery();
        this.results = results;
        this.size = resultBuilder.getDataSize();
        this.queryState = queryState;
        this.resultBuilder = resultBuilder;
        this.maxFetch = staQuery.getTopOrDefault();
    }

    @Override
    public void add(Entity entity) {
        throw new UnsupportedOperationException("Not supported on this implementation.");
    }

    @Override
    public int size() {
        throw new UnsupportedOperationException("Not supported on this implementation.");
    }

    @Override
    public long getCount() {
        return count;
    }

    @Override
    public void setCount(long count) {
        this.count = count;
    }

    @Override
    public String getNextLink() {
        if (nextLink == null && results.hasNext() && maxFetch > 0) {
            generateNextLink();
        }
        return nextLink;
    }

    private void generateNextLink() {
        nextLink = UrlHelper.generateNextLink(resultBuilder.getPath(), staQuery, maxFetch);
    }

    @Override
    public void setNextLink(String nextLink) {
        this.nextLink = nextLink;
    }

    @Override
    public EntityType getEntityType() {
        return type;
    }

    @Override
    public NavigationPropertyMain.NavigationPropertyEntitySet getNavigationProperty() {
        return navigationProperty;
    }

    @Override
    public boolean isEmpty() {
        return !results.hasNext();
    }

    @Override
    public Iterator<Entity> iterator() {
        if (iterator == null) {
            iterator = new CursorIterator(this);
            return iterator;
        }
        throw new IllegalStateException("EntitySetJooqCurser can only be iterated once.");
    }

    private static class CursorIterator implements Iterator<Entity> {

        private final EntitySetJooqCurser parent;
        private int fetchedCount = 0;

        public CursorIterator(EntitySetJooqCurser parent) {
            this.parent = parent;
        }

        @Override
        public boolean hasNext() {
            return parent.results.hasNext() && parent.maxFetch > fetchedCount;
        }

        @Override
        public Entity next() {
            if (!hasNext()) {
                throw new NoSuchElementException("Cursor is closed or empty.");
            }
            fetchedCount++;
            final Entity entity = fetchNext();
            if (parent.size.isExceeded()) {
                LOGGER.debug("Size limit reached: {} > {}.", parent.size.getDataSize(), parent.size.getMaxSize());
                parent.maxFetch = fetchedCount;
                generateNextAndClose(entity);
            } else if (fetchedCount >= parent.maxFetch) {
                generateNextAndClose(entity);
            }
            entity.setQuery(parent.staQuery);
            parent.resultBuilder.expandEntity(entity, parent.staQuery);
            return entity;
        }

        private Entity fetchNext() throws DataAccessException {
            final Record tuple = parent.results.fetchNext();
            return parent.queryState.entityFromQuery(tuple, parent.size);
        }

        private void generateNextLink(Entity last, Entity next) {
            parent.nextLink = UrlHelper.generateNextLink(parent.resultBuilder.getPath(), parent.staQuery, parent.maxFetch, last, next);
        }

        private void generateNextAndClose(Entity entity) throws DataAccessException {
            if (parent.results.hasNext()) {
                final Entity next = fetchNext();
                generateNextLink(entity, next);
            }
            parent.results.close();
        }
    }

}
