/*
 * Copyright (C) 2021 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 */
package de.fraunhofer.iosb.ilt.frostserver.property.type;

import com.fasterxml.jackson.core.type.TypeReference;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.TypeReferencesHelper;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author hylke
 */
public class TypeComplex extends PropertyType {

    public static TypeComplex STA_GEOJSON = new TypeComplex("Sta.GeoJson", "A GeoJSON Object", TypeReferencesHelper.TYPE_REFERENCE_GEOJSONOBJECT, true);
    public static TypeComplex STA_MAP = new TypeComplex("Sta.Object", "A free object that can contain anything", TypeReferencesHelper.TYPE_REFERENCE_MAP, true);
    public static TypeComplex STA_OBJECT = new TypeComplex("Sta.ANY", "A free type, can be anything", TypeReferencesHelper.TYPE_REFERENCE_OBJECT, true);

    private final boolean openType;
    private final Map<String, PropertyType> properties = new LinkedHashMap<>();

    public TypeComplex(String name, String description, TypeReference typeReference) {
        this(name, description, typeReference, false);
    }

    public TypeComplex(String name, String description, TypeReference typeReference, boolean openType) {
        super(name, description, typeReference);
        this.openType = openType;
    }

    public boolean isOpenType() {
        return openType;
    }

    public Map<String, PropertyType> getProperties() {
        return properties;
    }

    public TypeComplex addProperty(String name, PropertyType property) {
        properties.put(name, property);
        return this;
    }

}
