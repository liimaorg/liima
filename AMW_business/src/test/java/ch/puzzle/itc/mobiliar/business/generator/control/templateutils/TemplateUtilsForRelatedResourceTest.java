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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import ch.puzzle.itc.mobiliar.business.generator.control.TemplateUtils;
import ch.puzzle.itc.mobiliar.business.property.entity.PropertyDescriptorEntity;
import ch.puzzle.itc.mobiliar.business.property.entity.PropertyEntity;
import ch.puzzle.itc.mobiliar.business.template.entity.TemplateDescriptorEntity;

public class TemplateUtilsForRelatedResourceTest extends AbstractTemplateUtilsTest {

	
	@Test
	public void getTemplatesForRelatedResourceInGlobal() {
		Set<TemplateDescriptorEntity> templates = TemplateUtils.getTemplates(globalContext, application2database, null);
		
		// Es müssen sowohl die Templates von der Beziehung, dem Beziehungstyp,
		// sowie der Datenbank und des Datenbanktyps vorhanden sein.
		assertTrue(templates.contains(application2databaseTemplate));
		assertTrue(templates.contains(applicationType2databaseTypeTemplate));
		assertTrue(templates.contains(databaseTemplate));
		assertTrue(templates.contains(databaseTypeTemplate));
		assertEquals(templates.size(), 4);		
	}
	
	@Test
	public void getPropertyDescriptorsForRelatedResourceInGlobal() {
		Set<PropertyDescriptorEntity> properties = TemplateUtils.getPropertyDescriptors(globalContext, application2database, null);
		
		/** APPLIKATION 2 DB **/
		assertTrue(properties.contains(application2databaseProperty));
		
		/** APPLIKATIONSTYP 2 DB TYP **/
		assertTrue(properties.contains(applicationType2databaseTypeProperty));
		
		/** DATABASE **/
		assertTrue(properties.contains(databaseProperty));

		/** DATABASE TYP **/
		assertTrue(properties.contains(databaseTypeProperty));
		
		assertEquals(properties.size(), 4);
	}

	@Test
	public void getTemplatesForResourceInDev() {
		Set<TemplateDescriptorEntity> templates = TemplateUtils.getTemplates(devContext, application2database, null);

		// Es müssen sowohl die Templates von der Beziehung, dem Beziehungstyp,
		// sowie der Datenbank und des Datenbanktyps vorhanden sein.
		assertTrue(templates.contains(application2databaseTemplate));
		assertTrue(templates.contains(applicationType2databaseTypeTemplate));
		assertTrue(templates.contains(databaseTemplate));
		assertTrue(templates.contains(databaseTypeTemplate));
		assertEquals(templates.size(), 4);
	}
	
	@Test
	public void getPropertyDescriptorsForResourceInDev() {
		Set<PropertyDescriptorEntity> properties = TemplateUtils.getPropertyDescriptors(devContext, application2database, null);


		/** APPLIKATION 2 DB **/
		assertTrue(properties.contains(application2databaseProperty));

		/** APPLIKATIONSTYP 2 DB TYP **/
		assertTrue(properties.contains(applicationType2databaseTypeProperty));
		
		/** DATABASE **/
		assertTrue(properties.contains(databaseProperty));

		/** DATABASE TYP **/
		assertTrue(properties.contains(databaseTypeProperty));
		
		assertEquals(properties.size(), 4);
	}

	@Test
	public void getValueForRelatedResourcePropertyInGlobal() {
		List<PropertyEntity> properties = TemplateUtils.getValueForProperty(application2database, application2databaseProperty, globalContext);
		assertNotNull(properties);
		assertEquals(properties.size(), 1);
		assertEquals(properties.get(0), application2databasePropertyValueGlobal);
	}

	@Test
	public void getValueForRelatedResourcePropertyInDev() {
		List<PropertyEntity> properties = TemplateUtils.getValueForProperty(application2database, application2databaseProperty, devContext);
		assertNotNull(properties);
		assertEquals(properties.size(), 2);
		assertEquals(properties.get(0), application2databasePropertyValueDev);
		assertEquals(properties.get(1), application2databasePropertyValueGlobal);
	}

