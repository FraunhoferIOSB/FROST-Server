/*
 * Copyright (C) 2020 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 */
package de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils;

import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.StaMainTable;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyCustomSelect;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.property.Property;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jooq.Field;
import org.jooq.Record;

/**
 *
 * @author hylke
 * @param <J> The type of the ID fields.
 * @param <T> The table type this registry has fields for.
 * @param <E> The entity type for which the table holds data.
 */
public class PropertyFieldRegistry<J extends Comparable, E extends Entity<E>, T extends StaMainTable<J, E, T>> {

    private final T table;
    /**
     * The Fields that are allowed be appear in select statements.
     */
    private final Map<Property, PropertyFields<T, E>> epMapSelect;
    /**
     * The Fields that are allowed in where and orderby statements.
     */
    private final Map<Property, Map<String, ExpressionFactory<T>>> epMapAll;
    /**
     * All select-able fields, by class.
     */
    private final List<PropertyFields<T, E>> allSelectPropertyFields;

    public static interface ExpressionFactory<T> {

        public Field get(T table);
    }

    public static interface PropertySetter<T, E> {

        public void setOn(T table, Record tuple, E entity, DataSize dataSize);
    }

    public static class PropertyFields<T, E> {

        public final Property property;
        public final Map<String, ExpressionFactory<T>> fields = new LinkedHashMap<>();
        public final PropertySetter<T, E> setter;

        public PropertyFields(Property property, PropertySetter<T, E> setter) {
            this.property = property;
            this.setter = setter;
        }

        public PropertyFields<T, E> addField(String name, ExpressionFactory<T> field) {
            String key = name;
            if (key == null) {
                key = Integer.toString(fields.size());
            }
            fields.put(key, field);
            return this;
        }
    }

    public static class PropertyFactoryCombo<T> {

        public final Property property;
        public final ExpressionFactory<T> factory;

        public PropertyFactoryCombo(Property property, ExpressionFactory<T> factory) {
            this.property = property;
            this.factory = factory;
        }

    }

    /**
     * A NameFactoryPair for easier passing of a name and a factory.
     *
     * @param <T>
     */
    public static class NFP<T> {

        public final String name;
        public final ExpressionFactory<T> factory;

        public NFP(String name, ExpressionFactory<T> factory) {
            this.name = name;
            this.factory = factory;
        }
    }

    public PropertyFieldRegistry(T table) {
        this.table = table;
        this.epMapSelect = new HashMap<>();
        this.epMapAll = new HashMap<>();
        this.allSelectPropertyFields = new ArrayList<>();

    }

    public PropertyFieldRegistry(T table, PropertyFieldRegistry<J, E, T> copyFrom) {
        this.table = table;
        this.epMapSelect = copyFrom.epMapSelect;
        this.epMapAll = copyFrom.epMapAll;
        this.allSelectPropertyFields = copyFrom.allSelectPropertyFields;
    }

    /**
     * Get the Fields for the given class, that are allowed to be used in the
     * select clause of a query.
     *
     * @param target The list to add to. If null a new list will be created.
     * @return The target list, or a new list if target was null.
     */
    public Collection<PropertyFields<T, E>> getSelectFields(Collection<PropertyFields<T, E>> target) {
        Collection<PropertyFields<T, E>> result = target;
        if (result == null) {
            result = new ArrayList<>();
        }
        result.addAll(allSelectPropertyFields);
        return result;
    }

    /**
     * Get a list of Fields for the given property and table. Add it to the
     * given list, or a new list.
     *
     * @param property The property to get expressions for.
     * @return The target list, or a new list if target was null.
     */
    public PropertyFields<T, E> getSelectFieldsForProperty(Property property) {
        if (property instanceof EntityPropertyCustomSelect) {
            final EntityPropertyCustomSelect epCustomSelect = (EntityPropertyCustomSelect) property;
            return table.handleEntityPropertyCustomSelect(epCustomSelect);
        } else {
            return epMapSelect.get(property);
        }
    }

