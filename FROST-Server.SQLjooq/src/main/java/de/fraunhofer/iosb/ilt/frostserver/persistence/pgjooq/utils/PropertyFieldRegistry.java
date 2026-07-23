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
package de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils;

import static de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.Utils.getFieldOrNull;
import static de.fraunhofer.iosb.ilt.frostserver.util.Constants.NOT_IMPLEMENTED_MULTI_VALUE_PK;

import de.fraunhofer.iosb.ilt.frostserver.model.ComplexValue;
import de.fraunhofer.iosb.ilt.frostserver.model.DefaultEntity;
import de.fraunhofer.iosb.ilt.frostserver.model.EntityChangedMessage;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.model.core.PkSingle;
import de.fraunhofer.iosb.ilt.frostserver.model.core.PkValue;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.TimeInstant;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.TimeInterval;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.TimeValue;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.bindings.JsonBinding;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.bindings.JsonValue;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.factories.EntityFactories;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.fieldwrapper.JsonFieldFactory;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.fieldwrapper.JsonFieldFactory.JsonFieldWrapper;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.fieldwrapper.StaTimeIntervalWrapper;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.StaMainTable;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityProperty;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyCustomSelect;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain.NavigationPropertyEntity;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain.NavigationPropertyEntitySet;
import de.fraunhofer.iosb.ilt.frostserver.property.Property;
import de.fraunhofer.iosb.ilt.frostserver.property.StandardProperties;
import de.fraunhofer.iosb.ilt.frostserver.property.type.PropertyType;
import de.fraunhofer.iosb.ilt.frostserver.property.type.TypeComplex;
import de.fraunhofer.iosb.ilt.frostserver.util.StringHelper;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.time4j.Moment;
import org.apache.commons.lang3.NotImplementedException;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.core.TreeNode;

/**
 * The registry with methods for accessing and manipulating the properties and
 * fields of a single table.
 *
 * @param <T> The table type this registry has fields for.
 */
