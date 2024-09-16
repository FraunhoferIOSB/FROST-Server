/*
 * Copyright (C) 2024 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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
package de.fraunhofer.iosb.ilt.frostserver.model.core;

import de.fraunhofer.iosb.ilt.configurable.annotations.ConfigurableField;
import de.fraunhofer.iosb.ilt.configurable.editor.EditorString;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.TimeInstant;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.IncompleteEntityException;
import net.time4j.Moment;

/**
 * A Validator for Task.createTime. Sets the createTime to the current time when
 * not set.
 */
public class TimeDefaultValidator implements EntityValidator {

    @ConfigurableField(editor = EditorString.class,
            label = "propertyName", description = "Name of the property to auto fill")
    @EditorString.EdOptsString()
    private String propertyName;

    private EntityPropertyMain epTime;

    @Override
    public void validate(Entity entity) throws IncompleteEntityException {
        if (epTime == null) {
            epTime = entity.getEntityType().getEntityProperty(propertyName);
        }
        if (entity.getProperty(epTime) == null) {
            entity.setProperty(epTime, new TimeInstant(Moment.nowInSystemTime()));
        }
    }

    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

}
