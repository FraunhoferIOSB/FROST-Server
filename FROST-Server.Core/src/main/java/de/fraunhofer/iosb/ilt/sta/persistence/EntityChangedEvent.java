/*
 * Copyright (C) 2016 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131 Karlsruhe, Germany.
 *  * This program is free software: you can redistribute it and/or modify  * it under the terms of the GNU Lesser General Public License as published by  * the Free Software Foundation, either version 3 of the License, or  * (at your option) any later version.
 *  * This program is distributed in the hope that it will be useful,  * but WITHOUT ANY WARRANTY; without even the implied warranty of  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
See the
 * GNU Lesser General Public License for more details.
 *  * You should have received a copy of the GNU Lesser General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.fraunhofer.iosb.ilt.sta.persistence;

import de.fraunhofer.iosb.ilt.sta.model.core.Entity;
import java.util.EventObject;

/**
 *
 * @author jab
 */
public class EntityChangedEvent extends EventObject {

    private final Entity oldEntity;
    private final Entity newEntity;

    public EntityChangedEvent(PersistenceManager source, Entity oldEntity, Entity newEntity) {
        super(source);
        this.oldEntity = oldEntity;
        this.newEntity = newEntity;
    }

    @Override
    public PersistenceManager getSource() {
        return (PersistenceManager) source;
    }

    public Entity getOldEntity() {
        return oldEntity;
    }

    public Entity getNewEntity() {
        return newEntity;
    }

}
