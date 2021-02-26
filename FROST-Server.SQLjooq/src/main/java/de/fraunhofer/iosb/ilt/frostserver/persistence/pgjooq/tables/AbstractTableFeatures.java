package de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables;

import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.FeatureOfInterest;
import de.fraunhofer.iosb.ilt.frostserver.persistence.IdManager;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.bindings.JsonBinding;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.bindings.JsonValue;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.bindings.PostGisGeometryBinding;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.factories.EntityFactories;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.fieldwrapper.JsonFieldFactory;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.relations.RelationOneToMany;
import static de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.StaTableAbstract.jsonFieldFromPath;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.DataSize;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.PropertyFieldRegistry.NFP;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.PropertyFieldRegistry.PropertyFields;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.PropertyFieldRegistry.PropertySetter;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.Utils;
import static de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.Utils.getFieldOrNull;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyCustomSelect;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain;
import org.geolatte.geom.Geometry;
import org.jooq.Field;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultDataType;
import org.jooq.impl.SQLDataType;

public abstract class AbstractTableFeatures<J extends Comparable> extends StaTableAbstract<J, FeatureOfInterest, AbstractTableFeatures<J>> {

    private static final long serialVersionUID = 750481677;

    /**
     * The column <code>public.FEATURES.DESCRIPTION</code>.
     */
    public final TableField<Record, String> colDescription = createField(DSL.name("DESCRIPTION"), SQLDataType.CLOB, this, "");

    /**
     * The column <code>public.FEATURES.ENCODING_TYPE</code>.
     */
    public final TableField<Record, String> colEncodingType = createField(DSL.name("ENCODING_TYPE"), SQLDataType.CLOB, this, "");

    /**
     * The column <code>public.FEATURES.FEATURE</code>.
     */
    public final TableField<Record, String> colFeature = createField(DSL.name("FEATURE"), SQLDataType.CLOB, this, "");

    /**
     * The column <code>public.FEATURES.GEOM</code>.
     */
    public final TableField<Record, Geometry> colGeom = createField(DSL.name("GEOM"), DefaultDataType.getDefaultDataType(TYPE_GEOMETRY), this, "", new PostGisGeometryBinding());

    /**
     * The column <code>public.FEATURES.NAME</code>.
     */
    public final TableField<Record, String> colName = createField(DSL.name("NAME"), SQLDataType.CLOB.defaultValue(DSL.field("'no name'::text", SQLDataType.CLOB)), this, "");

    /**
     * The column <code>public.FEATURES.PROPERTIES</code>.
     */
    public final TableField<Record, JsonValue> colProperties = createField(DSL.name("PROPERTIES"), DefaultDataType.getDefaultDataType(TYPE_JSONB), this, "", new JsonBinding());

    /**
     * Create a <code>public.FEATURES</code> table reference
     */
    protected AbstractTableFeatures() {
        this(DSL.name("FEATURES"), null);
    }

    protected AbstractTableFeatures(Name alias, AbstractTableFeatures<J> aliased) {
        this(alias, aliased, null);
    }

    protected AbstractTableFeatures(Name alias, AbstractTableFeatures<J> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""));
    }

    @Override
    public void initRelations() {
        final TableCollection<J> tables = getTables();
        registerRelation(
                new RelationOneToMany<>(this, tables.getTableObservations(), EntityType.OBSERVATION, true)
                        .setSourceFieldAccessor(AbstractTableFeatures::getId)
                        .setTargetFieldAccessor(AbstractTableObservations::getFeatureId)
        );
    }

    @Override
    public void initProperties(final EntityFactories<J> entityFactories) {
        final IdManager idManager = entityFactories.idManager;
        final PropertySetter<AbstractTableFeatures<J>, FeatureOfInterest> setterId
                = (AbstractTableFeatures<J> table, Record tuple, FeatureOfInterest entity, DataSize dataSize) -> entity.setId(idManager.fromObject(tuple.get(table.getId())));
        pfReg.addEntry(EntityPropertyMain.ID, AbstractTableFeatures::getId, setterId);
        pfReg.addEntry(EntityPropertyMain.SELFLINK, AbstractTableFeatures::getId, setterId);
        pfReg.addEntry(
                EntityPropertyMain.NAME,
                table -> table.colName,
                (AbstractTableFeatures<J> table, Record tuple, FeatureOfInterest entity, DataSize dataSize) -> entity.setName(tuple.get(table.colName)));
        pfReg.addEntry(
                EntityPropertyMain.DESCRIPTION,
                table -> table.colDescription,
                (AbstractTableFeatures<J> table, Record tuple, FeatureOfInterest entity, DataSize dataSize) -> entity.setDescription(tuple.get(table.colDescription)));
        pfReg.addEntry(
                EntityPropertyMain.ENCODINGTYPE,
                table -> table.colEncodingType,
                (AbstractTableFeatures<J> table, Record tuple, FeatureOfInterest entity, DataSize dataSize) -> entity.setEncodingType(tuple.get(table.colEncodingType)));
        pfReg.addEntry(EntityPropertyMain.FEATURE,
                (AbstractTableFeatures<J> table, Record tuple, FeatureOfInterest entity, DataSize dataSize) -> {
                    String encodingType = getFieldOrNull(tuple, table.colEncodingType);
                    String locationString = tuple.get(table.colFeature);
                    dataSize.increase(locationString == null ? 0 : locationString.length());
                    entity.setFeature(Utils.locationFromEncoding(encodingType, locationString));
                },
                new NFP<>("j", table -> table.colFeature));
        pfReg.addEntryNoSelect(EntityPropertyMain.FEATURE, "g", table -> table.colGeom);
        pfReg.addEntry(EntityPropertyMain.PROPERTIES, table -> table.colProperties,
                (AbstractTableFeatures<J> table, Record tuple, FeatureOfInterest entity, DataSize dataSize) -> {
                    JsonValue props = Utils.getFieldJsonValue(tuple, table.colProperties);
                    dataSize.increase(props.getStringLength());
                    entity.setProperties(props.getMapValue());
                });
        pfReg.addEntry(NavigationPropertyMain.OBSERVATIONS, AbstractTableFeatures::getId, setterId);
    }

    @Override
    public FeatureOfInterest newEntity() {
        return new FeatureOfInterest();
    }

    @Override
    public abstract TableField<Record, J> getId();

    @Override
    public abstract AbstractTableFeatures<J> as(Name as);

    @Override
    public abstract AbstractTableFeatures<J> as(String alias);

    @Override
    public PropertyFields<AbstractTableFeatures<J>, FeatureOfInterest> handleEntityPropertyCustomSelect(final EntityPropertyCustomSelect epCustomSelect) {
        final EntityPropertyMain mainEntityProperty = epCustomSelect.getMainEntityProperty();
        if (mainEntityProperty == EntityPropertyMain.FEATURE) {
            PropertyFields<AbstractTableFeatures<J>, FeatureOfInterest> mainPropertyFields = pfReg.getSelectFieldsForProperty(mainEntityProperty);
            final Field mainField = mainPropertyFields.fields.values().iterator().next().get(getThis());

            JsonFieldFactory jsonFactory = jsonFieldFromPath(mainField, epCustomSelect);
            return propertyFieldForJsonField(jsonFactory, epCustomSelect);
        }
        return super.handleEntityPropertyCustomSelect(epCustomSelect);
    }

    @Override
    public AbstractTableFeatures<J> getThis() {
        return this;
    }

}