	@Test
	public void getPropertyValuesForRelatedResourceInGlobal() {
		Map<PropertyDescriptorEntity, List<PropertyEntity>> propertyValues = TemplateUtils.getPropertyValues(application2database,
				TemplateUtils.getPropertyDescriptors(globalContext, application2database, null), globalContext);
		
		/** APPLIKATION 2 DB **/
		assertTrue(propertyValues.containsKey(application2databaseProperty));
		assertNotNull(propertyValues.get(application2databaseProperty));
		assertEquals(propertyValues.get(application2databaseProperty).size(), 1);
		assertEquals(propertyValues.get(application2databaseProperty).toArray()[0], application2databasePropertyValueGlobal);

		/** APPLIKATIONSTYP 2 DB TYP **/
		assertTrue(propertyValues.containsKey(applicationType2databaseTypeProperty));
		assertNotNull(propertyValues.get(applicationType2databaseTypeProperty));
		assertEquals(propertyValues.get(applicationType2databaseTypeProperty).size(), 1);
		assertEquals(propertyValues.get(applicationType2databaseTypeProperty).toArray()[0], applicationType2databaseTypePropertyValueGlobal);

		/** DATABASE **/
		assertTrue(propertyValues.containsKey(databaseProperty));
		assertNotNull(propertyValues.get(databaseProperty));
		assertEquals(propertyValues.get(databaseProperty).size(), 2);
		assertEquals(propertyValues.get(databaseProperty).toArray()[0], databasePropertyValueOverriddenInGlobal);
		assertEquals(propertyValues.get(databaseProperty).toArray()[1], databasePropertyValueGlobal);

		/** DATABASE TYP **/
		assertTrue(propertyValues.containsKey(databaseTypeProperty));
		assertNotNull(propertyValues.get(databaseTypeProperty));
		assertEquals(propertyValues.get(databaseTypeProperty).size(), 2);
		assertEquals(propertyValues.get(databaseTypeProperty).toArray()[0], databaseTypePropertyValueOverriddenInGlobal);
		assertEquals(propertyValues.get(databaseTypeProperty).toArray()[1], databaseTypePropertyValueGlobal);
	}

	@Test
	public void getPropertyValuesForResourceInDev() {

		Map<PropertyDescriptorEntity, List<PropertyEntity>> propertyValues = TemplateUtils.getPropertyValues(application2database,
				TemplateUtils.getPropertyDescriptors(devContext, application2database, null), devContext);
		
		/** APPLIKATION 2 DB **/
		assertTrue(propertyValues.containsKey(application2databaseProperty));
		assertNotNull(propertyValues.get(application2databaseProperty));
		assertEquals(propertyValues.get(application2databaseProperty).size(), 2);
		assertEquals(propertyValues.get(application2databaseProperty).toArray()[0], application2databasePropertyValueDev);
		assertEquals(propertyValues.get(application2databaseProperty).toArray()[1], application2databasePropertyValueGlobal);

		
		/** APPLIKATIONSTYP 2 DB TYP **/
		assertTrue(propertyValues.containsKey(applicationType2databaseTypeProperty));
		assertNotNull(propertyValues.get(applicationType2databaseTypeProperty));
		assertEquals(propertyValues.get(applicationType2databaseTypeProperty).size(), 2);
		assertEquals(propertyValues.get(applicationType2databaseTypeProperty).toArray()[0], applicationType2databaseTypePropertyValueDev);
		assertEquals(propertyValues.get(applicationType2databaseTypeProperty).toArray()[1], applicationType2databaseTypePropertyValueGlobal);

		/** DATABASE **/
		assertTrue(propertyValues.containsKey(databaseProperty));
		assertNotNull(propertyValues.get(databaseProperty));
		assertEquals(propertyValues.get(databaseProperty).size(), 4);
		assertEquals(propertyValues.get(databaseProperty).toArray()[0], databasePropertyValueOverriddenInDev);
		assertEquals(propertyValues.get(databaseProperty).toArray()[1], databasePropertyValueOverriddenInGlobal);
		assertEquals(propertyValues.get(databaseProperty).toArray()[2], databasePropertyValueDev);
		assertEquals(propertyValues.get(databaseProperty).toArray()[3], databasePropertyValueGlobal);

		/** DATABASE TYP **/
		assertTrue(propertyValues.containsKey(databaseTypeProperty));
		assertNotNull(propertyValues.get(databaseTypeProperty));
		assertEquals(propertyValues.get(databaseTypeProperty).size(), 4);
		assertEquals(propertyValues.get(databaseTypeProperty).toArray()[0], databaseTypePropertyValueOverriddenInDev);
		assertEquals(propertyValues.get(databaseTypeProperty).toArray()[1], databaseTypePropertyValueOverriddenInGlobal);
		assertEquals(propertyValues.get(databaseTypeProperty).toArray()[2], databaseTypePropertyValueDev);
		assertEquals(propertyValues.get(databaseTypeProperty).toArray()[3], databaseTypePropertyValueGlobal);

	}

}
