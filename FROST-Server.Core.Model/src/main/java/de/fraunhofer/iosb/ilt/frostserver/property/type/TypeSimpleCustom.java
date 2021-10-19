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

/**
 *
 * @author hylke
 */
public class TypeSimpleCustom extends PropertyType {

    public static final TypeSimpleCustom STA_TIMEINTERVAL = new TypeSimpleCustom("Sta.TimeInterval", "An ISO time interval.", TypeSimplePrimitive.EDM_STRING);
    public static final TypeSimpleCustom STA_TIMEVALUE = new TypeSimpleCustom("Sta.TimeValue", "An ISO time instant or time interval.", TypeSimplePrimitive.EDM_STRING);

    public TypeSimpleCustom(String name, String description, TypeSimplePrimitive underlyingType) {
        super(name, description, underlyingType.getTypeReference());
    }

}
