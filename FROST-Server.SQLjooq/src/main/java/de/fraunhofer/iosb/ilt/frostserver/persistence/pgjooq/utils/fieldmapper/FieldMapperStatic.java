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
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 */
package de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.fieldmapper;

import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.PostgresPersistenceManager;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.StaTableDynamic;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.DataSize;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.PropertyFieldRegistry;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityProperty;
import de.fraunhofer.iosb.ilt.frostserver.property.Property;
import org.jooq.Record;

/**
 *
 * @author hylke
 */
public class FieldMapperStatic implements FieldMapper {

    /**
     * The value for the static field. Can be any type.
     */
    private Object value;

    @Override
    public void registerField(PostgresPersistenceManager ppm, StaTableDynamic staTable, Property property) {
        // Does nothing in a static context.
    }

    @Override
    public <J extends Comparable<J>> void registerMapping(PostgresPersistenceManager ppm, StaTableDynamic<J> staTable, final Property property) {
        if (!(property instanceof EntityProperty)) {
            throw new IllegalArgumentException("Property must be an EntityProperty, got: " + property);
        }
        EntityProperty entityProperty = (EntityProperty) property;
        PropertyFieldRegistry<J, StaTableDynamic<J>> pfReg = staTable.getPropertyFieldRegistry();
        pfReg.addEntry(
                entityProperty,
                null,
                new PropertyFieldRegistry.ConverterRecordDeflt<>(
                        (StaTableDynamic<J> table, Record tuple, Entity entity, DataSize dataSize) -> {
                            entity.setProperty(property, value);
                        }, null, null));
    }

    /**
     * The value for the static field. Can be any type.
     *
     * @return the value
     */
    public Object getValue() {
        return value;
    }

    /**
     * The value for the static field. Can be any type.
     *
     * @param value the value to set
     */
    public void setValue(Object value) {
        this.value = value;
    }

}
