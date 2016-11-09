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

package ch.puzzle.itc.mobiliar.business.resourcerelation.boundary;

import ch.puzzle.itc.mobiliar.business.appserverrelation.boundary.AppServerRelation;
import ch.puzzle.itc.mobiliar.business.appserverrelation.entity.AppServerRelationHierarchyEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.AbstractResourceRelationEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A boundary for display resource dependencies (all masters consuming or providing a certain slave)
 * 
 * @author oschmid
 */
@Stateless
@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
public class DisplayDependencies {

	@Inject
	private AppServerRelation asRelService;

	public Map<AbstractResourceRelationEntity, ResourceEntity> getAppServerRelations(ResourceEntity resource) {
		Map<AbstractResourceRelationEntity, ResourceEntity> result = new HashMap<AbstractResourceRelationEntity, ResourceEntity>();
		List<AppServerRelationHierarchyEntity> asRelations = asRelService.findAppServerRelationsByOverriddenResource(resource.getId());
		for(AppServerRelationHierarchyEntity asRelation : asRelations){
			if(asRelation.getAssignedConsumedResourceRelation()!=null){
				result.put(asRelation.getAssignedConsumedResourceRelation(), asRelation.getApplicationServer());
			}
		}		
		return result;
	}

}
