package de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables;

import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.persistence.IdManager;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.bindings.JsonBinding;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.bindings.JsonValue;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.factories.EntityFactories;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.relations.RelationOneToMany;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain;
import org.jooq.DataType;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultDataType;
import org.jooq.impl.SQLDataType;

public class AbstractTableSensors<J extends Comparable> extends StaTableAbstract<J, AbstractTableSensors<J>> {

    private static final long serialVersionUID = 1850108682;

    private static AbstractTableSensors INSTANCE;
    private static DataType INSTANCE_ID_TYPE;

    public static <J extends Comparable> AbstractTableSensors<J> getInstance(DataType<J> idType) {
        if (INSTANCE == null) {
            INSTANCE_ID_TYPE = idType;
            INSTANCE = new AbstractTableSensors(INSTANCE_ID_TYPE);
            return INSTANCE;
        }
        if (INSTANCE_ID_TYPE.equals(idType)) {
            return INSTANCE;
        }
        return new AbstractTableSensors<>(idType);
    }

    /**
     * The column <code>public.SENSORS.DESCRIPTION</code>.
     */
    public final TableField<Record, String> colDescription = createField(DSL.name("DESCRIPTION"), SQLDataType.CLOB, this);

    /**
     * The column <code>public.SENSORS.ENCODING_TYPE</code>.
     */
    public final TableField<Record, String> colEncodingType = createField(DSL.name("ENCODING_TYPE"), SQLDataType.CLOB, this);

    /**
     * The column <code>public.SENSORS.METADATA</code>.
     */
    public final TableField<Record, String> colMetadata = createField(DSL.name("METADATA"), SQLDataType.CLOB, this);

    /**
     * The column <code>public.SENSORS.NAME</code>.
     */
    public final TableField<Record, String> colName = createField(DSL.name("NAME"), SQLDataType.CLOB.defaultValue(DSL.field("'no name'::text", SQLDataType.CLOB)), this);

    /**
     * The column <code>public.SENSORS.PROPERTIES</code>.
     */
    public final TableField<Record, JsonValue> colProperties = createField(DSL.name("PROPERTIES"), DefaultDataType.getDefaultDataType(TYPE_JSONB), this, "", new JsonBinding());

    /**
     * The column <code>public.SENSORS.ID</code>.
     */
    public final TableField<Record, J> colId = createField(DSL.name("ID"), getIdType(), this);

    /**
     * Create a <code>public.SENSORS</code> table reference
     */
    private AbstractTableSensors(DataType<J> idType) {
        super(idType, DSL.name("SENSORS"), null);
    }

    private AbstractTableSensors(Name alias, AbstractTableSensors<J> aliased) {
        super(aliased.getIdType(), alias, aliased);
    }

    @Override
    public void initRelations() {
        final TableCollection<J> tables = getTables();
        registerRelation(
                new RelationOneToMany<>(this, AbstractTableDatastreams.getInstance(getIdType()), EntityType.DATASTREAM, true)
                        .setSourceFieldAccessor(AbstractTableSensors::getId)
                        .setTargetFieldAccessor(AbstractTableDatastreams::getSensorId)
        );

        registerRelation(
                new RelationOneToMany<>(this, AbstractTableMultiDatastreams.getInstance(getIdType()), EntityType.MULTI_DATASTREAM, true)
                        .setSourceFieldAccessor(AbstractTableSensors::getId)
                        .setTargetFieldAccessor(AbstractTableMultiDatastreams::getSensorId)
        );
    }

    @Override
    public void initProperties(final EntityFactories<J> entityFactories) {
        final IdManager idManager = entityFactories.idManager;
        pfReg.addEntryId(idManager, AbstractTableSensors::getId);
        pfReg.addEntryString(EntityPropertyMain.NAME, table -> table.colName);
        pfReg.addEntryString(EntityPropertyMain.DESCRIPTION, table -> table.colDescription);
        pfReg.addEntryString(EntityPropertyMain.ENCODINGTYPE, table -> table.colEncodingType);
        pfReg.addEntryString(EntityPropertyMain.METADATA, table -> table.colMetadata);
        pfReg.addEntryMap(EntityPropertyMain.PROPERTIES, table -> table.colProperties);
        pfReg.addEntry(NavigationPropertyMain.DATASTREAMS, AbstractTableSensors::getId, idManager);
        pfReg.addEntry(NavigationPropertyMain.MULTIDATASTREAMS, AbstractTableSensors::getId, idManager);
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.SENSOR;
    }

    @Override
    public TableField<Record, J> getId() {
        return colId;
    }

    @Override
    public AbstractTableSensors<J> as(Name alias) {
        return new AbstractTableSensors<>(alias, this);
    }

    @Override
    public AbstractTableSensors<J> as(String alias) {
        return new AbstractTableSensors<>(DSL.name(alias), this);
    }

    @Override
    public AbstractTableSensors<J> getThis() {
        return this;
    }

}
