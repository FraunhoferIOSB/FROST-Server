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
import de.fraunhofer.iosb.ilt.configurable.editor.EditorEnum;
import de.fraunhofer.iosb.ilt.configurable.editor.EditorList;
import de.fraunhofer.iosb.ilt.configurable.editor.EditorString;
import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.model.core.EntitySet;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.JooqPersistenceManager;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.validator.CheckNavLinkEmpty.EmptyState;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain.NavigationPropertyEntity;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain.NavigationPropertyEntitySet;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A check that succeeds if a NavigationLink of a new or updated Entity is empty
 * and fails if it is not empty.
 */
public class CheckNavLinksEmpty implements ValidationCheck {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(CheckNavLinksEmpty.class);

    @ConfigurableField(editor = EditorList.class,
            label = "Target NavLinks", description = "The navLinks that must be (not) set.")
    @EditorList.EdOptsList(editor = EditorString.class)
    @EditorString.EdOptsString
    private List<String> targetNavLinks;

    @ConfigurableField(editor = EditorEnum.class,
            label = "Req. State", description = "The required state: empty or filled (non-empty)")
    @EditorEnum.EdOptsEnum(sourceType = EmptyState.class, dflt = "MUST_BE_EMPTY")
    private EmptyState requiredState = EmptyState.MUST_BE_EMPTY;

    @ConfigurableField(editor = EditorEnum.class,
            label = "Combine Type", description = "How to combine checks. Executed as fail-fast.")
    @EditorEnum.EdOptsEnum(sourceType = CheckMulti.Type.class, dflt = "AND")
    private CheckMulti.Type combineType;

    private EntityType entityType;
    private List<NavigationPropertyMain> targetNps;

    @Override
    public boolean check(JooqPersistenceManager pm, Entity contextEntity) {
        if (targetNps == null) {
            init(contextEntity);
        }
        final boolean emptyRequired = requiredState == EmptyState.MUST_BE_EMPTY;
        final boolean filledRequired = requiredState == EmptyState.MUST_BE_FILLED;
        if (combineType == CheckMulti.Type.AND) {
            for (NavigationPropertyMain targetNp : targetNps) {
                boolean valid = checkNavLink(targetNp, contextEntity, emptyRequired, filledRequired);
                if (!valid) {
                    return false;
                }
            }
            return true;
        } else {
            for (NavigationPropertyMain targetNp : targetNps) {
                boolean valid = checkNavLink(targetNp, contextEntity, emptyRequired, filledRequired);
                if (valid) {
                    return true;
                }
            }
            return false;
        }
    }

    private boolean checkNavLink(NavigationPropertyMain targetNp, Entity contextEntity, final boolean emptyRequired, final boolean filledRequired) {
        boolean valid;
        if (targetNp instanceof NavigationPropertyEntity targetNpEntity) {
            final Entity targetEntity = contextEntity.getProperty(targetNpEntity);
            final boolean empty = targetEntity == null;
            valid = (emptyRequired && empty) || (filledRequired && !empty);
            LOGGER.debug("  Check on {}.{}: {}", entityType, targetNp, valid);
        } else if (targetNp instanceof NavigationPropertyEntitySet targetNpEntitySet) {
            EntitySet targetEntities = contextEntity.getProperty(targetNpEntitySet);
            final boolean empty = targetEntities == null || targetEntities.isEmpty();
            valid = (emptyRequired && empty) || (filledRequired && !empty);
            LOGGER.debug("  Check on {}.{}: {}", entityType, targetNp, valid);
        } else {
            valid = false;
        }
        return valid;
    }

    private synchronized void init(Entity contextEntity) {
        if (targetNps != null) {
            return;
        }
        entityType = contextEntity.getEntityType();
        targetNps = new ArrayList<>();
        for (String targetNavLink : targetNavLinks) {
            NavigationPropertyMain targetNp = entityType.getNavigationProperty(targetNavLink);
            targetNps.add(targetNp);
        }
        LOGGER.info("Initialised check on {}.{}", entityType, targetNps);
    }

    public List<String> getTargetNavLink() {
        return targetNavLinks;
    }

    public void setTargetNavLink(List<String> targetNavLinks) {
        this.targetNavLinks = targetNavLinks;
    }

    public EmptyState getRequiredState() {
        return requiredState;
    }

    public void setRequiredState(EmptyState requiredState) {
        this.requiredState = requiredState;
    }

    @Override
    public String toString() {
        return "CheckNavLinkEmpty: " + targetNavLinks;
    }

}
