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

package ch.puzzle.itc.mobiliar.presentation.propertyEdit;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;

import ch.puzzle.itc.mobiliar.business.property.entity.ResourceEditProperty;
import ch.puzzle.itc.mobiliar.business.property.entity.ResourceEditRelation;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceFactory;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceTypeEntity;
import ch.puzzle.itc.mobiliar.presentation.resourceRelation.events.ChangeSelectedRelationEvent;

public class EditPropertiesTableTest {

	private EditPropertiesTable editPropertiesTable;

	@Before
	public void setUp() {
		editPropertiesTable = new EditPropertiesTable();
	}

	@Test
	public void onChangedResourceTypeEventShouldSetCurrentSelectedResourceType() {
		// given
		Integer id = 1;
		ResourceTypeEntity resourceTypeEntity = createResourceTypeWithId(id);
		assertNull(editPropertiesTable.getResourceTypeId());
		assertNull(editPropertiesTable.getResourceId());

		// when
		editPropertiesTable.onChangedResourceType(resourceTypeEntity);

		// then
		assertEquals(id, editPropertiesTable.getResourceTypeId());
		assertNull(editPropertiesTable.getResourceId());
	}

	@Test
	public void onChangedResourceEventShouldSetCurrentSelectedResource() {
		// given
		Integer id = 1;
		ResourceEntity resourceEntity = createResourceWithId(id);
		assertNull(editPropertiesTable.getResourceTypeId());
		assertNull(editPropertiesTable.getResourceId());

		// when
		editPropertiesTable.onChangedResource(resourceEntity);

		// then
		assertEquals(id, editPropertiesTable.getResourceId());
		assertNull(editPropertiesTable.getResourceTypeId());
	}

	private ResourceTypeEntity createResourceTypeWithId(int id) {
		ResourceTypeEntity resourceTypeEntity = new ResourceTypeEntity();
		resourceTypeEntity.setId(id);
		return resourceTypeEntity;
	}

	private ResourceEntity createResourceWithId(int id) {
		ResourceEntity resourceEntity = ResourceFactory.createNewResource();
		resourceEntity.setId(id);
		return resourceEntity;
	}

	@Test
	public void isPropertyDisplayableWhenPropertyIsNullShouldReturnFalse() {
		// given

		// when
		boolean propertyDisplayable = editPropertiesTable.isPropertyDisplayable(null);

		// then
		assertFalse(propertyDisplayable);

	}

	@Test
	public void isPropertyDisplayableWhenCardinalityIsNullShouldReturnTrue() {
		// given
		Integer cardinality = null;

		ResourceEditProperty propertyMock = mock(ResourceEditProperty.class);
		when(propertyMock.getCardinalityProperty()).thenReturn(cardinality);

		// when
		boolean propertyDisplayable = editPropertiesTable.isPropertyDisplayable(propertyMock);

		// then
		assertTrue(propertyDisplayable);

	}

	@Test
	public void isPropertyDisplayableWhenCardinalityIsBiggerThanMinusOneShouldReturnTrue() {
		// given
		Integer cardinality = 1;

		ResourceEditProperty propertyMock = mock(ResourceEditProperty.class);
		when(propertyMock.getCardinalityProperty()).thenReturn(cardinality);

		// when
		boolean propertyDisplayable = editPropertiesTable.isPropertyDisplayable(propertyMock);

		// then
		assertTrue(propertyDisplayable);

	}

	@Test
	public void isPropertyDisplayableWhenCardinalityIsMinusOneShouldReturnFalse() {
		// given
		Integer cardinality = -1;

		ResourceEditProperty propertyMock = mock(ResourceEditProperty.class);
		when(propertyMock.getCardinalityProperty()).thenReturn(cardinality);

		// when
		boolean propertyDisplayable = editPropertiesTable.isPropertyDisplayable(propertyMock);

		// then
		assertFalse(propertyDisplayable);

	}

	@Test
	public void isEditableWhenInstanceAndFocusOnResourceShouldReturnTrue() {
		// given
		ResourceEditProperty.Origin propertyDescriptorOrigin = ResourceEditProperty.Origin.INSTANCE;

		ResourceEditProperty propertyMock = mock(ResourceEditProperty.class);
		when(propertyMock.getPropertyDescriptorOrigin()).thenReturn(propertyDescriptorOrigin);

		// set focus on Resource
		editPropertiesTable.onChangedResource(createResourceWithId(1));

		// when
		boolean isEditable = editPropertiesTable.isEditable(propertyMock);

		// then
		assertTrue(isEditable);

	}

	@Test
	public void isEditableWhenTypeAndFocusOnResourceShouldReturnFalse() {
		// given
		ResourceEditProperty.Origin propertyDescriptorOrigin = ResourceEditProperty.Origin.TYPE;

		ResourceEditProperty propertyMock = mock(ResourceEditProperty.class);
		when(propertyMock.getPropertyDescriptorOrigin()).thenReturn(propertyDescriptorOrigin);

		// set focus on Resource
		editPropertiesTable.onChangedResource(createResourceWithId(1));

		// when
		boolean isEditable = editPropertiesTable.isEditable(propertyMock);

		// then
		assertFalse(isEditable);

	}

