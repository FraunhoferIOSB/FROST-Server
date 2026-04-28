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

import de.fraunhofer.iosb.ilt.frostserver.model.DefaultEntity;
import de.fraunhofer.iosb.ilt.frostserver.model.EntityChangedMessage;
import de.fraunhofer.iosb.ilt.frostserver.model.ModelRegistry;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.model.core.PkSingle;
import de.fraunhofer.iosb.ilt.frostserver.model.core.PkValue;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.TimeInstant;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.TimeInterval;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.TimeValue;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.bindings.JsonValue;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.factories.EntityFactories;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.StaMainTable;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityProperty;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyCustomSelect;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain.NavigationPropertyEntity;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain.NavigationPropertyEntitySet;
import de.fraunhofer.iosb.ilt.frostserver.property.Property;
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
import tools.jackson.core.TreeNode;

/**
 *
 * @author hylke
 * @param <T> The table type this registry has fields for.
 */
public class PropertyFieldRegistry<T extends StaMainTable<T>> {

    private final T table;
    /**
     * The Fields that are allowed be appear in select statements.
     */
    private final Map<Property, PropertyFields<T>> epMapSelect;
    /**
     * The Fields that are allowed in where and orderby statements.
     */
    private final Map<Property, Map<String, ExpressionFactory<T>>> epMapAll;
    /**
     * All select-able fields, by class.
     */
    private final List<PropertyFields<T>> allSelectPropertyFields;

    public static interface ExpressionFactory<U extends StaMainTable<U>> {

        public Field get(U table);
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

    public static final ConverterRecordInsert NULL_INSERT = (table, entity, insertFields) -> {
        // Does nothing
    };

    public static interface ConverterRecordUpdate<U extends StaMainTable<U>> {

        public void convert(U table, ComplexValue<?> entity, Map<Field, Object> updateFields, EntityChangedMessage message);
    }

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

        public ConverterRecordDeflt(ConverterRecordRead<U> read) {
            this(read, null, null);
        }

