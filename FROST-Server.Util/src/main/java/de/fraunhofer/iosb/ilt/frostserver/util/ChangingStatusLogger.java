/*
 * Copyright (C) 2020 Fraunhofer IOSB
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.fraunhofer.iosb.ilt.frostserver.util;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;

/**
 * A logger that regularly logs a status message, if the status has changed.
 *
 * @author hylke
 */
public class ChangingStatusLogger {

    /**
     * Implementations MUST override the equals method.
     */
    public static interface ChangingStatus {

        /**
         * Get the message template.
         *
         * @return the message template, with placeholders for the parameters,
         * to pass to the logger.
         */
        public String getLogMessageTemplate();

        /**
         * Get the parameters to pass to the logger when logging a message.
         *
         * @return The parameters to pass to the logger.
         */
        public Object[] getCurrentParams();

        /**
         * Get a copy of the parameters array.
         *
         * @return a copy of the parameters array.
         */
        public Object[] getCopyCurrentParams();

        /**
         * Check if the status changed, and if so, log the configured message to
         * the given logger. The method will be periodically called.
         *
         * @param logger The logger to log to.
         */
        public void logIfChanged(Logger logger);

    }

    public static class ChangingStatusDefault implements ChangingStatus {

        private final String logMessageTemplate;
        private final Object[] status;
        private Object[] previous;

        public ChangingStatusDefault(String logMessageTemplate, Object[] status) {
            this.logMessageTemplate = logMessageTemplate;
            this.status = status;
        }

        public ChangingStatusDefault(String logMessageTemplate, int paramCount) {
            this.logMessageTemplate = logMessageTemplate;
            this.status = new Object[paramCount];
        }

        public final void setAllTo(Object value) {
            for (int idx = 0; idx < status.length; idx++) {
                status[idx] = value;
            }
        }

        public final void setObjectAt(int idx, Object value) {
            status[idx] = value;
        }

        @Override
        public String getLogMessageTemplate() {
            return logMessageTemplate;
        }

        @Override
        public final Object[] getCurrentParams() {
            return status;
        }

        @Override
        public Object[] getCopyCurrentParams() {
            return Arrays.copyOf(status, status.length);
        }

        @Override
        public void logIfChanged(Logger logger) {
            Object[] currentStatus = getCopyCurrentParams();
            if (!Arrays.deepEquals(currentStatus, previous)) {
                previous = currentStatus;
                logger.info(logMessageTemplate, previous);
            }
        }

    }

    private long logIntervalMs = 1000;

    private final Logger logger;
    private final List<ChangingStatus> logStatuses = new CopyOnWriteArrayList<>();

    private ScheduledExecutorService executor;
    private final Runnable task;
    private boolean running = false;

    /**
     * Create a new logger, with the given logger, message and status. The
     * status should match the message in that the number of placeholders in the
     * message must be the same as the number of items in the status.
     *
     * @param logger The logger to log to.
     */
    public ChangingStatusLogger(Logger logger) {
        this.logger = logger;
        task = this::maybeLog;
    }

    public ChangingStatusLogger addLogStatus(ChangingStatus logStatus) {
        logStatuses.add(logStatus);
        return this;
    }

    public ChangingStatusLogger removeLogStatus(ChangingStatus logStatus) {
        logStatuses.remove(logStatus);
        logStatus.logIfChanged(logger);
        return this;
    }

    public ChangingStatusLogger setLogIntervalMs(long logIntervalMs) {
        this.logIntervalMs = logIntervalMs;
        return this;
    }

    public ChangingStatusLogger start() {
        if (running) {
            return this;
        }
        if (executor == null) {
            executor = Executors.newSingleThreadScheduledExecutor();
        }
        running = true;
        executor.scheduleAtFixedRate(task, logIntervalMs, logIntervalMs, TimeUnit.MILLISECONDS);
        return this;
    }

    public void stop() {
        if (executor != null) {
            executor.shutdown();
            executor = null;
        }
        running = false;
        maybeLog();
    }

    private void maybeLog() {
        for (ChangingStatus status : logStatuses) {
            status.logIfChanged(logger);
        }
    }

}
