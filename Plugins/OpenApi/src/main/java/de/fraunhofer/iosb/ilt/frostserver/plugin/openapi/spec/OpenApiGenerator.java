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
package de.fraunhofer.iosb.ilt.frostserver.plugin.openapi.spec;

import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityProperty;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationProperty;
import de.fraunhofer.iosb.ilt.frostserver.property.Property;
import java.util.Map;
import java.util.TreeMap;

/**
 * Generates a partial OpenApi specification for the SensorThings API.
 *
 * @author scf
 */
public class OpenApiGenerator {

    private OpenApiGenerator() {
        // Only has static methods.
    }

    public static OADoc generateOpenApiDocument(GeneratorContext context) {
        OADoc document = new OADoc();
        context.setDocument(document);
        document.info = new OADocInfo(
                "SensorThings " + context.getVersion().urlPart,
                "1.0.0",
                "Version " + context.getVersion().urlPart + " of the OGC SensorThings API, including Part 2 - Tasking.");
        addComponents(document);

        Map<String, OAPath> paths = new TreeMap<>();
        document.paths = paths;
        OAPath basePath = new OAPath();
        paths.put(context.getBase(), basePath);

        for (EntityType entityType : EntityType.values()) {
            addPathsForSet(document, 0, paths, context.getBase(), entityType, context);
        }
        return document;
    }

    private static void addComponents(OADoc document) {
        document.components = new OAComponents();
        document.components.addParameter(
                "entityId",
                new OAParameter(
                        "entityId",
                        "The id of the requested entity",
                        new OASchema(OASchema.Type.INTEGER, OASchema.Format.INT64)));
        document.components.addParameter(
                "skip",
                new OAParameter(
                        "$skip",
                        OAParameter.In.QUERY,
                        "The number of elements to skip from the collection",
                        new OASchema(OASchema.Type.INTEGER, OASchema.Format.INT64)));
        document.components.addParameter(
                "top",
                new OAParameter(
                        "$top",
                        OAParameter.In.QUERY,
                        "The number of elements to return",
                        new OASchema(OASchema.Type.INTEGER, OASchema.Format.INT64)));
        document.components.addParameter(
                "count",
                new OAParameter(
                        "$count",
                        OAParameter.In.QUERY,
                        "Flag indicating if the total number of items in the collection should be returned.",
                        new OASchema(OASchema.Type.BOOLEAN, null)));
        document.components.addParameter(
                "select",
                new OAParameter(
                        "$select",
                        OAParameter.In.QUERY,
                        "The list of properties that need to be returned.",
                        new OASchema(OASchema.Type.STRING, null)));
        document.components.addParameter(
                "expand",
                new OAParameter(
                        "$expand",
                        OAParameter.In.QUERY,
                        "The list of related queries that need to be included in the result.",
                        new OASchema(OASchema.Type.STRING, null)));
        document.components.addParameter(
                "filter",
                new OAParameter(
                        "$filter",
                        OAParameter.In.QUERY,
                        "A filter query.",
                        new OASchema(OASchema.Type.STRING, null)));

        OASchema entityId = new OASchema(OASchema.Type.INTEGER, OASchema.Format.INT64);
        entityId.description = "The ID of an entity";
        document.components.addSchema("entityId", entityId);

        OASchema selfLink = new OASchema(OASchema.Type.STRING, null);
        selfLink.description = "The direct link to the entity";
        document.components.addSchema("selfLink", selfLink);

        OASchema navLink = new OASchema(OASchema.Type.STRING, null);
        navLink.description = "A link to a related entity or entity set";
        document.components.addSchema("navigationLink", navLink);

        OASchema count = new OASchema(OASchema.Type.INTEGER, OASchema.Format.INT64);
        count.description = "The total number of entities in the entityset";
        document.components.addSchema("count", count);

        OASchema nextLink = new OASchema(OASchema.Type.STRING, null);
        nextLink.description = "The link to the next page of entities";
        document.components.addSchema("nextLink", nextLink);

        OASchema properties = new OASchema(OASchema.Type.OBJECT, null);
        properties.description = "a set of additional properties specified for the entity in the form \"name\":\"value\" pairs";
        properties.additionalProperties = true;
        document.components.addSchema("properties", properties);
    }

