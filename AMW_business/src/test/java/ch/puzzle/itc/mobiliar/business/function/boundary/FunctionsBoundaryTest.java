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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ch.puzzle.itc.mobiliar.builders.ResourceEntityBuilder;
import ch.puzzle.itc.mobiliar.builders.ResourceTypeEntityBuilder;
import ch.puzzle.itc.mobiliar.business.function.control.FunctionRepository;
import ch.puzzle.itc.mobiliar.business.function.control.FunctionService;
import ch.puzzle.itc.mobiliar.business.function.entity.AmwFunctionEntity;
import ch.puzzle.itc.mobiliar.business.function.entity.AmwFunctionEntityBuilder;
import ch.puzzle.itc.mobiliar.business.resourcegroup.control.ResourceRepository;
import ch.puzzle.itc.mobiliar.business.resourcegroup.control.ResourceTypeRepository;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceTypeEntity;
import ch.puzzle.itc.mobiliar.business.security.boundary.PermissionBoundary;
import ch.puzzle.itc.mobiliar.business.template.control.FreemarkerSyntaxValidator;
import ch.puzzle.itc.mobiliar.common.exception.AMWException;
import ch.puzzle.itc.mobiliar.common.exception.NotFoundException;
import ch.puzzle.itc.mobiliar.common.exception.ValidationException;

@ExtendWith(MockitoExtension.class)
public class FunctionsBoundaryTest {

    private static final Integer ID_1 = 1;
    private static final Integer ID_2 = 2;
    private static final Integer ID_3 = 3;
    private static final Integer ID_4 = 4;
    private static final String FUNCTION_NAME = "functionName";

    private final ResourceTypeEntity resourceType = createResourceType("ResourceType", ID_1);
    private final ResourceEntity resource = createResource("resourceName", ID_2);
    private final AmwFunctionEntity functionA_ID_3 = createFunction(FUNCTION_NAME, ID_3);
    private final AmwFunctionEntity functionA_ID_4 = createFunction(FUNCTION_NAME, ID_4);

    private static final Set<String> MIKS = new HashSet<>();

    @Mock
    ResourceRepository resourceRepositoryMock;

    @Mock
    ResourceTypeRepository resourceTypeRepositoryMock;

    @Mock
    PermissionBoundary permissionBoundaryMock;

    @Mock
    FunctionService functionServiceMock;

    @Mock
    FunctionRepository functionRepositoryMock;

    @Mock
    FreemarkerSyntaxValidator freemarkerValidatorMock;

    @InjectMocks
    private FunctionsBoundary functionsBoundary;

    @Test
    public void getInstanceFunctionsForResourceShouldDelegateResultFromRepository() {
        // given
        ResourceEntity resource = createResource("resourceName", 1, functionA_ID_3);
        when(resourceRepositoryMock.loadWithFunctionsAndMiksForId(resource.getId())).thenReturn(resource);

        // when
        List<AmwFunctionEntity> resourceFunctions = functionsBoundary.getInstanceFunctions(resource);

        // then
        assertTrue(resourceFunctions.contains(functionA_ID_3));
    }

    @Test
    public void getInstanceFunctionsForResourceNullShouldThrowException() {
        // given
        ResourceEntity resource = null;

        // when
        assertThrows(NullPointerException.class, () -> {
            functionsBoundary.getInstanceFunctions(resource);
        });
    }

    @Test
    public void getInstanceFunctionsForResourceTypeNullShouldThrowException() {
        // given
        ResourceTypeEntity resource = null;

        // when
        assertThrows(NullPointerException.class, () -> {
            functionsBoundary.getInstanceFunctions(resource);
        });
    }

