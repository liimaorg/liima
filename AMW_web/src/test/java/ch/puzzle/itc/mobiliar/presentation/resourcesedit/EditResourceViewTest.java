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

import ch.puzzle.itc.mobiliar.business.releasing.entity.ReleaseEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceFactory;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceGroupEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceTypeEntity;
import ch.puzzle.itc.mobiliar.common.util.DefaultResourceTypeDefinition;
import ch.puzzle.itc.mobiliar.presentation.resourcesedit.EditResourceView;
import org.apache.commons.lang.time.DateUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Date;

@RunWith(MockitoJUnitRunner.class)
public class EditResourceViewTest {

	public EditResourceView context = new EditResourceView();
	
	@Mock
	ResourceTypeEntity resourceType;
	
	@Before
	public void setup(){
		context.resourceType = resourceType;
	}
	
	
	@Test
	public void testGetDefaultResourceTypeCapitalizedName() {
		Mockito.when(resourceType.getName()).thenReturn(DefaultResourceTypeDefinition.APPLICATION.name());
		Mockito.when(resourceType.isDefaultResourceType()).thenReturn(true);
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
}
