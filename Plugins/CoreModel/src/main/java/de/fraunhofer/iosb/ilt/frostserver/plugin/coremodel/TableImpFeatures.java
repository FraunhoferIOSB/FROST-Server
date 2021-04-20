package de.fraunhofer.iosb.ilt.frostserver.plugin.coremodel;

import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.ModelRegistry;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.persistence.IdManager;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.PostgresPersistenceManager;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.bindings.JsonBinding;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.bindings.JsonValue;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.bindings.PostGisGeometryBinding;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.factories.EntityFactories;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.relations.RelationOneToMany;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.StaTableAbstract;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.TableCollection;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.DataSize;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.PropertyFieldRegistry.ConverterRecordDeflt;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.PropertyFieldRegistry.NFP;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.Utils;
import static de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.Utils.getFieldOrNull;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.NoSuchEntityException;
import org.geolatte.geom.Geometry;
import org.jooq.DataType;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultDataType;
import org.jooq.impl.SQLDataType;

public class TableImpFeatures<J extends Comparable> extends StaTableAbstract<J, TableImpFeatures<J>> {

    public static final String NAME_TABLE = "FEATURES";
    public static final String NAME_COL_DESCRIPTION = "DESCRIPTION";
    public static final String NAME_COL_ENCODINGTYPE = "ENCODING_TYPE";
    public static final String NAME_COL_FEATURE = "FEATURE";
    public static final String NAME_COL_GEOM = "GEOM";
    public static final String NAME_COL_ID = "ID";
    public static final String NAME_COL_NAME = "NAME";
    public static final String NAME_COL_PROPERTIES = "PROPERTIES";

    private static final long serialVersionUID = 750481677;

    /**
     * The column <code>public.FEATURES.DESCRIPTION</code>.
     */
    public final TableField<Record, String> colDescription = createField(DSL.name(NAME_COL_DESCRIPTION), SQLDataType.CLOB, this, "");

    /**
     * The column <code>public.FEATURES.ENCODING_TYPE</code>.
     */
    public final TableField<Record, String> colEncodingType = createField(DSL.name(NAME_COL_ENCODINGTYPE), SQLDataType.CLOB, this, "");

    /**
     * The column <code>public.FEATURES.FEATURE</code>.
     */
    public final TableField<Record, String> colFeature = createField(DSL.name(NAME_COL_FEATURE), SQLDataType.CLOB, this, "");

    /**
     * The column <code>public.FEATURES.GEOM</code>.
     */
    public final TableField<Record, Geometry> colGeom = createField(DSL.name(NAME_COL_GEOM), DefaultDataType.getDefaultDataType(TYPE_GEOMETRY), this, "", new PostGisGeometryBinding());

    /**
     * The column <code>public.FEATURES.NAME</code>.
     */
    public final TableField<Record, String> colName = createField(DSL.name(NAME_COL_NAME), SQLDataType.CLOB.defaultValue(DSL.field("'no name'::text", SQLDataType.CLOB)), this, "");

    /**
     * The column <code>public.FEATURES.PROPERTIES</code>.
     */
    public final TableField<Record, JsonValue> colProperties = createField(DSL.name(NAME_COL_PROPERTIES), DefaultDataType.getDefaultDataType(TYPE_JSONB), this, "", new JsonBinding());

    /**
     * The column <code>public.FEATURES.ID</code>.
     */
    public final TableField<Record, J> colId = createField(DSL.name(NAME_COL_ID), getIdType(), this);

    private final PluginCoreModel pluginCoreModel;

    /**
     * Create a <code>public.FEATURES</code> table reference.
     *
     * @param idType The (SQL)DataType of the Id columns used in the actual
     * database.
     * @param pluginCoreModel the coreModel plugin this table belongs to.
     */
    public TableImpFeatures(DataType<J> idType, PluginCoreModel pluginCoreModel) {
        super(idType, DSL.name(NAME_TABLE), null);
        this.pluginCoreModel = pluginCoreModel;
    }

