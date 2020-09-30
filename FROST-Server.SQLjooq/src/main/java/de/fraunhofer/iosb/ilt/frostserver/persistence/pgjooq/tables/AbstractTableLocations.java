package de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables;

import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.Location;
import de.fraunhofer.iosb.ilt.frostserver.persistence.IdManager;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.bindings.JsonBinding;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.bindings.JsonValue;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.bindings.PostGisGeometryBinding;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.factories.EntityFactories;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.fieldwrapper.JsonFieldFactory;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.relations.RelationManyToMany;
import static de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.StaTableAbstract.jsonFieldFromPath;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.DataSize;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.PropertyFieldRegistry.NFP;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.PropertyFieldRegistry.PropertyFields;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.PropertyFieldRegistry.PropertySetter;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.Utils;
import static de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.Utils.getFieldOrNull;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyCustomSelect;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain;
import org.geolatte.geom.Geometry;
import org.jooq.Field;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.UniqueKey;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultDataType;
import org.jooq.impl.Internal;
import org.jooq.impl.SQLDataType;

public abstract class AbstractTableLocations<J extends Comparable> extends StaTableAbstract<J, Location, AbstractTableLocations<J>> {

    private static final long serialVersionUID = -806078255;
    public static final String TABLE_NAME = "LOCATIONS";

    private UniqueKey<Record> primaryKey;

    /**
     * The column <code>public.LOCATIONS.DESCRIPTION</code>.
     */
    public final TableField<Record, String> colDescription = createField(DSL.name("DESCRIPTION"), SQLDataType.CLOB, this, "");

    /**
     * The column <code>public.LOCATIONS.ENCODING_TYPE</code>.
     */
    public final TableField<Record, String> colEncodingType = createField(DSL.name("ENCODING_TYPE"), SQLDataType.CLOB, this, "");

    /**
     * The column <code>public.LOCATIONS.LOCATION</code>.
     */
    public final TableField<Record, String> colLocation = createField(DSL.name("LOCATION"), SQLDataType.CLOB, this, "");

    /**
     * The column <code>public.LOCATIONS.GEOM</code>.
     */
    public final TableField<Record, Geometry> colGeom = createField(DSL.name("GEOM"), DefaultDataType.getDefaultDataType(TYPE_GEOMETRY), this, "", new PostGisGeometryBinding());

    /**
     * The column <code>public.LOCATIONS.NAME</code>.
     */
    public final TableField<Record, String> colName = createField(DSL.name("NAME"), SQLDataType.CLOB.defaultValue(DSL.field("'no name'::text", SQLDataType.CLOB)), this, "");

    /**
     * The column <code>public.LOCATIONS.PROPERTIES</code>.
     */
    public final TableField<Record, JsonValue> colProperties = createField(DSL.name("PROPERTIES"), DefaultDataType.getDefaultDataType(TYPE_JSONB), this, "", new JsonBinding());

    /**
     * Create a <code>public.LOCATIONS</code> table reference
     */
    protected AbstractTableLocations() {
        this(DSL.name(TABLE_NAME), null);
    }

    protected AbstractTableLocations(Name alias, AbstractTableLocations<J> aliased) {
        this(alias, aliased, null);
    }

