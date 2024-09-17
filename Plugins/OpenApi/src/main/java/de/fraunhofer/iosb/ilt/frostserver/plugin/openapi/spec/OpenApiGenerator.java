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
package de.fraunhofer.iosb.ilt.frostserver.plugin.openapi.spec;

import static de.fraunhofer.iosb.ilt.frostserver.util.Constants.CONTENT_TYPE_APPLICATION_JSON;

import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.ModelRegistry;
import de.fraunhofer.iosb.ilt.frostserver.path.Version;
import de.fraunhofer.iosb.ilt.frostserver.plugin.odata.PluginOData;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationProperty;
import de.fraunhofer.iosb.ilt.frostserver.property.Property;
import de.fraunhofer.iosb.ilt.frostserver.util.user.PrincipalExtended;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

/**
 * Generates a partial OpenApi specification for the SensorThings API.
 *
 * @author scf
 */
public class OpenApiGenerator {

    private static final String PATH_COMPONENTS_SCHEMAS = "#/components/schemas/";
    private static final String PATH_COMPONENTS_RESPONSES = "#/components/responses/";
    private static final String PATH_PATHS = "#/paths/";
    private static final String PARAM_FILTER = "filter";
    private static final String PARAM_EXPAND = "expand";
    private static final String PARAM_SELECT = "select";
    private static final String PARAM_COUNT = "count";
    private static final String PARAM_TOP = "top";
    private static final String PARAM_SKIP = "skip";
    private static final String PARAM_ENTITY_ID = "entityId";
    private static final String[] REPLACE_CHARS = new String[]{"~1", "%28", "%29", "%7B", "%7D"};
    private static final String[] FIND_CHARS = new String[]{"/", "(", ")", "{", "}"};

    private OpenApiGenerator() {
        // Only has static methods.
    }

    public static OADoc generateOpenApiDocument(GeneratorContext context) {
        ModelRegistry modelRegistry = context.getSettings().getModelRegistry();
        OADoc document = new OADoc();
        context.setDocument(document);
        document.setInfo(new OADocInfo(
                "SensorThings " + context.getVersion().urlPart,
                "1.0.0",
                "Version " + context.getVersion().urlPart + " of the OGC SensorThings API, including Part 2 - Tasking."));
        addComponents(context, document);

        document.addServer(new OAServer(context.getServiceRootUrl(), "FROST-Server"));

        Map<String, OAPath> paths = document.getPaths();
        OAPath basePath = new OAPath();
        paths.put(context.getBase(), basePath);

        for (EntityType entityType : modelRegistry.getEntityTypes(PrincipalExtended.getLocalPrincipal().isAdmin())) {
            addPathsForSet(context, document, 0, paths, context.getBase(), entityType, entityType.plural, context);
        }
        return document;
    }