    @Test
    public void getInstanceFunctionsForResourceTypeShouldDelegateResultFromRepository() throws NullPointerException {
        // given
        ResourceTypeEntity resourceType = createResourceType("ResourceType", 1, functionA_ID_3);
        when(resourceTypeRepositoryMock.loadWithFunctionsAndMiksForId(resourceType.getId())).thenReturn(resourceType);

        // when
        List<AmwFunctionEntity> instanceFunctions = functionsBoundary.getInstanceFunctions(resourceType);

        // then
        assertTrue(instanceFunctions.contains(functionA_ID_3));
    }

    @Test
    public void getAllOverwritableSupertypeFunctionsForResourceShouldDelegateToService() {
        // given
        ResourceEntity resource = createResource("resourceName", 1);
        when(resourceRepositoryMock.loadWithFunctionsAndMiksForId(resource.getId())).thenReturn(resource);

        // when
        functionsBoundary.getAllOverwritableSupertypeFunctions(resource);

        // then
        verify(functionServiceMock).getAllOverwritableSupertypeFunctions(resource);
    }

    @Test
    public void getAllOverwritableSupertypeFunctionsForResourceNullShouldThrowException() {
        // given
        ResourceEntity resource = null;

        // when
        assertThrows(NullPointerException.class, () -> {
            functionsBoundary.getAllOverwritableSupertypeFunctions(resource);
        });
    }

    @Test
    public void getAllOverwritableSupertypeFunctionsForResourceTypeShouldDelegateToService() {
        // given
        ResourceTypeEntity rootResourceType = createResourceType("ResourceType", 1);
        when(resourceTypeRepositoryMock.loadWithFunctionsAndMiksForId(rootResourceType.getId()))
                .thenReturn(rootResourceType);

        // when
        functionsBoundary.getAllOverwritableSupertypeFunctions(rootResourceType);

        // then
        verify(functionServiceMock).getAllOverwritableSupertypeFunctions(rootResourceType);
    }

    @Test
    public void getAllOverwritableSupertypeFunctionsForResourceTypeNullShouldThrowException() {
        // given
        ResourceTypeEntity resourceType = null;

        // when
        assertThrows(NullPointerException.class, () -> {
            functionsBoundary.getAllOverwritableSupertypeFunctions(resourceType);
        });
    }

    @Test
    public void removeWhenIdIsNullShouldThrowException() throws ValidationException, NotFoundException {
        // given
        Integer selectedFunctionIdToBeRemoved = null;

        // when
        assertThrows(NullPointerException.class, () -> {
            functionsBoundary.deleteFunction(selectedFunctionIdToBeRemoved);
        });
    }

    @Test
    public void removeWhenFunctionForIdIsNullShouldThrowException() throws ValidationException, NotFoundException {
        // given
        Integer selectedFunctionIdToBeRemoved = 1;
        when(functionRepositoryMock.find(selectedFunctionIdToBeRemoved)).thenReturn(null);

        // when
        assertThrows(NotFoundException.class, () -> {
            functionsBoundary.deleteFunction(selectedFunctionIdToBeRemoved);
        });
    }

    @Test
    public void deleteFunctionWhenArgumentNullShouldThrowException() throws ValidationException, NotFoundException {
        // given
        Integer selectedFunctionIdToBeRemoved = null;

        // when
        assertThrows(NullPointerException.class, () -> {
            functionsBoundary.deleteFunction(selectedFunctionIdToBeRemoved);
        });
    }

    @Test
    public void deleteFunctionWhenNoEntityFoundForIdShouldThrowException()
            throws ValidationException, NotFoundException {
        // given
        Integer selectedFunctionIdToBeRemoved = 1;
        when(functionRepositoryMock.find(selectedFunctionIdToBeRemoved)).thenReturn(null);

        // when
        assertThrows(NotFoundException.class, () -> {
            functionsBoundary.deleteFunction(selectedFunctionIdToBeRemoved);
        });
    }

