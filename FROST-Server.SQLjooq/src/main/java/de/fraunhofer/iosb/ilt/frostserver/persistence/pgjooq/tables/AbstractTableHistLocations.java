package de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables;

import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.relations.RelationManyToMany;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.relations.RelationOneToMany;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.PropertyFieldRegistry;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain;
import java.time.OffsetDateTime;
import org.jooq.Field;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;

public abstract class AbstractTableHistLocations<J extends Comparable> extends StaTableAbstract<J, AbstractTableHistLocations<J>> {

    private static final long serialVersionUID = -1457801967;

    /**
     * The column <code>public.HIST_LOCATIONS.TIME</code>.
     */
    public final TableField<Record, OffsetDateTime> time = createField(DSL.name("TIME"), SQLDataType.TIMESTAMPWITHTIMEZONE, this, "");

    /**
     * Create a <code>public.HIST_LOCATIONS</code> table reference
     */
    protected AbstractTableHistLocations() {
        this(DSL.name("HIST_LOCATIONS"), null);
    }

    protected AbstractTableHistLocations(Name alias, AbstractTableHistLocations<J> aliased) {
        this(alias, aliased, null);
    }

    protected AbstractTableHistLocations(Name alias, AbstractTableHistLocations<J> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""));
    }

    @Override
    public void initRelations() {
        final TableCollection<J> tables = getTables();
        registerRelation(
                new RelationOneToMany<>(this, tables.getTableThings(), EntityType.THING)
                        .setSourceFieldAccessor(AbstractTableHistLocations::getThingId)
                        .setTargetFieldAccessor(AbstractTableThings::getId)
        );

        registerRelation(
                new RelationManyToMany<>(this, tables.getTableLocationsHistLocations(), tables.getTableLocations(), EntityType.LOCATION)
                        .setSourceFieldAcc(AbstractTableHistLocations::getId)
                        .setSourceLinkFieldAcc(AbstractTableLocationsHistLocations::getHistLocationId)
                        .setTargetLinkFieldAcc(AbstractTableLocationsHistLocations::getLocationId)
                        .setTargetFieldAcc(AbstractTableLocations::getId)
        );
    }

    @Override
    public void initProperties() {
        pfReg = new PropertyFieldRegistry<>(this);
        pfReg.addEntry(EntityPropertyMain.ID, AbstractTableHistLocations::getId);
        pfReg.addEntry(EntityPropertyMain.SELFLINK, AbstractTableHistLocations::getId);
        pfReg.addEntry(EntityPropertyMain.TIME, table -> table.time);
        pfReg.addEntry(NavigationPropertyMain.THING, AbstractTableHistLocations::getThingId);
        pfReg.addEntry(NavigationPropertyMain.LOCATIONS, AbstractTableHistLocations::getId);
    }

    @Override
    public abstract TableField<Record, J> getId();

    public abstract TableField<Record, J> getThingId();

    @Override
    public abstract AbstractTableHistLocations<J> as(Name as);

    @Override
    public abstract AbstractTableHistLocations<J> as(String alias);

    @Override
    public AbstractTableHistLocations<J> getThis() {
        return this;
    }

}
