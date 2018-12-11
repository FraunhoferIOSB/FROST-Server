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
package de.fraunhofer.iosb.ilt.sta.model.builder.core;

import de.fraunhofer.iosb.ilt.sta.model.Datastream;
import de.fraunhofer.iosb.ilt.sta.model.MultiDatastream;
import de.fraunhofer.iosb.ilt.sta.model.core.EntitySet;
import de.fraunhofer.iosb.ilt.sta.model.core.EntitySetImpl;
import de.fraunhofer.iosb.ilt.sta.model.core.NamedDsHoldingEntity;
import de.fraunhofer.iosb.ilt.sta.path.EntityType;

/**
 * @author scf
 * @param <U> Type of class to build.
 * @param <V> Type of the builder class (will be recursive)
 */
public abstract class NamedDsHoldingEntityBuilder<U extends NamedDsHoldingEntity<U>, V extends AbstractEntityBuilder<U, V>> extends NamedEntityBuilder<U, V> {

    private EntitySet<Datastream> datastreams;
    private EntitySet<MultiDatastream> multiDatastreams;

    public NamedDsHoldingEntityBuilder() {
        datastreams = new EntitySetImpl<>(EntityType.DATASTREAM);
        multiDatastreams = new EntitySetImpl<>(EntityType.MULTIDATASTREAM);
    }

    public V setDatastreams(EntitySet<Datastream> datastreams) {
        this.datastreams = datastreams;
        return getThis();
    }

    public V addDatastream(Datastream datastream) {
        this.datastreams.add(datastream);
        return getThis();
    }

    public V setMultiDatastreams(EntitySet<MultiDatastream> multiDatastreams) {
        this.multiDatastreams = multiDatastreams;
        return getThis();
    }

    public V addMultiDatastream(MultiDatastream multiDatastream) {
        this.multiDatastreams.add(multiDatastream);
        return getThis();
    }

    @Override
    protected U build(U entity) {
        super.build(entity);
        entity.setDatastreams(datastreams);
        entity.setMultiDatastreams(multiDatastreams);
        return entity;
    }

}
