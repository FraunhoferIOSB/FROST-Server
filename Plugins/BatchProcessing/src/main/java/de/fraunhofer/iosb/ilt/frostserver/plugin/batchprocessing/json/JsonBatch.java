package de.fraunhofer.iosb.ilt.frostserver.plugin.batchprocessing.json;

import de.fraunhofer.iosb.ilt.frostserver.path.Version;
import de.fraunhofer.iosb.ilt.frostserver.plugin.batchprocessing.batch.Batch;
import de.fraunhofer.iosb.ilt.frostserver.plugin.batchprocessing.batch.Content;
import de.fraunhofer.iosb.ilt.frostserver.plugin.batchprocessing.batch.Part;
import de.fraunhofer.iosb.ilt.frostserver.service.ServiceRequest;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import de.fraunhofer.iosb.ilt.frostserver.util.HttpMethod;
import de.fraunhofer.iosb.ilt.frostserver.util.StringHelper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.util.RawValue;
import static de.fraunhofer.iosb.ilt.frostserver.util.Constants.CONTENT_TYPE_APPLICATION_JSON;
import java.util.Collections;

/**
 * Parser for JSON request.
 */
public class JsonBatch extends Batch<Content> {

    private static final Logger LOGGER = LoggerFactory.getLogger(JsonBatch.class);

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public JsonBatch(Version batchVersion, CoreSettings settings, boolean isChangeSet) {
        super(batchVersion, settings, isChangeSet);
    }

    @Override
    public boolean parse(ServiceRequest serviceRequest) {
        try {
            JsonNode rootNode = OBJECT_MAPPER.readTree(serviceRequest.getContentString());
            JsonNode reqNode = rootNode.path("requests");
            Iterator<JsonNode> requests = reqNode.elements();

            Map<String, Batch<Content>> atomicityGroupToBatch = new HashMap<>();
            while (requests.hasNext()) {
                JsonNode req = requests.next();
                Part<Content> reqPart = new Part<>(this.batchVersion, this.settings, false, "");
                if (req.has("atomicityGroup")) {
                    Batch<Content> group = getAtomicityGroup(atomicityGroupToBatch,
                            req.get("atomicityGroup").textValue());
                    group.addPart(reqPart);
                    reqPart.setInChangeSet(true);
                } else {
                    this.addPart(reqPart);
                }
                JsonRequest request = new JsonRequest(this.batchVersion);
                reqPart.setContent(request);
                for (String property : Arrays.asList("id", "method", "url")) {
                    if (StringHelper.isNullOrEmpty(property)) {
                        parseFailed = true;
                        errors.add("All requests must have a valid " + property);
                    }
                }
                if (!parseFailed) {
                    request.setContentId(req.get("id").textValue());
                    request.setMethod(HttpMethod.fromString(req.get("method").textValue()));
                    request.parseUrl(req.get("url").textValue());
                    if (req.has("body")) {
                        request.addData(OBJECT_MAPPER.writeValueAsString(req.get("body")));
                    }
                }
            }
            return true;
        } catch (IOException exc) {
            LOGGER.error("Failed to read data.", exc);
            return false;
        }
    }

    private Batch<Content> getAtomicityGroup(Map<String, Batch<Content>> atomicityGroupToBatch,
            String atomicityGroupId) {
        Batch<Content> group;
        if (atomicityGroupToBatch.containsKey(atomicityGroupId)) {
            group = atomicityGroupToBatch.get(atomicityGroupId);
        } else {
            group = new JsonBatch(this.batchVersion, this.settings, true);
            atomicityGroupToBatch.put(atomicityGroupId, group);
            Part<Content> atomicityGroupPart = new Part<>(this.batchVersion, this.settings, true, "");
            atomicityGroupPart.setContent(group);
            this.addPart(atomicityGroupPart);
        }
        return group;
    }

    @Override
    public String getContent(boolean allHeaders) {
        List<String> responses = new ArrayList<>();
        addResponses(responses, this);
        ObjectNode rootNode = OBJECT_MAPPER.createObjectNode();
        ArrayNode arrayNode = OBJECT_MAPPER.createArrayNode();
        responses.forEach(s -> arrayNode.addRawValue(new RawValue(s)));
        rootNode.set("responses", arrayNode);
        try {
            return OBJECT_MAPPER.writeValueAsString(rootNode);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to generate JSON.", ex);
        }
    }

    @Override
    public String getContentType() {
        return CONTENT_TYPE_APPLICATION_JSON;
    }

    private void addResponses(List<String> responses, Batch<Content> batch) {
        for (Part<Content> part : batch.getParts()) {
            Content partContent = part.getContent();
            if (partContent instanceof Batch) {
                addResponses(responses, (Batch) partContent);
            } else {
                responses.add(partContent.getContent(false));
            }
        }
    }

    @Override
    public Map<String, String> getHeaders() {
        return Collections.emptyMap();
    }
}
