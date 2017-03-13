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

package ch.puzzle.itc.mobiliar.business.function.boundary;

import java.util.*;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.interceptor.Interceptors;
import javax.persistence.EntityManager;

import ch.puzzle.itc.mobiliar.business.database.entity.MyRevisionEntity;
import ch.puzzle.itc.mobiliar.business.function.control.FunctionRepository;
import ch.puzzle.itc.mobiliar.business.function.control.FunctionService;
import ch.puzzle.itc.mobiliar.business.function.entity.AmwFunctionEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.control.ResourceRepository;
import ch.puzzle.itc.mobiliar.business.resourcegroup.control.ResourceTypeRepository;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceTypeEntity;
import ch.puzzle.itc.mobiliar.business.security.boundary.PermissionBoundary;
import ch.puzzle.itc.mobiliar.business.security.entity.Permission;
import ch.puzzle.itc.mobiliar.business.security.interceptor.HasPermission;
import ch.puzzle.itc.mobiliar.business.security.interceptor.HasPermissionInterceptor;
import ch.puzzle.itc.mobiliar.business.template.control.FreemarkerSyntaxValidator;
import ch.puzzle.itc.mobiliar.business.template.entity.RevisionInformation;
import ch.puzzle.itc.mobiliar.business.utils.ValidationException;
import ch.puzzle.itc.mobiliar.common.exception.AMWException;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;

@Stateless
@Interceptors(HasPermissionInterceptor.class)
@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
public class FunctionsBoundary {

	@Inject
	EntityManager entityManager;

	@Inject
    PermissionBoundary permissionBoundary;

	@Inject
	ResourceRepository resourceRepository;

	@Inject
	ResourceTypeRepository resourceTypeRepository;

	@Inject
	FunctionService functionService;

	@Inject
	FunctionRepository functionRepository;

	@Inject
	FreemarkerSyntaxValidator freemarkerValidator;

	public AmwFunctionEntity getFunctionById(Integer functionId) {
		if (functionId != null) {
			return functionRepository.getFunctionByIdWithMiksAndParentChildFunctions(functionId);
		}
		return null;
	}

	/**
	 * Get all functions which are defined on the given resource
	 */
	public List<AmwFunctionEntity> getInstanceFunctions(ResourceEntity resource) {
		Objects.requireNonNull(resource, "Resource Entity must not be null");
		ResourceEntity resourceEntity = resourceRepository.loadWithFunctionsAndMiksForId(resource.getId());
		return new ArrayList<>(resourceEntity.getFunctions());
	}

	/**
	 * Get all functions which are defined on the given resourceType
	 */
	public List<AmwFunctionEntity> getInstanceFunctions(ResourceTypeEntity resourceType) {
		Objects.requireNonNull(resourceType, "Resource Type Entity must not be null");
		ResourceTypeEntity resourceTypeEntity = resourceTypeRepository.loadWithFunctionsAndMiksForId(resourceType.getId());
		return new ArrayList<>(resourceTypeEntity.getFunctions());
	}

	/**
	 * Returns all (overwritable) functions, which are defined on all parent resource types of the given
	 * resource instance - except the functions which are already defined on the given resource instance.
	 */
	public List<AmwFunctionEntity> getAllOverwritableSupertypeFunctions(ResourceEntity resource) {
		Objects.requireNonNull(resource, "Resource Entity must not be null");
		ResourceEntity resourceEntity = resourceRepository.loadWithFunctionsAndMiksForId(resource.getId());

		return functionService.getAllOverwritableSupertypeFunctions(resourceEntity);
	}

	/**
	 * Returns all (overwritable) functions, which are defined on the given resourceType instance parent
	 * resource types - except the functions which are already defined on the given resourceType instance.
	 */
	public List<AmwFunctionEntity> getAllOverwritableSupertypeFunctions(ResourceTypeEntity resourceType) {
		Objects.requireNonNull(resourceType, "Resource Type Entity must not be null");
		ResourceTypeEntity resourceTypeEntity = resourceTypeRepository.loadWithFunctionsAndMiksForId(resourceType.getId());

		return functionService.getAllOverwritableSupertypeFunctions(resourceTypeEntity);

	}

	/**
	 * Returns a AmwFunctionEntity identified by its id and revision id
	 */
	public AmwFunctionEntity getFunctionByIdAndRevision(Integer functionId, Number revisionId) {
		AmwFunctionEntity amwFunctionEntity = AuditReaderFactory.get(entityManager).find(
				AmwFunctionEntity.class, functionId, revisionId);
		return amwFunctionEntity;
	}

	/**
	 * Returns all RevisionInformation for the specified function id
	 */
	public List<RevisionInformation> getFunctionRevisions(Integer functionId) {
		List<RevisionInformation> result = new ArrayList<RevisionInformation>();
		if (functionId != null) {
			AuditReader reader = AuditReaderFactory.get(entityManager);
			List<Number> list = reader.getRevisions(AmwFunctionEntity.class, functionId);
			for (Number rev : list) {
				Date date = reader.getRevisionDate(rev);
				MyRevisionEntity myRev = entityManager.find(MyRevisionEntity.class, rev);
				result.add(new RevisionInformation(rev, date, myRev.getUsername()));
			}
			Collections.sort(result);
		}
		return result;
	}

