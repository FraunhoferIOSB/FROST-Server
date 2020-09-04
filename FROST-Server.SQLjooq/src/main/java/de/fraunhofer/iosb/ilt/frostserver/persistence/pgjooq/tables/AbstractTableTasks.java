package de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables;

import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.Task;
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
import java.time.OffsetDateTime;
import org.jooq.Field;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultDataType;
import org.jooq.impl.SQLDataType;

public abstract class AbstractTableTasks<J extends Comparable> extends StaTableAbstract<J, Task, AbstractTableTasks<J>> {

    private static final long serialVersionUID = -1457801967;

    /**
     * The column <code>public.TASKS.CREATION_TIME</code>.
     */
    public final TableField<Record, OffsetDateTime> colCreationTime = createField(DSL.name("CREATION_TIME"), SQLDataType.TIMESTAMPWITHTIMEZONE, this, "");

    /**
     * The column <code>public.TASKINGCAPABILITIES.PROPERTIES</code>.
     */
    public final TableField<Record, JsonValue> colTaskingParameters = createField(DSL.name("TASKING_PARAMETERS"), DefaultDataType.getDefaultDataType(TYPE_JSONB), this, "", new JsonBinding());

    /**
     * Create a <code>public.TASKS</code> table reference
     */
    protected AbstractTableTasks() {
        this(DSL.name("TASKS"), null);
    }

    protected AbstractTableTasks(Name alias, AbstractTableTasks<J> aliased) {
        this(alias, aliased, null);
    }

    protected AbstractTableTasks(Name alias, AbstractTableTasks<J> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""));
    }

    @Override
    public void initRelations() {
        final TableCollection<J> tables = getTables();
        registerRelation(
                new RelationOneToMany<>(this, tables.getTableTaskingCapabilities(), EntityType.TASKINGCAPABILITY)
                        .setSourceFieldAccessor(AbstractTableTasks::getTaskingCapabilityId)
                        .setTargetFieldAccessor(AbstractTableTaskingCapabilities::getId)
        );
    }

    @Override
    public void initProperties(final EntityFactories<J> entityFactories) {
        final IdManager idManager = entityFactories.idManager;
        final PropertyFieldRegistry.PropertySetter<AbstractTableTasks<J>, Task> setterId = (AbstractTableTasks<J> table, Record tuple, Task entity, DataSize dataSize) -> {
            entity.setId(idManager.fromObject(tuple.get(table.getId())));
        };
        pfReg.addEntry(EntityPropertyMain.ID, AbstractTableTasks::getId, setterId);
        pfReg.addEntry(EntityPropertyMain.SELFLINK, AbstractTableTasks::getId,
                (AbstractTableTasks<J> table, Record tuple, Task entity, DataSize dataSize) -> {
                    entity.setId(idManager.fromObject(tuple.get(table.getId())));
                });
        pfReg.addEntry(EntityPropertyMain.CREATIONTIME, table -> table.colCreationTime,
                (AbstractTableTasks<J> table, Record tuple, Task entity, DataSize dataSize) -> {
                    entity.setCreationTime(Utils.instantFromTime(tuple.get(table.colCreationTime)));
                });
        pfReg.addEntry(EntityPropertyMain.TASKINGPARAMETERS, table -> table.colTaskingParameters,
                (AbstractTableTasks<J> table, Record tuple, Task entity, DataSize dataSize) -> {
                    JsonValue taskingParams = Utils.getFieldJsonValue(tuple, table.colTaskingParameters);
                    dataSize.increase(taskingParams.getStringLength());
                    entity.setTaskingParameters(taskingParams.getMapValue());
                });
        pfReg.addEntry(NavigationPropertyMain.TASKINGCAPABILITY, AbstractTableTasks::getTaskingCapabilityId,
                (AbstractTableTasks<J> table, Record tuple, Task entity, DataSize dataSize) -> {
                    entity.setTaskingCapability(entityFactories.taskingCapabilityFromId(tuple.get(table.getTaskingCapabilityId())));
                });
    }

    @Override
    public Task newEntity() {
        return new Task();
    }

    @Override
    public abstract TableField<Record, J> getId();

    public abstract TableField<Record, J> getTaskingCapabilityId();

    @Override
    public abstract AbstractTableTasks<J> as(Name as);

    @Override
    public abstract AbstractTableTasks<J> as(String alias);

    @Override
    public AbstractTableTasks<J> getThis() {
        return this;
    }

}
