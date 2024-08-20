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
package de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.validator;

import de.fraunhofer.iosb.ilt.configurable.annotations.ConfigurableField;
import de.fraunhofer.iosb.ilt.configurable.editor.EditorEnum;
import de.fraunhofer.iosb.ilt.configurable.editor.EditorString;
import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.model.core.EntitySet;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.JooqPersistenceManager;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A check that succeeds if a NavigationLink of a new or updated Entity is empty
 * and fails if it is not empty.
 */
public class CheckNavLinkEmpty implements ValidationCheck {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(CheckNavLinkEmpty.class);

    public enum EmptyState {
        MUST_BE_EMPTY("Must be Empty"),
        MUST_BE_FILLED("Must be Filled (non Empty)");

        public final String label;

        private EmptyState(String label) {
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }

    }

    @ConfigurableField(editor = EditorString.class,
            label = "Target NavLink", description = "The navLink that must be (not) set.")
    @EditorString.EdOptsString
    private String targetNavLink;

    @ConfigurableField(editor = EditorEnum.class,
            label = "Req. State", description = "The required state: empty or filled (non-empty)")
    @EditorEnum.EdOptsEnum(sourceType = EmptyState.class, dflt = "MUST_BE_EMPTY")
    private EmptyState requiredState = EmptyState.MUST_BE_EMPTY;

    private EntityType entityType;
    private EntityType targetType;
    private NavigationPropertyMain targetNp;

    @Override
    public boolean check(JooqPersistenceManager pm, Entity contextEntity) {
        if (targetType == null) {
            init(contextEntity);
        }
        final boolean emptyRequired = requiredState == EmptyState.MUST_BE_EMPTY;
        final boolean filledRequired = requiredState == EmptyState.MUST_BE_FILLED;
        if (targetNp instanceof NavigationPropertyMain.NavigationPropertyEntity targetNpEntity) {
            final Entity targetEntity = contextEntity.getProperty(targetNpEntity);
            final boolean empty = targetEntity == null;
            final boolean valid = (emptyRequired && empty) || (filledRequired && !empty);
            LOGGER.debug("  Check on {}.{}: {}", entityType, targetNp, valid);
            return valid;
        }
        if (targetNp instanceof NavigationPropertyMain.NavigationPropertyEntitySet targetNpEntitySet) {
            EntitySet targetEntities = contextEntity.getProperty(targetNpEntitySet);
            final boolean empty = targetEntities == null || targetEntities.isEmpty();
            final boolean valid = (emptyRequired && empty) || (filledRequired && !empty);
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

    public EmptyState getRequiredState() {
        return requiredState;
    }

    public void setRequiredState(EmptyState requiredState) {
        this.requiredState = requiredState;
    }

    @Override
    public String toString() {
        return "CheckNavLinkEmpty: " + targetNavLink;
    }

}