        public ConverterRecordDeflt(ConverterRecordRead<U> read, ConverterRecordInsert<U> insert, ConverterRecordUpdate<U> update) {
            this.read = read;
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

    public static class PropertyFields<U extends StaMainTable<U>> {

        public final Property property;
        public final boolean jsonType;
        public final Map<String, ExpressionFactory<U>> fields = new LinkedHashMap<>();
        public final ConverterRecord<U> converter;

        public PropertyFields(Property property, ConverterRecord<U> converter) {
            this(property, false, converter);
        }

        public PropertyFields(Property property, boolean jsonType, ConverterRecord<U> converter) {
            this.property = property;
            this.converter = converter;
            this.jsonType = jsonType;
        }

        public PropertyFields<U> addField(String name, ExpressionFactory<U> field) {
            String key = name;
            if (key == null) {
                key = Integer.toString(fields.size());
            }
            fields.put(key, field);
            return this;
        }

        @Override
        public String toString() {
            return property.getName();
        }

    }

    public static class PropertyFactoryCombo<U extends StaMainTable<U>> {

        public final Property property;
        public final ExpressionFactory<U> factory;

        public PropertyFactoryCombo(Property property, ExpressionFactory<U> factory) {
            this.property = property;
            this.factory = factory;
        }

    }

    /**
     * A NameFactoryPair for easier passing of a name and a factory.
     *
     * @param <U> the table type this NFP fetches from.
     */
    public static class NFP<U extends StaMainTable<U>> {

        public final String name;
        public final ExpressionFactory<U> factory;

        public NFP(String name, ExpressionFactory<U> factory) {
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

    public PropertyFieldRegistry(T table, PropertyFieldRegistry<T> copyFrom) {
        this.table = table;
        this.epMapSelect = copyFrom.epMapSelect;
        this.epMapAll = copyFrom.epMapAll;
        this.allSelectPropertyFields = copyFrom.allSelectPropertyFields;
    }

    /**
     * Get the Fields for the given class, that are allowed to be used in the
     * select clause of a query.
     *
     * @param <C> The type of collection given as a target.
     * @param target The list to add to. If null a new ArrayList will be
     * created.
     * @return The target list, or a new list if target was null.
     */
    public <C extends Collection<PropertyFields<T>>> C getSelectFields(C target) {
        C result = target;
        if (result == null) {
            result = (C) new ArrayList();
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
    public PropertyFields<T> getSelectFieldsForProperty(Property property) {
        if (property instanceof EntityPropertyCustomSelect epCustomSelect) {
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
    public Set<PropertyFields<T>> getFieldsForProperties(Set<Property> selectedProperties) {
        Set<PropertyFields<T>> exprSet = new LinkedHashSet<>();
        if (selectedProperties.isEmpty()) {
            getSelectFields(exprSet);
        } else {
            for (Property property : selectedProperties) {
                final PropertyFields<T> selectFieldsForProperty = getSelectFieldsForProperty(property);
                if (selectFieldsForProperty != null) {
                    exprSet.add(selectFieldsForProperty);
                }
            }
        }
        return exprSet;
    }

    public void addEntry(NavigationPropertyMain property, ExpressionFactory<T> factory) {
        if (property instanceof NavigationPropertyEntity navigationPropertyEntity) {
            addEntry(navigationPropertyEntity, factory);
        } else if (property instanceof NavigationPropertyEntitySet navigationPropertyEntitySet) {
            addEntry(navigationPropertyEntitySet, factory);
        } else {
            throw new IllegalArgumentException("Unknown NavigationProperty type: " + property);
        }
    }

    public void addEntry(NavigationPropertyEntity property, ExpressionFactory<T> factory) {
        PropertyFields<T> pf = new PropertyFields<>(property, new ConverterEntity<>(property, factory));
        pf.addField(null, factory);
        epMapSelect.put(property, pf);
        allSelectPropertyFields.add(pf);
        addEntry(epMapAll, property, null, factory);
    }

    public void addEntry(NavigationPropertyEntity property, ExpressionFactory<T> factory, ConverterRecord<T> ps) {
        PropertyFields<T> pf = new PropertyFields<>(property, ps);
        pf.addField(null, factory);
        epMapSelect.put(property, pf);
        allSelectPropertyFields.add(pf);
        addEntry(epMapAll, property, null, factory);
    }

    public void addEntry(NavigationPropertyEntitySet property, ExpressionFactory<T> factory) {
        PropertyFields<T> pf = new PropertyFields<T>(property, new ConverterEntitySet<>());
        pf.addField(null, factory);
        epMapSelect.put(property, pf);
        allSelectPropertyFields.add(pf);
        addEntry(epMapAll, property, null, factory);
    }

    public void addEntryString(EntityProperty<String> property, ExpressionFactory<T> factory) {
        PropertyFields<T> pf = new PropertyFields<>(property, new ConverterString<>(property, factory));
        pf.addField(null, factory);
        epMapSelect.put(property, pf);
        allSelectPropertyFields.add(pf);
        addEntry(epMapAll, property, null, factory);
    }

    public void addEntryNumeric(EntityProperty<BigDecimal> property, ExpressionFactory<T> factory) {
        PropertyFields<T> pf = new PropertyFields<>(property, new ConverterSimple<>(property, factory));
        pf.addField(null, factory);
        epMapSelect.put(property, pf);
        allSelectPropertyFields.add(pf);
        addEntry(epMapAll, property, null, factory);
    }

    public void addEntryId(ExpressionFactory<T> factory) {
        final EntityPropertyMain keyProperty = table.getEntityType().getPrimaryKey().getKeyProperty(0);
        final ConverterSimple<T> converterId = new ConverterSimple<>(keyProperty, factory, true, false);
        addEntry(keyProperty, factory, converterId);
        final ConverterSimple<T> converterSelfLink = new ConverterSimple<>(keyProperty, factory, false, false);
        addEntry(ModelRegistry.EP_SELFLINK, factory, converterSelfLink);
    }

    public void addEntryMap(EntityProperty<TreeNode> property, ExpressionFactory<T> factory) {
        PropertyFields<T> pf = new PropertyFields<>(property, true, new ConverterMap<>(property, factory));
        pf.addField(null, factory);
        epMapSelect.put(property, pf);
        allSelectPropertyFields.add(pf);
        addEntry(epMapAll, property, null, factory);
    }

    public void addEntrySimple(EntityProperty property, ExpressionFactory<T> factory) {
        PropertyFields<T> pf = new PropertyFields<>(property, new ConverterSimple<>(property, factory));
        pf.addField(null, factory);
        epMapSelect.put(property, pf);
        allSelectPropertyFields.add(pf);
        addEntry(epMapAll, property, null, factory);
    }

    /**
     * Add an unnamed entry to the Field registry.
     *
     * @param property The property that this field supplies data for.
     * @param factory The factory to use to generate the Field instance.
     * @param ps The ConverterRecordRead to use to set the get the property from
     * a record and set it on an Entity.
     */
    public void addEntry(Property property, ExpressionFactory<T> factory, ConverterRecord<T> ps) {
        addEntry(property, false, factory, ps);
    }

    public void addEntry(Property property, boolean isJson, ExpressionFactory<T> factory, ConverterRecord<T> ps) {
        PropertyFields<T> pf = new PropertyFields<>(property, isJson, ps);
        if (factory != null) {
            pf.addField(null, factory);
            addEntry(epMapAll, property, null, factory);
        }
        epMapSelect.put(property, pf);
        allSelectPropertyFields.add(pf);
    }

    /**
     * Add an entry to the Field registry.
     *
     * @param property The property that this field supplies data for.
     * @param ps The ConverterRecordRead used to set the property from a
     * database record.
     * @param factories The factories to use to generate the Field instance used
     * for filter and orderby.
     */
    public void addEntry(Property property, ConverterRecord<T> ps, NFP<T>... factories) {
        addEntry(property, false, ps, factories);
    }

    public void addEntry(Property property, boolean isJson, ConverterRecord<T> ps, NFP<T>... factories) {
        PropertyFields<T> pf = new PropertyFields<>(property, isJson, ps);
        for (NFP<T> nfp : factories) {
            pf.addField(nfp.name, nfp.factory);
            addEntry(epMapAll, property, nfp.name, nfp.factory);
        }
        epMapSelect.put(property, pf);
        allSelectPropertyFields.add(pf);
    }

    /**
     * Add an entry to the Field registry, but do not register it to the entity.
     * This means the field is never used in "select" clauses, but can be used
     * in "filter" clauses.
     *
     * @param property The property that this field supplies data for.
     * @param name The name to use for this field. (j for json, s for string, g
     * for geometry, b for boolean)
     * @param factory The factory to use to generate the Field instance.
     */
    public void addEntryNoSelect(Property property, String name, ExpressionFactory<T> factory) {
        addEntry(epMapAll, property, name, factory);
    }

    private void addEntry(Map<Property, Map<String, ExpressionFactory<T>>> map, Property property, String name, ExpressionFactory<T> factory) {
        Map<String, ExpressionFactory<T>> coreMap = map.computeIfAbsent(
                property,
                k -> new LinkedHashMap<>());
        String key = name;
        if (key == null) {
            key = Integer.toString(coreMap.size());
        }
        coreMap.put(key, factory);
    }

    public static class ConverterSimple<U extends StaMainTable<U>> implements ConverterRecord<U> {

        private final Property property;
        private final ExpressionFactory<U> factory;
        private final boolean canCreate;
        private final boolean canUpdate;

        public ConverterSimple(Property property, ExpressionFactory<U> factory) {
            this(property, factory, true, true);
        }

        public ConverterSimple(Property property, ExpressionFactory<U> factory, boolean canCreate, boolean canUpdate) {
            this.property = property;
            this.factory = factory;
            this.canCreate = canCreate;
            this.canUpdate = canUpdate;
        }

        @Override
        public void convert(U table, Record input, Entity entity, DataSize dataSize) {
            entity.setProperty(property, input.get(factory.get(table)));
        }

        @Override
        public void convert(U table, Entity entity, Map<Field, Object> insertFields) {
            if (canCreate) {
                insertFields.put(factory.get(table), entity.getProperty(property));
            }
        }

        @Override
        public void convert(U table, Entity entity, Map<Field, Object> updateFields, EntityChangedMessage message) {
            if (canUpdate) {
                updateFields.put(factory.get(table), entity.getProperty(property));
                message.addField(property);
            }
        }
    }

    public static class ConverterString<U extends StaMainTable<U>> implements ConverterRecord<U> {

        private final Property property;
        private final ExpressionFactory<U> factory;

        public ConverterString(Property property, ExpressionFactory<U> factory) {
            this.property = property;
            this.factory = factory;
        }

        @Override
        public void convert(U table, Record input, Entity entity, DataSize dataSize) {
            String data = (String) input.get(factory.get(table));
            dataSize.increase(data == null ? 0 : data.length());
            entity.setProperty(property, data);
        }

        @Override
        public void convert(U table, Entity entity, Map<Field, Object> insertFields) {
            insertFields.put(factory.get(table), entity.getProperty(property));
        }

        @Override
        public void convert(U table, Entity entity, Map<Field, Object> updateFields, EntityChangedMessage message) {
            updateFields.put(factory.get(table), entity.getProperty(property));
            message.addField(property);
        }
    }

    public static class ConverterPassword<U extends StaMainTable<U>> implements ConverterRecord<U> {

        private final boolean plainTextPassword;
        private final Property property;
        private final ExpressionFactory<U> factory;

        public ConverterPassword(boolean plainTextPassword, Property property, ExpressionFactory<U> factory) {
            this.plainTextPassword = plainTextPassword;
            this.property = property;
            this.factory = factory;
        }

        @Override
        public void convert(U table, Record input, Entity entity, DataSize dataSize) {
            // Passwords can not be read.
        }

        @Override
        public void convert(U table, Entity entity, Map<Field, Object> insertFields) {
            if (plainTextPassword) {
                insertFields.put(factory.get(table), entity.getProperty(property));
            } else {
                Field<String> password = DSL.field("crypt(?, gen_salt('bf', 12))", String.class, entity.getProperty(property));
                insertFields.put(factory.get(table), password);
            }
        }

        @Override
        public void convert(U table, Entity entity, Map<Field, Object> updateFields, EntityChangedMessage message) {
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
        private final ExpressionFactory<U> factoryStart;
        private final ExpressionFactory<U> factoryEnd;

        public ConverterTimeInterval(Property<TimeInterval> property, ExpressionFactory<U> factoryStart, ExpressionFactory<U> factoryEnd) {
            this.property = property;
            this.factoryStart = factoryStart;
            this.factoryEnd = factoryEnd;
        }

        @Override
        public void convert(U table, Record input, Entity entity, DataSize dataSize) {
            entity.setProperty(property, Utils.intervalFromTimes(
                    (Moment) input.get(factoryStart.get(table)),
                    (Moment) input.get(factoryEnd.get(table))));
        }

        @Override
        public void convert(U table, Entity entity, Map<Field, Object> insertFields) {
            TimeInterval interval = entity.getProperty(property);
            EntityFactories.insertTimeInterval(insertFields, factoryStart.get(table), factoryEnd.get(table), interval);
        }

        @Override
        public void convert(U table, Entity entity, Map<Field, Object> updateFields, EntityChangedMessage message) {
            TimeInterval interval = entity.getProperty(property);
            EntityFactories.insertTimeInterval(updateFields, factoryStart.get(table), factoryEnd.get(table), interval);
            message.addField(property);
        }
    }

    public static class ConverterTimeInstant<U extends StaMainTable<U>> implements ConverterRecord<U> {

        private final Property<TimeInstant> property;
        private final ExpressionFactory<U> factory;

        public ConverterTimeInstant(Property<TimeInstant> property, ExpressionFactory<U> factory) {
            this.property = property;
            this.factory = factory;
        }

        @Override
        public void convert(U table, Record input, Entity entity, DataSize dataSize) {
            entity.setProperty(
                    property,
                    Utils.instantFromTime((Moment) input.get(factory.get(table))));
        }

        @Override
        public void convert(U table, Entity entity, Map<Field, Object> insertFields) {
            TimeInstant instant = entity.getProperty(property);
            EntityFactories.insertTimeInstant(insertFields, factory.get(table), instant);
        }

        @Override
        public void convert(U table, Entity entity, Map<Field, Object> updateFields, EntityChangedMessage message) {
            TimeInstant instant = entity.getProperty(property);
            EntityFactories.insertTimeInstant(updateFields, factory.get(table), instant);
            message.addField(property);
        }
    }

    public static class ConverterTimeValue<U extends StaMainTable<U>> implements ConverterRecord<U> {

        private final Property<TimeValue> property;
        private final ExpressionFactory<U> factoryStart;
        private final ExpressionFactory<U> factoryEnd;

        public ConverterTimeValue(Property<TimeValue> property, ExpressionFactory<U> factoryStart, ExpressionFactory<U> factoryEnd) {
            this.property = property;
            this.factoryStart = factoryStart;
            this.factoryEnd = factoryEnd;
        }

        @Override
        public void convert(U table, Record input, Entity entity, DataSize dataSize) {
            entity.setProperty(
                    property,
                    Utils.valueFromTimes(
                            (Moment) input.get(factoryStart.get(table)),
                            (Moment) input.get(factoryEnd.get(table))));
        }

        @Override
        public void convert(U table, Entity entity, Map<Field, Object> insertFields) {
            TimeValue value = entity.getProperty(property);
            EntityFactories.insertTimeValue(insertFields, factoryStart.get(table), factoryEnd.get(table), value);
        }

        @Override
        public void convert(U table, Entity entity, Map<Field, Object> updateFields, EntityChangedMessage message) {
            TimeValue value = entity.getProperty(property);
            EntityFactories.insertTimeValue(updateFields, factoryStart.get(table), factoryEnd.get(table), value);
            message.addField(property);
        }
    }

    public static class ConverterMap<U extends StaMainTable<U>> implements ConverterRecord<U> {

        private final Property property;
        private final ExpressionFactory<U> factory;

        public ConverterMap(Property property, ExpressionFactory<U> factory) {
            this.property = property;
            this.factory = factory;
        }

        @Override
        public void convert(U table, Record input, Entity entity, DataSize dataSize) {
            JsonValue data = Utils.getFieldJsonValue(input, factory.get(table));
            if (data == null) {
                return;
            }
            dataSize.increase(data.getStringLength());
            entity.setProperty(property, data.getTreeValue());
        }

        @Override
        public void convert(U table, Entity entity, Map<Field, Object> insertFields) {
            insertFields.put(factory.get(table), new JsonValue(entity.getProperty(property)));
        }

        @Override
        public void convert(U table, Entity entity, Map<Field, Object> updateFields, EntityChangedMessage message) {
            updateFields.put(factory.get(table), new JsonValue(entity.getProperty(property)));
            message.addField(property);
        }
    }

    public static class ConverterEntity<U extends StaMainTable<U>> implements ConverterRecord<U> {

        private final NavigationPropertyEntity property;
        private final ExpressionFactory<U> factory;

        public ConverterEntity(NavigationPropertyEntity property, ExpressionFactory<U> factory) {
            this.property = property;
            this.factory = factory;
            if (!(property.getEntityType().getPrimaryKey() instanceof PkSingle)) {
                throw new NotImplementedException(NOT_IMPLEMENTED_MULTI_VALUE_PK);
            }
        }

        @Override
        public void convert(U table, Record input, Entity entity, DataSize dataSize) {
            final Object rawId = getFieldOrNull(input, factory.get(table));
            if (rawId == null) {
                return;
            }
            DefaultEntity childEntity = new DefaultEntity(property.getEntityType(), PkValue.of(rawId));
            entity.setProperty(property, childEntity);
        }

        @Override
        public void convert(U table, Entity entity, Map<Field, Object> insertFields) {
            Entity child = entity.getProperty(property);
            insertFields.put(factory.get(table), child.getPrimaryKeyValues().get(0));
        }

        @Override
        public void convert(U table, Entity entity, Map<Field, Object> updateFields, EntityChangedMessage message) {
            Entity child = entity.getProperty(property);
            updateFields.put(factory.get(table), child.getPrimaryKeyValues().get(0));
            message.addField(property);
        }
    }

    public static class ConverterEntitySet<U extends StaMainTable<U>> implements ConverterRecord<U> {

        @Override
        public void convert(U table, Record input, Entity entity, DataSize dataSize) {
            // EntitySet properties are not fetched in this way
        }

        @Override
        public void convert(U table, Entity entity, Map<Field, Object> insertFields) {
            // EntitySet properties are not created in this way
        }

        @Override
        public void convert(U table, Entity entity, Map<Field, Object> updateFields, EntityChangedMessage message) {
            // EntitySet properties are not updated in this way
        }
    }

}
