/*
 * Copyright (C) 2017 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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
package de.fraunhofer.iosb.ilt.sta.persistence.postgres;

import org.joda.time.DateTime;

/**
 *
 * @author scf
 */
public interface PostgresPersistenceManager {

    public static final String TAG_DATA_SOURCE = "db_jndi_datasource";
    public static final String TAG_DB_DRIVER = "db_driver";
    public static final String TAG_DB_URL = "db_url";
    public static final String TAG_DB_USERNAME = "db_username";
    public static final String TAG_DB_PASSWORD = "db_password";

    public static final DateTime DATETIME_MAX = DateTime.parse("9999-12-31T23:59:59.999Z");
    public static final DateTime DATETIME_MIN = DateTime.parse("-4000-01-01T00:00:00.000Z");

}
