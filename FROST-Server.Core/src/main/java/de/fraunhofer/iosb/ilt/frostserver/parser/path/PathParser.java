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
import de.fraunhofer.iosb.ilt.frostserver.persistence.IdManager;
import de.fraunhofer.iosb.ilt.frostserver.persistence.IdManagerLong;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.util.StringHelper;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PathParser implements ParserVisitor {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(PathParser.class);

    private final IdManager idmanager;
    private final ModelRegistry modelRegistry;

    /**
     * Parse the given path with an IdManagerlong and UTF-8 encoding.
     *
     * @param serviceRootUrl The root URL of the service.
     * @param version The version of the service.
     * @param path The path to parse.
     * @return The parsed ResourcePath.
     */
    public static ResourcePath parsePath(ModelRegistry modelRegistry, String serviceRootUrl, Version version, String path) {
        return parsePath(modelRegistry, new IdManagerLong(), serviceRootUrl, version, path, StringHelper.UTF8);
    }

    /**
     * Parse the given path, assuming UTF-8 encoding.
     *
     * @param idmanager The IdManager to use
     * @param serviceRootUrl The root URL of the service.
     * @param version The version of the service.
     * @param path The path to parse.
     * @return The parsed ResourcePath.
     */
    public static ResourcePath parsePath(ModelRegistry modelRegistry, IdManager idmanager, String serviceRootUrl, Version version, String path) {
        return parsePath(modelRegistry, idmanager, serviceRootUrl, version, path, StringHelper.UTF8);
    }

    /**
     * Parse the given path.
     *
     * @param idmanager The IdManager to use.
     * @param serviceRootUrl The root URL of the service.
     * @param version The version of the service.
     * @param path The path to parse.
     * @param encoding The character encoding to use when parsing.
     * @return The parsed ResourcePath.
     */
    public static ResourcePath parsePath(ModelRegistry modelRegistry, IdManager idmanager, String serviceRootUrl, Version version, String path, Charset encoding) {
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
        Parser t = new Parser(is, StringHelper.UTF8.name());
        try {
            ASTStart start = t.Start();
            PathParser v = new PathParser(modelRegistry, idmanager);
            start.jjtAccept(v, resourcePath);
        } catch (ParseException | TokenMgrError ex) {
            LOGGER.error("Failed to parse because (Set loglevel to trace for stack): {}", ex.getMessage());
            LOGGER.trace("Exception: ", ex);
            throw new IllegalArgumentException("Path is not valid: " + ex.getMessage());
        }
        return resourcePath;
    }

    public PathParser(ModelRegistry modelRegistry, IdManager idmanager) {
        this.modelRegistry = modelRegistry;
        this.idmanager = idmanager;
    }

    public ResourcePath defltAction(SimpleNode node, ResourcePath data) {
        if (node.value == null) {
            LOGGER.debug("{}", node);
        } else {
            LOGGER.debug("{} : ({}){}", node, node.value.getClass().getSimpleName(), node.value);
        }
        node.childrenAccept(this, data);
        return data;
    }

    private void addAsEntitiy(ResourcePath rp, SimpleNode node, EntityType type, String id) {
        PathElementEntity epa = new PathElementEntity();
        epa.setEntityType(type);
        if (id != null) {
            epa.setId(idmanager.parseId(id));
            rp.setIdentifiedElement(epa);
        }
        epa.setParent(rp.getLastElement());
        rp.addPathElement(epa, true, false);
    }

    private void addAsEntitiySet(ResourcePath rp, EntityType type) {
        PathElementEntitySet espa = new PathElementEntitySet();
        espa.setEntityType(type);
        espa.setParent(rp.getLastElement());
        rp.addPathElement(espa, true, false);
    }

    private void addAsEntitiyProperty(ResourcePath rp, EntityPropertyMain type) {
        PathElementProperty ppe = new PathElementProperty();
        ppe.setProperty(type);
        ppe.setParent(rp.getLastElement());
        rp.addPathElement(ppe);
    }

    private void addAsCustomProperty(ResourcePath rp, SimpleNode node) {
        PathElementCustomProperty cppa = new PathElementCustomProperty();
        cppa.setName(node.value.toString());
        cppa.setParent(rp.getLastElement());
        rp.addPathElement(cppa);
    }

    private void addAsArrayIndex(ResourcePath rp, SimpleNode node) {
        PathElementArrayIndex cpai = new PathElementArrayIndex();
        String image = node.value.toString();
        if (!image.startsWith("[") && image.endsWith("]")) {
            throw new IllegalArgumentException("Received node is not an array index: " + image);
        }
        String numberString = image.substring(1, image.length() - 1);
        try {
            int index = Integer.parseInt(numberString);
            cpai.setIndex(index);
            cpai.setParent(rp.getLastElement());
            rp.addPathElement(cpai);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Array indices must be integer values. Failed to parse: " + image);
        }
    }

    @Override
    public ResourcePath visit(SimpleNode node, ResourcePath data) {
        LOGGER.error("{}: acceptor not implemented in subclass?", node);
        node.childrenAccept(this, data);
        return data;
    }

    @Override
    public ResourcePath visit(ASTStart node, ResourcePath data) {
        node.childrenAccept(this, data);
        return data;
    }

    @Override
    public ResourcePath visit(ASTIdentifiedPath node, ResourcePath data) {
        return defltAction(node, data);
    }

    @Override
    public ResourcePath visit(ASTRef node, ResourcePath data) {
        data.setRef(true);
        return defltAction(node, data);
    }

    @Override
    public ResourcePath visit(ASTValue node, ResourcePath data) {
        data.setValue(true);
        return defltAction(node, data);
    }

    @Override
    public ResourcePath visit(ASTSubProperty node, ResourcePath data) {
        addAsCustomProperty(data, node);
        return defltAction(node, data);
    }

    @Override
    public ResourcePath visit(ASTArrayIndex node, ResourcePath data) {
        addAsArrayIndex(data, node);
        return defltAction(node, data);
    }

    @Override
    public ResourcePath visit(ASTLong node, ResourcePath data) {
        return defltAction(node, data);
    }

    @Override
    public ResourcePath visit(ASTString node, ResourcePath data) {
        return defltAction(node, data);
    }

    @Override
    public ResourcePath visit(ASTEntityType node, ResourcePath data) {
        PathElement parent = data.getLastElement();
        final String name = node.value.toString();
        EntityType entityType = modelRegistry.getEntityTypeForName(name);
        if (parent == null) {
            if (!entityType.plural.equals(name)) {
                throw new IllegalArgumentException("Path must start with an EntitySet.");
            }
            addAsEntitiySet(data, entityType);
            return defltAction(node, data);
        }
        if (parent instanceof PathElementEntity) {
            PathElementEntity parentEntity = (PathElementEntity) parent;
            EntityType parentType = parentEntity.getEntityType();
            NavigationPropertyMain np = parentType.getNavigationProperty(entityType);
            if (np == null) {
                throw new IllegalArgumentException("Entities of type " + parentEntity.getEntityType() + " do not have a navigation property named " + node.value);
            }
            if (!np.getName().equals(name)) {
                throw new IllegalArgumentException("Entities of type " + parentEntity.getEntityType() + " do not have a navigation property named " + node.value);
            }
            if (entityType.plural.equals(name)) {
                addAsEntitiySet(data, entityType);
                return defltAction(node, data);
            } else {
                addAsEntitiy(data, node, entityType, null);
                return defltAction(node, data);
            }
        }
        throw new IllegalArgumentException("Do not know what to do with: " + node.value);
    }

    @Override
    public ResourcePath visit(ASTentityId node, ResourcePath data) {
        PathElement parent = data.getLastElement();
        if (parent instanceof PathElementEntitySet) {
            PathElementEntitySet parentEntitySet = (PathElementEntitySet) parent;
            addAsEntitiy(data, node, parentEntitySet.getEntityType(), node.value.toString());
            return defltAction(node, data);
        }
        throw new IllegalArgumentException("IDs must follow after EntitySets");
    }

    @Override
    public ResourcePath visit(ASTEntityProperty node, ResourcePath data) {
        PathElement parent = data.getLastElement();
        if (parent instanceof PathElementEntity) {
            PathElementEntity parentEntity = (PathElementEntity) parent;
            EntityPropertyMain property = modelRegistry.getEntityProperty(node.value.toString());
            if (property == null || !parentEntity.getEntityType().getPropertySet().contains(property)) {
                throw new IllegalArgumentException("Entities of type " + parentEntity.getEntityType() + " do not have an entity property named " + node.value);
            }
            addAsEntitiyProperty(data, property);
            return defltAction(node, data);
        }
        throw new IllegalArgumentException("Properties must follow after Entities");
    }

}
