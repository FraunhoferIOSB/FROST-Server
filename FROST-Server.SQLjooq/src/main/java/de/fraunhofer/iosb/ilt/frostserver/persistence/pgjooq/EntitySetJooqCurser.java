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
import org.jooq.Cursor;
import org.jooq.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hylke
 */
public class EntitySetJooqCurser implements EntitySet, Iterator<Entity> {

    private static final Logger LOGGER = LoggerFactory.getLogger(EntitySetJooqCurser.class.getName());

    private final Query staQuery;
    private final QueryState queryState;
    private final Cursor<Record> results;
    private final DataSize size;
    private final ResultBuilder resultBuilder;

    private String nextLink;
    private long count = -1;
    private int fetchedCount = 0;
    private int maxFetch;

    private final EntityType type;
    private NavigationPropertyMain.NavigationPropertyEntitySet navigationProperty;

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
        nextLink = UrlHelper.generateNextLink(staQuery.getPath(), staQuery, maxFetch);
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
        return this;
    }

    @Override
    public boolean hasNext() {
        return results.hasNext() && maxFetch > fetchedCount;
    }

    @Override
    public Entity next() {
        fetchedCount++;
        Record tuple = results.fetchNext();
        Entity entity = queryState.entityFromQuery(tuple, size);
        if (size.isExceeded()) {
            LOGGER.debug("Size limit reached: {} > {}.", size.getDataSize(), size.getMaxSize());
            maxFetch = fetchedCount;
            if (results.hasNext()) {
                generateNextLink();
            }
            results.close();
        } else if (fetchedCount >= maxFetch) {
            if (results.hasNext()) {
                generateNextLink();
            }
            results.close();
        }
        entity.setQuery(staQuery);
        resultBuilder.expandEntity(entity, staQuery);
        return entity;
    }

}
