package de.fraunhofer.iosb.ilt.frostserver.plugin.actuation;

import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.ModelRegistry;
import de.fraunhofer.iosb.ilt.frostserver.persistence.IdManager;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.bindings.JsonBinding;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.bindings.JsonValue;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.factories.EntityFactories;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.fieldwrapper.JsonFieldFactory;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.relations.RelationOneToMany;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.StaTableAbstract;
import static de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.StaTableAbstract.jsonFieldFromPath;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.TableCollection;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.PropertyFieldRegistry.ConverterTimeInstant;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.PropertyFieldRegistry.PropertyFields;
import de.fraunhofer.iosb.ilt.frostserver.plugin.coremodel.PluginCoreModel;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyCustomSelect;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyMain;
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

    /**
     * The column <code>public.TASKS.CREATION_TIME</code>.
     */
    public final TableField<Record, OffsetDateTime> colCreationTime = createField(DSL.name("CREATION_TIME"), SQLDataType.TIMESTAMPWITHTIMEZONE, this);

    /**
     * The column <code>public.TASKINGCAPABILITIES.EP_PROPERTIES</code>.
     */
    public final TableField<Record, JsonValue> colTaskingParameters = createField(DSL.name("TASKING_PARAMETERS"), DefaultDataType.getDefaultDataType(TYPE_JSONB), this, "", new JsonBinding());

    /**
     * The column <code>public.TASKS.EP_ID</code>.
     */
    public final TableField<Record, J> colId = createField(DSL.name("ID"), getIdType(), this);

    /**
     * The column <code>public.TASKS.THING_ID</code>.
     */
    public final TableField<Record, J> colTaskingCapabilityId = createField(DSL.name("TASKINGCAPABILITY_ID"), getIdType(), this);

    private final PluginActuation pluginActuation;
    private final PluginCoreModel pluginCoreModel;

    /**
     * Create a <code>public.TASKS</code> table reference.
     *
     * @param idType The (SQL)DataType of the Id columns used in the actual
     * database.
     * @param pluginActuation the actuation plugin this table belongs to.
     * @param pluginCoreModel the coreModel plugin that this data model links
     * to.
     */
    public TableImpTasks(DataType<J> idType, PluginActuation pluginActuation, PluginCoreModel pluginCoreModel) {
        super(idType, DSL.name("TASKS"), null);
        this.pluginActuation = pluginActuation;
        this.pluginCoreModel = pluginCoreModel;
    }

    private TableImpTasks(Name alias, TableImpTasks<J> aliased, PluginActuation pluginActuation, PluginCoreModel pluginCoreModel) {
        super(aliased.getIdType(), alias, aliased);
        this.pluginActuation = pluginActuation;
        this.pluginCoreModel = pluginCoreModel;
    }

    @Override
    public void initRelations() {
        final TableCollection<J> tables = getTables();
        final TableImpTaskingCapabilities<J> tableTaskingCaps = tables.getTableForClass(TableImpTaskingCapabilities.class);
        registerRelation(new RelationOneToMany<>(this, tableTaskingCaps, pluginActuation.TASKING_CAPABILITY)
                .setSourceFieldAccessor(TableImpTasks::getTaskingCapabilityId)
                .setTargetFieldAccessor(TableImpTaskingCapabilities::getId)
        );
    }

    @Override
    public void initProperties(final EntityFactories<J> entityFactories) {
        final ModelRegistry modelRegistry = getModelRegistry();
        final IdManager idManager = entityFactories.getIdManager();
        pfReg.addEntryId(idManager, TableImpTasks::getId);
        pfReg.addEntry(pluginCoreModel.EP_CREATIONTIME, table -> table.colCreationTime,
                new ConverterTimeInstant<>(pluginCoreModel.EP_CREATIONTIME, table -> table.colCreationTime));
        pfReg.addEntryMap(pluginActuation.EP_TASKINGPARAMETERS, table -> table.colTaskingParameters);
        pfReg.addEntry(pluginActuation.NP_TASKINGCAPABILITY, TableImpTasks::getTaskingCapabilityId, idManager);
    }

    @Override
    public EntityType getEntityType() {
        return pluginActuation.TASK;
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
        return new TableImpTasks<>(alias, this, pluginActuation, pluginCoreModel);
    }

    @Override
    public TableImpTasks<J> as(String alias) {
        return new TableImpTasks<>(DSL.name(alias), this, pluginActuation, pluginCoreModel);
    }

    @Override
    public PropertyFields<TableImpTasks<J>> handleEntityPropertyCustomSelect(final EntityPropertyCustomSelect epCustomSelect) {
        final EntityPropertyMain mainEntityProperty = epCustomSelect.getMainEntityProperty();
        if (mainEntityProperty == pluginActuation.EP_TASKINGPARAMETERS) {
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
