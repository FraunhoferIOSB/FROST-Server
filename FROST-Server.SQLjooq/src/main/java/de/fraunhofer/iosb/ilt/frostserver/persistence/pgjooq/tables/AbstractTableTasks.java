package de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables;

import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.relations.RelationOneToMany;
import java.time.OffsetDateTime;
import org.jooq.Field;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;

public abstract class AbstractTableTasks<J extends Comparable> extends StaTableAbstract<J> {

    private static final long serialVersionUID = -1457801967;

    /**
     * The column <code>public.TASKS.CREATION_TIME</code>.
     */
    public final TableField<Record, OffsetDateTime> creationTime = createField(DSL.name("CREATION_TIME"), SQLDataType.TIMESTAMPWITHTIMEZONE, this, "");

    /**
     * The column <code>public.TASKINGCAPABILITIES.PROPERTIES</code>.
     */
    public final TableField<Record, String> taskingParameters = createField(DSL.name("TASKING_PARAMETERS"), SQLDataType.CLOB, this, "");

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
    public abstract TableField<Record, J> getId();

    public abstract TableField<Record, J> getTaskingCapabilityId();

    @Override
    public abstract AbstractTableTasks<J> as(Name as);

    @Override
    public abstract AbstractTableTasks<J> as(String alias);

}
