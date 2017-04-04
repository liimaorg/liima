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

package ch.puzzle.itc.mobiliar.presentation.resourcesedit;

import ch.puzzle.itc.mobiliar.business.environment.entity.ContextEntity;
import ch.puzzle.itc.mobiliar.business.releasing.entity.ReleaseEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.*;
import ch.puzzle.itc.mobiliar.business.security.boundary.PermissionBoundary;
import ch.puzzle.itc.mobiliar.business.security.entity.Action;
import ch.puzzle.itc.mobiliar.business.security.entity.Permission;
import ch.puzzle.itc.mobiliar.common.util.DefaultResourceTypeDefinition;
import ch.puzzle.itc.mobiliar.presentation.common.context.SessionContext;
import org.apache.commons.lang.time.DateUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Date;

import static ch.puzzle.itc.mobiliar.business.security.entity.Action.READ;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EditResourceViewTest {

	public EditResourceView context = new EditResourceView();
	
	@Mock
	ResourceTypeEntity resourceType;

	@Mock
	PermissionBoundary permissionBoundary;

	@Mock
	SessionContext sessionContext;
	
	@Before
	public void setup(){
		context.resourceType = resourceType;
		context.permissionBoundary = permissionBoundary;
		context.sessionContext = sessionContext;
	}
	
	
	@Test
	public void testGetDefaultResourceTypeCapitalizedName() {
		when(resourceType.getName()).thenReturn(DefaultResourceTypeDefinition.APPLICATION.name());
		when(resourceType.isDefaultResourceType()).thenReturn(true);
		String displayName = context.getCapitalizedResourceTypeName();
		Assert.assertEquals("Application", displayName);
	}

	@Test
	public void testNewerRelease(){
		//given
		ResourceGroupEntity group = new ResourceGroupEntity();
		ResourceEntity r = ResourceFactory.createNewResource(group);
		ReleaseEntity rel = new ReleaseEntity();
		rel.setInstallationInProductionAt(new Date());
		r.setRelease(rel);
		
		ResourceEntity r2 = ResourceFactory.createNewResource(group);
		ReleaseEntity rel2 = new ReleaseEntity();
		rel2.setInstallationInProductionAt(DateUtils.addDays(new Date(),2));	
		r2.setRelease(rel2);
		
		//when
		context.resource=r;
		//then
		Assert.assertTrue(context.hasNewerRelease());
		
		//when
		context.resource=r2;
		//then
		Assert.assertFalse(context.hasNewerRelease());
		
	}
	
	@Test
	public void testExistsForThisRelease(){
		//given
		ResourceGroupEntity group = new ResourceGroupEntity();
		ResourceEntity r = ResourceFactory.createNewResource(group);
		
		ResourceGroupEntity group2 = new ResourceGroupEntity();
		ResourceEntity r2 = ResourceFactory.createNewResource(group2);

		ReleaseEntity rel = new ReleaseEntity();
		rel.setInstallationInProductionAt(new Date());
		
		ReleaseEntity rel2 = new ReleaseEntity();
		rel2.setInstallationInProductionAt(DateUtils.addDays(new Date(),2));
		
		r.setRelease(rel);
		r2.setRelease(rel2);
		
		//when
		context.resource=r;
		//then	
		
		//The second group does only exist for a release later than the first - therefore the method returns false.
		Assert.assertFalse(context.existsForThisRelease(group2));
		
		//when
		context.resource=r2;
		//then
		//In the inverse direction, the first group already exists...
		Assert.assertTrue(context.existsForThisRelease(group));
		
		//when
		r.setRelease(rel2);
		//if both have the same release, the method also returns true
		Assert.assertTrue(context.existsForThisRelease(group));
		
	}

	@Test
	public void canSaveChangesShouldInvokeTheRightMethodsOfTheBoundaryWhenEditingResourceType(){
		//given
		ResourceTypeEntity type = new ResourceTypeEntity();
		context.resourceType = type;
		ContextEntity ce = new ContextEntity();
		when(sessionContext.getCurrentContext()).thenReturn(ce);
		// when
		context.canSaveChanges();
		//then
		verify(permissionBoundary, times(1)).hasPermission(Permission.RESOURCETYPE, ce, Action.UPDATE, null, type);
	}

	@Test
	public void canSaveChangesShouldInvokeTheRightMethodsOfTheBoundaryWhenEditingResource(){
		//given
		ResourceGroupEntity group = new ResourceGroupEntity();
		ResourceEntity r = ResourceFactory.createNewResource(group);
		ResourceTypeEntity type = new ResourceTypeEntity();
		type.setId(9);
		r.setResourceType(type);
		context.resource = r;
		ContextEntity ce = new ContextEntity();
		when(sessionContext.getCurrentContext()).thenReturn(ce);
		when(permissionBoundary.hasPermissionToEditPropertiesOfResource(anyInt())).thenReturn(true);
		// when
		context.canSaveChanges();
		//then
		verify(permissionBoundary, times(1)).hasPermission(Permission.RESOURCE, ce, Action.UPDATE, r, type);
	}

	@Test
	public void shouldInvokePermissionBoundaryOnSetResourceIdFromParam() {
		//given
		ResourceGroupEntity group = new ResourceGroupEntity();
		ResourceEntity r = ResourceFactory.createNewResource(group);
		r.setId(7);
		context.resource = r;
		//when
		context.setResourceIdFromParam(7);
		//then
		verify(permissionBoundary, times(1)).checkPermissionActionAndFireException(Permission.RESOURCE, READ, "edit resources");
	}

}
