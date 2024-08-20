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
package de.fraunhofer.iosb.ilt.frostserver.plugin.batchprocessing.batch;

import de.fraunhofer.iosb.ilt.frostserver.path.Version;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;

public interface BatchFactory<C extends Content> {

    /**
     * Return lower-case content-type.
     *
     * @return the content type that this factory can handle.
     */
    String getContentType();

    Batch<C> createBatch(Version version, CoreSettings settings, boolean isChangeSet);

    Part<C> createPart(Version batchVersion, CoreSettings settings, boolean inChangeSet, String logIndent);

    Request createRequest(Version version, boolean inChangeSet);

}