	@HasPermission(permission = Permission.MANAGE_AMW_APP_INSTANCE_FUNCTIONS)
	public void deleteFunction(Integer selectedFunctionIdToBeRemoved) throws ValidationException {
		Objects.requireNonNull(selectedFunctionIdToBeRemoved, "Resource Type Entity must not be null");
		AmwFunctionEntity functionToDelete = functionRepository.find(selectedFunctionIdToBeRemoved);

		if(functionToDelete.getResource() == null && functionToDelete.getResourceType() != null){
			permissionBoundary.checkPermissionAndFireException(Permission.MANAGE_AMW_FUNCTIONS, "missing Permission to delete ResourceType functions");
		}

		if (functionToDelete != null) {
			if (!functionToDelete.isOverwrittenBySubTypeOrResourceFunction()) {
                functionService.deleteFunction(functionToDelete);
			} else {
                throw new ValidationException("Can not delete function because it is overwrittten by at least one sub resource type or resource function", functionToDelete.getOverwritingChildFunction().iterator().next());
            }
		}
		else {
			throw new RuntimeException("No function entity found for id " + selectedFunctionIdToBeRemoved);
		}
	}

	/**
	 * Creates a new Function for a Resource with miks
	 */
	@HasPermission(oneOfPermission = Permission.MANAGE_AMW_APP_INSTANCE_FUNCTIONS)
	public AmwFunctionEntity createNewResourceFunction(AmwFunctionEntity amwFunction, Integer resourceId, Set<String> functionMikNames) throws ValidationException, AMWException {
		ResourceEntity resource = resourceRepository.find(resourceId);

		// search for already existing functions with this name on functiontree
		List<AmwFunctionEntity> allFunctionsWithName = functionService.findFunctionsByNameInNamespace(resource, amwFunction.getName());
		if (allFunctionsWithName.isEmpty()) {
			amwFunction.setResource(resource);
            freemarkerValidator.validateFreemarkerSyntax(amwFunction.getDecoratedImplementation());
			functionService.saveFunctionWithMiks(amwFunction, functionMikNames);
		}
		else {
			throw new ValidationException("Function name already in use", allFunctionsWithName.get(0));
		}
        return amwFunction;
    }

	/**
	 * Creates a new Function for a ResourceType with miks
	 */
	@HasPermission(permission = Permission.MANAGE_AMW_FUNCTIONS)
	public AmwFunctionEntity createNewResourceTypeFunction(AmwFunctionEntity amwFunction, Integer resourceTypeId, Set<String> functionMikNames) throws ValidationException, AMWException {
        ResourceTypeEntity resourceType = resourceTypeRepository.find(resourceTypeId);

		// search for already existing functions with this name on functiontree
        List<AmwFunctionEntity> allFunctionsWithName = functionService.findFunctionsByNameInNamespace(resourceType, amwFunction.getName());
        if (allFunctionsWithName.isEmpty()) {
			amwFunction.setResourceType(resourceType);
            freemarkerValidator.validateFreemarkerSyntax(amwFunction.getDecoratedImplementation());
			functionService.saveFunctionWithMiks(amwFunction, functionMikNames);
		}
		else {
            throw new ValidationException("Function name already in use", allFunctionsWithName.get(0));
		}
        return amwFunction;
    }

	@HasPermission(permission = Permission.MANAGE_AMW_APP_INSTANCE_FUNCTIONS)
	public void saveFunction(AmwFunctionEntity amwFunction) throws AMWException {
		if(amwFunction.getResource() == null && amwFunction.getResourceType() != null){
			permissionBoundary.checkPermissionAndFireException(Permission.MANAGE_AMW_FUNCTIONS, "missing Permission to save ResourceType functions");
		}
		freemarkerValidator.validateFreemarkerSyntax(amwFunction.getDecoratedImplementation());
		functionRepository.persistOrMergeFunction(amwFunction);
	}

    @HasPermission(permission = Permission.MANAGE_AMW_APP_INSTANCE_FUNCTIONS)
    public AmwFunctionEntity overwriteResourceFunction(String functionBody, Integer resourceId, Integer functionToOverwriteId) throws AMWException {
        AmwFunctionEntity functionToOverwrite = functionRepository.getFunctionByIdWithChildFunctions(functionToOverwriteId);
        ResourceEntity resource = resourceRepository.find(resourceId);
        if (functionToOverwrite != null && resource != null) {
			AmwFunctionEntity overwritingFunction = functionService.overwriteResourceFunction(functionBody, functionToOverwrite, resource);
            freemarkerValidator.validateFreemarkerSyntax(overwritingFunction.getDecoratedImplementation());
			return overwritingFunction;
        }
        else {
            throw new RuntimeException("No function or resource type found");
        }
    }

    @HasPermission(permission = Permission.MANAGE_AMW_FUNCTIONS)
    public AmwFunctionEntity overwriteResourceTypeFunction(String functionBody, Integer resourceTypeId, Integer functionToOverwriteId) throws AMWException {
        AmwFunctionEntity functionToOverwrite = functionRepository.getFunctionByIdWithChildFunctions(functionToOverwriteId);
        ResourceTypeEntity resourceType = resourceTypeRepository.find(resourceTypeId);

        if (functionToOverwrite != null && resourceType != null) {
			AmwFunctionEntity overwritingFunction =  functionService.overwriteResourceTypeFunction(functionBody, functionToOverwrite, resourceType);
            freemarkerValidator.validateFreemarkerSyntax(overwritingFunction.getDecoratedImplementation());
			return overwritingFunction;

        } else {
            throw new RuntimeException("No function or resource found");
        }
    }
}
