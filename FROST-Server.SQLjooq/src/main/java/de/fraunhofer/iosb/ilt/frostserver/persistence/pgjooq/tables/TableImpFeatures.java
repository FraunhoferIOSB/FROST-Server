package de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables;

import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.persistence.IdManager;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.PostgresPersistenceManager;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.bindings.JsonBinding;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.bindings.JsonValue;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.bindings.PostGisGeometryBinding;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.factories.EntityFactories;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.fieldwrapper.JsonFieldFactory;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.relations.RelationOneToMany;
import static de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.StaTableAbstract.jsonFieldFromPath;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.DataSize;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.PropertyFieldRegistry.ConverterRecordDeflt;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.PropertyFieldRegistry.NFP;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.PropertyFieldRegistry.PropertyFields;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.Utils;
import static de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.Utils.getFieldOrNull;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyCustomSelect;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.NoSuchEntityException;
import org.geolatte.geom.Geometry;
import org.jooq.DataType;
import org.jooq.Field;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultDataType;
import org.jooq.impl.SQLDataType;

public class TableImpFeatures<J extends Comparable> extends StaTableAbstract<J, TableImpFeatures<J>> {

    private static final long serialVersionUID = 750481677;

    private static TableImpFeatures INSTANCE;
    private static DataType INSTANCE_ID_TYPE;

    public static <J extends Comparable> TableImpFeatures<J> getInstance(DataType<J> idType) {
        if (INSTANCE == null) {
            INSTANCE_ID_TYPE = idType;
            INSTANCE = new TableImpFeatures(INSTANCE_ID_TYPE);
            return INSTANCE;
        }
        if (INSTANCE_ID_TYPE.equals(idType)) {
            return INSTANCE;
        }
        return new TableImpFeatures<>(idType);
    }

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
     * The column <code>public.FEATURES.ID</code>.
     */
    public final TableField<Record, J> colId = createField(DSL.name("ID"), getIdType(), this);

    /**
     * Create a <code>public.FEATURES</code> table reference
     */
    private TableImpFeatures(DataType<J> idType) {
        super(idType, DSL.name("FEATURES"), null);
    }

    private TableImpFeatures(Name alias, TableImpFeatures<J> aliased) {
        super(aliased.getIdType(), alias, aliased);
    }

    @Override
    public void initRelations() {
        final TableCollection<J> tables = getTables();
        registerRelation(new RelationOneToMany<>(this, TableImpObservations.getInstance(getIdType()), EntityType.OBSERVATION, true)
                        .setSourceFieldAccessor(TableImpFeatures::getId)
                        .setTargetFieldAccessor(TableImpObservations::getFeatureId)
        );
    }

    @Override
    public void initProperties(final EntityFactories<J> entityFactories) {
        final IdManager idManager = entityFactories.idManager;
        pfReg.addEntryId(idManager, TableImpFeatures::getId);
        pfReg.addEntryString(EntityPropertyMain.NAME, table -> table.colName);
        pfReg.addEntryString(EntityPropertyMain.DESCRIPTION, table -> table.colDescription);
        pfReg.addEntryString(EntityPropertyMain.ENCODINGTYPE, table -> table.colEncodingType);
        pfReg.addEntry(EntityPropertyMain.FEATURE,
                new ConverterRecordDeflt<>(
                        (TableImpFeatures<J> table, Record tuple, Entity entity, DataSize dataSize) -> {
                            String encodingType = getFieldOrNull(tuple, table.colEncodingType);
                            String locationString = tuple.get(table.colFeature);
                            dataSize.increase(locationString == null ? 0 : locationString.length());
                            entity.setProperty(EntityPropertyMain.FEATURE, Utils.locationFromEncoding(encodingType, locationString));
                        },
                        (table, entity, insertFields) -> {
                            Object feature = entity.getProperty(EntityPropertyMain.FEATURE);
                            String encodingType = entity.getProperty(EntityPropertyMain.ENCODINGTYPE);
                            EntityFactories.insertGeometry(insertFields, table.colFeature, table.colGeom, encodingType, feature);
                        },
                        (table, entity, updateFields, message) -> {
                            Object feature = entity.getProperty(EntityPropertyMain.FEATURE);
                            String encodingType = entity.getProperty(EntityPropertyMain.ENCODINGTYPE);
                            EntityFactories.insertGeometry(updateFields, table.colFeature, table.colGeom, encodingType, feature);
                            message.addField(EntityPropertyMain.FEATURE);
                        }),
                new NFP<>("j", table -> table.colFeature));
        pfReg.addEntryNoSelect(EntityPropertyMain.FEATURE, "g", table -> table.colGeom);
        pfReg.addEntryMap(EntityPropertyMain.PROPERTIES, table -> table.colProperties);
        pfReg.addEntry(NavigationPropertyMain.OBSERVATIONS, TableImpFeatures::getId, idManager);
    }

    @Override
    public void delete(PostgresPersistenceManager<J> pm, J entityId) throws NoSuchEntityException {
        super.delete(pm, entityId);

        // Delete references to the FoI in the Locations table.
        TableImpLocations<J> tLoc = TableImpLocations.getInstance(getIdType());
        pm.getDslContext()
                .update(tLoc)
                .set(tLoc.getGenFoiId(), (J) null)
                .where(tLoc.getGenFoiId().eq(entityId))
                .execute();
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.FEATURE_OF_INTEREST;
    }

    @Override
    public TableField<Record, J> getId() {
        return colId;
    }

    @Override
    public TableImpFeatures<J> as(Name alias) {
        return new TableImpFeatures<>(alias, this);
    }

    @Override
    public TableImpFeatures<J> as(String alias) {
        return new TableImpFeatures<>(DSL.name(alias), this);
    }

    @Override
    public PropertyFields<TableImpFeatures<J>> handleEntityPropertyCustomSelect(final EntityPropertyCustomSelect epCustomSelect) {
        final EntityPropertyMain mainEntityProperty = epCustomSelect.getMainEntityProperty();
        if (mainEntityProperty == EntityPropertyMain.FEATURE) {
            PropertyFields<TableImpFeatures<J>> mainPropertyFields = pfReg.getSelectFieldsForProperty(mainEntityProperty);
            final Field mainField = mainPropertyFields.fields.values().iterator().next().get(getThis());

            JsonFieldFactory jsonFactory = jsonFieldFromPath(mainField, epCustomSelect);
            return propertyFieldForJsonField(jsonFactory, epCustomSelect);
        }
        return super.handleEntityPropertyCustomSelect(epCustomSelect);
    }

    @Override
    public TableImpFeatures<J> getThis() {
        return this;
    }

}