    /**
     * Get a Map of expressions for the given property and table. Add it to the
     * given Map, or a new Map.
     *
     * @param property The property to get expressions for.
     * @param target The Map to add to. If null a new Map will be created.
     * @return The target Map, or a new Map if target was null.
     */
    public Map<String, Field> getAllFieldsForProperty(EntityPropertyMain property, Map<String, Field> target) {
        Map<String, ExpressionFactory<T>> coreMap = epMapAll.get(property);
        if (coreMap == null) {
            throw new IllegalArgumentException("No property called " + property.toString() + " for " + table.getClass());
        }
        Map<String, Field> result = target;
        if (result == null) {
            result = new LinkedHashMap<>();
        }
        for (Map.Entry<String, ExpressionFactory<T>> es : coreMap.entrySet()) {
            result.put(es.getKey(), es.getValue().get(table));
        }
        return result;
    }

    /**
     * Get the set of expressions for the given set of selected properties.
     *
     * @param selectedProperties The set of properties to get the expressions
     * of.
     * @return The set of expressions.
     */
    public Set<PropertyFields<T, E>> getFieldsForProperties(Set<Property> selectedProperties) {
        Set<PropertyFields<T, E>> exprSet = new LinkedHashSet<>();
        if (selectedProperties.isEmpty()) {
            getSelectFields(exprSet);
        } else {
            for (Property property : selectedProperties) {
                final PropertyFields<T, E> selectFieldsForProperty = getSelectFieldsForProperty(property);
                if (selectFieldsForProperty != null) {
                    exprSet.add(selectFieldsForProperty);
                }
            }
        }
        return exprSet;
    }

    /**
     * Add an unnamed entry to the Field registry.
     *
     * @param property The property that this field supplies data for.
     * @param factory The factory to use to generate the Field instance.
     */
    public void addEntry(Property property, ExpressionFactory<T> factory, PropertySetter<T, E> ps) {
        PropertyFields<T, E> pf = new PropertyFields(property, ps);
        pf.addField(null, factory);
        epMapSelect.put(property, pf);
        allSelectPropertyFields.add(pf);
        addEntry(epMapAll, property, null, factory);
    }

    /**
     * Add an entry to the Field registry.
     *
     * @param property The property that this field supplies data for.
     * @param ps The PropertySetter used to set the property from a database
     * tuple.
     * @param factories The factories to use to generate the Field instance.
     */
    public void addEntry(Property property, PropertySetter<T, E> ps, NFP<T>... factories) {
        PropertyFields<T, E> pf = new PropertyFields(property, ps);
        for (NFP<T> nfp : factories) {
            pf.addField(nfp.name, nfp.factory);
            addEntry(epMapAll, property, nfp.name, nfp.factory);
        }
        epMapSelect.put(property, pf);
        allSelectPropertyFields.add(pf);
    }

    /**
     * Add an entry to the Field registry, but do not register it to the entity.
     * This means the field is never used in "select" clauses.
     *
     * @param property The property that this field supplies data for.
     * @param name The name to use for this field. (j for json, s for string, g
     * for geometry)
     * @param factory The factory to use to generate the Field instance.
     */
    public void addEntryNoSelect(Property property, String name, ExpressionFactory<T> factory) {
        addEntry(epMapAll, property, name, factory);
    }

    /**
     * Add an entry to the Field registry, but do not register it to the entity.
     * This means the field is never used in "select" clauses.
     *
     * @param property The property that this field supplies data for.
     * @param factory The factory to use to generate the Field instance.
     */
    public void addEntryNoSelect(Property property, ExpressionFactory<T> factory) {
        addEntry(epMapAll, property, null, factory);
    }

    private void addEntry(Map<Property, Map<String, ExpressionFactory<T>>> map, Property property, String name, ExpressionFactory<T> factory) {
        Map<String, ExpressionFactory<T>> coreMap = map.computeIfAbsent(
                property,
                k -> new LinkedHashMap<>()
        );
        String key = name;
        if (key == null) {
            key = Integer.toString(coreMap.size());
        }
        coreMap.put(key, factory);
    }
}
