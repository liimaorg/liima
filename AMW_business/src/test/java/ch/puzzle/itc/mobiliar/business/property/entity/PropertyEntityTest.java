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

import ch.puzzle.itc.mobiliar.builders.PropertyDescriptorEntityBuilder;
import ch.puzzle.itc.mobiliar.builders.PropertyEntityBuilder;
import ch.puzzle.itc.mobiliar.builders.ResourceEntityBuilder;
import ch.puzzle.itc.mobiliar.business.foreignable.entity.ForeignableOwner;
import ch.puzzle.itc.mobiliar.business.generator.control.TemplateUtils;
import ch.puzzle.itc.mobiliar.business.resourcegroup.control.CopyResourceDomainService;
import ch.puzzle.itc.mobiliar.business.resourcegroup.control.CopyUnit;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceContextEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.utils.CopyHelper;
import ch.puzzle.itc.mobiliar.common.exception.AMWException;
import ch.puzzle.itc.mobiliar.common.util.ConfigKey;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

public class PropertyEntityTest {

	private PropertyEntity entity = new PropertyEntity();
	private PropertyDescriptorEntity descriptor = new PropertyDescriptorEntity();
	private ResourceContextEntity resourceContext = new ResourceContextEntity();
	private PropertyDescriptorEntityBuilder propDescBuilder = new PropertyDescriptorEntityBuilder();
	private PropertyEntityBuilder propBuilder = new PropertyEntityBuilder();

	@Before
	public void setUp(){
		// set Up Encription Key
		System.getProperties().put(ConfigKey.ENCRYPTION_KEY.getValue(), "78E76138D98F00BBF713136BC13DEE4B");
	}
	
	@After
	public void tearDown(){
		// set Up Encription Key
		System.getProperties().remove(ConfigKey.ENCRYPTION_KEY.getValue());
	}
	
	@Test
	public void testCreateProperty() {
		String propertyValue = "testProperty";
		PropertyEntity.createProperty(propertyValue, descriptor, resourceContext);
		assertEquals(resourceContext.getProperties().size(), 1);
		assertEquals(resourceContext.getProperties().toArray(new PropertyEntity[0])[0].getDecryptedValue(), propertyValue);
		assertEquals(resourceContext.getProperties().toArray(new PropertyEntity[0])[0].getDescriptor(), descriptor);
	}

	@Test
	public void copy() {
		// given
		entity.setDescriptor(descriptor);
		entity.setValue("fooValue");

		// when
		PropertyEntity copy = entity.copy();

		// then
		assertNotNull(copy);
		assertEquals(entity.getDescriptor(), copy.getDescriptor());
		assertEquals(entity.getValue(), copy.getValue());
	}

	@Test
	public void setValueAndEncrypt_shouldEncrypt() {
		// given
		descriptor.setEncrypt(true);
		entity.setDescriptor(descriptor);

		// when
		entity.setValueAndEncrypt("fooValue");

		// then
		assertEquals(TemplateUtils.encrypt("fooValue"), entity.getValue());
	}

	@Test
	public void setValueAndEncrypt_shouldNotEncrypt() {
		// given
		descriptor.setEncrypt(false);
		entity.setDescriptor(descriptor);

		// when
		entity.setValueAndEncrypt("fooValue");

		// then
		assertEquals("fooValue", entity.getValue());
	}

	@Test
	public void getDecryptedValue_shouldDecrypt() {
		// given
		descriptor.setEncrypt(true);
		entity.setDescriptor(descriptor);
		entity.setValue(TemplateUtils.encrypt("fooValue"));

		// when
		String result = entity.getDecryptedValue();

		// then
		assertEquals("fooValue", result);
	}

	@Test
	public void test_copyPropertyEntity() throws AMWException {
		Map<CopyResourceDomainService.CopyMode, Set<ForeignableOwner>> validCopyModeOwnerCombinations = CopyHelper.getValidModeOwnerCombinationsMap();
		for (CopyResourceDomainService.CopyMode copyMode : validCopyModeOwnerCombinations.keySet()) {
			for(ForeignableOwner foreignableOwner : validCopyModeOwnerCombinations.get(copyMode)){
				ResourceEntity originResource = new ResourceEntityBuilder().mockAppServerEntity("originResource", null, null, null);
				when(originResource.isDeletable()).thenReturn(true);

				ResourceEntity targetResource = new ResourceEntityBuilder().buildAppServerEntity("targetResource", null, null, true);

				CopyUnit copyUnit = new CopyUnit(originResource, targetResource, copyMode, foreignableOwner);

				copyPropertyEntity_targetNull(copyUnit);
				copyPropertyEntity(copyUnit);
			}
		}
	}

	public void copyPropertyEntity_targetNull(CopyUnit copyUnit) {
		// given
		PropertyTypeEntity origType = propDescBuilder.buildPropertyTypeEntity("type1");
		PropertyDescriptorEntity origDesc = propDescBuilder.buildPropertyDescriptorEntity(null, "foo",
				"bar", false, false, false, origType, "defaultVal", "exampleVal", "MIK", true,
				"displayName");
		PropertyEntity origin = propBuilder.buildPropertyEntity("foo", origDesc);

		// when
		PropertyEntity copy = origin.getCopy(null,copyUnit);

		// then
		assertNotNull(copy);
		assertEquals(origDesc, copy.getDescriptor());
		assertTrue(copy.getDescriptor().getProperties().contains(copy));
		assertEquals(origin.getValue(), copy.getValue());
	}

	public void copyPropertyEntity(CopyUnit copyUnit) {
		// given
		PropertyTypeEntity origType = propDescBuilder.mockPropertyTypeEntity("type1");
		PropertyDescriptorEntity origDesc = propDescBuilder.buildPropertyDescriptorEntity(null, "foo",
				"bar", false, false, false, origType, "defaultVal", "exampleVal", "MIK", true,
				"displayName");
		PropertyEntity origin = propBuilder.buildPropertyEntity("foo", origDesc);

		PropertyTypeEntity targetType = propDescBuilder.mockPropertyTypeEntity("type2");
		PropertyDescriptorEntity targetDesc = propDescBuilder.buildPropertyDescriptorEntity(null, "foo",
				"boo", true, true, true,  targetType, "defaultVal", "exampleVal", "MIK", true,
				"displayName");

		PropertyEntity target = propBuilder.buildPropertyEntity("foo", targetDesc);

		// when
		PropertyEntity copy = origin.getCopy(target, copyUnit);

		// then
		assertNotNull(copy);
		assertEquals(targetDesc, copy.getDescriptor());
		assertEquals(origin.getValue(), copy.getValue());
		assertTrue(copy.getDescriptor().getProperties().contains(copy));
	}

}
