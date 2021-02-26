package de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables;

import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.Thing;
import de.fraunhofer.iosb.ilt.frostserver.persistence.IdManager;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.bindings.JsonBinding;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.bindings.JsonValue;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.factories.EntityFactories;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.relations.RelationManyToMany;
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

public abstract class AbstractTableThings<J extends Comparable> extends StaTableAbstract<J, Thing, AbstractTableThings<J>> {

    private static final long serialVersionUID = -729589982;

    /**
     * The column <code>public.THINGS.DESCRIPTION</code>.
     */
    public final TableField<Record, String> colDescription = createField(DSL.name("DESCRIPTION"), SQLDataType.CLOB, this, "");

    /**
     * The column <code>public.THINGS.PROPERTIES</code>.
     */
    public final TableField<Record, JsonValue> colProperties = createField(DSL.name("PROPERTIES"), DefaultDataType.getDefaultDataType(TYPE_JSONB), this, "", new JsonBinding());

    /**
     * The column <code>public.THINGS.NAME</code>.
     */
    public final TableField<Record, String> colName = createField(DSL.name("NAME"), SQLDataType.CLOB.defaultValue(DSL.field("'no name'::text", SQLDataType.CLOB)), this, "");

    /**
     * Create a <code>public.THINGS</code> table reference
     */
    protected AbstractTableThings() {
        this(DSL.name("THINGS"), null);
    }

    protected AbstractTableThings(Name alias, AbstractTableThings<J> aliased) {
        this(alias, aliased, null);
    }

    protected AbstractTableThings(Name alias, AbstractTableThings<J> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""));
    }

    @Override
    public void initRelations() {
        final TableCollection<J> tables = getTables();
        registerRelation(
                new RelationOneToMany<>(this, tables.getTableDatastreams(), EntityType.DATASTREAM, true)
                        .setSourceFieldAccessor(AbstractTableThings::getId)
                        .setTargetFieldAccessor(AbstractTableDatastreams::getThingId)
        );

        registerRelation(
                new RelationOneToMany<>(this, tables.getTableMultiDatastreams(), EntityType.MULTIDATASTREAM, true)
                        .setSourceFieldAccessor(AbstractTableThings::getId)
                        .setTargetFieldAccessor(AbstractTableMultiDatastreams::getThingId)
        );

        registerRelation(
                new RelationOneToMany<>(this, tables.getTableTaskingCapabilities(), EntityType.TASKINGCAPABILITY, true)
                        .setSourceFieldAccessor(AbstractTableThings::getId)
                        .setTargetFieldAccessor(AbstractTableTaskingCapabilities::getThingId)
        );

        registerRelation(
                new RelationOneToMany<>(this, tables.getTableHistLocations(), EntityType.HISTORICALLOCATION, true)
                        .setSourceFieldAccessor(AbstractTableThings::getId)
                        .setTargetFieldAccessor(AbstractTableHistLocations::getThingId)
        );

        registerRelation(new RelationManyToMany<>(this, tables.getTableThingsLocations(), tables.getTableLocations(), EntityType.LOCATION)
                .setSourceFieldAcc(AbstractTableThings::getId)
                .setSourceLinkFieldAcc(AbstractTableThingsLocations::getThingId)
                .setTargetLinkFieldAcc(AbstractTableThingsLocations::getLocationId)
                .setTargetFieldAcc(AbstractTableLocations::getId)
        );
    }

    @Override
    public void initProperties(final EntityFactories<J> entityFactories) {
        final IdManager idManager = entityFactories.idManager;
        final PropertyFieldRegistry.PropertySetter<AbstractTableThings<J>, Thing> setterId
                = (AbstractTableThings<J> table, Record tuple, Thing entity, DataSize dataSize)
                -> entity.setId(idManager.fromObject(tuple.get(table.getId())));
        pfReg.addEntry(EntityPropertyMain.ID, AbstractTableThings::getId, setterId);
        pfReg.addEntry(EntityPropertyMain.SELFLINK, AbstractTableThings::getId, setterId);
        pfReg.addEntry(
                EntityPropertyMain.NAME,
                table -> table.colName,
                (AbstractTableThings<J> table, Record tuple, Thing entity, DataSize dataSize) -> entity.setName(tuple.get(table.colName))
        );
        pfReg.addEntry(
                EntityPropertyMain.DESCRIPTION,
                table -> table.colDescription,
                (AbstractTableThings<J> table, Record tuple, Thing entity, DataSize dataSize) -> entity.setDescription(tuple.get(table.colDescription))
        );
        pfReg.addEntry(EntityPropertyMain.PROPERTIES, table -> table.colProperties,
                (AbstractTableThings<J> table, Record tuple, Thing entity, DataSize dataSize) -> {
                    JsonValue props = Utils.getFieldJsonValue(tuple, table.colProperties);
                    dataSize.increase(props.getStringLength());
                    entity.setProperties(props.getMapValue());
                });
        pfReg.addEntry(NavigationPropertyMain.DATASTREAMS, AbstractTableThings::getId, setterId);
        pfReg.addEntry(NavigationPropertyMain.HISTORICALLOCATIONS, AbstractTableThings::getId, setterId);
        pfReg.addEntry(NavigationPropertyMain.LOCATIONS, AbstractTableThings::getId, setterId);
        pfReg.addEntry(NavigationPropertyMain.MULTIDATASTREAMS, AbstractTableThings::getId, setterId);
        pfReg.addEntry(NavigationPropertyMain.TASKINGCAPABILITIES, AbstractTableThings::getId, setterId);
    }

    @Override
    public Thing newEntity() {
        return new Thing();
    }

    @Override
    public abstract TableField<Record, J> getId();

    @Override
    public abstract AbstractTableThings<J> as(Name as);

    @Override
    public abstract AbstractTableThings<J> as(String alias);

    @Override
    public AbstractTableThings<J> getThis() {
        return this;
    }

}