    @Test
    public void deleteFunctionWhenOverwrittenByFunctionShouldNotDeleteFunction() throws NotFoundException {
        // given
        functionA_ID_4.overwrite(functionA_ID_3);
        functionA_ID_3.setResource(resource);
        assertTrue(functionA_ID_3.isOverwrittenBySubTypeOrResourceFunction());
        when(functionRepositoryMock.find(ID_3)).thenReturn(functionA_ID_3);

        // when
        assertThrows(ValidationException.class, () -> {
            functionsBoundary.deleteFunction(functionA_ID_3.getId());
        });

        // then
        verify(functionServiceMock, never()).deleteFunction(functionA_ID_3);
    }

    @Test
    public void deleteFunctionWhenOverwrittenByFunctionShouldThrowException()
            throws ValidationException, NotFoundException {
        // given
        functionA_ID_4.overwrite(functionA_ID_3);
        functionA_ID_3.setResource(resource);
        assertTrue(functionA_ID_3.isOverwrittenBySubTypeOrResourceFunction());
        when(functionRepositoryMock.find(ID_3)).thenReturn(functionA_ID_3);

        // when
        assertThrows(ValidationException.class, () -> {
            functionsBoundary.deleteFunction(functionA_ID_3.getId());
        });
    }

    @Test
    public void deleteFunctionWhenOverwrittenByFunctionShouldAddOverwritingFunctionToException()
            throws NotFoundException {

        // given
        functionA_ID_4.overwrite(functionA_ID_3);
        functionA_ID_3.setResource(resource);
        assertTrue(functionA_ID_3.isOverwrittenBySubTypeOrResourceFunction());
        when(functionRepositoryMock.find(ID_3)).thenReturn(functionA_ID_3);

        // when
        try {
            functionsBoundary.deleteFunction(functionA_ID_3.getId());
        } catch (ValidationException e) {
            assertTrue(e.hasCausingObject());
            assertEquals(functionA_ID_4, e.getCausingObject());
        }
    }

    @Test
    public void deleteFunctionWhenNotOverwrittenShouldDelegateDelete() throws ValidationException, NotFoundException {

        // given
        AmwFunctionEntity resourceTypeFunction = new AmwFunctionEntityBuilder("resourceTypeFunction", 8)
                .forResourceType(resourceType).build();
        assertFalse(resourceTypeFunction.isOverwrittenBySubTypeOrResourceFunction());
        when(functionRepositoryMock.find(8)).thenReturn(resourceTypeFunction);

        // when
        functionsBoundary.deleteFunction(resourceTypeFunction.getId());

        // then
        verify(functionServiceMock, times(1)).deleteFunction(resourceTypeFunction);
    }

    @Test
    public void deleteFunctionWhenNotOverwrittenShouldReturnTrue() throws ValidationException, NotFoundException {
        // given
        AmwFunctionEntity resourceFunction = new AmwFunctionEntityBuilder("resourceFunction", 9).forResource(resource)
                .build();
        assertFalse(resourceFunction.isOverwrittenBySubTypeOrResourceFunction());
        when(functionRepositoryMock.find(9)).thenReturn(resourceFunction);

        // when
        functionsBoundary.deleteFunction(resourceFunction.getId());
    }

    @Test
    public void createNewResourceFunctionShouldLoadResourceAndDelegateToService()
            throws ValidationException, AMWException {
        // when
        when(resourceRepositoryMock.find(resource.getId())).thenReturn(resource);
        functionsBoundary.createNewResourceFunction(functionA_ID_3, resource.getId(), MIKS);

        // then
        verify(functionServiceMock).findFunctionsByNameInNamespace(resource, functionA_ID_3.getName());
    }

    @Test
    public void createNewResourceFunctionWhenNameIsNotYetUsedShouldDelegateSave()
            throws ValidationException, AMWException {
        // given
        when(resourceRepositoryMock.find(resource.getId())).thenReturn(resource);
        List<AmwFunctionEntity> functionsWithSameName = new ArrayList<>();
        when(functionServiceMock.findFunctionsByNameInNamespace(resource, functionA_ID_3.getName()))
                .thenReturn(functionsWithSameName);

        // when
        functionsBoundary.createNewResourceFunction(functionA_ID_3, resource.getId(), MIKS);

        // then
        verify(functionServiceMock).saveFunctionWithMiks(functionA_ID_3, MIKS);
    }

