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

package ch.puzzle.itc.mobiliar.business.resourcegroup.control;

import ch.puzzle.itc.mobiliar.business.foreignable.entity.ForeignableOwner;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ConsumedResourceRelationEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ResourceRelationTypeEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceGroupEntity;
import ch.puzzle.itc.mobiliar.business.security.entity.Permission;
import ch.puzzle.itc.mobiliar.business.security.interceptor.HasPermission;
import ch.puzzle.itc.mobiliar.business.security.interceptor.HasPermissionInterceptor;
import org.apache.commons.lang.NotImplementedException;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.interceptor.Interceptors;
import javax.persistence.EntityManager;
import java.util.HashSet;
import java.util.Set;

/**
 * Use {@link ResourceRepository} to fulfill cec pattern. move all methods to the control
 *
 * This is a helper service for more complex business logic on top of the {@link ResourceEntity}.
 */
@Stateless
@Interceptors(HasPermissionInterceptor.class)
@Deprecated
public class ResourceEntityService {


	@Inject
	ResourceTypeProvider restypeProvider;

	@Inject
	EntityManager entityManager;

	public Set<ResourceGroupEntity> getConsumedResourceGroups(ResourceEntity resource) {
		Set<ResourceGroupEntity> groups = new HashSet<>();
		for (ConsumedResourceRelationEntity rel : resource.getConsumedMasterRelations()) {
			groups.add(rel.getSlaveResource().getResourceGroup());
		}
		return groups;
	}

//	public void setConsumedMasterRelations(ResourceEntity resource, Set<ConsumedResourceRelationEntity> relations) {
//		for (ConsumedResourceRelationEntity r : relations) {
//			if (r.getSlaveResource().getResourceType().isRuntimeType()) {
//				setRuntime(resource, r.getSlaveResource().getResourceGroup());
//			} else {
//				resource.addConsumedRelation(r);
//			}
//		}
//	}

	@HasPermission(permission = Permission.SELECT_RUNTIME)
	public void setRuntime(ResourceEntity applicationServer, ResourceGroupEntity runtime, ForeignableOwner changingOwner) {
		if (applicationServer.getResourceType().isApplicationServerResourceType()) {
			ResourceGroupEntity existingResGroup = applicationServer.getRuntime();
			if (existingResGroup != null) {
				for (ConsumedResourceRelationEntity rel : applicationServer.getRuntimeRelations()) {
					applicationServer.removeRelation(rel);
					entityManager.remove(rel);
				}
			}
			// Every existing release needs to be connected...
			ResourceRelationTypeEntity appserverToRuntimeRelation = restypeProvider.getOrCreateResourceRelationTypeIncludingParents(applicationServer.getResourceType(),
					runtime.getResourceType(), null);
			for (ResourceEntity res : runtime.getResources()) {
				applicationServer.addConsumedResourceRelation(res, appserverToRuntimeRelation, null, changingOwner);
			}
		} else {
			throw new NotImplementedException("It's only allowed to add runtimes to application servers!");
		}
	}

}
