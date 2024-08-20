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
package de.fraunhofer.iosb.ilt.frostserver.plugin.batchprocessing.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonRawValue;
import de.fraunhofer.iosb.ilt.frostserver.json.serialize.JsonWriter;
import de.fraunhofer.iosb.ilt.frostserver.path.Version;
import de.fraunhofer.iosb.ilt.frostserver.plugin.batchprocessing.batch.Request;
import de.fraunhofer.iosb.ilt.frostserver.util.StringHelper;
import java.io.IOException;
import java.util.List;

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
            final List<String> locations = getInnerHeaders().get("Location");
            String location = StringHelper.isNullOrEmpty(locations) ? null : locations.get(0);
            return JsonWriter.writeObject(new ODataResponse(
                    getContentId(),
                    status,
                    data.length() > 0 ? data.toString() : null,
                    location));
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