    @Test
    public void createNewResourceFunctionWhenNameIsAlreadyUsedShouldThrowException()
            throws ValidationException, AMWException {
        // given
        when(resourceRepositoryMock.find(resource.getId())).thenReturn(resource);
        List<AmwFunctionEntity> functionsWithSameName = new ArrayList<AmwFunctionEntity>() {
            {
                add(functionA_ID_3);
            }
        };
        when(functionServiceMock.findFunctionsByNameInNamespace(resource, functionA_ID_3.getName()))
                .thenReturn(functionsWithSameName);

        // when
        assertThrows(ValidationException.class, () -> {
            functionsBoundary.createNewResourceFunction(functionA_ID_3, resource.getId(), MIKS);
        });
    }

    @Test
    public void createNewResourceFunctionShouldDelegateVerificationToService()
            throws ValidationException, AMWException {
        // given
        String functionContent = "freemarkerContent";
        AmwFunctionEntity function = createFunction("functionName", 1);
        function.setImplementation(functionContent);

        // when
        functionsBoundary.createNewResourceTypeFunction(function, resource.getId(), MIKS);

        // then
        verify(freemarkerValidatorMock).validateFreemarkerSyntax(function.getDecoratedImplementation());
    }

    @Test
    public void createNewResourceFunctionWhenNotValidContentShouldThrowException()
            throws ValidationException, AMWException {
        // given
        String functionContent = "freemarkerContent";
        AmwFunctionEntity function = createFunction("functionName", 1);
        function.setImplementation(functionContent);
        doThrow(new AMWException("not valid freemarker syntax")).when(freemarkerValidatorMock)
                .validateFreemarkerSyntax(anyString());

        // when
        assertThrows(AMWException.class, () -> {
            functionsBoundary.createNewResourceTypeFunction(function, resource.getId(), MIKS);
        });
    }

    @Test
    public void createNewResourceTypeFunctionShouldLoadResourceTypeAndDelegateToService()
            throws ValidationException, AMWException {
        // when
        when(resourceTypeRepositoryMock.find(resourceType.getId())).thenReturn(resourceType);
        functionsBoundary.createNewResourceTypeFunction(functionA_ID_3, resourceType.getId(), MIKS);

        // then
        verify(functionServiceMock).findFunctionsByNameInNamespace(resourceType, functionA_ID_3.getName());
    }

    @Test
    public void createNewResourceTypeFunctionShouldDelegateVerificationToService()
            throws ValidationException, AMWException {
        // given
        String functionContent = "freemarkerContent";
        AmwFunctionEntity function = createFunction("functionName", 1);
        function.setImplementation(functionContent);

        // when
        functionsBoundary.createNewResourceTypeFunction(function, resourceType.getId(), MIKS);

        // then
        verify(freemarkerValidatorMock).validateFreemarkerSyntax(function.getDecoratedImplementation());
    }

    @Test
    public void createNewResourceTypeFunctionWhenNotValidContentShouldThrowException()
            throws ValidationException, AMWException {
        // given
        String functionContent = "freemarkerContent";
        AmwFunctionEntity function = createFunction("functionName", 1);
        function.setImplementation(functionContent);
        doThrow(new AMWException("not valid freemarker syntax")).when(freemarkerValidatorMock)
                .validateFreemarkerSyntax(anyString());

        // when
        assertThrows(AMWException.class, () -> {
            functionsBoundary.createNewResourceTypeFunction(function, resourceType.getId(), MIKS);
        });
    }

