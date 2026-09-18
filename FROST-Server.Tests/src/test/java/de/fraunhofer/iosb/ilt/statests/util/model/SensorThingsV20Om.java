/*
 * Copyright (C) 2024 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
 * Karlsruhe, Germany.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.fraunhofer.iosb.ilt.statests.util.model;

import static de.fraunhofer.iosb.ilt.frostclient.model.property.type.TypePrimitive.EDM_STRING;
import static de.fraunhofer.iosb.ilt.frostclient.models.CommonProperties.EP_ENCODINGTYPE;
import static de.fraunhofer.iosb.ilt.frostclient.models.CommonProperties.NAME_DATASTREAMS;
import static de.fraunhofer.iosb.ilt.frostclient.models.CommonProperties.NAME_OBSERVEDPROPERTIES;
import static de.fraunhofer.iosb.ilt.frostclient.models.CommonProperties.NAME_OBSERVEDPROPERTY;
import static de.fraunhofer.iosb.ilt.frostclient.models.CommonProperties.NAME_SENSOR;
import static de.fraunhofer.iosb.ilt.frostclient.models.CommonProperties.NAME_SENSORS;
import static de.fraunhofer.iosb.ilt.frostclient.models.CommonProperties.NAME_THING;
import static de.fraunhofer.iosb.ilt.frostclient.models.CommonProperties.NAME_THINGS;
import static de.fraunhofer.iosb.ilt.frostclient.models.SensorThingsV20Core.NAMESPACE;

import de.fraunhofer.iosb.ilt.frostclient.SensorThingsService;
import de.fraunhofer.iosb.ilt.frostclient.exception.Exceptions;
import de.fraunhofer.iosb.ilt.frostclient.model.Entity;
import de.fraunhofer.iosb.ilt.frostclient.model.EntityType;
import de.fraunhofer.iosb.ilt.frostclient.model.ModelRegistry;
import de.fraunhofer.iosb.ilt.frostclient.model.PkValue;
import de.fraunhofer.iosb.ilt.frostclient.model.property.EntityPropertyMain;
import de.fraunhofer.iosb.ilt.frostclient.model.property.NavigationPropertyEntity;
import de.fraunhofer.iosb.ilt.frostclient.model.property.NavigationPropertyEntitySet;
import de.fraunhofer.iosb.ilt.frostclient.model.property.type.TypeComplex;
import de.fraunhofer.iosb.ilt.frostclient.model.property.type.TypePrimitive;
import de.fraunhofer.iosb.ilt.frostclient.models.CommonProperties;
import de.fraunhofer.iosb.ilt.frostclient.models.DataModel;
import de.fraunhofer.iosb.ilt.frostclient.models.ext.MapValue;
import de.fraunhofer.iosb.ilt.frostclient.models.ext.TimeInstant;
import de.fraunhofer.iosb.ilt.frostclient.models.ext.TimeValue;
import net.time4j.Moment;

/**
 * The Data Model implements the SensorThings V2.0 Tasking extension.
 */
public class SensorThingsV20Om implements DataModel {

    public static enum Status {
        CREATED("Created"),
        RUNNING("Running"),
        COMPLETED("Completed"),
        REJECTED("Rejected"),
        FAILED("Failed");

        public final String name;

        private Status(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        @Override
        public String toString() {
            return getName();
        }

    }

    public static final String NAME_DEPLOYMENT = "Deployment";
    public static final String NAME_DEPLOYMENTS = "Deployments";
    public static final String NAME_LINKING_TIME = "LinkingTime";
    public static final String NAME_LINKING_TIMES = "LinkingTimes";
    public static final String NAME_MONITORING_ACTIVITY = "MonitoringActivity";
    public static final String NAME_MONITORING_ACTIVITIES = "MonitoringActivities";
    public static final String NAME_MONITORING_NETWORK = "MonitoringNetwork";
    public static final String NAME_MONITORING_NETWORKS = "MonitoringNetworks";
    public static final String NAME_MONITORING_PROGRAM = "MonitoringProgram";
    public static final String NAME_MONITORING_PROGRAMS = "MonitoringPrograms";
    public static final String NAME_OBSERVING_PROCEDURE = "ObservingProcedure";
    public static final String NAME_OBSERVING_PROCEDURES = "ObservingProcedures";

    public static final String NAME_EP_POSITION = "position";
    public static final String NAME_EP_REASON = "reason";
    public static final String NAME_EP_TIME = "time";

