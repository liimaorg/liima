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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.HashSet;

import org.junit.Test;

public class ResourceFactoryTest {

	@Test
	public void test_createNewResource() {

		// when
		ResourceEntity resource = ResourceFactory.createNewResource();

		// then
		assertNotNull(resource);
		assertNotNull(resource.getResourceGroup());
		assertEquals(resource, resource.getResourceGroup().getResources().iterator().next());
	}

	@Test
	public void test_createNewResourceWithName() {

		// when
		ResourceEntity resource = ResourceFactory.createNewResource("foo");

		// then
		assertNotNull(resource);
		assertNotNull(resource.getResourceGroup());
		assertEquals("foo", resource.getName());
		assertEquals("foo", resource.getResourceGroup().getName());
		assertEquals(resource, resource.getResourceGroup().getResources().iterator().next());
	}

	@Test
	public void test_createNewResourceForGroup() {
		// given
		ResourceEntity res1 = ResourceFactory.createNewResource();
		ResourceGroupEntity group = res1.getResourceGroup();
		group.setResources(new HashSet<ResourceEntity>());
		group.getResources().add(res1);
		group.setName("foo");

		// when
		ResourceEntity resource = ResourceFactory.createNewResource(group);

		// then
		assertNotNull(resource);
		assertNotNull(resource.getResourceGroup());
		assertEquals(group, resource.getResourceGroup());
		assertEquals(2, group.getResources().size());
		for (ResourceEntity r : group.getResources()) {
			assertEquals("foo", r.getName());
		}
	}

}
