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

import de.fraunhofer.iosb.ilt.sta.model.Sensor;
import de.fraunhofer.iosb.ilt.sta.model.builder.core.NamedDsHoldingEntityBuilder;

/**
 * Builder class for Sensor objects.
 *
 * @author jab
 */
public class SensorBuilder extends NamedDsHoldingEntityBuilder<Sensor, SensorBuilder> {

    private String encodingType;
    private Object metadata;

    public SensorBuilder setEncodingType(String encodingType) {
        this.encodingType = encodingType;
        return this;
    }

    public SensorBuilder setMetadata(Object metadata) {
        this.metadata = metadata;
        return this;
    }

    @Override
    protected SensorBuilder getThis() {
        return this;
    }

    @Override
    public Sensor build() {
        Sensor sensor = new Sensor();
        super.build(sensor);
        sensor.setEncodingType(encodingType);
        sensor.setMetadata(metadata);
        return sensor;
    }

}