    @Test
    public void createNewResourceTypeFunctionWhenNameIsNotYetUsedShouldDelegateSave()
            throws ValidationException, AMWException {
        // given
        when(resourceTypeRepositoryMock.find(resourceType.getId())).thenReturn(resourceType);
        List<AmwFunctionEntity> functionsWithSameName = new ArrayList<>();
        when(functionServiceMock.findFunctionsByNameInNamespace(resourceType, functionA_ID_3.getName()))
                .thenReturn(functionsWithSameName);

        // when
        functionsBoundary.createNewResourceTypeFunction(functionA_ID_3, resourceType.getId(), MIKS);

        // then
        verify(functionServiceMock).saveFunctionWithMiks(functionA_ID_3, MIKS);
    }

    @Test
    public void createNewResourceTypeFunctionWhenNameIsAlreadyUsedShouldThrowException()
            throws ValidationException, AMWException {
        when(resourceTypeRepositoryMock.find(resourceType.getId())).thenReturn(resourceType);
        // given
        List<AmwFunctionEntity> functionsWithSameName = new ArrayList<AmwFunctionEntity>() {
            {
                add(functionA_ID_3);
            }
        };
        when(functionServiceMock.findFunctionsByNameInNamespace(resourceType, functionA_ID_3.getName()))
                .thenReturn(functionsWithSameName);

        // when
        assertThrows(ValidationException.class, () -> {
            functionsBoundary.createNewResourceTypeFunction(functionA_ID_3, resourceType.getId(), MIKS);

        });
    }

    @Test
    public void saveFunctionShouldDelegateToRepository() throws ValidationException, AMWException {
        // given
        functionA_ID_3.setResource(resource);

        // when
        functionsBoundary.saveFunction(functionA_ID_3);

        // then
        verify(functionRepositoryMock).persistOrMergeFunction(functionA_ID_3);
    }

    @Test
    public void saveFunctionShouldDelegateVerification() throws ValidationException, AMWException {
        // given
        String functionContent = "freemarkerContent";
        AmwFunctionEntity function = createFunction("functionName", 1);
        function.setImplementation(functionContent);
        function.setResourceType(resourceType);

        // when
        functionsBoundary.saveFunction(function);

        // then
        verify(freemarkerValidatorMock).validateFreemarkerSyntax(function.getDecoratedImplementation());
    }

    @Test
    public void saveFunctionWhenNotValidContentShouldThrowException() throws ValidationException, AMWException {
        // given
        String functionContent = "freemarkerContent";
        AmwFunctionEntity function = createFunction("functionName", 1);
        function.setImplementation(functionContent);
        function.setResource(resource);
        doThrow(new AMWException("not valid freemarker syntax")).when(freemarkerValidatorMock)
                .validateFreemarkerSyntax(anyString());

        // when
        assertThrows(AMWException.class, () -> {
            functionsBoundary.saveFunction(function);
        });
    }

    @Test
    public void overwriteResourceFunctionWhenFunctionForIdNotFoundShouldThrowException()
            throws ValidationException, AMWException {
        // given
        when(functionRepositoryMock.getFunctionByIdWithChildFunctions(functionA_ID_3.getId())).thenReturn(null);

        // when
        assertThrows(NotFoundException.class, () -> {
            functionsBoundary.overwriteResourceFunction("functionBody", resourceType.getId(), functionA_ID_3.getId());
        });
    }

    @Test
    public void overwriteResourceFunctionWhenResourceForIdNotFoundShouldThrowException() throws AMWException {
        // given
        when(functionRepositoryMock.getFunctionByIdWithChildFunctions(functionA_ID_3.getId()))
                .thenReturn(functionA_ID_3);
        when(resourceRepositoryMock.find(resource.getId())).thenReturn(null);

        // when
        assertThrows(NotFoundException.class, () -> {
            functionsBoundary.overwriteResourceFunction("functionBody", resource.getId(), functionA_ID_3.getId());
        });
    }

