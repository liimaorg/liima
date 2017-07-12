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

package ch.puzzle.itc.mobiliar.business.resourcerelation.boundary;

import java.util.*;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceException;
import javax.persistence.TypedQuery;

import ch.puzzle.itc.mobiliar.business.foreignable.control.ForeignableService;
import ch.puzzle.itc.mobiliar.business.foreignable.entity.ForeignableOwner;
import ch.puzzle.itc.mobiliar.business.foreignable.entity.ForeignableOwnerViolationException;
import ch.puzzle.itc.mobiliar.business.resourcegroup.boundary.ResourceGroupLocator;
import ch.puzzle.itc.mobiliar.business.resourcegroup.boundary.ResourceLocator;
import ch.puzzle.itc.mobiliar.business.resourcegroup.control.ResourceGroupPersistenceService;
import ch.puzzle.itc.mobiliar.business.resourcegroup.control.ResourceRepository;
import ch.puzzle.itc.mobiliar.business.resourcegroup.control.ResourceTypeDomainService;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.NamedIdentifiable;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceGroupEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceTypeEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.control.ResourceRelationService;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.AbstractResourceRelationEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ResourceRelationTypeEntity;
import ch.puzzle.itc.mobiliar.business.utils.ValidationException;
import ch.puzzle.itc.mobiliar.common.exception.ElementAlreadyExistsException;
import ch.puzzle.itc.mobiliar.common.exception.ResourceNotFoundException;
import ch.puzzle.itc.mobiliar.common.exception.ResourceTypeNotFoundException;
import ch.puzzle.itc.mobiliar.common.util.DefaultResourceTypeDefinition;

/**
 * A boundary for relation editing
 * 
 * @author cweber
 */
@Stateless
@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
public class RelationEditor {

	@Inject
	EntityManager entityManager;

	@Inject
	ResourceRelationService resourceRelationService;

	@Inject
	ResourceTypeDomainService resourceTypeDomainService;

	@Inject
	ResourceRepository resourceRepository;

	@Inject
	ResourceLocator resourceLocator;

	@Inject
	ResourceGroupLocator resourceGroupLocator;

    @Inject
    ForeignableService foreignableService;

	@Inject
	private ResourceGroupPersistenceService resourceGroupService;

	/**
	 * Load all resourceGroups for the given type
	 * 
	 * @param typeName
	 * @param resourceId
	 *             - the resourceGroup for this resource will be excluded
	 * @return a list with resourceGroup entities
	 */
	public List<? extends NamedIdentifiable> loadResourceGroupsForType(String typeName, Integer resourceId) {
		ResourceEntity resource = resourceRepository
				.loadWithResourceGroupAndRelatedResourcesForId(resourceId);
		return resourceGroupService.loadGroupsForTypeNameExcludeSelected(typeName,
				Collections.singletonList(resource.getResourceGroup().getId()));
	}

	/**
	 * @param masterId
	 * @param provided
	 * @param relationName
	 * @throws ResourceNotFoundException
	 * @throws ElementAlreadyExistsException
	 */
	public void addRelation(Integer masterId, Integer slaveGroupId, boolean provided, String relationName,
			ForeignableOwner changingOwner) throws ResourceNotFoundException, ElementAlreadyExistsException {
		resourceRelationService.addRelationByGroup(masterId, slaveGroupId, provided, relationName, null, changingOwner);
	}

