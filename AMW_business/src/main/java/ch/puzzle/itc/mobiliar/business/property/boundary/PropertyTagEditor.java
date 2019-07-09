/*
 * AMW - Automated Middleware allows you to manage the configurations of
 * your Java EE applications on an unlimited number of different environments
 * with various versions, including the automated deployment of those apps.
 * Copyright (C) 2013-2016 by Puzzle ITC
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ch.puzzle.itc.mobiliar.business.property.boundary;

import ch.puzzle.itc.mobiliar.business.property.control.PropertyTagEditingService;
import ch.puzzle.itc.mobiliar.business.property.entity.PropertyTagEntity;
import ch.puzzle.itc.mobiliar.business.property.entity.PropertyTagType;
import ch.puzzle.itc.mobiliar.business.security.entity.Permission;
import ch.puzzle.itc.mobiliar.business.security.interceptor.HasPermission;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import java.util.List;

@Stateless
@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
public class PropertyTagEditor {

    @Inject
    PropertyTagEditingService propertyTagService;

    public List<PropertyTagEntity> getAllGlobalPropertyTags() {
        return propertyTagService.loadAllGlobalPropertyTagEntities(false);
    }

    /**
     * Deletes a PropertyTagEntity identified by its Id
     * @param propertyTagId
     * @return
     */
    @HasPermission(permission = Permission.MANAGE_GLOBAL_TAGS)
    public boolean deletePropertyTag(Integer propertyTagId) {
        return propertyTagService.deletePropertyTagById(propertyTagId);
    }

    /**
     * Adds a PropertyTagEntity of PropertyTagType.GLOBAL
     * @param propertyTagName
     */
    @HasPermission(permission = Permission.MANAGE_GLOBAL_TAGS)
    public void addGlobalPropertyTag(String propertyTagName) {
        addPropertyTag(propertyTagName, PropertyTagType.GLOBAL);
    }

    /**
     * Adds a PropertyTagEntity of PropertyTagType.LOCAL
     * @param propertyTagName
     */
    public void addLocalPropertyTag(String propertyTagName) {
        addPropertyTag(propertyTagName, PropertyTagType.LOCAL);
    }

    private void addPropertyTag(String propertyTagName, PropertyTagType propertyTagType) {
        if (propertyTagName != null && !propertyTagName.isEmpty()) {
            PropertyTagEntity propertyTag = new PropertyTagEntity();
            propertyTag.setName(propertyTagName);
            propertyTag.setTagType(propertyTagType);
            propertyTagService.addPropertyTag(propertyTag);
        }
    }

    /**
     * returns the PropertyTagEntities a comma separated String
     *
     * @param globalPropertyTags
     * @return
     */
    public String getTagsAsList(List<PropertyTagEntity> globalPropertyTags) {
        StringBuilder sb = new StringBuilder();
        if(globalPropertyTags != null){
            int count = 0;
            for (PropertyTagEntity tag : globalPropertyTags){
            	if(tag.getName() != null && !tag.getName().isEmpty()){
            		if(count > 0){
                        sb.append(", ");
                    }
                    sb.append("'");
                    sb.append(tag.getName());
                    sb.append("'");

                    count ++;
            	}
            }
        }
        return sb.toString();
    }

    /**
     * returns the PropertyTagEntities a comma separated String
     *
     * @param propertyTags
     * @return
     */
    public String getTagsAsCommaSeparatedString(List<PropertyTagEntity> propertyTags){
        StringBuilder result = new StringBuilder();
        if (propertyTags != null) {
            for (PropertyTagEntity tag : propertyTags) {
            	if(tag.getName() != null && !tag.getName().isEmpty()){
            		result.append(tag.getName()).append(",");
            	}
            }
        }
        return result.toString();
    }



}
