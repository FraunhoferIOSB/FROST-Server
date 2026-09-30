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

import static de.fraunhofer.iosb.ilt.frostserver.plugin.coremodelv2.PluginCoreServiceV2.VERSION_STA_2_0;

import de.fraunhofer.iosb.ilt.frostserver.json.deserialize.JsonReaderDefault;
import de.fraunhofer.iosb.ilt.frostserver.messagebus.MessageBus;
import de.fraunhofer.iosb.ilt.frostserver.messagebus.MessageBusFactory;
import de.fraunhofer.iosb.ilt.frostserver.model.ModelRegistry;
import de.fraunhofer.iosb.ilt.frostserver.persistence.PersistenceManagerFactory;
import de.fraunhofer.iosb.ilt.frostserver.request.ServiceContext;
import de.fraunhofer.iosb.ilt.frostserver.request.ServiceRequest;
import de.fraunhofer.iosb.ilt.frostserver.request.Version;
import de.fraunhofer.iosb.ilt.frostserver.service.RequestTypeUtils;
import de.fraunhofer.iosb.ilt.frostserver.service.Service;
import de.fraunhofer.iosb.ilt.frostserver.service.ServiceResponseDefault;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import de.fraunhofer.iosb.ilt.frostserver.util.GitVersionInfo;
import de.fraunhofer.iosb.ilt.frostserver.util.MetricsSettings;
import de.fraunhofer.iosb.ilt.frostserver.util.StringHelper;
import de.fraunhofer.iosb.ilt.frostserver.util.user.PrincipalExtended;
import io.modelcontextprotocol.json.jackson3.JacksonMcpJsonMapper;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.server.transport.HttpServletStreamableServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.ServerCapabilities;
import io.modelcontextprotocol.spec.McpSchema.Tool;
import io.prometheus.metrics.exporter.httpserver.HTTPServer;
import io.prometheus.metrics.instrumentation.jvm.JvmMetrics;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

/**
 * MCP Service implementation.
 */
public class FrostMcpServer {

    private static final TypeReference<Map<String, Object>> TYPE_REF_MAP = new TypeReference<>() {
        // Empty by design.
    };

    private static final String SCHEMA_TOOL_QUERY = """
            {
                "type": "object",
                "properties": {
                    "url": { type: "string", description: "The OData 4.01 / SensorThings 2.0 query to execute." }
                },
                required: ["url"],
            }
            """;

    private static final Logger LOGGER = LoggerFactory.getLogger(FrostMcpServer.class);
    private static final String KEY_WAIT_FOR_ENTER = "WaitForEnter";
    private static final String CONFIG_FILE_NAME = "FrostMcp.properties";
    private final CoreSettings coreSettings;
    private HTTPServer metricsServer;
    private McpSyncServer mcpServer;
    private Thread shutdownHook;

    private final Version version = VERSION_STA_2_0;

