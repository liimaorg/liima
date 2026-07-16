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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.apache.commons.lang3.StringUtils;
import java.util.Arrays;

import ch.puzzle.itc.mobiliar.business.resourcegroup.boundary.ResourceGroupLocator;
import ch.puzzle.itc.mobiliar.business.resourcegroup.boundary.ResourceLocator;
import ch.puzzle.itc.mobiliar.business.resourcegroup.control.ResourceGroupPersistenceService;
import ch.puzzle.itc.mobiliar.business.resourcegroup.control.ResourceTypeDomainService;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceGroupEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceTypeEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.control.ResourceRelationService;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.AbstractResourceRelationEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ResourceRelationTypeEntity;
import ch.puzzle.itc.mobiliar.common.exception.ElementAlreadyExistsException;
import ch.puzzle.itc.mobiliar.common.exception.ResourceNotFoundException;
import ch.puzzle.itc.mobiliar.common.exception.ResourceTypeNotFoundException;
import ch.puzzle.itc.mobiliar.common.exception.ValidationException;

/**
 * A boundary for relation editing
 * 
 * @author cweber
 */
@Stateless
public class RelationEditor {

	@Inject
	EntityManager entityManager;

	@Inject
	ResourceRelationService resourceRelationService;

	@Inject
	ResourceRelationBoundary resourceRelationBoundary;

	@Inject
	ResourceTypeDomainService resourceTypeDomainService;

	@Inject
	ResourceLocator resourceLocator;

	@Inject
	ResourceGroupLocator resourceGroupLocator;

    public enum ResourceRelationType {
		CONSUMED,
		PROVIDED
	}

	/**
	 * @param masterId
	 * @param provided
	 * @param relationName
	 * @throws ResourceNotFoundException
	 * @throws ElementAlreadyExistsException
	 */
	public void addRelation(Integer masterId, Integer slaveGroupId, boolean provided, String relationName)
			throws ResourceNotFoundException, ElementAlreadyExistsException {
		resourceRelationService.addRelationByGroup(masterId, slaveGroupId, provided, relationName, null);
	}

	/**
	 *
	 * @param masterGroupName
	 * @param slaveGroupName
	 * @param provided
	 * @param relationName
	 * @param typeIdentifier
	 * @param releaseName
	 * @throws ResourceNotFoundException
	 * @throws ElementAlreadyExistsException
	 * @throws ValidationException
	 */
	public void addResourceRelationForSpecificRelease(String masterGroupName, String slaveGroupName, boolean provided,
			String relationName, String typeIdentifier, String releaseName)
			throws ResourceNotFoundException, ElementAlreadyExistsException, ValidationException {

		ResourceEntity master = resourceLocator.getResourceByGroupNameAndRelease(masterGroupName, releaseName);
		if (master == null) {
			throw new ResourceNotFoundException("Resource with name '" + masterGroupName + "' and Release '" + releaseName + "' not found");
		}
		// a Resource shall only be provided by one ResourceGroup
		if (typeIdentifier.toLowerCase().equals("provided") && !resourceRelationBoundary.isAddableAsProvidedResourceToResourceGroup(master, slaveGroupName)) {
			throw new ValidationException("Resource '" + slaveGroupName + "' is already provided by another ResourceGroup");
		}

		ResourceGroupEntity slaveGroup = null;
		try {
			slaveGroup = resourceGroupLocator.getResourceGroupByName(slaveGroupName);
		} catch (RuntimeException e) {
			throw new ResourceNotFoundException("ResourceGroup with name '" + slaveGroupName + "' not found");
		}

		resourceRelationService.addRelationByGroup(master.getId(), slaveGroup.getId(), provided, relationName,
				typeIdentifier);
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
	 * Removes a consumed or provided ResourceRelationEntity, identified either by its relation name (aka relation identifier) or by the name of the slave resouce (group)
	 *
	 * @param relations a Collection containing consumed or provided ResourceRelationEntities (the haystack)
	 * @param relationName the relation "name" or the name of the slave resource (group)
	 * @return boolean true if it has been removed, false if it could not be found
	 * @throws ResourceNotFoundException
	 * @throws ElementAlreadyExistsException
	 */
	public boolean removeMatchingRelation(Collection<? extends AbstractResourceRelationEntity> relations, String relationName)
			throws ResourceNotFoundException, ElementAlreadyExistsException {
		for (AbstractResourceRelationEntity relation : relations) {
			if (isMatchingRelationName(relation, relationName)) {
				removeRelation(relation.getId());
				return true;
			}
		}
		return false;
	}

	protected boolean isMatchingRelationName(AbstractResourceRelationEntity relation, String relationName) {
		if (relation.getIdentifier() != null && relation.getIdentifier().equals(relationName)) {
			return true;
		}
		return relation.getIdentifier() == null && relation.getSlaveResource().getName().equals(relationName);
	}

	/**
	 * @param relationId
	 * @throws ResourceNotFoundException
	 * @throws ElementAlreadyExistsException
	 */
	public void removeRelation(Integer relationId) throws ResourceNotFoundException, ElementAlreadyExistsException {
        AbstractResourceRelationEntity resourceRelationEntity = resourceRelationService.getResourceRelation(relationId);

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

	public boolean isValidResourceRelationType(String resourceRelationTypeString) {
		if (StringUtils.isEmpty(resourceRelationTypeString)) {
			return false;
		}
		return Arrays.stream(ResourceRelationType.values())
				.anyMatch(type -> type.name().equalsIgnoreCase(resourceRelationTypeString));
	}

}
