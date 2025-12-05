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
package de.fraunhofer.iosb.ilt.frostserver.plugin.dcat;

import static de.fraunhofer.iosb.ilt.frostserver.service.InitResult.INIT_DELAY;
import static de.fraunhofer.iosb.ilt.frostserver.util.Constants.CONTENT_TYPE_APPLICATION_RDF_XML;
import static de.fraunhofer.iosb.ilt.frostserver.util.HttpMethod.GET;
import static de.fraunhofer.iosb.ilt.frostserver.util.HttpMethod.HEAD;

import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.ModelRegistry;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.model.core.EntitySet;
import de.fraunhofer.iosb.ilt.frostserver.model.core.PkValue;
import de.fraunhofer.iosb.ilt.frostserver.path.ResourcePath;
import de.fraunhofer.iosb.ilt.frostserver.path.Version;
import de.fraunhofer.iosb.ilt.frostserver.persistence.PersistenceManager;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.JooqPersistenceManager;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.bindings.MomentBinding;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.StaMainTable;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.StaTable;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.TableCollection;
import de.fraunhofer.iosb.ilt.frostserver.plugin.coremodel.PluginCoreModel;
import de.fraunhofer.iosb.ilt.frostserver.plugin.coremodelv2.PluginCoreModelV2;
import de.fraunhofer.iosb.ilt.frostserver.plugin.modelloader.PluginModelLoader;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain.NavigationPropertyEntity;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain.NavigationPropertyEntitySet;
import de.fraunhofer.iosb.ilt.frostserver.query.Expand;
import de.fraunhofer.iosb.ilt.frostserver.query.Query;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.Path;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.constant.ConstantFactory;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.function.comparison.Equal;
import de.fraunhofer.iosb.ilt.frostserver.service.InitResult;
import de.fraunhofer.iosb.ilt.frostserver.service.PluginManager;
import de.fraunhofer.iosb.ilt.frostserver.service.PluginModel;
import de.fraunhofer.iosb.ilt.frostserver.service.PluginService;
import de.fraunhofer.iosb.ilt.frostserver.service.Service;
import de.fraunhofer.iosb.ilt.frostserver.service.ServiceRequest;
import de.fraunhofer.iosb.ilt.frostserver.service.ServiceResponse;
import de.fraunhofer.iosb.ilt.frostserver.settings.ConfigDefaults;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import de.fraunhofer.iosb.ilt.frostserver.settings.Settings;
import de.fraunhofer.iosb.ilt.frostserver.settings.annotation.DefaultValueBoolean;
import de.fraunhofer.iosb.ilt.frostserver.util.HttpMethod;
import de.fraunhofer.iosb.ilt.frostserver.util.StringHelper;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import net.time4j.Moment;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.datatypes.DatatypeFormatException;
import org.apache.jena.datatypes.xsd.XSDDateTime;
import org.apache.jena.geosparql.implementation.GeometryWrapper;
import org.apache.jena.geosparql.implementation.datatype.WKTDatatype;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.impl.PropertyImpl;
import org.apache.jena.vocabulary.DCAT;
import org.apache.jena.vocabulary.DCTerms;
import org.jooq.DSLContext;
import org.jooq.Record3;
import org.jooq.TableField;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Plugin loader for the Projects plugin.
 */
public class PluginDcat implements PluginModel, PluginService, ConfigDefaults {

    private static final Logger LOGGER = LoggerFactory.getLogger(PluginDcat.class.getName());

    /**
     * The path for the DCAT entry.
     */
    public static final String PATH_GET_DCAT = "/$getDCAT";

    /**
     * The RequestType definition for the get DCAT request type.
     */
    public static final String REQUEST_TYPE_GET_DCAT = "getDcat";

    @DefaultValueBoolean(false)
    public static final String TAG_ENABLE_PROJECTS = "dcat.enable";