    public static final EntityPropertyMain<Object> EP_POSITION = new EntityPropertyMain<>(NAME_EP_POSITION, TypePrimitive.EDM_GEOMETRY);
    public static final EntityPropertyMain<String> EP_REASON = new EntityPropertyMain<>(NAME_EP_REASON, EDM_STRING);
    public static final EntityPropertyMain<TimeValue> EP_TIME = new EntityPropertyMain<>(NAME_EP_TIME, TypeComplex.STA_TIMEVALUE);

    public final NavigationPropertyEntitySet npDatastreamDeployments = new NavigationPropertyEntitySet(NAME_DEPLOYMENTS);
    public final NavigationPropertyEntitySet npDatastreamObservingprocedure = new NavigationPropertyEntitySet(NAME_OBSERVING_PROCEDURE);

    public final NavigationPropertyEntitySet npDeploymentDatastreams = new NavigationPropertyEntitySet(NAME_DATASTREAMS, npDatastreamDeployments);
    public final NavigationPropertyEntity npDeploymentSensor = new NavigationPropertyEntity(NAME_SENSOR);
    public final NavigationPropertyEntity npDeploymentThing = new NavigationPropertyEntity(NAME_THING);

    public final NavigationPropertyEntity npLinkingtimeThing = new NavigationPropertyEntity(NAME_THING);
    public final NavigationPropertyEntity npLinkingtimeNetwork = new NavigationPropertyEntity(NAME_MONITORING_NETWORK);

    public final NavigationPropertyEntitySet npMonitoringactivityPrograms = new NavigationPropertyEntitySet(NAME_MONITORING_PROGRAMS);
    public final NavigationPropertyEntitySet npMonitoringactivityNetworks = new NavigationPropertyEntitySet(NAME_MONITORING_NETWORKS);

    public final NavigationPropertyEntitySet npMonitoringnetworkActivities = new NavigationPropertyEntitySet(NAME_MONITORING_ACTIVITIES, npMonitoringactivityNetworks);
    public final NavigationPropertyEntitySet npMonitoringnetworkLinkingtimes = new NavigationPropertyEntitySet(NAME_LINKING_TIMES, npLinkingtimeNetwork);
    public final NavigationPropertyEntitySet npMonitoringnetworkThings = new NavigationPropertyEntitySet(NAME_THINGS);

    public final NavigationPropertyEntitySet npMonitoringprogramActivities = new NavigationPropertyEntitySet(NAME_MONITORING_ACTIVITIES, npMonitoringactivityPrograms);

    public final NavigationPropertyEntitySet npObservedpropertyObservingprocedure = new NavigationPropertyEntitySet(NAME_OBSERVING_PROCEDURES);

    public final NavigationPropertyEntitySet npObservingprocedureDatastreams = new NavigationPropertyEntitySet(NAME_DATASTREAMS, npDatastreamObservingprocedure);
    public final NavigationPropertyEntitySet npObservingprocedureObservedproperties = new NavigationPropertyEntitySet(NAME_OBSERVEDPROPERTIES, npObservedpropertyObservingprocedure);
    public final NavigationPropertyEntitySet npObservingprocedureSensors = new NavigationPropertyEntitySet(NAME_SENSORS);

    public final NavigationPropertyEntitySet npSensorDeployments = new NavigationPropertyEntitySet(NAME_DEPLOYMENTS, npDeploymentSensor);
    public final NavigationPropertyEntitySet npSensorObservingprocedures = new NavigationPropertyEntitySet(NAME_OBSERVING_PROCEDURES, npObservingprocedureSensors);

    public final NavigationPropertyEntitySet npThingDeployments = new NavigationPropertyEntitySet(NAME_DEPLOYMENTS, npDeploymentThing);
    public final NavigationPropertyEntitySet npThingLinkingTimes = new NavigationPropertyEntitySet(NAME_LINKING_TIMES, npLinkingtimeThing);
    public final NavigationPropertyEntitySet npThingMonitoringnetworks = new NavigationPropertyEntitySet(NAME_MONITORING_NETWORKS, npMonitoringnetworkThings);

    public final EntityType etDeployment = new EntityType(NAME_DEPLOYMENT, NAME_DEPLOYMENTS).setNamespace(NAMESPACE);
    public final EntityType etLinkingTime = new EntityType(NAME_LINKING_TIME, NAME_LINKING_TIMES).setNamespace(NAMESPACE);
    public final EntityType etMonitoringActivity = new EntityType(NAME_MONITORING_ACTIVITY, NAME_MONITORING_ACTIVITIES).setNamespace(NAMESPACE);
    public final EntityType etMonitoringNetwork = new EntityType(NAME_MONITORING_NETWORK, NAME_MONITORING_NETWORKS).setNamespace(NAMESPACE);
    public final EntityType etMonitoringProgram = new EntityType(NAME_MONITORING_PROGRAM, NAME_MONITORING_PROGRAMS).setNamespace(NAMESPACE);
    public final EntityType etObservingProcedure = new EntityType(NAME_OBSERVING_PROCEDURE, NAME_OBSERVING_PROCEDURES).setNamespace(NAMESPACE);

