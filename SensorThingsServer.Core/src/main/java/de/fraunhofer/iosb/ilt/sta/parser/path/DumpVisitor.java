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

import de.fraunhofer.iosb.ilt.sta.path.ResourcePath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DumpVisitor implements ParserVisitor {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(DumpVisitor.class);
    private int indent = 0;

    private String indentString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < indent; ++i) {
            sb.append("  ");
        }
        return sb.toString();
    }

    public ResourcePath defltAction(SimpleNode node, ResourcePath data) {
        if (node.value == null) {
            LOGGER.info(indentString() + node);
        } else {
            LOGGER.info("{}{} : ({}){}", indentString(), node, node.value.getClass().getSimpleName(), node.value);
        }
        ++indent;
        node.childrenAccept(this, data);
        --indent;
        return data;
    }

    @Override
    public ResourcePath visit(SimpleNode node, ResourcePath data) {
        LOGGER.info("{}{}: acceptor not implemented in subclass?", indentString(), node);
        ++indent;
        node.childrenAccept(this, data);
        --indent;
        return data;
    }

    @Override
    public ResourcePath visit(ASTStart node, ResourcePath data) {
        LOGGER.info(indentString() + node);
        ++indent;
        node.childrenAccept(this, data);
        --indent;
        return data;
    }

    @Override
    public ResourcePath visit(ASTIdentifiedPath node, ResourcePath data) {
        return defltAction(node, data);
    }

    @Override
    public ResourcePath visit(ASTeDatastream node, ResourcePath data) {
        return defltAction(node, data);
    }

    @Override
    public ResourcePath visit(ASTcDatastreams node, ResourcePath data) {
        return defltAction(node, data);
    }

    @Override
    public ResourcePath visit(ASTeMultiDatastream node, ResourcePath data) {
        return defltAction(node, data);
    }

    @Override
    public ResourcePath visit(ASTcMultiDatastreams node, ResourcePath data) {
        return defltAction(node, data);
    }

    @Override
    public ResourcePath visit(ASTeFeatureOfInterest node, ResourcePath data) {
        return defltAction(node, data);
    }

    @Override
    public ResourcePath visit(ASTcFeaturesOfInterest node, ResourcePath data) {
        return defltAction(node, data);
    }

    @Override
    public ResourcePath visit(ASTeHistLocation node, ResourcePath data) {
        return defltAction(node, data);
    }

    @Override
    public ResourcePath visit(ASTcHistLocations node, ResourcePath data) {
        return defltAction(node, data);
    }

    @Override
    public ResourcePath visit(ASTeLocation node, ResourcePath data) {
        return defltAction(node, data);
    }

    @Override
    public ResourcePath visit(ASTcLocations node, ResourcePath data) {
        return defltAction(node, data);
    }

    @Override
    public ResourcePath visit(ASTeSensor node, ResourcePath data) {
        return defltAction(node, data);
    }

    @Override
    public ResourcePath visit(ASTcSensors node, ResourcePath data) {
        return defltAction(node, data);
    }

    @Override
    public ResourcePath visit(ASTeThing node, ResourcePath data) {
        return defltAction(node, data);
    }

    @Override
    public ResourcePath visit(ASTcThings node, ResourcePath data) {
        return defltAction(node, data);
    }

    @Override
    public ResourcePath visit(ASTeObservation node, ResourcePath data) {
        return defltAction(node, data);
    }

    @Override
    public ResourcePath visit(ASTcObservations node, ResourcePath data) {
        return defltAction(node, data);
    }

    @Override
    public ResourcePath visit(ASTeObservedProp node, ResourcePath data) {
        return defltAction(node, data);
    }

    @Override
    public ResourcePath visit(ASTcObservedProps node, ResourcePath data) {
        return defltAction(node, data);
    }

    @Override
    public ResourcePath visit(ASTpId node, ResourcePath data) {
        return defltAction(node, data);
    }

    @Override
    public ResourcePath visit(ASTpSelfLink node, ResourcePath data) {
        return defltAction(node, data);
    }

    @Override
    public ResourcePath visit(ASTpDescription node, ResourcePath data) {
        return defltAction(node, data);
    }

    @Override
    public ResourcePath visit(ASTpDefinition node, ResourcePath data) {
        return defltAction(node, data);
    }

    @Override
    public ResourcePath visit(ASTpEncodingType node, ResourcePath data) {
        return defltAction(node, data);
    }

    @Override
    public ResourcePath visit(ASTpFeature node, ResourcePath data) {
        return defltAction(node, data);
    }

    @Override
    public ResourcePath visit(ASTpLocation node, ResourcePath data) {
        return defltAction(node, data);
    }

    @Override
    public ResourcePath visit(ASTpMetadata node, ResourcePath data) {
        return defltAction(node, data);
    }

    @Override
    public ResourcePath visit(ASTpName node, ResourcePath data) {
        return defltAction(node, data);
    }

    @Override
    public ResourcePath visit(ASTpObservationType node, ResourcePath data) {
        return defltAction(node, data);
    }

    @Override
    public ResourcePath visit(ASTpMultiObservationDataTypes node, ResourcePath data) {
        return defltAction(node, data);
    }

    @Override
    public ResourcePath visit(ASTpPhenomenonTime node, ResourcePath data) {
        return defltAction(node, data);
    }

    @Override
    public ResourcePath visit(ASTpProperties node, ResourcePath data) {
        return defltAction(node, data);
    }

    @Override
    public ResourcePath visit(ASTpResult node, ResourcePath data) {
        return defltAction(node, data);
    }

    @Override
    public ResourcePath visit(ASTpResultTime node, ResourcePath data) {
        return defltAction(node, data);
    }

    @Override
    public ResourcePath visit(ASTpTime node, ResourcePath data) {
        return defltAction(node, data);
    }

    @Override
    public ResourcePath visit(ASTpUnitOfMeasurement node, ResourcePath data) {
        return defltAction(node, data);
    }

    @Override
    public ResourcePath visit(ASTpUnitOfMeasurements node, ResourcePath data) {
        return defltAction(node, data);
    }

    @Override
    public ResourcePath visit(ASTcpRef node, ResourcePath data) {
        return defltAction(node, data);
    }

    @Override
    public ResourcePath visit(ASTppValue node, ResourcePath data) {
        return defltAction(node, data);
    }

    @Override
    public ResourcePath visit(ASTppSubProperty node, ResourcePath data) {
        return defltAction(node, data);
    }

    @Override
    public ResourcePath visit(ASTppArrayIndex node, ResourcePath data) {
        return defltAction(node, data);
    }

    @Override
    public ResourcePath visit(ASTLong node, ResourcePath data) {
        return defltAction(node, data);
    }

    @Override
    public ResourcePath visit(ASTpObservedArea node, ResourcePath data) {
        return defltAction(node, data);
    }

    @Override
    public ResourcePath visit(ASTpParameters node, ResourcePath data) {
        return defltAction(node, data);
    }

    @Override
    public ResourcePath visit(ASTpResultQuality node, ResourcePath data) {
        return defltAction(node, data);
    }

    @Override
    public ResourcePath visit(ASTpValidTime node, ResourcePath data) {
        return defltAction(node, data);
    }

}
