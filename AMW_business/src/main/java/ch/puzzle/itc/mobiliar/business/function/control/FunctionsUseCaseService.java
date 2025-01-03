package ch.puzzle.itc.mobiliar.business.function.control;

import ch.puzzle.itc.mobiliar.business.function.boundary.*;
import ch.puzzle.itc.mobiliar.business.function.entity.AmwFunctionEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.control.ResourceRepository;
import ch.puzzle.itc.mobiliar.business.resourcegroup.control.ResourceTypeRepository;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceGroupEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceTypeEntity;
import ch.puzzle.itc.mobiliar.business.security.boundary.PermissionBoundary;
import ch.puzzle.itc.mobiliar.business.security.entity.Action;
import ch.puzzle.itc.mobiliar.business.security.entity.Permission;
import ch.puzzle.itc.mobiliar.business.security.interceptor.HasPermission;
import ch.puzzle.itc.mobiliar.business.template.control.FreemarkerSyntaxValidator;
import ch.puzzle.itc.mobiliar.business.template.entity.RevisionInformation;
import ch.puzzle.itc.mobiliar.common.exception.NotFoundException;
import ch.puzzle.itc.mobiliar.common.exception.ValidationException;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

import static ch.puzzle.itc.mobiliar.business.security.entity.Action.*;