    private ModelRegistry mr;

    public SensorThingsV20Om() {
    }

    @Override
    public final void init(SensorThingsService service, ModelRegistry modelRegistry) {
        if (this.mr != null) {
            throw new IllegalArgumentException("Already initialised.");
        }
        this.mr = modelRegistry;
        mr.addDataModel(this)
                .registerEntityType(etDeployment)
                .registerEntityType(etLinkingTime)
                .registerEntityType(etMonitoringActivity)
                .registerEntityType(etMonitoringNetwork)
                .registerEntityType(etMonitoringProgram)
                .registerEntityType(etObservingProcedure);

        etDeployment
                .registerProperty(CommonProperties.EP_ID)
                .registerProperty(CommonProperties.EP_NAME)
                .registerProperty(CommonProperties.EP_DEFINITION)
                .registerProperty(CommonProperties.EP_DESCRIPTION)
                .registerProperty(CommonProperties.EP_PROPERTIES)
                .registerProperty(EP_REASON)
                .registerProperty(EP_ENCODINGTYPE)
                .registerProperty(EP_POSITION)
                .registerProperty(EP_TIME)
                .registerProperty(npDeploymentDatastreams)
                .registerProperty(npDeploymentSensor)
                .registerProperty(npDeploymentThing);

        etLinkingTime
                .registerProperty(CommonProperties.EP_ID)
                .registerProperty(EP_TIME)
                .registerProperty(npLinkingtimeNetwork)
                .registerProperty(npLinkingtimeThing);

        etMonitoringActivity
                .registerProperty(CommonProperties.EP_ID)
                .registerProperty(CommonProperties.EP_NAME)
                .registerProperty(CommonProperties.EP_DEFINITION)
                .registerProperty(CommonProperties.EP_DESCRIPTION)
                .registerProperty(CommonProperties.EP_PROPERTIES)
                .registerProperty(npMonitoringactivityNetworks)
                .registerProperty(npMonitoringactivityPrograms);

        etMonitoringNetwork
                .registerProperty(CommonProperties.EP_ID)
                .registerProperty(CommonProperties.EP_NAME)
                .registerProperty(CommonProperties.EP_DEFINITION)
                .registerProperty(CommonProperties.EP_DESCRIPTION)
                .registerProperty(CommonProperties.EP_PROPERTIES)
                .registerProperty(npMonitoringnetworkActivities)
                .registerProperty(npMonitoringnetworkLinkingtimes)
                .registerProperty(npMonitoringnetworkThings);

        etMonitoringProgram
                .registerProperty(CommonProperties.EP_ID)
                .registerProperty(CommonProperties.EP_NAME)
                .registerProperty(CommonProperties.EP_DEFINITION)
                .registerProperty(CommonProperties.EP_DESCRIPTION)
                .registerProperty(CommonProperties.EP_PROPERTIES)
                .registerProperty(npMonitoringprogramActivities);

        etObservingProcedure
                .registerProperty(CommonProperties.EP_ID)
                .registerProperty(CommonProperties.EP_NAME)
                .registerProperty(CommonProperties.EP_DEFINITION)
                .registerProperty(CommonProperties.EP_DESCRIPTION)
                .registerProperty(CommonProperties.EP_PROPERTIES)
                .registerProperty(npObservingprocedureDatastreams)
                .registerProperty(npObservingprocedureObservedproperties)
                .registerProperty(npObservingprocedureSensors);

        mr.getEntityTypeForName(NAME_THING)
                .registerProperty(npThingDeployments)
                .registerProperty(npThingLinkingTimes)
                .registerProperty(npThingMonitoringnetworks);
        mr.getEntityTypeForName(NAME_OBSERVEDPROPERTY)
                .registerProperty(npObservedpropertyObservingprocedure);
        mr.getEntityTypeForName(NAME_DEPLOYMENT)
                .registerProperty(npDeploymentDatastreams)
                .registerProperty(npDeploymentSensor)
                .registerProperty(npDeploymentThing);
    }

    @Override
    public boolean isInitialised() {
        return mr != null;
    }

    public ModelRegistry getModelRegistry() {
        return mr;
    }

