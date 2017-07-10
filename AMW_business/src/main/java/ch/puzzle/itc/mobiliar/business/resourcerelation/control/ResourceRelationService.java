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

/*
 * To change this license header, choose License Headers in Project Properties. To change this template file,
 * choose Tools | Templates and open the template in the editor.
 */
package ch.puzzle.itc.mobiliar.business.resourcerelation.control;

import ch.puzzle.itc.mobiliar.business.domain.commons.CommonDomainService;
import ch.puzzle.itc.mobiliar.business.foreignable.control.ForeignableService;
import ch.puzzle.itc.mobiliar.business.foreignable.entity.ForeignableOwner;
import ch.puzzle.itc.mobiliar.business.generator.control.extracted.ResourceDependencyResolverService;
import ch.puzzle.itc.mobiliar.business.property.entity.ResourceEditRelation;
import ch.puzzle.itc.mobiliar.business.resourcegroup.control.ResourceEntityService;
import ch.puzzle.itc.mobiliar.business.resourcegroup.control.ResourceTypeProvider;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.Application;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ApplicationServer;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceGroupEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.AbstractResourceRelationEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ConsumedResourceRelationEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ProvidedResourceRelationEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ResourceRelationTypeEntity;
import ch.puzzle.itc.mobiliar.business.security.control.PermissionService;
import ch.puzzle.itc.mobiliar.business.security.entity.Action;
import ch.puzzle.itc.mobiliar.business.security.entity.Permission;
import ch.puzzle.itc.mobiliar.business.security.interceptor.HasPermission;
import ch.puzzle.itc.mobiliar.common.exception.ElementAlreadyExistsException;
import ch.puzzle.itc.mobiliar.common.exception.ResourceNotFoundException;
import ch.puzzle.itc.mobiliar.common.util.DefaultResourceTypeDefinition;
import org.apache.commons.lang.StringUtils;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.io.Serializable;
import java.util.*;
import java.util.logging.Logger;

/**
 * @author oschmid
 */
@Stateless
public class ResourceRelationService implements Serializable{

	@Inject
	EntityManager entityManager;

	@Inject
	ResourceTypeProvider resourceTypeProvider;

	@Inject
	ResourceEntityService resourceEntityService;

	@Inject
	CommonDomainService commonService;

	@Inject
	ForeignableService foreignableService;

	@Inject
	PermissionService permissionService;

    @Inject
    ResourceDependencyResolverService resourceDependencyResolverService;

	@Inject
	public Logger log;

	/**
	 * Returns the resource relation entity based on its id. ATTENTION: This method assumes, that the ids
	 * between the two instances are exclusive! This is currently achieved by using the same sequence for id
	 * generation (defined on the abstract class).
	 * 
	 * @param resourceRelationId
	 * @return
	 */
	public AbstractResourceRelationEntity getResourceRelation(Integer resourceRelationId) {
		AbstractResourceRelationEntity result = getConsumedResourceRelation(resourceRelationId);
		if (result == null) {
			// If the resource relation id is not found within the consumed resource relations, it is
			// probably a provided relation (otherwise it is a wrong id and we return null)!
			result = entityManager.find(ProvidedResourceRelationEntity.class, resourceRelationId);
		}
		return result;
	}


     public ConsumedResourceRelationEntity getConsumedResourceRelation(Integer consumedResourceRelationId){
		return entityManager.find(ConsumedResourceRelationEntity.class,	consumedResourceRelationId);
	}

	/**
	 * @return
	 */
	public ResourceRelationTypeEntity getResourceTypeRelation(Integer resourceTypeRelationId) {
		return entityManager.find(ResourceRelationTypeEntity.class, resourceTypeRelationId);
	}

