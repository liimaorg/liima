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

package ch.puzzle.itc.mobiliar.builders;

import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

import java.util.Set;

import org.mockito.Mockito;

import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceGroupEntity;

public class ResourceGroupEntityBuilder extends BaseEntityBuilder {

	public ResourceGroupEntity mockResourceGroupEntity(String name, Set<ResourceEntity> resources) {
		ResourceGroupEntity mock = mock(ResourceGroupEntity.class);
		if (resources != null) {
			for (ResourceEntity r : resources) {
				if (Mockito.mockingDetails(r).isMock()) {
					lenient().when(r.getResourceGroup()).thenReturn(mock);
				}
			}
		}
		return mock;
	}

	public ResourceGroupEntity buildResourceGroupEntity(String name, Set<ResourceEntity> resources, boolean withId) {
		ResourceGroupEntity entity = new ResourceGroupEntity();
		if (withId) {
			Integer id = getNextId();
			entity.setId(id);
		}
		entity.setName(name);
		if (resources != null) {
			entity.setResources(resources);
			for (ResourceEntity r : resources) {
				r.setResourceGroup(entity);
			}
		}
		return entity;
	}

}