public class PropertyFieldRegistry<T extends StaMainTable<T>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(PropertyFieldRegistry.class.getName());

    /**
     * The table that this PropertyFieldRegistry handles.
     */
    private final T table;

    /**
     * The Fields that are allowed be appear in select statements.
     */
    private final Map<Property, PropertyFields<T>> propFieldsMap;

    /**
     * All select-able fields.
     */
    private final List<PropertyFields<T>> propFieldsList;

    public PropertyFieldRegistry(T table) {
        this.table = table;
        this.propFieldsMap = new HashMap<>();
        this.propFieldsList = new ArrayList<>();
    }

    public PropertyFieldRegistry(T table, PropertyFieldRegistry<T> copyFrom) {
        this.table = table;
        this.propFieldsMap = copyFrom.propFieldsMap;
        this.propFieldsList = copyFrom.propFieldsList;
    }

    /**
     * Get the PropertyFields of this registry.
     *
     * @param <C> The type of collection given as a target.
     * @param target The list to add to. If null a new ArrayList will be
     * created.
     * @return The target list, or a new list if target was null.
     */
    public <C extends Collection<PropertyFields<T>>> C getPropertyFields(C target) {
        C result = target;
        if (result == null) {
            result = (C) new ArrayList();
        }
        result.addAll(propFieldsList);
        return result;
    }

    /**
     * Get a list of PropertyFields for the given property. If the property is a
     * deep-select property, a trimmed copy of the real PropertyField tree is
     * returned, containing only the relevant path through the tree.
     *
     * @param property The property to get expressions for.
     * @return A PropertyFields that matches the requested property.
     */
    public PropertyFields<T> getPropertyFieldsForProperty(Property property) {
        if (property instanceof EntityPropertyCustomSelect epcs) {
            return getPropertyFieldsForCustomProperty(epcs);
        } else {
            return propFieldsMap.get(property);
        }
    }

    private static class CustomPropsRecurseState<T extends StaMainTable<T>> {

        public CustomPropsRecurseState(EntityPropertyCustomSelect epcs) {
            this.epcs = epcs;
        }

        public final EntityPropertyCustomSelect epcs;
        public PropertyFields<T> lastPropFields;
        public PropertyFields<T> lastPropCopy;
        public EntityPropertyMain lastProp;
        public PropertyType lastType;

    }

    private PropertyFields<T> getPropertyFieldsForCustomProperty(EntityPropertyCustomSelect epcs) throws IllegalArgumentException {
        final EntityPropertyMain parentProp = epcs.getMainProperty();
        final PropertyType parentType = parentProp.getType();

        final var rs = new CustomPropsRecurseState<T>(epcs);
        rs.lastPropFields = propFieldsMap.get(parentProp);
        rs.lastPropCopy = rs.lastPropFields.emptyCopy();
        rs.lastProp = parentProp;
        rs.lastType = parentType;

        final PropertyFields<T> mainPropCopy = rs.lastPropCopy;
        final List<String> subPath = rs.epcs.getSubPath();

        for (int idx = 0; idx < subPath.size(); idx++) {
            if (rs.lastType instanceof TypeComplex tc) {
                if (handleComplexType(tc, subPath, idx, rs)) {
                    return handleEntityPropertyCustomSelect(rs.epcs, rs.lastPropFields, subPath, idx);
                }
            } else {
                return handleNonComplexType(rs, subPath, idx, parentType);
            }
        }
        rs.lastPropCopy.fieldsAll.putAll(rs.lastPropFields.fieldsAll);
        rs.lastPropCopy.fieldsSelect.putAll(rs.lastPropFields.fieldsSelect);
        rs.lastPropCopy.subFields.putAll(rs.lastPropFields.subFields);
        return mainPropCopy;
    }

    private PropertyFields<T> handleNonComplexType(final CustomPropsRecurseState<T> rs, final List<String> subPath, int idx, final PropertyType parentType) {
        final boolean hasCustomProperties = rs.lastProp.hasCustomProperties();
        if (hasCustomProperties || rs.lastPropFields.jsonType) {
            if (hasCustomProperties != rs.lastPropFields.jsonType) {
                LOGGER.warn("Config diference between Property.hasCustomProperties ({}) and PropertyField.jsonType ({})", hasCustomProperties, rs.lastPropFields.jsonType);
            }
            // Not a complex type, but can be queried.
            return handleEntityPropertyCustomSelect(rs.epcs, rs.lastPropFields, subPath, idx);
        } else {
            LOGGER.error("Not a complex property: {} {}", rs.epcs, parentType);
            return null;
        }
    }

    private boolean handleComplexType(TypeComplex tc, List<String> subPath, int idx, CustomPropsRecurseState<T> rs) throws IllegalArgumentException {
        boolean openType = tc.isOpenType();
        String pathPart = subPath.get(idx);
        EntityPropertyMain subProp = tc.getEntityProperty(pathPart);
        if (subProp == null) {
            // We have reached a custom property.
            if (!openType) {
                throw new IllegalArgumentException("No path: at " + pathPart + " of " + rs.epcs);
            }
            return true;
        } else {
            // Nested properties
            PropertyFields<T> subPropFields = rs.lastPropFields.subFields.get(subProp);
            PropertyFields<T> subPropCopy = subPropFields.emptyCopy();
            rs.lastPropCopy.addSubProperty(subPropCopy);

            rs.lastProp = subProp;
            rs.lastType = subProp.getType();
            rs.lastPropFields = subPropFields;
            rs.lastPropCopy = subPropCopy;
        }
        return false;
    }

    private PropertyFields<T> handleEntityPropertyCustomSelect(EntityPropertyCustomSelect epcs, PropertyFields<T> propFields, List<String> subPath, int idx) {
        FieldFetcher<T> factory = propFields.fieldsAll.get("j");
        if (factory == null) {
            factory = propFields.fieldsAll.values().iterator().next();
        }
        final Field mainField = factory.get(table);
        final JsonFieldFactory.JsonFieldWrapper jsonFactory = jsonFieldFromPath(mainField, subPath, idx);
        return propertyFieldForJsonField(jsonFactory, epcs);
    }

    private PropertyFields<T> propertyFieldForJsonField(JsonFieldWrapper jsonFactory, final EntityPropertyCustomSelect epcs) {
        final Field<Object> deepField = jsonFactory.materialise().getJsonExpression();
        PropertyFields<T> pfs = new PropertyFieldsSimple<T>(
                epcs,
                new PropertyFieldRegistry.ConverterRecordDeflt<>(
                        (tbl, tuple, entity, dataSize) -> {
                            final JsonValue jsonValue = JsonBinding.getConverterInstance().from(tuple.get(deepField));
                            dataSize.increase(jsonValue.getStringLength());
                            Object value = jsonValue.getValue();
                            epcs.setOn(entity, value);
                        }, null, null));
        pfs.addField("1", t -> deepField);
        return pfs;
    }

    /**
     * Get a Map of expressions for the given property and table. Add it to the
     * given Map, or a new Map.
     *
     * @param property The property to get expressions for.
     * @param target The Map to add to. If null a new Map will be created.
     * @return The target Map, or a new Map if target was null.
     */
    public Map<String, Field> resolveAllFieldsForProperty(EntityPropertyMain property, Map<String, Field> target) {
        PropertyFields<T> propFields = propFieldsMap.get(property);
        if (propFields == null) {
            throw new IllegalArgumentException("No property called " + property.toString() + " for " + table.getClass());
        }
        return PropertyFieldRegistry.this.resolveAllFieldsForProperty(propFields, target);
    }

    public Map<String, Field> resolveAllFieldsForProperty(PropertyFields<T> propFields, Map<String, Field> target) {
        Map<String, Field> result = target;
        if (result == null) {
            result = new LinkedHashMap<>();
        }
        for (Map.Entry<String, FieldFetcher<T>> es : propFields.fieldsAll.entrySet()) {
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
    public Set<PropertyFields<T>> getFieldsForProperties(Set<Property> selectedProperties) {
        Set<PropertyFields<T>> exprSet = new LinkedHashSet<>();
        if (selectedProperties.isEmpty()) {
            getPropertyFields(exprSet);
        } else {
            for (Property property : selectedProperties) {
                final PropertyFields<T> selectFieldsForProperty = getPropertyFieldsForProperty(property);
                if (selectFieldsForProperty != null) {
                    exprSet.add(selectFieldsForProperty);
                }
            }
        }
        return exprSet;
    }

    public void addEntry(NavigationPropertyMain property, FieldFetcher<T> factory) {
        switch (property) {
            case NavigationPropertyEntity npe ->
                addEntry(npe, factory);

            case NavigationPropertyEntitySet nps ->
                addEntry(nps, factory);

            default ->
                throw new IllegalArgumentException("Unknown NavigationProperty type: " + property);
        }
    }

    public void addEntry(NavigationPropertyEntity property, FieldFetcher<T> factory) {
        PropertyFields<T> pf = new PropertyFieldsSimple<>(property, new ConverterEntity<>(property, factory));
        pf.addField(null, factory);
        propFieldsMap.put(property, pf);
        propFieldsList.add(pf);
    }

    public void addEntry(NavigationPropertyEntity property, FieldFetcher<T> factory, ConverterRecord<T> ps) {
        PropertyFields<T> pf = new PropertyFieldsSimple<>(property, ps);
        pf.addField(null, factory);
        propFieldsMap.put(property, pf);
        propFieldsList.add(pf);
    }

    public void addEntry(NavigationPropertyEntitySet property, FieldFetcher<T> factory) {
        PropertyFields<T> pf = new PropertyFieldsSimple<T>(property, new ConverterEntitySet<>());
        pf.addField(null, factory);
        propFieldsMap.put(property, pf);
        propFieldsList.add(pf);
    }

    public PropertyFields<T> addEntry(PropertyFields<T> pf) {
        propFieldsMap.put(pf.property, pf);
        propFieldsList.add(pf);
        return pf;
    }

    public PropertyFields<T> addEntry(Property property, ConverterRecord<T> converter, FieldFetcher<T> factory) {
        return addEntry(property, false, converter, new NFP<>(factory));
    }

    public PropertyFields<T> addEntry(Property property, ConverterRecord<T> converter, NFP<T>... factories) {
        return addEntry(property, false, converter, factories);
    }

    public PropertyFields<T> addEntry(Property property, boolean isJson, ConverterRecord<T> converter, NFP<T>... factories) {
        PropertyFields<T> pf;
        if (property.getType() instanceof TypeComplex) {
            pf = createEntryComplex(property, isJson, converter, factories);
        } else {
            pf = createEntrySimple(property, isJson, converter, factories);
        }
        return addEntry(pf);
    }

    public void addEntryId(FieldFetcher<T> factory) {
        final EntityPropertyMain keyProperty = table.getEntityType().getPrimaryKey().getKeyProperty(0);
        final var converterId = new ConverterSimple<>(keyProperty, factory, true, false);
        addEntry(keyProperty, converterId, new NFP<>("", factory));
        final ConverterSimple<T> converterSelfLink = new ConverterSimple<>(keyProperty, factory, false, false);
        addEntry(StandardProperties.EP_SELFLINK, converterSelfLink, new NFP<>("", factory));
    }

    public PropertyFields<T> addEntrySimple(EntityProperty property, FieldFetcher<T> factory) {
        PropertyFields<T> pf = createEntrySimple(property, factory);
        return addEntry(pf);
    }

    public PropertyFields<T> addEntryString(EntityProperty<String> property, FieldFetcher<T> factory) {
        PropertyFields<T> pf = createEntryString(property, factory);
        return addEntry(pf);
    }

    public PropertyFields<T> addEntryNumeric(EntityProperty<BigDecimal> property, FieldFetcher<T> factory) {
        PropertyFields<T> pf = createEntryNumeric(property, factory);
        return addEntry(pf);
    }

    public PropertyFields<T> addEntryMap(EntityProperty<TreeNode> property, FieldFetcher<T> factory) {
        PropertyFields<T> pf = createEntryMap(property, factory);
        return addEntry(pf);
    }

    public PropertyFields<T> addEntryJson(EntityProperty<TreeNode> property, FieldFetcher<T> factory) {
        PropertyFields<T> pf = createEntryJson(property, factory);
        return addEntry(pf);
    }

    public PropertyFields<T> addEntryTimeInstant(EntityProperty property, FieldFetcher<T> factory) {
        PropertyFields<T> pf = createEntryTimeInstant(property, factory);
        return addEntry(pf);
    }

    public PropertyFields<T> addEntryTimeInterval(EntityProperty property, FieldFetcher<T> factoryStart, FieldFetcher<T> factoryEnd) {
        final var converter = new ConverterTimeInterval<>(property, factoryStart, factoryEnd);
        final var nfpStart = new NFP<>(StaTimeIntervalWrapper.KEY_TIME_INTERVAL_START, factoryStart);
        final var nfpEnd = new NFP<>(StaTimeIntervalWrapper.KEY_TIME_INTERVAL_END, factoryEnd);
        final var pf = createEntrySimple(property, false, converter, nfpStart, nfpEnd)
                .addSubProperty(createEntryTimeInstant(TimeInterval.EP_START_TIME, factoryStart))
                .addSubProperty(createEntryTimeInstant(TimeInterval.EP_END_TIME, factoryEnd));
        return addEntry(pf);
    }

    public PropertyFields<T> addEntryTimeValue(EntityProperty property, FieldFetcher<T> factoryStart, FieldFetcher<T> factoryEnd) {
        final var converter = new ConverterTimeValue<>(property, factoryStart, factoryEnd);
        final var nfpStart = new NFP<>(StaTimeIntervalWrapper.KEY_TIME_INTERVAL_START, factoryStart);
        final var nfpEnd = new NFP<>(StaTimeIntervalWrapper.KEY_TIME_INTERVAL_END, factoryEnd);
        final var pf = createEntrySimple(property, false, converter, nfpStart, nfpEnd)
                .addSubProperty(createEntryTimeInstant(TimeValue.EP_START_TIME, factoryStart))
                .addSubProperty(createEntryTimeInstant(TimeValue.EP_END_TIME, factoryEnd));
        return addEntry(pf);
    }

    public PropertyFields<T> createEntrySimple(EntityProperty property, FieldFetcher<T> factory) {
        PropertyFields<T> pf = new PropertyFieldsSimple<>(property, new ConverterSimple<>(property, factory));
        pf.addField(null, factory);
        return pf;
    }

    public PropertyFields<T> createEntrySimple(Property property, boolean isJson, ConverterRecord<T> converter, NFP<T>... factories) {
        PropertyFields<T> pf = new PropertyFieldsSimple<>(property, isJson, converter);
        for (NFP<T> nfp : factories) {
            pf.addField(nfp.name, nfp.factory);
        }
        return pf;
    }

    public PropertyFields<T> createEntryString(EntityProperty<String> property, FieldFetcher<T> factory) {
        PropertyFields<T> pf = new PropertyFieldsSimple<>(property, new ConverterString<>(property, factory));
        pf.addField(null, factory);
        return pf;
    }

    public PropertyFields<T> createEntryNumeric(EntityProperty<BigDecimal> property, FieldFetcher<T> factory) {
        PropertyFields<T> pf = new PropertyFieldsSimple<>(property, new ConverterSimple<>(property, factory));
        pf.addField(null, factory);
        return pf;
    }

    public PropertyFields<T> createEntryMap(EntityProperty<TreeNode> property, FieldFetcher<T> factory) {
        PropertyFields<T> pf = new PropertyFieldsSimple<>(property, true, new ConverterMap<>(property, factory));
        pf.addField(null, factory);
        return pf;
    }

    public PropertyFields<T> createEntryJson(EntityProperty<TreeNode> property, FieldFetcher<T> factory) {
        PropertyFields<T> pf = new PropertyFieldsSimple<>(property, true, new ConverterJson<>(property, factory));
        pf.addField(null, factory);
        return pf;
    }

    public PropertyFields<T> createEntryTimeInstant(EntityProperty<TimeInstant> property, FieldFetcher<T> factory) {
        PropertyFields<T> pf = new PropertyFieldsSimple<>(property, new ConverterTimeInstant<>(property, factory));
        pf.addField(null, factory);
        return pf;
    }

    public PropertyFields<T> createEntryComplex(Property property, boolean isJson, ConverterRecord<T> converter, NFP<T>... factories) {
        PropertyFields<T> pf = new PropertyFieldsComplex<>(property, isJson, converter);
        for (NFP<T> nfp : factories) {
            pf.addField(nfp);
        }
        return pf;
    }

    public static JsonFieldWrapper jsonFieldFromPath(final Field mainField, List<String> subPath, int startIdx) {
        JsonFieldWrapper jsonFactory = new JsonFieldWrapper(mainField);
        for (int idx = startIdx; idx < subPath.size(); idx++) {
            String pathItem = subPath.get(idx);
            jsonFactory.addToPath(pathItem);
        }
        return jsonFactory;
    }

    /**
     * Interface for fetching a field from a table.
     *
     * @param <U> The type of table.
     */
    public static interface FieldFetcher<U extends StaMainTable<U>> {

        public Field get(U table);
    }

    public abstract static class PropertyFields<U extends StaMainTable<U>> {

        public final Property property;
        public final boolean jsonType;
        public final Map<String, FieldFetcher<U>> fieldsAll = new LinkedHashMap<>();
        public final Map<String, FieldFetcher<U>> fieldsSelect = new LinkedHashMap<>();
        public final Map<EntityProperty, PropertyFields<U>> subFields = new LinkedHashMap<>();
        public final ConverterRecord<U> converter;

        protected PropertyFields(Property property, boolean jsonType, ConverterRecord<U> converter) {
            this.property = property;
            this.converter = converter;
            this.jsonType = jsonType;
        }

        public PropertyFields<U> getSubField(EntityProperty property) {
            return subFields.get(property);
        }

        public abstract void convert(U table, Record input, ComplexValue<?> entity, DataSize dataSize);

        public abstract void convert(U table, ComplexValue<?> entity, Map<Field, Object> insertFields);

        public abstract void convert(U table, ComplexValue<?> entity, Map<Field, Object> updateFields, EntityChangedMessage message);

        public PropertyFields<U> addField(NFP<U> nfp) {
            return addField(nfp.name, nfp.factory, nfp.canSelect);
        }

        public PropertyFields<U> addField(String name, FieldFetcher<U> field) {
            return addField(name, field, true);
        }

        public PropertyFields<U> addField(String name, FieldFetcher<U> field, boolean canSelect) {
            String key = name;
            if (StringHelper.isNullOrEmpty(key)) {
                key = Integer.toString(fieldsAll.size());
            }
            fieldsAll.put(key, field);
            if (canSelect) {
                fieldsSelect.put(key, field);
            }
            return this;
        }

        public PropertyFields<U> addSubProperty(PropertyFields<U> propFields) {
            if (propFields.property instanceof EntityProperty ep) {
                subFields.put(ep, propFields);
            } else {
                LOGGER.error("Adding non-EntityProperty {} as subField of {}", propFields.property, property);
            }
            return this;
        }

        public void getFieldsAllRecursive(Collection<FieldFetcher<U>> target) {
            target.addAll(fieldsAll.values());
            for (var subField : subFields.values()) {
                subField.getFieldsAllRecursive(target);
            }
        }

        public void getFieldsSelectRecursive(Collection<FieldFetcher<U>> target) {
            target.addAll(fieldsAll.values());
            for (var subField : subFields.values()) {
                subField.getFieldsSelectRecursive(target);
            }
        }

        public abstract PropertyFields<U> emptyCopy();

        @Override
        public String toString() {
            return property.getName();
        }

    }

    public static class PropertyFieldsSimple<U extends StaMainTable<U>> extends PropertyFields<U> {

        public PropertyFieldsSimple(Property property, ConverterRecord<U> converter) {
            super(property, false, converter);
        }

        public PropertyFieldsSimple(Property property, boolean jsonType, ConverterRecord<U> converter) {
            super(property, jsonType, converter);
        }

        @Override
        public void convert(U table, Record input, ComplexValue<?> entity, DataSize dataSize) {
            converter.convert(table, input, entity, dataSize);
        }

        @Override
        public void convert(U table, ComplexValue<?> entity, Map<Field, Object> insertFields) {
            converter.convert(table, entity, insertFields);
        }

        @Override
        public void convert(U table, ComplexValue<?> entity, Map<Field, Object> updateFields, EntityChangedMessage message) {
            converter.convert(table, entity, updateFields, message);
        }

        @Override
        public PropertyFieldsSimple<U> emptyCopy() {
            return new PropertyFieldsSimple<>(property, jsonType, converter);
        }

    }

    public static class PropertyFieldsComplex<U extends StaMainTable<U>> extends PropertyFields<U> {

        public PropertyFieldsComplex(Property property, ConverterRecord<U> converter) {
            super(property, false, converter);
        }

        public PropertyFieldsComplex(Property property, boolean jsonType, ConverterRecord<U> converter) {
            super(property, jsonType, converter);
        }

        @Override
        public void convert(U table, Record input, ComplexValue<?> entity, DataSize dataSize) {
            ComplexValue value = ((TypeComplex) property.getType()).instantiate();
            entity.setProperty(property, value);
            converter.convert(table, input, value, dataSize);
            for (PropertyFields<U> subField : subFields.values()) {
                subField.convert(table, input, value, dataSize);
            }
        }

        @Override
        public void convert(U table, ComplexValue<?> entity, Map<Field, Object> insertFields) {
            Object value = entity.getProperty(property);
            if (value instanceof ComplexValue cv) {
                converter.convert(table, cv, insertFields);
                for (PropertyFields<U> subField : subFields.values()) {
                    subField.convert(table, cv, insertFields);
                }
            }
        }

        @Override
        public void convert(U table, ComplexValue<?> entity, Map<Field, Object> updateFields, EntityChangedMessage message) {
            Object value = entity.getProperty(property);
            if (value instanceof ComplexValue cv) {
                converter.convert(table, cv, updateFields, message);
                EntityChangedMessage subMessage = new EntityChangedMessage();
                for (PropertyFields<U> subField : subFields.values()) {
                    subField.convert(table, cv, updateFields, subMessage);
                }
                if (!StringHelper.isNullOrEmpty(subMessage.getEpFields())) {
                    message.addField(property);
                }
            }
        }

        @Override
        public PropertyFields<U> emptyCopy() {
            return new PropertyFieldsComplex<>(property, jsonType, converter);
        }

    }

    /**
     * A NameFetcherPair for easier passing of a name and a factory.
     *
     * @param <U> the table type this NFP fetches from.
     */
    public static class NFP<U extends StaMainTable<U>> {

        public final String name;
        public final FieldFetcher<U> factory;
        public final boolean canSelect;

        public NFP(FieldFetcher<U> factory) {
            this("", factory, true);
        }

        public NFP(String name, FieldFetcher<U> factory) {
            this(name, factory, true);
        }

        public NFP(String name, FieldFetcher<U> factory, boolean canSelect) {
            this.name = name;
            this.factory = factory;
            this.canSelect = canSelect;
        }
    }

    /**
     * Convert the given Record, holding data from the given Table, into the
     * given Entity.
     *
     * @param <U> The table type.
     */
    public static interface ConverterRecordRead<U extends StaMainTable<U>> {

        /**
         * Convert the given Record, holding data from the given Table, into the
         * given Entity. If possible, the data size is added to the DataSize
         * object.
         *
         * @param table The table used to generate the Record.
         * @param input The Record to read the data from.
         * @param entity The entity to write the data to.
         * @param dataSize The DataSize to use to register the amount of data.
         */
        public void convert(U table, Record input, ComplexValue<?> entity, DataSize dataSize);
    }

    public static interface ConverterRecordInsert<U extends StaMainTable<U>> {

        public void convert(U table, ComplexValue<?> entity, Map<Field, Object> insertFields);
    }

    public static interface ConverterRecordUpdate<U extends StaMainTable<U>> {

        public void convert(U table, ComplexValue<?> entity, Map<Field, Object> updateFields, EntityChangedMessage message);
    }

    public static final ConverterRecordRead NULL_READ = (table, input, entity, dataSize) -> {
        // Does nothing
    };
    public static final ConverterRecordInsert NULL_INSERT = (table, entity, insertFields) -> {
        // Does nothing
    };
    public static final ConverterRecordUpdate NULL_UPDATE = (table, entity, updateFields, message) -> {
        // Does nothing
    };

    public static interface ConverterRecord<U extends StaMainTable<U>> extends ConverterRecordRead<U>, ConverterRecordInsert<U>, ConverterRecordUpdate<U> {
        // No own methods.
    }

    public static class ConverterRecordDeflt<U extends StaMainTable<U>> implements ConverterRecord<U> {

        private final ConverterRecordRead<U> read;
        private final ConverterRecordInsert<U> insert;
        private final ConverterRecordUpdate<U> update;

        public ConverterRecordDeflt() {
            this(null, null, null);
        }

        public ConverterRecordDeflt(ConverterRecordRead<U> read) {
            this(read, null, null);
        }

        public ConverterRecordDeflt(ConverterRecordRead<U> read, ConverterRecordInsert<U> insert, ConverterRecordUpdate<U> update) {
            this.read = (read == null) ? NULL_READ : read;
            this.insert = (insert == null) ? NULL_INSERT : insert;
            this.update = (update == null) ? NULL_UPDATE : update;
        }

        @Override
        public void convert(U table, Record input, ComplexValue<?> entity, DataSize dataSize) {
            read.convert(table, input, entity, dataSize);
        }

        @Override
        public void convert(U table, ComplexValue<?> entity, Map<Field, Object> insertFields) {
            insert.convert(table, entity, insertFields);
        }

        @Override
        public void convert(U table, ComplexValue<?> entity, Map<Field, Object> updateFields, EntityChangedMessage message) {
            update.convert(table, entity, updateFields, message);
        }

    }

    public static class ConverterSimple<U extends StaMainTable<U>> implements ConverterRecord<U> {

        private final Property property;
        private final FieldFetcher<U> factory;
        private final boolean canCreate;
        private final boolean canUpdate;

        public ConverterSimple(Property property, FieldFetcher<U> factory) {
            this(property, factory, true, true);
        }

        public ConverterSimple(Property property, FieldFetcher<U> factory, boolean canCreate, boolean canUpdate) {
            this.property = property;
            this.factory = factory;
            this.canCreate = canCreate;
            this.canUpdate = canUpdate;
        }

        @Override
        public void convert(U table, Record input, ComplexValue<?> entity, DataSize dataSize) {
            entity.setProperty(property, input.get(factory.get(table)));
        }

        @Override
        public void convert(U table, ComplexValue<?> entity, Map<Field, Object> insertFields) {
            if (canCreate) {
                insertFields.put(factory.get(table), entity.getProperty(property));
            }
        }

        @Override
        public void convert(U table, ComplexValue<?> entity, Map<Field, Object> updateFields, EntityChangedMessage message) {
            if (canUpdate) {
                updateFields.put(factory.get(table), entity.getProperty(property));
                message.addField(property);
            }
        }
    }

    public static class ConverterString<U extends StaMainTable<U>> implements ConverterRecord<U> {

        private final Property property;
        private final FieldFetcher<U> factory;

        public ConverterString(Property property, FieldFetcher<U> factory) {
            this.property = property;
            this.factory = factory;
        }

        @Override
        public void convert(U table, Record input, ComplexValue<?> entity, DataSize dataSize) {
            String data = (String) input.get(factory.get(table));
            dataSize.increase(data == null ? 0 : data.length());
            entity.setProperty(property, data);
        }

        @Override
        public void convert(U table, ComplexValue<?> entity, Map<Field, Object> insertFields) {
            insertFields.put(factory.get(table), entity.getProperty(property));
        }

        @Override
        public void convert(U table, ComplexValue<?> entity, Map<Field, Object> updateFields, EntityChangedMessage message) {
            updateFields.put(factory.get(table), entity.getProperty(property));
            message.addField(property);
        }
    }

    public static class ConverterPassword<U extends StaMainTable<U>> implements ConverterRecord<U> {

        private final boolean plainTextPassword;
        private final Property property;
        private final FieldFetcher<U> factory;

        public ConverterPassword(boolean plainTextPassword, Property property, FieldFetcher<U> factory) {
            this.plainTextPassword = plainTextPassword;
            this.property = property;
            this.factory = factory;
        }

        @Override
        public void convert(U table, Record input, ComplexValue<?> entity, DataSize dataSize) {
            // Passwords can not be read.
        }

        @Override
        public void convert(U table, ComplexValue<?> entity, Map<Field, Object> insertFields) {
            if (plainTextPassword) {
                insertFields.put(factory.get(table), entity.getProperty(property));
            } else {
                Field<String> password = DSL.field("crypt(?, gen_salt('bf', 12))", String.class, entity.getProperty(property));
                insertFields.put(factory.get(table), password);
            }
        }

        @Override
        public void convert(U table, ComplexValue<?> entity, Map<Field, Object> updateFields, EntityChangedMessage message) {
            if (plainTextPassword) {
                updateFields.put(factory.get(table), entity.getProperty(property));
            } else {

                Field<String> password = DSL.field("crypt(?, gen_salt('bf', 12))", String.class, entity.getProperty(property));
                updateFields.put(factory.get(table), password);
            }
            message.addField(property);
        }
    }

    public static class ConverterTimeInterval<U extends StaMainTable<U>> implements ConverterRecord<U> {

        private final Property<TimeInterval> property;
        private final FieldFetcher<U> factoryStart;
        private final FieldFetcher<U> factoryEnd;

        public ConverterTimeInterval(Property<TimeInterval> property, FieldFetcher<U> factoryStart, FieldFetcher<U> factoryEnd) {
            this.property = property;
            this.factoryStart = factoryStart;
            this.factoryEnd = factoryEnd;
        }

        @Override
        public void convert(U table, Record input, ComplexValue<?> entity, DataSize dataSize) {
            entity.setProperty(property, Utils.intervalFromTimes(
                    (Moment) input.get(factoryStart.get(table)),
                    (Moment) input.get(factoryEnd.get(table))));
        }

        @Override
        public void convert(U table, ComplexValue<?> entity, Map<Field, Object> insertFields) {
            TimeInterval interval = entity.getProperty(property);
            EntityFactories.insertTimeInterval(insertFields, factoryStart.get(table), factoryEnd.get(table), interval);
        }

        @Override
        public void convert(U table, ComplexValue<?> entity, Map<Field, Object> updateFields, EntityChangedMessage message) {
            TimeInterval interval = entity.getProperty(property);
            EntityFactories.insertTimeInterval(updateFields, factoryStart.get(table), factoryEnd.get(table), interval);
            message.addField(property);
        }
    }

    public static class ConverterTimeInstant<U extends StaMainTable<U>> implements ConverterRecord<U> {

        private final Property<TimeInstant> property;
        private final FieldFetcher<U> factory;

        public ConverterTimeInstant(Property<TimeInstant> property, FieldFetcher<U> factory) {
            this.property = property;
            this.factory = factory;
        }

        @Override
        public void convert(U table, Record input, ComplexValue<?> entity, DataSize dataSize) {
            entity.setProperty(
                    property,
                    Utils.instantFromTime((Moment) input.get(factory.get(table))));
        }

        @Override
        public void convert(U table, ComplexValue<?> entity, Map<Field, Object> insertFields) {
            TimeInstant instant = entity.getProperty(property);
            EntityFactories.insertTimeInstant(insertFields, factory.get(table), instant);
        }

        @Override
        public void convert(U table, ComplexValue<?> entity, Map<Field, Object> updateFields, EntityChangedMessage message) {
            TimeInstant instant = entity.getProperty(property);
            EntityFactories.insertTimeInstant(updateFields, factory.get(table), instant);
            message.addField(property);
        }
    }

    public static class ConverterTimeValue<U extends StaMainTable<U>> implements ConverterRecord<U> {

        private final Property<TimeValue> property;
        private final FieldFetcher<U> factoryStart;
        private final FieldFetcher<U> factoryEnd;

        public ConverterTimeValue(Property<TimeValue> property, FieldFetcher<U> factoryStart, FieldFetcher<U> factoryEnd) {
            this.property = property;
            this.factoryStart = factoryStart;
            this.factoryEnd = factoryEnd;
        }

        @Override
        public void convert(U table, Record input, ComplexValue<?> entity, DataSize dataSize) {
            entity.setProperty(
                    property,
                    Utils.valueFromTimes(
                            (Moment) input.get(factoryStart.get(table)),
                            (Moment) input.get(factoryEnd.get(table))));
        }

        @Override
        public void convert(U table, ComplexValue<?> entity, Map<Field, Object> insertFields) {
            TimeValue value = entity.getProperty(property);
            EntityFactories.insertTimeValue(insertFields, factoryStart.get(table), factoryEnd.get(table), value);
        }

        @Override
        public void convert(U table, ComplexValue<?> entity, Map<Field, Object> updateFields, EntityChangedMessage message) {
            TimeValue value = entity.getProperty(property);
            EntityFactories.insertTimeValue(updateFields, factoryStart.get(table), factoryEnd.get(table), value);
            message.addField(property);
        }
    }

    public static class ConverterMap<U extends StaMainTable<U>> implements ConverterRecord<U> {

        private final Property property;
        private final FieldFetcher<U> factory;

        public ConverterMap(Property property, FieldFetcher<U> factory) {
            this.property = property;
            this.factory = factory;
        }

        @Override
        public void convert(U table, Record input, ComplexValue<?> entity, DataSize dataSize) {
            JsonValue data = Utils.getFieldJsonValue(input, factory.get(table));
            if (data == null) {
                return;
            }
            dataSize.increase(data.getStringLength());
            entity.setProperty(property, data.getTreeValue());
        }

        @Override
        public void convert(U table, ComplexValue<?> entity, Map<Field, Object> insertFields) {
            insertFields.put(factory.get(table), new JsonValue(entity.getProperty(property)));
        }

        @Override
        public void convert(U table, ComplexValue<?> entity, Map<Field, Object> updateFields, EntityChangedMessage message) {
            updateFields.put(factory.get(table), new JsonValue(entity.getProperty(property)));
            message.addField(property);
        }
    }

    public static class ConverterJson<U extends StaMainTable<U>> implements ConverterRecord<U> {

        private final Property property;
        private final FieldFetcher<U> factory;

        public ConverterJson(Property property, FieldFetcher<U> factory) {
            this.property = property;
            this.factory = factory;
        }

        @Override
        public void convert(U table, Record input, ComplexValue<?> entity, DataSize dataSize) {
            JsonValue data = Utils.getFieldJsonValue(input, factory.get(table));
            if (data == null) {
                return;
            }
            dataSize.increase(data.getStringLength());
            entity.setProperty(property, data.getValue());
        }

        @Override
        public void convert(U table, ComplexValue<?> entity, Map<Field, Object> insertFields) {
            insertFields.put(factory.get(table), new JsonValue(entity.getProperty(property)));
        }

        @Override
        public void convert(U table, ComplexValue<?> entity, Map<Field, Object> updateFields, EntityChangedMessage message) {
            updateFields.put(factory.get(table), new JsonValue(entity.getProperty(property)));
            message.addField(property);
        }
    }

    public static class ConverterEntity<U extends StaMainTable<U>> implements ConverterRecord<U> {

        private final NavigationPropertyEntity property;
        private final FieldFetcher<U> factory;

        public ConverterEntity(NavigationPropertyEntity property, FieldFetcher<U> factory) {
            this.property = property;
            this.factory = factory;
            if (!(property.getEntityType().getPrimaryKey() instanceof PkSingle)) {
                throw new NotImplementedException(NOT_IMPLEMENTED_MULTI_VALUE_PK);
            }
        }

        @Override
        public void convert(U table, Record input, ComplexValue<?> entity, DataSize dataSize) {
            final Object rawId = getFieldOrNull(input, factory.get(table));
            if (rawId == null) {
                return;
            }
            DefaultEntity childEntity = new DefaultEntity(property.getEntityType(), PkValue.of(rawId));
            entity.setProperty(property, childEntity);
        }

        @Override
        public void convert(U table, ComplexValue<?> entity, Map<Field, Object> insertFields) {
            Entity child = entity.getProperty(property);
            insertFields.put(factory.get(table), child.getPrimaryKeyValues().get(0));
        }

        @Override
        public void convert(U table, ComplexValue<?> entity, Map<Field, Object> updateFields, EntityChangedMessage message) {
            Entity child = entity.getProperty(property);
            updateFields.put(factory.get(table), child.getPrimaryKeyValues().get(0));
            message.addField(property);
        }
    }

    public static class ConverterEntitySet<U extends StaMainTable<U>> implements ConverterRecord<U> {

        @Override
        public void convert(U table, Record input, ComplexValue<?> entity, DataSize dataSize) {
            // EntitySet properties are not fetched in this way
        }

        @Override
        public void convert(U table, ComplexValue<?> entity, Map<Field, Object> insertFields) {
            // EntitySet properties are not created in this way
        }

        @Override
        public void convert(U table, ComplexValue<?> entity, Map<Field, Object> updateFields, EntityChangedMessage message) {
            // EntitySet properties are not updated in this way
        }
    }

}
