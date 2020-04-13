package de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables;

import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.relations.RelationOneToMany;
import org.jooq.Field;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;

public abstract class AbstractTableTaskingCapabilities<J extends Comparable> extends StaTableAbstract<J> {

    private static final long serialVersionUID = -1460005950;

    /**
     * The column <code>public.TASKINGCAPABILITIES.DESCRIPTION</code>.
     */
    public final TableField<Record, String> description = createField(DSL.name("DESCRIPTION"), SQLDataType.CLOB, this, "");

    /**
     * The column <code>public.TASKINGCAPABILITIES.NAME</code>.
     */
    public final TableField<Record, String> name = createField(DSL.name("NAME"), SQLDataType.CLOB.defaultValue(DSL.field("'no name'::text", SQLDataType.CLOB)), this, "");

    /**
     * The column <code>public.TASKINGCAPABILITIES.PROPERTIES</code>.
     */
    public final TableField<Record, String> properties = createField(DSL.name("PROPERTIES"), SQLDataType.CLOB, this, "");

    /**
     * The column <code>public.TASKINGCAPABILITIES.PROPERTIES</code>.
     */
    public final TableField<Record, String> taskingParameters = createField(DSL.name("TASKING_PARAMETERS"), SQLDataType.CLOB, this, "");

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
                new RelationOneToMany<>(this, tables.tableThings, EntityType.THING)
                        .setSourceFieldAccessor(AbstractTableTaskingCapabilities::getThingId)
                        .setTargetFieldAccessor(AbstractTableThings::getId)
        );

        registerRelation(
                new RelationOneToMany<>(this, tables.tableActuators, EntityType.ACTUATOR)
                        .setSourceFieldAccessor(AbstractTableTaskingCapabilities::getActuatorId)
                        .setTargetFieldAccessor(AbstractTableActuators::getId)
        );

        registerRelation(
                new RelationOneToMany<>(this, tables.tableTasks, EntityType.TASK, true)
                        .setSourceFieldAccessor(AbstractTableTaskingCapabilities::getId)
                        .setTargetFieldAccessor(AbstractTableTasks::getTaskingCapabilityId)
        );
    }

    @Override
    public abstract TableField<Record, J> getId();

    public abstract TableField<Record, J> getActuatorId();

    public abstract TableField<Record, J> getThingId();

    @Override
    public abstract AbstractTableTaskingCapabilities<J> as(String alias);

    @Override
    public abstract AbstractTableTaskingCapabilities<J> as(Name as);

}
