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

package ch.puzzle.itc.mobiliar.builders;

import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Set;

import org.mockito.Mockito;

import ch.puzzle.itc.mobiliar.business.property.entity.PropertyDescriptorEntity;
import ch.puzzle.itc.mobiliar.business.property.entity.PropertyEntity;

public class PropertyEntityBuilder extends BaseEntityBuilder {

	/**
	 * @param value
	 * @param descriptor
	 *             property will be added to descriptors property set
	 * @return
	 */
	public PropertyEntity mockPropertyEntity(String value, PropertyDescriptorEntity descriptor) {
		PropertyEntity mock = Mockito.mock(PropertyEntity.class);
		when(mock.getId()).thenReturn(getNextId());
		when(mock.getValue()).thenReturn(value);
		when(mock.getDescriptor()).thenReturn(descriptor);
		if (descriptor != null) {
			Set<PropertyEntity> properties = descriptor.getProperties() != null ? descriptor.getProperties() : new HashSet<PropertyEntity>();
			properties.add(mock);
		}
		return mock;
	}

	/**
	 * @param value
	 * @param descriptor
	 *             property will be added to descriptors property set
	 * @return
	 */
	public PropertyEntity buildPropertyEntity(String value, PropertyDescriptorEntity descriptor) {
		PropertyEntity propertyEntity = new PropertyEntity();
		propertyEntity.setId(getNextId());
		propertyEntity.setValue(value);
		propertyEntity.setDescriptor(descriptor);
		if(descriptor!=null){
			Set<PropertyEntity> properties = descriptor.getProperties() != null ? descriptor.getProperties() : new HashSet<PropertyEntity>();
			properties.add(propertyEntity);
		}
		return propertyEntity;
	}
}