	/**
	 * Adds a relation for all releases existing for slave resource
	 * 
	 * @param masterId
	 * @param slaveGroupId
	 * @param provided
	 * @param identifier
	 * @param typeIdentifier
	 * @throws ElementAlreadyExistsException
	 * @throws ResourceNotFoundException
	 */
	public void addRelationByGroup(Integer masterId, Integer slaveGroupId, boolean provided, Integer identifier,
			String typeIdentifier, ForeignableOwner changingOwner) throws ElementAlreadyExistsException, ResourceNotFoundException {
		ResourceEntity master = entityManager.find(ResourceEntity.class, masterId);
		ResourceGroupEntity slaveGroup = entityManager.find(ResourceGroupEntity.class, slaveGroupId);

		String slaveResourceType = slaveGroup.getResourceType().getName();
		if (DefaultResourceTypeDefinition.NODE.name().equals(slaveResourceType)) {
			addNodeByGroup(masterId, slaveGroupId, provided, identifier, typeIdentifier, changingOwner);
		}
		else if (DefaultResourceTypeDefinition.APPLICATION.name()
				.equals(master.getResourceType().getName())) {
			permissionService.checkPermissionAndFireException(Permission.RESOURCE, null, Action.UPDATE, master.getResourceGroup(), null, null);
			addRelationInEditInstanceApplicationByGroup(masterId, slaveGroupId, provided,
					identifier, typeIdentifier, changingOwner);
		} else if (slaveGroup.getResourceType().isRuntimeType()) {
			resourceEntityService.setRuntime(master, slaveGroup, changingOwner);
		} else {
			addEveryRelationByGroup(masterId, slaveGroupId, provided, identifier,
					typeIdentifier, changingOwner);
			// If an application is added to the applicationserver, it has to be removed from the
			// applications without applicationserver container
			if (DefaultResourceTypeDefinition.APPLICATIONSERVER.name().equals(
					master.getResourceType().getName())){
				if(DefaultResourceTypeDefinition.APPLICATION.name().equals(slaveResourceType)) {
					removeAppFromContainerGroupAndRemoveGroupIfEmpty(slaveGroup);
				}
			}
		}
	}

	/**
	 * Entfernt Application aus ApplicationServer. Falls ApplicationServerContainer nur die soeben
	 * verschobene Applikation enthält, so soll diese Gruppe gelöscht werden.
	 * 
	 * @throws ResourceNotFoundException
	 */
	public void removeAppFromContainerGroupAndRemoveGroupIfEmpty(ResourceGroupEntity applicationGroup)
			throws ResourceNotFoundException {
		if (applicationGroup != null) {

			ApplicationServer applicationCollectorGroup = commonService
					.createOrGetApplicationCollectorServer();
			List<Application> applications = applicationCollectorGroup.getApplications();
			for (Application app : applications) {
				if (applicationGroup.getResources().contains(app.getEntity())) {
					List<AbstractResourceRelationEntity> relations = getConsumedRelationsByMasterAndSlave(
							applicationCollectorGroup.getEntity(), applicationGroup.getResources(),
							null);
					if (!relations.isEmpty()) {
						doRemoveResourceRelationForAllReleases(relations.get(0).getId());
					}
				}
			}
			// Falls ApplicationServerContainer nur die soeben verschobene
			// Applikation enthält, so soll diese Gruppe gelöscht werden!
			if (applications.size() == 0 || applications.size() == applicationGroup.getResources().size()) {
				entityManager.remove(applicationCollectorGroup.getEntity());
				log.info("ApplicationServerContainer removed");
			}
		}
	}

	/**
	 * Adds a relation for all releases existing for slave resource<br>
	 * Only for users with permission to add every related resource
	 * 
	 * @param masterId
	 * @param slaveGroupId
	 * @param provided
	 * @param identifier
	 * @param typeIdentifier
	 * @throws ResourceNotFoundException
	 * @throws ElementAlreadyExistsException
	 */
	@HasPermission(permission = Permission.ADD_EVERY_RELATED_RESOURCE)
	private void addEveryRelationByGroup(Integer masterId, Integer slaveGroupId, boolean provided, Integer identifier,
			String typeIdentifier, ForeignableOwner changingOwner) throws ResourceNotFoundException, ElementAlreadyExistsException {
		doAddResourceRelationForAllReleases(masterId, slaveGroupId, provided, identifier,
				typeIdentifier, changingOwner);
	}

