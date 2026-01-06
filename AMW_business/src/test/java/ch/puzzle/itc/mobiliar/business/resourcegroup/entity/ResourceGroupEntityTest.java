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

package ch.puzzle.itc.mobiliar.business.resourcegroup.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;

import org.apache.commons.lang3.time.DateUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ch.puzzle.itc.mobiliar.business.foreignable.entity.ForeignableOwner;
import ch.puzzle.itc.mobiliar.business.releasing.entity.ReleaseEntity;

public class ResourceGroupEntityTest {

	ResourceGroupEntity resourceGroup;

	@BeforeEach
	public void setUp() throws Exception {
		resourceGroup = new ResourceGroupEntity();
	}

	private ResourceEntity createResourceForRelease(Date releaseDate){
		ReleaseEntity release = new ReleaseEntity();
		release.setInstallationInProductionAt(releaseDate);
		ResourceEntity resource = new ResourceEntity(ForeignableOwner.getSystemOwner());
		resource.setRelease(release);
		return resource;
	}
	
	@Test
	public void testGetNewestRelease(){
		//given 
		Date now = new Date();
		ResourceEntity newestResource = createResourceForRelease(DateUtils.addDays(now, 1));
		ResourceEntity currentResource = createResourceForRelease(now);
		ResourceEntity earlyResource = createResourceForRelease(DateUtils.addDays(now, -1));
		resourceGroup.setResources(new HashSet<ResourceEntity>(Arrays.asList(newestResource, currentResource, earlyResource)));
		
		//when
		ReleaseEntity rel = resourceGroup.getNewestRelease();
		
		//then
		assertEquals(newestResource.getRelease(), rel);
	}
	
	
	@Test
	public void testGetNewestReleaseNull(){
		//given 			
		//when
		ReleaseEntity rel = resourceGroup.getNewestRelease();
		
		//then
		assertEquals(null, rel);
	}

	@Test
	public void testGetFirstRelease(){
		//given 
		Date now = new Date();
		ResourceEntity newestResource = createResourceForRelease(DateUtils.addDays(now, 1));
		ResourceEntity currentResource = createResourceForRelease(now);
		ResourceEntity earlyResource = createResourceForRelease(DateUtils.addDays(now, -1));
		resourceGroup.setResources(new HashSet<ResourceEntity>(Arrays.asList(newestResource, earlyResource, currentResource)));
		
		//when
		ReleaseEntity rel = resourceGroup.getFirstRelease();
		
		//then
		assertEquals(earlyResource.getRelease(), rel);
	}
	
	@Test
	public void testGetFirstReleaseNull(){
		//given 			
		//when
		ReleaseEntity rel = resourceGroup.getFirstRelease();
		
		//then
		assertEquals(null, rel);
	}
	
	@Test
	public void testCompareTo(){
		ResourceGroupEntity entity1 = new ResourceGroupEntity();
		entity1.setName("aaaa");
		ResourceGroupEntity entity2 = new ResourceGroupEntity();
		entity2.setName("bbbb");
		assertEquals(-1, entity1.compareTo(entity2));		
	}
	
	@Test
	public void testCompareToNullName1(){
		ResourceGroupEntity entity1 = new ResourceGroupEntity();
		entity1.setName(null);
		ResourceGroupEntity entity2 = new ResourceGroupEntity();
		entity2.setName("bbbb");
		assertEquals(-1, entity1.compareTo(entity2));		
	}
	
	@Test
	public void testCompareToNullName2(){
		ResourceGroupEntity entity1 = new ResourceGroupEntity();
		entity1.setName("aaaa");
		ResourceGroupEntity entity2 = new ResourceGroupEntity();
		entity2.setName(null);
		assertEquals(1, entity1.compareTo(entity2));		
	}
	
	@Test
	public void testCompareToEqual(){
		ResourceGroupEntity entity1 = new ResourceGroupEntity();
		entity1.setName("aaaa");
		ResourceGroupEntity entity2 = new ResourceGroupEntity();
		entity2.setName("aaaa");
		assertEquals(0, entity1.compareTo(entity2));		
	}
	
	@Test
	public void testCompareToEqualBothNullName(){
		ResourceGroupEntity entity1 = new ResourceGroupEntity();
		entity1.setName(null);
		ResourceGroupEntity entity2 = new ResourceGroupEntity();
		entity2.setName(null);
		assertEquals(0, entity1.compareTo(entity2));		
	}

}
