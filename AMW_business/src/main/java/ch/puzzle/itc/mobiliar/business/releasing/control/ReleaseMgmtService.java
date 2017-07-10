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

package ch.puzzle.itc.mobiliar.business.releasing.control;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.interceptor.Interceptors;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import ch.puzzle.itc.mobiliar.business.generator.control.extracted.ResourceDependencyResolverService;
import ch.puzzle.itc.mobiliar.business.security.interceptor.HasPermission;
import ch.puzzle.itc.mobiliar.business.security.interceptor.HasPermissionInterceptor;
import ch.puzzle.itc.mobiliar.business.property.boundary.PropertyEditor;
import ch.puzzle.itc.mobiliar.business.environment.entity.ContextEntity;
import ch.puzzle.itc.mobiliar.business.deploy.entity.DeploymentEntity;
import ch.puzzle.itc.mobiliar.business.releasing.entity.ReleaseEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceGroupEntity;
import ch.puzzle.itc.mobiliar.business.security.entity.Permission;
import ch.puzzle.itc.mobiliar.common.exception.GeneralDBException;
import ch.puzzle.itc.mobiliar.common.exception.ResourceNotFoundException;

import static ch.puzzle.itc.mobiliar.business.security.entity.Action.*;

@Stateless
@Interceptors(HasPermissionInterceptor.class)
public class ReleaseMgmtService {

	@Inject
	ReleaseMgmtPersistenceService persistenceService;
	
	@Inject
	ResourceDependencyResolverService resourceDependencyResolver;
	
	@Inject
	EntityManager em;

	@Inject
	Logger log;
	
	@Inject
	PropertyEditor propertyEditor;

	/**
	 * Load all releases
	 * 
	 * @param sortDesc
	 * @return
	 */
	public List<ReleaseEntity> loadAllReleases(boolean sortDesc) {
		return persistenceService.loadAllReleaseEntities(sortDesc);
	}

	/**
	 * @param name
	 * @return the release entity with given name
	 */
	public ReleaseEntity findByName(String name) {
		return persistenceService.findByName(name);
	}

	/**
	 * Returns a list of releases for management operations. This means, we
	 * don't care about relations to resources or deployments but only want to
	 * create, edit or delete plain release-instances
	 * 
	 * @return
	 */
	@HasPermission(permission = Permission.RELEASE, action = READ)
	public List<ReleaseEntity> loadReleasesForMgmt(int startIndex, int length, boolean sortDesc) {
		return persistenceService.loadReleaseEntities(startIndex, length, sortDesc);
	}

	/**
	 * @return a list with releaseEntities, excluding the one we want to copy from
	 */
	public Map<Integer, ReleaseEntity> loadReleasesForCreatingNewRelease(Set<ReleaseEntity> existingReleases) {
		Map<Integer, ReleaseEntity> releases = new HashMap<>();
		List<ReleaseEntity> allReleases = persistenceService.loadAllReleaseEntities(false);
		for (ReleaseEntity rel : allReleases) {
			if (!existingReleases.contains(rel)) {
				releases.put(rel.getId(), rel);
			}
		}
		return releases;
	}

	/**
	 * @return the number of existing releases
	 */
	@HasPermission(permission = Permission.RELEASE, action = READ)
	public int countReleases(){
		return persistenceService.count();
	}


	/**
	 * Deletes the given release from the database
	 * 
	 * @param id of release
	 */
	@HasPermission(permission = Permission.RELEASE, action = DELETE)
	public boolean delete(int id) {
		return persistenceService.deleteReleaseEntity(id);
	}

	/**
	 * Persists the given new release.
	 * 
	 * @param release
	 */
	@HasPermission(permission = Permission.RELEASE, action = CREATE)
	public boolean create(ReleaseEntity release) throws GeneralDBException {
		return persistenceService.saveReleaseEntity(release);
	}

	/**
	 * Persists the given release - the already existing instance will be updated.
	 *
	 * @param release
	 */
	@HasPermission(permission = Permission.RELEASE, action = UPDATE)
	public boolean update(ReleaseEntity release) throws GeneralDBException {
		return persistenceService.saveReleaseEntity(release);
	}

	public ReleaseEntity getDefaultRelease(){
		return persistenceService.getDefaultRelease();
	}

	public List<ResourceEntity> getResourcesForRelease(Integer releaseId) {
		return persistenceService.getResourcesForRelease(releaseId);
	}

	
	public Map<ReleaseEntity, Date> getDeployableReleasesForResourceGroupWithLatestDeploymentDate(ResourceGroupEntity resourceGroup, ContextEntity context){
		Map<ReleaseEntity, Date> result = new LinkedHashMap<>();
		List<ReleaseEntity> releases = getDeployableReleasesForResourceGroup(resourceGroup);
		for(ReleaseEntity rel : releases){
			DeploymentEntity deployment = getLastSuccessfulDeploymentForResourceGroup(resourceGroup, rel, context);
			Date lastSuccessfulDeployment = null;
			if(deployment!=null){
				lastSuccessfulDeployment = deployment.getDeploymentDate();
			}
			result.put(rel, lastSuccessfulDeployment);
		}		
		return result;		
	}
	
	private DeploymentEntity getLastSuccessfulDeploymentForResourceGroup(ResourceGroupEntity resourceGroup, ReleaseEntity release, ContextEntity context){
		TypedQuery<DeploymentEntity> query = em.createNamedQuery(DeploymentEntity.LAST_SUCCESSFUL_DEPLOYMENT, DeploymentEntity.class);
		query.setParameter("context", context).setParameter("release", release).setParameter("resourceGroup", resourceGroup).setParameter("deploymentState", DeploymentEntity.DeploymentState.success);
		List<DeploymentEntity> queryResult = query.getResultList();
		if(queryResult.size()==0){
			return null;
		}
		else {
			if(queryResult.size()>1){
				log.warning("Multiple last deployments for resource group "+resourceGroup.getId()+" in release "+release.getId()+" and in context "+context.getId()+" found...");
			}
			return queryResult.get(0);			
		}		
	}
	
	
	public List<ReleaseEntity> getDeployableReleasesForResourceGroup(ResourceGroupEntity rg) {

		TreeSet<ReleaseEntity> releases = new TreeSet<>(loadAllReleases(false));
		if(!em.contains(rg)){
			rg = em.merge(rg);
		}		
		
		if (rg != null) {
			SortedSet<ReleaseEntity> groupReleases = rg.getReleases();
			return new ArrayList<>(releases.tailSet(groupReleases.first()));
		}
		return Collections.<ReleaseEntity> emptyList();

	}

	public void changeReleaseOfResource(ResourceEntity resource, ReleaseEntity release) throws ResourceNotFoundException {
		persistenceService.changeReleaseOfResource(resource.getId(), release.getId());
	}

}
