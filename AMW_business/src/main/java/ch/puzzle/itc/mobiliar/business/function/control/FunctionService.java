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

package ch.puzzle.itc.mobiliar.business.function.control;

import ch.puzzle.itc.mobiliar.business.database.entity.MyRevisionEntity;
import ch.puzzle.itc.mobiliar.business.function.boundary.*;
import ch.puzzle.itc.mobiliar.business.function.entity.AmwFunctionEntity;
import ch.puzzle.itc.mobiliar.business.property.entity.MikEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.control.ResourceRepository;
import ch.puzzle.itc.mobiliar.business.resourcegroup.control.ResourceTypeRepository;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceTypeEntity;
import ch.puzzle.itc.mobiliar.business.security.entity.Permission;
import ch.puzzle.itc.mobiliar.business.security.interceptor.HasPermission;
import ch.puzzle.itc.mobiliar.business.template.control.FreemarkerSyntaxValidator;
import ch.puzzle.itc.mobiliar.business.template.entity.RevisionInformation;
import ch.puzzle.itc.mobiliar.business.utils.Identifiable;
import ch.puzzle.itc.mobiliar.common.exception.NotFoundException;
import ch.puzzle.itc.mobiliar.common.exception.ValidationException;
import org.hibernate.Hibernate;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;

import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ch.puzzle.itc.mobiliar.business.security.entity.Action.CREATE;
import static ch.puzzle.itc.mobiliar.business.security.entity.Action.READ;

public class FunctionService implements AddFunctionUseCase, GetFunctionUseCase, ListFunctionsUseCase, GetFunctionRevisionUseCase, ListFunctionRevisionsUseCase {

    @Inject
    FunctionRepository functionRepository;

    @Inject
    ResourceRepository resourceRepository;

    @Inject
    ResourceTypeRepository resourceTypeRepository;

    @Inject
    EntityManager entityManager;

    @Inject
    FreemarkerSyntaxValidator freemarkerValidator;

    /**
     * Returns all (overwritable) functions, which are defined on all parent resource types of the given resource instance - except the functions which are already defined on the given resource instance.
     */
    public List<AmwFunctionEntity> getAllOverwritableSupertypeFunctions(ResourceEntity resourceEntity) {
        Map<String, AmwFunctionEntity> allSuperTypeFunctions = getAllTypeAndSuperTypeFunctions(resourceEntity.getResourceType());

        for (AmwFunctionEntity overwrittenFunction : resourceEntity.getFunctions()) {
            if (allSuperTypeFunctions.containsKey(overwrittenFunction.getName())) {
                allSuperTypeFunctions.remove(overwrittenFunction.getName());
            }
        }

        return new ArrayList<>(allSuperTypeFunctions.values());
    }

    /**
     * Returns all (overwritable) functions, which are defined on the given resourceType instance parent resource types - except the functions which are already defined on the given resourceType instance.
     */
    public List<AmwFunctionEntity> getAllOverwritableSupertypeFunctions(ResourceTypeEntity resourceTypeEntity) {

        Map<String, AmwFunctionEntity> allSuperTypeFunctions = new LinkedHashMap<>();

        if (!resourceTypeEntity.isRootResourceType()) {
            allSuperTypeFunctions = getAllTypeAndSuperTypeFunctions(resourceTypeEntity.getParentResourceType());
        }

        for (AmwFunctionEntity overwrittenFunction : resourceTypeEntity.getFunctions()) {
            if (allSuperTypeFunctions.containsKey(overwrittenFunction.getName())) {
                allSuperTypeFunctions.remove(overwrittenFunction.getName());
            }
        }

        return new ArrayList<>(allSuperTypeFunctions.values());
    }

    private Map<String, AmwFunctionEntity> getAllTypeAndSuperTypeFunctions(ResourceTypeEntity resourceTypeEntity) {
        Map<String, AmwFunctionEntity> superTypeFunctions = new LinkedHashMap<>();
        if (resourceTypeEntity != null) {
            if (!Hibernate.isInitialized(resourceTypeEntity.getFunctions())) {
                resourceTypeEntity = resourceTypeRepository.loadWithFunctionsAndMiksForId(resourceTypeEntity.getId());
            }

            for (AmwFunctionEntity function : resourceTypeEntity.getFunctions()) {
                AmwFunctionEntity functionWithMik = functionRepository.getFunctionByIdWithMiksAndParentChildFunctions(function.getId());
                superTypeFunctions.put(function.getName(), functionWithMik);
            }

            if (!resourceTypeEntity.isRootResourceType()) {
                for (Map.Entry<String, AmwFunctionEntity> entry : getAllTypeAndSuperTypeFunctions(resourceTypeEntity.getParentResourceType()).entrySet()) {
                    if (!superTypeFunctions.containsKey(entry.getKey())) {
                        superTypeFunctions.put(entry.getKey(), entry.getValue());
                    }
                }
            }
        }
        return superTypeFunctions;
    }