    private static OAPath createPathForSet(GeneratorContext context, String path, EntityType entityType, boolean withId) {
        String reference = entityType.plural;
        if (withId) {
            reference += "-withId";
        }
        OAPath oaPath;
        if (context.getPathTargets().containsKey(reference)) {
            oaPath = context.getPathTargets().get(reference);
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

            if (context.isAddEditing()) {
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
            context.getPathTargets().put(reference, refPath);
        }
        return oaPath;
    }

    private static OAPath createPathForSetRef(GeneratorContext context, String path, EntityType entityType, boolean withId) {
        String reference = entityType.plural + "-ref";
        if (withId) {
            reference += "-withId";
        }

        OAPath oaPath;
        if (context.getPathTargets().containsKey(reference)) {
            oaPath = context.getPathTargets().get(reference);
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
            context.getPathTargets().put(reference, refPath);
        }
        return oaPath;
    }

    private static OAResponse createEntitySetGet200Response(GeneratorContext context, EntityType entityType) {
        OAComponents components = context.getDocument().components;
        String name = entityType.plural + "-get-200";
        if (!context.getResponseTargets().containsKey(name)) {
            String schemaName = entityType.plural;
            if (!components.hasSchema(schemaName)) {
                createEntitySetSchema(context, entityType);
            }
            OAResponse resp = new OAResponse();
            OAMediaType jsonType = new OAMediaType(new OASchema("#/components/schemas/" + schemaName));
            resp.addContent("application/json", jsonType);
            context.getDocument().components.addResponse(name, resp);

            OAResponse ref = new OAResponse();
            ref.ref = "#/components/responses/" + name;
            context.getResponseTargets().put(name, ref);
        }
        return context.getResponseTargets().get(name);

    }

    private static OAResponse createEntitySetPost201Response(GeneratorContext context, EntityType entityType) {
        String name = entityType.plural + "-post-201";
        if (!context.getResponseTargets().containsKey(name)) {
            createLocationHeader(context);
            OAResponse resp = new OAResponse();
            resp.addHeader("Location", new OAHeader("#/components/headers/location"));
            context.getDocument().components.addResponse(name, resp);

            OAResponse ref = new OAResponse();
            ref.ref = "#/components/responses/" + name;
            context.getResponseTargets().put(name, ref);
        }
        return context.getResponseTargets().get(name);
    }

    private static void createLocationHeader(GeneratorContext context) {
        if (!context.getDocument().components.hasHeader("location")) {
            context.getDocument().components.addHeader(
                    "location",
                    new OAHeader("The selflink of the newly created entity.", new OASchema(OASchema.Type.STRING, null)));
        }
    }

    private static void createEntitySetSchema(GeneratorContext context, EntityType entityType) {
        OAComponents components = context.getDocument().components;
        String schemaName = entityType.plural;
        OASchema schema = new OASchema(OASchema.Type.OBJECT, null);
        components.addSchema(schemaName, schema);

        OASchema nextLink = new OASchema("#/components/schemas/nextLink");
        schema.addProperty("@iot.nextLink", nextLink);

        OASchema count = new OASchema("#/components/schemas/count");
        schema.addProperty("@iot.count", count);

        OASchema value = new OASchema(OASchema.Type.ARRAY, null);
        value.items = new OASchema("#/components/schemas/" + entityType.entityName);
        schema.addProperty("value", value);
    }

    private static OAPath createPathForEntity(GeneratorContext context, String path, EntityType entityType) {
        String reference = entityType.entityName;
        OAPath oaPath;
        if (context.getPathTargets().containsKey(reference)) {
            oaPath = context.getPathTargets().get(reference);
        } else {
            oaPath = new OAPath();
            oaPath.addParameter(new OAParameter("entityId"));
            oaPath.get = new OAOperation();
            oaPath.get.addParameter(new OAParameter("select"));
            oaPath.get.addParameter(new OAParameter("expand"));
            oaPath.get.responses.put("200", createEntityGet200Response(context, entityType));

            if (context.isAddEditing()) {
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
            context.getPathTargets().put(reference, refPath);
        }
        return oaPath;
    }

    private static OAResponse createEntityGet200Response(GeneratorContext context, EntityType entityType) {
        OAComponents components = context.getDocument().components;
        String name = entityType.entityName + "-get-200";
        if (!context.getResponseTargets().containsKey(name)) {
            String schemaName = entityType.entityName;
            if (!components.hasSchema(schemaName)) {
                createEntitySchema(context, entityType);
            }
            OAResponse resp = new OAResponse();
            OAMediaType jsonType = new OAMediaType(new OASchema("#/components/schemas/" + schemaName));
            resp.addContent("application/json", jsonType);
            context.getDocument().components.addResponse(name, resp);

            OAResponse ref = new OAResponse();
            ref.ref = "#/components/responses/" + name;
            context.getResponseTargets().put(name, ref);
        }
        return context.getResponseTargets().get(name);

    }

    private static OAResponse createEntityPatch200Response(GeneratorContext context, EntityType entityType) {
        String name = entityType.plural + "-patch-200";
        if (!context.getResponseTargets().containsKey(name)) {
            OAResponse resp = new OAResponse();
            context.getDocument().components.addResponse(name, resp);

            OAResponse ref = new OAResponse();
            ref.ref = "#/components/responses/" + name;
            context.getResponseTargets().put(name, ref);
        }
        return context.getResponseTargets().get(name);
    }

    private static OAResponse createEntityDelete200Response(GeneratorContext context, EntityType entityType) {
        String name = entityType.plural + "-delete-200";
        if (!context.getResponseTargets().containsKey(name)) {
            OAResponse resp = new OAResponse();
            context.getDocument().components.addResponse(name, resp);

            OAResponse ref = new OAResponse();
            ref.ref = "#/components/responses/" + name;
            context.getResponseTargets().put(name, ref);
        }
        return context.getResponseTargets().get(name);
    }

    private static void createEntitySchema(GeneratorContext context, EntityType entityType) {
        OAComponents components = context.getDocument().components;
        String schemaName = entityType.entityName;
        OASchema schema = new OASchema(OASchema.Type.OBJECT, null);
        components.addSchema(schemaName, schema);

        for (Property property : entityType.getPropertySet()) {
            OASchema propSchema = null;
            if (property instanceof EntityProperty) {
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
                if (property instanceof NavigationProperty) {
                    NavigationProperty navigationProperty = (NavigationProperty) property;
                    if (navigationProperty.isSet) {
                        propSchema = new OASchema(OASchema.Type.ARRAY, null);
                        propSchema.items = new OASchema("#/components/schemas/" + navigationProperty.getType().entityName);
                        schema.addProperty(property.getJsonName(), propSchema);

                        OASchema count = new OASchema("#/components/schemas/count");
                        schema.addProperty(navigationProperty.getType().plural + "@iot.count", count);

                        OASchema navLink = new OASchema("#/components/schemas/navigationLink");
                        schema.addProperty(navigationProperty.getType().plural + "@iot.navigationLink", navLink);

                        OASchema nextLink = new OASchema("#/components/schemas/nextLink");
                        schema.addProperty(navigationProperty.getType().plural + "@iot.nextLink", nextLink);
                    } else {
                        propSchema = new OASchema("#/components/schemas/" + navigationProperty.getType().entityName);
                        schema.addProperty(property.getJsonName(), propSchema);

                        OASchema navLink = new OASchema("#/components/schemas/navigationLink");
                        schema.addProperty(navigationProperty.getType().entityName + "@iot.navigationLink", navLink);
                    }
                }
            }
        }
    }

    private static void addPathsForSet(OADoc document, int level, Map<String, OAPath> paths, String base, EntityType entityType, GeneratorContext options) {
        String path = base + "/" + entityType.plural;
        OAPath pathCollection = createPathForSet(options, path, entityType, level > 0);
        paths.put(path, pathCollection);

        if (options.isAddRef()) {
            String refPath = base + "/" + entityType.plural + "/$ref";
            OAPath pathRef = createPathForSetRef(options, refPath, entityType, level > 0);
            paths.put(refPath, pathRef);
        }

        if (level < options.getRecurse()) {
            String baseId = base + "/" + entityType.plural + "({entityId})";
            OAPath pathBaseId = createPathForEntity(options, baseId, entityType);
            paths.put(baseId, pathBaseId);
            addPathsForEntity(document, level, paths, baseId, entityType, options);
        }
    }

    private static void addPathsForEntity(OADoc document, int level, Map<String, OAPath> paths, String base, EntityType entityType, GeneratorContext options) {
        if (options.isAddEntityProperties()) {
            for (Property entityProperty : entityType.getPropertySet()) {
                if (!(entityProperty instanceof NavigationProperty)) {
                    OAPath pathProperty = new OAPath();
                    pathProperty.addParameter(new OAParameter("entityId"));

                    paths.put(base + "/" + entityProperty.getName(), pathProperty);
                    if (options.isAddValue()) {
                        paths.put(base + "/" + entityProperty.getName() + "/$value", pathProperty);
                    }
                }
            }
        }
        for (NavigationProperty navProp : entityType.getNavigationSets()) {
            if (level < options.getRecurse()) {
                addPathsForSet(document, level + 1, paths, base, navProp.type, options);
            }
        }
        for (NavigationProperty navProp : entityType.getNavigationEntities()) {
            if (level < options.getRecurse()) {
                String baseName = base + "/" + navProp.type.entityName;
                OAPath paPath = createPathForEntity(options, baseName, navProp.type);
                paths.put(baseName, paPath);
                addPathsForEntity(document, level, paths, baseName, navProp.type, options);
            }
        }
    }

}
