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

import de.fraunhofer.iosb.ilt.sta.model.id.Id;

/**
 * Abstract base class for implementing an EntityBuilder
 *
 * @author jab
 * @param <U> Type of class to build.
 * @param <V> Type of the builder class (will be recursive)
 */
public abstract class AbstractEntityBuilder<U, V extends AbstractEntityBuilder<U, V>> {

    protected Id id;
    protected String selfLink;
    protected String navigationLink;
    private boolean exportObject;

    public V setId(Id id) {
        this.id = id;
        return getThis();
    }

    public V setSelfLink(String selfLink) {
        this.selfLink = selfLink;
        return getThis();
    }

    public V setNavigationLink(String navigationLink) {
        this.navigationLink = navigationLink;
        return getThis();
    }

    public abstract U build();

    protected abstract V getThis();

    public boolean isExportObject() {
        return exportObject;
    }

    public V setExportObject(boolean exportObject) {
        this.exportObject = exportObject;
        return getThis();
    }
}