    protected AbstractTableLocations(Name alias, AbstractTableLocations<J> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""));
    }

    @Override
    public void initRelations() {
        final TableCollection<J> tables = getTables();
        registerRelation(
                new RelationManyToMany<>(this, tables.getTableThingsLocations(), tables.getTableThings(), EntityType.THING)
                        .setSourceFieldAcc(AbstractTableLocations::getId)
                        .setSourceLinkFieldAcc(AbstractTableThingsLocations::getLocationId)
                        .setTargetLinkFieldAcc(AbstractTableThingsLocations::getThingId)
                        .setTargetFieldAcc(AbstractTableThings::getId)
        );

        registerRelation(
                new RelationManyToMany<>(this, tables.getTableLocationsHistLocations(), tables.getTableHistLocations(), EntityType.HISTORICALLOCATION)
                        .setSourceFieldAcc(AbstractTableLocations::getId)
                        .setSourceLinkFieldAcc(AbstractTableLocationsHistLocations::getLocationId)
                        .setTargetLinkFieldAcc(AbstractTableLocationsHistLocations::getHistLocationId)
                        .setTargetFieldAcc(AbstractTableHistLocations::getId)
        );
    }

    @Override
    public void initProperties(final EntityFactories<J> entityFactories) {
        final IdManager idManager = entityFactories.idManager;
        final PropertySetter<AbstractTableLocations<J>, Location> setterId = (AbstractTableLocations<J> table, Record tuple, Location entity, DataSize dataSize) -> {
            entity.setId(idManager.fromObject(tuple.get(table.getId())));
        };
        pfReg.addEntry(EntityPropertyMain.ID, AbstractTableLocations::getId, setterId);
        pfReg.addEntry(EntityPropertyMain.SELFLINK, AbstractTableLocations::getId,
                (AbstractTableLocations<J> table, Record tuple, Location entity, DataSize dataSize) -> {
                    entity.setId(idManager.fromObject(tuple.get(table.getId())));
                });
        pfReg.addEntry(EntityPropertyMain.NAME, table -> table.colName,
                (AbstractTableLocations<J> table, Record tuple, Location entity, DataSize dataSize) -> {
                    entity.setName(tuple.get(table.colName));
                });
        pfReg.addEntry(EntityPropertyMain.DESCRIPTION, table -> table.colDescription,
                (AbstractTableLocations<J> table, Record tuple, Location entity, DataSize dataSize) -> {
                    entity.setDescription(tuple.get(table.colDescription));
                });
        pfReg.addEntry(EntityPropertyMain.ENCODINGTYPE, table -> table.colEncodingType,
                (AbstractTableLocations<J> table, Record tuple, Location entity, DataSize dataSize) -> {
                    entity.setEncodingType(tuple.get(table.colEncodingType));
                });
        pfReg.addEntry(EntityPropertyMain.LOCATION,
                (AbstractTableLocations<J> table, Record tuple, Location entity, DataSize dataSize) -> {
                    String encodingType = getFieldOrNull(tuple, table.colEncodingType);
                    String locationString = tuple.get(table.colLocation);
                    dataSize.increase(locationString == null ? 0 : locationString.length());
                    entity.setLocation(Utils.locationFromEncoding(encodingType, locationString));
                },
                new NFP<>("j", table -> table.colLocation));
        pfReg.addEntryNoSelect(EntityPropertyMain.LOCATION, "g", table -> table.colGeom);
        pfReg.addEntry(EntityPropertyMain.PROPERTIES, table -> table.colProperties,
                (AbstractTableLocations<J> table, Record tuple, Location entity, DataSize dataSize) -> {
                    JsonValue props = Utils.getFieldJsonValue(tuple, table.colProperties);
                    dataSize.increase(props.getStringLength());
                    entity.setProperties(props.getMapValue());
                });
        pfReg.addEntry(NavigationPropertyMain.THINGS, AbstractTableLocations::getId, setterId);
        pfReg.addEntry(NavigationPropertyMain.HISTORICALLOCATIONS, AbstractTableLocations::getId, setterId);
    }

    @Override
    public Location newEntity() {
        return new Location();
    }

    @Override
    public abstract TableField<Record, J> getId();

    public abstract TableField<Record, J> getGenFoiId();

    @Override
    public final UniqueKey<Record> getPrimaryKey() {
        if (primaryKey == null) {
            primaryKey = Internal.createUniqueKey(this, TABLE_NAME + "_PKEY", getId());
        }
        return primaryKey;
    }

    @Override
    public abstract AbstractTableLocations<J> as(Name as);

    @Override
    public abstract AbstractTableLocations<J> as(String alias);

    @Override
    public PropertyFields<AbstractTableLocations<J>, Location> handleEntityPropertyCustomSelect(final EntityPropertyCustomSelect epCustomSelect) {
        final EntityPropertyMain mainEntityProperty = epCustomSelect.getMainEntityProperty();
        if (mainEntityProperty == EntityPropertyMain.LOCATION) {
            PropertyFields<AbstractTableLocations<J>, Location> mainPropertyFields = pfReg.getSelectFieldsForProperty(mainEntityProperty);
            final Field mainField = mainPropertyFields.fields.values().iterator().next().get(getThis());

            JsonFieldFactory jsonFactory = jsonFieldFromPath(mainField, epCustomSelect);
            return propertyFieldForJsonField(jsonFactory, epCustomSelect);
        }
        return super.handleEntityPropertyCustomSelect(epCustomSelect);
    }

    @Override
    public AbstractTableLocations<J> getThis() {
        return this;
    }

}