    @Test
    public void overwriteResourceFunctionWhenInvalidContentShouldThrowException() throws AMWException {
        // given
        when(resourceRepositoryMock.find(resource.getId())).thenReturn(resource);
        when(functionRepositoryMock.getFunctionByIdWithChildFunctions(functionA_ID_3.getId()))
                .thenReturn(functionA_ID_3);
        when(functionServiceMock.overwriteResourceFunction("functionBody", functionA_ID_3, resource))
                .thenReturn(createFunction(FUNCTION_NAME, 55));
        doThrow(new AMWException("not valid freemarker syntax")).when(freemarkerValidatorMock)
                .validateFreemarkerSyntax(anyString());

        // when
        assertThrows(AMWException.class, () -> {
            functionsBoundary.overwriteResourceFunction("functionBody", resource.getId(), functionA_ID_3.getId());
        });
    }

    @Test
    public void overwriteResourceFunctionForShouldDelegateOverwriteAndVerification()
            throws ValidationException, AMWException {
        // given
        when(resourceRepositoryMock.find(resource.getId())).thenReturn(resource);
        AmwFunctionEntity overwrittenFunction = createFunction(FUNCTION_NAME, 55);
        when(functionRepositoryMock.getFunctionByIdWithChildFunctions(functionA_ID_3.getId()))
                .thenReturn(functionA_ID_3);
        when(functionServiceMock.overwriteResourceFunction("functionBody", functionA_ID_3, resource))
                .thenReturn(overwrittenFunction);

        // when
        functionsBoundary.overwriteResourceFunction("functionBody", resource.getId(), functionA_ID_3.getId());

        // then
        verify(freemarkerValidatorMock).validateFreemarkerSyntax(overwrittenFunction.getDecoratedImplementation());
    }

    @Test
    public void overwriteResourceTypeFunctionWhenFunctionForIdNotFoundShouldThrowException() throws AMWException {
        // given
        when(functionRepositoryMock.getFunctionByIdWithChildFunctions(functionA_ID_3.getId())).thenReturn(null);

        // when
        assertThrows(NotFoundException.class, () -> {
            functionsBoundary.overwriteResourceTypeFunction("functionBody", resourceType.getId(),
                    functionA_ID_3.getId());
        });
    }

    @Test
    public void overwriteResourceTypeFunctionWhenInvalidContentShouldThrowException() throws AMWException {
        // given
        when(resourceTypeRepositoryMock.find(resourceType.getId())).thenReturn(resourceType);
        when(functionRepositoryMock.getFunctionByIdWithChildFunctions(functionA_ID_3.getId()))
                .thenReturn(functionA_ID_3);
        when(functionServiceMock.overwriteResourceTypeFunction("functionBody", functionA_ID_3, resourceType))
                .thenReturn(createFunction(FUNCTION_NAME, 55));
        doThrow(new AMWException("not valid freemarker syntax")).when(freemarkerValidatorMock)
                .validateFreemarkerSyntax(anyString());

        // when
        assertThrows(AMWException.class, () -> {
            functionsBoundary.overwriteResourceTypeFunction("functionBody", resourceType.getId(),
                    functionA_ID_3.getId());
        });
    }

    @Test
    public void overwriteResourceTypeFunctionWhenResourceTypeForIdNotFoundShouldThrowException() throws AMWException {
        // given
        when(functionRepositoryMock.getFunctionByIdWithChildFunctions(functionA_ID_3.getId()))
                .thenReturn(functionA_ID_3);
        when(resourceTypeRepositoryMock.find(resourceType.getId())).thenReturn(null);

        // when
        assertThrows(NotFoundException.class, () -> {
            functionsBoundary.overwriteResourceTypeFunction("functionBody", resourceType.getId(),
                    functionA_ID_3.getId());
        });
    }