    private static void addComponents(GeneratorContext context, OADoc document) {
        document.getComponents().addParameter(
                PARAM_ENTITY_ID,
                new OAParameter(
                        PARAM_ENTITY_ID,
                        "The id of the requested entity",
                        new OASchema(OASchema.Type.INTEGER, OASchema.Format.INT64)));
        document.getComponents().addParameter(
                PARAM_SKIP,
                new OAParameter(
                        "$skip",
                        OAParameter.In.QUERY,
                        "The number of elements to skip from the collection",
                        new OASchema(OASchema.Type.INTEGER, OASchema.Format.INT64)));
        document.getComponents().addParameter(
                PARAM_TOP,
                new OAParameter(
                        "$top",
                        OAParameter.In.QUERY,
                        "The number of elements to return",
                        new OASchema(OASchema.Type.INTEGER, OASchema.Format.INT64)));
        document.getComponents().addParameter(
                PARAM_COUNT,
                new OAParameter(
                        "$count",
                        OAParameter.In.QUERY,
                        "Flag indicating if the total number of items in the collection should be returned.",
                        new OASchema(OASchema.Type.BOOLEAN, null)));
        document.getComponents().addParameter(
                PARAM_SELECT,
                new OAParameter(
                        "$select",
                        OAParameter.In.QUERY,
                        "The list of properties that need to be returned.",
                        new OASchema(OASchema.Type.STRING, null)));
        document.getComponents().addParameter(
                PARAM_EXPAND,
                new OAParameter(
                        "$expand",
                        OAParameter.In.QUERY,
                        "The list of related queries that need to be included in the result.",
                        new OASchema(OASchema.Type.STRING, null)));
        document.getComponents().addParameter(
                PARAM_FILTER,
                new OAParameter(
                        "$filter",
                        OAParameter.In.QUERY,
                        "A filter query.",
                        new OASchema(OASchema.Type.STRING, null)));

        OASchema entityId = new OASchema(OASchema.Type.INTEGER, OASchema.Format.INT64);
        if ("ServerGeneratedOnly".equalsIgnoreCase(context.getSettings().getPersistenceSettings().getIdGenerationMode())) {
            entityId.setReadOnly(true);
        }
        entityId.setDescription("The ID of an entity");
        document.getComponents().addSchema(PARAM_ENTITY_ID, entityId);

        OASchema selfLink = new OASchema(OASchema.Type.STRING, null);
        selfLink.setReadOnly(true);
        selfLink.setDescription("The direct link to the entity");
        document.getComponents().addSchema("selfLink", selfLink);

        OASchema navLink = new OASchema(OASchema.Type.STRING, null);
        navLink.setReadOnly(true);
        navLink.setDescription("A link to a related entity or entity set");
        document.getComponents().addSchema("navigationLink", navLink);

        OASchema count = new OASchema(OASchema.Type.INTEGER, OASchema.Format.INT64);
        count.setReadOnly(true);
        count.setDescription("The total number of entities in the entityset");
        document.getComponents().addSchema(PARAM_COUNT, count);

        OASchema nextLink = new OASchema(OASchema.Type.STRING, null);
        nextLink.setReadOnly(true);
        nextLink.setDescription("The link to the next page of entities");
        document.getComponents().addSchema("nextLink", nextLink);

        Version version = context.getVersion();
        if (version == PluginOData.VERSION_ODATA_40 || version == PluginOData.VERSION_ODATA_401) {
            OASchema odataContext = new OASchema(OASchema.Type.STRING, null);
            odataContext.setReadOnly(true);
            odataContext.setDescription("The link to the OData context");
            document.getComponents().addSchema("context", odataContext);
        }

        OASchema properties = new OASchema(OASchema.Type.OBJECT, null);
        properties.setDescription("a set of additional properties specified for the entity in the form \"name\":\"value\" pairs");
        properties.setAdditionalProperties(true);
        document.getComponents().addSchema("properties", properties);
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
                oaPath.addParameter(new OAParameter(PARAM_ENTITY_ID));
            }
            oaPath.setGet(new OAOperation());
            oaPath.getGet().addParameter(new OAParameter(PARAM_SKIP));
            oaPath.getGet().addParameter(new OAParameter(PARAM_TOP));
            oaPath.getGet().addParameter(new OAParameter(PARAM_COUNT));
            oaPath.getGet().addParameter(new OAParameter(PARAM_SELECT));
            oaPath.getGet().addParameter(new OAParameter(PARAM_EXPAND));
            oaPath.getGet().addParameter(new OAParameter(PARAM_FILTER));
            oaPath.getGet().getResponses().put("200", createEntitySetGet200Response(context, entityType));

            if (context.isAddEditing()) {
                oaPath.setPost(new OAOperation());
                oaPath.getPost().setRequestBody(new OARequestBody()
                        .setDescription("Creates a new entity of type " + entityType.entityName)
                        .setRequired(true)
                        .addContent(CONTENT_TYPE_APPLICATION_JSON, new OAMediaType(new OASchema(PATH_COMPONENTS_SCHEMAS + entityType.entityName))));
                oaPath.getPost().getResponses().put("201", createEntitySetPost201Response(context, entityType));
            }

