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
