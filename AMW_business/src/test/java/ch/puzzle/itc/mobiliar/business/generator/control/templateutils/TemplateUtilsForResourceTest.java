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

import ch.puzzle.itc.mobiliar.business.generator.control.AMWTemplateExceptionHandler;
import ch.puzzle.itc.mobiliar.business.generator.control.TemplateUtils;
import ch.puzzle.itc.mobiliar.business.property.entity.PropertyDescriptorEntity;
import ch.puzzle.itc.mobiliar.business.property.entity.PropertyEntity;
import ch.puzzle.itc.mobiliar.business.template.entity.TemplateDescriptorEntity;

public class TemplateUtilsForResourceTest extends AbstractTemplateUtilsTest{

	@Test
	public void getTemplatesForResourceInGlobal(){
		Set<TemplateDescriptorEntity> templates = TemplateUtils.getTemplates(globalContext, application, null);
		
		//Es müssen sowohl die Templates von Applikation als auch von Applikationstyp vorhanden sein.
		assertTrue(templates.contains(applicationTemplate));
		assertTrue(templates.contains(applicationTypeTemplate));
		assertEquals(templates.size(), 2);		
	}
	
	
	@Test
	public void getPropertyDescriptorsForResourceInGlobal(){
		Set<PropertyDescriptorEntity> properties = TemplateUtils.getPropertyDescriptors(globalContext, application, null);
						
		/** APPLIKATION **/
		assertTrue(properties.contains(applicationProperty));

		/** APPLIKATIONSTYP **/
		assertTrue(properties.contains(applicationTypeProperty));
		
		assertEquals(properties.size(), 2);
	}
	
	@Test
	public void getTemplatesForResourceInDev(){
		Set<TemplateDescriptorEntity> templates = TemplateUtils.getTemplates(devContext, application, null);
		
		//Es müssen sowohl die Templates von Applikation als auch von Applikationstyp vorhanden sein.
		assertTrue(templates.contains(applicationTemplate));
		assertTrue(templates.contains(applicationTypeTemplate));
		assertEquals(templates.size(), 2);
	}
	
	@Test
	public void getPropertyDescriptorsForResourceInDev(){
		Set<PropertyDescriptorEntity> properties = TemplateUtils.getPropertyDescriptors(devContext, application, null);
		
		/** APPLIKATION **/		
		assertTrue(properties.contains(applicationProperty));

		/** APPLIKATIONSTYP **/
		assertTrue(properties.contains(applicationTypeProperty));
		
		assertEquals(properties.size(), 2);
	}
	
	
	@Test
	public void getValueForResourcePropertyInGlobal() {
		AMWTemplateExceptionHandler templateExceptionHandler = new AMWTemplateExceptionHandler();
		List<PropertyEntity> properties = TemplateUtils.getValueForProperty(application, applicationProperty, globalContext, false,
				templateExceptionHandler);
		assertNotNull(properties);
		assertEquals(properties.size(), 1);
		assertEquals(properties.get(0), applicationPropertyValueGlobal);
		assertTrue(templateExceptionHandler.isSuccess());
	}
	
	@Test
	public void getValueForResourcePropertyInDev() {
		AMWTemplateExceptionHandler templateExceptionHandler = new AMWTemplateExceptionHandler();
		List<PropertyEntity> properties = TemplateUtils.getValueForProperty(application, applicationProperty, devContext, false,
				templateExceptionHandler);
		assertNotNull(properties);
		assertEquals(properties.size(), 2);
		assertEquals(properties.get(0), applicationPropertyValueDev);	
		assertEquals(properties.get(1), applicationPropertyValueGlobal);
		assertTrue(templateExceptionHandler.isSuccess());
	}
	
	
	@Test
	public void getPropertyValuesForResourceInGlobal() {
		AMWTemplateExceptionHandler templateExceptionHandler = new AMWTemplateExceptionHandler();
		Map<PropertyDescriptorEntity, List<PropertyEntity>> propertyValues = TemplateUtils.getPropertyValues(application,
				TemplateUtils.getPropertyDescriptors(globalContext, application, null), globalContext, false, templateExceptionHandler);
			
		/** APPLIKATION **/
		assertTrue(propertyValues.containsKey(applicationProperty));
		assertNotNull(propertyValues.get(applicationProperty));
		assertEquals(propertyValues.get(applicationProperty).size(), 1);
		assertEquals(propertyValues.get(applicationProperty).toArray()[0], applicationPropertyValueGlobal);
			
		/** APPLIKATIONSTYP **/
		assertTrue(propertyValues.containsKey(applicationTypeProperty));
		assertNotNull(propertyValues.get(applicationTypeProperty));
		assertEquals(propertyValues.get(applicationTypeProperty).size(), 1);
		assertEquals(propertyValues.get(applicationTypeProperty).toArray()[0], applicationTypePropertyValueGlobal);
		assertTrue(templateExceptionHandler.isSuccess());
	}
		
	
	@Test
	public void getPropertyValuesForResourceInDev() {
		AMWTemplateExceptionHandler templateExceptionHandler = new AMWTemplateExceptionHandler();
		Map<PropertyDescriptorEntity, List<PropertyEntity>> propertyValues = TemplateUtils.getPropertyValues(application,
				TemplateUtils.getPropertyDescriptors(devContext, application, null), devContext, false, templateExceptionHandler);
		
		/** APPLIKATION **/
		assertTrue(propertyValues.containsKey(applicationProperty));
		assertNotNull(propertyValues.get(applicationProperty));

		//Es müssen sowohl die Property-Werte von Dev als auch von Global vorhanden sein
		assertEquals(propertyValues.get(applicationProperty).size(), 2);
		
		//Der Wert von Dev ist höher priorisiert und kommt somit vor dem Wert von Global
		assertEquals(propertyValues.get(applicationProperty).get(0), applicationPropertyValueDev);
		assertEquals(propertyValues.get(applicationProperty).get(1), applicationPropertyValueGlobal);
			
		
		/** APPLIKATIONSTYP **/
	
		assertTrue(propertyValues.containsKey(applicationTypeProperty));
		assertNotNull(propertyValues.get(applicationTypeProperty));
		
		//Es müssen sowohl die Property-Werte von Dev als auch von Global vorhanden sein
		assertEquals(propertyValues.get(applicationTypeProperty).size(), 2);
		
		//Der Wert von Dev ist höher priorisiert und kommt somit vor dem Wert von Global
		assertEquals(propertyValues.get(applicationTypeProperty).get(0), applicationTypePropertyValueDev);
		assertEquals(propertyValues.get(applicationTypeProperty).get(1), applicationTypePropertyValueGlobal);
		assertTrue(templateExceptionHandler.isSuccess());
	}
	

}
