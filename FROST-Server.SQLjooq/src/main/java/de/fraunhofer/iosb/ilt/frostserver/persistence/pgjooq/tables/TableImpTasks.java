package de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables;

import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.persistence.IdManager;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.bindings.JsonBinding;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.bindings.JsonValue;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.factories.EntityFactories;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.fieldwrapper.JsonFieldFactory;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.relations.RelationOneToMany;
import static de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.StaTableAbstract.jsonFieldFromPath;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.PropertyFieldRegistry.ConverterTimeInstant;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.PropertyFieldRegistry.PropertyFields;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyCustomSelect;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain;
import java.time.OffsetDateTime;
import org.jooq.DataType;
import org.jooq.Field;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultDataType;
import org.jooq.impl.SQLDataType;

public class TableImpTasks<J extends Comparable> extends StaTableAbstract<J, TableImpTasks<J>> {

    private static final long serialVersionUID = -1457801967;

    private static TableImpTasks INSTANCE;
    private static DataType INSTANCE_ID_TYPE;

    public static <J extends Comparable> TableImpTasks<J> getInstance(DataType<J> idType) {
        if (INSTANCE == null) {
            INSTANCE_ID_TYPE = idType;
            INSTANCE = new TableImpTasks(INSTANCE_ID_TYPE);
            return INSTANCE;
        }
        if (INSTANCE_ID_TYPE.equals(idType)) {
            return INSTANCE;
        }
        return new TableImpTasks<>(idType);
    }

    /**
     * The column <code>public.TASKS.CREATION_TIME</code>.
     */
    public final TableField<Record, OffsetDateTime> colCreationTime = createField(DSL.name("CREATION_TIME"), SQLDataType.TIMESTAMPWITHTIMEZONE, this);

    /**
     * The column <code>public.TASKINGCAPABILITIES.PROPERTIES</code>.
     */
    public final TableField<Record, JsonValue> colTaskingParameters = createField(DSL.name("TASKING_PARAMETERS"), DefaultDataType.getDefaultDataType(TYPE_JSONB), this, "", new JsonBinding());

    /**
     * The column <code>public.TASKS.ID</code>.
     */
    public final TableField<Record, J> colId = createField(DSL.name("ID"), getIdType(), this);

    /**
     * The column <code>public.TASKS.THING_ID</code>.
     */
    public final TableField<Record, J> colTaskingCapabilityId = createField(DSL.name("TASKINGCAPABILITY_ID"), getIdType(), this);

    /**
     * Create a <code>public.TASKS</code> table reference
     */
    private TableImpTasks(DataType<J> idType) {
        super(idType, DSL.name("TASKS"), null);
    }

    private TableImpTasks(Name alias, TableImpTasks<J> aliased) {
        super(aliased.getIdType(), alias, aliased);
    }

    @Override
    public void initRelations() {
        final TableCollection<J> tables = getTables();
        registerRelation(new RelationOneToMany<>(this, TableImpTaskingCapabilities.getInstance(getIdType()), EntityType.TASKING_CAPABILITY)
                        .setSourceFieldAccessor(TableImpTasks::getTaskingCapabilityId)
                        .setTargetFieldAccessor(TableImpTaskingCapabilities::getId)
        );
    }

    @Override
    public void initProperties(final EntityFactories<J> entityFactories) {
        final IdManager idManager = entityFactories.idManager;
        pfReg.addEntryId(idManager, TableImpTasks::getId);
        pfReg.addEntry(EntityPropertyMain.CREATIONTIME, table -> table.colCreationTime,
                new ConverterTimeInstant<>(EntityPropertyMain.CREATIONTIME, table -> table.colCreationTime));
        pfReg.addEntryMap(EntityPropertyMain.TASKINGPARAMETERS, table -> table.colTaskingParameters);
        pfReg.addEntry(NavigationPropertyMain.TASKINGCAPABILITY, TableImpTasks::getTaskingCapabilityId, idManager);
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.TASK;
    }

    @Override
    public TableField<Record, J> getId() {
        return colId;
    }

    public TableField<Record, J> getTaskingCapabilityId() {
        return colTaskingCapabilityId;
    }

    @Override
    public TableImpTasks<J> as(Name alias) {
        return new TableImpTasks<>(alias, this);
    }

    @Override
    public TableImpTasks<J> as(String alias) {
        return new TableImpTasks<>(DSL.name(alias), this);
    }

    @Override
    public PropertyFields<TableImpTasks<J>> handleEntityPropertyCustomSelect(final EntityPropertyCustomSelect epCustomSelect) {
        final EntityPropertyMain mainEntityProperty = epCustomSelect.getMainEntityProperty();
        if (mainEntityProperty == EntityPropertyMain.TASKINGPARAMETERS) {
            PropertyFields<TableImpTasks<J>> mainPropertyFields = pfReg.getSelectFieldsForProperty(mainEntityProperty);
            final Field mainField = mainPropertyFields.fields.values().iterator().next().get(getThis());

            JsonFieldFactory jsonFactory = jsonFieldFromPath(mainField, epCustomSelect);
            return propertyFieldForJsonField(jsonFactory, epCustomSelect);
        }
        return super.handleEntityPropertyCustomSelect(epCustomSelect);
    }

    @Override
    public TableImpTasks<J> getThis() {
        return this;
    }

}