    /**
     * Get all relevant functions for the given resource:
     * <ul>
     * <li>All functions of the resource</li>
     * <li>Functions of the parent resourceTypes if not overwritten by the resource itself</li>
     * </ul>
     *
     * @param resource
     * @return a list of AmwFunctions
     */
    public List<AmwFunctionEntity> getAllFunctionsForResource(ResourceEntity resource) {
        Objects.requireNonNull(resource, "Resource Entity must not be null");

        ResourceEntity resourceWithFctAndMiks = resourceRepository.loadWithFunctionsAndMiksForId(resource.getId());
        List<AmwFunctionEntity> allFunctions = new ArrayList<>(resourceWithFctAndMiks.getFunctions());
        allFunctions.addAll(getAllOverwritableSupertypeFunctions(resourceWithFctAndMiks));

        return allFunctions;
    }

    /**
     * Find the function for the given mik
     *
     * @param functions
     * @param mik
     * @return AmwFunctionEntity
     */
    public AmwFunctionEntity getAMWFunctionForMIK(List<AmwFunctionEntity> functions, String mik) {
        for (AmwFunctionEntity function : functions) {
            for (String mikName : function.getMikNames()) {
                if (mikName.equals(mik)) {
                    return function;
                }
            }
        }
        return null;
    }

    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public void saveFunctionWithMiks(AmwFunctionEntity amwFunction, Set<String> functionMikNames) {
        if (amwFunction != null) {
            Set<MikEntity> miks = new HashSet<>();
            for (String mikName : functionMikNames) {
                miks.add(new MikEntity(mikName, amwFunction));
            }

            amwFunction.setMiks(miks);
            functionRepository.persistOrMergeFunction(amwFunction);
        }
    }

    /**
     * Find all functions within the namespace (scope of function name uniques: RootResourceType -> subResourceType -> ResourceInstance) for given name
     */
    public List<AmwFunctionEntity> findFunctionsByNameInNamespace(Identifiable resourceOrType, String name) {

        List<AmwFunctionEntity> allFunctions = new ArrayList<>();

        if (resourceOrType instanceof ResourceEntity) {
            allFunctions.addAll(findAllResourceFunctionsByName((ResourceEntity) resourceOrType, name));
        }
        if (resourceOrType instanceof ResourceTypeEntity) {
            ResourceTypeEntity resourceType = (ResourceTypeEntity) resourceOrType;
            allFunctions.addAll(findFirstFunctionDefinedOnTypeAndSuperResourceTypesWithName(resourceType.getParentResourceType(), name));
            allFunctions.addAll(findAllFunctionsDefinedOnResourceTypeWithName(resourceType, name));
            allFunctions.addAll(findFirstFunctionDefinedOnSubResourceTypesOrResourcesWithName(resourceType, name));
        }

        return allFunctions;
    }

    private List<AmwFunctionEntity> findFirstFunctionDefinedOnTypeAndSuperResourceTypesWithName(ResourceTypeEntity resourceType, String name) {
        List<AmwFunctionEntity> allFunctions = new ArrayList<>();

        Map<String, AmwFunctionEntity> allSuperTypeFunctions = getAllTypeAndSuperTypeFunctions(resourceType);
        if (allSuperTypeFunctions.containsKey(name)) {
            allFunctions.add(allSuperTypeFunctions.get(name));
        }

        return allFunctions;
    }

    private List<AmwFunctionEntity> findAllFunctionsDefinedOnResourceTypeWithName(ResourceTypeEntity resourceType, String name) {
        List<AmwFunctionEntity> allFunctions = new ArrayList<>();

        for (AmwFunctionEntity function : resourceType.getFunctions()) {
            if (function.getName().equalsIgnoreCase(name)) {
                allFunctions.add(function);
            }
        }
        return allFunctions;
    }


