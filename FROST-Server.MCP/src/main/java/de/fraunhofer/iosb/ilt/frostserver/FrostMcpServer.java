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
package de.fraunhofer.iosb.ilt.frostserver;

import de.fraunhofer.iosb.ilt.frostserver.auth.basic.BasicAuthFilter;
import de.fraunhofer.iosb.ilt.frostserver.auth.keycloak.KeycloakFilter;
import de.fraunhofer.iosb.ilt.frostserver.mcp.McpSettings;
import de.fraunhofer.iosb.ilt.frostserver.mcp.resource.FrostResourceHandler;
import de.fraunhofer.iosb.ilt.frostserver.mcp.session.PrincipalExtractor;
import de.fraunhofer.iosb.ilt.frostserver.mcp.tool.FrostTool;
import de.fraunhofer.iosb.ilt.frostserver.mcp.tool.FrostToolCreate;
import de.fraunhofer.iosb.ilt.frostserver.mcp.tool.FrostToolDelete;
import de.fraunhofer.iosb.ilt.frostserver.mcp.tool.FrostToolGet;
import de.fraunhofer.iosb.ilt.frostserver.mcp.tool.FrostToolHandler;
import de.fraunhofer.iosb.ilt.frostserver.mcp.tool.FrostToolQuery;
import de.fraunhofer.iosb.ilt.frostserver.mcp.tool.FrostToolReplace;
import de.fraunhofer.iosb.ilt.frostserver.mcp.tool.FrostToolUpdate;
import de.fraunhofer.iosb.ilt.frostserver.mcp.util.StaJsonHelper;
import de.fraunhofer.iosb.ilt.frostserver.messagebus.MessageBusFactory;
import de.fraunhofer.iosb.ilt.frostserver.persistence.PersistenceManager;
import de.fraunhofer.iosb.ilt.frostserver.persistence.PersistenceManagerFactory;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.JooqPersistenceManager;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import de.fraunhofer.iosb.ilt.frostserver.util.GitVersionInfo;
import de.fraunhofer.iosb.ilt.frostserver.util.MetricsSettings;
import de.fraunhofer.iosb.ilt.frostserver.util.StringHelper;
import io.modelcontextprotocol.json.jackson3.JacksonMcpJsonMapper;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.transport.HttpServletStreamableServerTransportProvider;
import io.prometheus.metrics.exporter.httpserver.HTTPServer;
import io.prometheus.metrics.instrumentation.jvm.JvmMetrics;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.FilterRegistration;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Properties;
import java.util.stream.Stream;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.ee10.servlet.ServletHolder;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

/**
 * The stand-alone FROST MCP server: boots the FROST core (settings,
 * persistence manager, optional message bus), installs the MCP auth filter
 * for the /mcp endpoint in authenticate-only mode, and serves the MCP
 * streamable-HTTP transport with the FROST tools and resources. The tool
 * layer itself lives in
 * de.fraunhofer.iosb.ilt.frostserver.mcp.tool.
 */
public class FrostMcpServer {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(FrostMcpServer.class);
    private static final String KEY_WAIT_FOR_ENTER = "WaitForEnter";
    private static final String CONFIG_FILE_NAME = "FrostMcp.properties";
    private static final String SERVLET_PATH = "/mcp";
    private static final String FILTER_NAME = "AuthFilterMcp";
    private static final String PROVIDER_BASIC = "de.fraunhofer.iosb.ilt.frostserver.auth.basic.BasicAuthProvider";
    private static final String PROVIDER_KEYCLOAK = "de.fraunhofer.iosb.ilt.frostserver.auth.keycloak.KeycloakAuthProvider";

    private static final String INSTRUCTIONS = "Tools for the OGC SensorThings API. "
            + "Use frost_query for typed entity-set queries (results carry @iot.nextLink for paging) "
            + "and frost_get for arbitrary paths (deep paths, $value, index document). "
            + "Structural details are available as frost://index and frost://model resources.";

    private final CoreSettings coreSettings;
    private final McpSettings mcpSettings;
    private HTTPServer metricsServer;
    private Server jettyServer;
    private McpSyncServer mcpServer;
    private Thread shutdownHook;

    private Class<? extends jakarta.servlet.Filter> authFilterClass;
    private final Properties authFilterInitParams = new Properties();

    public FrostMcpServer(CoreSettings coreSettings) {
        this.coreSettings = coreSettings;
        this.mcpSettings = new McpSettings(coreSettings.getSettings().getSubSettings(McpSettings.PREFIX_MCP));
    }

    private McpSettings mcpSettings() {
        return mcpSettings;
    }

    private synchronized void addShutdownHook() {
        if (this.shutdownHook == null) {
            this.shutdownHook = new Thread(() -> {
                LOGGER.info("Shutting down...");
                try {
                    stop();
                } catch (Exception ex) {
                    LOGGER.warn("Exception stopping listeners.", ex);
                }
            });
            Runtime.getRuntime().addShutdownHook(shutdownHook);
        }
    }

