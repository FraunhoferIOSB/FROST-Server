package de.fraunhofer.iosb.ilt.frostserver.plugin.batchprocessing.json;

import de.fraunhofer.iosb.ilt.frostserver.path.Version;
import de.fraunhofer.iosb.ilt.frostserver.plugin.batchprocessing.batch.Batch;
import de.fraunhofer.iosb.ilt.frostserver.plugin.batchprocessing.batch.BatchFactory;
import de.fraunhofer.iosb.ilt.frostserver.plugin.batchprocessing.batch.Content;
import de.fraunhofer.iosb.ilt.frostserver.plugin.batchprocessing.batch.Part;
import de.fraunhofer.iosb.ilt.frostserver.plugin.batchprocessing.batch.Request;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;

public class JsonBatchFactory implements BatchFactory<Content> {

    @Override
    public String getContentType() {
        return "application/json";
    }

    @Override
    public Batch<Content> createBatch(Version version, CoreSettings settings, boolean isChangeSet) {
        return new JsonBatch(version, settings, isChangeSet);
    }

    @Override
    public Request createRequest(Version version, boolean requireContentId) {
        return new JsonRequest(version, requireContentId);
    }

    @Override
    public Part<Content> createPart(Version batchVersion, CoreSettings settings, boolean inChangeSet,
            String logIndent) {
        return new Part<>(batchVersion, settings, inChangeSet, logIndent);
    }

}
