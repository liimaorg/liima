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

package ch.puzzle.itc.mobiliar.business.environment.entity;

import ch.puzzle.itc.mobiliar.business.property.entity.PropertyDescriptorEntity;
import ch.puzzle.itc.mobiliar.business.property.entity.PropertyEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class ContextEntityTest {

	PropertyEntity propertyEntity;

	@BeforeEach
	public void setUp() {
		propertyEntity = new PropertyEntity();
		propertyEntity.setId(Integer.valueOf(1));

	}

	@Test
	public void addProperty() {
		// given
		ContextEntity c = new ContextEntity();

		// when
		c.addProperty(propertyEntity);

		// then
		assertNotNull(c.getProperties());
		assertEquals(1, c.getProperties().size());
	}

	@Test
	public void removeProperty_NotSameID() {
		// given
		ContextEntity c = new ContextEntity();
		c.addProperty(propertyEntity);

		PropertyEntity property = new PropertyEntity();
		property.setId(Integer.valueOf(2));
		// when
		c.removeProperty(property);

		// then
		assertNotNull(c.getProperties());
		assertEquals(1, c.getProperties().size());

	}

	@Test
	public void removeProperty_SameID() {
		// given
		ContextEntity c = new ContextEntity();
		c.addProperty(propertyEntity);

		PropertyEntity property = new PropertyEntity();
		property.setId(Integer.valueOf(1));
		// when
		c.removeProperty(property);

		// then
		assertNotNull(c.getProperties());
		assertEquals(0, c.getProperties().size());
	}

	@Test
	public void removeProperty_Object() {
		// given
		ContextEntity c = new ContextEntity();
		c.addProperty(propertyEntity);

		// when
		c.removeProperty(propertyEntity);

		// then
		assertNotNull(c.getProperties());
		assertEquals(0, c.getProperties().size());
	}

	@Test
	public void replacePropertyDescriptor_null() {
		// given
		ContextEntity c = new ContextEntity();
		
		PropertyDescriptorEntity propertyDescriptor2remove = new PropertyDescriptorEntity();
		propertyDescriptor2remove.setId(Integer.valueOf(2));
		
		// when
		c.replacePropertyDescriptor(propertyDescriptor2remove);

		// then
		assertNull(c.getPropertyDescriptors());
	}
	
	@Test
	public void replacePropertyDescriptor_not_available() {
		// given
		ContextEntity c = new ContextEntity();

		PropertyDescriptorEntity propertyDescriptor = new PropertyDescriptorEntity();
		propertyDescriptor.setId(Integer.valueOf(1));
		
		c.addPropertyDescriptor(propertyDescriptor);
		
		PropertyDescriptorEntity propertyDescriptor2remove = new PropertyDescriptorEntity();
		propertyDescriptor2remove.setId(Integer.valueOf(2));
		
		// when
		c.replacePropertyDescriptor(propertyDescriptor2remove);

		// then
		assertNotNull(c.getPropertyDescriptors());
		assertEquals(Integer.valueOf(1), new ArrayList<PropertyDescriptorEntity>(c.getPropertyDescriptors()).get(0).getId());
	}
	
	@Test
	public void replacePropertyDescriptor_replace() {
		// given
		ContextEntity c = new ContextEntity();

		PropertyDescriptorEntity propertyDescriptor = new PropertyDescriptorEntity();
		propertyDescriptor.setId(Integer.valueOf(1));
		propertyDescriptor.setPropertyName("test");
		
		c.addPropertyDescriptor(propertyDescriptor);
		
		PropertyDescriptorEntity propertyDescriptor2remove = new PropertyDescriptorEntity();
		propertyDescriptor2remove.setId(Integer.valueOf(1));
		propertyDescriptor2remove.setPropertyName("test-toreplace");
		
		// when
		c.replacePropertyDescriptor(propertyDescriptor2remove);

		// then
		assertNotNull(c.getPropertyDescriptors());
		assertEquals("test-toreplace", new ArrayList<PropertyDescriptorEntity>(c.getPropertyDescriptors()).get(0).getPropertyName());
	}
	
	

}
