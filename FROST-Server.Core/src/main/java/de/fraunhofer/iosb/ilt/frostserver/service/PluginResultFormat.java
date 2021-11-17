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
package de.fraunhofer.iosb.ilt.frostserver.service;

import de.fraunhofer.iosb.ilt.frostserver.formatter.ResultFormatter;
import de.fraunhofer.iosb.ilt.frostserver.path.Version;
import java.util.Collection;

/**
 * The interface that result format extensions must implement.
 *
 * @author scf
 */
public interface PluginResultFormat extends Plugin {

    /**
     * The "name" of the default resultFormatter.
     */
    public static final String DEFAULT_FORMAT_NAME = "default";

    /**
     * Lists the Versions for which this plugin is relevant.
     *
     * @return The Versions for which this plugin is relevant.
     */
    public Collection<Version> getVersions();

    /**
     * Get the names of the formats this formatter plugin supports.
     *
     * @return The names of the formats this formatter plugin supports.
     */
    public Collection<String> getFormatNames();

    /**
     * Get an actual result formatter.
     *
     * @return An actual result formatter.
     */
    public ResultFormatter getResultFormatter();
}
