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
import de.fraunhofer.iosb.ilt.configurable.editor.EditorSubclass;
import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.JooqPersistenceManager;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.factories.HookPreInsert;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.StaMainTable;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.ForbiddenException;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.UnauthorizedException;
import de.fraunhofer.iosb.ilt.frostserver.util.user.PrincipalExtended;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hylke
 */
public class ValidatorCUD implements HookValidator {

    private static final Logger LOGGER = LoggerFactory.getLogger(ValidatorCUD.class);
    private static final String OPERATION_NOT_ALLOWED = "Operation not allowed.";

    @ConfigurableField(editor = EditorSubclass.class,
            label = "Early Insert Check", description = "The check to validate if the user can insert. Runs before required relations are validated.")
    @EditorSubclass.EdOptsSubclass(iface = ValidationCheck.class, merge = true, nameField = "@class", shortenClassNames = true)
    private ValidationCheck checkInsertPreRel;

    @ConfigurableField(editor = EditorSubclass.class,
            label = "Insert Check", description = "The check to validate if the user can insert. Runs after required relations are validated.")
    @EditorSubclass.EdOptsSubclass(iface = ValidationCheck.class, merge = true, nameField = "@class", shortenClassNames = true)
    private ValidationCheck checkInsert;

    @ConfigurableField(editor = EditorSubclass.class,
            label = "Update Check", description = "The check to validate if the user can insert.")
    @EditorSubclass.EdOptsSubclass(iface = ValidationCheck.class, merge = true, nameField = "@class", shortenClassNames = true)
    private ValidationCheck checkUpdate;

    @ConfigurableField(editor = EditorSubclass.class,
            label = "Delete Check", description = "The check to validate if the user can insert.")
    @EditorSubclass.EdOptsSubclass(iface = ValidationCheck.class, merge = true, nameField = "@class", shortenClassNames = true)
    private ValidationCheck checkDelete;

    @Override
    public void registerHooks(StaMainTable mainTable, JooqPersistenceManager ppm) {
        LOGGER.info("    Registering hooks for {}", mainTable.getName());
        final EntityType entityType = mainTable.getEntityType();
        if (getCheckInsertPreRel() != null) {
            LOGGER.info("    - insert pre relations: {}", getCheckInsertPreRel());
            mainTable.registerHookPreInsert(-10, (phase, pm, entity, insertFields) -> {
                if (PrincipalExtended.getLocalPrincipal().isAdmin()) {
                    return true;
                }
                if (phase == HookPreInsert.Phase.PRE_RELATIONS && !checkInsertPreRel.check(pm, entity)) {
                    throwUnautorizedOrForbidden(OPERATION_NOT_ALLOWED);
                }
                return true;
            });
        }
        if (getCheckInsert() != null) {
            LOGGER.info("    - insert: {}", getCheckInsert());
            mainTable.registerHookPreInsert(-10, (phase, pm, entity, insertFields) -> {
                if (PrincipalExtended.getLocalPrincipal().isAdmin()) {
                    return true;
                }
                if (phase == HookPreInsert.Phase.POST_RELATIONS && !checkInsert.check(pm, entity)) {
                    throwUnautorizedOrForbidden(OPERATION_NOT_ALLOWED);
                }
                return true;
            });
        }
        if (getCheckUpdate() != null) {
            LOGGER.info("    - update: {}", getCheckUpdate());
            mainTable.registerHookPreUpdate(-10, (pm, entity, id) -> {
                if (PrincipalExtended.getLocalPrincipal().isAdmin()) {
                    return;
                }
                if (!checkUpdate.check(pm, entity)) {
                    throwUnautorizedOrForbidden(OPERATION_NOT_ALLOWED);
                }
            });
        }
        if (getCheckDelete() != null) {
            LOGGER.info("    - delete: {}", getCheckDelete());
            mainTable.registerHookPreDelete(-10, (pm, id) -> {
                if (PrincipalExtended.getLocalPrincipal().isAdmin()) {
                    return;
                }
                final Entity entity = pm.get(entityType, id);
                if (!checkDelete.check(pm, entity)) {
                    throwUnautorizedOrForbidden(OPERATION_NOT_ALLOWED);
                }
            });
        }
    }

    public ValidationCheck getCheckInsertPreRel() {
        return checkInsertPreRel;
    }

    public ValidatorCUD setCheckInsertPreRel(ValidationCheck checkInsertPreRel) {
        this.checkInsertPreRel = checkInsertPreRel;
        return this;
    }

    public ValidationCheck getCheckInsert() {
        return checkInsert;
    }

    public ValidatorCUD setCheckInsert(ValidationCheck checkInsert) {
        this.checkInsert = checkInsert;
        return this;
    }

    public ValidationCheck getCheckUpdate() {
        return checkUpdate;
    }

    public ValidatorCUD setCheckUpdate(ValidationCheck checkUpdate) {
        this.checkUpdate = checkUpdate;
        return this;
    }

    public ValidationCheck getCheckDelete() {
        return checkDelete;
    }

    public ValidatorCUD setCheckDelete(ValidationCheck checkDelete) {
        this.checkDelete = checkDelete;
        return this;
    }

    private void throwUnautorizedOrForbidden(String cause) {
        if (PrincipalExtended.ANONYMOUS_PRINCIPAL.equals(PrincipalExtended.getLocalPrincipal())) {
            throw new UnauthorizedException(cause);
        } else {
            throw new ForbiddenException(cause);
        }
    }
}
