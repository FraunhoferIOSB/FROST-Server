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
package de.fraunhofer.iosb.ilt.sta.model.builder.core;

import de.fraunhofer.iosb.ilt.sta.model.core.Entity;
import de.fraunhofer.iosb.ilt.sta.model.core.Id;

/**
 * Abstract base class for implementing an EntityBuilder
 *
 * @author jab
 * @param <U> Type of class to build.
 * @param <V> Type of the builder class (will be recursive)
 */
public abstract class AbstractEntityBuilder<U extends Entity<U>, V extends AbstractEntityBuilder<U, V>> {

    private Id id;
    private String selfLink;
    private String navigationLink;
    private boolean exportObject;

    public final V setId(Id id) {
        this.id = id;
        return getThis();
    }

    public final V setSelfLink(String selfLink) {
        this.selfLink = selfLink;
        return getThis();
    }

    public final V setNavigationLink(String navigationLink) {
        this.navigationLink = navigationLink;
        return getThis();
    }

    public final V setExportObject(boolean exportObject) {
        this.exportObject = exportObject;
        return getThis();
    }

    public abstract U build();

    protected U build(U entity) {
        entity.setId(id);
        entity.setSelfLink(selfLink);
        entity.setNavigationLink(navigationLink);
        entity.setExportObject(exportObject);
        return entity;
    }

    protected abstract V getThis();
}
