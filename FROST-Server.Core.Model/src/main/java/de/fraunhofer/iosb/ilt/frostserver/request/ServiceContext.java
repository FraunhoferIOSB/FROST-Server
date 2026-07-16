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
package de.fraunhofer.iosb.ilt.frostserver.request;

import de.fraunhofer.iosb.ilt.frostserver.model.ModelRegistry;
import de.fraunhofer.iosb.ilt.frostserver.query.QueryDefaults;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.FunctionRegistry;

/**
 * A collection of service and request related instances.
 */
public class ServiceContext {

    private static final UrlPrefixGenerator PREFIX_GEN_DEFAULT = () -> "";

    private FunctionRegistry functionRegistry;
    private ModelRegistry modelRegistry;
    private QueryDefaults queryDefaults;
    private JsonReader jsonReader;
    private UrlPrefixGenerator prefixGen;
    private boolean mqttContext;

    public ServiceContext() {
        this.prefixGen = PREFIX_GEN_DEFAULT;
    }

    public FunctionRegistry getFunctionRegistry() {
        return functionRegistry;
    }

    public ServiceContext setFunctionRegistry(FunctionRegistry functionRegistry) {
        this.functionRegistry = functionRegistry;
        return this;
    }

    public JsonReader getJsonReader() {
        return jsonReader;
    }

    public ServiceContext setJsonReader(JsonReader jsonReader) {
        this.jsonReader = jsonReader;
        return this;
    }

    public ModelRegistry getModelRegistry() {
        return modelRegistry;
    }

    public ServiceContext setModelRegistry(ModelRegistry modelRegistry) {
        this.modelRegistry = modelRegistry;
        return this;
    }

    public ServiceContext setMqttContext(boolean mqttContext) {
        this.mqttContext = mqttContext;
        return this;
    }

    public boolean isMqttContext() {
        return mqttContext;
    }

    public UrlPrefixGenerator getPrefixGen() {
        return prefixGen;
    }

    public ServiceContext setPrefixGen(UrlPrefixGenerator prefixGen) {
        this.prefixGen = prefixGen;
        return this;
    }

    public QueryDefaults getQueryDefaults() {
        return queryDefaults;
    }

    public ServiceContext setQueryDefaults(QueryDefaults queryDefaults) {
        this.queryDefaults = queryDefaults;
        return this;
    }

    public ServiceContext copy() {
        return new ServiceContext()
                .setFunctionRegistry(functionRegistry)
                .setJsonReader(jsonReader)
                .setModelRegistry(modelRegistry)
                .setMqttContext(mqttContext)
                .setPrefixGen(prefixGen)
                .setQueryDefaults(queryDefaults);
    }
}
