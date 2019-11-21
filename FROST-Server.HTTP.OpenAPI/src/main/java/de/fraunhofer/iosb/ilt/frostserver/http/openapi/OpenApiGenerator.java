/*
 * Copyright (C) 2019 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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
package de.fraunhofer.iosb.ilt.frostserver.http.openapi;

import de.fraunhofer.iosb.ilt.frostserver.http.openapi.spec.OAResponse;
import de.fraunhofer.iosb.ilt.frostserver.http.openapi.spec.OAParameter;
import de.fraunhofer.iosb.ilt.frostserver.http.openapi.spec.OAComponents;
import de.fraunhofer.iosb.ilt.frostserver.http.openapi.spec.OADocInfo;
import de.fraunhofer.iosb.ilt.frostserver.http.openapi.spec.OAPath;
import de.fraunhofer.iosb.ilt.frostserver.http.openapi.spec.OAMediaType;
import de.fraunhofer.iosb.ilt.frostserver.http.openapi.spec.OARequestBody;
import de.fraunhofer.iosb.ilt.frostserver.http.openapi.spec.OAHeader;
import de.fraunhofer.iosb.ilt.frostserver.http.openapi.spec.OASchema;
import de.fraunhofer.iosb.ilt.frostserver.http.openapi.spec.OADoc;
import de.fraunhofer.iosb.ilt.frostserver.http.openapi.spec.OAOperation;
import de.fraunhofer.iosb.ilt.frostserver.json.serialize.EntityFormatter;
import de.fraunhofer.iosb.ilt.frostserver.path.EntityProperty;
import de.fraunhofer.iosb.ilt.frostserver.path.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.path.NavigationProperty;
import de.fraunhofer.iosb.ilt.frostserver.path.Property;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import static de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings.TAG_CORE_SETTINGS;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.TreeMap;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Generates a partial OpenApi specification for the SensorThings API.
 *
 * @author scf
 */
@WebServlet(name = "OpenApi", urlPatterns = {"/api"})
public class OpenApiGenerator extends HttpServlet {

    private static final String DESCRIPTION = "OpenAPI specification document generator.";
    private static final String ENCODING = "UTF-8";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        CoreSettings coreSettings = (CoreSettings) request.getServletContext().getAttribute(TAG_CORE_SETTINGS);
        GeneratorContext context = new GeneratorContext(request);
        context.base = "/v1.0";
        OADoc document = new OADoc();
        context.document = document;
        document.info = new OADocInfo("SensorThings v1.1", "1.0.0", "Version 1.1 of the OGC SensorThings API, including Part 2 - Tasking.");
        addComponents(document);

        Map<String, OAPath> paths = new TreeMap<>();
        document.paths = paths;
        OAPath basePath = new OAPath();
        paths.put(context.base, basePath);

        for (EntityType entityType : EntityType.values()) {
            addPathsForSet(document, 0, paths, context.base, entityType, context);
        }

        response.setContentType("application/json");
        response.setCharacterEncoding(ENCODING);

