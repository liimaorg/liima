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

package ch.puzzle.itc.mobiliar.business.appserverrelation.boundary;

import java.util.*;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import ch.puzzle.itc.mobiliar.business.appserverrelation.control.AppServerRelationPath;
import ch.puzzle.itc.mobiliar.business.appserverrelation.entity.AppServerRelationCapable;
import ch.puzzle.itc.mobiliar.business.appserverrelation.entity.AppServerRelationHierarchyEntity;
import ch.puzzle.itc.mobiliar.business.generator.control.extracted.ResourceDependencyResolverService;
import ch.puzzle.itc.mobiliar.business.releasing.entity.ReleaseEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.boundary.ResourceLocator;
import ch.puzzle.itc.mobiliar.business.resourcegroup.control.ResourceEditService;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceGroupEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ConsumedResourceRelationEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ResourceRelationTypeEntity;
import ch.puzzle.itc.mobiliar.business.softlinkRelation.control.SoftlinkRelationService;

@Stateless
@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
public class AppServerRelation {

	@Inject
	EntityManager entityManager;

	@Inject
	ResourceEditService resourceEditService;

	@Inject
	ResourceDependencyResolverService dependencyResolver;

    @Inject
    ResourceLocator resourceLocator;

	@Inject
	SoftlinkRelationService softlinkRelationService;

	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public List<AppServerRelationHierarchyEntity> findAppServerRelationsByOverriddenResource(
			Integer resourceId) {
		TypedQuery<AppServerRelationHierarchyEntity> query = entityManager.createNamedQuery(
				AppServerRelationHierarchyEntity.LOAD_ALL_ENTITIES_BY_OVERRIDDEN_RESSOURCE,
				AppServerRelationHierarchyEntity.class).setParameter("resourceId", resourceId);
		return query.getResultList();
	}

    /**
     * Obtains all softlink related PPIs for a given list of resourceIds
     * @param resourceIds
     * @return
     */
    public Map<Integer,Map<ResourceEntity,ResourceEntity>> getAllSoftlinkRelatedResources(List<Integer> resourceIds) {
        Map<Integer,Map<ResourceEntity,ResourceEntity>> aMap = new HashMap<>();
        for (Integer resourceId : resourceIds) {
			ResourceEntity resource = entityManager.find(ResourceEntity.class, resourceId);
			for (ConsumedResourceRelationEntity consumedResourceRelationEntity : resource.getConsumedMasterRelations()) {
				ResourceEntity maybeCpi = consumedResourceRelationEntity.getSlaveResource();
				// only CONSUMABLE_SOFTLINK_RESOURCE_TYPES may have a SoftLinkRelation
				if (resourceLocator.hasResourceConsumableSoftlinkType(maybeCpi) && maybeCpi.getSoftlinkRelation() != null) {
					//List<ResourceEntity> ppis = softlinkRelationService.getProvidingResources(resource);
					ResourceEntity ppi = softlinkRelationService.getSoftlinkResolvableSlaveResource(maybeCpi.getSoftlinkRelation().getSoftlinkRef(), maybeCpi.getRelease());
					if (ppi != null) {
                        Map<ResourceEntity,ResourceEntity> bMap = new HashMap<>();
                        bMap.put(maybeCpi,ppi);
						aMap.put(resourceId, bMap);
					}
				}
			}
        }
        return aMap;
    }

	/**
	 * @param appServerId
	 * @param release
	 *             - if release is null, the release of the given app server is used...
	 * @return
	 */
	public List<AppServerRelationPath> getAppServerRelationsFromLiveDB(Integer appServerId, ReleaseEntity release) {

		ResourceEntity appServer = entityManager.find(ResourceEntity.class, appServerId);
		appServer = dependencyResolver.getResourceEntityForRelease(appServer.getResourceGroup(), release);
		return getAppServerRelationsInSameTransaction(appServer, release, true);
	}

	public ResourceEntity getAppServer(Integer appServerId) {
		return entityManager.find(ResourceEntity.class, appServerId);
	}
	
	
	public List<AppServerRelationPath> getAppServerRelationsInSameTransaction(ResourceEntity appServer, ReleaseEntity release, boolean fromLiveDB) {
		List<AppServerRelationPath> relations = new ArrayList<>();
		if (release == null) {
			release = appServer.getRelease();
		}
		getAllConsumedRelationsRec(release, appServer, new ArrayList<AppServerRelationCapable>(0),
				relations);
		mergeAppServerRelations(relations, fromLiveDB);
		return relations;
	}

