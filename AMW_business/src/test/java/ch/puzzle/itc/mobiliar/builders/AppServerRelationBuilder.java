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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ch.puzzle.itc.mobiliar.business.appserverrelation.entity.AppServerRelationHierarchyEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ConsumedResourceRelationEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceGroupEntity;

public class AppServerRelationBuilder extends BaseEntityBuilder {

	/**
	 * @param asId
	 * @param consumedId
	 * @param dirty
	 * @param parentIds
	 * @param relId
	 * @param identifier
	 * @param typeId
	 * @return
	 */
	public AppServerRelationHierarchyEntity mockAppServerRelationEntityForConsumedRel(ResourceEntity appServer, ConsumedResourceRelationEntity consumedResRel, ResourceGroupEntity overriddenResource, AppServerRelationHierarchyEntity parentRelation) {
		AppServerRelationHierarchyEntity mock = mock(AppServerRelationHierarchyEntity.class);
		when(mock.getApplicationServer()).thenReturn(appServer);
		when(mock.getAssignedConsumedResourceRelation()).thenReturn(consumedResRel);
		when(mock.getOverriddenSlaveResource()).thenReturn(overriddenResource);
		when(mock.getParentRelation()).thenReturn(parentRelation);
		return mock;
	}

	public AppServerRelationHierarchyEntity forConsumedResourceRelation(ConsumedResourceRelationEntity rel, AppServerRelationHierarchyEntity parentRelation){
		AppServerRelationHierarchyEntity entity = new AppServerRelationHierarchyEntity();
		entity.setRelation(rel);
		rel.getAppServerRelations().add(entity);
		if(parentRelation!=null){
			entity.setParentRelation(parentRelation);
			parentRelation.getChildRelations().add(entity);
		}
		return entity;
	}
}