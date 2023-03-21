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
package de.fraunhofer.iosb.ilt.frostserver.model.core.annotations;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 *
 * @author hylke
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public interface Annotation {

    public enum DocType {
        JSON("json"),
        XML("xml");
        private final String defaultExtension;

        private DocType(String defaultExtension) {
            this.defaultExtension = defaultExtension;
        }

        public String getDefaultExtension() {
            return defaultExtension;
        }

    }

    /**
     * The URL of the document that defines the annotation.
     *
     * @param docType the
     * @return The URL of the document that defines the annotation.
     */
    public String getSourceUrl(DocType docType);

    /**
     * The namespace of the annotation Term.
     *
     * @return The name space of the annotation Term.
     */
    public String getNameSpace();

    /**
     * The name of the annotation Term.
     *
     * @return The name of the annotation Term.
     */
    public String getName();

    /**
     * The value of the annotation.
     *
     * @return The value of the annotation.
     */
    public Object getValue();

}