    public FrostMcpServer(CoreSettings coreSettings) {
        this.coreSettings = coreSettings;
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

    public void start() {
        addShutdownHook();

        MetricsSettings metricsSettings = coreSettings.getMetricsSettings();
        if (metricsSettings.isInternal()) {
            startMetricsServer(metricsSettings);
        }

        PersistenceManagerFactory.init(coreSettings);
        MessageBusFactory.createMessageBus(coreSettings);

        ModelRegistry mr = coreSettings.getModelRegistry();
        JsonMapper jsonMapper = JsonReaderDefault.getObjectMapper(mr, version, false);

        HttpServletStreamableServerTransportProvider transportProvider = HttpServletStreamableServerTransportProvider.builder()
                .jsonMapper(new JacksonMcpJsonMapper(jsonMapper))
                .mcpEndpoint("/mcp")
                .build();

        mcpServer = McpServer.sync(transportProvider)
                .serverInfo("FROST-Server", GitVersionInfo.getBuildVersion())
                .capabilities(ServerCapabilities.builder()
                        .resources(false, true) // Resource support: subscribe=false, listChanged=true
                        .tools(true) // Enable tool support with list changes
                        .prompts(false) // Enable prompt support with list changes
                        .logging() // Enable logging support
                        .build())
                .build();

        final Map<String, Object> inputSchema = jsonMapper.readValue(SCHEMA_TOOL_QUERY, TYPE_REF_MAP);
        McpServerFeatures.SyncToolSpecification toolSpec = new McpServerFeatures.SyncToolSpecification.Builder()
                .tool(Tool.builder("OData_Query", inputSchema)
                        .description("Run an OData 4.01 or SensorThings 2.0 query.")
                        .build())
                .callHandler(this::handleOdataQuery)
                .build();
        mcpServer.addTool(toolSpec);
    }

    private CallToolResult handleOdataQuery(McpSyncServerExchange exchange, CallToolRequest request) {
        String url = Objects.toString(request.arguments().get("url"));

        final ServiceRequest serviceRequest = new ServiceRequest()
                .setContext(new ServiceContext()
                        .setPrefixGen(() -> version.urlPart + '/')
                        .setFunctionRegistry(coreSettings.getFunctionRegistry())
                        .setModelRegistry(coreSettings.getModelRegistry())
                        .setQueryDefaults(coreSettings.getQueryDefaults()))
                .setVersion(version)
                .setRequestType(RequestTypeUtils.Type_23019.READ.requestType)
                .setUrl(url)
                .setUserPrincipal(PrincipalExtended.ANONYMOUS_PRINCIPAL);

        final ServiceResponseDefault serviceResponse = new ServiceResponseDefault();
        try (Service service = new Service(coreSettings)) {
            ServiceRequest.setLocalRequest(serviceRequest);
            service.distributeRequest(serviceRequest, serviceResponse);
        } finally {
            ServiceRequest.removeLocalRequest();
        }
        final McpSchema.TextContent resultContent = McpSchema.TextContent.builder(serviceResponse.getFormattedResult()).build();
        // Resource read implementation
        return McpSchema.CallToolResult.builder()
                .addContent(resultContent)
                .build();
    }

    public void startMetricsServer(MetricsSettings settings) {
        int metricsPort = settings.getInt(MetricsSettings.TAG_ENDPOINT_PORT);
        try {
            // initialize the out-of-the-box JVM metrics
            JvmMetrics.builder().register();

            metricsServer = HTTPServer.builder()
                    .port(metricsPort)
                    .buildAndStart();
            LOGGER.info("Prometheus metrics endpoint started on port {}", metricsPort);
        } catch (IOException ex) {
            LOGGER.error("Failed to start metrics server.", ex);
        }
    }

    public void stop() {
        LOGGER.info("Shutting down threads...");
        try {
            Runtime.getRuntime().removeShutdownHook(shutdownHook);
        } catch (IllegalStateException ex) {
            LOGGER.trace("Already shutting down.", ex);
        }
        if (metricsServer != null) {
            metricsServer.stop();
        }
        if (mcpServer != null) {
            mcpServer.close();
        }
        final MessageBus messageBus = coreSettings.getMessageBus();
        if (messageBus != null) {
            messageBus.stop();
        }
        try {
            Thread.sleep(3000L);
        } catch (InterruptedException ex) {
            LOGGER.debug("Rude wakeup?", ex);
            Thread.currentThread().interrupt();
        }
        LOGGER.info("Done shutting down threads.");
    }

    private static CoreSettings loadCoreSettings(String configFileName) throws IOException {
        Properties defaults = new Properties();
        defaults.setProperty(CoreSettings.TAG_TEMP_PATH, System.getProperty("java.io.tmpdir"));
        Properties properties = new Properties(defaults);
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
     * @throws java.io.FileNotFoundException if the config file is not found.
     */
    public static void main(String[] args) throws IOException {
        GitVersionInfo.logGitInfo();
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();

        String configFileName = CONFIG_FILE_NAME;
        if (args.length > 0) {
            configFileName = args[0];
        }
        CoreSettings coreSettings = loadCoreSettings(configFileName);

        FrostMcpServer server = new FrostMcpServer(coreSettings);
        server.start();

        boolean waitForEnter = coreSettings.getMqttSettings().getCustomSettings().getBoolean(KEY_WAIT_FOR_ENTER, false);
        if (waitForEnter) {
            try (BufferedReader input = new BufferedReader(new InputStreamReader(System.in, StringHelper.UTF8))) {
                LOGGER.warn("Press Enter to exit.");
                String read = input.readLine();
                LOGGER.warn("Exiting due to input {}...", read);
                server.stop();
            }
        }
    }

}
