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
package de.fraunhofer.iosb.ilt.sta.model.builder;

import de.fraunhofer.iosb.ilt.sta.model.Datastream;
import de.fraunhofer.iosb.ilt.sta.model.Sensor;
import de.fraunhofer.iosb.ilt.sta.model.core.EntitySet;
import de.fraunhofer.iosb.ilt.sta.model.core.EntitySetImpl;
import de.fraunhofer.iosb.ilt.sta.path.EntityType;

/**
 * Builder class for Sensor objects.
 *
 * @author jab
 */
public class SensorBuilder extends AbstractEntityBuilder<Sensor, SensorBuilder> {

    private String name;
    private String description;
    private String encodingType;
    private Object metadata;
    private EntitySet<Datastream> datastreams;

    public SensorBuilder() {
        datastreams = new EntitySetImpl<>(EntityType.Datastream);
    }

    public SensorBuilder setName(String name) {
        this.name = name;
        return this;
    }

    public SensorBuilder setDescription(String description) {
        this.description = description;
        return this;
    }

    public SensorBuilder setEncodingType(String encodingType) {
        this.encodingType = encodingType;
        return this;
    }

    public SensorBuilder setMetadata(Object metadata) {
        this.metadata = metadata;
        return this;
    }

    public SensorBuilder setDatastreams(EntitySet<Datastream> datastreams) {
        this.datastreams = datastreams;
        return this;
    }

    public SensorBuilder addDatastream(Datastream datastream) {
        this.datastreams.add(datastream);
        return this;
    }

    @Override
    protected SensorBuilder getThis() {
        return this;
    }

    @Override
    public Sensor build() {
        Sensor sensor = new Sensor(id, selfLink, navigationLink, name, description, encodingType, metadata, datastreams);
        sensor.setExportObject(isExportObject());
        return sensor;
    }

}
