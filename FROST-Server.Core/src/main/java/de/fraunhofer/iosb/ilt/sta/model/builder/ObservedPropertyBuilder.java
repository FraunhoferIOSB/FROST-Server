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
import de.fraunhofer.iosb.ilt.sta.model.MultiDatastream;
import de.fraunhofer.iosb.ilt.sta.model.ObservedProperty;
import de.fraunhofer.iosb.ilt.sta.model.core.EntitySet;
import de.fraunhofer.iosb.ilt.sta.model.core.EntitySetImpl;
import de.fraunhofer.iosb.ilt.sta.path.EntityType;
import java.util.HashMap;
import java.util.Map;

/**
 * Builder class for ObservedProperty objects.
 *
 * @author jab
 */
public class ObservedPropertyBuilder extends AbstractEntityBuilder<ObservedProperty, ObservedPropertyBuilder> {

    private String name;
    private String definition;
    private String description;
    private Map<String, Object> properties;
    private EntitySet<Datastream> datastreams;
    private EntitySet<MultiDatastream> multiDatastreams;

    public ObservedPropertyBuilder() {
        properties = new HashMap<>();
        datastreams = new EntitySetImpl<>(EntityType.DATASTREAM);
        multiDatastreams = new EntitySetImpl<>(EntityType.MULTIDATASTREAM);
    }

    public ObservedPropertyBuilder setName(String name) {
        this.name = name;
        return this;
    }

    public ObservedPropertyBuilder setDefinition(String definition) {
        this.definition = definition;
        return this;
    }

    public ObservedPropertyBuilder setDescription(String description) {
        this.description = description;
        return this;
    }

    public ObservedPropertyBuilder setProperties(Map<String, Object> properties) {
        this.properties = properties;
        return this;
    }

    public ObservedPropertyBuilder addProperty(String name, Object value) {
        this.properties.put(name, value);
        return this;
    }

    @Override
    protected ObservedPropertyBuilder getThis() {
        return this;
    }

    /**
     * @param datastreams the datastreams to set
     * @return This builder.
     */
    public ObservedPropertyBuilder setDatastreams(EntitySet<Datastream> datastreams) {
        this.datastreams = datastreams;
        return this;
    }

    public ObservedPropertyBuilder addDatastream(Datastream datastream) {
        this.datastreams.add(datastream);
        return this;
    }

    public ObservedPropertyBuilder setMultiDatastreams(EntitySet<MultiDatastream> multiDatastreams) {
        this.multiDatastreams = multiDatastreams;
        return this;
    }

    public ObservedPropertyBuilder addMultiDatastream(MultiDatastream multiDatastream) {
        this.multiDatastreams.add(multiDatastream);
        return this;
    }

    @Override
    public ObservedProperty build() {
        ObservedProperty op = new ObservedProperty(id);
        op.setSelfLink(selfLink);
        op.setNavigationLink(navigationLink);
        op.setName(name);
        op.setDefinition(definition);
        op.setDescription(description);
        op.setProperties(properties);
        op.setDatastreams(datastreams);
        op.setMultiDatastreams(multiDatastreams);
        op.setExportObject(isExportObject());
        return op;
    }

}
