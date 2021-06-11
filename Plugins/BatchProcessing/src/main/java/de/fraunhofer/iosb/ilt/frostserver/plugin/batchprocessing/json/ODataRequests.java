package de.fraunhofer.iosb.ilt.frostserver.plugin.batchprocessing.json;

import java.util.List;

/** OData Batch request. */
public class ODataRequests {

    public class ODataRequest {
        public final String id;

        public final String atomicityGroup;

        public final String method;

        public final String url;

        public final Object body;

        public ODataRequest(String id, String atomicityGroup, String method, String url, Object body) {
            this.id = id;
            this.atomicityGroup = atomicityGroup;
            this.method = method;
            this.url = url;
            this.body = body;
        }
    }

    public final List<ODataRequest> requests;

    public ODataRequests(List<ODataRequest> requests) {
        super();
        this.requests = requests;
    }

}
