package de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables;

import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.bindings.JsonBinding;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.bindings.JsonValue;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.relations.RelationManyToManyOrdered;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.relations.RelationOneToMany;
import org.jooq.Field;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultDataType;
import org.jooq.impl.SQLDataType;

public abstract class AbstractTableObsProperties<J extends Comparable> extends StaTableAbstract<J> {

    private static final long serialVersionUID = -1873692390;

    /**
     * The column <code>public.OBS_PROPERTIES.NAME</code>.
     */
    public final TableField<Record, String> colName = createField(DSL.name("NAME"), SQLDataType.CLOB, this, "");

    /**
     * The column <code>public.OBS_PROPERTIES.DEFINITION</code>.
     */
    public final TableField<Record, String> colDefinition = createField(DSL.name("DEFINITION"), SQLDataType.CLOB, this, "");

    /**
     * The column <code>public.OBS_PROPERTIES.DESCRIPTION</code>.
     */
    public final TableField<Record, String> colDescription = createField(DSL.name("DESCRIPTION"), SQLDataType.CLOB, this, "");

    /**
     * The column <code>public.OBS_PROPERTIES.PROPERTIES</code>.
     */
    public final TableField<Record, JsonValue> colProperties = createField(DSL.name("PROPERTIES"), DefaultDataType.getDefaultDataType(TYPE_JSONB), this, "", new JsonBinding());

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
                new RelationOneToMany<>(this, tables.getTableDatastreams(), EntityType.DATASTREAM, true)
                        .setSourceFieldAccessor(AbstractTableObsProperties::getId)
                        .setTargetFieldAccessor(AbstractTableDatastreams::getObsPropertyId)
        );

        registerRelation(
                new RelationManyToManyOrdered<J, AbstractTableObsProperties<J>, AbstractTableMultiDatastreamsObsProperties<J>, Integer, AbstractTableMultiDatastreams<J>>(
                        this, tables.getTableMultiDatastreamsObsProperties(), tables.getTableMultiDatastreams(),
                        EntityType.MULTIDATASTREAM)
                        .setSourceFieldAcc(AbstractTableObsProperties::getId)
                        .setSourceLinkFieldAcc(AbstractTableMultiDatastreamsObsProperties::getObsPropertyId)
                        .setTargetLinkFieldAcc(AbstractTableMultiDatastreamsObsProperties::getMultiDatastreamId)
                        .setTargetFieldAcc(AbstractTableMultiDatastreams::getId)
                        .setOrderFieldAcc((AbstractTableMultiDatastreamsObsProperties<J> table) -> table.colRank)
        );
    }

    @Override
    public abstract TableField<Record, J> getId();

    @Override
    public abstract AbstractTableObsProperties<J> as(Name as);

    @Override
    public abstract AbstractTableObsProperties<J> as(String alias);

}
