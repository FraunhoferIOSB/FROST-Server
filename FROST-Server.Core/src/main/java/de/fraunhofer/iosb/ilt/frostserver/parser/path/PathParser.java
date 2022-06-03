/*
 * Copyright (C) 2016 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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
package de.fraunhofer.iosb.ilt.frostserver.parser.path;

import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.ModelRegistry;
import de.fraunhofer.iosb.ilt.frostserver.path.PathElement;
import de.fraunhofer.iosb.ilt.frostserver.path.PathElementArrayIndex;
import de.fraunhofer.iosb.ilt.frostserver.path.PathElementCustomProperty;
import de.fraunhofer.iosb.ilt.frostserver.path.PathElementEntity;
import de.fraunhofer.iosb.ilt.frostserver.path.PathElementEntitySet;
import de.fraunhofer.iosb.ilt.frostserver.path.PathElementProperty;
import de.fraunhofer.iosb.ilt.frostserver.path.ResourcePath;
import de.fraunhofer.iosb.ilt.frostserver.path.Version;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain.NavigationPropertyEntity;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain.NavigationPropertyEntitySet;
import de.fraunhofer.iosb.ilt.frostserver.property.Property;
import de.fraunhofer.iosb.ilt.frostserver.util.StringHelper;
import de.fraunhofer.iosb.ilt.frostserver.util.pathparser.Node.Visitor;
import de.fraunhofer.iosb.ilt.frostserver.util.pathparser.PParser;
import de.fraunhofer.iosb.ilt.frostserver.util.pathparser.ParseException;
import de.fraunhofer.iosb.ilt.frostserver.util.pathparser.Token;
import de.fraunhofer.iosb.ilt.frostserver.util.pathparser.nodes.P_EntityId;
import de.fraunhofer.iosb.ilt.frostserver.util.pathparser.nodes.T_ARRAYINDEX;
import de.fraunhofer.iosb.ilt.frostserver.util.pathparser.nodes.T_LONG;
import de.fraunhofer.iosb.ilt.frostserver.util.pathparser.nodes.T_NAME;
import de.fraunhofer.iosb.ilt.frostserver.util.pathparser.nodes.T_REF;
import de.fraunhofer.iosb.ilt.frostserver.util.pathparser.nodes.T_STR_LIT;
import de.fraunhofer.iosb.ilt.frostserver.util.pathparser.nodes.T_VALUE;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PathParser extends Visitor {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(PathParser.class);

    private final ModelRegistry modelRegistry;
    private final ResourcePath resourcePath;
    private boolean foundFirstId;

    /**
     * Parse the given path, assuming UTF-8 encoding.
     *
     * @param modelRegistry The Model Registry to use.
     * @param serviceRootUrl The root URL of the service.
     * @param version The version of the service.
     * @param path The path to parse.
     * @return The parsed ResourcePath.
     */
    public static ResourcePath parsePath(ModelRegistry modelRegistry, String serviceRootUrl, Version version, String path) {
        return parsePath(modelRegistry, serviceRootUrl, version, path, StringHelper.UTF8);
    }

    /**
     * Parse the given path.
     *
     * @param modelRegistry The Model Registry to use.
     * @param serviceRootUrl The root URL of the service.
     * @param version The version of the service.
     * @param path The path to parse.
     * @param encoding The character encoding to use when parsing.
     * @return The parsed ResourcePath.
     */
    public static ResourcePath parsePath(ModelRegistry modelRegistry, String serviceRootUrl, Version version, String path, Charset encoding) {
        ResourcePath resourcePath = new ResourcePath();
        resourcePath.setServiceRootUrl(serviceRootUrl);
        resourcePath.setVersion(version);
        if (path == null) {
            resourcePath.setPath("");
            return resourcePath;
        }
        resourcePath.setPath(path);
        LOGGER.debug("Parsing: {}", path);
        InputStream is = new ByteArrayInputStream(path.getBytes(encoding));
        PParser parser = new PParser(is);
        try {
            parser.Start();
            PathParser pp = new PathParser(modelRegistry, resourcePath);
            pp.visit(parser.rootNode());
        } catch (ParseException ex) {
            throw new IllegalArgumentException("Path " + StringHelper.cleanForLogging(path) + " is not valid: " + ex.getMessage());
        }
        return resourcePath;
    }

    public PathParser(ModelRegistry modelRegistry, ResourcePath resourcePath) {
        this.modelRegistry = modelRegistry;
        this.resourcePath = resourcePath;
    }

    private void addAsEntity(EntityType type, String id) {
        PathElementEntity epa = new PathElementEntity(type, resourcePath.getLastElement());
        if (id != null) {
            epa.setId(type.parsePrimaryKey(id));
            resourcePath.setIdentifiedElement(epa);
        }
        resourcePath.addPathElement(epa, true, false);
    }

    private void addAsEntity(NavigationPropertyEntity type, String id) {
        PathElementEntity epa = new PathElementEntity(type, resourcePath.getLastElement());
        if (id != null) {
            epa.setId((type).getEntityType().parsePrimaryKey(id));
            resourcePath.setIdentifiedElement(epa);
        }
        resourcePath.addPathElement(epa, true, false);
    }

    private void addAsEntitySet(EntityType type) {
        if (resourcePath.getLastElement() != null) {
            throw new IllegalArgumentException("Adding a set by type should only happen on an empty path. Add a set by NavigationProperty instead." + resourcePath);
        }
        PathElementEntitySet espa = new PathElementEntitySet(type);
        resourcePath.addPathElement(espa, true, false);
    }

    private void addAsEntitySet(NavigationPropertyMain type) {
        if (type instanceof NavigationPropertyEntitySet) {
            PathElementEntitySet espa = new PathElementEntitySet((NavigationPropertyEntitySet) type, resourcePath.getLastElement());
            resourcePath.addPathElement(espa, true, false);
        } else {
            throw new IllegalArgumentException("NavigationProperty should be of type NavigationPropertyEntitySet, got: " + StringHelper.cleanForLogging(type));
        }
    }

    private void addAsEntityProperty(EntityPropertyMain type) {
        PathElementProperty ppe = new PathElementProperty();
        ppe.setProperty(type);
        ppe.setParent(resourcePath.getLastElement());
        resourcePath.addPathElement(ppe);
    }

    private void addAsCustomProperty(String name) {
        PathElementCustomProperty cppa = new PathElementCustomProperty();
        cppa.setName(name);
        cppa.setParent(resourcePath.getLastElement());
        resourcePath.addPathElement(cppa);
    }

    private void addAsArrayIndex(Token node) {
        final String image = node.getImage();
        final PathElement parent = resourcePath.getLastElement();
        if (!(parent instanceof PathElementProperty || parent instanceof PathElementCustomProperty || parent instanceof PathElementArrayIndex)) {
            throw new IllegalArgumentException("Array indices must follow a property or array index: " + StringHelper.cleanForLogging(image));
        }
        if (!image.startsWith("[") && image.endsWith("]")) {
            throw new IllegalArgumentException("Received node is not an array index: " + StringHelper.cleanForLogging(image));
        }
        String numberString = image.substring(1, image.length() - 1);
        try {
            final int index = Integer.parseInt(numberString);
            final PathElementArrayIndex cpai = new PathElementArrayIndex();
            cpai.setIndex(index);
            cpai.setParent(parent);
            resourcePath.addPathElement(cpai);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Array indices must be integer values. Failed to parse: " + StringHelper.cleanForLogging(image));
        }
    }

    public void visit(T_REF node) {
        resourcePath.setRef(true);
        recurse(node);
    }

    public void visit(T_VALUE node) {
        resourcePath.setValue(true);
        recurse(node);
    }

    public void visit(T_NAME node) {
        final String name = node.getImage();
        final PathElement parent = resourcePath.getLastElement();
        if (parent == null) {
            final EntityType entityType = modelRegistry.getEntityTypeForName(name);
            if (entityType == null) {
                throw new IllegalArgumentException("Unknown EntityType: '" + StringHelper.cleanForLogging(name) + "'");
            }
            if (!entityType.plural.equals(name)) {
                throw new IllegalArgumentException("Path must start with an EntitySet.");
            }
            addAsEntitySet(entityType);
            return;
        }
        if (!foundFirstId) {
            throw new IllegalArgumentException("Second element should be an ID or $ref, not " + StringHelper.cleanForLogging(node.toString()));
        }

        final EntityType parentType;
        if (parent instanceof PathElementEntitySet) {
            throw new IllegalArgumentException("A property name can not follow a set: " + StringHelper.cleanForLogging(node.toString()));
        }

        if (parent instanceof PathElementEntity) {
            PathElementEntity parentEntity = (PathElementEntity) parent;
            parentType = parentEntity.getEntityType();
            Property property = parentType.getProperty(name);
            if (property instanceof EntityPropertyMain) {
                addAsEntityProperty((EntityPropertyMain) property);
                return;
            }
            if (property instanceof NavigationPropertyEntity) {
                addAsEntity((NavigationPropertyEntity) property, null);
                return;
            }
            if (property instanceof NavigationPropertyEntitySet) {
                addAsEntitySet((NavigationPropertyEntitySet) property);
                return;
            }
            throw new IllegalArgumentException("EntityType " + parentType + " does not have a property: " + StringHelper.cleanForLogging(node.getImage()));
        }

        if (parent instanceof PathElementProperty) {
            addAsCustomProperty(name);
            return;
        }
        if (parent instanceof PathElementCustomProperty) {
            addAsCustomProperty(name);
            return;
        }
        if (parent instanceof PathElementArrayIndex) {
            addAsCustomProperty(name);
            return;
        }

        throw new IllegalArgumentException("Do not know what to do with: " + StringHelper.cleanForLogging(node.toString()));
    }

    public void visit(T_ARRAYINDEX node) {
        addAsArrayIndex(node);
        recurse(node);
    }

    public void visit(P_EntityId node) {
        foundFirstId = true;
        recurse(node);
    }

    public void visit(T_LONG node) {
        handleIdentifierToken(node);
    }

    public void visit(T_STR_LIT node) {
        handleIdentifierToken(node);
    }

    void handleIdentifierToken(Token node) throws IllegalArgumentException {
        final PathElement parent = resourcePath.getLastElement();
        if (parent instanceof PathElementEntitySet) {
            final PathElementEntitySet parentSet = (PathElementEntitySet) parent;
            addAsEntity(parentSet.getEntityType(), node.getImage());
        } else {
            throw new IllegalArgumentException("An ID must follow a set: " + StringHelper.cleanForLogging(node.getImage()));
        }
    }

}
