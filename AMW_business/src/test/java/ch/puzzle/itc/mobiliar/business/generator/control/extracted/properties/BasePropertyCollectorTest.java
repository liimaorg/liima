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

package ch.puzzle.itc.mobiliar.business.generator.control.extracted.properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ch.puzzle.itc.mobiliar.business.environment.entity.ContextEntity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ch.puzzle.itc.mobiliar.business.generator.control.TemplateUtils;
import ch.puzzle.itc.mobiliar.business.property.entity.FreeMarkerProperty;
import ch.puzzle.itc.mobiliar.business.property.entity.PropertyDescriptorEntity;
import ch.puzzle.itc.mobiliar.business.property.entity.PropertyEntity;
import ch.puzzle.itc.mobiliar.common.util.ConfigKey;

public class BasePropertyCollectorTest {
	
	BasePropertyCollector c = new BasePropertyCollector();

	@BeforeEach
	public void setUp(){
		// set Up Encription Key
		System.getProperties().put(ConfigKey.ENCRYPTION_KEY.getValue(), "78E76138D98F00BBF713136BC13DEE4B");
	}

	@AfterEach
	public void tearDown(){
		// set Up Encription Key
		System.getProperties().remove(ConfigKey.ENCRYPTION_KEY.getValue());
	}
	
	@Test
	public void translatePropertyList_shouldAddProperty_decriptedValue() {
		// given
		PropertyEntity p = new PropertyEntity();
		PropertyDescriptorEntity d = new PropertyDescriptorEntity();
		d.setOptional(false);
		d.setPropertyName("propName");
		d.setEncrypt(true);
		
		p.setDescriptor(d);
		p.setValue(TemplateUtils.encrypt("value"));
		
		List<PropertyEntity> properties = new ArrayList<>();
		properties.add(p);
		
		// when
		Map<String, FreeMarkerProperty> result = c.translatePropertyList(properties , null, null, null, null, null);
		
		// then
		assertNotNull(result);
		assertEquals(1, result.keySet().size());
		assertEquals("value", result.get("propName").getCurrentValue());
	}
	
	@Test
	public void translatePropertyList_shouldAddEmptyProperty_whenValueOptionalAndNoValue() {
		// given
		PropertyEntity p = new PropertyEntity();
		PropertyDescriptorEntity d = new PropertyDescriptorEntity();
		d.setOptional(false);
		d.setNullable(true);
		d.setPropertyName("propName");
		
		p.setDescriptor(d);
		p.setValue(null);
		
		List<PropertyEntity> properties = new ArrayList<>();
		properties.add(p);
		
		// when
		Map<String, FreeMarkerProperty> result = c.translatePropertyList(properties , null, null, null, null, null);
		
		// then
		assertNotNull(result);
		assertEquals(1, result.keySet().size());
		assertEquals(null, result.get("propName").getCurrentValue());
	}
	
	@Test
	public void translatePropertyList_shouldNotAddProperty_keyOptionalTrue_and_value_null() {
		// given
		PropertyEntity p = new PropertyEntity();
		PropertyDescriptorEntity d = new PropertyDescriptorEntity();
		d.setOptional(true);
		d.setPropertyName("propName");
		
		p.setDescriptor(d);
		p.setValue(null);
		
		List<PropertyEntity> properties = new ArrayList<>();
		properties.add(p);
		
		// when
		Map<String, FreeMarkerProperty> result = c.translatePropertyList(properties , null, null, null, null, null);
		
		// then
		assertNotNull(result);
		assertEquals(0, result.keySet().size());
	}
	
	@Test
	public void translatePropertyList_shouldNotAddProperty_keyOptionalTrue_and_no_value() {
		// given
		PropertyEntity p = new PropertyEntity();
		PropertyDescriptorEntity d = new PropertyDescriptorEntity();
		d.setOptional(true);
		d.setPropertyName("propName");
		
		p.setDescriptor(d);
		p.setValue("");
		
		List<PropertyEntity> properties = new ArrayList<>();
		properties.add(p);
		
		// when
		Map<String, FreeMarkerProperty> result = c.translatePropertyList(properties , null, null, null, null, null);
		
		// then
		assertNotNull(result);
		assertEquals(0, result.keySet().size());
	}
	