            OAPath refPath = new OAPath();
            refPath.setRef(PATH_PATHS + ecodeForRef(path));
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
                oaPath.addParameter(new OAParameter(PARAM_ENTITY_ID));
            }
            oaPath.addParameter(new OAParameter(PARAM_SKIP));
            oaPath.addParameter(new OAParameter(PARAM_TOP));
            oaPath.addParameter(new OAParameter(PARAM_COUNT));
            oaPath.addParameter(new OAParameter(PARAM_FILTER));
            OAPath refPath = new OAPath();
            refPath.setRef(PATH_PATHS + ecodeForRef(path));
            context.getPathTargets().put(reference, refPath);
        }
        return oaPath;
    }

    private static OAResponse createEntitySetGet200Response(GeneratorContext context, EntityType entityType) {
        OAComponents components = context.getDocument().getComponents();
        String name = entityType.plural + "-get-200";
        if (!context.getResponseTargets().containsKey(name)) {
            String schemaName = entityType.plural;
            if (!components.hasSchema(schemaName)) {
                createEntitySetSchema(context, entityType);
            }
            OAResponse resp = new OAResponse();
            OAMediaType jsonType = new OAMediaType(new OASchema(PATH_COMPONENTS_SCHEMAS + schemaName));
            resp.addContent(CONTENT_TYPE_APPLICATION_JSON, jsonType);
            resp.setDescription("A set of entities of type " + entityType.entityName);
            context.getDocument().getComponents().addResponse(name, resp);

            OAResponse ref = new OAResponse();
            ref.setRef(PATH_COMPONENTS_RESPONSES + name);
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
            resp.setDescription("Creation of an entity of type " + entityType.entityName);
            context.getDocument().getComponents().addResponse(name, resp);

            OAResponse ref = new OAResponse();
            ref.setRef(PATH_COMPONENTS_RESPONSES + name);
            context.getResponseTargets().put(name, ref);
        }
        return context.getResponseTargets().get(name);
    }

    private static void createLocationHeader(GeneratorContext context) {
        if (!context.getDocument().getComponents().hasHeader("location")) {
            context.getDocument().getComponents().addHeader(
                    "location",
                    new OAHeader("The selflink of the newly created entity.", new OASchema(OASchema.Type.STRING, null)));
        }
    }

    private static void createEntitySetSchema(GeneratorContext context, EntityType entityType) {
        final Version version = context.getVersion();
        OAComponents components = context.getDocument().getComponents();
        String schemaName = entityType.plural;
        OASchema schema = new OASchema(OASchema.Type.OBJECT, null);
        components.addSchema(schemaName, schema);

        OASchema nextLink = new OASchema("#/components/schemas/nextLink");
        schema.addProperty(version.nextLinkName, nextLink);

        OASchema count = new OASchema("#/components/schemas/count");
        schema.addProperty(version.countName, count);

        OASchema value = new OASchema(OASchema.Type.ARRAY, null);
        value.setItems(new OASchema(PATH_COMPONENTS_SCHEMAS + entityType.entityName));
        schema.addProperty("value", value);
    }

    private static OAPath createPathForEntity(GeneratorContext context, String path, EntityType entityType) {
        String reference = entityType.entityName;
        OAPath oaPath;
        if (context.getPathTargets().containsKey(reference)) {
            oaPath = context.getPathTargets().get(reference);
        } else {
            oaPath = new OAPath();
            oaPath.addParameter(new OAParameter(PARAM_ENTITY_ID));
            oaPath.setGet(new OAOperation()
                    .addParameter(new OAParameter(PARAM_SELECT))
                    .addParameter(new OAParameter(PARAM_EXPAND))
                    .addResponse("200", createEntityGet200Response(context, entityType)));

            if (context.isAddEditing()) {
                OAMediaType jsonType = new OAMediaType(new OASchema(PATH_COMPONENTS_SCHEMAS + entityType.entityName));
                oaPath.setPatch(new OAOperation()
                        .setRequestBody(new OARequestBody()
                                .setDescription("Patches the entity of type " + entityType.entityName)
                                .setRequired(true)
                                .addContent(CONTENT_TYPE_APPLICATION_JSON, jsonType))
                        .addResponse("201", createEntityPatch200Response(context, entityType)));

                oaPath.setPut(new OAOperation()
                        .setRequestBody(new OARequestBody()
                                .setDescription("Replaces the entity of type " + entityType.entityName)
                                .setRequired(true)
                                .addContent(CONTENT_TYPE_APPLICATION_JSON, jsonType))
                        .addResponse("201", createEntityPatch200Response(context, entityType)));

                oaPath.setDelete(new OAOperation()
                        .addResponse("201", createEntityDelete200Response(context, entityType)));
            }

            OAPath refPath = new OAPath();
            refPath.setRef(PATH_PATHS + ecodeForRef(path));
            context.getPathTargets().put(reference, refPath);
        }
        return oaPath;
    }

    private static OAResponse createEntityGet200Response(GeneratorContext context, EntityType entityType) {
        OAComponents components = context.getDocument().getComponents();
        String name = entityType.entityName + "-get-200";
        if (!context.getResponseTargets().containsKey(name)) {
            String schemaName = entityType.entityName;
            if (!components.hasSchema(schemaName)) {
                createEntitySchema(context, entityType);
            }
            OAResponse resp = new OAResponse();
            OAMediaType jsonType = new OAMediaType(new OASchema(PATH_COMPONENTS_SCHEMAS + schemaName));
            resp.addContent(CONTENT_TYPE_APPLICATION_JSON, jsonType);
            resp.setDescription("A single entity of type " + entityType.entityName);
            context.getDocument().getComponents().addResponse(name, resp);

            OAResponse ref = new OAResponse();
            ref.setRef(PATH_COMPONENTS_RESPONSES + name);
            context.getResponseTargets().put(name, ref);
        }
        return context.getResponseTargets().get(name);

    }

    private static OAResponse createEntityPatch200Response(GeneratorContext context, EntityType entityType) {
        String name = entityType.plural + "-patch-200";
        if (!context.getResponseTargets().containsKey(name)) {
            OAResponse resp = new OAResponse();
            resp.setDescription("Patch of single entity of type " + entityType.entityName);
            context.getDocument().getComponents().addResponse(name, resp);

            OAResponse ref = new OAResponse();
            ref.setRef(PATH_COMPONENTS_RESPONSES + name);
            context.getResponseTargets().put(name, ref);
        }
        return context.getResponseTargets().get(name);
    }

    private static OAResponse createEntityDelete200Response(GeneratorContext context, EntityType entityType) {
        String name = entityType.plural + "-delete-200";
        if (!context.getResponseTargets().containsKey(name)) {
            OAResponse resp = new OAResponse();
            resp.setDescription("Delete of single entity of type " + entityType.entityName);
            context.getDocument().getComponents().addResponse(name, resp);

            OAResponse ref = new OAResponse();
            ref.setRef(PATH_COMPONENTS_RESPONSES + name);
            context.getResponseTargets().put(name, ref);
        }
        return context.getResponseTargets().get(name);
    }

    private static void createEntitySchema(GeneratorContext context, EntityType entityType) {
        final Version version = context.getVersion();
        final OAComponents components = context.getDocument().getComponents();
        final String schemaName = entityType.entityName;
        final OASchema schema = new OASchema(OASchema.Type.OBJECT, null);
        components.addSchema(schemaName, schema);

        for (Property property : entityType.getPropertySet()) {
            String propertyName = property.getJsonName();
            if (property instanceof EntityPropertyMain epm) {
                if (epm.getAliases().contains("@iot.id")) {
                    propertyName = version.getIdName();
                }

                OASchema propSchema;
                if (ModelRegistry.EP_PROPERTIES.equals(epm)) {
                    propSchema = new OASchema("#/components/schemas/properties");
                } else if (ModelRegistry.EP_SELFLINK.equals(epm)) {
                    propSchema = new OASchema("#/components/schemas/selfLink");
                } else {
                    propSchema = new OASchema(context.getVersion(), epm.getType());
                }
                if (entityType.getPrimaryKey().getKeyProperties().contains(epm)) {
                    propSchema.setReadOnly(true);
                }
                schema.addProperty(propertyName, propSchema);
            } else {
                if (property instanceof NavigationProperty np) {
                    if (np.isEntitySet()) {
                        OASchema propSchema = new OASchema(OASchema.Type.ARRAY, null);
                        propSchema.setItems(new OASchema(PATH_COMPONENTS_SCHEMAS + np.getEntityType().entityName));
                        schema.addProperty(propertyName, propSchema);

                        OASchema count = new OASchema("#/components/schemas/count");
                        schema.addProperty(np.getEntityType().plural + version.countName, count);

                        OASchema navLink = new OASchema("#/components/schemas/navigationLink");
                        schema.addProperty(np.getEntityType().plural + version.navLinkName, navLink);

                        OASchema nextLink = new OASchema("#/components/schemas/nextLink");
                        schema.addProperty(np.getEntityType().plural + version.nextLinkName, nextLink);
                    } else {
                        OASchema propSchema = new OASchema(PATH_COMPONENTS_SCHEMAS + np.getEntityType().entityName);
                        schema.addProperty(propertyName, propSchema);

                        OASchema navLink = new OASchema("#/components/schemas/navigationLink");
                        schema.addProperty(np.getEntityType().entityName + version.navLinkName, navLink);
                    }
                }
            }
        }
    }

    private static void addPathsForSet(GeneratorContext context, OADoc document, int level, Map<String, OAPath> paths, String base, EntityType entityType, String setName, GeneratorContext options) {
        String path = base + "/" + setName;
        OAPath pathCollection = createPathForSet(options, path, entityType, level > 0);
        paths.put(path, pathCollection);

        if (options.isAddRef()) {
            String refPath = path + "/$ref";
            OAPath pathRef = createPathForSetRef(options, refPath, entityType, level > 0);
            paths.put(refPath, pathRef);
        }

        if (level < options.getRecurse()) {
            String baseId = path + "({entityId})";
            OAPath pathBaseId = createPathForEntity(options, baseId, entityType);
            paths.put(baseId, pathBaseId);
            addPathsForEntity(context, document, level, paths, baseId, entityType, options);
        }
    }

    private static void addPathsForEntity(GeneratorContext context, OADoc document, int level, Map<String, OAPath> paths, String base, EntityType entityType, GeneratorContext options) {
        if (options.isAddEntityProperties()) {
            addPathsForEntityProperties(entityType, paths, base, options);
        }
        for (NavigationProperty navProp : entityType.getNavigationSets()) {
            if (level < options.getRecurse()) {
                final EntityType targetType = navProp.getEntityType();
                addPathsForSet(context, document, level + 1, paths, base, targetType, navProp.getName(), options);
            }
        }
        for (NavigationProperty navProp : entityType.getNavigationEntities()) {
            if (level < options.getRecurse()) {
                EntityType type = navProp.getEntityType();
                String baseName = base + "/" + navProp.getName();
                OAPath paPath = createPathForEntity(options, baseName, type);
                paths.put(baseName, paPath);
                addPathsForEntity(context, document, level + 1, paths, baseName, type, options);
            }
        }
    }

    private static void addPathsForEntityProperties(EntityType entityType, Map<String, OAPath> paths, String base, GeneratorContext options) {
        for (Property entityProperty : entityType.getPropertySet()) {
            if (!(entityProperty instanceof NavigationProperty)) {
                OAPath pathProperty = new OAPath();
                pathProperty.addParameter(new OAParameter(PARAM_ENTITY_ID));

                paths.put(base + "/" + entityProperty.getName(), pathProperty);
                if (options.isAddValue()) {
                    paths.put(base + "/" + entityProperty.getName() + "/$value", pathProperty);
                }
            }
        }
    }

    private static String ecodeForRef(String path) {
        return StringUtils.replaceEach(path, FIND_CHARS, REPLACE_CHARS);
    }
}