    @DefaultValueBoolean(true)
    public static final String TAG_DEFAULT_RULES = "dcat.enableDefaultRules";

    private boolean enabled;
    private boolean fullyInitialised;
    private CoreSettings coreSettings;
    private Mdl mdl;

    @Override
    public InitResult init(CoreSettings coreSettings) {
        this.coreSettings = coreSettings;
        Settings pluginSettings = coreSettings.getPluginSettings();
        enabled = pluginSettings.getBoolean(TAG_ENABLE_PROJECTS, PluginDcat.class);
        if (enabled) {
            final PluginManager pluginManager = coreSettings.getPluginManager();
            PluginModelLoader pml = pluginManager.getPlugin(PluginModelLoader.class);

            if (pml == null || !pml.isEnabled()) {
                LOGGER.warn("PluginModelLoader must be enabled before the DCAT plugin, delaying initialisation...");
                return INIT_DELAY;
            }

            pml.addLiquibaseFile("plugindcat/sta1/liquibase/tables.xml");

            pml.addModelFile("plugindcat/sta1/model/Agent.json");
            pml.addModelFile("plugindcat/sta1/model/DataService.json");
            pml.addModelFile("plugindcat/sta1/model/Dataset.json");
            pml.addModelFile("plugindcat/sta1/model/Distribution.json");
            pml.addModelFile("plugindcat/sta1/model/License.json");
            pml.addModelFile("plugindcat/sta1/model/Standard.json");

            pluginManager.registerPlugin(this);
        }
        return InitResult.INIT_OK;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void registerEntityTypes() {
        // Nothing to do here.
    }

    @Override
    public boolean linkEntityTypes(PersistenceManager pm) {
        fullyInitialised = true;
        return true;
    }

    @Override
    public boolean isFullyInitialised() {
        return fullyInitialised;
    }

    @Override
    public Collection<Version> getVersions() {
        return coreSettings.getPluginManager().getVersions().values();
    }

    @Override
    public Collection<String> getVersionedUrlPaths() {
        return Arrays.asList(PATH_GET_DCAT);
    }

    @Override
    public Collection<String> getRequestTypes() {
        return Arrays.asList(REQUEST_TYPE_GET_DCAT);
    }

    @Override
    public String getRequestTypeFor(Version version, String path, HttpMethod method, String contentType) {
        if ((GET == method || HEAD == method) && path.equals(PATH_GET_DCAT)) {
            return REQUEST_TYPE_GET_DCAT;
        }
        return null;
    }

    private class Mdl {

        final EntityType etAgent;
        final EntityType etDataService;
        final EntityType etDataset;
        final EntityType etDatastream;
        final EntityType etDistribution;
        final EntityType etStandard;
        final EntityType etLicense;
        final EntityType etObservedProperty;

        final NavigationPropertyEntity npDtstPublisher;
        final NavigationPropertyEntitySet npDtstContactPoint;
        final NavigationPropertyEntitySet npDtstConformsTo;
        final NavigationPropertyEntitySet npDtstCreators;
        final NavigationPropertyEntitySet npDtstDatastreams;
        final NavigationPropertyEntitySet npDtstDataServices;
        final NavigationPropertyEntitySet npDtstDistributions;

        final NavigationPropertyEntitySet npDbtnAccessServices;
        final NavigationPropertyEntitySet npDbtnConformsTo;
        final NavigationPropertyEntity npDbtnLicense;

        final NavigationPropertyEntity npDatSrvPublisher;
        final NavigationPropertyEntity npDatSrvLicense;
        final NavigationPropertyEntitySet npDatSrvConformsTo;

        final NavigationPropertyEntity npDtstrmObservedProperty;
        final NavigationPropertyEntitySet npDtstrmDatasets;
        final NavigationPropertyEntitySet npObspropDatastreams;
        final EntityPropertyMain<String> epObspropName;

        final EntityPropertyMain<String> epAgentDefinition;
        final EntityPropertyMain<String> epAgentName;
        final EntityPropertyMain<String> epAgentEmail;
        final EntityPropertyMain<String> epAgentTelephone;
        final EntityPropertyMain<String> epAgentCountryName;
        final EntityPropertyMain<String> epAgentPostalCode;
        final EntityPropertyMain<String> epAgentLocality;
        final EntityPropertyMain<String> epAgentStreetAddress;

        final EntityPropertyMain<String> epDtstTitle;
        final EntityPropertyMain<String> epDtstDescription;
        final EntityPropertyMain<String> epDtstTemporalResolution;
        final EntityPropertyMain<String> epDtstKeywords;
        final EntityPropertyMain<Double> epDtstSpatialResolution;

        final EntityPropertyMain<String> epDbtnAccessURL;
        final EntityPropertyMain<String> epDbtnTitle;
        final EntityPropertyMain<String> epDbtnDescription;
        final EntityPropertyMain<String> epDbtnFormat;
        final EntityPropertyMain<String> epDbtnAvailability;

        final EntityPropertyMain<String> epStdrdDefinition;
        final EntityPropertyMain<String> epStdrdTitle;

        final EntityPropertyMain<String> epLicenseDefinition;
        final EntityPropertyMain<String> epLicenseTitle;

        final EntityPropertyMain<String> epDatSrvTitle;
        final EntityPropertyMain<String> epDatSrvEndpointURL;

        final String colNameDsId;
        final String colNameDsPhenEnd;
        final String colNameDsPhenStart;
        final String colNameDsObservedArea;
        final String tblNameDsDtst;
        final String colNameDsDtstDatasetId;
        final String colNameDsDtstDatastreamId;

        public Mdl(ModelRegistry mr, PluginManager pm) {
            etAgent = mr.getEntityTypeForName("Agent", true);
            etDataService = mr.getEntityTypeForName("DataService", true);
            etDataset = mr.getEntityTypeForName("Dataset", true);
            etDistribution = mr.getEntityTypeForName("Distribution", true);
            etStandard = mr.getEntityTypeForName("Standard", true);
            etLicense = mr.getEntityTypeForName("License", true);
            etDatastream = mr.getEntityTypeForName("Datastream", true);
            etObservedProperty = mr.getEntityTypeForName("ObservedProperty", true);

            epAgentDefinition = etAgent.getEntityProperty("definition");
            epAgentName = etAgent.getEntityProperty("name");
            epAgentEmail = etAgent.getEntityProperty("email");
            epAgentTelephone = etAgent.getEntityProperty("telephone");
            epAgentCountryName = etAgent.getEntityProperty("countryName");
            epAgentPostalCode = etAgent.getEntityProperty("postalCode");
            epAgentLocality = etAgent.getEntityProperty("locality");
            epAgentStreetAddress = etAgent.getEntityProperty("streetAddress");

            npDtstPublisher = etDataset.getNavigationPropertyEntity("Publisher");
            npDtstContactPoint = etDataset.getNavigationPropertyEntitySet("ContactPoint");
            npDtstConformsTo = etDataset.getNavigationPropertyEntitySet("ConformsTo");
            npDtstCreators = etDataset.getNavigationPropertyEntitySet("Creators");
            npDtstDataServices = etDataset.getNavigationPropertyEntitySet("DataServices");
            npDtstDatastreams = etDataset.getNavigationPropertyEntitySet("Datastreams");
            npDtstDistributions = etDataset.getNavigationPropertyEntitySet("Distributions");
            epDtstTitle = etDataset.getEntityProperty("title");
            epDtstDescription = etDataset.getEntityProperty("description");
            epDtstTemporalResolution = etDataset.getEntityProperty("temporalResolution");
            epDtstKeywords = etDataset.getEntityProperty("keywords");
            epDtstSpatialResolution = etDataset.getEntityProperty("spatialResolution");

            npDbtnAccessServices = etDistribution.getNavigationPropertyEntitySet("AccessServices");
            npDbtnConformsTo = etDistribution.getNavigationPropertyEntitySet("ConformsTo");
            npDbtnLicense = etDistribution.getNavigationPropertyEntity("License");
            epDbtnAccessURL = etDistribution.getEntityProperty("accessURL");
            epDbtnTitle = etDistribution.getEntityProperty("title");
            epDbtnDescription = etDistribution.getEntityProperty("description");
            epDbtnFormat = etDistribution.getEntityProperty("format");
            epDbtnAvailability = etDistribution.getEntityProperty("availability");

            epStdrdDefinition = etStandard.getEntityProperty("definition");
            epStdrdTitle = etStandard.getEntityProperty("title");

            epLicenseDefinition = etLicense.getEntityProperty("definition");
            epLicenseTitle = etLicense.getEntityProperty("title");

            npDatSrvConformsTo = etDataService.getNavigationPropertyEntitySet("ConformsTo");
            npDatSrvLicense = etDataService.getNavigationPropertyEntity("License");
            npDatSrvPublisher = etDataService.getNavigationPropertyEntity("Publisher");
            epDatSrvTitle = etDataService.getEntityProperty("title");
            epDatSrvEndpointURL = etDataService.getEntityProperty("endpointURL");

            npDtstrmObservedProperty = etDatastream.getNavigationPropertyEntity("ObservedProperty");
            npObspropDatastreams = etObservedProperty.getNavigationPropertyEntitySet("Datastreams");
            epObspropName = etObservedProperty.getEntityProperty("name");
            npDtstrmDatasets = etDatastream.getNavigationPropertyEntitySet("Datasets");

            if (pm.isPluginEnabled(PluginCoreModel.class)) {
                colNameDsPhenEnd = "PHENOMENON_TIME_END";
                colNameDsPhenStart = "PHENOMENON_TIME_START";
                colNameDsObservedArea = "OBSERVED_AREA";
                colNameDsId = "ID";
            } else if (pm.isPluginEnabled(PluginCoreModelV2.class)) {
                colNameDsPhenEnd = "phenomenon_time_end";
                colNameDsPhenStart = "phenomenon_time_start";
                colNameDsObservedArea = "observed_area";
                colNameDsId = "id";
            } else {
                colNameDsPhenEnd = "UNKNOWN";
                colNameDsPhenStart = "UNKNOWN";
                colNameDsObservedArea = "UNKNOWN";
                colNameDsId = "UNKNOWN";
            }
            tblNameDsDtst = "dataset_datastream";
            colNameDsDtstDatasetId = "dataset_id";
            colNameDsDtstDatastreamId = "datastream_id";
        }
    }

    private static class DCATAP {

        public static final String NS = "http://data.europa.eu/r5r/";
        public static final Property AVAILABILITY = new PropertyImpl(NS, "availability");

    }

    private static class FOAF {

        public static final String NS = "http://xmlns.com/foaf/0.1/";
        public static final Property NAME = new PropertyImpl(NS, "name");

    }

    private static class VCARD {

        public static final String NS = "http://www.w3.org/2006/vcard/ns#";
        public static final Property hasEmail = new PropertyImpl(NS, "hasEmail");
        public static final Property hasTelephone = new PropertyImpl(NS, "hasTelephone");
        public static final Property hasStreetAddress = new PropertyImpl(NS, "hasStreetAddress");
        public static final Property hasLocality = new PropertyImpl(NS, "hasLocality");
        public static final Property hasPostalCode = new PropertyImpl(NS, "hasPostalCode");
        public static final Property hasCountryName = new PropertyImpl(NS, "hasCountryName");
    }

    private static class LOCN {

        public static final String NS = "http://www.w3.org/ns/locn#";
        public static final Property geometry = new PropertyImpl(NS, "geometry");
    }

    private static class Cache {

        Map<PkValue, Resource> accessServices = new HashMap<>();
        Map<PkValue, Resource> agents = new HashMap<>();
        Map<PkValue, Resource> distributions = new HashMap<>();
        Map<PkValue, Resource> licenses = new HashMap<>();
        Map<PkValue, Resource> standards = new HashMap<>();

    }

    @Override
    public ServiceResponse execute(Service mainService, ServiceRequest request, ServiceResponse response) {
        PersistenceManager pm = mainService.getPm();
        JooqPersistenceManager jpm = null;
        if (pm instanceof JooqPersistenceManager jpml) {
            jpm = jpml;
        }

        if (mdl == null) {
            mdl = new Mdl(request.getCoreSettings().getModelRegistry(), request.getCoreSettings().getPluginManager());
        }

        ResourcePath path = request.newPath("/Datasets");
        Query query = request.newQuery()
                .setPath(path)
                .addExpand(new Expand(mdl.npDtstCreators))
                .addExpand(new Expand(mdl.npDtstPublisher))
                .addExpand(new Expand(mdl.npDtstDataServices))
                .addExpand(new Expand(mdl.npDtstConformsTo))
                .addExpand(new Expand(mdl.npDtstContactPoint))
                .addExpand(new Expand(mdl.npDtstDistributions)
                        .setSubQuery(request.newQuery()
                                .setPath(path)
                                .addExpand(new Expand(mdl.npDbtnAccessServices)
                                        .setSubQuery(request.newQuery()
                                                .setPath(path)
                                                .addExpand(new Expand(mdl.npDatSrvPublisher))
                                                .addExpand(new Expand(mdl.npDatSrvLicense))
                                                .addExpand(new Expand(mdl.npDatSrvConformsTo))))
                                .addExpand(new Expand(mdl.npDbtnConformsTo))))
                .addExpand(new Expand(mdl.npDtstDatastreams)
                        .setSubQuery(request.newQuery()
                                .setPath(path)
                                .addExpand(new Expand(mdl.npDtstrmObservedProperty))))
                .validate();
        EntitySet datasets = pm.get(path, query, EntitySet.class);

        Cache cache = new Cache();

        Model model = ModelFactory.createDefaultModel();

        model.setNsPrefix("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
        model.setNsPrefix("dct", DCTerms.NS);
        model.setNsPrefix("dcat", DCAT.NS);
        model.setNsPrefix("foaf", FOAF.NS);
        model.setNsPrefix("vcard", VCARD.NS);
        model.setNsPrefix("locn", LOCN.NS);

        for (Entity dataset : datasets) {
            Resource datasetRs = model.createResource(dataset.getSelfLink(), DCAT.Dataset);
            addLiteralIfNotNull(datasetRs, DCTerms.title, dataset, mdl.epDtstTitle);
            addLiteralIfNotNull(datasetRs, DCTerms.description, dataset, mdl.epDtstDescription);
            addLiteralIfNotNull(datasetRs, DCAT.temporalResolution, dataset, mdl.epDtstTemporalResolution);
            addLiteralIfNotNull(datasetRs, DCAT.spatialResolutionInMeters, dataset, mdl.epDtstSpatialResolution);
            String keywords = dataset.getProperty(mdl.epDtstKeywords);
            if (!StringHelper.isNullOrEmpty(keywords)) {
                String[] split = StringUtils.split(keywords, ", ");
                for (String keyword : split) {
                    datasetRs.addLiteral(DCAT.keyword, keyword);
                }
            }

            ResourcePath obsPropsPath = request.newPath("/ObservedProperties");
            final Object dataSetId = dataset.getPrimaryKeyValues().get(0);
            Query obsPropsQuery = request.newQuery()
                    .setPath(obsPropsPath)
                    .setFilter(new Equal(
                            new Path(mdl.npObspropDatastreams.getName(), mdl.npDtstrmDatasets.getName(), "id"),
                            ConstantFactory.of(dataSetId)))
                    .validate();
            EntitySet obsProps = pm.get(obsPropsPath, obsPropsQuery, EntitySet.class);
            for (Entity obsProp : obsProps) {
                datasetRs.addLiteral(DCAT.keyword, obsProp.getProperty(mdl.epObspropName));
            }

            for (Entity standard : dataset.getProperty(mdl.npDtstConformsTo)) {
                final Resource standardRs = getOrCreateStandard(cache, model, standard);
                datasetRs.addProperty(DCTerms.conformsTo, standardRs);
            }
            for (Entity agent : dataset.getProperty(mdl.npDtstContactPoint)) {
                if (agent != null) {
                    final Resource agentRs = getOrCreateAgent(cache, model, agent);
                    datasetRs.addProperty(DCAT.contactPoint, agentRs);
                }
            }
            {
                Entity agent = dataset.getProperty(mdl.npDtstPublisher);
                if (agent != null) {
                    final Resource agentRs = getOrCreateAgent(cache, model, agent);
                    datasetRs.addProperty(DCTerms.publisher, agentRs);
                }
            }
            for (Entity agent : dataset.getProperty(mdl.npDtstCreators)) {
                final Resource agentRs = getOrCreateAgent(cache, model, agent);
                datasetRs.addProperty(DCTerms.creator, agentRs);
            }
            for (Entity distribution : dataset.getProperty(mdl.npDtstDistributions)) {
                final Resource distributionRs = getOrCreateDistribution(cache, model, distribution);
                datasetRs.addProperty(DCAT.distribution, distributionRs);
            }

            if (jpm != null) {
                DSLContext dslContext = jpm.getDslContext();
                TableCollection tc = jpm.getTableCollection();
                StaMainTable<?> tDs = tc.getTableForType(mdl.etDatastream);
                StaTable tDsDtst = tc.getTableForName(mdl.tblNameDsDtst);
                final TableField fieldDsId = (TableField) tDs.field(mdl.colNameDsId);
                final TableField fieldDsDtstDatastreamId = (TableField) tDsDtst.field(mdl.colNameDsDtstDatastreamId);
                final TableField fieldDsDtstDatasetId = (TableField) tDsDtst.field(mdl.colNameDsDtstDatasetId);
                Record3<Moment, Moment, Object> result;
                if (dataset.getProperty(mdl.npDtstDatastreams).isEmpty()) {
                    result = dslContext
                            .select(
                                    DSL.min(tDs.field(mdl.colNameDsPhenStart, MomentBinding.dataType())),
                                    DSL.max(tDs.field(mdl.colNameDsPhenEnd, MomentBinding.dataType())),
                                    DSL.field("ST_AsText(ST_ConvexHull(ST_Collect(?)))", DSL.name(mdl.colNameDsObservedArea)))
                            .from(tDs)
                            .fetchOne();
                } else {
                    result = dslContext
                            .select(
                                    DSL.min(tDs.field(mdl.colNameDsPhenStart, MomentBinding.dataType())),
                                    DSL.max(tDs.field(mdl.colNameDsPhenEnd, MomentBinding.dataType())),
                                    DSL.field("ST_AsText(ST_ConvexHull(ST_Collect(?)))", DSL.name(mdl.colNameDsObservedArea)))
                            .from(tDs)
                            .innerJoin(tDsDtst)
                            .on(fieldDsId.equal(fieldDsDtstDatastreamId))
                            .where(fieldDsDtstDatasetId.eq(dataSetId))
                            .fetchOne();
                }

                if (result != null) {
                    Moment start = result.component1();
                    Moment end = result.component2();
                    String area = (String) result.component3();
                    LOGGER.info("{} {} {}", start, end, area);
                    if (start != null && end != null) {
                        Resource periodOfTime = model.createResource(DCTerms.PeriodOfTime);
                        GregorianCalendar startCal = GregorianCalendar.from(ZonedDateTime.ofInstant(start.toTemporalAccessor(), ZoneId.systemDefault()));
                        GregorianCalendar endCal = GregorianCalendar.from(ZonedDateTime.ofInstant(end.toTemporalAccessor(), ZoneId.systemDefault()));
                        periodOfTime.addLiteral(DCAT.startDate, new XSDDateTime(startCal));
                        periodOfTime.addLiteral(DCAT.endDate, new XSDDateTime(endCal));
                        datasetRs.addProperty(DCTerms.temporal, periodOfTime);
                    }
                    if (!StringHelper.isNullOrEmpty(area)) {
                        try {
                            Resource location = model.createResource(DCTerms.Location);
                            GeometryWrapper gepSparql = WKTDatatype.INSTANCE.parse(area);
                            location.addProperty(LOCN.geometry, gepSparql.asLiteral());
                            datasetRs.addProperty(DCTerms.spatial, location);
                        } catch (DatatypeFormatException ex) {
                            LOGGER.info("Failed to generate Location: {}", ex.getMessage());
                        }
                    }
                }
            }

        }
        response.setContentType(CONTENT_TYPE_APPLICATION_RDF_XML);
        model.write(response.getWriter(), "RDF/XML-ABBREV", null);
        return response;
    }

    private Resource getOrCreateStandard(Cache cache, Model model, Entity standard) {
        final PkValue standardPk = standard.getPrimaryKeyValues();
        final Resource standardRs;
        if (cache.standards.containsKey(standardPk)) {
            return cache.standards.get(standardPk);
        } else {
            String def = standard.getProperty(mdl.epStdrdDefinition);
            if (StringHelper.isNullOrEmpty(def)) {
                def = standard.getSelfLink();
            }
            standardRs = model.createResource(def, DCTerms.Standard);
            addLiteralIfNotNull(standardRs, DCTerms.title, standard, mdl.epStdrdTitle);
            cache.standards.put(standardPk, standardRs);
            return standardRs;
        }
    }

    private Resource getOrCreateDistribution(Cache cache, Model model, Entity distribution) {
        final PkValue distPk = distribution.getPrimaryKeyValues();
        if (cache.distributions.containsKey(distPk)) {
            return cache.distributions.get(distPk);
        } else {
            String def = distribution.getSelfLink();
            Resource distRs = model.createResource(def, DCAT.Distribution);
            addLiteralIfNotNull(distRs, DCAT.accessURL, distribution, mdl.epDbtnAccessURL);
            addLiteralIfNotNull(distRs, DCATAP.AVAILABILITY, distribution, mdl.epDbtnAvailability);
            addLiteralIfNotNull(distRs, DCTerms.description, distribution, mdl.epDbtnDescription);
            addLiteralIfNotNull(distRs, DCTerms.format, distribution, mdl.epDbtnFormat);
            addLiteralIfNotNull(distRs, DCTerms.title, distribution, mdl.epDbtnTitle);

            for (Entity standard : distribution.getProperty(mdl.npDbtnConformsTo)) {
                final Resource standardRs = getOrCreateStandard(cache, model, standard);
                distRs.addProperty(DCTerms.conformsTo, standardRs);
            }
            for (Entity accService : distribution.getProperty(mdl.npDbtnAccessServices)) {
                final Resource accServiceRs = getOrCreateAccessService(cache, model, accService);
                distRs.addProperty(DCAT.accessService, accServiceRs);
            }
            Entity license = distribution.getProperty(mdl.npDbtnLicense);
            if (license != null) {
                Resource licenseRs = getOrCreateLicense(cache, model, license);
                distRs.addProperty(DCTerms.license, licenseRs);
            }

            cache.distributions.put(distPk, distRs);
            return distRs;
        }
    }

    private Resource getOrCreateAccessService(Cache cache, Model model, Entity accService) {
        final PkValue agentPk = accService.getPrimaryKeyValues();
        if (cache.accessServices.containsKey(agentPk)) {
            return cache.accessServices.get(agentPk);
        } else {
            String def = accService.getSelfLink();
            Resource accServiceRs = model.createResource(def, DCAT.DataService);
            addLiteralIfNotNull(accServiceRs, DCAT.endpointURL, accService, mdl.epDatSrvEndpointURL);
            addLiteralIfNotNull(accServiceRs, DCTerms.title, accService, mdl.epDatSrvTitle);

            for (Entity standard : accService.getProperty(mdl.npDatSrvConformsTo)) {
                final Resource standardRs = getOrCreateStandard(cache, model, standard);
                accServiceRs.addProperty(DCTerms.conformsTo, standardRs);
            }
            Entity agent = accService.getProperty(mdl.npDatSrvPublisher);
            if (agent != null) {
                final Resource agentRs = getOrCreateAgent(cache, model, agent);
                accServiceRs.addProperty(DCTerms.publisher, agentRs);
            }

            Entity license = accService.getProperty(mdl.npDatSrvLicense);
            if (license != null) {
                Resource licenseRs = getOrCreateLicense(cache, model, license);
                accServiceRs.addProperty(DCTerms.license, licenseRs);
            }

            cache.accessServices.put(agentPk, accServiceRs);
            return accServiceRs;
        }
    }

    private Resource getOrCreateAgent(Cache cache, Model model, Entity agent) {
        final PkValue agentPk = agent.getPrimaryKeyValues();
        if (cache.agents.containsKey(agentPk)) {
            return cache.agents.get(agentPk);
        } else {
            String def = agent.getProperty(mdl.epAgentDefinition);
            if (StringHelper.isNullOrEmpty(def)) {
                def = agent.getSelfLink();
            }
            Resource agentRs = model.createResource(def, DCTerms.Agent);
            addLiteralIfNotNull(agentRs, FOAF.NAME, agent, mdl.epAgentName);
            addLiteralIfNotNull(agentRs, VCARD.hasCountryName, agent, mdl.epAgentCountryName);
            addLiteralIfNotNull(agentRs, VCARD.hasEmail, agent, mdl.epAgentEmail);
            addLiteralIfNotNull(agentRs, VCARD.hasLocality, agent, mdl.epAgentLocality);
            addLiteralIfNotNull(agentRs, VCARD.hasPostalCode, agent, mdl.epAgentPostalCode);
            addLiteralIfNotNull(agentRs, VCARD.hasCountryName, agent, mdl.epAgentCountryName);
            addLiteralIfNotNull(agentRs, VCARD.hasStreetAddress, agent, mdl.epAgentStreetAddress);
            addLiteralIfNotNull(agentRs, VCARD.hasTelephone, agent, mdl.epAgentTelephone);
            cache.agents.put(agentPk, agentRs);
            return agentRs;
        }
    }

    private Resource getOrCreateLicense(Cache cache, Model model, Entity license) {
        final PkValue licensePk = license.getPrimaryKeyValues();
        if (cache.licenses.containsKey(licensePk)) {
            return cache.licenses.get(licensePk);
        } else {
            String def = license.getProperty(mdl.epLicenseDefinition);
            if (StringHelper.isNullOrEmpty(def)) {
                def = license.getSelfLink();
            }
            Resource licenseRs = model.createResource(def, DCTerms.license);
            addLiteralIfNotNull(licenseRs, DCTerms.title, license, mdl.epLicenseTitle);
            cache.licenses.put(licensePk, licenseRs);
            return licenseRs;
        }
    }

    public static final void addLiteralIfNotNull(Resource target, Property p, Entity source, EntityPropertyMain ep) {
        Object value = source.getProperty(ep);
        if (value == null) {
            return;
        }
        target.addLiteral(p, value);
    }
}