@Stateless
public class FunctionsUseCaseService implements
        AddFunctionUseCase,
        GetFunctionRevisionUseCase,
        GetFunctionUseCase,
        ListFunctionRevisionsUseCase,
        ListFunctionsUseCase,
        OverwriteFunctionUseCase,
        UpdateFunctionUseCase,
        DeleteFunctionUseCase {

    @Inject
    FunctionService functionService;

    @Inject
    FunctionRepository functionRepository;

    @Inject
    ResourceRepository resourceRepository;

    @Inject
    ResourceTypeRepository resourceTypeRepository;

    @Inject
    FreemarkerSyntaxValidator freemarkerValidator;

    @Inject
    PermissionBoundary permissionBoundary;

    @Override
    @HasPermission(permission = Permission.RESOURCE_AMWFUNCTION, action = CREATE)
    public Integer addForResource(AddFunctionCommand addFunctionCommand) throws IllegalStateException, NotFoundException, ValidationException {
        ResourceEntity resource = resourceRepository.find((addFunctionCommand.getResourceId()));
        if (resource == null) throw new NotFoundException("Resource not found.");

        if (!functionService.findFunctionsByNameInNamespace(resource, addFunctionCommand.getName()).isEmpty()) {
            throw new NotFoundException("Function with name " + addFunctionCommand.getName() + " already exists.");
        }
        AmwFunctionEntity amwFunctionEntity = new AmwFunctionEntity();
        try {
            amwFunctionEntity.setName(addFunctionCommand.getName());
            amwFunctionEntity.setResource(resource);
            amwFunctionEntity.setImplementation(addFunctionCommand.getContent());
            freemarkerValidator.validateFreemarkerSyntax(addFunctionCommand.getContent());
            functionService.saveFunctionWithMiks(amwFunctionEntity, addFunctionCommand.getMiks());
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

        if (!functionService.findFunctionsByNameInNamespace(resourceType, addFunctionCommand.getName()).isEmpty()) {
            throw new NotFoundException("Function with name " + addFunctionCommand.getName() + " already exists.");
        }
        AmwFunctionEntity amwFunctionEntity = new AmwFunctionEntity();
        try {
            amwFunctionEntity.setName(addFunctionCommand.getName());
            amwFunctionEntity.setResourceType(resourceType);
            amwFunctionEntity.setImplementation(addFunctionCommand.getContent());
            freemarkerValidator.validateFreemarkerSyntax(addFunctionCommand.getContent());
            functionService.saveFunctionWithMiks(amwFunctionEntity, addFunctionCommand.getMiks());
        } catch (Exception e) {
            throw new ValidationException(e.getMessage());
        }
        return amwFunctionEntity.getId();
    }

    @Override
    @HasPermission(permission = Permission.RESOURCE_AMWFUNCTION, action = READ)
    public AmwFunctionEntity getFunctionRevision(int id, int revisionId) throws NotFoundException {
        return functionService.getFunctionRevision(id, revisionId);
    }

    @Override
    @HasPermission(permission = Permission.RESOURCE_AMWFUNCTION, action = READ)
    public AmwFunctionEntity getFunction(Integer id) throws NotFoundException {
        AmwFunctionEntity entity = functionRepository.getFunctionByIdWithMiksAndParentChildFunctions(id);
        if (entity != null) {
            return entity;
        } else {
            throw new NotFoundException("Function not found.");
        }
    }

    @Override
    @HasPermission(permission = Permission.RESOURCE_AMWFUNCTION, action = READ)
    public List<RevisionInformation> getRevisions(Integer functionId) {
        return functionService.getRevisions(functionId);
    }

    @Override
    @HasPermission(permission = Permission.RESOURCE_AMWFUNCTION, action = READ)
    public List<AmwFunctionEntity> functionsForResource(Integer id) throws NotFoundException {
        ResourceEntity resourceEntity = resourceRepository.getResourceByIdWithRelations(id);
        if (resourceEntity != null) {
            return functionService.getAllFunctionsForResource(resourceEntity);
        } else {
            throw new NotFoundException("Resource not found.");
        }
    }

    @Override
    @HasPermission(permission = Permission.RESOURCE_AMWFUNCTION, action = READ)
    public List<AmwFunctionEntity> functionsForResourceType(Integer id) throws NotFoundException {
        ResourceTypeEntity resourceTypeEntity = resourceTypeRepository.loadWithFunctionsAndMiksForId(id);
        if (resourceTypeEntity != null) {
            List<AmwFunctionEntity> allFunctions = new ArrayList<>();
            allFunctions.addAll(resourceTypeEntity.getFunctions());
            allFunctions.addAll(functionService.getAllOverwritableSupertypeFunctions(resourceTypeEntity));
            return allFunctions;
        } else {
            throw new NotFoundException("Resource not found.");
        }
    }

    @Override
    @HasPermission(permission = Permission.RESOURCE_AMWFUNCTION, action = UPDATE)
    public int overwriteForResource(OverwriteFunctionCommand overwriteFunctionCommand) throws NotFoundException, ValidationException {
        AmwFunctionEntity functionToOverwrite = functionRepository.getFunctionByIdWithChildFunctions(overwriteFunctionCommand.getId());
        ResourceEntity resource = resourceRepository.find(overwriteFunctionCommand.getResourceId());
        if (functionToOverwrite == null || resource == null) {
            throw new NotFoundException("Function or Resource not found");
        }
        try {
            freemarkerValidator.validateFreemarkerSyntax(overwriteFunctionCommand.getContent());
            AmwFunctionEntity overwritingFunction = functionService.overwriteResourceFunction(
                    overwriteFunctionCommand.getContent(), functionToOverwrite, resource);
            return overwritingFunction.getId();
        } catch (Exception e) {
            throw new ValidationException(e.getMessage());
        }
    }

    @Override
    @HasPermission(permission = Permission.RESOURCETYPE_AMWFUNCTION, action = UPDATE)
    public int overwriteForResourceType(OverwriteFunctionCommand overwriteFunctionCommand) throws IllegalStateException, NotFoundException, ValidationException {
        AmwFunctionEntity functionToOverwrite = functionRepository.getFunctionByIdWithChildFunctions(overwriteFunctionCommand.getId());
        ResourceTypeEntity resourceType = resourceTypeRepository.find(overwriteFunctionCommand.getResourceId());
        if (functionToOverwrite == null || resourceType == null) {
            throw new NotFoundException("Function or ResourceType not found");
        }
        try {
            freemarkerValidator.validateFreemarkerSyntax(overwriteFunctionCommand.getContent());
            AmwFunctionEntity overwritingFunction = functionService.overwriteResourceTypeFunction(
                    overwriteFunctionCommand.getContent(), functionToOverwrite, resourceType);
            return overwritingFunction.getId();
        } catch (Exception e) {
            throw new ValidationException(e.getMessage());
        }
    }

    @Override
    @HasPermission(permission = Permission.RESOURCETYPE_AMWFUNCTION, action = UPDATE)
    public int update(UpdateFunctionCommand updateFunctionCommand) throws IllegalStateException, NotFoundException, ValidationException {
        AmwFunctionEntity amwFunctionEntity = getFunction(updateFunctionCommand.getId());
        try {
            amwFunctionEntity.setImplementation(updateFunctionCommand.getContent());
            freemarkerValidator.validateFreemarkerSyntax(updateFunctionCommand.getContent());
            functionRepository.persistOrMergeFunction(amwFunctionEntity);
            return amwFunctionEntity.getId();
        } catch (Exception e) {
            throw new ValidationException(e.getMessage());
        }
    }

    @Override
    public void deleteFunction(Integer id) throws ValidationException, NotFoundException {
        AmwFunctionEntity functionToDelete = functionRepository.find(id);
        if (functionToDelete == null) {
            throw new NotFoundException("No function entity found for id " + id);
        }

        if (functionToDelete.getResource() == null && functionToDelete.getResourceType() != null) {
            permissionBoundary.checkPermissionAndFireException(Permission.RESOURCETYPE_AMWFUNCTION, null, Action.DELETE,
                    null, functionToDelete.getResourceType(), "missing Permission to delete ResourceType functions");
        } else {
            ResourceGroupEntity group = functionToDelete.getResource() != null ? functionToDelete.getResource().getResourceGroup() : null;
            permissionBoundary.checkPermissionAndFireException(Permission.RESOURCE_AMWFUNCTION, null, Action.DELETE,
                    group, null,
                    "missing Permission to delete Resource functions");
        }

        if (!functionToDelete.isOverwrittenBySubTypeOrResourceFunction()) {
            functionService.deleteFunction(functionToDelete);
        } else {
            throw new ValidationException("Can not delete function because it is overwritten by at least one sub resource type or resource function", functionToDelete.getOverwritingChildFunction().iterator().next());
        }
    }


}
