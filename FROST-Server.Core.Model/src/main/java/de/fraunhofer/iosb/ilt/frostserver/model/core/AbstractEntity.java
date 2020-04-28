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
package de.fraunhofer.iosb.ilt.frostserver.model.core;

import de.fraunhofer.iosb.ilt.frostserver.model.EntityChangedMessage;
import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.path.PathElementEntitySet;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityProperty;
import de.fraunhofer.iosb.ilt.frostserver.property.Property;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.IncompleteEntityException;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Abstract base class of all entities
 *
 * @author jab
 * @author scf
 * @param <T> The exact type of the entity.
 */
public abstract class AbstractEntity<T extends AbstractEntity<T>> implements Entity<T> {

    private Id id;

    private String selfLink;

    private String navigationLink;

    private boolean exportObject = true;

    private Set<String> selectedPropertyNames;

    /**
     * Flag indicating the Id was set by the user.
     */
    private boolean setId;
    /**
     * Flag indicating the selfLink was set by the user.
     */
    private boolean setSelfLink;

    public AbstractEntity(Id id) {
        setId(id);
    }

    @Override
    public void setEntityPropertiesSet(boolean set, boolean entityPropertiesOnly) {
        setSets(set);
    }

    private void setSets(boolean set) {
        setId = set;
        setSelfLink = set;
    }

    @Override
    public void setEntityPropertiesSet(T comparedTo, EntityChangedMessage message) {
        setSets(false);
        if (!Objects.equals(id, comparedTo.getId())) {
            setId = true;
            message.addEpField(EntityProperty.ID);
        }
        if (!Objects.equals(selfLink, comparedTo.getSelfLink())) {
            setSelfLink = true;
            message.addEpField(EntityProperty.SELFLINK);
        }
    }

    @Override
    public Id getId() {
        return id;
    }

    /**
     * @param id the id to set
     * @return this
     */
    @Override
    public final T setId(Id id) {
        this.id = id;
        setId = true;
        return getThis();
    }

    /**
     * Flag indicating the Id was set by the user.
     *
     * @return Flag indicating the Id was set by the user.
     */
    public boolean isSetId() {
        return setId;
    }

    @Override
    public String getSelfLink() {
        return selfLink;
    }

    /**
     * @param selfLink the selfLink to set
     * @return this
     */
    @Override
    public T setSelfLink(String selfLink) {
        this.selfLink = selfLink;
        setSelfLink = true;
        return getThis();
    }

    /**
     * Flag indicating the selfLink was set by the user.
     *
     * @return Flag indicating the selfLink was set by the user.
     */
    public boolean isSetSelfLink() {
        return setSelfLink;
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
     * @return
     */
    @Override
    public T setNavigationLink(String navigationLink) {
        this.navigationLink = navigationLink;
        return getThis();
    }

    @Override
    public Set<String> getSelectedPropertyNames() {
        return selectedPropertyNames;
    }

    @Override
    public void setSelectedPropertyNames(Set<String> selectedProperties) {
        this.selectedPropertyNames = selectedProperties;
    }

    @Override
    public void setSelectedProperties(Set<Property> selectedProperties) {
        AbstractEntity.this.setSelectedPropertyNames(
                selectedProperties
                        .stream()
                        .map(Property::getJsonName)
                        .collect(Collectors.toSet())
        );
    }

    @Override
    public Object getProperty(Property property) {
        return property.getFrom(this);
    }

    @Override
    public void setProperty(Property property, Object value) {
        property.setOn(this, value);
    }

    @Override
    public void unsetProperty(Property property) {
        property.setOn(this, null);
    }

    @Override
    public boolean isSetProperty(Property property) {
        return property.isSetOn(this);
    }

    @Override
    public boolean isExportObject() {
        return exportObject;
    }

    @Override
    public T setExportObject(boolean exportObject) {
        this.exportObject = exportObject;
        return getThis();
    }

    @Override
    public void complete(PathElementEntitySet containingSet) throws IncompleteEntityException {
        EntityType type = containingSet.getEntityType();
        if (type != getEntityType()) {
            throw new IllegalStateException("Set of type " + type + " can not contain a " + getEntityType());
        }
        complete();
    }

    protected abstract T getThis();

    @Override
    public int hashCode() {
        return Objects.hash(id, selfLink, navigationLink);
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
        final AbstractEntity<T> other = (AbstractEntity<T>) obj;
        return Objects.equals(this.id, other.id)
                && Objects.equals(this.selfLink, other.selfLink)
                && Objects.equals(this.navigationLink, other.navigationLink);
    }

}