	@Test
	public void isEditableWhenRelationAndFocusOnResourceShouldReturnFalse() {
		// given
		ResourceEditProperty.Origin propertyDescriptorOrigin = ResourceEditProperty.Origin.RELATION;

		ResourceEditProperty propertyMock = mock(ResourceEditProperty.class);
		when(propertyMock.getPropertyDescriptorOrigin()).thenReturn(propertyDescriptorOrigin);

		// set focus on Resource
		editPropertiesTable.onChangedResource(createResourceWithId(1));

		// when
		boolean isEditable = editPropertiesTable.isEditable(propertyMock);

		// then
		assertFalse(isEditable);

	}

	@Test
	public void isEditableWhenTypeRelationAndFocusOnResourceShouldReturnFalse() {
		// given
		ResourceEditProperty.Origin propertyDescriptorOrigin = ResourceEditProperty.Origin.TYPE_REL;

		ResourceEditProperty propertyMock = mock(ResourceEditProperty.class);
		when(propertyMock.getPropertyDescriptorOrigin()).thenReturn(propertyDescriptorOrigin);

		// set focus on Resource
		editPropertiesTable.onChangedResource(createResourceWithId(1));

		// when
		boolean isEditable = editPropertiesTable.isEditable(propertyMock);

		// then
		assertFalse(isEditable);

	}

	@Test
	public void isEditableWhenInstanceAndFocusOnResourceTypeShouldReturnFalse() {
		// given
		ResourceEditProperty.Origin propertyDescriptorOrigin = ResourceEditProperty.Origin.INSTANCE;

		ResourceEditProperty propertyMock = mock(ResourceEditProperty.class);
		when(propertyMock.getPropertyDescriptorOrigin()).thenReturn(propertyDescriptorOrigin);

		// set focus on Resource type
		editPropertiesTable.onChangedResourceType(createResourceTypeWithId(1));

		// when
		boolean isEditable = editPropertiesTable.isEditable(propertyMock);

		// then
		assertFalse(isEditable);

	}

	@Test
	public void isEditableWhenTypeAndFocusOnResourceTypeShouldReturnTrue() {
		// given
		ResourceEditProperty.Origin propertyDescriptorOrigin = ResourceEditProperty.Origin.TYPE;

		ResourceEditProperty propertyMock = mock(ResourceEditProperty.class);
		when(propertyMock.getPropertyDescriptorOrigin()).thenReturn(propertyDescriptorOrigin);

		// set focus on Resource type
		editPropertiesTable.onChangedResourceType(createResourceTypeWithId(1));

		// when
		boolean isEditable = editPropertiesTable.isEditable(propertyMock);

		// then
		assertTrue(isEditable);

	}

	@Test
	public void isEditableWhenRelationAndFocusOnResourceTypeShouldReturnFalse() {
		// given
		ResourceEditProperty.Origin propertyDescriptorOrigin = ResourceEditProperty.Origin.RELATION;

		ResourceEditProperty propertyMock = mock(ResourceEditProperty.class);
		when(propertyMock.getPropertyDescriptorOrigin()).thenReturn(propertyDescriptorOrigin);

		// set focus on Resource type
		editPropertiesTable.onChangedResourceType(createResourceTypeWithId(1));

		// when
		boolean isEditable = editPropertiesTable.isEditable(propertyMock);

		// then
		assertFalse(isEditable);

	}

	@Test
	public void isEditableWhenTypeRelationAndFocusOnResourceTypeShouldReturnFalse() {
		// given
		ResourceEditProperty.Origin propertyDescriptorOrigin = ResourceEditProperty.Origin.TYPE_REL;

		ResourceEditProperty propertyMock = mock(ResourceEditProperty.class);
		when(propertyMock.getPropertyDescriptorOrigin()).thenReturn(propertyDescriptorOrigin);

		// set focus on Resource type
		editPropertiesTable.onChangedResourceType(createResourceTypeWithId(1));

		// when
		boolean isEditable = editPropertiesTable.isEditable(propertyMock);

		// then
		assertFalse(isEditable);

	}

	@Test
	public void onChangedRelationEventWhenResRelationIdShouldSetRelationId() {
		// given
		Integer resRelTypeId = 1;
		Integer resRelId = 99;

		ResourceEditRelation relationMock = mock(ResourceEditRelation.class);
		ChangeSelectedRelationEvent relationEvent = new ChangeSelectedRelationEvent(relationMock);

		when(relationMock.getResRelTypeId()).thenReturn(resRelTypeId);
		when(relationMock.getResRelId()).thenReturn(resRelId);

		when(relationMock.isResourceTypeRelation()).thenReturn(false);

		assertNull(editPropertiesTable.getRelationId());

		// when
		editPropertiesTable.onChangedRelation(relationEvent);

		// then
		assertEquals(resRelId, editPropertiesTable.getRelationId());
	}

	@Test
	public void onChangedRelationEventWhenResRelationTypeIdShouldSetRelationId() {
		// given
		Integer resRelTypeId = 1;
		Integer resRelId = 99;

		ResourceEditRelation relationMock = mock(ResourceEditRelation.class);
		ChangeSelectedRelationEvent relationEvent = new ChangeSelectedRelationEvent(relationMock);

		when(relationMock.getResRelTypeId()).thenReturn(resRelTypeId);
		when(relationMock.getResRelId()).thenReturn(resRelId);

		when(relationMock.isResourceTypeRelation()).thenReturn(true);

		assertNull(editPropertiesTable.getRelationId());

		// when
		editPropertiesTable.onChangedRelation(relationEvent);

		// then
		assertEquals(resRelTypeId, editPropertiesTable.getRelationId());
	}

}
