package de.fraunhofer.iosb.ilt.frostserver.query;

import de.fraunhofer.iosb.ilt.frostserver.util.StringHelper;

public enum Metadata {
    FULL, MINIMAL, OFF;

    static final Metadata DEFAULT = Metadata.FULL;
    static final String START = "metadata=";

    public static Metadata lookup(String metadata) {
        if (metadata == null) {
            return DEFAULT;
        }
        try {
            return Enum.valueOf(Metadata.class, metadata.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown metadata: " + StringHelper.cleanForLogging(metadata));
        }
    }
}
