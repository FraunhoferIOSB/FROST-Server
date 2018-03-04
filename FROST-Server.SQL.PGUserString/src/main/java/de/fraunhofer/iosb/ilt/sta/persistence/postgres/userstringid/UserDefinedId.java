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

import de.fraunhofer.iosb.ilt.sta.model.core.AbstractEntity;
import de.fraunhofer.iosb.ilt.sta.model.id.StringId;
import de.fraunhofer.iosb.ilt.sta.util.IncompleteEntityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author koepke
 */


public class UserDefinedId {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(UserDefinedId.class);
    
    // check these for consitency!
    private boolean forcedefaultid = false;
    private boolean allowdefaultid = true;
    
    private final AbstractEntity entity;
    private final StringId id;
    private String idvalue;

    
    // constructor
    public UserDefinedId(AbstractEntity entity) {
        this.entity = entity;
        this.id = (StringId) entity.getId();
        
        if(id == null) {
            this.idvalue = null;
        }
        else {
            // keep null pointer for error handling
            this.idvalue = (String) id.getValue();
        }
    }
    
    
    // public methods
    public String getId() {
        return idvalue;
    }
    
    
    public String getOriginalId() {
        if(id == null) {
            return null;
        }
        else {
            return (String) id.getValue();
        }
    }
    
    
    public void setForceDefaultId(boolean mode) {
        forcedefaultid = mode;
        
        if(forcedefaultid) {
            allowdefaultid = true;
        }
    }
    
    
    public void setAllowDefaultId(boolean mode) {
        allowdefaultid = mode;
        
        if(! allowdefaultid) {
            forcedefaultid = false;
        }
    }
    
    
    public void parseUserId() {
        if(!checkId())
            return;
        
        // idvalue = idvalue + "testing";
        entity.setId(new StringId(idvalue));
    }
    
    
    public boolean isUserId() throws IncompleteEntityException {
        // never signal user id if default is enforced
        if(forcedefaultid) {
            LOGGER.info("enforcing default id");
            return false;
        }
        
        return checkId();
    }
    
    
    // private checks
    private boolean checkId() throws IncompleteEntityException {
        // throw exception if there is no user id and default id is forebidden
        if(idvalue == null) {
            if(allowdefaultid == false) {
                LOGGER.error("no @iot.it and default id not allowed");
                throw new IncompleteEntityException("error: no @iot.id");
            }
            else {
                LOGGER.warn("no @iot.id. Falling back to default id.");
                return false;
            }
        }
        
        // TODO: additional checks
        
        return true;
    }
}
