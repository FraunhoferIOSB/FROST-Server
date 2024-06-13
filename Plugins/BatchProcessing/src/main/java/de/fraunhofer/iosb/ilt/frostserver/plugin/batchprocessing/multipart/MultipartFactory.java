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
package de.fraunhofer.iosb.ilt.frostserver.plugin.batchprocessing.multipart;

import de.fraunhofer.iosb.ilt.frostserver.path.Version;
import de.fraunhofer.iosb.ilt.frostserver.plugin.batchprocessing.batch.Batch;
import de.fraunhofer.iosb.ilt.frostserver.plugin.batchprocessing.batch.BatchFactory;
import de.fraunhofer.iosb.ilt.frostserver.plugin.batchprocessing.batch.Part;
import de.fraunhofer.iosb.ilt.frostserver.plugin.batchprocessing.batch.Request;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;

public class MultipartFactory implements BatchFactory<MultipartContent> {

    @Override
    public String getContentType() {
        return "multipart/mixed";
    }

    @Override
    public Batch<MultipartContent> createBatch(Version version, CoreSettings settings, boolean isChangeSet) {
        return new MixedContent(version, settings, isChangeSet);
    }

    @Override
    public Request createRequest(Version version, boolean requireContentId) {
        return new HttpContent(version, requireContentId);
    }

    @Override
    public Part<MultipartContent> createPart(Version batchVersion, CoreSettings settings, boolean inChangeSet,
            String logIndent) {
        return new MixedPart(batchVersion, settings, inChangeSet, logIndent);
    }

}