	/**
	 * Adds a relation for all releases existing for slave resource<br>
	 * Only for users with permission to add related resource
	 * 
	 * @param masterId
	 * @param slaveGroupId
	 * @param provided
	 * @param identifier
	 * @param typeIdentifier
	 * @throws ElementAlreadyExistsException
	 * @throws ResourceNotFoundException
	 */
	private void addRelationInEditInstanceApplicationByGroup(Integer masterId, Integer slaveGroupId, boolean provided, Integer identifier,
			String typeIdentifier, ForeignableOwner changingOwner) throws ElementAlreadyExistsException, ResourceNotFoundException {
		doAddResourceRelationForAllReleases(masterId, slaveGroupId, provided, identifier,
				typeIdentifier, changingOwner);
	}

	/**
	 * Adds a relation for all releases existing for slave resource<br>
	 * Only for users with permission to add node
	 * 
	 * @param masterId
	 * @param slaveGroupId
	 * @param provided
	 * @param identifier
	 * @param typeIdentifier
	 * @throws ElementAlreadyExistsException
	 * @throws ResourceNotFoundException
	 */
	@HasPermission(permission = Permission.ADD_NODE_RELATION)
	private void addNodeByGroup(Integer masterId, Integer slaveGroupId, boolean provided, Integer identifier,
			String typeIdentifier, ForeignableOwner changingOwner) throws ElementAlreadyExistsException, ResourceNotFoundException {
		doAddResourceRelationForAllReleases(masterId, slaveGroupId, provided, identifier,
                typeIdentifier, changingOwner);
	}

	public void doAddResourceRelationForAllReleases(Integer masterId, Integer slaveGroupId, boolean provided, Integer identifier,
			String typeIdentifier, ForeignableOwner changingOwner) throws ElementAlreadyExistsException, ResourceNotFoundException {
		addResourceRelation(masterId, slaveGroupId, provided, identifier, typeIdentifier, null, changingOwner);

	}

	/**
	 * Adds a relation for a specific release
	 *
	 * @param masterId
	 * @param slaveGroupId
	 * @param provided
	 * @param identifier
	 * @param typeIdentifier
	 * @param releaseId
	 * @param changingOwner
	 * @throws ElementAlreadyExistsException
	 */
	public void doAddResourceRelationForSpecificRelease(Integer masterId, Integer slaveGroupId, boolean provided, Integer identifier,
													String typeIdentifier, Integer releaseId, ForeignableOwner changingOwner) throws ElementAlreadyExistsException, ResourceNotFoundException {
		addResourceRelation(masterId, slaveGroupId, provided, identifier, typeIdentifier, releaseId, changingOwner);
	}

	private void addResourceRelation(Integer masterId, Integer slaveGroupId, boolean provided, Integer identifier,
									 String typeIdentifier, Integer releaseId, ForeignableOwner changingOwner) throws ElementAlreadyExistsException {
		ResourceEntity master = entityManager.find(ResourceEntity.class, masterId);
		ResourceGroupEntity slaveGroup = entityManager.find(ResourceGroupEntity.class, slaveGroupId);
		ResourceRelationTypeEntity resourceRelationType = resourceTypeProvider
				.getOrCreateResourceRelationTypeIncludingParents(master.getResourceType(), slaveGroup.getResources()
						.iterator().next().getResourceType(), typeIdentifier);

        for (ResourceEntity slave : slaveGroup.getResources()) {
            if (releaseId == null || slave.getRelease().getId().equals(releaseId)) {

                if (provided) {
                    master.addProvidedResourceRelation(slave, resourceRelationType, changingOwner);
                } else {
                    master.addConsumedResourceRelation(slave, resourceRelationType, identifier, changingOwner);
                }
                log.info("Relation between resourceId: " + master.getId() + " and resourceId: "
                        + slave.getId() + " added");
            }
        }

		entityManager.persist(master);

	}


