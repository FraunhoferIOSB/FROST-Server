/*
 * Copyright (C) 2023 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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
package de.fraunhofer.iosb.ilt.frostserver.plugin.format.geojson.tools;

import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.path.Version;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationProperty;
import de.fraunhofer.iosb.ilt.frostserver.query.Query;

/**
 * A class for gathering data from Entities link to by a NavigationLink of an
 * Entity into a CSV file.
 *
 * @author scf
 */
public class GjEntityExpand implements GjEntityEntry {

    private final NavigationProperty property;
    private final GjElementSet expandedElements;

    public GjEntityExpand(String serviceRootUrl, Version version, String name, NavigationProperty property, Query subQuery) {
        this.property = property;
        expandedElements = new GjElementSet(subQuery, serviceRootUrl, version, name, false);
        expandedElements.initFrom(property.getEntityType());
    }

    @Override
    public void writeData(GjRowCollector collector, Entity e, String namePrefix) {
        expandedElements.writeData(collector, property.getFrom(e), namePrefix);
    }

}