	public List<ResourceGroupEntity> getPotentialResources(AppServerRelationPath path) {
		if (path == null || path.getLastRelationOfPath() == null) {
			return Collections.emptyList();
		}
		Integer resourceTypeId = path.getLastRelationOfPath().getSlaveResourceTypeId();
		// TODO check if we need to filter out the resourceGroups which do not exist in the requested
		// release
		TypedQuery<ResourceGroupEntity> r = entityManager.createQuery(
				"select r from ResourceGroupEntity r where r.resourceType.id=:id",
				ResourceGroupEntity.class).setParameter("id", resourceTypeId);
		return r.getResultList();
	}

    	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	private AppServerRelationHierarchyEntity createAppServerRelationHierarchyForPath(
			AppServerRelationPath path) {
		AppServerRelationHierarchyEntity parentEntity = null;
		for (AppServerRelationCapable p : path.getPath()) {
			AppServerRelationHierarchyEntity entity;
			if (p.getBaseClass() == ConsumedResourceRelationEntity.class) {
				entity = findAppServerRelationHierarchyEntityByConsumedRelation(parentEntity, p.getId());
				if (entity == null) {
					entity = new AppServerRelationHierarchyEntity();
					entity.setRelation(entityManager.find(ConsumedResourceRelationEntity.class,
							p.getId()));
					entity.setParentRelation(parentEntity);
					entityManager.persist(entity);
				}
			}
			else if (p.getBaseClass() == ResourceRelationTypeEntity.class) {
				entity = findAppServerRelationHierarchyEntityByResourceRelationType(parentEntity,
						p.getId());
				if (entity == null) {
					entity = new AppServerRelationHierarchyEntity();
					entity.setRelation(entityManager.find(ResourceRelationTypeEntity.class, p.getId()));
					entity.setParentRelation(parentEntity);
					entityManager.persist(entity);
				}
			}
			else {
				throw new RuntimeException("The path " + p + " does not have an appropriate base class!");
			}
			parentEntity = entity;
		}
		return parentEntity;
	}

    	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	private AppServerRelationHierarchyEntity findAppServerRelationHierarchyEntityByResourceRelationType(
			AppServerRelationHierarchyEntity parentEntity, Integer relId) {
		TypedQuery<AppServerRelationHierarchyEntity> query;
		if (parentEntity == null) {
			query = entityManager
					.createQuery(
							"select a from AppServerRelationHierarchyEntity a where a.parentRelation IS NULL and a.assignedResourceTypeRelation.id=:relId",
							AppServerRelationHierarchyEntity.class);
		}
		else {
			query = entityManager
					.createQuery(
							"select a from AppServerRelationHierarchyEntity a where a.parentRelation=:parentEntity and a.assignedResourceTypeRelation.id=:relId",
							AppServerRelationHierarchyEntity.class).setParameter("parentEntity",
							parentEntity);
		}
		List<AppServerRelationHierarchyEntity> result = query.setParameter("relId", relId).getResultList();
		if (result != null && result.size() > 1) {
			throw new RuntimeException("Illegal state - there are too many appserverrelations for parent "
					+ parentEntity + " and resource relation type " + relId);
		}
		return result == null || result.isEmpty() ? null : result.get(0);
	}

    	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	private AppServerRelationHierarchyEntity findAppServerRelationHierarchyEntityByConsumedRelation(
			AppServerRelationHierarchyEntity parentEntity, Integer relId) {
		TypedQuery<AppServerRelationHierarchyEntity> query;
		if (parentEntity == null) {
			query = entityManager
					.createQuery(
							"select a from AppServerRelationHierarchyEntity a where a.parentRelation IS NULL and a.assignedConsumedResourceRelation.id=:consumedResRel",
							AppServerRelationHierarchyEntity.class);
		}
		else {
			query = entityManager
					.createQuery(
							"select a from AppServerRelationHierarchyEntity a where a.parentRelation=:parentEntity and a.assignedConsumedResourceRelation.id=:consumedResRel",
							AppServerRelationHierarchyEntity.class).setParameter("parentEntity",
							parentEntity);
		}
		List<AppServerRelationHierarchyEntity> result = query.setParameter("consumedResRel", relId)
				.getResultList();
		if (result != null && result.size() > 1) {
			throw new RuntimeException("Illegal state - there are too many appserverrelations for parent "
					+ parentEntity + " and consumed resource relation " + relId);
		}
		return result == null || result.isEmpty() ? null : result.get(0);
	}

