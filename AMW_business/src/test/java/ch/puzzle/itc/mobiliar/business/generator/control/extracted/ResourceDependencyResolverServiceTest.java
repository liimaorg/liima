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

package ch.puzzle.itc.mobiliar.business.generator.control.extracted;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.*;

import ch.puzzle.itc.mobiliar.builders.ResourceEntityBuilder;
import ch.puzzle.itc.mobiliar.business.resourcegroup.control.ResourceReleaseComparator;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import ch.puzzle.itc.mobiliar.business.generator.control.extracted.ResourceDependencyResolverService.ReleaseComparator;
import ch.puzzle.itc.mobiliar.business.releasing.entity.ReleaseEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceFactory;

public class ResourceDependencyResolverServiceTest {

	ResourceDependencyResolverService service = new ResourceDependencyResolverService();

	ReleaseEntity release1;
	ReleaseEntity release2;
	ReleaseEntity release3;
	ReleaseEntity release4;

	ResourceEntity r1;
	ResourceEntity r2;
	ResourceEntity r3;
	ResourceEntity r4;


	@Before
	public void before(){
		service.resourceReleaseComparator = new ResourceReleaseComparator();

		Calendar cal = new GregorianCalendar();

		cal.set(2001, Calendar.JANUARY, 1);
		release1 = createRelease(1, new Date(cal.getTimeInMillis()));

		cal.set(2002, Calendar.JANUARY, 1);
		release2 = createRelease(2, new Date(cal.getTimeInMillis()));

		cal.set(2003, Calendar.JANUARY, 1);
		release3 = createRelease(3, new Date(cal.getTimeInMillis()));

		cal.set(2004, Calendar.JANUARY, 1);
		release4 = createRelease(4, new Date(cal.getTimeInMillis()));

		r1 = ResourceEntityBuilder.createResourceEntity("test1", 1);
		r1.setRelease(release1);

		r2 = ResourceEntityBuilder.createResourceEntity("test2", 2);
		r2.setRelease(release2);

		r3 = ResourceEntityBuilder.createResourceEntity("test3", 3);
		r3.setRelease(release3);

		r4 = ResourceEntityBuilder.createResourceEntity("test4", 4);
		r4.setRelease(release4);

	}

	private ReleaseEntity createRelease(int id, Date installationInProductionAt) {
		ReleaseEntity release = new ReleaseEntity();
		release.setId(id);
		release.setInstallationInProductionAt(installationInProductionAt);
		return release;
	}

	@Test
	public void testFindNearestRelease() {
		// given
		Calendar cal = new GregorianCalendar();

		cal.set(2001, Calendar.JANUARY, 1);
		ReleaseEntity release1 = createRelease(1, new Date(cal.getTimeInMillis()));

		cal.set(2013, Calendar.JANUARY, 1);
		ReleaseEntity release2 = createRelease(2, new Date(cal.getTimeInMillis()));

		cal.set(2013, Calendar.JANUARY, 23);
		ReleaseEntity release3 = createRelease(3, new Date(cal.getTimeInMillis()));
		
		cal.set(2013, Calendar.AUGUST, 8);
		ReleaseEntity release4 = createRelease(4, new Date(cal.getTimeInMillis()));

		cal.set(2013, Calendar.DECEMBER, 31);
		ReleaseEntity release5 = createRelease(5, new Date(cal.getTimeInMillis()));

		cal.set(2014, Calendar.NOVEMBER, 10);
		ReleaseEntity release6 = createRelease(6, new Date(cal.getTimeInMillis()));

		SortedSet<ReleaseEntity> releases = new TreeSet<ReleaseEntity>();

		// when only past release exists
		releases.add(release1);
		cal.set(2013, Calendar.JANUARY, 10);
		ReleaseEntity nearestRelease = service.findMostRelevantRelease(releases, new Date(cal.getTimeInMillis()));

		// then
		assertEquals(release1.getId(), nearestRelease.getId());

		// when there are multiple past releases
		releases.add(release2);
		releases.add(release3);
		cal.set(2013, Calendar.FEBRUARY, 10);
		nearestRelease = service.findMostRelevantRelease(releases, new Date(cal.getTimeInMillis()));

		// then
		assertEquals(release3.getId(), nearestRelease.getId());

		// when there is one future release
		releases.add(release4);
		cal.set(2013, Calendar.FEBRUARY, 10);
		nearestRelease = service.findMostRelevantRelease(releases, new Date(cal.getTimeInMillis()));

		// then
		assertEquals(release4.getId(), nearestRelease.getId());

		// when there are multiple future releases
		cal.set(2013, Calendar.AUGUST, 9);
		releases.add(release5);
		releases.add(release6);
		nearestRelease = service.findMostRelevantRelease(releases, new Date(cal.getTimeInMillis()));

		// then
		Assert.assertEquals(release5.getId(), nearestRelease.getId());
	}

	@Test
	public void findMostRelevantResource_null(){
		assertNull(service.findMostRelevantResource(null, null));
		assertNull(service.findMostRelevantResource(new ArrayList<ResourceEntity>(), null));
		assertNull(service.findMostRelevantResource(null, new Date()));
	}