        PrintWriter writer = response.getWriter();
        writer.write(EntityFormatter.writeObject(document));
    }

    private void addComponents(OADoc document) {
        document.components = new OAComponents();
        document.components.addParameter(
                "entityId",
                new OAParameter(
                        "entityId",
                        "The id of the requested entity",
                        new OASchema(OASchema.Type.integer, OASchema.Format.int64)));
        document.components.addParameter(
                "skip",
                new OAParameter(
                        "$skip",
                        OAParameter.In.query,
                        "The number of elements to skip from the collection",
                        new OASchema(OASchema.Type.integer, OASchema.Format.int64)));
        document.components.addParameter(
                "top",
                new OAParameter(
                        "$top",
                        OAParameter.In.query,
                        "The number of elements to return",
                        new OASchema(OASchema.Type.integer, OASchema.Format.int64)));
        document.components.addParameter(
                "count",
                new OAParameter(
                        "$count",
                        OAParameter.In.query,
                        "Flag indicating if the total number of items in the collection should be returned.",
                        new OASchema(OASchema.Type.bool, null)));
        document.components.addParameter(
                "select",
                new OAParameter(
                        "$select",
                        OAParameter.In.query,
                        "The list of properties that need to be returned.",
                        new OASchema(OASchema.Type.string, null)));
        document.components.addParameter(
                "expand",
                new OAParameter(
                        "$expand",
                        OAParameter.In.query,
                        "The list of related queries that need to be included in the result.",
                        new OASchema(OASchema.Type.string, null)));
        document.components.addParameter(
                "filter",
                new OAParameter(
                        "$filter",
                        OAParameter.In.query,
                        "A filter query.",
                        new OASchema(OASchema.Type.string, null)));

        OASchema entityId = new OASchema(OASchema.Type.integer, OASchema.Format.int64);
        entityId.description = "The ID of an entity";
        document.components.schemas.put("entityId", entityId);

        OASchema selfLink = new OASchema(OASchema.Type.string, null);
        selfLink.description = "The direct link to the entity";
        document.components.schemas.put("selfLink", selfLink);

        OASchema count = new OASchema(OASchema.Type.integer, OASchema.Format.int64);
        count.description = "The total number of entities in the entityset";
        document.components.schemas.put("count", count);

        OASchema nextLink = new OASchema(OASchema.Type.string, null);
        nextLink.description = "The link to the next page of entities";
        document.components.schemas.put("nextLink", nextLink);

        OASchema properties = new OASchema(OASchema.Type.object, null);
        properties.description = "a set of additional properties specified for the entity in the form \"name\":\"value\" pairs";
        properties.additionalProperties = true;
        document.components.schemas.put("properties", properties);
    }

    private OAPath createPathForSet(GeneratorContext context, String path, EntityType entityType, int level, boolean withId) {
        String reference = entityType.plural;
        if (withId) {
            reference += "-withId";
        }
        OAPath oaPath;
        if (context.pathTargets.containsKey(reference)) {
            oaPath = context.pathTargets.get(reference);
        } else {
            oaPath = new OAPath();
            if (withId) {
                oaPath.addParameter(new OAParameter("entityId"));
            }
            oaPath.get = new OAOperation();
            oaPath.get.addParameter(new OAParameter("skip"));
            oaPath.get.addParameter(new OAParameter("top"));
            oaPath.get.addParameter(new OAParameter("count"));
            oaPath.get.addParameter(new OAParameter("select"));
            oaPath.get.addParameter(new OAParameter("expand"));
            oaPath.get.addParameter(new OAParameter("filter"));
            oaPath.get.responses.put("200", createEntitySetGet200Response(context, entityType));

            if (context.addEditing) {
                oaPath.post = new OAOperation();
                oaPath.post.requestBody = new OARequestBody();
                oaPath.post.requestBody.description = "Creates a new " + entityType.entityName + " entity";
                oaPath.post.requestBody.required = true;
                OAMediaType jsonType = new OAMediaType(new OASchema("#/components/schemas/" + entityType.entityName));
                oaPath.post.requestBody.addContent("application/json", jsonType);
                oaPath.post.responses.put("201", createEntitySetPost201Response(context, entityType));
            }

            OAPath refPath = new OAPath();
            refPath.ref = "#/paths/" + path.replace("/", "~1");
            context.pathTargets.put(reference, refPath);
        }
        return oaPath;
    }

    private OAPath createPathForSetRef(GeneratorContext options, String path, EntityType entityType, int level, boolean withId) {
        String reference = entityType.plural + "-ref";
        if (withId) {
            reference += "-withId";
        }

        OAPath oaPath;
        if (options.pathTargets.containsKey(reference)) {
            oaPath = options.pathTargets.get(reference);
        } else {
            oaPath = new OAPath();
            if (withId) {
                oaPath.addParameter(new OAParameter("entityId"));
            }
            oaPath.addParameter(new OAParameter("skip"));
            oaPath.addParameter(new OAParameter("top"));
            oaPath.addParameter(new OAParameter("count"));
            oaPath.addParameter(new OAParameter("filter"));
            OAPath refPath = new OAPath();
            refPath.ref = "#/paths/" + path.replace("/", "~1");
            options.pathTargets.put(reference, refPath);
        }
        return oaPath;
    }

    private OAResponse createEntitySetGet200Response(GeneratorContext context, EntityType entityType) {
        OAComponents components = context.document.components;
        String name = entityType.plural + "-get-200";
        if (!context.responseTargets.containsKey(name)) {
            String schemaName = entityType.plural;
            if (!components.schemas.containsKey(schemaName)) {
                createEntitySetSchema(context, entityType);
            }
            OAResponse resp = new OAResponse();
            OAMediaType jsonType = new OAMediaType(new OASchema("#/components/schemas/" + schemaName));
            resp.addContent("application/json", jsonType);
            context.document.components.responses.put(name, resp);

            OAResponse ref = new OAResponse();
            ref.ref = "#/components/responses/" + name;
            context.responseTargets.put(name, ref);
        }
        return context.responseTargets.get(name);

    }

    private OAResponse createEntitySetPost201Response(GeneratorContext context, EntityType entityType) {
        String name = entityType.plural + "-post-201";
        if (!context.responseTargets.containsKey(name)) {
            createLocationHeader(context);
            OAResponse resp = new OAResponse();
            resp.addHeader("Location", new OAHeader("#/components/headers/location"));
            context.document.components.responses.put(name, resp);

            OAResponse ref = new OAResponse();
            ref.ref = "#/components/responses/" + name;
            context.responseTargets.put(name, ref);
        }
        return context.responseTargets.get(name);
    }

    private void createLocationHeader(GeneratorContext context) {
        if (!context.document.components.hasHeader("location")) {
            context.document.components.addHeader(
                    "location",
                    new OAHeader("The selflink of the newly created entity.", new OASchema(OASchema.Type.string, null)));
        }
    }

    private void createEntitySetSchema(GeneratorContext context, EntityType entityType) {
        OAComponents components = context.document.components;
        String schemaName = entityType.plural;
        OASchema schema = new OASchema(OASchema.Type.object, null);
        components.schemas.put(schemaName, schema);

        OASchema nextLink = new OASchema("#/components/schemas/nextLink");
        schema.addProperty("@iot.nextLink", nextLink);

        OASchema count = new OASchema("#/components/schemas/count");
        schema.addProperty("@iot.count", count);

        OASchema value = new OASchema(OASchema.Type.array, null);
        value.items = new OASchema("#/components/schemas/" + entityType.entityName);
        schema.addProperty("value", value);
    }

    private OAPath createPathForEntity(GeneratorContext context, String path, EntityType entityType, int level) {
        String reference = entityType.entityName;
        OAPath oaPath;
        if (context.pathTargets.containsKey(reference)) {
            oaPath = context.pathTargets.get(reference);
        } else {
            oaPath = new OAPath();
            oaPath.addParameter(new OAParameter("entityId"));
            oaPath.get = new OAOperation();
            oaPath.get.addParameter(new OAParameter("select"));
            oaPath.get.addParameter(new OAParameter("expand"));
            oaPath.get.responses.put("200", createEntityGet200Response(context, entityType));

            if (context.addEditing) {
                oaPath.patch = new OAOperation();
                oaPath.patch.requestBody = new OARequestBody();
                oaPath.patch.requestBody.description = "Patches the " + entityType.entityName + " entity";
                oaPath.patch.requestBody.required = true;
                OAMediaType jsonType = new OAMediaType(new OASchema("#/components/schemas/" + entityType.entityName));
                oaPath.patch.requestBody.addContent("application/json", jsonType);
                oaPath.patch.responses.put("201", createEntityPatch200Response(context, entityType));

                oaPath.put = new OAOperation();
                oaPath.put.requestBody = new OARequestBody();
                oaPath.put.requestBody.description = "Replaces the " + entityType.entityName + " entity";
                oaPath.put.requestBody.required = true;
                oaPath.put.requestBody.addContent("application/json", jsonType);
                oaPath.put.responses.put("201", createEntityPatch200Response(context, entityType));

                oaPath.delete = new OAOperation();
                oaPath.delete.responses.put("201", createEntityDelete200Response(context, entityType));
            }

            OAPath refPath = new OAPath();
            refPath.ref = "#/paths/" + path.replace("/", "~1");
            context.pathTargets.put(reference, refPath);
        }
        return oaPath;
    }

    private OAResponse createEntityGet200Response(GeneratorContext context, EntityType entityType) {
        OAComponents components = context.document.components;
        String name = entityType.entityName + "-get-200";
        if (!context.responseTargets.containsKey(name)) {
            String schemaName = entityType.entityName;
            if (!components.schemas.containsKey(schemaName)) {
                createEntitySchema(context, entityType);
            }
            OAResponse resp = new OAResponse();
            OAMediaType jsonType = new OAMediaType(new OASchema("#/components/schemas/" + schemaName));
            resp.addContent("application/json", jsonType);
            context.document.components.responses.put(name, resp);

            OAResponse ref = new OAResponse();
            ref.ref = "#/components/responses/" + name;
            context.responseTargets.put(name, ref);
        }
        return context.responseTargets.get(name);

    }

    private OAResponse createEntityPatch200Response(GeneratorContext context, EntityType entityType) {
        String name = entityType.plural + "-patch-200";
        if (!context.responseTargets.containsKey(name)) {
            OAResponse resp = new OAResponse();
            context.document.components.responses.put(name, resp);

            OAResponse ref = new OAResponse();
            ref.ref = "#/components/responses/" + name;
            context.responseTargets.put(name, ref);
        }
        return context.responseTargets.get(name);
    }

    private OAResponse createEntityDelete200Response(GeneratorContext context, EntityType entityType) {
        String name = entityType.plural + "-delete-200";
        if (!context.responseTargets.containsKey(name)) {
            OAResponse resp = new OAResponse();
            context.document.components.responses.put(name, resp);

            OAResponse ref = new OAResponse();
            ref.ref = "#/components/responses/" + name;
            context.responseTargets.put(name, ref);
        }
        return context.responseTargets.get(name);
    }

    private void createEntitySchema(GeneratorContext context, EntityType entityType) {
        OAComponents components = context.document.components;
        String schemaName = entityType.entityName;
        OASchema schema = new OASchema(OASchema.Type.object, null);
        components.schemas.put(schemaName, schema);

        for (Property property : entityType.getPropertySet()) {
            if (property instanceof EntityProperty) {
                OASchema propSchema;
                switch ((EntityProperty) property) {
                    case ID:
                        propSchema = new OASchema("#/components/schemas/entityId");
                        break;

                    case PROPERTIES:
                        propSchema = new OASchema("#/components/schemas/properties");
                        break;

                    case SELFLINK:
                        propSchema = new OASchema("#/components/schemas/selfLink");
                        break;

                    default:
                        propSchema = new OASchema(property);
                        break;

                }
                schema.addProperty(property.getJsonName(), propSchema);
            } else {
                // TODO: Navigation properties for expand.
            }
        }
    }

    private void addPathsForSet(OADoc document, int level, Map<String, OAPath> paths, String base, EntityType entityType, GeneratorContext options) {
        String path = base + "/" + entityType.plural;
        OAPath pathCollection = createPathForSet(options, path, entityType, level, level > 0);
        paths.put(path, pathCollection);

        if (options.addRef) {
            String refPath = base + "/" + entityType.plural + "/$ref";
            OAPath pathRef = createPathForSetRef(options, refPath, entityType, level, level > 0);
            paths.put(refPath, pathRef);
        }

        if (level < options.recurse) {
            String baseId = base + "/" + entityType.plural + "({entityId})";
            OAPath pathBaseId = createPathForEntity(options, baseId, entityType, level);
            paths.put(baseId, pathBaseId);
            addPathsForEntity(document, level, paths, baseId, entityType, options);
        }
    }

    private void addPathsForEntity(OADoc document, int level, Map<String, OAPath> paths, String base, EntityType entityType, GeneratorContext options) {
        if (options.addEntityProperties) {
            for (Property entityProperty : entityType.getPropertySet()) {
                if (!(entityProperty instanceof NavigationProperty)) {
                    OAPath pathProperty = new OAPath();
                    pathProperty.addParameter(new OAParameter("entityId"));

                    paths.put(base + "/" + entityProperty.getName(), pathProperty);
                    if (options.addValue) {
                        paths.put(base + "/" + entityProperty.getName() + "/$value", pathProperty);
                    }
                }
            }
        }
        for (NavigationProperty navProp : entityType.getNavigationSets()) {
            if (level < options.recurse) {
                addPathsForSet(document, level + 1, paths, base, navProp.type, options);
            }
        }
        for (NavigationProperty navProp : entityType.getNavigationEntities()) {
            if (level < options.recurse) {
                String baseName = base + "/" + navProp.type.entityName;
                OAPath paPath = createPathForEntity(options, baseName, navProp.type, level);
                paths.put(baseName, paPath);
                addPathsForEntity(document, level, paths, baseName, navProp.type, options);
            }
        }
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return DESCRIPTION;
    }

}
