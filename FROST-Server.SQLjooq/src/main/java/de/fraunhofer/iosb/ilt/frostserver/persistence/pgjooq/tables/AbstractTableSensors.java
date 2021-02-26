package de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables;

import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.Sensor;
import de.fraunhofer.iosb.ilt.frostserver.persistence.IdManager;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.bindings.JsonBinding;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.bindings.JsonValue;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.factories.EntityFactories;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.relations.RelationOneToMany;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.DataSize;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.PropertyFieldRegistry;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.Utils;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain;
import org.jooq.Field;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultDataType;
import org.jooq.impl.SQLDataType;

public abstract class AbstractTableSensors<J extends Comparable> extends StaTableAbstract<J, Sensor, AbstractTableSensors<J>> {

    private static final long serialVersionUID = 1850108682;

    /**
     * The column <code>public.SENSORS.DESCRIPTION</code>.
     */
    public final TableField<Record, String> colDescription = createField(DSL.name("DESCRIPTION"), SQLDataType.CLOB, this, "");

    /**
     * The column <code>public.SENSORS.ENCODING_TYPE</code>.
     */
    public final TableField<Record, String> colEncodingType = createField(DSL.name("ENCODING_TYPE"), SQLDataType.CLOB, this, "");

    /**
     * The column <code>public.SENSORS.METADATA</code>.
     */
    public final TableField<Record, String> colMetadata = createField(DSL.name("METADATA"), SQLDataType.CLOB, this, "");

    /**
     * The column <code>public.SENSORS.NAME</code>.
     */
    public final TableField<Record, String> colName = createField(DSL.name("NAME"), SQLDataType.CLOB.defaultValue(DSL.field("'no name'::text", SQLDataType.CLOB)), this, "");

    /**
     * The column <code>public.SENSORS.PROPERTIES</code>.
     */
    public final TableField<Record, JsonValue> colProperties = createField(DSL.name("PROPERTIES"), DefaultDataType.getDefaultDataType(TYPE_JSONB), this, "", new JsonBinding());

    /**
     * Create a <code>public.SENSORS</code> table reference
     */
    protected AbstractTableSensors() {
        this(DSL.name("SENSORS"), null);
    }

    protected AbstractTableSensors(Name alias, AbstractTableSensors<J> aliased) {
        this(alias, aliased, null);
    }

    protected AbstractTableSensors(Name alias, AbstractTableSensors<J> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""));
    }

    @Override
    public void initRelations() {
        final TableCollection<J> tables = getTables();
        registerRelation(
                new RelationOneToMany<>(this, tables.getTableDatastreams(), EntityType.DATASTREAM, true)
                        .setSourceFieldAccessor(AbstractTableSensors::getId)
                        .setTargetFieldAccessor(AbstractTableDatastreams::getSensorId)
        );

        registerRelation(
                new RelationOneToMany<>(this, tables.getTableMultiDatastreams(), EntityType.MULTIDATASTREAM, true)
                        .setSourceFieldAccessor(AbstractTableSensors::getId)
                        .setTargetFieldAccessor(AbstractTableMultiDatastreams::getSensorId)
        );
    }

    @Override
    public void initProperties(final EntityFactories<J> entityFactories) {
        final IdManager idManager = entityFactories.idManager;
        final PropertyFieldRegistry.PropertySetter<AbstractTableSensors<J>, Sensor> setterId
                = (AbstractTableSensors<J> table, Record tuple, Sensor entity, DataSize dataSize)
                -> entity.setId(idManager.fromObject(tuple.get(table.getId())));
        pfReg.addEntry(EntityPropertyMain.ID, AbstractTableSensors::getId, setterId);
        pfReg.addEntry(
                EntityPropertyMain.SELFLINK,
                AbstractTableSensors::getId,
                (AbstractTableSensors<J> table, Record tuple, Sensor entity, DataSize dataSize) -> entity.setId(idManager.fromObject(tuple.get(table.getId()))));
        pfReg.addEntry(
                EntityPropertyMain.NAME,
                table -> table.colName,
                (AbstractTableSensors<J> table, Record tuple, Sensor entity, DataSize dataSize) -> entity.setName(tuple.get(table.colName)));
        pfReg.addEntry(
                EntityPropertyMain.DESCRIPTION,
                table -> table.colDescription,
                (AbstractTableSensors<J> table, Record tuple, Sensor entity, DataSize dataSize) -> entity.setDescription(tuple.get(table.colDescription)));
        pfReg.addEntry(
                EntityPropertyMain.ENCODINGTYPE,
                table -> table.colEncodingType,
                (AbstractTableSensors<J> table, Record tuple, Sensor entity, DataSize dataSize) -> entity.setEncodingType(tuple.get(table.colEncodingType)));
        pfReg.addEntry(
                EntityPropertyMain.METADATA,
                table -> table.colMetadata,
                (AbstractTableSensors<J> table, Record tuple, Sensor entity, DataSize dataSize) -> {
                    String metaDataString = tuple.get(table.colMetadata);
                    dataSize.increase(metaDataString == null ? 0 : metaDataString.length());
                    entity.setMetadata(metaDataString);
                });
        pfReg.addEntry(
                EntityPropertyMain.PROPERTIES,
                table -> table.colProperties,
                (AbstractTableSensors<J> table, Record tuple, Sensor entity, DataSize dataSize) -> {
                    JsonValue props = Utils.getFieldJsonValue(tuple, table.colProperties);
                    dataSize.increase(props.getStringLength());
                    entity.setProperties(props.getMapValue());
                });
        pfReg.addEntry(NavigationPropertyMain.DATASTREAMS, AbstractTableSensors::getId, setterId);
        pfReg.addEntry(NavigationPropertyMain.MULTIDATASTREAMS, AbstractTableSensors::getId, setterId);
    }

    @Override
    public Sensor newEntity() {
        return new Sensor();
    }

    @Override
    public abstract TableField<Record, J> getId();

    @Override
    public abstract AbstractTableSensors<J> as(Name as);

    @Override
    public abstract AbstractTableSensors<J> as(String alias);

    @Override
    public AbstractTableSensors<J> getThis() {
        return this;
    }

}
