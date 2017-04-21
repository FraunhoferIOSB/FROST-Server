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
package de.fraunhofer.iosb.ilt.sta.parser.path;

import de.fraunhofer.iosb.ilt.sta.model.id.LongId;
import de.fraunhofer.iosb.ilt.sta.path.CustomPropertyArrayIndex;
import de.fraunhofer.iosb.ilt.sta.path.CustomPropertyPathElement;
import de.fraunhofer.iosb.ilt.sta.path.EntityPathElement;
import de.fraunhofer.iosb.ilt.sta.path.EntityProperty;
import de.fraunhofer.iosb.ilt.sta.path.EntitySetPathElement;
import de.fraunhofer.iosb.ilt.sta.path.EntityType;
import de.fraunhofer.iosb.ilt.sta.path.PropertyPathElement;
import de.fraunhofer.iosb.ilt.sta.path.ResourcePath;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PathParser implements ParserVisitor {

    private static final Charset ENCODING = Charset.forName("UTF-8");
    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(PathParser.class);

    public static ResourcePath parsePath(String serviceRootUrl, String path) {
        return parsePath(serviceRootUrl, path, ENCODING);
    }

    public static ResourcePath parsePath(String serviceRootUrl, String path, Charset encoding) {
        ResourcePath resourcePath = new ResourcePath();
        resourcePath.setServiceRootUrl(serviceRootUrl);
        resourcePath.setPathUrl(path);
        resourcePath.setPathElements(new ArrayList<>());
        if (path == null) {
            return resourcePath;
        }
        LOGGER.debug("Parsing: {}", path);
        InputStream is = new ByteArrayInputStream(path.getBytes(encoding));
        Parser t = new Parser(is, ENCODING.name());
        try {
            ASTStart start = t.Start();
            PathParser v = new PathParser();
            start.jjtAccept(v, resourcePath);
        } catch (ParseException | TokenMgrError ex) {
            LOGGER.error("Failed to parse because (Set loglevel to trace for stack): {}", ex.getMessage());
            LOGGER.trace("Exception: ", ex);
            throw new IllegalStateException("Path is not valid.");
        }
        return resourcePath;
    }

    public ResourcePath defltAction(SimpleNode node, ResourcePath data) {
        if (node.value == null) {
            LOGGER.debug(node.toString());
        } else {
            LOGGER.debug("{} : ({}){}", node, node.value.getClass().getSimpleName(), node.value);
        }
        node.childrenAccept(this, data);
        return data;
    }

    private void addAsEntitiy(ResourcePath rp, SimpleNode node, EntityType type) {
        EntityPathElement epa = new EntityPathElement();
        epa.setEntityType(type);
        if (node.value != null) {
            try {
                long id = Long.parseLong(node.value.toString());
                epa.setId(new LongId(id));
            } catch (NumberFormatException e) {
                epa.setId(new LongId(0L));
            }
            rp.setIdentifiedElement(epa);
        }
        epa.setParent(rp.getLastElement());
        rp.getPathElements().add(epa);
        rp.setMainElement(epa);
    }

    private void addAsEntitiySet(ResourcePath rp, EntityType type) {
        EntitySetPathElement espa = new EntitySetPathElement();
        espa.setEntityType(type);
        espa.setParent(rp.getLastElement());
        rp.getPathElements().add(espa);
        rp.setMainElement(espa);
    }

    private void addAsEntitiyProperty(ResourcePath rp, EntityProperty type) {
        PropertyPathElement ppe = new PropertyPathElement();
        ppe.setProperty(type);
        ppe.setParent(rp.getLastElement());
        rp.getPathElements().add(ppe);
    }

    private void addAsCustomProperty(ResourcePath rp, SimpleNode node) {
        CustomPropertyPathElement cppa = new CustomPropertyPathElement();
        cppa.setName(node.value.toString());
        cppa.setParent(rp.getLastElement());
        rp.getPathElements().add(cppa);
    }

    private void addAsArrayIndex(ResourcePath rp, SimpleNode node) {
        CustomPropertyArrayIndex cpai = new CustomPropertyArrayIndex();
        String image = node.value.toString();
        if (!image.startsWith("[") && image.endsWith("]")) {
            throw new IllegalArgumentException("Received node is not an array index: " + image);
        }
        String numberString = image.substring(1, image.length() - 1);
        try {
            int index = Integer.parseInt(numberString);
            cpai.setIndex(index);
            cpai.setParent(rp.getLastElement());
            rp.getPathElements().add(cpai);
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
    public ResourcePath visit(ASTeDatastream node, ResourcePath data) {
        addAsEntitiy(data, node, EntityType.Datastream);
        return defltAction(node, data);
    }

    @Override
    public ResourcePath visit(ASTcDatastreams node, ResourcePath data) {
        addAsEntitiySet(data, EntityType.Datastream);
        return defltAction(node, data);
    }

    @Override
    public ResourcePath visit(ASTeMultiDatastream node, ResourcePath data) {
        addAsEntitiy(data, node, EntityType.MultiDatastream);
        return defltAction(node, data);
    }

    @Override
    public ResourcePath visit(ASTcMultiDatastreams node, ResourcePath data) {
        addAsEntitiySet(data, EntityType.MultiDatastream);
        return defltAction(node, data);
    }

    @Override
    public ResourcePath visit(ASTeFeatureOfInterest node, ResourcePath data) {
        addAsEntitiy(data, node, EntityType.FeatureOfInterest);
        return defltAction(node, data);
    }

    @Override
    public ResourcePath visit(ASTcFeaturesOfInterest node, ResourcePath data) {
        addAsEntitiySet(data, EntityType.FeatureOfInterest);
        return defltAction(node, data);
    }

    @Override
    public ResourcePath visit(ASTeHistLocation node, ResourcePath data) {
        addAsEntitiy(data, node, EntityType.HistoricalLocation);
        return defltAction(node, data);
    }

    @Override
    public ResourcePath visit(ASTcHistLocations node, ResourcePath data) {
        addAsEntitiySet(data, EntityType.HistoricalLocation);
        return defltAction(node, data);
    }

    @Override
    public ResourcePath visit(ASTeLocation node, ResourcePath data) {
        addAsEntitiy(data, node, EntityType.Location);
        return defltAction(node, data);
    }

    @Override
    public ResourcePath visit(ASTcLocations node, ResourcePath data) {
        addAsEntitiySet(data, EntityType.Location);
        return defltAction(node, data);
    }

    @Override
    public ResourcePath visit(ASTeSensor node, ResourcePath data) {
        addAsEntitiy(data, node, EntityType.Sensor);
        return defltAction(node, data);
    }

    @Override
    public ResourcePath visit(ASTcSensors node, ResourcePath data) {
        addAsEntitiySet(data, EntityType.Sensor);
        return defltAction(node, data);
    }

    @Override
    public ResourcePath visit(ASTeThing node, ResourcePath data) {
        addAsEntitiy(data, node, EntityType.Thing);
        return defltAction(node, data);
    }

    @Override
    public ResourcePath visit(ASTcThings node, ResourcePath data) {
        addAsEntitiySet(data, EntityType.Thing);
        return defltAction(node, data);
    }

    @Override
    public ResourcePath visit(ASTeObservation node, ResourcePath data) {
        addAsEntitiy(data, node, EntityType.Observation);
        return defltAction(node, data);
    }

    @Override
    public ResourcePath visit(ASTcObservations node, ResourcePath data) {
        addAsEntitiySet(data, EntityType.Observation);
        return defltAction(node, data);
    }

    @Override
    public ResourcePath visit(ASTeObservedProp node, ResourcePath data) {
        addAsEntitiy(data, node, EntityType.ObservedProperty);
        return defltAction(node, data);
    }

    @Override
    public ResourcePath visit(ASTcObservedProps node, ResourcePath data) {
        addAsEntitiySet(data, EntityType.ObservedProperty);
        return defltAction(node, data);
    }

    @Override
    public ResourcePath visit(ASTpId node, ResourcePath data) {
        addAsEntitiyProperty(data, EntityProperty.Id);
        return defltAction(node, data);
    }

    @Override
    public ResourcePath visit(ASTpSelfLink node, ResourcePath data) {
        addAsEntitiyProperty(data, EntityProperty.SelfLink);
        return defltAction(node, data);
    }

    @Override
    public ResourcePath visit(ASTpDescription node, ResourcePath data) {
        addAsEntitiyProperty(data, EntityProperty.Description);
        return defltAction(node, data);
    }

    @Override
    public ResourcePath visit(ASTpDefinition node, ResourcePath data) {
        addAsEntitiyProperty(data, EntityProperty.Definition);
        return defltAction(node, data);
    }

    @Override
    public ResourcePath visit(ASTpEncodingType node, ResourcePath data) {
        addAsEntitiyProperty(data, EntityProperty.EncodingType);
        return defltAction(node, data);
    }

    @Override
    public ResourcePath visit(ASTpFeature node, ResourcePath data) {
        addAsEntitiyProperty(data, EntityProperty.Feature);
        return defltAction(node, data);
    }

    @Override
    public ResourcePath visit(ASTpLocation node, ResourcePath data) {
        addAsEntitiyProperty(data, EntityProperty.Location);
        return defltAction(node, data);
    }

    @Override
    public ResourcePath visit(ASTpMetadata node, ResourcePath data) {
        addAsEntitiyProperty(data, EntityProperty.Metadata);
        return defltAction(node, data);
    }

    @Override
    public ResourcePath visit(ASTpName node, ResourcePath data) {
        addAsEntitiyProperty(data, EntityProperty.Name);
        return defltAction(node, data);
    }

    @Override
    public ResourcePath visit(ASTpObservationType node, ResourcePath data) {
        addAsEntitiyProperty(data, EntityProperty.ObservationType);
        return defltAction(node, data);
    }

    @Override
    public ResourcePath visit(ASTpMultiObservationDataTypes node, ResourcePath data) {
        addAsEntitiyProperty(data, EntityProperty.MultiObservationDataTypes);
        return defltAction(node, data);
    }

    @Override
    public ResourcePath visit(ASTpPhenomenonTime node, ResourcePath data) {
        addAsEntitiyProperty(data, EntityProperty.PhenomenonTime);
        return defltAction(node, data);
    }

    @Override
    public ResourcePath visit(ASTpProperties node, ResourcePath data) {
        addAsEntitiyProperty(data, EntityProperty.Properties);
        return defltAction(node, data);
    }

    @Override
    public ResourcePath visit(ASTpResult node, ResourcePath data) {
        addAsEntitiyProperty(data, EntityProperty.Result);
        return defltAction(node, data);
    }

    @Override
    public ResourcePath visit(ASTpResultTime node, ResourcePath data) {
        addAsEntitiyProperty(data, EntityProperty.ResultTime);
        return defltAction(node, data);
    }

    @Override
    public ResourcePath visit(ASTpTime node, ResourcePath data) {
        addAsEntitiyProperty(data, EntityProperty.Time);
        return defltAction(node, data);
    }

    @Override
    public ResourcePath visit(ASTpUnitOfMeasurement node, ResourcePath data) {
        addAsEntitiyProperty(data, EntityProperty.UnitOfMeasurement);
        return defltAction(node, data);
    }

    @Override
    public ResourcePath visit(ASTpUnitOfMeasurements node, ResourcePath data) {
        addAsEntitiyProperty(data, EntityProperty.UnitOfMeasurements);
        return defltAction(node, data);
    }

    @Override
    public ResourcePath visit(ASTcpRef node, ResourcePath data) {
        data.setRef(true);
        return defltAction(node, data);
    }

    @Override
    public ResourcePath visit(ASTppValue node, ResourcePath data) {
        data.setValue(true);
        return defltAction(node, data);
    }

    @Override
    public ResourcePath visit(ASTppSubProperty node, ResourcePath data) {
        addAsCustomProperty(data, node);
        return defltAction(node, data);
    }

    @Override
    public ResourcePath visit(ASTppArrayIndex node, ResourcePath data) {
        addAsArrayIndex(data, node);
        return defltAction(node, data);
    }

    @Override
    public ResourcePath visit(ASTLong node, ResourcePath data) {
        return defltAction(node, data);
    }

    @Override
    public ResourcePath visit(ASTpObservedArea node, ResourcePath data) {
        addAsEntitiyProperty(data, EntityProperty.ObservedArea);
        return defltAction(node, data);
    }

    @Override
    public ResourcePath visit(ASTpParameters node, ResourcePath data) {
        addAsEntitiyProperty(data, EntityProperty.Parameters);
        return defltAction(node, data);
    }

    @Override
    public ResourcePath visit(ASTpResultQuality node, ResourcePath data) {
        addAsEntitiyProperty(data, EntityProperty.ResultQuality);
        return defltAction(node, data);
    }

    @Override
    public ResourcePath visit(ASTpValidTime node, ResourcePath data) {
        addAsEntitiyProperty(data, EntityProperty.ValidTime);
        return defltAction(node, data);
    }

}
