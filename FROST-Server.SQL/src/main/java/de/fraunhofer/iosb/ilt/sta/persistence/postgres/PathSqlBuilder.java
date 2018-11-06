/*
 * Copyright (C) 2016 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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
package de.fraunhofer.iosb.ilt.sta.persistence.postgres;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.ComparableExpressionBase;
import com.querydsl.sql.RelationalPathBase;
import com.querydsl.sql.SQLQuery;
import com.querydsl.sql.SQLQueryFactory;
import de.fraunhofer.iosb.ilt.sta.model.core.Id;
import de.fraunhofer.iosb.ilt.sta.path.EntityProperty;
import de.fraunhofer.iosb.ilt.sta.path.EntityType;
import de.fraunhofer.iosb.ilt.sta.path.ResourcePath;
import de.fraunhofer.iosb.ilt.sta.path.ResourcePathVisitor;
import de.fraunhofer.iosb.ilt.sta.query.Query;
import de.fraunhofer.iosb.ilt.sta.settings.PersistenceSettings;
import java.util.Map;

/**
 *
 * @author scf
 */
public interface PathSqlBuilder extends ResourcePathVisitor {

    /**
     * A class that keeps track of the latest table that was joined.
     *
     * @param <I> The type of path used for the ID fields.
     * @param <J> The type of the ID fields.
     */
    public static class TableRef<I extends ComparableExpressionBase<J> & Path<J>, J extends Comparable> {

        private EntityType type;
        private RelationalPathBase<?> qPath;
        private I idPath;

        public TableRef() {
        }

        public TableRef(TableRef<I,J> source) {
            type = source.type;
            qPath = source.qPath;
            idPath = source.idPath;
        }

        public EntityType getType() {
            return type;
        }

        public void setType(EntityType type) {
            this.type = type;
        }

        public void clear() {
            type = null;
            qPath = null;
            idPath = null;
        }

        public TableRef copy() {
            return new TableRef(this);
        }

        public boolean isEmpty() {
            return type == null && qPath == null;
        }

        public RelationalPathBase<?> getqPath() {
            return qPath;
        }

        public void setqPath(RelationalPathBase<?> qPath) {
            this.qPath = qPath;
        }

        public I getIdPath() {
            return idPath;
        }

        public void setIdPath(I idPath) {
            this.idPath = idPath;
        }

    }

    public SQLQuery<Tuple> buildFor(EntityType entityType, Id id, SQLQueryFactory sqlQueryFactory, PersistenceSettings settings);

    public SQLQuery<Tuple> buildFor(ResourcePath path, Query query, SQLQueryFactory sqlQueryFactory, PersistenceSettings settings);

    public void queryEntityType(EntityType type, Id id, TableRef last);

    public Map<String, Expression<?>> expressionsForProperty(EntityProperty property, Path<?> qPath, Map<String, Expression<?>> target);
}
