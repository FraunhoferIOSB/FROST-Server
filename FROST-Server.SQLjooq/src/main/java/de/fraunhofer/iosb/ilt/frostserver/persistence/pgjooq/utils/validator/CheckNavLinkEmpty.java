/*
 * Copyright (C) 2023 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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
package de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.validator;

import de.fraunhofer.iosb.ilt.configurable.annotations.ConfigurableField;
import de.fraunhofer.iosb.ilt.configurable.editor.EditorString;
import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.model.core.EntitySet;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.PostgresPersistenceManager;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author scf
 */
public class CheckNavLinkEmpty implements ValidationCheck {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(CheckNavLinkEmpty.class);

    @ConfigurableField(editor = EditorString.class,
            label = "Target NavLink", description = "The navLink that must be not set.")
    @EditorString.EdOptsString
    private String targetNavLink;

    private EntityType entityType;
    private EntityType targetType;
    private NavigationPropertyMain targetNp;

    @Override
    public boolean check(PostgresPersistenceManager pm, Entity contextEntity) {
        if (targetType == null) {
            init(contextEntity);
        }
        if (targetNp instanceof NavigationPropertyMain.NavigationPropertyEntity targetNpEntity) {
            final Entity targetEntity = contextEntity.getProperty(targetNpEntity);
            final boolean valid = targetEntity == null;
            LOGGER.debug("  Check on {}.{}: {}", entityType, targetNp, valid);
            return valid;
        }
        if (targetNp instanceof NavigationPropertyMain.NavigationPropertyEntitySet targetNpEntitySet) {
            EntitySet targetEntities = contextEntity.getProperty(targetNpEntitySet);
            final boolean valid = targetEntities == null || targetEntities.isEmpty();
            LOGGER.debug("  Check on {}.{}: {}", entityType, targetNp, valid);
            return valid;
        }
        return false;
    }

    private void init(Entity contextEntity) {
        entityType = contextEntity.getEntityType();
        targetNp = entityType.getNavigationProperty(getTargetNavLink());
        targetType = targetNp.getEntityType();
        LOGGER.info("Initialised check on {}.{}", entityType, targetNp);
    }

    public String getTargetNavLink() {
        return targetNavLink;
    }

    public void setTargetNavLink(String targetNavLink) {
        this.targetNavLink = targetNavLink;
    }

    @Override
    public String toString() {
        return "CheckNavLinkEmpty: " + targetNavLink;
    }

}