    public void start() throws Exception {
        addShutdownHook();

        final MetricsSettings metricsSettings = coreSettings.getMetricsSettings();
        if (metricsSettings.isInternal()) {
            startMetricsServer(metricsSettings);
        }

        PersistenceManagerFactory.init(coreSettings);
        resolveAuthFilter();
        if (authFilterClass != null) {
            // Only arm the persistence-layer role checks when an auth
            // provider is actually mapped onto /mcp — mirroring the auth
            // providers' own init, so no-auth deployments stay writable
            // through the MCP tools, exactly like through HTTP.
            installSecurityValidators();
        }
        if (mcpSettings.isStartBus()) {
            MessageBusFactory.createMessageBus(coreSettings);
        }

        startJetty();
        LOGGER.info("FROST MCP server started on port {}.", mcpSettings.getPort());
    }

    /**
     * Ensures the persistence-layer default security validators are
     * installed (insert/update/delete role checks) when an auth provider is
     * mapped onto /mcp — the same idiom the auth providers use in their
     * init when running authenticate-only.
     */
    private void installSecurityValidators() {
        try (PersistenceManager pm = PersistenceManagerFactory.getInstance(coreSettings).create()) {
            if (pm instanceof JooqPersistenceManager jpm) {
                jpm.getTableCollection().addSecurityValidator("", null);
            }
        }
    }

    public void startMetricsServer(MetricsSettings settings) {
        int metricsPort = settings.getInt(MetricsSettings.TAG_ENDPOINT_PORT);
        try {
            JvmMetrics.builder().register();
            metricsServer = HTTPServer.builder()
                    .port(metricsPort)
                    .buildAndStart();
            LOGGER.info("Prometheus metrics endpoint started on port {}", metricsPort);
        } catch (IOException ex) {
            LOGGER.error("Failed to start metrics server.", ex);
        }
    }

    /**
     * Resolves the auth provider class configured in auth.provider and
     * prepares the MCP filter registration. Basic and Keycloak map onto /mcp
     * with authenticate-only semantics; anything else fails the boot when
     * authentication is required, and starts with a warning otherwise.
     */
    private void resolveAuthFilter() {
        final String providerClassName = coreSettings.getAuthSettings()
                .get(CoreSettings.TAG_AUTH_PROVIDER, CoreSettings.class);
        if (StringHelper.isNullOrEmpty(providerClassName)) {
            failSafeNoAuthProvider(null);
            return;
        }
        switch (providerClassName) {
            case PROVIDER_BASIC ->
                authFilterClass = BasicAuthFilter.class;
            case PROVIDER_KEYCLOAK ->
                authFilterClass = KeycloakFilter.class;
            default -> {
                failSafeNoAuthProvider(providerClassName);
                return;
            }
        }
        authFilterInitParams.setProperty(CoreSettings.TAG_AUTHENTICATE_ONLY, "T");
        authFilterInitParams.setProperty(
                CoreSettings.TAG_AUTH_ALLOW_ANON_READ,
                mcpSettings.isRequireAuthenticated() ? "F" : "T");
    }

    private void failSafeNoAuthProvider(String className) {
        final String detail = StringHelper.isNullOrEmpty(className)
                ? "No auth provider configured."
                : "Auth provider '" + className + "' cannot be mapped onto the /mcp endpoint.";
        if (mcpSettings.isRequireAuthenticated()) {
            throw new IllegalStateException(detail
                    + " Refusing to start the MCP server because mcp.auth.requireAuthenticated=T "
                    + "would leave the endpoint unprotected otherwise.");
        }
        LOGGER.warn("{} The /mcp endpoint runs unprotected (mcp.auth.requireAuthenticated=F).", detail);
    }

    /**
     * Builds the MCP streamable HTTP transport: the SDK's HttpServlet on
     * /mcp, with the shared FROST JsonMapper and the principal extractor.
     *
     * @return the transport provider, an HttpServlet.
     */
    private HttpServletStreamableServerTransportProvider buildTransportProvider() {
        return HttpServletStreamableServerTransportProvider.builder()
                .jsonMapper(new JacksonMcpJsonMapper(StaJsonHelper.getJsonMapper()))
                .mcpEndpoint(SERVLET_PATH)
                .contextExtractor(new PrincipalExtractor())
                .build();
    }

    private McpSyncServer buildMcpServer(HttpServletStreamableServerTransportProvider transportProvider) {
        final FrostToolHandler toolHandler = new FrostToolHandler(coreSettings, mcpSettings);
        final FrostResourceHandler resourceHandler = new FrostResourceHandler(toolHandler);

        final McpServerFeatures.SyncToolSpecification[] tools = collectTools(toolHandler);
        final McpServer.SyncSpecification<McpServer.StreamableSyncSpecification> specification = McpServer.sync(transportProvider)
                .serverInfo("FROST-Server-MCP", GitVersionInfo.getBuildVersion())
                .instructions(INSTRUCTIONS)
                .tools(tools)
                .resources(resourceHandler.indexSpecification(), resourceHandler.modelSpecification())
                .resourceTemplates(resourceHandler.modelSetSpecification(), resourceHandler.staSpecification());

        mcpServer = specification.build();
        return mcpServer;
    }

