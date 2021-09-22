package de.fraunhofer.iosb.ilt.frostserver.plugin.batchprocessing.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonRawValue;
import de.fraunhofer.iosb.ilt.frostserver.json.serialize.JsonWriter;
import de.fraunhofer.iosb.ilt.frostserver.path.Version;
import de.fraunhofer.iosb.ilt.frostserver.plugin.batchprocessing.batch.Request;
import java.io.IOException;

/**
 *
 */
public class JsonRequest extends Request {

    /**
     * OData Batch response.
     */
    @JsonInclude(Include.NON_NULL)
    public static class ODataResponse {

        public final String id;

        public final int status;

        @JsonRawValue
        public final String body;

        public final String location;

        public ODataResponse(String id, int status, String body, String location) {
            this.id = id;
            this.status = status;
            this.body = body;
            this.location = location;
        }
    }

    private int status;

    public JsonRequest(Version batchVersion) {
        this(batchVersion, true);
    }

    public JsonRequest(Version batchVersion, boolean requireContentId) {
        // content id always mandatory for JSON
        super(batchVersion, true);
    }

    @Override
    public String getContent(boolean allHeaders) {
        try {
            return JsonWriter.writeObject(
                    new ODataResponse(getContentId(), status, data.length() > 0 ? data.toString() : null,
                            getHttpHeaders().get("location")));
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to generate JSON.", ex);
        }
    }

    @Override
    public void setStatus(int status, String text) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "JsonRequest [status=" + status + ", logIndent=" + logIndent + ", method=" + method + ", version="
                + version + ", path=" + path + ", headersOuter=" + headersOuter + ", headersInner=" + headersInner
                + ", parseFailed=" + parseFailed + ", executeFailed=" + executeFailed + ", errors=" + errors
                + ", requireContentId=" + requireContentId + ", contentId=" + contentId + ", contentIdValue="
                + contentIdValue + ", data=" + data + ", batchVersion=" + batchVersion + "]";
    }

}
