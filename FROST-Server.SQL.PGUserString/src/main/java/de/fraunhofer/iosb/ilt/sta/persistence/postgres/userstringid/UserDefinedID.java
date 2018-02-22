/*
 * Copyright (C) 2016 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
 * Karlsruhe, Germany.
 * Copyright (C) 2018 KIT TECO, Vincenz-Prießnitz-Str. 1, D 76131
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
package de.fraunhofer.iosb.ilt.sta.persistence.postgres.userstringid;

import de.fraunhofer.iosb.ilt.sta.model.Thing;
import de.fraunhofer.iosb.ilt.sta.model.Datastream;
import de.fraunhofer.iosb.ilt.sta.util.IncompleteEntityException;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author koepke
 */


public class UserDefinedID {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(UserDefinedID.class);
    private static final String IDKEY = "User Defined ID";
    
    // check these for consitency!
    private boolean forcedefaultid = false;
    private boolean allowdefaultid = true;
    
    private final Map<String, Object> properties;
    private String id;

    
    // constructors
    public UserDefinedID(Thing entity) {
        this.properties = entity.getProperties();
        this.id = this.properties.get(IDKEY).toString();
    }
    
    
    public UserDefinedID(Datastream entity) {
        this.properties = entity.getProperties();
        this.id = this.properties.get(IDKEY).toString();
    }
    
    
    // public methods
    public static String getKey() {
        return IDKEY;
    }
    
    
    public String getID() {
        return id;
    }
    
    
    public String getOriginalID() {
        return properties.get(IDKEY).toString();
    }
    
    
    public void setForceDefaultID(boolean mode) {
        forcedefaultid = mode;
        
        if(forcedefaultid) {
            allowdefaultid = true;
        }
    }
    
    
    public void setAllowDefaultID(boolean mode) {
        allowdefaultid = mode;
        
        if(! allowdefaultid) {
            forcedefaultid = false;
        }
    }
    
    
    public void parseUserID() {
        if(!checkID())
            return;
        
        // TODO: parse id
        id = id;
    }
    
    
    public boolean isUserID() throws IncompleteEntityException {
        // never signal user id if default is enforced
        if(forcedefaultid) {
            LOGGER.info("enforcing default id");
            return false;
        }
        
        return checkID();
    }
    
    
    // private checks
    private boolean checkID() throws IncompleteEntityException {
        // throw exception if there is no user id and default id is forebidden
        if(properties.containsKey(IDKEY) == false) {
            if(allowdefaultid == false) {
                LOGGER.error("no User Defined ID and default id not allowed");
                throw new IncompleteEntityException("error: no User Defined ID");
            }
            else {
                LOGGER.warn("no User Defined ID. Falling back to default id.");
                return false;
            }
        }
        
        // TODO: additional checks
        
        return true;
    }
}
