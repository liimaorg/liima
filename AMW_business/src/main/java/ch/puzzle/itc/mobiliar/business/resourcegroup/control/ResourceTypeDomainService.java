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

import ch.puzzle.itc.mobiliar.business.auditview.control.AuditService;
import ch.puzzle.itc.mobiliar.business.database.control.QueryUtils;
import ch.puzzle.itc.mobiliar.business.domain.commons.CommonDomainService;
import ch.puzzle.itc.mobiliar.business.environment.control.ContextDomainService;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceType;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceTypeEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ResourceRelationTypeEntity;
import ch.puzzle.itc.mobiliar.business.security.control.PermissionService;
import ch.puzzle.itc.mobiliar.business.security.control.RestrictionRepository;
import ch.puzzle.itc.mobiliar.business.security.entity.Action;
import ch.puzzle.itc.mobiliar.business.security.entity.Permission;
import ch.puzzle.itc.mobiliar.business.security.interceptor.HasPermission;
import ch.puzzle.itc.mobiliar.common.exception.ElementAlreadyExistsException;
import ch.puzzle.itc.mobiliar.common.exception.ResourceNotFoundException;
import ch.puzzle.itc.mobiliar.common.exception.ResourceTypeNotFoundException;
import ch.puzzle.itc.mobiliar.common.exception.ValidationException;
import org.apache.commons.lang3.StringUtils;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.criteria.*;
import java.util.List;
import java.util.logging.Logger;

@Stateless
public class ResourceTypeDomainService {

	@Inject
	private Logger log;

	@Inject
	private EntityManager entityManager;

	@Inject
	private CommonDomainService commonService;

	@Inject
	private PermissionService permissionService;

	@Inject
	private ContextDomainService context;

	@Inject
	private ResourcesScreenQueries queries;

	@Inject
	RestrictionRepository restrictionRepository;

    @Inject
    AuditService auditService;

	public List<ResourceTypeEntity> getAllResourceTypesWithoutChildren() {
		return entityManager.createQuery("select n from ResourceTypeEntity n order by n.name asc").getResultList();
	}

	public List<ResourceTypeEntity> getResourceTypes() {
		return QueryUtils.fetch(ResourceTypeEntity.class, fetchAllResourceTypes(), 0, -1);
	}

	public ResourceTypeEntity getResourceType(Integer resourceTypeId) {
		return entityManager.find(ResourceTypeEntity.class, resourceTypeId);
	}

	protected Query fetchAllResourceTypes() {
		return entityManager
				.createQuery("select n from ResourceTypeEntity n left join fetch n.childrenResourceTypes as c order by n.name asc, c.name asc");
	}

	/**
	 * Fügt einen neuen ResourceType anhand des Namens hinzu
	 * 
	 * @param newResourceTypeName
	 * @throws ElementAlreadyExistsException
	 * @throws ResourceTypeNotFoundException
	 */
	@HasPermission(permission = Permission.RESOURCETYPE, action = Action.CREATE)
	public ResourceType addResourceType(String newResourceTypeName, Integer parentId) throws ElementAlreadyExistsException, ResourceTypeNotFoundException, ValidationException {

		ResourceType result = null;
		ResourceTypeEntity resourceType = getUniqueResourceTypeByName(newResourceTypeName);
		if (resourceType == null) {
			result = ResourceType.createByName(newResourceTypeName, context.getGlobalResourceContextEntity());
			if (parentId != null) {
				ResourceTypeEntity parent = commonService.getResourceTypeEntityById(parentId);
				if (parent != null) {
					result.getEntity().setParentResourceType(parent);
				}
			}
			entityManager.persist(result.getEntity());
			log.info("Resource Type " + newResourceTypeName + " persisted in DB");
		}
		else {
			String message = "Resource Type " + newResourceTypeName
					+ " already exists";
			log.info(message);
			throw new ElementAlreadyExistsException(message, ResourceType.class, newResourceTypeName);
		}
		return result;
	}

	public ResourceTypeEntity getUniqueResourceTypeByName(String resourceTypeName) throws ValidationException {
		ResourceTypeEntity resourceType = null;
		if (StringUtils.isEmpty(resourceTypeName)) {
			throw new ValidationException("Resource type name must not be null or blank");
		}
		try {
			Query searchUniqueResourceTypeQuery = queries.searchResourceTypeByNameCaseInsensitive(resourceTypeName);
			resourceType = (ResourceTypeEntity) searchUniqueResourceTypeQuery.getSingleResult();
		}
		catch (NoResultException nre) {
			String message = "Der ResourceType: " + resourceTypeName + " existiert nicht auf der DB";
			log.info(message);
		}

		return resourceType;
	}