	@Test
	public void findMostRelevantResource_emptyList(){
		assertNull(service.findMostRelevantResource(new ArrayList<ResourceEntity>(), new Date()));
	}

	@Test
	public void findMostRelevantResource_after(){
		// given
		List<ResourceEntity> resources = new ArrayList<>();
		resources.add(r1);
		resources.add(r2);
		resources.add(r3);
		resources.add(r4);

		Calendar cal = new GregorianCalendar();
		cal.set(2004, Calendar.FEBRUARY, 1);
		Date relevantDate = new Date(cal.getTimeInMillis());

		// when
		ResourceEntity mostRelevantResource = service.findMostRelevantResource(resources, relevantDate);

		// then
		assertEquals(r4,mostRelevantResource);
	}

	@Test
	public void findMostRelevantResource_before(){
		// given
		List<ResourceEntity> resources = new ArrayList<>();
		resources.add(r1);
		resources.add(r2);
		resources.add(r3);
		resources.add(r4);

		Calendar cal = new GregorianCalendar();
		cal.set(1999, Calendar.FEBRUARY, 1);
		Date relevantDate = new Date(cal.getTimeInMillis());

		// when
		ResourceEntity mostRelevantResource = service.findMostRelevantResource(resources, relevantDate);

		// then
		assertEquals(r1,mostRelevantResource);
	}

	@Test
	public void findMostRelevantResource_nextRel(){
		// given
		List<ResourceEntity> resources = new ArrayList<>();
		resources.add(r1);
		resources.add(r2);
		resources.add(r3);
		resources.add(r4);

		Calendar cal = new GregorianCalendar();
		cal.set(2002, Calendar.FEBRUARY, 1);
		Date relevantDate = new Date(cal.getTimeInMillis());

		// when
		ResourceEntity mostRelevantResource = service.findMostRelevantResource(resources, relevantDate);

		// then
		assertEquals(r3,mostRelevantResource);
	}

	@Test
	public void findExactOrClosestPastReleaseShouldReturnExactRelease(){
		// given
		SortedSet<ReleaseEntity> releases = new TreeSet<>();
		releases.add(release1);
		releases.add(release2);
		releases.add(release3);
		releases.add(release4);

		Calendar cal = new GregorianCalendar();
		cal.set(2002, Calendar.JANUARY, 1);
		Date relevantDate = new Date(cal.getTimeInMillis());

		// when
		ReleaseEntity mostRelevantRelease = service.findExactOrClosestPastRelease(releases, relevantDate);

		// then
		assertThat(release2, is(mostRelevantRelease));
	}

	@Test
	public void findExactOrClosestPastReleaseShouldReturnClosestPastRelease(){
		// given
		SortedSet<ReleaseEntity> releases = new TreeSet<>();
		releases.add(release1);
		releases.add(release2);
		releases.add(release3);
		releases.add(release4);

		Calendar cal = new GregorianCalendar();
		cal.set(2002, Calendar.JANUARY, 5);
		Date relevantDate = new Date(cal.getTimeInMillis());

		// when
		ReleaseEntity mostRelevantRelease = service.findExactOrClosestPastRelease(releases, relevantDate);

		// then
		assertThat(release2, is(mostRelevantRelease));
	}

	@Test
	public void findExactOrClosestPastReleaseShouldReturnNullIfNoPastReleaseHasBeenFound(){
		// given
		SortedSet<ReleaseEntity> releases = new TreeSet<>();
		releases.add(release1);
		releases.add(release2);
		releases.add(release3);
		releases.add(release4);

		Calendar cal = new GregorianCalendar();
		cal.set(2000, Calendar.DECEMBER, 31);
		Date relevantDate = new Date(cal.getTimeInMillis());

		// when
		ReleaseEntity mostRelevantRelease = service.findExactOrClosestPastRelease(releases, relevantDate);

		// then
		assertNull(mostRelevantRelease);
	}
	
	@Test
	public void testReleaseEntityComparator(){
		ReleaseComparator comparator = new ReleaseComparator();	
		
		//release today
		ReleaseEntity currentRelease = new ReleaseEntity();
		currentRelease.setInstallationInProductionAt(new Date());
		
		//release in a year
		ReleaseEntity laterRelease = new ReleaseEntity();
		laterRelease.setInstallationInProductionAt(DateUtils.addYears(new Date(), 1));
		
		Assert.assertEquals(-1, comparator.compare(currentRelease, laterRelease));
	}
	
	@Test
	public void testReleaseEntityComparatorEqual(){
		ReleaseComparator comparator = new ReleaseComparator();	
		
		//release today
		ReleaseEntity currentRelease = new ReleaseEntity();
		currentRelease.setInstallationInProductionAt(new Date());
				
		Assert.assertEquals(0, comparator.compare(currentRelease, currentRelease));
	}
	
	@Test
	public void testReleaseEntityComparatorWithNull(){
		ReleaseComparator comparator = new ReleaseComparator();	
		
		//release today
		ReleaseEntity currentRelease = new ReleaseEntity();
		currentRelease.setInstallationInProductionAt(new Date());
				
		Assert.assertEquals(1, comparator.compare(currentRelease, null));
	}
	
