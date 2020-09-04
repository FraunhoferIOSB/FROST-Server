package de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables;

import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.TaskingCapability;
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

public abstract class AbstractTableTaskingCapabilities<J extends Comparable> extends StaTableAbstract<J, TaskingCapability, AbstractTableTaskingCapabilities<J>> {

    private static final long serialVersionUID = -1460005950;

    /**
     * The column <code>public.TASKINGCAPABILITIES.DESCRIPTION</code>.
     */
    public final TableField<Record, String> colDescription = createField(DSL.name("DESCRIPTION"), SQLDataType.CLOB, this, "");

    /**
     * The column <code>public.TASKINGCAPABILITIES.NAME</code>.
     */
    public final TableField<Record, String> colName = createField(DSL.name("NAME"), SQLDataType.CLOB.defaultValue(DSL.field("'no name'::text", SQLDataType.CLOB)), this, "");

    /**
     * The column <code>public.TASKINGCAPABILITIES.PROPERTIES</code>.
     */
    public final TableField<Record, JsonValue> colProperties = createField(DSL.name("PROPERTIES"), DefaultDataType.getDefaultDataType(TYPE_JSONB), this, "", new JsonBinding());

    /**
     * The column <code>public.TASKINGCAPABILITIES.TASKING_PARAMETERS</code>.
     */
    public final TableField<Record, JsonValue> colTaskingParameters = createField(DSL.name("TASKING_PARAMETERS"), DefaultDataType.getDefaultDataType(TYPE_JSONB), this, "", new JsonBinding());

    /**
     * Create a <code>public.TASKINGCAPABILITIES</code> table reference
     */
    protected AbstractTableTaskingCapabilities() {
        this(DSL.name("TASKINGCAPABILITIES"), null);
    }

    protected AbstractTableTaskingCapabilities(Name alias, AbstractTableTaskingCapabilities<J> aliased) {
        this(alias, aliased, null);
    }

    protected AbstractTableTaskingCapabilities(Name alias, AbstractTableTaskingCapabilities<J> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""));
    }

    @Override
    public void initRelations() {
        final TableCollection<J> tables = getTables();
        registerRelation(
                new RelationOneToMany<>(this, tables.getTableThings(), EntityType.THING)
                        .setSourceFieldAccessor(AbstractTableTaskingCapabilities::getThingId)
                        .setTargetFieldAccessor(AbstractTableThings::getId)
        );

        registerRelation(
                new RelationOneToMany<>(this, tables.getTableActuators(), EntityType.ACTUATOR)
                        .setSourceFieldAccessor(AbstractTableTaskingCapabilities::getActuatorId)
                        .setTargetFieldAccessor(AbstractTableActuators::getId)
        );

        registerRelation(
                new RelationOneToMany<>(this, tables.getTableTasks(), EntityType.TASK, true)
                        .setSourceFieldAccessor(AbstractTableTaskingCapabilities::getId)
                        .setTargetFieldAccessor(AbstractTableTasks::getTaskingCapabilityId)
        );
    }

    @Override
    public void initProperties(final EntityFactories<J> entityFactories) {
        final IdManager idManager = entityFactories.idManager;
        final PropertyFieldRegistry.PropertySetter<AbstractTableTaskingCapabilities<J>, TaskingCapability> setterId = (AbstractTableTaskingCapabilities<J> table, Record tuple, TaskingCapability entity, DataSize dataSize) -> {
            entity.setId(idManager.fromObject(tuple.get(table.getId())));
        };
        pfReg.addEntry(EntityPropertyMain.ID, AbstractTableTaskingCapabilities::getId, setterId);
        pfReg.addEntry(EntityPropertyMain.SELFLINK, AbstractTableTaskingCapabilities::getId,
                (AbstractTableTaskingCapabilities<J> table, Record tuple, TaskingCapability entity, DataSize dataSize) -> {
                    entity.setId(idManager.fromObject(tuple.get(table.getId())));
                });
        pfReg.addEntry(EntityPropertyMain.NAME, table -> table.colName,
                (AbstractTableTaskingCapabilities<J> table, Record tuple, TaskingCapability entity, DataSize dataSize) -> {
                    entity.setName(tuple.get(table.colName));
                });
        pfReg.addEntry(EntityPropertyMain.DESCRIPTION, table -> table.colDescription,
                (AbstractTableTaskingCapabilities<J> table, Record tuple, TaskingCapability entity, DataSize dataSize) -> {
                    entity.setDescription(tuple.get(table.colDescription));
                });
        pfReg.addEntry(EntityPropertyMain.PROPERTIES, table -> table.colProperties,
                (AbstractTableTaskingCapabilities<J> table, Record tuple, TaskingCapability entity, DataSize dataSize) -> {
                    JsonValue props = Utils.getFieldJsonValue(tuple, table.colProperties);
                    dataSize.increase(props.getStringLength());
                    entity.setProperties(props.getMapValue());
                });
        pfReg.addEntry(EntityPropertyMain.TASKINGPARAMETERS, table -> table.colTaskingParameters,
                (AbstractTableTaskingCapabilities<J> table, Record tuple, TaskingCapability entity, DataSize dataSize) -> {
                    JsonValue taskingParams = Utils.getFieldJsonValue(tuple, table.colTaskingParameters);
                    dataSize.increase(taskingParams.getStringLength());
                    entity.setTaskingParameters(taskingParams.getMapValue());
                });
        pfReg.addEntry(NavigationPropertyMain.ACTUATOR, AbstractTableTaskingCapabilities::getActuatorId,
                (AbstractTableTaskingCapabilities<J> table, Record tuple, TaskingCapability entity, DataSize dataSize) -> {
                    entity.setActuator(entityFactories.actuatorFromId(tuple.get(table.getActuatorId())));
                });
        pfReg.addEntry(NavigationPropertyMain.THING, AbstractTableTaskingCapabilities::getThingId,
                (AbstractTableTaskingCapabilities<J> table, Record tuple, TaskingCapability entity, DataSize dataSize) -> {
                    entity.setThing(entityFactories.thingFromId(tuple.get(table.getThingId())));
                });
        pfReg.addEntry(NavigationPropertyMain.TASKS, AbstractTableTaskingCapabilities::getId, setterId);
    }

    @Override
    public TaskingCapability newEntity() {
        return new TaskingCapability();
    }

    @Override
    public abstract TableField<Record, J> getId();

    public abstract TableField<Record, J> getActuatorId();

    public abstract TableField<Record, J> getThingId();

    @Override
    public abstract AbstractTableTaskingCapabilities<J> as(String alias);

    @Override
    public abstract AbstractTableTaskingCapabilities<J> as(Name as);

    @Override
    public AbstractTableTaskingCapabilities<J> getThis() {
        return this;
    }

}
