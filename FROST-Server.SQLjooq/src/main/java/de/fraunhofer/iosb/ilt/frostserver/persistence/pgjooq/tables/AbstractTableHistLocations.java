package de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables;

import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.TimeInstant;
import de.fraunhofer.iosb.ilt.frostserver.persistence.IdManager;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.PostgresPersistenceManager;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.factories.EntityFactories;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.relations.RelationManyToMany;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.relations.RelationOneToMany;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.PropertyFieldRegistry;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.util.Constants;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.IncompleteEntityException;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.NoSuchEntityException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Collections;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractTableHistLocations<J extends Comparable> extends StaTableAbstract<J, AbstractTableHistLocations<J>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractTableHistLocations.class.getName());
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
    public void initProperties(final EntityFactories<J> entityFactories) {
        final IdManager idManager = entityFactories.idManager;
        pfReg.addEntryId(idManager, AbstractTableHistLocations::getId);
        pfReg.addEntry(EntityPropertyMain.TIME, table -> table.time,
                new PropertyFieldRegistry.ConverterTimeInstant<>(EntityPropertyMain.TIME, table -> table.time)
        );
        pfReg.addEntry(NavigationPropertyMain.THING, AbstractTableHistLocations::getThingId, idManager);
        pfReg.addEntry(NavigationPropertyMain.LOCATIONS, AbstractTableHistLocations::getId, idManager);
    }

    @Override
    public boolean insertIntoDatabase(PostgresPersistenceManager<J> pm, Entity histLoc) throws NoSuchEntityException, IncompleteEntityException {
        super.insertIntoDatabase(pm, histLoc);
        EntityFactories<J> entityFactories = pm.getEntityFactories();
        Entity thing = histLoc.getProperty(NavigationPropertyMain.THING);
        J thingId = (J) thing.getId().getValue();
        DSLContext dslContext = pm.getDslContext();
        AbstractTableHistLocations<J> thl = getTables().getTableHistLocations();

        final TimeInstant hlTime = histLoc.getProperty(EntityPropertyMain.TIME);
        OffsetDateTime newTime = OffsetDateTime.ofInstant(Instant.ofEpochMilli(hlTime.getDateTime().getMillis()), Constants.UTC);

        // https://github.com/opengeospatial/sensorthings/issues/30
        // Check the time of the latest HistoricalLocation of our thing.
        // If this time is earlier than our time, set the Locations of our Thing to our Locations.
        Record lastHistLocation = dslContext.select(Collections.emptyList())
                .from(thl)
                .where(thl.getThingId().eq(thingId).and(thl.time.gt(newTime)))
                .orderBy(thl.time.desc())
                .limit(1)
                .fetchOne();
        if (lastHistLocation == null) {
            // We are the newest.
            // Unlink old Locations from Thing.
            AbstractTableThingsLocations<J> qtl = getTables().getTableThingsLocations();
            long count = dslContext
                    .delete(qtl)
                    .where(qtl.getThingId().eq(thingId))
                    .execute();
            LOGGER.debug(EntityFactories.UNLINKED_L_FROM_T, count, thingId);

            // Link new locations to Thing.
            for (Entity l : histLoc.getProperty(NavigationPropertyMain.LOCATIONS)) {
                if (l.getId() == null || !entityFactories.entityExists(pm, l)) {
                    throw new NoSuchEntityException("Location with no id.");
                }
                J locationId = (J) l.getId().getValue();

                dslContext.insertInto(qtl)
                        .set(qtl.getThingId(), thingId)
                        .set(qtl.getLocationId(), locationId)
                        .execute();
                LOGGER.debug(EntityFactories.LINKED_L_TO_T, locationId, thingId);
            }
        }

        return true;
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.HISTORICAL_LOCATION;
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
