/*
 * Copyright (C) 2020 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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
import de.fraunhofer.iosb.ilt.frostserver.path.UrlHelper;
import de.fraunhofer.iosb.ilt.frostserver.path.Version;

/**
 *
 * @author scf
 */
public class GjSelfLinkProperty implements GjEntityEntry {

    private final String name;
    private final String serviceRootUrl;
    private final Version version;

    public GjSelfLinkProperty(String serviceRootUrl, Version version, String name) {
        this.name = name;
        this.serviceRootUrl = serviceRootUrl;
        this.version = version;
    }

    @Override
    public void writeData(GjRowCollector collector, Entity<?> source, String namePrefix) {
        collector.collectEntry(namePrefix + name, UrlHelper.generateSelfLink(serviceRootUrl, version, source));
    }

}
