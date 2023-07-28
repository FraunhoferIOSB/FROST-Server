/*
 * Copyright (C) 2023 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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

import de.fraunhofer.iosb.ilt.frostserver.messagebus.MessageBusFactory;
import de.fraunhofer.iosb.ilt.frostserver.mqtt.MqttManager;
import de.fraunhofer.iosb.ilt.frostserver.persistence.PersistenceManagerFactory;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import de.fraunhofer.iosb.ilt.frostserver.util.GitVersionInfo;
import de.fraunhofer.iosb.ilt.frostserver.util.StringHelper;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

/**
 *
 * @author scf
 */
public class FrostMqttServer {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(FrostMqttServer.class);
    private static final String KEY_TEMP_PATH = "tempPath";
    private static final String KEY_WAIT_FOR_ENTER = "WaitForEnter";
    private static final String CONFIG_FILE_NAME = "FrostMqtt.properties";
    private final CoreSettings coreSettings;
    private MqttManager mqttManager;
    private Thread shutdownHook;

    public FrostMqttServer(CoreSettings coreSettings) {
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
        PersistenceManagerFactory.init(coreSettings);
        MessageBusFactory.createMessageBus(coreSettings);
        mqttManager = new MqttManager(coreSettings);
        coreSettings.getMessageBus().addMessageListener(mqttManager);
    }

    public void stop() {
        LOGGER.info("Shutting down threads...");
        try {
            Runtime.getRuntime().removeShutdownHook(shutdownHook);
        } catch (IllegalStateException ex) {
            LOGGER.trace("Already shutting down.", ex);
        }
        mqttManager.shutdown();
        coreSettings.getMessageBus().stop();
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
        defaults.setProperty(KEY_TEMP_PATH, System.getProperty("java.io.tmpdir"));
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
        FrostMqttServer server = new FrostMqttServer(coreSettings);
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