    @Test
    public void overwriteResourceTypeFunctionForShouldDelegateOverwritingAndVerification() throws AMWException {
        // given
        AmwFunctionEntity overwrittenFunction = createFunction(FUNCTION_NAME, 55);
        when(resourceTypeRepositoryMock.find(resourceType.getId())).thenReturn(resourceType);
        when(functionRepositoryMock.getFunctionByIdWithChildFunctions(functionA_ID_3.getId()))
                .thenReturn(functionA_ID_3);
        when(functionServiceMock.overwriteResourceTypeFunction("functionBody", functionA_ID_3, resourceType))
                .thenReturn(overwrittenFunction);

        // when
        functionsBoundary.overwriteResourceTypeFunction("functionBody", resourceType.getId(), functionA_ID_3.getId());

        // then
        verify(freemarkerValidatorMock).validateFreemarkerSyntax(overwrittenFunction.getDecoratedImplementation());
    }

    private AmwFunctionEntity createFunction(String name, Integer id) {
        return new AmwFunctionEntityBuilder(name, id).build();
    }

    private ResourceEntity createResource(String name, int id, AmwFunctionEntity... functions) {
        ResourceEntity resource = new ResourceEntityBuilder().withName(name).withId(id)
                .withFunctions(new HashSet<>(Arrays.asList(functions))).build();
        for (AmwFunctionEntity function : functions) {
            function.setResource(resource);
        }
        return resource;
    }

    private ResourceTypeEntity createResourceType(String name, int id, AmwFunctionEntity... functions) {
        ResourceTypeEntity resourceType = new ResourceTypeEntityBuilder().buildResourceTypeEntity(name,
                new HashSet<ResourceEntity>(), false);
        resourceType.setId(id);
        resourceType.setFunctions(new HashSet<>(Arrays.asList(functions)));

        for (AmwFunctionEntity function : functions) {
            function.setResourceType(resourceType);
        }
        return resourceType;
    }

    @Test
    public void verifyCreateFunctionTestMethod() {
        // given
        String name = "name";
        Integer id = 1;

        // when
        AmwFunctionEntity fct = createFunction(name, id);

        // then
        assertEquals(name, fct.getName());
        assertEquals(id, fct.getId());
    }

    @Test
    public void verifyCreateResourceWithoutFunctionTestMethod() {
        // given
        String name = "name";
        Integer id = 1;

        // when
        ResourceEntity resource = createResource(name, id);

        // then
        assertEquals(name, resource.getName());
        assertEquals(id, resource.getId());
        assertTrue(resource.getFunctions().isEmpty());
    }

    @Test
    public void verifyCreateResourceWithFunctionTestMethod() {
        // given
        String name = "name";
        Integer id = 1;
        AmwFunctionEntity function1 = createFunction("function1", 1);
        AmwFunctionEntity function2 = createFunction("function2", 2);

        // when
        ResourceEntity resource = createResource(name, id, function1, function2);

        // then
        assertTrue(resource.getFunctions().contains(function1));
        assertTrue(resource.getFunctions().contains(function2));
        for (AmwFunctionEntity function : resource.getFunctions()) {
            assertEquals(resource, function.getResource());
        }
    }

    @Test
    public void verifyCreateResourceTypeWithoutFunctionTestMethod() {
        // given
        String name = "name";
        Integer id = 1;

        // when
        ResourceTypeEntity resourceType = createResourceType(name, id);

        // then
        assertEquals(name, resourceType.getName());
        assertEquals(id, resourceType.getId());
        assertTrue(resourceType.getFunctions().isEmpty());
    }

    @Test
    public void verifyCreateResourceTypeWithFunctionTestMethod() {
        // given
        String name = "name";
        Integer id = 1;
        AmwFunctionEntity function1 = createFunction("function1", 1);
        AmwFunctionEntity function2 = createFunction("function2", 2);

        // when
        ResourceTypeEntity resourceType = createResourceType(name, id, function1, function2);

        // then
        assertTrue(resourceType.getFunctions().contains(function1));
        assertTrue(resourceType.getFunctions().contains(function2));
        for (AmwFunctionEntity function : resourceType.getFunctions()) {
            assertEquals(resourceType, function.getResourceType());
        }
    }

}