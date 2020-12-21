package de.fraunhofer.iosb.ilt.frostserver.plugin.coremodel;

import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.ModelRegistry;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.TimeInstant;
import de.fraunhofer.iosb.ilt.frostserver.persistence.IdManager;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.PostgresPersistenceManager;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.factories.EntityFactories;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.relations.RelationManyToMany;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.relations.RelationOneToMany;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.StaTableAbstract;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.TableCollection;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.PropertyFieldRegistry;
import de.fraunhofer.iosb.ilt.frostserver.util.Constants;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.IncompleteEntityException;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.NoSuchEntityException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Collections;
import org.jooq.DSLContext;
import org.jooq.DataType;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TableImpHistLocations<J extends Comparable> extends StaTableAbstract<J, TableImpHistLocations<J>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(TableImpHistLocations.class.getName());
    private static final long serialVersionUID = -1457801967;

    /**
     * The column <code>public.HIST_LOCATIONS.TIME</code>.
     */
    public final TableField<Record, OffsetDateTime> time = createField(DSL.name("TIME"), SQLDataType.TIMESTAMPWITHTIMEZONE, this);

    /**
     * The column <code>public.HIST_LOCATIONS.ID</code>.
     */
    public final TableField<Record, J> colId = createField(DSL.name("ID"), getIdType(), this);

    /**
     * The column <code>public.HIST_LOCATIONS.THING_ID</code>.
     */
    public final TableField<Record, J> colThingId = createField(DSL.name("THING_ID"), getIdType(), this);

    private final PluginCoreModel pluginCoreModel;

    /**
     * Create a <code>public.HIST_LOCATIONS</code> table reference
     */
    public TableImpHistLocations(DataType<J> idType, PluginCoreModel pluginCoreModel) {
        super(idType, DSL.name("HIST_LOCATIONS"), null);
        this.pluginCoreModel = pluginCoreModel;
    }

    private TableImpHistLocations(Name alias, TableImpHistLocations<J> aliased, PluginCoreModel pluginCoreModel) {
        super(aliased.getIdType(), alias, aliased);
        this.pluginCoreModel = pluginCoreModel;
    }

    @Override
    public void initRelations() {
        final TableCollection<J> tables = getTables();
        TableImpThings<J> tableThings = tables.getTableForClass(TableImpThings.class);
        registerRelation(new RelationOneToMany<>(this, tableThings, pluginCoreModel.THING)
                .setSourceFieldAccessor(TableImpHistLocations::getThingId)
                .setTargetFieldAccessor(TableImpThings::getId)
        );
        final TableImpLocationsHistLocations<J> tableLocHistLoc = tables.getTableForClass(TableImpLocationsHistLocations.class);
        final TableImpLocations<J> tableLocations = tables.getTableForClass(TableImpLocations.class);
        registerRelation(new RelationManyToMany<>(this, tableLocHistLoc, tableLocations, pluginCoreModel.LOCATION)
                .setSourceFieldAcc(TableImpHistLocations::getId)
                .setSourceLinkFieldAcc(TableImpLocationsHistLocations::getHistLocationId)
                .setTargetLinkFieldAcc(TableImpLocationsHistLocations::getLocationId)
                .setTargetFieldAcc(TableImpLocations::getId)
        );
    }

    @Override
    public void initProperties(final EntityFactories<J> entityFactories) {
        final IdManager idManager = entityFactories.getIdManager();
        pfReg.addEntryId(idManager, TableImpHistLocations::getId);
        pfReg.addEntry(pluginCoreModel.EP_TIME, table -> table.time,
                new PropertyFieldRegistry.ConverterTimeInstant<>(pluginCoreModel.EP_TIME, table -> table.time)
        );
        pfReg.addEntry(pluginCoreModel.NP_THING, TableImpHistLocations::getThingId, idManager);
        pfReg.addEntry(pluginCoreModel.NP_LOCATIONS, TableImpHistLocations::getId, idManager);
    }

    @Override
    public boolean insertIntoDatabase(PostgresPersistenceManager<J> pm, Entity histLoc) throws NoSuchEntityException, IncompleteEntityException {
        super.insertIntoDatabase(pm, histLoc);
        EntityFactories<J> entityFactories = pm.getEntityFactories();
        Entity thing = histLoc.getProperty(pluginCoreModel.NP_THING);
        J thingId = (J) thing.getId().getValue();
        DSLContext dslContext = pm.getDslContext();
        TableImpHistLocations<J> thl = getTables().getTableForClass(TableImpHistLocations.class);

        final TimeInstant hlTime = histLoc.getProperty(pluginCoreModel.EP_TIME);
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
            TableImpThingsLocations<J> qtl = getTables().getTableForClass(TableImpThingsLocations.class);
            long count = dslContext
                    .delete(qtl)
                    .where(qtl.getThingId().eq(thingId))
                    .execute();
            LOGGER.debug(EntityFactories.UNLINKED_L_FROM_T, count, thingId);

            // Link new locations to Thing.
            for (Entity l : histLoc.getProperty(pluginCoreModel.NP_LOCATIONS)) {
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
        return pluginCoreModel.HISTORICAL_LOCATION;
    }

    @Override
    public TableField<Record, J> getId() {
        return colId;
    }

    public TableField<Record, J> getThingId() {
        return colThingId;
    }

    @Override
    public TableImpHistLocations<J> as(Name alias) {
        return new TableImpHistLocations<>(alias, this, pluginCoreModel);
    }

    @Override
    public TableImpHistLocations<J> as(String alias) {
        return new TableImpHistLocations<>(DSL.name(alias), this, pluginCoreModel);
    }

    @Override
    public TableImpHistLocations<J> getThis() {
        return this;
    }

}