	@Test
	public void testReleaseEntityComparatorBothNull(){
		ReleaseComparator comparator = new ReleaseComparator();							
		Assert.assertEquals(0, comparator.compare(null, null));
	}
	
	@Test
	public void testGetResourceEntityForRelease() {
		
		ResourceEntity firstReleaseResource = ResourceFactory.createNewResource();
		firstReleaseResource.getResourceGroup().setResources(new HashSet<ResourceEntity>());	
		firstReleaseResource.getResourceGroup().getResources().add(firstReleaseResource);
		
		//release one year ago
		ReleaseEntity firstRelease = new ReleaseEntity();
		firstRelease.setInstallationInProductionAt(DateUtils.addYears(new Date(), -1));
		firstReleaseResource.setRelease(firstRelease);
		
		//release today
		ReleaseEntity currentRelease = new ReleaseEntity();
		currentRelease.setInstallationInProductionAt(new Date());
		
		//release in a year
		ReleaseEntity laterRelease = new ReleaseEntity();
		laterRelease.setInstallationInProductionAt(DateUtils.addYears(new Date(), 1));
		
		
		ResourceDependencyResolverService service = new ResourceDependencyResolverService();
		ResourceEntity resource = service.getResourceEntityForRelease(firstReleaseResource.getResourceGroup(), currentRelease);
		Assert.assertEquals("There is only one release defined - so this should be returned", firstReleaseResource, resource);
				
		//Add a second release
		ResourceEntity secondReleaseResource = ResourceFactory.createNewResource(firstReleaseResource.getResourceGroup());
		secondReleaseResource.setRelease(currentRelease);
		secondReleaseResource.getResourceGroup().getResources().add(secondReleaseResource);
		
		resource = service.getResourceEntityForRelease(firstReleaseResource.getResourceGroup(), currentRelease);
		Assert.assertEquals("Now that there is another release with a perfect match, this should be returned", secondReleaseResource, resource);
		
		
		//Add a third release
		ResourceEntity thirdReleaseResource = ResourceFactory.createNewResource(firstReleaseResource.getResourceGroup());
		thirdReleaseResource.setRelease(laterRelease);
		thirdReleaseResource.getResourceGroup().getResources().add(thirdReleaseResource);
		
		resource = service.getResourceEntityForRelease(firstReleaseResource.getResourceGroup(), currentRelease);
		Assert.assertEquals("Even with a third release (in the future), the second release should be returned", secondReleaseResource, resource);
		
				
		resource = service.getResourceEntityForRelease(thirdReleaseResource.getResourceGroup(), currentRelease);
		Assert.assertEquals("We check that we get the right resource even if we ask with a later release", secondReleaseResource, resource);
		
		
		resource = service.getResourceEntityForRelease(thirdReleaseResource.getResourceGroup(), firstRelease);
		Assert.assertEquals("We ask for the first release - so the first resource should be returned", firstReleaseResource, resource);
		
	}
	
	@Test
	public void testGetResourceEntitiesByRelease() {
		
		//given
		/**
		 * We create two releases and two resources both connected through a single resource group.
		 */
		
		ResourceEntity firstReleaseResource = ResourceFactory.createNewResource();
		firstReleaseResource.getResourceGroup().setResources(new HashSet<ResourceEntity>());	
		firstReleaseResource.getResourceGroup().getResources().add(firstReleaseResource);
		
		ReleaseEntity firstRelease = new ReleaseEntity();
		firstRelease.setInstallationInProductionAt(DateUtils.addYears(new Date(), -1));
		firstReleaseResource.setRelease(firstRelease);
				
		ReleaseEntity currentRelease = new ReleaseEntity();
		currentRelease.setInstallationInProductionAt(new Date());
						
		ResourceEntity secondReleaseResource = ResourceFactory.createNewResource(firstReleaseResource.getResourceGroup());
		secondReleaseResource.setRelease(currentRelease);
		secondReleaseResource.getResourceGroup().getResources().add(secondReleaseResource);
		
		ResourceDependencyResolverService service = new ResourceDependencyResolverService();
		
		Collection<ResourceEntity> resources = Arrays.asList(firstReleaseResource, secondReleaseResource);
		
		//when	
		/**
		 * We extract the matching resource for two different target releases
		 */
		Set<ResourceEntity> resourcesForFirstRelease = service.getResourceEntitiesByRelease(resources, firstRelease);
		Set<ResourceEntity> resourcesForSecondRelease = service.getResourceEntitiesByRelease(resources, currentRelease);
		
		//then
		/**
		 * We expect one single result (only the actually matching resource)
		 */
		Assert.assertEquals(1, resourcesForFirstRelease.size());
		Assert.assertEquals(1, resourcesForSecondRelease.size());
		Assert.assertEquals(firstReleaseResource, resourcesForFirstRelease.toArray()[0]);
		Assert.assertEquals(secondReleaseResource, resourcesForSecondRelease.toArray()[0]);
	}

}
