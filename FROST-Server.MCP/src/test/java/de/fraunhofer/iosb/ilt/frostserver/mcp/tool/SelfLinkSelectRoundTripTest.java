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
package de.fraunhofer.iosb.ilt.frostserver.mcp.tool;

import static de.fraunhofer.iosb.ilt.frostserver.property.SpecialNames.AT_IOT_ID;
import static de.fraunhofer.iosb.ilt.frostserver.util.Constants.VALUE_ID_TYPE_LONG;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.fraunhofer.iosb.ilt.frostserver.mcp.TestModelMcp;
import de.fraunhofer.iosb.ilt.frostserver.parser.query.DefaultFunctions;
import de.fraunhofer.iosb.ilt.frostserver.plugin.coremodel.PluginCoreService;
import de.fraunhofer.iosb.ilt.frostserver.plugin.coremodelv2.PluginCoreServiceV2;
import de.fraunhofer.iosb.ilt.frostserver.plugin.odata.PluginOData;
import de.fraunhofer.iosb.ilt.frostserver.query.Query;
import de.fraunhofer.iosb.ilt.frostserver.request.ServiceContext;
import de.fraunhofer.iosb.ilt.frostserver.request.Version;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import de.fraunhofer.iosb.ilt.frostserver.settings.McpSettings;
import de.fraunhofer.iosb.ilt.frostserver.util.user.PrincipalExtended;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Regression tests for the version-dependent name of the synthetic selfLink
 * property in $select: the query string re-rendered by the MCP tools must
 * use the version's own selfLink name ({@code @iot.selfLink} for
 * SensorThings 1.1, {@code @id} for SensorThings 2.0 and OData 4.01,
 * {@code @odata.id} for OData 4.0), so it re-parses on the same version. The
 * primary key property (canonical name {@code @iot.id}, alias
 * {@code id}) is a different property and must stay un-renamed; the two
 * concepts are kept apart in these tests.
 */
class SelfLinkSelectRoundTripTest {

    private static final String PATH_HOUSES = "Houses";

    private static CoreSettings coreSettings;
    private static FrostToolHandler handler;
    private static final List<Version> versions = List.of(
            PluginCoreService.V_1_1,
            PluginCoreServiceV2.VERSION_STA_2_0,
            PluginOData.VERSION_ODATA_40,
            PluginOData.VERSION_ODATA_401);
    private static final Map<String, String> selfLinkNameByVersionName = Map.of(
            PluginCoreService.VERSION_STA_V11_NAME, "@iot.selfLink",
            PluginCoreServiceV2.VERSION_STA_V20_NAME, "@id",
            PluginOData.VERSION_ODATA_40_NAME, "@odata.id",
            PluginOData.VERSION_ODATA_401_NAME, "@id");

    @BeforeAll
    static void beforeAll() {
        final Properties properties = new Properties();
        properties.put(McpSettings.PREFIX_MCP + McpSettings.TAG_MAX_TOP, "50");
        coreSettings = new CoreSettings();
        properties.forEach((key, value) -> coreSettings.getSettings().set(String.valueOf(key), String.valueOf(value)));
        DefaultFunctions.registerDefaultFunctions(coreSettings.getFunctionRegistry());
        coreSettings.getQueryDefaults()
                .setAlwaysOrder(false)
                .setUseAbsoluteNavigationLinks(false);
        final TestModelMcp testModel = new TestModelMcp();
        testModel.initModel(coreSettings.getModelRegistry(), VALUE_ID_TYPE_LONG);
        testModel.init(coreSettings);
        coreSettings.getModelRegistry().initFinalise();
        final McpSettings mcpSettings = new McpSettings(coreSettings, coreSettings.getSettings().getSubSettings(McpSettings.PREFIX_MCP));
        handler = new FrostToolHandler(coreSettings, mcpSettings);
    }

    /**
     * Selecting the selfLink must serialise to the version's own selfLink
     * name on every version, and the result must re-parse on the same
     * version (the MCP parse/serialise/parse pipeline, and the server's own
     * nextLinks, depend on this).
     */
    @Test
    void testSelfLinkSelectRoundTrip() {
        final PrincipalExtended principal = PrincipalExtended.ANONYMOUS_PRINCIPAL;
        for (final Version version : versions) {
            final ServiceContext context = handler.buildContext(coreSettings.getQueryDefaults().copy(), version);
            final String selfLinkName = selfLinkNameByVersionName.get(version.urlPart);
            assertNotNull(selfLinkName, "selfLink name must be known for version " + version.urlPart);

            // Parse 1: the version's own name is accepted.
            final Query first = handler.parseAndValidateQuery(context, version, principal, PATH_HOUSES, "$select=" + selfLinkName);
            assertNotNull(first.getTop(), "Entity sets get an implicit top.");

            // Serialisation must use the version's selfLink name.
            final String rendered = handler.queryStringFrom(first);
            assertTrue(rendered.contains("$select="), "Select must survive the round trip: " + rendered);
            assertEquals("$select=" + selfLinkName, extractOption(rendered, "$select"), "selfLink must render with the version's own name on " + version.urlPart);

            // Parse 2: the MCP tools and nextLinks re-parse the rendered string.
            final Query second = handler.parseAndValidateQuery(context, version, principal, PATH_HOUSES, rendered);
            assertEquals(first.getSelect(), second.getSelect(), "The rendered selfLink name must re-parse on " + version.urlPart);

            // And it is a fixpoint: rendering again gives the same string.
            assertEquals(rendered, handler.queryStringFrom(second), "The rendered query must be stable on " + version.urlPart);
        }
    }

    /**
     * The primary key property is not the selfLink: its canonical name
     * {@code @iot.id} (alias {@code id}) is the same on every version and is
     * not renamed.
     */
    @Test
    void testPrimaryKeySelectIsNotVersionRenamed() {
        final PrincipalExtended principal = PrincipalExtended.ANONYMOUS_PRINCIPAL;
        for (final Version version : versions) {
            final ServiceContext context = handler.buildContext(coreSettings.getQueryDefaults().copy(), version);
            final String rendered = render(context, version, principal, "$select=id");
            assertEquals("$select=" + AT_IOT_ID, extractOption(rendered, "$select"), "The primary key keeps its canonical name on " + version.urlPart);

            final String renderedFull = render(context, version, principal, "$select=" + AT_IOT_ID);
            assertEquals("$select=" + AT_IOT_ID, extractOption(renderedFull, "$select"), "The canonical pk name is stable on " + version.urlPart);
        }
    }

    private String render(ServiceContext context, Version version, PrincipalExtended principal, String selectOption) {
        final Query query = handler.parseAndValidateQuery(context, version, principal, PATH_HOUSES, selectOption);
        final String rendered = handler.queryStringFrom(query);
        assertNotNull(rendered, "A $select query must render to a query string.");
        return rendered;
    }

    private static String extractOption(String queryString, String option) {
        for (final String part : queryString.split("&")) {
            if (part.startsWith(option + "=")) {
                return part;
            }
        }
        return "";
    }

}