    private McpServerFeatures.SyncToolSpecification[] collectTools(FrostToolHandler handler) {
        return Stream.of(
                toolSpec(handler, new FrostToolGet(handler)),
                toolSpec(handler, new FrostToolQuery(handler)),
                toolSpec(handler, new FrostToolCreate(handler)),
                toolSpec(handler, new FrostToolUpdate(handler)),
                toolSpec(handler, new FrostToolReplace(handler)),
                toolSpec(handler, new FrostToolDelete(handler)))
                .filter(Objects::nonNull)
                .toArray(McpServerFeatures.SyncToolSpecification[]::new);
    }

    /**
     * Returns the tool registration, or null when mcp.allowTools does not
     * include the tool — deployments can use it to expose a read-only MCP
     * (e.g. "get,query"). A disabled tool is not listed by tools/list; if it
     * is still called, the per-call gate answers with McpError -32601
     * ("Method not found") as defence in depth.
     *
     * @param handler the shared tool handler.
     * @param tool the tool.
     * @return the spec, or null when the tool is disabled.
     */
    private McpServerFeatures.SyncToolSpecification toolSpec(FrostToolHandler handler, FrostTool tool) {
        if (!handler.getMcpSettings().isToolAllowed(tool.toolName())) {
            LOGGER.info("Tool {} disabled by mcp.allowTools; not registering it.", tool.toolName());
            return null;
        }
        return tool.specification();
    }

    private void startJetty() throws Exception {
        jettyServer = new Server();
        final ServerConnector connector = new ServerConnector(jettyServer);
        connector.setHost(mcpSettings.getHost());
        connector.setPort(mcpSettings.getPort());
        jettyServer.addConnector(connector);

        final ServletContextHandler contextHandler = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
        contextHandler.setContextPath("/");
        contextHandler.setAttribute(CoreSettings.TAG_CORE_SETTINGS, coreSettings);
        jettyServer.setHandler(contextHandler);

        if (authFilterClass != null) {
            final FilterRegistration.Dynamic filter = contextHandler.getServletContext()
                    .addFilter(FILTER_NAME, authFilterClass);
            authFilterInitParams.stringPropertyNames()
                    .forEach(name -> filter.setInitParameter(name, authFilterInitParams.getProperty(name)));
            filter.addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), true, SERVLET_PATH);
        }

        final HttpServletStreamableServerTransportProvider transportProvider = buildTransportProvider();
        buildMcpServer(transportProvider);
        final ServletHolder servletHolder = new ServletHolder(transportProvider);
        servletHolder.setAsyncSupported(true);
        contextHandler.addServlet(servletHolder, SERVLET_PATH);

        jettyServer.start();
    }

    public void stop() {
        LOGGER.info("Shutting down threads...");
        try {
            Runtime.getRuntime().removeShutdownHook(shutdownHook);
        } catch (IllegalStateException ex) {
            LOGGER.trace("Already shutting down.", ex);
        }
        if (mcpServer != null) {
            mcpServer.close();
        }
        if (metricsServer != null) {
            metricsServer.stop();
        }
        if (jettyServer != null) {
            try {
                jettyServer.stop();
            } catch (Exception ex) {
                LOGGER.warn("Exception stopping Jetty.", ex);
            }
        }
        if (coreSettings.getMessageBus() != null) {
            coreSettings.getMessageBus().stop();
        }
        LOGGER.info("Done shutting down threads.");
    }

    private static CoreSettings loadCoreSettings(String configFileName) throws IOException {
        final Properties defaults = new Properties();
        defaults.setProperty(CoreSettings.TAG_TEMP_PATH, System.getProperty("java.io.tmpdir"));
        final Properties properties = new Properties(defaults);
        try (FileInputStream input = new FileInputStream(configFileName)) {
            properties.load(input);
            LOGGER.info("Read {} properties from {}.", properties.size(), configFileName);
        } catch (IOException exc) {
            LOGGER.info("Could not read properties from file: {}.", exc.getMessage());
        }
        return new CoreSettings(properties);
    }

    /**
     * @param args the command line arguments
     * @throws IOException if the config file is not found.
     */
    public static void main(String[] args) throws Exception {
        GitVersionInfo.logGitInfo();
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();

        String configFileName = CONFIG_FILE_NAME;
        if (args.length > 0) {
            configFileName = args[0];
        }
        final CoreSettings coreSettings = loadCoreSettings(configFileName);

        final FrostMcpServer server = new FrostMcpServer(coreSettings);
        server.start();

        final boolean waitForEnter = server.mcpSettings().getCustomSettings()
                .getBoolean(KEY_WAIT_FOR_ENTER, false);
        if (waitForEnter) {
            try (BufferedReader input = new BufferedReader(new InputStreamReader(System.in, StringHelper.UTF8))) {
                LOGGER.warn("Press Enter to exit.");
                final String read = input.readLine();
                LOGGER.warn("Exiting due to input {}...", read);
                server.stop();
            }
        }
    }

}
