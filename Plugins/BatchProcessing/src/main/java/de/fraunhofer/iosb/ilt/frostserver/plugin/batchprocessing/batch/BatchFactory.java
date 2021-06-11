package de.fraunhofer.iosb.ilt.frostserver.plugin.batchprocessing.batch;

import de.fraunhofer.iosb.ilt.frostserver.path.Version;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;

public interface BatchFactory<C extends Content> {

    /** Return lower-case content-type. */
    String getContentType();

    Batch<C> createBatch(Version version, CoreSettings settings, boolean isChangeSet);

    Part<C> createPart(Version batchVersion, CoreSettings settings, boolean inChangeSet, String logIndent);

    Request createRequest(Version version, boolean inChangeSet);

}