    private List<AmwFunctionEntity> findFirstFunctionDefinedOnSubResourceTypesOrResourcesWithName(ResourceTypeEntity resourceType, String name) {
        List<AmwFunctionEntity> allFunctions = new ArrayList<>();

        Map<String, AmwFunctionEntity> allSubTypesAndResourcesFunctions = getAllSubTypeAndResourceFunctions(resourceType);
        if (allSubTypesAndResourcesFunctions.containsKey(name)) {
            allFunctions.add(allSubTypesAndResourcesFunctions.get(name));
        }

        return allFunctions;
    }

    private List<AmwFunctionEntity> findAllResourceFunctionsByName(ResourceEntity resource, String name) {
        List<AmwFunctionEntity> allResourceFunctions = new ArrayList<>();
        for (AmwFunctionEntity function : getAllFunctionsForResource(resource)) {
            if (function.getName().equalsIgnoreCase(name)) {
                allResourceFunctions.add(function);
            }
        }
        return allResourceFunctions;
    }


    private Map<String, AmwFunctionEntity> getAllSubTypeAndResourceFunctions(ResourceTypeEntity resourceTypeEntity) {
        Map<String, AmwFunctionEntity> subTypeFunctions = new LinkedHashMap<>();

        if (!Hibernate.isInitialized(resourceTypeEntity.getResources())) {
            resourceTypeEntity = resourceTypeRepository.loadWithResources(resourceTypeEntity.getId());
        }

        for (ResourceEntity resource : resourceTypeEntity.getResources()) {
            resource = resourceRepository.loadWithFunctionsAndMiksForId(resource.getId());
            for (AmwFunctionEntity function : resource.getFunctions()) {
                subTypeFunctions.put(function.getName(), function);
            }
        }

        for (ResourceTypeEntity subResourceType : resourceTypeEntity.getChildrenResourceTypes()) {
            subResourceType = resourceTypeRepository.loadWithFunctionsAndMiksForId(subResourceType.getId());
            for (AmwFunctionEntity function : subResourceType.getFunctions()) {
                subTypeFunctions.put(function.getName(), function);
            }
            subTypeFunctions.putAll(getAllSubTypeAndResourceFunctions(subResourceType));
        }

        return subTypeFunctions;
    }


    public AmwFunctionEntity overwriteResourceFunction(String functionBody, AmwFunctionEntity functionToOverwrite, ResourceEntity resource) {
        AmwFunctionEntity overwritingFunction = overwriteFunction(functionBody, functionToOverwrite);
        overwritingFunction.setResource(resource);
        return overwritingFunction;
    }

    public AmwFunctionEntity overwriteResourceTypeFunction(String functionBody, AmwFunctionEntity functionToOverwrite, ResourceTypeEntity resourceType) {
        if (resourceType.isRootResourceType() || isAlreadyOverwrittenInResourceType(functionToOverwrite, resourceType)) {
            throw new RuntimeException("Can not overwrite resource type function!");
        }
        AmwFunctionEntity overwritingFunction = null;
        if (functionToOverwrite.isOverwrittenBySubTypeOrResourceFunction()) {
            for (AmwFunctionEntity oldOverwritingFunction : functionToOverwrite.getOverwritingChildFunction()) {
                if (isOverwrittenInSubTypeOrResource(resourceType, oldOverwritingFunction) && hasSameParentResourceType(resourceType, oldOverwritingFunction)) {
                    overwritingFunction = replaceOverwriting(functionBody, functionToOverwrite, oldOverwritingFunction);
                    break;
                }
            }

        }
        if (overwritingFunction == null) {
            overwritingFunction = overwriteFunction(functionBody, functionToOverwrite);
        }

        overwritingFunction.setResourceType(resourceType);
        return overwritingFunction;
    }

    private boolean isAlreadyOverwrittenInResourceType(AmwFunctionEntity functionToOverwrite, ResourceTypeEntity resourceType) {
        for (AmwFunctionEntity overwritingFunction : functionToOverwrite.getOverwritingChildFunction()) {
            if (overwritingFunction.isDefinedOnResourceType() && overwritingFunction.getResourceType().equals(resourceType)) {
                return true;
            }
        }
        return false;
    }

