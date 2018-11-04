/*
 * Copyright (C) 2018 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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
package de.fraunhofer.iosb.ilt.sta.persistence.postgres.factories;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.SimpleExpression;
import de.fraunhofer.iosb.ilt.sta.model.FeatureOfInterest;
import de.fraunhofer.iosb.ilt.sta.path.EntityProperty;
import de.fraunhofer.iosb.ilt.sta.path.EntityType;
import de.fraunhofer.iosb.ilt.sta.path.Property;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.DataSize;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.EntityFactories;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.EntityFromTupleFactory;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.Utils;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.relationalpaths.AbstractQFeatures;
import de.fraunhofer.iosb.ilt.sta.query.Query;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * @author Hylke van der Schaaf
 * @param <I> The type of path used for the ID fields.
 * @param <J> The type of the ID fields.
 */
public class FeatureOfInterestFactory<I extends SimpleExpression<J> & Path<J>, J> implements EntityFromTupleFactory<FeatureOfInterest, I, J> {

    private final EntityFactories<I, J> factories;
    private final AbstractQFeatures<?, I, J> qInstance;

    public FeatureOfInterestFactory(EntityFactories<I, J> factories, AbstractQFeatures<?, I, J> qInstance) {
        this.factories = factories;
        this.qInstance = qInstance;
    }

    @Override
    public FeatureOfInterest create(Tuple tuple, Query query, DataSize dataSize) {
        Set<Property> select = query == null ? Collections.emptySet() : query.getSelect();
        FeatureOfInterest entity = new FeatureOfInterest();
        J id = factories.getIdFromTuple(tuple, qInstance.getId());
        if (id != null) {
            entity.setId(factories.idFromObject(id));
        }
        entity.setName(tuple.get(qInstance.name));
        entity.setDescription(tuple.get(qInstance.description));
        String encodingType = tuple.get(qInstance.encodingType);
        entity.setEncodingType(encodingType);
        if (select.isEmpty() || select.contains(EntityProperty.FEATURE)) {
            String locationString = tuple.get(qInstance.feature);
            dataSize.increase(locationString == null ? 0 : locationString.length());
            entity.setFeature(Utils.locationFromEncoding(encodingType, locationString));
        }
        if (select.isEmpty() || select.contains(EntityProperty.PROPERTIES)) {
            String props = tuple.get(qInstance.properties);
            entity.setProperties(Utils.jsonToObject(props, Map.class));
        }
        return entity;
    }

    @Override
    public I getPrimaryKey() {
        return qInstance.getId();
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.FEATUREOFINTEREST;
    }

}
