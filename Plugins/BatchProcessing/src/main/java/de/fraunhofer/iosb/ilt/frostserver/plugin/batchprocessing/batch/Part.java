package de.fraunhofer.iosb.ilt.frostserver.plugin.batchprocessing.batch;

import de.fraunhofer.iosb.ilt.frostserver.path.Version;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import java.util.HashMap;
import java.util.Map;

public class Part<C extends Content> {

    protected final CoreSettings settings;

    protected final Map<String, String> headers = new HashMap<>();

    protected C content;

    protected String logIndent = "";

    protected boolean inChangeSet;

    protected final Version batchVersion;

    /**
     * Creates a new Part.
     *
     * @param batchVersion Batch request API version
     * @param settings The settings.
     * @param inChangeSet flag indicating the Part is part of a ChangeSet, and
     * thus if the part itself can be a ChangeSet.
     * @param logIndent
     */
    public Part(Version batchVersion, CoreSettings settings, boolean inChangeSet, String logIndent) {
        this.batchVersion = batchVersion;
        this.settings = settings;
        this.inChangeSet = inChangeSet;
        this.logIndent = logIndent;
    }

    /**
     * Get the value of the header with the given name.
     *
     * @param name The name of the header to get.
     * @return The value of the header with the given name.
     */
    public String getHeader(String name) {
        return headers.get(name);
    }

    /**
     * Get the Content of this Part.
     *
     * @return the Content of this Part.
     */
    public C getContent() {
        return content;
    }

    /**
     * Set the Content of this Part.
     *
     * @param content the Content of this Part.
     */
    public void setContent(C content) {
        this.content = content;
    }

    public void setInChangeSet(boolean inChangeSet) {
        this.inChangeSet = inChangeSet;
    }

    /**
     * Sets the indentation of log messages. Since Content can be nested, this
     * makes debug output better readable.
     *
     * @param logIndent the indentation of log messages.
     */
    public void setLogIndent(String logIndent) {
        this.logIndent = logIndent;
    }

    @Override
    public String toString() {
        return "Part [settings=" + settings + ", headers=" + headers + ", content=" + content + ", logIndent="
                + logIndent + ", inChangeSet=" + inChangeSet + ", batchVersion=" + batchVersion + "]";
    }
}
