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

package ch.puzzle.itc.mobiliar.business.generator.control.templateutils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import ch.puzzle.itc.mobiliar.business.generator.control.TemplateUtils;
import ch.puzzle.itc.mobiliar.business.property.entity.PropertyDescriptorEntity;
import ch.puzzle.itc.mobiliar.business.property.entity.PropertyEntity;
import ch.puzzle.itc.mobiliar.business.template.entity.TemplateDescriptorEntity;

public class TemplateUtilsForResourceTypeTest extends AbstractTemplateUtilsTest{

	@Test
	public void getTemplatesForResourceTypeInGlobal(){
		Set<TemplateDescriptorEntity> templates = TemplateUtils.getTemplates(globalContext, applicationType, null);
		assertTrue(templates.contains(applicationTypeTemplate));
		assertEquals(templates.size(), 1);
	}
	
	@Test
	public void getPropertyDescriptorsForResourceTypeInGlobal(){
		Set<PropertyDescriptorEntity> properties = TemplateUtils.getPropertyDescriptors(globalContext, applicationType, null);
				
		assertTrue(properties.contains(applicationTypeProperty));
		assertEquals(properties.size(), 1);
	}
	
	@Test
	public void getTemplatesForResourceTypeInDev(){
		Set<TemplateDescriptorEntity> templates = TemplateUtils.getTemplates(devContext, applicationType, null);
		
		assertTrue(templates.contains(applicationTypeTemplate));
		assertEquals(templates.size(), 1);
	}
	
	@Test
	public void getPropertyDescriptorsForResourceTypeInDev(){
		Set<PropertyDescriptorEntity> properties = TemplateUtils.getPropertyDescriptors(devContext, applicationType, null);
		
		assertTrue(properties.contains(applicationTypeProperty));
		assertEquals(properties.size(), 1);
	}
	
	
	@Test
	public void getValueForResourceTypePropertyInGlobal(){	
		List<PropertyEntity> properties = TemplateUtils.getValueForProperty(applicationType, applicationTypeProperty, globalContext);
		assertNotNull(properties);
		assertEquals(properties.size(), 1);
		assertEquals(properties.get(0), applicationTypePropertyValueGlobal);		
	}
	
	@Test
	public void getValueForResourceTypePropertyInDev(){	
		List<PropertyEntity> properties = TemplateUtils.getValueForProperty(applicationType, applicationTypeProperty, devContext);
		assertNotNull(properties);
		assertEquals(properties.size(), 2);
		assertEquals(properties.get(0), applicationTypePropertyValueDev);	
		assertEquals(properties.get(1), applicationTypePropertyValueGlobal);	
	}
	
	
	@Test
	public void getPropertyValuesForResourceTypeInGlobal(){
		Map<PropertyDescriptorEntity, List<PropertyEntity>> propertyValues = TemplateUtils.getPropertyValues(applicationType, TemplateUtils.getPropertyDescriptors(globalContext, applicationType, null), globalContext);
		
		assertTrue(propertyValues.containsKey(applicationTypeProperty));
		assertNotNull(propertyValues.get(applicationTypeProperty));
		assertEquals(propertyValues.get(applicationTypeProperty).size(), 1);
		assertEquals(propertyValues.get(applicationTypeProperty).toArray()[0], applicationTypePropertyValueGlobal);
				
	}
		
	
	@Test
	public void getPropertyValuesForResourceTypeInDev(){
		Map<PropertyDescriptorEntity, List<PropertyEntity>> propertyValues = TemplateUtils.getPropertyValues(applicationType, TemplateUtils.getPropertyDescriptors(devContext, applicationType, null), devContext);
		
		assertTrue(propertyValues.containsKey(applicationTypeProperty));
		assertNotNull(propertyValues.get(applicationTypeProperty));
		
		//Es müssen sowohl die Property-Werte von Dev als auch von Global vorhanden sein
		assertEquals(propertyValues.get(applicationTypeProperty).size(), 2);
		
		//Der Wert von Dev ist höher priorisiert und kommt somit vor dem Wert von Global
		assertEquals(propertyValues.get(applicationTypeProperty).get(0), applicationTypePropertyValueDev);
		assertEquals(propertyValues.get(applicationTypeProperty).get(1), applicationTypePropertyValueGlobal);
	}
	

}