    public void deleteRelation(AbstractResourceRelationEntity relation){
        if (relation != null){
            entityManager.remove(relation);
        }
    }

	/**
	 * Removes all relations for all releases of the slave resource
	 * 
	 * @throws ResourceNotFoundException
	 * @throws ElementAlreadyExistsException
	 */
	public void removeRelation(AbstractResourceRelationEntity relation) throws ResourceNotFoundException, ElementAlreadyExistsException {
        Integer relationId = relation.getId();
		ResourceEntity master = entityManager.find(ResourceEntity.class, relation.getMasterResource()
				.getId());
		String slaveResourceType = relation.getSlaveResource().getResourceType().getName();
		if (DefaultResourceTypeDefinition.NODE.name().equals(slaveResourceType)) {
			removeNodeRelation(relationId);
		}
		else if (DefaultResourceTypeDefinition.APPLICATION.name()
				.equals(master.getResourceType().getName())) {
			permissionService.checkPermissionAndFireException(Permission.RESOURCE, null, Action.UPDATE, master.getResourceGroup(), null, null);
			doRemoveResourceRelationForAllReleases(relationId);
		}
		// If an application is removed from the applicationserver
		// and application is not related to the applicationserver in an other release,
		// it has to be added to the applications without applicationserver container (otherwise removed)
		else if (DefaultResourceTypeDefinition.APPLICATIONSERVER.name().equals(
				master.getResourceType().getName())
				&& DefaultResourceTypeDefinition.APPLICATION.name().equals(slaveResourceType)) {

			// list all appServers consuming slave
			List<ResourceEntity> appServersConsumingSlave = relation.getSlaveResource()
					.getMasterResourcesOfConsumedSlaveRelationByResourceType(
							DefaultResourceTypeDefinition.APPLICATIONSERVER);

			for (Iterator<ResourceEntity> it = appServersConsumingSlave.iterator(); it.hasNext();) {
				ResourceEntity next = it.next();
				if (next.getId().equals(master.getId())) {
					// remove appServer from relation we are currently editing
					it.remove();
				}
			}
			if (appServersConsumingSlave.size() <= 0) {
				// there is no other relation to appServer, therefore add to applications without
				// applicationserver collector
				ApplicationServer applicationCollectorServer = commonService
						.createOrGetApplicationCollectorServer();
				// This has to be done for the whole resource group...
				for (ResourceEntity r : relation.getSlaveResource().getResourceGroup().getResources()) {
					r.changeResourceRelation(master, applicationCollectorServer.getEntity(),
							relation.getResourceRelationType());
				}
			}
			else {
				permissionService.checkPermissionAndFireException(Permission.RESOURCE, null, Action.UPDATE, master.getResourceGroup(), null, null);
				// there are still relations to the appServer in other releases, therefore remove this one
				doRemoveResourceRelationForAllReleases(relationId);
			}

		}
		else {
			permissionService.checkPermissionAndFireException(Permission.RESOURCE, null, Action.UPDATE, master.getResourceGroup(), null, null);
			doRemoveResourceRelationForAllReleases(relationId);
		}
	}

	@HasPermission(permission = Permission.DELETE_NODE_RELATION)
	private void removeNodeRelation(Integer rel) throws ResourceNotFoundException{
		doRemoveResourceRelationForAllReleases(rel);
	}

