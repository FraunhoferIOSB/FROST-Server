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

/**
 *
 * @author hylke
 * @param <J> The type of the ID fields.
 * @param <T> The table type this registry has fields for.
 */
public class PropertyFieldRegistry<J extends Comparable, T extends StaMainTable<J, T>> {

    private final T table;
    /**
     * The Fields that are allowed be appear in select statements.
     */
    private final Map<Property, Map<String, ExpressionFactory<T>>> epMapSelect;
    /**
     * The Fields that are allowed in where and orderby statements.
     */
    private final Map<Property, Map<String, ExpressionFactory<T>>> epMapAll;
    /**
     * All select-able fields, by class.
     */
    private final List<PropertyFactoryCombo<T>> allSelectPropertyFields;

    public static interface ExpressionFactory<T> {

        Field get(T table);
    }

    public static class PropertyFactoryCombo<T> {

        public final Property property;
        public final ExpressionFactory<T> factory;

        public PropertyFactoryCombo(Property property, ExpressionFactory<T> factory) {
            this.property = property;
            this.factory = factory;
        }

    }

    public PropertyFieldRegistry(T table) {
        this.table = table;
        this.epMapSelect = new HashMap<>();
        this.epMapAll = new HashMap<>();
        this.allSelectPropertyFields = new ArrayList<>();

    }

    public PropertyFieldRegistry(T table, PropertyFieldRegistry<J, T> copyFrom) {
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
    public Collection<SelectedProperty> getSelectFields(Collection<SelectedProperty> target) {
        Collection<SelectedProperty> result = target;
        if (result == null) {
            result = new ArrayList<>();
        }
        for (PropertyFactoryCombo pfc : allSelectPropertyFields) {
            result.add(new SelectedProperty(pfc.property, pfc.factory.get(table)));
        }
        return result;
    }

    /**
     * Get a list of Fields for the given property and table. Add it to the
     * given list, or a new list.
     *
     * @param property The property to get expressions for.
     * @param target The list to add to. If null a new list will be created.
     * @return The target list, or a new list if target was null.
     */
    public Collection<SelectedProperty> getSelectFieldsForProperty(Property property, Collection<SelectedProperty> target) {
        Collection<SelectedProperty> result = target;
        if (result == null) {
            result = new ArrayList<>();
        }
        if (property instanceof EntityPropertyCustomSelect) {
            // TODO: implement
        } else {
            Map<String, ExpressionFactory<T>> coreMap = epMapSelect.get(property);
            for (Map.Entry<String, ExpressionFactory<T>> es : coreMap.entrySet()) {
                result.add(new SelectedProperty(property, es.getValue().get(table)));
            }
        }
        return result;
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
    public Set<SelectedProperty> getFieldsForProperties(Set<Property> selectedProperties) {
        Set<SelectedProperty> exprSet = new LinkedHashSet<>();
        if (selectedProperties.isEmpty()) {
            getSelectFields(exprSet);
        } else {
            for (Property property : selectedProperties) {
                getSelectFieldsForProperty(property, exprSet);
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
    public void addEntry(Property property, ExpressionFactory<T> factory) {
        addEntry(epMapSelect, property, null, factory);
        addEntry(epMapAll, property, null, factory);
        addToAll(property, factory);
    }

    /**
     * Add an entry to the Field registry.
     *
     * @param property The property that this field supplies data for.
     * @param name The name to use for this field. (j for json, s for string, g
     * for geometry)
     * @param factory The factory to use to generate the Field instance.
     */
    public void addEntry(Property property, String name, ExpressionFactory<T> factory) {
        addEntry(epMapSelect, property, name, factory);
        addEntry(epMapAll, property, name, factory);
        addEntry(epMapSelect, property, name, factory);
        addToAll(property, factory);
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

    private void addToAll(Property property, ExpressionFactory<T> factory) {
        PropertyFactoryCombo pfc = new PropertyFactoryCombo(property, factory);
        allSelectPropertyFields.add(pfc);
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