    private TableImpFeatures(Name alias, TableImpFeatures<J> aliased, PluginCoreModel pluginCoreModel) {
        super(aliased.getIdType(), alias, aliased);
        this.pluginCoreModel = pluginCoreModel;
    }

    @Override
    public void initRelations() {
        final TableCollection<J> tables = getTables();
        final TableImpObservations<J> observationsTable = tables.getTableForClass(TableImpObservations.class);
        registerRelation(new RelationOneToMany<>(this, observationsTable, pluginCoreModel.etObservation, true)
                .setSourceFieldAccessor(TableImpFeatures::getId)
                .setTargetFieldAccessor(TableImpObservations::getFeatureId)
        );
    }

    @Override
    public void initProperties(final EntityFactories<J> entityFactories) {
        final IdManager idManager = entityFactories.getIdManager();
        pfReg.addEntryId(idManager, TableImpFeatures::getId);
        pfReg.addEntryString(pluginCoreModel.epName, table -> table.colName);
        pfReg.addEntryString(pluginCoreModel.epDescription, table -> table.colDescription);
        pfReg.addEntryString(ModelRegistry.EP_ENCODINGTYPE, table -> table.colEncodingType);
        pfReg.addEntry(pluginCoreModel.epFeature,
                new ConverterRecordDeflt<>(
                        (TableImpFeatures<J> table, Record tuple, Entity entity, DataSize dataSize) -> {
                            String encodingType = getFieldOrNull(tuple, table.colEncodingType);
                            String locationString = tuple.get(table.colFeature);
                            dataSize.increase(locationString == null ? 0 : locationString.length());
                            entity.setProperty(pluginCoreModel.epFeature, Utils.locationFromEncoding(encodingType, locationString));
                        },
                        (table, entity, insertFields) -> {
                            Object feature = entity.getProperty(pluginCoreModel.epFeature);
                            String encodingType = entity.getProperty(ModelRegistry.EP_ENCODINGTYPE);
                            EntityFactories.insertGeometry(insertFields, table.colFeature, table.colGeom, encodingType, feature);
                        },
                        (table, entity, updateFields, message) -> {
                            Object feature = entity.getProperty(pluginCoreModel.epFeature);
                            String encodingType = entity.getProperty(ModelRegistry.EP_ENCODINGTYPE);
                            EntityFactories.insertGeometry(updateFields, table.colFeature, table.colGeom, encodingType, feature);
                            message.addField(pluginCoreModel.epFeature);
                        }),
                new NFP<>("j", table -> table.colFeature));
        pfReg.addEntryNoSelect(pluginCoreModel.epFeature, "g", table -> table.colGeom);
        pfReg.addEntryMap(ModelRegistry.EP_PROPERTIES, table -> table.colProperties);
        pfReg.addEntry(pluginCoreModel.npObservations, TableImpFeatures::getId, idManager);
    }

    @Override
    public void delete(PostgresPersistenceManager<J> pm, J entityId) throws NoSuchEntityException {
        super.delete(pm, entityId);

        // Delete references to the FoI in the Locations table.
        TableImpLocations<J> tLoc = getTables().getTableForClass(TableImpLocations.class);
        pm.getDslContext()
                .update(tLoc)
                .set(tLoc.getGenFoiId(), (J) null)
                .where(tLoc.getGenFoiId().eq(entityId))
                .execute();
    }

    @Override
    public EntityType getEntityType() {
        return pluginCoreModel.etFeatureOfInterest;
    }

    @Override
    public TableField<Record, J> getId() {
        return colId;
    }

    @Override
    public TableImpFeatures<J> as(Name alias) {
        return new TableImpFeatures<>(alias, this, pluginCoreModel).initCustomFields();
    }

    @Override
    public TableImpFeatures<J> getThis() {
        return this;
    }

}
