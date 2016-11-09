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

package ch.puzzle.itc.mobiliar.business.property.entity;

import lombok.Getter;


import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;


/**
 * This class represents the PropertyDescriptor in the FreeMarker AMW Model
 */
public class FreeMarkerPropertyDescriptor implements Serializable {

    private static final long serialVersionUID = 1L;

    @Getter
    private boolean encrypt;

    @Getter
    private String technicalKey;

    @Getter
    private boolean valueOptional;

    @Getter
    private boolean testing;

    @Getter
    private String validationLogic;

    @Getter
    private String propertyComment;

    @Getter
    private Integer cardinalityProperty;

    @Getter
    private String defaultValue;

    @Getter
    private String exampleValue;

    @Getter
    private String machineInterpretationKey;

    @Getter
    private boolean keyOptional;

    @Getter
    private String displayName;

    @Getter
    private Set<String> tags = new HashSet<>();

    public FreeMarkerPropertyDescriptor(PropertyDescriptorEntity propertyDescriptorEntity) {
        if (propertyDescriptorEntity != null) {
            encrypt = propertyDescriptorEntity.isEncrypt();
            // TODO: Rename propertyDescriptorEntity.getPropertyName() to TechnicalKey
            technicalKey = propertyDescriptorEntity.getPropertyName();
            valueOptional = propertyDescriptorEntity.isNullable();
            testing = propertyDescriptorEntity.isTesting();
            validationLogic = propertyDescriptorEntity.getValidationLogic();
            propertyComment = propertyDescriptorEntity.getPropertyComment();
            cardinalityProperty = propertyDescriptorEntity.getCardinalityProperty();
            defaultValue = propertyDescriptorEntity.getDefaultValue();
            exampleValue = propertyDescriptorEntity.getExampleValue();
            machineInterpretationKey = propertyDescriptorEntity.getMachineInterpretationKey();
            keyOptional = propertyDescriptorEntity.isOptional();
            displayName = propertyDescriptorEntity.getDisplayName();

            if (propertyDescriptorEntity.getPropertyTags() != null) {
                for (PropertyTagEntity propertyTagEntity : propertyDescriptorEntity.getPropertyTags()) {
                    tags.add(propertyTagEntity.getName());
                }
            }
        }
    }

    /**
     * Checks whether a property has a certain tag
     * @param tag
     * @return
     */
    public boolean hasTag(PropertyTagEntity tag) {
        if (tag != null) {
            return hasTag(tag.getName());
        }
        return false;
    }

    /**
     * Checks whether a property has a tag with a certain tag name
     * @param tagName
     * @return
     */
    public boolean hasTag(String tagName) {
        if (tagName != null && !tagName.isEmpty()) {
            for (String tag : tags) {
                if (tag.equalsIgnoreCase(tagName)) {
                    return true;
                }
            }
        }
        return false;
    }

}