    private boolean isOverwrittenInSubTypeOrResource(ResourceTypeEntity resourceType, AmwFunctionEntity oldOverwritingFunction) {
        boolean isSubType = false;

        List<ResourceTypeEntity> allSubResourceTypes = getAllSubResourceTypes(resourceType);

        if (oldOverwritingFunction.isDefinedOnResourceType()) {
            isSubType = allSubResourceTypes.contains(oldOverwritingFunction.getResourceType());
        }
        if (oldOverwritingFunction.isDefinedOnResource()) {
            ResourceTypeEntity resourceTypeOfFunctionResource = oldOverwritingFunction.getResource().getResourceType();
            isSubType = allSubResourceTypes.contains(resourceTypeOfFunctionResource) || resourceType.equals(resourceTypeOfFunctionResource);
        }

        return isSubType;
    }

    private List<ResourceTypeEntity> getAllParentResourceTypes(ResourceTypeEntity resourceType) {
        List<ResourceTypeEntity> allParentResourceTypes = new ArrayList<>();

        if (!resourceType.isRootResourceType()) {
            allParentResourceTypes.add(resourceType.getParentResourceType());
            allParentResourceTypes.addAll(getAllParentResourceTypes(resourceType.getParentResourceType()));
        }

        return allParentResourceTypes;
    }

    private List<ResourceTypeEntity> getAllSubResourceTypes(ResourceTypeEntity resourceType) {
        List<ResourceTypeEntity> allSubResourceTypes = new ArrayList<>();

        for (ResourceTypeEntity subResourceType : resourceType.getChildrenResourceTypes()) {
            allSubResourceTypes.add(subResourceType);
            allSubResourceTypes.addAll(getAllSubResourceTypes(subResourceType));
        }

        return allSubResourceTypes;
    }

    /**
     * Removes old overwriting references from and to overwritten function and create a new intermediate overwriting function which holds the references
     */
    private AmwFunctionEntity replaceOverwriting(String functionBody, AmwFunctionEntity functionToOverwrite, AmwFunctionEntity oldOverwritingFunction) {
        AmwFunctionEntity overwritingFunction;// remove old overwriting reference
        oldOverwritingFunction.resetOverwriting();

        // create new overwriting function
        overwritingFunction = overwriteFunction(functionBody, functionToOverwrite);

        // set overwrining references
        oldOverwritingFunction.overwrite(overwritingFunction);

        return overwritingFunction;
    }

    private boolean hasSameParentResourceType(ResourceTypeEntity resourceType, AmwFunctionEntity oldOverwritingFunction) {
        return oldOverwritingFunction.getOverwrittenParent().isDefinedOnResourceType()
                && getAllParentResourceTypes(resourceType).contains(oldOverwritingFunction.getOverwrittenParent().getResourceType());
    }


    private AmwFunctionEntity overwriteFunction(String functionBody, AmwFunctionEntity functionToOverwrite) {
        AmwFunctionEntity overwritingFunction = new AmwFunctionEntity();
        overwritingFunction.setName(functionToOverwrite.getName());
        overwritingFunction.setImplementation(functionBody);
        overwritingFunction.overwrite(functionToOverwrite);
        saveFunctionWithMiks(overwritingFunction, functionToOverwrite.getMikNames());
        return overwritingFunction;
    }

    /**
     * Delete function and removes dependencies in overridden parent functions
     */
    public void deleteFunction(AmwFunctionEntity functionToDelete) {
        if (functionToDelete.isOverwritingResourceTypeFunction()) {
            functionToDelete.resetOverwriting();
        }

        functionRepository.remove(functionToDelete);
    }

    @Override
    @HasPermission(permission = Permission.RESOURCE_AMWFUNCTION, action = READ)
    public AmwFunctionEntity get(Integer id) throws NotFoundException {
        AmwFunctionEntity entity = functionRepository.getFunctionByIdWithMiksAndParentChildFunctions(id);
        if (entity != null) {
            return entity;
        } else {
            throw new NotFoundException("Function not found.");
        }
    }

    @Override
    @HasPermission(permission = Permission.RESOURCE_AMWFUNCTION, action = READ)
    public List<AmwFunctionEntity> functionsForResource(Integer id) throws NotFoundException {

        ResourceEntity resourceEntity = resourceRepository.loadWithFunctionsAndMiksForId(id);
        if (resourceEntity != null) {
            List<AmwFunctionEntity> instanceFuncs = new ArrayList<>(resourceEntity.getFunctions());
            List<AmwFunctionEntity> superFuncs = getAllOverwritableSupertypeFunctions(resourceEntity);
            return Stream.of(instanceFuncs, superFuncs).flatMap(Collection::stream).collect(Collectors.toList());
        } else {
            throw new NotFoundException("Resource not found.");
        }
    }