	/**
	 *
	 * @param masterGroupName
	 * @param slaveGroupName
	 * @param provided
	 * @param relationName
	 * @param typeIdentifier
	 * @param releaseName
	 * @param changingOwner
	 * @throws ResourceNotFoundException
	 * @throws ElementAlreadyExistsException
	 * @throws ValidationException
	 */
	public void addResourceRelationForSpecificRelease(String masterGroupName, String slaveGroupName, boolean provided,
			String relationName, String typeIdentifier, String releaseName, ForeignableOwner changingOwner)
			throws ResourceNotFoundException, ElementAlreadyExistsException, ValidationException {

		ResourceEntity master = resourceLocator.getResourceByGroupNameAndRelease(masterGroupName, releaseName);
		if (master == null) {
			throw new ResourceNotFoundException("Resource with name '" + masterGroupName + "' and Release '" + releaseName + "' not found");
		}
		ResourceGroupEntity slaveGroup = null;
		try {
			slaveGroup = resourceGroupLocator.getResourceGroupByName(slaveGroupName);
		} catch (RuntimeException e) {
			throw new ResourceNotFoundException("ResourceGroup with name '" + slaveGroupName + "' not found");
		}

		resourceRelationService.addRelationByGroup(master.getId(), slaveGroup.getId(), provided, relationName,
				typeIdentifier, changingOwner);
	}

	public void addResourceTypeRelation(ResourceTypeEntity masterType, Integer slaveResourceTypeId)
			throws ResourceTypeNotFoundException {
		ResourceTypeEntity slaveResourceType = entityManager.find(ResourceTypeEntity.class,
				slaveResourceTypeId);
		Set<ResourceRelationTypeEntity> relations = slaveResourceType.getResourceRelationTypesB();
		List<String> identifiers = new ArrayList<>();
		for (ResourceRelationTypeEntity relation : relations) {
			if (relation.getResourceTypeA().getId().equals(masterType.getId())) {
				identifiers.add(relation.getRelationIdentifier());
			}
		}
		resourceTypeDomainService.createResourceTypeRelation(masterType.getId(), slaveResourceTypeId,
				resourceTypeDomainService.nextFreeIdentifier(identifiers, slaveResourceType.getName(),
						null));
	}

	/**
	 * @param relationId
	 * @throws ResourceNotFoundException
	 * @throws ElementAlreadyExistsException
	 */
	public void removeRelation(ForeignableOwner deletingOwner, Integer relationId) throws ResourceNotFoundException, ElementAlreadyExistsException, ForeignableOwnerViolationException {
        AbstractResourceRelationEntity resourceRelationEntity = resourceRelationService.getResourceRelation(relationId);

        foreignableService.verifyDeletableByOwner(deletingOwner, resourceRelationEntity);
        resourceRelationService.removeRelation(resourceRelationEntity);
	}

	/**
	 * @throws ResourceNotFoundException
	 * @throws ElementAlreadyExistsException
	 */
	public void removeResourceTypeRelation(Integer resourceTypeRelationId) throws
			ResourceTypeNotFoundException {
		resourceTypeDomainService.removeResourceTypeRelation(resourceTypeRelationId);
	}

	public Set<ResourceGroupEntity> getApplicationsFromOtherResourcesInGroup(ResourceEntity resource) {

		// ... but we can also add those applications, which are already consumed by another resource of our
		// own resoure group as long as it is not connected to our release...
		Set<ResourceGroupEntity> applicationsFromOtherResourcesInGroup = new HashSet<>();
		resource = entityManager.find(ResourceEntity.class, resource.getId());
		for (ResourceEntity r : resource.getResourceGroup().getResources()) {
			if (!resource.equals(r)) {
				List<ResourceEntity> resourceEntities = r
						.getConsumedRelatedResourcesByResourceType(DefaultResourceTypeDefinition.APPLICATION);
				if (resourceEntities != null) {
					for (ResourceEntity r2 : resourceEntities) {
						applicationsFromOtherResourcesInGroup.add(getResourceGroupWithAllResources(r2
								.getResourceGroup().getId()));
					}
				}
			}
		}
		return applicationsFromOtherResourcesInGroup;
	}

	public ResourceGroupEntity getResourceGroupWithAllResources(Integer resourceGroupId) {
		try {
			TypedQuery<ResourceGroupEntity> resGroupQuery = entityManager
					.createQuery(
							"select rg from ResourceGroupEntity rg left join fetch rg.resources where rg.id=:id",
							ResourceGroupEntity.class).setParameter("id", resourceGroupId);
			return resGroupQuery.getSingleResult();
		}
		catch (NoResultException e) {
			return null;
		}
	}

}
