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
package de.fraunhofer.iosb.ilt.frostserver.plugin.coremodelv2;

import de.fraunhofer.iosb.ilt.configurable.annotations.ConfigurableField;
import de.fraunhofer.iosb.ilt.configurable.editor.EditorString;
import de.fraunhofer.iosb.ilt.frostserver.model.ModelRegistry;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.model.core.EntitySet;
import de.fraunhofer.iosb.ilt.frostserver.model.core.EntitySetImpl;
import de.fraunhofer.iosb.ilt.frostserver.model.core.EntityValidator;
import de.fraunhofer.iosb.ilt.frostserver.path.UrlHelper;
import de.fraunhofer.iosb.ilt.frostserver.plugin.coremodelv2.swecommon.AbstractDataComponent;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain.NavigationPropertyEntitySet;
import de.fraunhofer.iosb.ilt.frostserver.util.SimpleJsonMapper;
import de.fraunhofer.iosb.ilt.frostserver.util.StringHelper;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.IncompleteEntityException;
import java.util.HashSet;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.core.TreeNode;

/**
 * A validator for Datastreams. It checks resultType and resultEncoding. It also
 * sets the ObservedProperties based on the definitions used in the resultType.
 */
public class SweCommonValidator implements EntityValidator {

    private static final Logger LOGGER = LoggerFactory.getLogger(SweCommonValidator.class.getName());

    @ConfigurableField(editor = EditorString.class,
            label = "encoding", description = "Name of the property that holds the encoding")
    @EditorString.EdOptsString()
    private String nameEncoding;

    @ConfigurableField(editor = EditorString.class,
            label = "structure", description = "Name of the property that holds the SWE-Common structure")
    @EditorString.EdOptsString()
    private String nameStructure;

    @ConfigurableField(editor = EditorString.class,
            label = "definitions", description = "Name of the navigation property that collects observed properties")
    @EditorString.EdOptsString()
    private String nameDefinitions;

    private EntityPropertyMain<TreeNode> epEncoding;
    private EntityPropertyMain<TreeNode> epStructure;
    private NavigationPropertyEntitySet npDefinitions;
    private ModelRegistry mr;
    private boolean initialised;

    private void initialise(Entity entity) {
        if (initialised) {
            return;
        }
        if (!StringHelper.isNullOrEmpty(nameEncoding)) {
            epEncoding = entity.getType().getEntityProperty(nameEncoding);
        }
        if (!StringHelper.isNullOrEmpty(nameStructure)) {
            epStructure = entity.getType().getEntityProperty(nameStructure);
        }
        if (!StringHelper.isNullOrEmpty(nameDefinitions)) {
            npDefinitions = entity.getType().getNavigationPropertyEntitySet(nameDefinitions);
        }
        mr = entity.getType().getModelRegistry();
        initialised = true;
    }

    @Override
    public void validate(Entity entity) throws IncompleteEntityException {
        if (!initialised) {
            initialise(entity);
        }
        TreeNode structureTree = entity.getProperty(epStructure);
        if (structureTree == null) {
            return;
        }
        AbstractDataComponent resultType = SimpleJsonMapper.treeToObject(structureTree, AbstractDataComponent.class);

        Set<String> defs = new HashSet<>();
        resultType.gatherDefinitions(defs);

        EntitySet observedProperties = new EntitySetImpl(npDefinitions);
        for (String def : defs) {
            LOGGER.debug("Found definition {}", def);
            Entity op = UrlHelper.parseSelfLink(def, mr, false);
            observedProperties.add(op);
        }
        entity.setProperty(npDefinitions, observedProperties);
    }

    public String getNameEncoding() {
        return nameEncoding;
    }

    public SweCommonValidator setNameEncoding(String nameEncoding) {
        this.nameEncoding = nameEncoding;
        return this;
    }

    public EntityPropertyMain<TreeNode> getEpEncoding() {
        return epEncoding;
    }

    public String getNameStructure() {
        return nameStructure;
    }

    public SweCommonValidator setNameStructure(String nameStructure) {
        this.nameStructure = nameStructure;
        return this;
    }

    public EntityPropertyMain<TreeNode> getEpStructure() {
        return epStructure;
    }

    public String getNameDefinitions() {
        return nameDefinitions;
    }

    public SweCommonValidator setNameDefinitions(String nameDefinitions) {
        this.nameDefinitions = nameDefinitions;
        return this;
    }

}