	private void doRemoveResourceRelationForAllReleases(Integer relationId) throws ResourceNotFoundException {

		// check for Consumed and ProvidedRelations
		AbstractResourceRelationEntity relation = getResourceRelation(relationId);

		if (relation != null) {
			boolean provided = (relation instanceof ProvidedResourceRelationEntity);
			Set<ResourceEntity> slaveResources = relation.getSlaveResource().getResourceGroup()
					.getResources();

			List<AbstractResourceRelationEntity> relations;
			if (provided) {
				relations = getProvidedRelationsByMasterAndSlave(relation.getMasterResource(),
						slaveResources, relation.getIdentifier());
			}
			else {
				relations = getConsumedRelationsByMasterAndSlave(relation.getMasterResource(),
						slaveResources, relation.getIdentifier());
			}

			boolean hasResourceIdentifier = !StringUtils.isEmpty(relation.getIdentifier());
			boolean hasResourceTypeIdentifier = !StringUtils.isEmpty(relation.getResourceRelationType()
					.getIdentifier());

			for (AbstractResourceRelationEntity rel : relations) {
				if (isMatchingRelation(relation, hasResourceIdentifier, hasResourceTypeIdentifier, rel)) {
					relation.getMasterResource().removeRelation(rel);
					relation.getSlaveResource().removeSlaveRelation(rel);
					entityManager.remove(rel);
					log.info("Relation between resourceId: " + relation.getMasterResource().getId()
							+ " and resourceId: " + rel.getSlaveResource().getId() + " removed");
				}
			}
		}
		else {
			throw new ResourceNotFoundException("Relation not found");
		}
	}

	private boolean isMatchingRelation(AbstractResourceRelationEntity relation, boolean hasResourceIdentifier,
									   boolean hasResourceTypeIdentifier, AbstractResourceRelationEntity rel) {
		if (hasResourceIdentifier && relation.getIdentifier().equals(rel.getIdentifier())) {
            return true;
        }
        else if (hasResourceTypeIdentifier
                && rel.getResourceRelationType() != null
                && relation.getResourceRelationType().getIdentifierOrTypeBName()
                .equals(rel.getResourceRelationType().getIdentifierOrTypeBName())) {
            return true;
        }
        else if (rel.getIdentifier() == null
                && (rel.getResourceRelationType() == null || rel.getResourceRelationType()
                .getIdentifier() == null)) {
            return true;
        }
		return false;
	}

	protected List<AbstractResourceRelationEntity> getConsumedRelationsByMasterAndSlave(
			ResourceEntity masterResource, Set<ResourceEntity> slaveResources, String identifier) {
		List<AbstractResourceRelationEntity> consumed = new ArrayList<>();
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<ConsumedResourceRelationEntity> q = cb
				.createQuery(ConsumedResourceRelationEntity.class);
		Root<ConsumedResourceRelationEntity> r = q.from(ConsumedResourceRelationEntity.class);
		Predicate masterResourceIs = cb.equal(r.<ResourceEntity> get("masterResource"), masterResource);
		Predicate slaveResourceIn = r.<ResourceEntity> get("slaveResource").in(slaveResources);

		if (!StringUtils.isEmpty(identifier)) {
			Predicate identifierIs = cb.equal(r.<String> get("identifier"), identifier);
			q.where(cb.and(masterResourceIs, slaveResourceIn, identifierIs));
		}
		else {
			q.where(cb.and(masterResourceIs, slaveResourceIn));
		}

		TypedQuery<ConsumedResourceRelationEntity> query = entityManager.createQuery(q);
		List<ConsumedResourceRelationEntity> result = query.getResultList();
		for (ConsumedResourceRelationEntity rel : result) {
			consumed.add(rel);
		}
		return consumed;
	}

	protected List<AbstractResourceRelationEntity> getProvidedRelationsByMasterAndSlave(
			ResourceEntity masterResource, Set<ResourceEntity> slaveResources, String identifier) {
		List<AbstractResourceRelationEntity> provided = new ArrayList<>();
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<ProvidedResourceRelationEntity> q = cb
				.createQuery(ProvidedResourceRelationEntity.class);
		Root<ProvidedResourceRelationEntity> r = q.from(ProvidedResourceRelationEntity.class);
		Predicate masterResourceIs = cb.equal(r.<ResourceEntity> get("masterResource"), masterResource);
		Predicate slaveResourceIn = r.<ResourceEntity> get("slaveResource").in(slaveResources);

		if (!StringUtils.isEmpty(identifier)) {
			Predicate identifierIs = cb.equal(r.<String> get("identifier"), identifier);
			q.where(cb.and(masterResourceIs, slaveResourceIn, identifierIs));
		}
		else {
			q.where(cb.and(masterResourceIs, slaveResourceIn));
		}

		TypedQuery<ProvidedResourceRelationEntity> query = entityManager.createQuery(q);
		List<ProvidedResourceRelationEntity> result = query.getResultList();
		for (ProvidedResourceRelationEntity rel : result) {
			provided.add(rel);
		}
		return provided;
	}

