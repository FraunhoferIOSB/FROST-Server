package de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables;

import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.relations.RelationManyToManyOrdered;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.relations.RelationOneToMany;
import org.jooq.Field;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;

public abstract class AbstractTableObsProperties<J extends Comparable> extends StaTableAbstract<J> {

    private static final long serialVersionUID = -1873692390;

    /**
     * The column <code>public.OBS_PROPERTIES.NAME</code>.
     */
    public final TableField<Record, String> name = createField(DSL.name("NAME"), SQLDataType.CLOB, this, "");

    /**
     * The column <code>public.OBS_PROPERTIES.DEFINITION</code>.
     */
    public final TableField<Record, String> definition = createField(DSL.name("DEFINITION"), SQLDataType.CLOB, this, "");

    /**
     * The column <code>public.OBS_PROPERTIES.DESCRIPTION</code>.
     */
    public final TableField<Record, String> description = createField(DSL.name("DESCRIPTION"), SQLDataType.CLOB, this, "");

    /**
     * The column <code>public.OBS_PROPERTIES.PROPERTIES</code>.
     */
    public final TableField<Record, String> properties = createField(DSL.name("PROPERTIES"), SQLDataType.CLOB, this, "");

    /**
     * Create a <code>public.OBS_PROPERTIES</code> table reference
     */
    protected AbstractTableObsProperties() {
        this(DSL.name("OBS_PROPERTIES"), null);
    }

    protected AbstractTableObsProperties(Name alias, AbstractTableObsProperties<J> aliased) {
        this(alias, aliased, null);
    }

    protected AbstractTableObsProperties(Name alias, AbstractTableObsProperties<J> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""));
    }

    @Override
    public void initRelations() {
        final TableCollection<J> tables = getTables();
        registerRelation(
                new RelationOneToMany<>(this, tables.tableDatastreams, EntityType.DATASTREAM, true)
                        .setSourceFieldAccessor(AbstractTableObsProperties::getId)
                        .setTargetFieldAccessor(AbstractTableDatastreams::getObsPropertyId)
        );

        registerRelation(
                new RelationManyToManyOrdered<J, AbstractTableObsProperties<J>, AbstractTableMultiDatastreamsObsProperties<J>, Integer, AbstractTableMultiDatastreams<J>>(
                        this,
                        tables.tableMultiDatastreamsObsProperties,
                        tables.tableMultiDatastreams,
                        EntityType.MULTIDATASTREAM)
                        .setSourceFieldAcc(AbstractTableObsProperties::getId)
                        .setSourceLinkFieldAcc(AbstractTableMultiDatastreamsObsProperties::getObsPropertyId)
                        .setTargetLinkFieldAcc(AbstractTableMultiDatastreamsObsProperties::getMultiDatastreamId)
                        .setTargetFieldAcc(AbstractTableMultiDatastreams::getId)
                        .setOrderFieldAcc((AbstractTableMultiDatastreamsObsProperties<J> table) -> table.rank)
        );
    }

    @Override
    public abstract TableField<Record, J> getId();

    @Override
    public abstract AbstractTableObsProperties<J> as(Name as);

    @Override
    public abstract AbstractTableObsProperties<J> as(String alias);

}
