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
package de.fraunhofer.iosb.ilt.sta.model.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.fraunhofer.iosb.ilt.sta.path.EntitySetPathElement;
import de.fraunhofer.iosb.ilt.sta.path.EntityType;
import de.fraunhofer.iosb.ilt.sta.path.Property;
import de.fraunhofer.iosb.ilt.sta.util.IncompleteEntityException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;
import org.slf4j.LoggerFactory;

/**
 * Abstract base class of all entities
 *
 * @author jab
 */
public abstract class AbstractEntity implements Entity {

    /**
     * The logger for this class.
     */
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(AbstractEntity.class);

    public AbstractEntity() {
    }

    public AbstractEntity(Id id, String selfLink, String navigationLink) {
        this.id = id;
        this.selfLink = selfLink;
        this.navigationLink = navigationLink;
    }
    @JsonProperty("@iot.id")
    protected Id id;

    @JsonProperty("@iot.selfLink")
    protected String selfLink;

    @JsonIgnore
    protected String navigationLink;

    @JsonIgnore
    private boolean exportObject = true;

    @Override
    public Id getId() {
        return id;
    }

    @Override
    public String getSelfLink() {
        return selfLink;
    }

    /**
     * @param id the id to set
     */
    @Override
    public void setId(Id id) {
        this.id = id;
    }

    /**
     * @param selfLink the selfLink to set
     */
    @Override
    public void setSelfLink(String selfLink) {
        this.selfLink = selfLink;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 89 * hash + Objects.hashCode(this.id);
        hash = 89 * hash + Objects.hashCode(this.selfLink);
        hash = 89 * hash + Objects.hashCode(this.navigationLink);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final AbstractEntity other = (AbstractEntity) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        if (!Objects.equals(this.selfLink, other.selfLink)) {
            return false;
        }
        if (!Objects.equals(this.navigationLink, other.navigationLink)) {
            return false;
        }
        return true;
    }

    /**
     * @return the navigationLink
     */
    @Override
    public String getNavigationLink() {
        return navigationLink;
    }

    /**
     * @param navigationLink the navigationLink to set
     */
    @Override
    public void setNavigationLink(String navigationLink) {
        this.navigationLink = navigationLink;
    }

    @Override
    public Object getProperty(Property property) {
        String methodName = property.getGetterName();
        try {
            Method getMethod = this.getClass().getMethod(methodName, (Class<?>[]) null);
            return getMethod.invoke(this, (Object[]) null);
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            LOGGER.error("Failed to find or execute method " + methodName, ex);
            return null;
        }
    }

    @Override
    public void setProperty(Property property, Object value) {
        String methodName = property.getSetterName();
        try {
            for (Method m : this.getClass().getMethods()) {
                if (m.getParameterCount() == 1 && methodName.equals(m.getName())) {
                    try {
                        m.invoke(this, value);
                        return;
                    } catch (SecurityException | IllegalAccessException | InvocationTargetException | IllegalArgumentException e) {
                        LOGGER.trace("Wrong setter method.");
                    }
                }
            }
        } catch (SecurityException ex) {
            LOGGER.error("Failed to find or execute method " + methodName, ex);
        }
    }

    @Override
    public boolean isExportObject() {
        return exportObject;
    }

    @Override
    public void setExportObject(boolean exportObject) {
        this.exportObject = exportObject;
    }

    @Override
    public void unsetProperty(Property property) {
        String methodName = property.getSetterName();
        try {
            Method[] methods = this.getClass().getMethods();
            for (Method method : methods) {
                if (method.getName().equalsIgnoreCase(methodName) && method.getParameterCount() == 1) {
                    method.invoke(this, new Object[]{null});
                    return;
                }
            }
            LOGGER.error("Failed to find method {}.", methodName);
        } catch (SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            LOGGER.error("Failed to find or execute method " + methodName, ex);
        }
    }

    @Override
    public void complete(EntitySetPathElement containingSet) throws IncompleteEntityException {
        EntityType type = containingSet.getEntityType();
        if (type != getEntityType()) {
            throw new IllegalStateException("Set of type " + type + " can not contain a " + getEntityType());
        }
        complete();
    }
}