	public void storeAppServerRelationPaths(List<AppServerRelationPath> paths) {
		for (AppServerRelationPath path : paths) {
			AppServerRelationHierarchyEntity lastHierarchyEntity;
			if (path.getSelectedResourceGroup() != null) {
				if (path.getAppserverRelation() != null) {
					AppServerRelationHierarchyEntity relation = entityManager.find(
							AppServerRelationHierarchyEntity.class, path.getAppserverRelation()
									.getId());
					if (relation != null) {
						entityManager.remove(relation);
					}
					path.setAppserverRelation(null);
				}
				// The path is redefined, but no appserverrelation exists -> new appserver relation!
				lastHierarchyEntity = createAppServerRelationHierarchyForPath(path);
				lastHierarchyEntity.setOverriddenSlaveResource(path.getSelectedResource());
				path.setAppserverRelation(lastHierarchyEntity);
			}
			else if (path.getAppserverRelation() != null
					&& path.getAppserverRelation().getOverriddenSlaveResource() == null) {
				// There is a appserver relation, but no overridden slave resource. This can only mean,
				// that the app server relation has been reset. We therefore remove the appserver relation
				// from the database.
				AppServerRelationHierarchyEntity relation = entityManager.find(
						AppServerRelationHierarchyEntity.class, path.getAppserverRelation().getId());
				if (relation != null) {
					entityManager.remove(relation);
				}
				path.setAppserverRelation(null);
			}
		}
	}

    	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	void getAllConsumedRelationsRec(ReleaseEntity release, ResourceEntity masterResource,
			List<AppServerRelationCapable> previouslyFoundRelations, List<AppServerRelationPath> relations) {
		if (previouslyFoundRelations != null && !previouslyFoundRelations.isEmpty()) {
			relations.add(new AppServerRelationPath(previouslyFoundRelations));
		}

		// Ends as soon as there are no consumed master relations anymore...
		Set<Integer> resolvedTypeRelations = new HashSet<>();

		Set<ConsumedResourceRelationEntity> consumedMasterRelations = dependencyResolver
				.getConsumedMasterRelationsForRelease(masterResource, release);
		if (consumedMasterRelations != null && !consumedMasterRelations.isEmpty()) {
			for (ConsumedResourceRelationEntity rel : consumedMasterRelations) {
				resolvedTypeRelations.add(rel.getResourceRelationType().getId());
				// We can not share the list because its recursive - we therefore create new lists
				List<AppServerRelationCapable> newPreviouslyFoundRelations = new ArrayList<>(
						previouslyFoundRelations.size() + 1);
				newPreviouslyFoundRelations.addAll(previouslyFoundRelations);
				newPreviouslyFoundRelations.add(rel);
				getAllConsumedRelationsRec(release, rel.getSlaveResource(), newPreviouslyFoundRelations,
						relations);
			}
		}
		// Only non-default resource types can have unresolved relations
		if (!masterResource.getResourceType().isDefaultResourceType()) {			
			List<ResourceRelationTypeEntity> typeRelations = masterResource.getConsumedResourceRelationTypes();
			for (ResourceRelationTypeEntity typeRel : typeRelations) {
				if (!resolvedTypeRelations.contains(typeRel.getId())) {
					List<AppServerRelationCapable> newPreviouslyFoundRelations = new ArrayList<>(
							previouslyFoundRelations.size() + 1);
					newPreviouslyFoundRelations.addAll(previouslyFoundRelations);
					typeRel.setTemporaryMasterResource(masterResource);
					newPreviouslyFoundRelations.add(typeRel);
					relations.add(new AppServerRelationPath(newPreviouslyFoundRelations));
				}
			}
		}
	}
    	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	Set<Integer> getRelationsFromAppServers(List<AppServerRelationPath> relations, Class<?> baseClass) {
		Set<Integer> result = new HashSet<>();
		for (AppServerRelationPath relation : relations) {
			for (AppServerRelationCapable capable : relation.getPath()) {
				if (capable.getBaseClass() == baseClass) {
					result.add(capable.getId());
				}
			}
		}
		return result;
	}

    	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	Set<AppServerRelationCapable> getAllRelationsFromAppServers(List<AppServerRelationPath> relations) {
		Set<AppServerRelationCapable> result = new HashSet<>();
		for (AppServerRelationPath relation : relations) {
			result.addAll(relation.getPath());
		}
		return result;
	}

    	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	List<AppServerRelationHierarchyEntity> filterLastElements(
			List<AppServerRelationHierarchyEntity> allInvolvedRelations,
			List<AppServerRelationPath> relations) {
		List<AppServerRelationHierarchyEntity> result = new ArrayList<>();
		Set<Integer> lastRelationIds = new HashSet<>();
		for (AppServerRelationPath r : relations) {
			if (r.getLastRelationOfPath() != null) {
				lastRelationIds.add(r.getLastRelationOfPath().getId());
			}
		}
		for (AppServerRelationHierarchyEntity involvedRelation : allInvolvedRelations) {
			if (involvedRelation.getAssignedConsumedResourceRelation() != null) {
				if (lastRelationIds.contains(involvedRelation.getAssignedConsumedResourceRelation()
						.getId())) {
					result.add(involvedRelation);
				}
			}
		}
		return result;
	}