	/**
	 * Löscht den ResourceType anhand der Id
	 * 
	 * @param resourceTypeId
	 * @throws ResourceNotFoundException
	 * @throws ResourceTypeNotFoundException
	 */
	@HasPermission(permission = Permission.RESOURCETYPE, action = Action.DELETE)
	public void removeResourceType(Integer resourceTypeId) throws ResourceNotFoundException, ResourceTypeNotFoundException {
		ResourceTypeEntity resourceTypeEntity = commonService.getResourceTypeEntityById(resourceTypeId);
		restrictionRepository.deleteAllWithResourceType(resourceTypeEntity);
		removeResourceTypeEntity(resourceTypeEntity);
	}

	private void removeResourceTypeEntity(ResourceTypeEntity resourceTypeEntity) throws ResourceNotFoundException {
		if (resourceTypeEntity == null) {
			String message = "Der zu löschende RessourceType ist nicht vorhanden";
			log.info(message);
			throw new ResourceNotFoundException(message);
		}
		if (resourceTypeEntity.getParentResourceType() != null) {
			resourceTypeEntity.getParentResourceType().getChildrenResourceTypes().remove(resourceTypeEntity);
		}
		entityManager.remove(resourceTypeEntity);
		log.info("ResourceType mit der Id: " + resourceTypeEntity.getId() + " wurde aus der DB gelöscht");
	}

	public void createResourceTypeRelation(Integer resTypeId, Integer relResTypeId, String identifier) throws ResourceTypeNotFoundException {
		ResourceTypeEntity resType = commonService.getResourceTypeEntityById(resTypeId);
		permissionService.checkPermissionAndFireException(Permission.RESOURCETYPE, null, Action.UPDATE, null, resType, null);
		ResourceTypeEntity relResType = commonService.getResourceTypeEntityById(relResTypeId);
		ResourceRelationTypeEntity relation = new ResourceRelationTypeEntity();
		relation.setResourceTypes(resType, relResType);
		relation.setIdentifier(identifier);
		log.info("Resourcetype-Relation in DB persistieren");
		entityManager.persist(relation);
	}

	public void removeResourceTypeRelation(Integer relResTypeId) throws ResourceTypeNotFoundException {
		ResourceRelationTypeEntity resRelType = getResourceRelationTypeById(relResTypeId);
		auditService.storeIdInThreadLocalForAuditLog(resRelType);
		permissionService.checkPermissionAndFireException(Permission.RESOURCETYPE, null, Action.UPDATE, null, resRelType.getResourceTypeA(), null);
		permissionService.checkPermissionAndFireException(Permission.RESOURCETYPE, null, Action.UPDATE, null, resRelType.getResourceTypeB(), null);
		resRelType.getResourceTypeA().getResourceRelationTypesA().remove(resRelType);
		resRelType.getResourceTypeB().getResourceRelationTypesB().remove(resRelType);
		entityManager.remove(resRelType);
	}

	public ResourceRelationTypeEntity getResourceRelationTypeById(Integer relResTypeId) {
		ResourceRelationTypeEntity resRelType = entityManager.find(ResourceRelationTypeEntity.class, relResTypeId);
		return resRelType;
	}

    /**
	* find the next free identifier
	*
	* @param identifiers
	* @param prefix
	*             the String before "_"
	* @param count
	*             the count after "_", can be null
	* @return
	*/
    public String nextFreeIdentifier(List<String> identifiers, String prefix, Integer count) {
	   if (identifiers.size() <= 0) {
		  return prefix.toLowerCase();
	   }
	   else if (count == null) {
		  return nextFreeIdentifier(identifiers, prefix, identifiers.size());
	   }
	   else {
		  String sPrefix = !StringUtils.isBlank(prefix) ? prefix.toLowerCase() : "";
		  String nextIdentifier = sPrefix + "_" + String.valueOf(count);

		  if (identifiers.contains(nextIdentifier)) {
			 return nextFreeIdentifier(identifiers, prefix, count + 1);
		  }
		  return nextIdentifier;
	   }
    }
}
