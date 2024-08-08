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
package de.fraunhofer.iosb.ilt.frostserver.plugin.actuation;

import static de.fraunhofer.iosb.ilt.frostserver.plugin.coremodel.PluginCoreModel.NAME_EP_CREATIONTIME;

import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.model.core.EntityValidator;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.TimeInstant;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.IncompleteEntityException;
import net.time4j.Moment;

/**
 * A Validator for Task.createTime. Sets the createTime to the current time when
 * not set.
 */
public class CreateTimeValidator implements EntityValidator {

    private EntityPropertyMain epCreationTime;

    @Override
    public void validate(Entity entity) throws IncompleteEntityException {
        if (epCreationTime == null) {
            epCreationTime = entity.getEntityType().getEntityProperty(NAME_EP_CREATIONTIME);
        }
        if (entity.getProperty(epCreationTime) == null) {
            entity.setProperty(epCreationTime, new TimeInstant(Moment.nowInSystemTime()));
        }
    }

}