	public List<ConsumedResourceRelationEntity> getConsumedSlaveRelations(ResourceEntity slaveResource) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<ConsumedResourceRelationEntity> q = cb
				.createQuery(ConsumedResourceRelationEntity.class);
		Root<ConsumedResourceRelationEntity> r = q.from(ConsumedResourceRelationEntity.class);
		Join<ConsumedResourceRelationEntity, ResourceEntity> slaveResourceJoin = r.join("slaveResource");
		q.where(cb.equal(slaveResourceJoin.get("id"), slaveResource.getId()));

		TypedQuery<ConsumedResourceRelationEntity> query = entityManager.createQuery(q);
		return query.getResultList();
	}

	public List<ProvidedResourceRelationEntity> getProvidedSlaveRelations(ResourceEntity slaveResource) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<ProvidedResourceRelationEntity> q = cb
				.createQuery(ProvidedResourceRelationEntity.class);
		Root<ProvidedResourceRelationEntity> r = q.from(ProvidedResourceRelationEntity.class);
		Join<ProvidedResourceRelationEntity, ResourceEntity> slaveResourceJoin = r.join("slaveResource");
		q.where(cb.equal(slaveResourceJoin.get("id"), slaveResource.getId()));

		TypedQuery<ProvidedResourceRelationEntity> query = entityManager.createQuery(q);
		return query.getResultList();
	}


    /**
     * Find best matching release relation for selected resource in release
     * @param releaseRelations
     * @param releaseResource
     * @return
     */
    public ResourceEditRelation getBestMatchingRelationRelease(List<ResourceEditRelation> releaseRelations, ResourceEntity releaseResource) {
        long currentTime = releaseResource != null && releaseResource.getRelease() != null && releaseResource.getRelease().getInstallationInProductionAt() != null ? releaseResource.getRelease().getInstallationInProductionAt().getTime() : (new Date()).getTime();

		ResourceEditRelation bestMatch = findBestMatchingPastRelease(releaseRelations, currentTime);

        if (bestMatch == null){
            bestMatch = findBestMatchingFutureRelease(releaseRelations, currentTime);
        }

        return bestMatch;
    }

    private ResourceEditRelation findBestMatchingPastRelease(List<ResourceEditRelation> releaseRelations, long currentTime) {
        ResourceEditRelation bestMatch = null;
        for (ResourceEditRelation relation : releaseRelations) {
            if (relation != null){
                long releaseInstallationTime = relation.getSlaveReleaseDate().getTime();
                Long bestMatchingReleaseTime = bestMatch != null ? bestMatch.getSlaveReleaseDate().getTime() : null;

                if (resourceDependencyResolverService.isBestMatchingPastReleaseTime(bestMatchingReleaseTime, releaseInstallationTime, currentTime)) {
                    bestMatch = relation;
                }
            }
        }
        return bestMatch;
    }

    private ResourceEditRelation findBestMatchingFutureRelease(List<ResourceEditRelation> releaseRelations, long currentTime) {
        ResourceEditRelation bestMatch = null;
        for (ResourceEditRelation relation : releaseRelations) {
            if (relation != null){
                long releaseInstallationTime = relation.getSlaveReleaseDate().getTime();
                Long bestMatchingReleaseTime = bestMatch != null ? bestMatch.getSlaveReleaseDate().getTime() : null;

                if (resourceDependencyResolverService.isBestMatchingFutureReleaseTime(bestMatchingReleaseTime, releaseInstallationTime, currentTime)) {
                    bestMatch = relation;
                }
            }
        }
        return bestMatch;
    }

}
