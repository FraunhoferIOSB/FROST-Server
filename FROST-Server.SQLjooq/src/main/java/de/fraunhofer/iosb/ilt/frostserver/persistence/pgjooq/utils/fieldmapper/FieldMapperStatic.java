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
import de.fraunhofer.iosb.ilt.frostserver.model.loader.DefEntityProperty;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.PostgresPersistenceManager;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.StaMainTable;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.DataSize;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.PropertyFieldRegistry;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityProperty;
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

    private DefEntityProperty parent;

    @Override
    public void setParent(DefEntityProperty parent) {
        this.parent = parent;
    }

    @Override
    public void registerField(PostgresPersistenceManager ppm, StaMainTable staTable) {
        // Does nothing in a static context.
    }

    @Override
    public <J extends Comparable<J>, T extends StaMainTable<J, T>> void registerMapping(PostgresPersistenceManager ppm, T staTable) {
        final EntityProperty entityProperty = parent.getEntityProperty();
        PropertyFieldRegistry<J, T> pfReg = staTable.getPropertyFieldRegistry();
        pfReg.addEntry(
                entityProperty,
                null,
                new PropertyFieldRegistry.ConverterRecordDeflt<>(
                        (T table, Record tuple, Entity entity, DataSize dataSize) -> {
                            entity.setProperty(entityProperty, value);
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