	/**
	 * Takes the {@link AppServerRelationPath}s, loads potentially matching relations and assures their
	 * correct paths. finally, it assigns the correct AppServerRelationHierarchyEntity if found.
	 * 
	 * @param relations
	 * @param fromLiveDB - indicating if the resources shall be loaded from the live db (can profit from performance optimizations)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	void mergeAppServerRelations(List<AppServerRelationPath> relations, boolean fromLiveDB) {
		List<AppServerRelationHierarchyEntity> allInvolvedRelations;
		if(fromLiveDB){
			allInvolvedRelations = loadInvolvedRelationsFromLiveDB(relations);
		}
		else{
			allInvolvedRelations = loadInvolvedRelationsFromHistory(relations);
		}		
		List<AppServerRelationPath> remainingPaths = new ArrayList<>(relations);
		for (AppServerRelationHierarchyEntity potentialRelation : allInvolvedRelations) {
			Iterator<AppServerRelationPath> iterator = remainingPaths.iterator();
			while (iterator.hasNext()) {
				AppServerRelationPath path = iterator.next();
				if (isSamePath(potentialRelation, path)) {
					path.setAppserverRelation(potentialRelation);
					iterator.remove();
				}
			}
		}
	}

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    private List<AppServerRelationHierarchyEntity> loadInvolvedRelationsFromLiveDB(
            List<AppServerRelationPath> relations) {
        Set<Integer> consumedRelations = getRelationsFromAppServers(relations, ConsumedResourceRelationEntity.class);
        Set<Integer> restypeRelations = getRelationsFromAppServers(relations, ResourceRelationTypeEntity.class);
        // Oracle will throw an exception if value for the IN statement is an empty list. Instead null should be assigned.
        Query query = entityManager
                .createQuery(
                        "select a from AppServerRelationHierarchyEntity a where a.assignedConsumedResourceRelation.id in (:consumedRelations) or a.assignedResourceTypeRelation.id in (:restypeRelations)",
                        AppServerRelationHierarchyEntity.class)
                .setParameter("consumedRelations", consumedRelations.isEmpty() ? null : consumedRelations)
                .setParameter("restypeRelations", restypeRelations.isEmpty() ? null : restypeRelations);
        List<AppServerRelationHierarchyEntity> allInvolvedRelations = query.getResultList();
        return allInvolvedRelations;
    }

    	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	private List<AppServerRelationHierarchyEntity> loadInvolvedRelationsFromHistory(
			List<AppServerRelationPath> relations) {
		List<AppServerRelationHierarchyEntity> result = new ArrayList<>();
		for(AppServerRelationPath relation : relations){
			for(AppServerRelationCapable capable : relation.getPath()){
				if(capable instanceof ConsumedResourceRelationEntity){
					result.addAll(((ConsumedResourceRelationEntity)capable).getAppServerRelations());
				}
				else if(capable instanceof ResourceRelationTypeEntity){
					result.addAll(((ResourceRelationTypeEntity)capable).getAppServerRelations());
				}
			}			
		}
		return result;
	}

    	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	boolean isSamePathElement(AppServerRelationHierarchyEntity relation, AppServerRelationCapable pathElement) {
		if (relation != null) {
			if (relation.getAssignedConsumedResourceRelation() != null) {
				if (pathElement == null
						|| pathElement.getBaseClass() != ConsumedResourceRelationEntity.class) {
					return false;
				}
				return relation.getAssignedConsumedResourceRelation().getId().equals(pathElement.getId());
			}
			else if (relation.getAssignedResourceTypeRelation() != null) {
				if (pathElement == null || pathElement.getBaseClass() != ResourceRelationTypeEntity.class) {
					return false;
				}
				return relation.getAssignedResourceTypeRelation().getId().equals(pathElement.getId());
			}
		}
		return false;
	}

	/**
	 * Checks if the given {@link AppServerRelationPath} matches the hierarchy defined in the
	 * {@link AppServerRelationHierarchyEntity}
	 * 
	 * @param relation
	 * @param path
	 * @return true if they are describing the same path, false otherwise
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	boolean isSamePath(AppServerRelationHierarchyEntity relation, AppServerRelationPath path) {
		// If the last path element does not match, we can just stop because it's obviously not the path
		// we're looking for...
		if (isSamePathElement(relation, path.getLastRelationOfPath())) {
			List<AppServerRelationCapable> reverseRelations = new ArrayList<>(path.getPath());
			Collections.reverse(reverseRelations);
			AppServerRelationHierarchyEntity currentRelation = relation;
			for (AppServerRelationCapable consRel : reverseRelations) {
				if (isSamePathElement(currentRelation, consRel)) {
					currentRelation = currentRelation.getParentRelation();
					// We ended up at the last element - this means, we have found the full path and
					// therefore it is the one we're looking for.
					if (currentRelation == null) {
						return true;
					}
				}
				else {
					return false;
				}
			}
		}
		return false;
	}
}