    public LinkingTimeBuilder buildLinkingTime() {
        return new LinkingTimeBuilder(this);
    }

    public LinkingTimeBuilder editLinkingTime(Entity entity) {
        return new LinkingTimeBuilder(this, entity);

    }

    public MonitoringNetworkBuilder buildMonitoringNetwork() {
        return new MonitoringNetworkBuilder(this);
    }

    public MonitoringNetworkBuilder editMonitoringNetwork(Entity entity) {
        return new MonitoringNetworkBuilder(this, entity);
    }

    public static class BuilderId<T extends BuilderId> {

        protected final Entity entity;

        public BuilderId(Entity entity) {
            this.entity = entity;
        }

        public T setId(Object value) {
            entity.setProperty(CommonProperties.EP_ID, PkValue.of(value));
            return getThis();
        }

        public T setId(PkValue value) {
            entity.setPrimaryKeyValues(value);
            return getThis();
        }

        public Entity build() {
            return entity;
        }

        public T getThis() {
            return (T) this;
        }

    }

    public static class BuilderIdNameDefDesProp<T extends BuilderIdNameDefDesProp<T>> extends BuilderId<T> {

        public BuilderIdNameDefDesProp(Entity entity) {
            super(entity);
        }

        public T setName(String value) {
            entity.setProperty(CommonProperties.EP_NAME, value);
            return getThis();
        }

        public T setDefinition(String value) {
            entity.setProperty(CommonProperties.EP_DEFINITION, value);
            return getThis();
        }

        public T setDescription(String value) {
            entity.setProperty(CommonProperties.EP_DESCRIPTION, value);
            return getThis();
        }

        public T setProperties(MapValue properties) {
            entity.setProperty(CommonProperties.EP_PROPERTIES, properties);
            return getThis();
        }

    }

    public static class LinkingTimeBuilder extends BuilderId<LinkingTimeBuilder> {

        SensorThingsV20Om mdlOm;

        public LinkingTimeBuilder(SensorThingsV20Om mdlOm) {
            super(new Entity(mdlOm.etLinkingTime));
            this.mdlOm = mdlOm;
        }

        public LinkingTimeBuilder(SensorThingsV20Om mdlOm, Entity entity) {
            super(entity);
            this.mdlOm = mdlOm;
        }

        public LinkingTimeBuilder setTime(TimeValue time) {
            entity.setProperty(EP_TIME, time);
            return getThis();
        }

        public LinkingTimeBuilder setTimeStart(Moment start) {
            TimeValue time = entity.getProperty(EP_TIME);
            if (time == null) {
                time = TimeValue.create(start);
                entity.setProperty(EP_TIME, time);
            } else {
                time.setProperty(TimeValue.EP_START_TIME, TimeInstant.create(start));
            }
            return getThis();
        }

        public LinkingTimeBuilder setTimeEnd(Moment end) {
            TimeValue time = entity.getProperty(EP_TIME);
            Exceptions.illegalArgumentIf(time == null, "Set the start time first.");
            time.setProperty(TimeValue.EP_END_TIME, TimeInstant.create(end));
            return getThis();
        }

        public LinkingTimeBuilder addMonitoringNetwork(Entity monNet) {
            entity.setProperty(mdlOm.npLinkingtimeNetwork, monNet);
            return getThis();
        }

        public LinkingTimeBuilder addThing(Entity thing) {
            entity.setProperty(mdlOm.npLinkingtimeThing, thing);
            return getThis();
        }

    }

    public static class MonitoringNetworkBuilder extends BuilderIdNameDefDesProp<MonitoringNetworkBuilder> {

        SensorThingsV20Om mdlOm;

        public MonitoringNetworkBuilder(SensorThingsV20Om mdlOm) {
            super(new Entity(mdlOm.etMonitoringNetwork));
            this.mdlOm = mdlOm;
        }

        public MonitoringNetworkBuilder(SensorThingsV20Om mdlOm, Entity entity) {
            super(entity);
            this.mdlOm = mdlOm;
        }

        public MonitoringNetworkBuilder addMonitoringActivity(Entity monAct) {
            entity.addNavigationEntity(mdlOm.npMonitoringnetworkActivities, monAct);
            return getThis();
        }

        public MonitoringNetworkBuilder addLinkingTime(Entity lt) {
            entity.addNavigationEntity(mdlOm.npMonitoringnetworkLinkingtimes, lt);
            return getThis();
        }

        public MonitoringNetworkBuilder addThing(Entity thing) {
            entity.addNavigationEntity(mdlOm.npMonitoringnetworkThings, thing);
            return getThis();
        }

    }

}