	@Test
	public void translatePropertyList_shouldNotAddProperty_keyOptionalTrue_and_no_value_but_default() {
		// given
		PropertyEntity p = new PropertyEntity();
		PropertyDescriptorEntity d = new PropertyDescriptorEntity();
		d.setOptional(true);
		d.setPropertyName("propName");
		d.setDefaultValue("defaultValue");
		
		p.setDescriptor(d);
		p.setValue("");
		
		List<PropertyEntity> properties = new ArrayList<>();
		properties.add(p);
		
		// when
		Map<String, FreeMarkerProperty> result = c.translatePropertyList(properties , null, null, null, null, null);
		
		// then
		assertNotNull(result);
		assertEquals(0, result.keySet().size());
	}
	
	@Test
	public void translatePropertyList_shouldAddProperty_keyOptionalFalse_and_value() {
		// given
		PropertyEntity p = new PropertyEntity();
		PropertyDescriptorEntity d = new PropertyDescriptorEntity();
		d.setOptional(false);
		d.setPropertyName("propName");
		
		p.setDescriptor(d);
		p.setValue("value");
		
		List<PropertyEntity> properties = new ArrayList<>();
		properties.add(p);
		
		// when
		Map<String, FreeMarkerProperty> result = c.translatePropertyList(properties , null, null, null, null, null);
		
		// then
		assertNotNull(result);
		assertEquals(1, result.keySet().size());
		assertEquals("value", result.get("propName").getCurrentValue());
	}
	
	@Test
	public void translatePropertyList_shouldAddProperty_withDefaultValue_keyOptionalFalse_and_no_value() {
		// given
		PropertyEntity p = new PropertyEntity();
		PropertyDescriptorEntity d = new PropertyDescriptorEntity();
		d.setOptional(false);
		d.setPropertyName("propName");
		d.setDefaultValue("defaultValue");
		
		p.setDescriptor(d);
		p.setValue(null);
		
		List<PropertyEntity> properties = new ArrayList<>();
		properties.add(p);
		
		// when
		Map<String, FreeMarkerProperty> result = c.translatePropertyList(properties , null, null, null, null, null);
		
		// then
		assertNotNull(result);
		assertEquals(1, result.keySet().size());
		assertEquals("defaultValue", result.get("propName").getCurrentValue());
	}
	
	@Test
	public void translatePropertyList_shouldAddAdditionalProperties() {
		// when
		Map<String, FreeMarkerProperty> result = c.translatePropertyList(null,"name", Integer.valueOf(1), Integer.valueOf(2), "release", "outofServiceRel");
		
		// then
		assertNotNull(result);
		assertEquals(5, result.keySet().size());
		assertEquals("name", result.get("name").getCurrentValue());
		assertEquals("1", result.get("id").getCurrentValue());
		assertEquals("2", result.get("resGroupId").getCurrentValue());
		assertEquals("release", result.get("release").getCurrentValue());
		assertEquals("outofServiceRel", result.get("outOfServiceRelease").getCurrentValue());
		
	}

	@Test
	public void propertiesForContext_shouldAddAdditionalProperties() {
		// when
		ContextEntity context = new ContextEntity();
		context.setParent(new ContextEntity());
		context.setName("B");
		context.setNameAlias("Test");
		Map<String, FreeMarkerProperty> props = c.propertiesForContext(context);

		// then
		assertNotNull(props);
		assertEquals(3, props.keySet().size());
		assertEquals(null, props.get("domain").getCurrentValue());
		assertEquals(context.getName(), props.get("name").getCurrentValue());
		assertEquals(context.getNameAlias(), props.get("nameAlias").getCurrentValue());

	}

}
