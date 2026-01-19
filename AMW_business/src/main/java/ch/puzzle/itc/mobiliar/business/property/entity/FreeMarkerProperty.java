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

public class FreeMarkerProperty implements Comparable<FreeMarkerProperty>, Serializable {

	private static final long serialVersionUID = 1L;
	
	@Getter
	String currentValue;
    @Getter
    FreeMarkerPropertyDescriptor _descriptor;

	/**
	 * creates based on the technicalKey a default PropertyDescriptor
	 * Special case: PropertyDescriptorEntities having no PropertyEntity
	 * @param value
	 * @param technicalKey
	 */
	public FreeMarkerProperty(String value, String technicalKey) {
		PropertyDescriptorEntity desc = new PropertyDescriptorEntity();
		desc.setPropertyName(technicalKey);
		desc.setDisplayName(technicalKey);

		_descriptor = new FreeMarkerPropertyDescriptor(desc);
		currentValue = value;
	}

    public boolean hasValue(){
        return currentValue!=null;
    }

	public FreeMarkerProperty(String propertyValue, PropertyDescriptorEntity propertyDescriptorEntity) {
        if (propertyDescriptorEntity != null) {
			_descriptor = new FreeMarkerPropertyDescriptor(propertyDescriptorEntity);
		}
		currentValue = propertyValue;
	}

	/**
     *
     * This is the method which is responsible for the value actually written into the template (by
     * freemarker)
	 * @return
	 */
	@Override
	public String toString() {
		// To please FreeMarkers null String behaviour
		return getCurrentValue() != null ? getCurrentValue() : "";
	}

	@Override
	public int compareTo(FreeMarkerProperty o) {
		if (o == null || o.toString() == null) {
			return toString() == null ? 0 : -1;
		}
		if (toString() == null) {
			return 1;
		}
		return toString().compareTo(o.toString());
	}
	
	/**
	 * This Property is used in templates to differentiate whether the property is a "hash" or a AMW FreeMarkerProperty
	 * 
	 * <#if !property.amwProperty??>
	 * ...
	 * </#if>
	 * 
	 * @return
	 */
	public boolean getAmwProperty(){
		return true;
	}

    public void setEvaluatedValue(String s) {
        this.currentValue = s;
    }

    /**
     * Masks the current value if the property is encrypted.
     * Used when user doesn't have decrypt permissions.
     */
    public void maskIfEncrypted() {
        if (_descriptor != null && _descriptor.isEncrypt() && currentValue != null) {
            currentValue = "****";
        }
    }
}