    @Override
    @HasPermission(permission = Permission.RESOURCE_AMWFUNCTION, action = READ)
    public List<AmwFunctionEntity> functionsForResourceType(Integer id) throws NotFoundException {
        ResourceTypeEntity resourceTypeEntity = resourceTypeRepository.loadWithFunctionsAndMiksForId(id);
        if (resourceTypeEntity != null) {
            List<AmwFunctionEntity> instanceFuncs = new ArrayList<>(resourceTypeEntity.getFunctions());
            List<AmwFunctionEntity> superFuncs = getAllOverwritableSupertypeFunctions(resourceTypeEntity);

            return Stream.of(instanceFuncs, superFuncs).flatMap(Collection::stream).collect(Collectors.toList());
        } else {
            throw new NotFoundException("Resource not found.");
        }
    }


    @Override
    @HasPermission(permission = Permission.RESOURCE_AMWFUNCTION, action = READ)
    public AmwFunctionEntity getFunctionRevision(int functionId, int revisionId) throws NotFoundException {
        AmwFunctionEntity function = AuditReaderFactory.get(entityManager).find(
                AmwFunctionEntity.class, functionId, revisionId);
        if (function == null) {
            throw new NotFoundException("No function with id " + functionId + " and revision id " + revisionId + " found");
        }
        return function;
    }

    @Override
    @HasPermission(permission = Permission.RESOURCE_AMWFUNCTION, action = READ)
    public List<RevisionInformation> getRevisions(Integer functionId) {
        List<RevisionInformation> result = new ArrayList<>();
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

    @Override
    @HasPermission(permission = Permission.RESOURCE_AMWFUNCTION, action = CREATE)
    public Integer addForResource(AddFunctionCommand addFunctionCommand) throws NotFoundException, ValidationException {
        ResourceEntity resource = resourceRepository.find((addFunctionCommand.getResourceId()));
        if (resource == null) throw new NotFoundException("Resource not found.");

        if (!findFunctionsByNameInNamespace(resource, addFunctionCommand.getName()).isEmpty()) {
            throw new NotFoundException("Function with name " + addFunctionCommand.getName() + " already exists.");
        }
        AmwFunctionEntity amwFunctionEntity = new AmwFunctionEntity();
        try {
            amwFunctionEntity.setName(addFunctionCommand.getName());
            amwFunctionEntity.setResource(resource);
            amwFunctionEntity.setImplementation(addFunctionCommand.getContent());
            freemarkerValidator.validateFreemarkerSyntax(addFunctionCommand.getContent());
            saveFunctionWithMiks(amwFunctionEntity, addFunctionCommand.getMiks());
        } catch (Exception e) {
            throw new ValidationException(e.getMessage());
        }
        return amwFunctionEntity.getId();
    }


    @Override
    @HasPermission(permission = Permission.RESOURCETYPE_AMWFUNCTION, action = CREATE)
    public Integer addForResourceType(AddFunctionCommand addFunctionCommand) throws IllegalStateException, NotFoundException, ValidationException {
        ResourceTypeEntity resourceType = resourceTypeRepository.loadWithFunctionsAndMiksForId(addFunctionCommand.getResourceId());
        if (resourceType == null) throw new NotFoundException("ResourceType not found.");

        if (!findFunctionsByNameInNamespace(resourceType, addFunctionCommand.getName()).isEmpty()) {
            throw new NotFoundException("Function with name " + addFunctionCommand.getName() + " already exists.");
        }
        AmwFunctionEntity amwFunctionEntity = new AmwFunctionEntity();
        try {
            amwFunctionEntity.setName(addFunctionCommand.getName());
            amwFunctionEntity.setResourceType(resourceType);
            amwFunctionEntity.setImplementation(addFunctionCommand.getContent());
            freemarkerValidator.validateFreemarkerSyntax(addFunctionCommand.getContent());
            saveFunctionWithMiks(amwFunctionEntity, addFunctionCommand.getMiks());
        } catch (Exception e) {
            throw new ValidationException(e.getMessage());
        }
        return amwFunctionEntity.getId();
    }
}
