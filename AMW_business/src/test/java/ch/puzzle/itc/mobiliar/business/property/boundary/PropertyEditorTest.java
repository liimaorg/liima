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

package ch.puzzle.itc.mobiliar.business.property.boundary;

import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

import ch.puzzle.itc.mobiliar.business.security.boundary.PermissionBoundary;
import ch.puzzle.itc.mobiliar.business.security.entity.Action;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import ch.puzzle.itc.mobiliar.builders.PropertyDescriptorEntityBuilder;
import ch.puzzle.itc.mobiliar.builders.ResourceEditPropertyBuilder;
import ch.puzzle.itc.mobiliar.builders.ResourceRelationEntityBuilder;
import ch.puzzle.itc.mobiliar.business.environment.boundary.ContextLocator;
import ch.puzzle.itc.mobiliar.business.environment.control.ContextDomainService;
import ch.puzzle.itc.mobiliar.business.environment.entity.ContextDependency;
import ch.puzzle.itc.mobiliar.business.environment.entity.ContextEntity;
import ch.puzzle.itc.mobiliar.business.environment.entity.HasContexts;
import ch.puzzle.itc.mobiliar.business.foreignable.control.ForeignableService;
import ch.puzzle.itc.mobiliar.business.foreignable.entity.ForeignableOwner;
import ch.puzzle.itc.mobiliar.business.foreignable.entity.ForeignableOwnerViolationException;
import ch.puzzle.itc.mobiliar.business.integration.entity.util.ResourceTypeEntityBuilder;
import ch.puzzle.itc.mobiliar.business.property.control.*;
import ch.puzzle.itc.mobiliar.business.property.entity.PropertyDescriptorEntity;
import ch.puzzle.itc.mobiliar.business.property.entity.PropertyTagEntity;
import ch.puzzle.itc.mobiliar.business.property.entity.ResourceEditProperty;
import ch.puzzle.itc.mobiliar.business.resourcegroup.boundary.ResourceLocator;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.*;
import ch.puzzle.itc.mobiliar.business.resourcerelation.boundary.ResourceRelationLocator;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ConsumedResourceRelationEntity;
import ch.puzzle.itc.mobiliar.business.security.entity.Permission;
import ch.puzzle.itc.mobiliar.business.utils.ValidationException;
import ch.puzzle.itc.mobiliar.common.exception.AMWException;
import ch.puzzle.itc.mobiliar.common.exception.NotAuthorizedException;

@RunWith(MockitoJUnitRunner.class)
public class PropertyEditorTest {

   @Mock
	EntityManager entityManagerMock;

	@Mock
	PropertyEditingService propertyEditingServiceMock;

	@Mock
	PropertyValueService propertyValueServiceMock;

	@Mock
    PermissionBoundary permissionBoundaryMock;

	@Mock
	ResourceLocator resourceLocatorMock;

    @Mock
    ResourceRelationLocator resourceRelationLocatorMock;

	@Mock
	ContextLocator contextLocatorMock;

    @Mock
    ContextDomainService contextServiceMock;

    @Mock
    PropertyValidationService propertyValidationServiceMock;

    @Mock
    PropertyTagEditingService propertyTagEditingServiceMock;

    @Mock
    PropertyDescriptorService propertyDescriptorServiceMock;

    @Mock
    ForeignableService foreignableServiceMock;

	@Mock
	Logger log;

	@InjectMocks
	PropertyEditor editor;


	@Test(expected = ValidationException.class)
	public void setPropertyValueOnResourceForContextOnInvalidArgumentShouldThrowValidationException() throws Exception {
        // given
        String resourceGroupName = "";
        String releaseName = null;
        String contextName = "";
        String propertyName = "";
        String propertyValue = "";

        // when
        editor.setPropertyValueOnResourceForContext(resourceGroupName, releaseName, contextName, propertyName, propertyValue);
	}

    @Test(expected = NoResultException.class)
    public void setPropertyValueOnResourceForContextShouldThrowExceptionWhenPropertyNotFound() throws Exception {
        // given
        String resourceGroupName = "resourceGroupName";
        String releaseName = "releaseName";
        String contextName = "contextName";
        String propertyName = "propertyName";
        String propertyValue = "propertyValue";
        String typeName = "resourceGroupTypeName";

        List<ResourceEditProperty> properties = new ArrayList<>();

        ContextEntity contextMock = mock(ContextEntity.class);

        when(resourceLocatorMock.getResourceByGroupNameAndRelease(resourceGroupName, releaseName)).thenReturn(createWithIdNameAndTypeName(1, resourceGroupName, typeName));
        when(contextLocatorMock.getContextByName(contextName)).thenReturn(contextMock);
        when(propertyValueServiceMock.decryptProperties(anyList())).thenReturn(properties);


        // when
        Assert.assertTrue(properties.isEmpty());
        editor.setPropertyValueOnResourceForContext(resourceGroupName, releaseName, contextName, propertyName, propertyValue);
    }

    private ResourceEntity createWithIdNameAndTypeName(int id, String name, String typeName){
        ResourceEntity resource = ResourceFactory.createNewResource(name);
        ResourceTypeEntity type =  new ResourceTypeEntityBuilder().name(typeName).build();
        resource.setResourceType(type);
        resource.setId(id);

        return resource;
    }

    @Test(expected = NotAuthorizedException.class)
    public void setPropertyValueOnResourceForContextShouldThrowExceptionWhenResourceUpdatePermissionIsMissing() throws Exception {
        // given
        String resourceGroupName = "resourceGroupName";
        String releaseName = "releaseName";
        String contextName = "contextName";
        String propertyName = "propertyName";
        String propertyValue = "propertyValue";
        String typeName = "resourceGroupTypeName";

        List<ResourceEditProperty> properties = new ArrayList<>();
        properties.add(new ResourceEditPropertyBuilder().withDisplayAndTechKeyName(propertyName).withValue(propertyValue).build());

        ResourceEntity resource = createWithIdNameAndTypeName(1, resourceGroupName, typeName);

        ContextEntity contextMock = mock(ContextEntity.class);

        when(resourceLocatorMock.getResourceByGroupNameAndRelease(resourceGroupName, releaseName)).thenReturn(resource);
        when(contextLocatorMock.getContextByName(contextName)).thenReturn(contextMock);
        when(propertyEditingServiceMock.loadPropertiesForEditResource(anyInt(), any(ResourceTypeEntity.class), any(ContextEntity.class))).thenReturn(properties);
        when(permissionBoundaryMock.hasPermission(Permission.RESOURCE, contextMock, Action.UPDATE, null, null)).thenReturn(false);
        when(permissionBoundaryMock.hasPermission(Permission.RESOURCE_PROPERTY_DECRYPT, contextMock, Action.ALL, resource, null)).thenReturn(false);

        // when
        Assert.assertTrue(!properties.isEmpty());
        editor.setPropertyValueOnResourceForContext(resourceGroupName, releaseName, contextName, propertyName, propertyValue);
    }

    @Test
    public void setPropertyValueOnResourceForContextShouldSetPropertyValue() throws Exception {
        // given
        String resourceGroupName = "resourceGroupName";
        String releaseName = "releaseName";
        String contextName = "contextName";
        String propertyName = "propertyName";
        String propertyValue = "propertyValue";
        String typeName = "resourceGroupTypeName";

        List<ResourceEditProperty> properties = new ArrayList<>();
        properties.add(new ResourceEditPropertyBuilder().withDisplayAndTechKeyName(propertyName).withValue(propertyValue).build());
        ContextEntity contextMock = mock(ContextEntity.class);
        ResourceEntity resource = createWithIdNameAndTypeName(1, resourceGroupName, typeName);

        when(resourceLocatorMock.getResourceByGroupNameAndRelease(resourceGroupName, releaseName)).thenReturn(resource);
        when(contextLocatorMock.getContextByName(contextName)).thenReturn(contextMock);
        when(propertyEditingServiceMock.loadPropertiesForEditResource(any(Integer.class), any(ResourceTypeEntity.class), any(ContextEntity.class))).thenReturn(properties);
        when(permissionBoundaryMock.hasPermission(Permission.RESOURCE, contextMock, Action.UPDATE, resource, null)).thenReturn(true);
        when(permissionBoundaryMock.hasPermission(Permission.RESOURCE_PROPERTY_DECRYPT, contextMock, Action.ALL, resource, null)).thenReturn(true);

        setupMocksForSinglePropertiesModificationsFor(resource);

        // when
        Assert.assertTrue(!properties.isEmpty());
        editor.setPropertyValueOnResourceForContext(resourceGroupName, releaseName, contextName, propertyName, propertyValue);

        //then
        verify(propertyValueServiceMock).setPropertyValue(Matchers.any(ContextDependency.class), Matchers.anyInt(), Matchers.eq(propertyValue));

    }

    private void setupMocksForSinglePropertiesModificationsFor(HasContexts<?> hasContexts){
        HasContexts hasContextMergedMock = mock(HasContexts.class);
        when(entityManagerMock.merge(hasContexts)).thenReturn(hasContextMergedMock);

        ResourceContextEntity resourceContextEntityMock = mock(ResourceContextEntity.class);
        when(hasContextMergedMock.getOrCreateContext(any(ContextEntity.class))).thenReturn(resourceContextEntityMock);
    }


    @Test(expected = ValidationException.class)
    public void resetPropertyValueOnResourceForContextOnInvalidArgumentShouldThrowValidationException() throws Exception {
        // given
        String resourceGroupName = "";
        String releaseName = null;
        String contextName = "";
        String propertyName = "";

        // when
        editor.resetPropertyValueOnResourceForContext(resourceGroupName, releaseName, contextName, propertyName);
    }

    @Test(expected = NoResultException.class)
    public void resetPropertyValueOnResourceForContextShouldThrowExceptionWhenPropertyNotFound() throws Exception {
        // given
        String resourceGroupName = "resourceGroupName";
        String releaseName = "releaseName";
        String contextName = "contextName";
        String propertyName = "propertyName";
        String typeName = "resourceGroupTypeName";

        List<ResourceEditProperty> properties = new ArrayList<>();

        ContextEntity contextMock = mock(ContextEntity.class);

        when(resourceLocatorMock.getResourceByGroupNameAndRelease(resourceGroupName, releaseName)).thenReturn(createWithIdNameAndTypeName(1, resourceGroupName, typeName));
        when(contextLocatorMock.getContextByName(contextName)).thenReturn(contextMock);
        when(propertyValueServiceMock.decryptProperties(anyList())).thenReturn(properties);


        // when
        Assert.assertTrue(properties.isEmpty());
        editor.resetPropertyValueOnResourceForContext(resourceGroupName, releaseName, contextName, propertyName);
    }

    @Test(expected = NotAuthorizedException.class)
    public void resetPropertyValueOnResourceForContextShouldThrowExceptionWhenResourceUpdatePermissionIsMissing() throws Exception {
        // given
        String resourceGroupName = "resourceGroupName";
        String releaseName = "releaseName";
        String contextName = "contextName";
        String propertyName = "propertyName";
        String propertyValue = "propertyValue";
        String typeName = "resourceGroupTypeName";

        List<ResourceEditProperty> properties = new ArrayList<>();
        properties.add(new ResourceEditPropertyBuilder().withDisplayAndTechKeyName(propertyName).withValue(propertyValue).build());

        ContextEntity contextMock = mock(ContextEntity.class);

        ResourceEntity resource = createWithIdNameAndTypeName(1, resourceGroupName, typeName);

        when(resourceLocatorMock.getResourceByGroupNameAndRelease(resourceGroupName, releaseName)).thenReturn(resource);
        when(contextLocatorMock.getContextByName(contextName)).thenReturn(contextMock);
        when(propertyValueServiceMock.decryptProperties(anyList())).thenReturn(properties);
        when(permissionBoundaryMock.hasPermission(Permission.RESOURCE, contextMock, Action.UPDATE, null, null)).thenReturn(false);
        when(permissionBoundaryMock.hasPermission(Permission.RESOURCE_PROPERTY_DECRYPT, contextMock, Action.ALL, resource, null)).thenReturn(true);

        // when
        Assert.assertTrue(!properties.isEmpty());
        editor.resetPropertyValueOnResourceForContext(resourceGroupName, releaseName, contextName, propertyName);
    }

    @Test
    public void resetPropertyValueOnResourceForContextShouldBeSuccessfullWhenResourceUpdatePermissionIsPresentButResourcePropertyDecryptIsMissing() throws Exception {
        // given
        String resourceGroupName = "resourceGroupName";
        String releaseName = "releaseName";
        String contextName = "contextName";
        String propertyName = "propertyName";
        String propertyValue = "propertyValue";
        String typeName = "resourceGroupTypeName";

        List<ResourceEditProperty> properties = new ArrayList<>();
        properties.add(new ResourceEditPropertyBuilder().withDisplayAndTechKeyName(propertyName).withValue(propertyValue).withDescriptorId(12).build());

        ContextEntity contextMock = mock(ContextEntity.class);

        ResourceEntity resource = createWithIdNameAndTypeName(1, resourceGroupName, typeName);

        when(resourceLocatorMock.getResourceByGroupNameAndRelease(resourceGroupName, releaseName)).thenReturn(resource);
        when(contextLocatorMock.getContextByName(contextName)).thenReturn(contextMock);
        when(propertyEditingServiceMock.loadPropertiesForEditResource(anyInt(), any(ResourceTypeEntity.class), any(ContextEntity.class))).thenReturn(properties);
        when(permissionBoundaryMock.hasPermission(Permission.RESOURCE, contextMock, Action.UPDATE, resource, null)).thenReturn(true);
        when(permissionBoundaryMock.hasPermission(Permission.RESOURCE_PROPERTY_DECRYPT, contextMock, Action.ALL, resource, null)).thenReturn(false);
        when(entityManagerMock.merge(resource)).thenReturn(resource);
        when(entityManagerMock.merge(contextMock)).thenReturn(contextMock);

        // when
        Assert.assertTrue(!properties.isEmpty());
        editor.resetPropertyValueOnResourceForContext(resourceGroupName, releaseName, contextName, propertyName);
    }

    @Test
    public void resetPropertyValueOnResourceForContextShouldResetPropertyValue() throws Exception {
        // given
        String resourceGroupName = "resourceGroupName";
        String releaseName = "releaseName";
        String contextName = "contextName";
        String propertyName = "propertyName";
        String propertyValue = "propertyValue";
        String typeName = "resourceGroupTypeName";

        List<ResourceEditProperty> properties = new ArrayList<>();
        properties.add(new ResourceEditPropertyBuilder().withDisplayAndTechKeyName(propertyName).withValue(propertyValue).build());

        ContextEntity contextMock = mock(ContextEntity.class);

        ResourceEntity resource = createWithIdNameAndTypeName(1, resourceGroupName, typeName);
        when(resourceLocatorMock.getResourceByGroupNameAndRelease(resourceGroupName, releaseName)).thenReturn(resource);
        when(contextLocatorMock.getContextByName(contextName)).thenReturn(contextMock);
        when(propertyValueServiceMock.decryptProperties(anyList())).thenReturn(properties);
        when(permissionBoundaryMock.hasPermission(Permission.RESOURCE, contextMock, Action.UPDATE, resource, null)).thenReturn(true);
        when(permissionBoundaryMock.hasPermission(Permission.RESOURCE_PROPERTY_DECRYPT, contextMock, Action.ALL, resource, null)).thenReturn(true);

        setupMocksForSinglePropertiesModificationsFor(resource);


        // when
        Assert.assertTrue(!properties.isEmpty());
        editor.resetPropertyValueOnResourceForContext(resourceGroupName, releaseName, contextName, propertyName);

        //then
        verify(propertyValueServiceMock).resetPropertyValue(Matchers.any(ContextDependency.class), Matchers.anyInt());

    }



    @Test(expected = ValidationException.class)
    public void setPropertyValueOnResourceRelationForContextOnInvalidArgumentShouldThrowValidationException() throws Exception {
        // given
        String resourceGroupName = "";
        String releaseName = null;
        String relatedResourceGroupName = "";
        String relatedResourceReleaseName ="";
        String contextName = "";
        String propertyName = "";
        String propertyValue ="";

        // when
        editor.setPropertyValueOnResourceRelationForContext(resourceGroupName, releaseName, relatedResourceGroupName, relatedResourceReleaseName, contextName, propertyName, propertyValue);
    }

    @Test(expected = NoResultException.class)
    public void setPropertyValueOnResourceRelationForContextShouldThrowExceptionWhenPropertyNotFound() throws Exception {
        // given
        String resourceGroupName = "resourceGroupName";
        String releaseName = "releaseName";
        String relatedResourceGroupName = "relatedResourceGroupName";
        String relatedResourceReleaseName ="relatedResourceReleaseName";
        String contextName = "contextName";
        String propertyName = "propertyName";
        String propertyValue = "propertyValue";

        List<ResourceEditProperty> properties = new ArrayList<>();

        ContextEntity contextMock = mock(ContextEntity.class);

        when(resourceRelationLocatorMock.getResourceRelation(resourceGroupName, releaseName, relatedResourceGroupName, relatedResourceReleaseName)).thenReturn(createWithMasterAndSlave(resourceGroupName, relatedResourceGroupName));
        when(contextLocatorMock.getContextByName(contextName)).thenReturn(contextMock);
        when(entityManagerMock.find(ResourceEntity.class, 1)).thenReturn(mock(ResourceEntity.class));
        when(entityManagerMock.find(ResourceEntity.class, 2)).thenReturn(mock(ResourceEntity.class));
        when(propertyValueServiceMock.decryptProperties(anyList())).thenReturn(properties);

        // when
        Assert.assertTrue(properties.isEmpty());
        editor.setPropertyValueOnResourceRelationForContext(resourceGroupName, releaseName, relatedResourceGroupName, relatedResourceReleaseName, contextName, propertyName, propertyValue);
    }



    @Test(expected = NotAuthorizedException.class)
    public void setPropertyValueOnResourceRelationForContextShouldThrowExceptionWhenWhenResourceUpdatePermissionIsMissing() throws Exception {
        // given
        String resourceGroupName = "resourceGroupName";
        String releaseName = "releaseName";
        String relatedResourceGroupName = "relatedResourceGroupName";
        String relatedResourceReleaseName ="relatedResourceReleaseName";
        String contextName = "contextName";
        String propertyName = "propertyName";
        String propertyValue = "propertyValue";

        List<ResourceEditProperty> properties = new ArrayList<>();
        properties.add(new ResourceEditPropertyBuilder().withDisplayAndTechKeyName(propertyName).withValue(propertyValue).build());

        ContextEntity contextMock = mock(ContextEntity.class);

        ConsumedResourceRelationEntity relation = createWithMasterAndSlave(resourceGroupName, relatedResourceGroupName);

        when(resourceRelationLocatorMock.getResourceRelation(resourceGroupName, releaseName, relatedResourceGroupName, relatedResourceReleaseName)).thenReturn(relation);
        when(contextLocatorMock.getContextByName(contextName)).thenReturn(contextMock);
        when(entityManagerMock.find(ResourceEntity.class, 1)).thenReturn(mock(ResourceEntity.class));
        when(entityManagerMock.find(ResourceEntity.class, 2)).thenReturn(mock(ResourceEntity.class));
        when(propertyValueServiceMock.decryptProperties(anyList())).thenReturn(properties);
        when(permissionBoundaryMock.hasPermission(Permission.RESOURCE, contextMock, Action.UPDATE, null, null)).thenReturn(false);
        when(permissionBoundaryMock.hasPermission(Permission.RESOURCE_PROPERTY_DECRYPT, contextMock, Action.ALL, relation.getSlaveResource(), null)).thenReturn(true);


        // when
        Assert.assertTrue(!properties.isEmpty());
        editor.setPropertyValueOnResourceRelationForContext(resourceGroupName, releaseName, relatedResourceGroupName, relatedResourceReleaseName, contextName, propertyName, propertyValue);
    }

    @Test
    public void setPropertyValueOnResourceRelationForContextShouldSetPropertyValue() throws Exception {
        // given
        String resourceGroupName = "resourceGroupName";
        String releaseName = "releaseName";
        String relatedResourceGroupName = "relatedResourceGroupName";
        String relatedResourceReleaseName ="relatedResourceReleaseName";
        String contextName = "contextName";
        String propertyName = "propertyName";
        String propertyValue = "propertyValue";

        List<ResourceEditProperty> properties = new ArrayList<>();
        properties.add(new ResourceEditPropertyBuilder().withDisplayAndTechKeyName(propertyName).withValue(propertyValue).build());

        ContextEntity contextMock = mock(ContextEntity.class);

        ConsumedResourceRelationEntity relation = createWithMasterAndSlave(resourceGroupName, relatedResourceGroupName);
        when(resourceRelationLocatorMock.getResourceRelation(resourceGroupName, releaseName, relatedResourceGroupName, relatedResourceReleaseName)).thenReturn(relation);
        when(contextLocatorMock.getContextByName(contextName)).thenReturn(contextMock);
        when(entityManagerMock.find(ResourceEntity.class, 1)).thenReturn(mock(ResourceEntity.class));
        when(entityManagerMock.find(ResourceEntity.class, 2)).thenReturn(mock(ResourceEntity.class));
        when(propertyValueServiceMock.decryptProperties(anyList())).thenReturn(properties);
        when(permissionBoundaryMock.hasPermission(Permission.RESOURCE, contextMock, Action.UPDATE, relation.getMasterResource(), null)).thenReturn(true);
        when(permissionBoundaryMock.hasPermission(Permission.RESOURCE_PROPERTY_DECRYPT, contextMock, Action.ALL, relation.getSlaveResource(), null)).thenReturn(true);
        setupMocksForSinglePropertiesModificationsFor(relation);

        // when
        Assert.assertTrue(!properties.isEmpty());
        editor.setPropertyValueOnResourceRelationForContext(resourceGroupName, releaseName, relatedResourceGroupName, relatedResourceReleaseName, contextName, propertyName, propertyValue);

        // then
        verify(propertyValueServiceMock).setPropertyValue(Matchers.any(ContextDependency.class), Matchers.anyInt(), Matchers.eq(propertyValue));
    }


    private ConsumedResourceRelationEntity createWithMasterAndSlave(String masterResourceName, String slaveResourceName){
        ConsumedResourceRelationEntity resource = new ResourceRelationEntityBuilder().buildConsumedResRelEntity(createWithIdNameAndTypeName(1, masterResourceName, "masterResourceType"), createWithIdNameAndTypeName(2, slaveResourceName, "slaveResourceType"), "identifier", null);

        return resource;
    }


    @Test(expected = ValidationException.class)
    public void resetPropertyValueOnResourceRelationForContextOnInvalidArgumentShouldThrowValidationException() throws Exception {
        // given
        String resourceGroupName = "";
        String releaseName = null;
        String relatedResourceGroupName = "";
        String relatedResourceReleaseName ="";
        String contextName = "";
        String propertyName = "";

        // when
        editor.resetPropertyValueOnResourceRelationForContext(resourceGroupName, releaseName, relatedResourceGroupName, relatedResourceReleaseName, contextName, propertyName);
    }

    @Test(expected = NoResultException.class)
    public void resetPropertyValueOnResourceRelationForContextShouldThrowExceptionWhenPropertyNotFound() throws Exception {
        // given
        String resourceGroupName = "resourceGroupName";
        String releaseName = "releaseName";
        String relatedResourceGroupName = "relatedResourceGroupName";
        String relatedResourceReleaseName ="relatedResourceReleaseName";
        String contextName = "contextName";
        String propertyName = "propertyName";

        List<ResourceEditProperty> properties = new ArrayList<>();

        ContextEntity contextMock = mock(ContextEntity.class);

        when(resourceRelationLocatorMock.getResourceRelation(resourceGroupName, releaseName, relatedResourceGroupName, relatedResourceReleaseName)).thenReturn(createWithMasterAndSlave(resourceGroupName, relatedResourceGroupName));
        when(contextLocatorMock.getContextByName(contextName)).thenReturn(contextMock);
        when(entityManagerMock.find(ResourceEntity.class, 1)).thenReturn(mock(ResourceEntity.class));
        when(entityManagerMock.find(ResourceEntity.class, 2)).thenReturn(mock(ResourceEntity.class));
        when(propertyValueServiceMock.decryptProperties(anyList())).thenReturn(properties);

        // when
        Assert.assertTrue(properties.isEmpty());
        editor.resetPropertyValueOnResourceRelationForContext(resourceGroupName, releaseName, relatedResourceGroupName, relatedResourceReleaseName, contextName, propertyName);
    }



    @Test(expected = NotAuthorizedException.class)
    public void resetPropertyValueOnResourceRelationForContextShouldThrowExceptionWhenWhenResourceUpdatePermissionIsMissing() throws Exception {
        // given
        String resourceGroupName = "resourceGroupName";
        String releaseName = "releaseName";
        String relatedResourceGroupName = "relatedResourceGroupName";
        String relatedResourceReleaseName ="relatedResourceReleaseName";
        String contextName = "contextName";
        String propertyName = "propertyName";
        String propertyValue = "propertyValue";

        ConsumedResourceRelationEntity relationWithMasterAndSlave = createWithMasterAndSlave(resourceGroupName, relatedResourceGroupName);

        List<ResourceEditProperty> properties = new ArrayList<>();
        properties.add(new ResourceEditPropertyBuilder().withDisplayAndTechKeyName(propertyName).withValue(propertyValue).build());

        ContextEntity contextMock = mock(ContextEntity.class);

        when(resourceRelationLocatorMock.getResourceRelation(resourceGroupName, releaseName, relatedResourceGroupName, relatedResourceReleaseName)).thenReturn(relationWithMasterAndSlave);
        when(contextLocatorMock.getContextByName(contextName)).thenReturn(contextMock);
        when(entityManagerMock.find(ResourceEntity.class, 1)).thenReturn(mock(ResourceEntity.class));
        when(entityManagerMock.find(ResourceEntity.class, 2)).thenReturn(mock(ResourceEntity.class));
        when(propertyValueServiceMock.decryptProperties(anyList())).thenReturn(properties);
        when(permissionBoundaryMock.hasPermission(Permission.RESOURCE, contextMock, Action.UPDATE, relationWithMasterAndSlave.getMasterResource(), null)).thenReturn(false);
        when(permissionBoundaryMock.hasPermission(Permission.RESOURCE_PROPERTY_DECRYPT, contextMock, Action.ALL, relationWithMasterAndSlave.getSlaveResource(), null)).thenReturn(true);

        // when
        Assert.assertTrue(!properties.isEmpty());
        editor.resetPropertyValueOnResourceRelationForContext(resourceGroupName, releaseName, relatedResourceGroupName, relatedResourceReleaseName, contextName, propertyName);
    }

    @Test
    public void resetPropertyValueOnResourceRelationForContextShouldResetPropertyValue() throws Exception {
        // given
        String resourceGroupName = "resourceGroupName";
        String releaseName = "releaseName";
        String relatedResourceGroupName = "relatedResourceGroupName";
        String relatedResourceReleaseName ="relatedResourceReleaseName";
        String contextName = "contextName";
        String propertyName = "propertyName";
        String propertyValue = "propertyValue";

        List<ResourceEditProperty> properties = new ArrayList<>();
        properties.add(new ResourceEditPropertyBuilder().withDisplayAndTechKeyName(propertyName).withValue(propertyValue).build());

        ContextEntity contextMock = mock(ContextEntity.class);

        ConsumedResourceRelationEntity relation = createWithMasterAndSlave(resourceGroupName, relatedResourceGroupName);
        when(resourceRelationLocatorMock.getResourceRelation(resourceGroupName, releaseName, relatedResourceGroupName, relatedResourceReleaseName)).thenReturn(relation);
        when(contextLocatorMock.getContextByName(contextName)).thenReturn(contextMock);
        when(entityManagerMock.find(ResourceEntity.class, 1)).thenReturn(mock(ResourceEntity.class));
        when(entityManagerMock.find(ResourceEntity.class, 2)).thenReturn(mock(ResourceEntity.class));
        when(propertyValueServiceMock.decryptProperties(anyList())).thenReturn(properties);
        when(permissionBoundaryMock.hasPermission(Permission.RESOURCE, contextMock, Action.UPDATE, relation.getMasterResource(), null)).thenReturn(true);
        when(permissionBoundaryMock.hasPermission(Permission.RESOURCE_PROPERTY_DECRYPT, contextMock, Action.ALL, relation.getSlaveResource(), null)).thenReturn(true);
        setupMocksForSinglePropertiesModificationsFor(relation);

        // when
        Assert.assertTrue(!properties.isEmpty());
        editor.resetPropertyValueOnResourceRelationForContext(resourceGroupName, releaseName, relatedResourceGroupName, relatedResourceReleaseName, contextName, propertyName);

        // then
        verify(propertyValueServiceMock).resetPropertyValue(Matchers.any(ContextDependency.class), Matchers.anyInt());
    }


    @Test(expected = ValidationException.class)
    public void setPropertyValueOnAllResourceRelationsForContextWhereNotYetSetOnInvalidArgumentShouldThrowValidationException() throws Exception {
        // given
        String resourceGroupName = "";
        String relatedResourceGroupName = "";
        String relatedResourceReleaseName ="";
        String contextName = "";
        String propertyName = "";
        String propertyValue ="";

        // when
        editor.setPropertyValueOnAllResourceRelationsForContextWhereNotYetSet(resourceGroupName, relatedResourceGroupName, relatedResourceReleaseName, contextName, propertyName, propertyValue);
    }

    @Test(expected = NoResultException.class)
    public void setPropertyValueOnAllResourceRelationsForContextWhereNotYetSetShouldThrowExceptionWhenNoRelationsFound() throws Exception {
        // given
        String resourceGroupName = "resourceGroupName";
        String relatedResourceGroupName = "relatedResourceGroupName";
        String relatedResourceReleaseName ="relatedResourceReleaseName";
        String contextName = "contextName";
        String propertyName = "propertyName";
        String propertyValue = "propertyValue";

        List<ResourceEntity> resourcesByGroupNameWithRelations = new ArrayList<>();

        when(resourceLocatorMock.getResourcesByGroupNameWithRelations(resourceGroupName)).thenReturn(resourcesByGroupNameWithRelations);

        ResourceEntity relatedResource = createWithIdNameAndTypeName(1, relatedResourceGroupName, "relatedResourceType");
        when(resourceLocatorMock.getResourceByGroupNameAndRelease(relatedResourceGroupName, relatedResourceReleaseName)).thenReturn(relatedResource);

        // when
        Assert.assertTrue(resourcesByGroupNameWithRelations.isEmpty());
        editor.setPropertyValueOnAllResourceRelationsForContextWhereNotYetSet(resourceGroupName, relatedResourceGroupName, relatedResourceReleaseName, contextName, propertyName, propertyValue);
    }

    @Test(expected = NoResultException.class)
    public void setPropertyValueOnAllResourceRelationsForContextWhereNotYetSetShouldThrowExceptionWhenNoSlaveResourceFound() throws Exception {
        // given
        String resourceGroupName = "resourceGroupName";
        String relatedResourceGroupName = "relatedResourceGroupName";
        String relatedResourceReleaseName ="relatedResourceReleaseName";
        String contextName = "contextName";
        String propertyName = "propertyName";
        String propertyValue = "propertyValue";

        List<ResourceEntity> resourcesByGroupNameWithRelations = new ArrayList<>();
        resourcesByGroupNameWithRelations.add(createWithIdNameAndTypeName(1, resourceGroupName, "resourceType"));

        when(resourceLocatorMock.getResourcesByGroupNameWithRelations(resourceGroupName)).thenReturn(resourcesByGroupNameWithRelations);
        when(resourceLocatorMock.getResourceByGroupNameAndRelease(relatedResourceGroupName, relatedResourceReleaseName)).thenReturn(null);

        // when
        Assert.assertTrue(!resourcesByGroupNameWithRelations.isEmpty());
        editor.setPropertyValueOnAllResourceRelationsForContextWhereNotYetSet(resourceGroupName, relatedResourceGroupName, relatedResourceReleaseName, contextName, propertyName, propertyValue);
    }

    @Test(expected = NoResultException.class)
    public void setPropertyValueOnAllResourceRelationsForContextWhereNotYetSetShouldThrowExceptionWhenNoPropertyFoundOnRelation() throws Exception {
        // given
        String resourceGroupName = "resourceGroupName";
        String relatedResourceGroupName = "relatedResourceGroupName";
        String relatedResourceReleaseName ="relatedResourceReleaseName";
        String contextName = "contextName";
        String propertyName = "propertyName";
        String propertyValue = "propertyValue";

        List<ResourceEditProperty> properties = new ArrayList<>();

        ResourceEntity relatedResource = createWithIdNameAndTypeName(2, relatedResourceGroupName, "relatedResourceType");
        ResourceEntity resource = createWithIdNameAndTypeNameWithRelations(1, resourceGroupName, "resourceType", relatedResource);

        List<ResourceEntity> resourcesByGroupNameWithRelations = new ArrayList<>();
        resourcesByGroupNameWithRelations.add(resource);

        ContextEntity contextMock = mock(ContextEntity.class);

        when(resourceLocatorMock.getResourcesByGroupNameWithRelations(resourceGroupName)).thenReturn(resourcesByGroupNameWithRelations);

        when(resourceLocatorMock.getResourceByGroupNameAndRelease(relatedResourceGroupName, relatedResourceReleaseName)).thenReturn(relatedResource);

        when(contextLocatorMock.getContextByName(contextName)).thenReturn(contextMock);
        when(entityManagerMock.find(ResourceEntity.class, 1)).thenReturn(resource);
        when(entityManagerMock.find(ResourceEntity.class, 2)).thenReturn(relatedResource);
        when(propertyValueServiceMock.decryptProperties(anyList())).thenReturn(properties);

        // when
        Assert.assertTrue(!resourcesByGroupNameWithRelations.isEmpty());
        Assert.assertTrue(properties.isEmpty());
        editor.setPropertyValueOnAllResourceRelationsForContextWhereNotYetSet(resourceGroupName, relatedResourceGroupName, relatedResourceReleaseName, contextName, propertyName, propertyValue);
    }

    @Test(expected = NotAuthorizedException.class)
    public void setPropertyValueOnAllResourceRelationsForContextWhereNotYetSetShouldThrowExceptionWhenPropertyHasNoValueButResourceUpdatePErmissionIsMissing() throws Exception {
        // given
        String resourceGroupName = "resourceGroupName";
        String relatedResourceGroupName = "relatedResourceGroupName";
        String relatedResourceReleaseName ="relatedResourceReleaseName";
        String contextName = "contextName";
        String propertyName = "propertyName";
        String propertyValue = "propertyValue";

        List<ResourceEditProperty> properties = new ArrayList<>();
        properties.add(new ResourceEditPropertyBuilder().withDisplayAndTechKeyName(propertyName).build());

        ResourceEntity relatedResource = createWithIdNameAndTypeName(2, relatedResourceGroupName, "relatedResourceType");
        ResourceEntity resource = createWithIdNameAndTypeNameWithRelations(1, resourceGroupName, "resourceType", relatedResource);

        List<ResourceEntity> resourcesByGroupNameWithRelations = new ArrayList<>();
        resourcesByGroupNameWithRelations.add(resource);

        ContextEntity contextMock = mock(ContextEntity.class);

        when(resourceLocatorMock.getResourcesByGroupNameWithRelations(resourceGroupName)).thenReturn(resourcesByGroupNameWithRelations);

        when(resourceLocatorMock.getResourceByGroupNameAndRelease(relatedResourceGroupName, relatedResourceReleaseName)).thenReturn(relatedResource);

        when(contextLocatorMock.getContextByName(contextName)).thenReturn(contextMock);
        when(entityManagerMock.find(ResourceEntity.class, 1)).thenReturn(resource);
        when(entityManagerMock.find(ResourceEntity.class, 2)).thenReturn(relatedResource);
        when(propertyValueServiceMock.decryptProperties(anyList())).thenReturn(properties);
        when(permissionBoundaryMock.hasPermission(Permission.RESOURCE, contextMock, Action.UPDATE, null, null)).thenReturn(false);
        when(permissionBoundaryMock.hasPermission(Permission.RESOURCE_PROPERTY_DECRYPT, contextMock, Action.ALL, relatedResource, null)).thenReturn(true);

        setupMocksForSinglePropertiesModificationsFor(resource);

        // when
        Assert.assertTrue(!resourcesByGroupNameWithRelations.isEmpty());
        Assert.assertTrue(!properties.isEmpty());
        editor.setPropertyValueOnAllResourceRelationsForContextWhereNotYetSet(resourceGroupName, relatedResourceGroupName, relatedResourceReleaseName, contextName, propertyName, propertyValue);
    }



    @Test
    public void setPropertyValueOnAllResourceRelationsForContextWhereNotYetSetShouldSetNoPropertyValueWhenValueAlreadySet() throws Exception {
        // given
        String resourceGroupName = "resourceGroupName";
        String relatedResourceGroupName = "relatedResourceGroupName";
        String relatedResourceReleaseName ="relatedResourceReleaseName";
        String contextName = "contextName";
        String propertyName = "propertyName";
        String propertyValue = "propertyValue";

        List<ResourceEditProperty> properties = new ArrayList<>();
        properties.add(new ResourceEditPropertyBuilder().withDisplayAndTechKeyName(propertyName).withValue(propertyValue).build());

        ResourceEntity relatedResource = createWithIdNameAndTypeName(2, relatedResourceGroupName, "relatedResourceType");
        ResourceEntity resource = createWithIdNameAndTypeNameWithRelations(1, resourceGroupName, "resourceType", relatedResource);

        List<ResourceEntity> resourcesByGroupNameWithRelations = new ArrayList<>();
        resourcesByGroupNameWithRelations.add(resource);

        ContextEntity contextMock = mock(ContextEntity.class);

        when(resourceLocatorMock.getResourcesByGroupNameWithRelations(resourceGroupName)).thenReturn(resourcesByGroupNameWithRelations);

        when(resourceLocatorMock.getResourceByGroupNameAndRelease(relatedResourceGroupName, relatedResourceReleaseName)).thenReturn(relatedResource);

        when(contextLocatorMock.getContextByName(contextName)).thenReturn(contextMock);
        when(entityManagerMock.find(ResourceEntity.class, 1)).thenReturn(resource);
        when(entityManagerMock.find(ResourceEntity.class, 2)).thenReturn(relatedResource);
        when(propertyValueServiceMock.decryptProperties(anyList())).thenReturn(properties);
        when(permissionBoundaryMock.hasPermission(Permission.RESOURCE, contextMock, Action.UPDATE, null, null)).thenReturn(false);
        when(permissionBoundaryMock.hasPermission(Permission.RESOURCE_PROPERTY_DECRYPT, contextMock, Action.ALL, resource, null)).thenReturn(true);
        when(permissionBoundaryMock.hasPermission(Permission.RESOURCE_PROPERTY_DECRYPT, contextMock, Action.ALL, relatedResource, null)).thenReturn(true);

        // when
        Assert.assertTrue(!resourcesByGroupNameWithRelations.isEmpty());
        Assert.assertTrue(!properties.isEmpty());
        editor.setPropertyValueOnAllResourceRelationsForContextWhereNotYetSet(resourceGroupName, relatedResourceGroupName, relatedResourceReleaseName, contextName, propertyName, propertyValue);

        // then
        verify(propertyValueServiceMock, never()).setPropertyValue(Matchers.any(ContextDependency.class), Matchers.anyInt(), Matchers.eq(propertyValue));

    }

    @Test
    public void setPropertyValueOnAllResourceRelationsForContextWhereNotYetSetShouldSetPropertyValueWhenNoValueSetAndUserHasAllRequiredPermissions() throws Exception {
        // given
        String resourceGroupName = "resourceGroupName";
        String relatedResourceGroupName = "relatedResourceGroupName";
        String relatedResourceReleaseName ="relatedResourceReleaseName";
        String contextName = "contextName";
        String propertyName = "propertyName";
        String propertyValue = "propertyValue";

        List<ResourceEditProperty> properties = new ArrayList<>();
        properties.add(new ResourceEditPropertyBuilder().withDisplayAndTechKeyName(propertyName).build());

        ResourceEntity relatedResource = createWithIdNameAndTypeName(2, relatedResourceGroupName, "relatedResourceType");
        ResourceEntity resource = createWithIdNameAndTypeNameWithRelations(1, resourceGroupName, "resourceType", relatedResource);

        List<ResourceEntity> resourcesByGroupNameWithRelations = new ArrayList<>();
        resourcesByGroupNameWithRelations.add(resource);

        ContextEntity contextMock = mock(ContextEntity.class);

        when(resourceLocatorMock.getResourcesByGroupNameWithRelations(resourceGroupName)).thenReturn(resourcesByGroupNameWithRelations);

        when(resourceLocatorMock.getResourceByGroupNameAndRelease(relatedResourceGroupName, relatedResourceReleaseName)).thenReturn(relatedResource);

        when(contextLocatorMock.getContextByName(contextName)).thenReturn(contextMock);
        when(entityManagerMock.find(ResourceEntity.class, 1)).thenReturn(resource);
        when(entityManagerMock.find(ResourceEntity.class, 2)).thenReturn(relatedResource);
        when(propertyValueServiceMock.decryptProperties(anyList())).thenReturn(properties);
        when(permissionBoundaryMock.hasPermission(Permission.RESOURCE, contextMock, Action.UPDATE, resource, null)).thenReturn(true);
        when(permissionBoundaryMock.hasPermission(Permission.RESOURCE_PROPERTY_DECRYPT, contextMock, Action.ALL, relatedResource, null)).thenReturn(true);

        setupMocksForSinglePropertiesModificationsFor(resource.getConsumedRelation(relatedResource));

        // when
        Assert.assertTrue(!resourcesByGroupNameWithRelations.isEmpty());
        Assert.assertTrue(!properties.isEmpty());
        editor.setPropertyValueOnAllResourceRelationsForContextWhereNotYetSet(resourceGroupName, relatedResourceGroupName, relatedResourceReleaseName, contextName, propertyName, propertyValue);

        // then
        verify(propertyValueServiceMock).setPropertyValue(Matchers.any(ContextDependency.class), Matchers.anyInt(), Matchers.eq(propertyValue));

    }


    @Test(expected = NullPointerException.class)
    public void savePropertyDescriptorForResourceWhenResourceIdIsNullShouldThrowException() throws ForeignableOwnerViolationException, AMWException {
        // given
        ForeignableOwner changingOwner = ForeignableOwner.AMW;
        Integer resourceId = null;
        PropertyDescriptorEntity descriptor = new PropertyDescriptorEntityBuilder().withId(2).build();
        String propertyTagsString = "propertyTagsString";

        // when
        editor.savePropertyDescriptorForResource(changingOwner, resourceId, descriptor, descriptor.foreignableFieldHashCode(), propertyTagsString);
    }

    @Test(expected = AMWException.class)
    public void savePropertyDescriptorForResourceWhenDublicateDescriptorNamesShouldThrowException() throws ForeignableOwnerViolationException, AMWException {
        // given
        ForeignableOwner changingOwner = ForeignableOwner.AMW;
        Integer resourceId = 1;
        PropertyDescriptorEntity descriptor = new PropertyDescriptorEntityBuilder().withId(2).build();
        String propertyTagsString = "propertyTagsString";

        ResourceEntity resourceEntityMock = mock(ResourceEntity.class);
        ResourceTypeEntity resourceTypeEntityMock = mock(ResourceTypeEntity.class);
        ResourceContextEntity resourceContextEntityMock = mock(ResourceContextEntity.class);
        ResourceTypeContextEntity resourceTypeContextEntityMock = mock(ResourceTypeContextEntity.class);

        when(entityManagerMock.find(ResourceEntity.class, resourceId)).thenReturn(resourceEntityMock);
        when(resourceEntityMock.getOrCreateContext(any(ContextEntity.class))).thenReturn(resourceContextEntityMock);
        when(resourceEntityMock.getResourceType()).thenReturn(resourceTypeEntityMock);
        when(resourceTypeEntityMock.getOrCreateContext(any(ContextEntity.class))).thenReturn(resourceTypeContextEntityMock);
        List<String> notEmptyList = new ArrayList<>();
        notEmptyList.add("dublicat property");

        when(propertyValidationServiceMock.getDuplicatePropertyDescriptors(resourceContextEntityMock, resourceTypeContextEntityMock, descriptor)).thenReturn(notEmptyList);

        // when
        editor.savePropertyDescriptorForResource(changingOwner, resourceId, descriptor, descriptor.foreignableFieldHashCode(), propertyTagsString);

        // then

    }


    @Test
    public void savePropertyDescriptorForResourceShouldDelegatePropertyTagEditingService() throws ForeignableOwnerViolationException, AMWException {
        // given
        ForeignableOwner changingOwner = ForeignableOwner.AMW;
        Integer resourceId = 1;
        PropertyDescriptorEntity descriptor = new PropertyDescriptorEntityBuilder().withId(2).build();
        String propertyTagsString = "propertyTagsString";

        ResourceEntity resourceEntityMock = mock(ResourceEntity.class);
        ResourceTypeEntity resourceTypeEntityMock = mock(ResourceTypeEntity.class);
        ResourceContextEntity resourceContextEntityMock = mock(ResourceContextEntity.class);
        ResourceTypeContextEntity resourceTypeContextEntityMock = mock(ResourceTypeContextEntity.class);

        when(entityManagerMock.find(ResourceEntity.class, resourceId)).thenReturn(resourceEntityMock);
        when(resourceEntityMock.getOrCreateContext(any(ContextEntity.class))).thenReturn(resourceContextEntityMock);
        when(resourceEntityMock.getResourceType()).thenReturn(resourceTypeEntityMock);
        when(resourceTypeEntityMock.getOrCreateContext(any(ContextEntity.class))).thenReturn(resourceTypeContextEntityMock);
        List<String> emptyList = new ArrayList<>();
        when(propertyValidationServiceMock.getDuplicatePropertyDescriptors(resourceContextEntityMock, resourceTypeContextEntityMock, descriptor)).thenReturn(emptyList);

        // when
        editor.savePropertyDescriptorForResource(changingOwner, resourceId, descriptor, descriptor.foreignableFieldHashCode(), propertyTagsString);

        // then
        verify(propertyTagEditingServiceMock).convertToTags(propertyTagsString);

    }

    @Test
    public void savePropertyDescriptorForResourceShouldSaveWithOwner() throws ForeignableOwnerViolationException, AMWException {
        // given
        ForeignableOwner changingOwner = ForeignableOwner.AMW;
        Integer resourceId = 1;
        PropertyDescriptorEntity descriptor = new PropertyDescriptorEntityBuilder().withId(2).build();
        String propertyTagsString = "propertyTagsString";

        ResourceEntity resourceEntityMock = mock(ResourceEntity.class);
        ResourceTypeEntity resourceTypeEntityMock = mock(ResourceTypeEntity.class);
        ResourceContextEntity resourceContextEntityMock = mock(ResourceContextEntity.class);
        ResourceTypeContextEntity resourceTypeContextEntityMock = mock(ResourceTypeContextEntity.class);

        when(entityManagerMock.find(ResourceEntity.class, resourceId)).thenReturn(resourceEntityMock);
        when(resourceEntityMock.getOrCreateContext(any(ContextEntity.class))).thenReturn(resourceContextEntityMock);
        when(resourceEntityMock.getResourceType()).thenReturn(resourceTypeEntityMock);
        when(resourceTypeEntityMock.getOrCreateContext(any(ContextEntity.class))).thenReturn(resourceTypeContextEntityMock);
        List<String> emptyList = new ArrayList<>();
        when(propertyValidationServiceMock.getDuplicatePropertyDescriptors(resourceContextEntityMock, resourceTypeContextEntityMock, descriptor)).thenReturn(emptyList);
        when(permissionBoundaryMock.hasPermission(Permission.IGNORE_FOREIGNABLE_OWNER)).thenReturn(false);
        List<PropertyTagEntity> propertyTags = new ArrayList<>();
        propertyTags.add(new PropertyTagEntity());
        when(propertyTagEditingServiceMock.convertToTags(propertyTagsString)).thenReturn(propertyTags);

        // when
        editor.savePropertyDescriptorForResource(changingOwner, resourceId, descriptor, descriptor.foreignableFieldHashCode(), propertyTagsString);

        // then
        verify(propertyDescriptorServiceMock).savePropertyDescriptorForOwner(changingOwner, resourceContextEntityMock, descriptor, propertyTags, resourceEntityMock);
    }

    @Test
    public void savePropertyDescriptorForResourceShouldVerifyIfEditableByOwner() throws ForeignableOwnerViolationException, AMWException {
        // given
        ForeignableOwner changingOwner = ForeignableOwner.AMW;
        Integer resourceId = 1;
        PropertyDescriptorEntity descriptor = new PropertyDescriptorEntityBuilder().withId(2).build();
        String propertyTagsString = "propertyTagsString";

        ResourceEntity resourceEntityMock = mock(ResourceEntity.class);
        ResourceTypeEntity resourceTypeEntityMock = mock(ResourceTypeEntity.class);
        ResourceContextEntity resourceContextEntityMock = mock(ResourceContextEntity.class);
        ResourceTypeContextEntity resourceTypeContextEntityMock = mock(ResourceTypeContextEntity.class);

        when(entityManagerMock.find(ResourceEntity.class, resourceId)).thenReturn(resourceEntityMock);
        when(resourceEntityMock.getOrCreateContext(any(ContextEntity.class))).thenReturn(resourceContextEntityMock);
        when(resourceEntityMock.getResourceType()).thenReturn(resourceTypeEntityMock);
        when(resourceTypeEntityMock.getOrCreateContext(any(ContextEntity.class))).thenReturn(resourceTypeContextEntityMock);
        List<String> emptyList = new ArrayList<>();
        when(propertyValidationServiceMock.getDuplicatePropertyDescriptors(resourceContextEntityMock, resourceTypeContextEntityMock, descriptor)).thenReturn(emptyList);
        when(permissionBoundaryMock.hasPermission(Permission.IGNORE_FOREIGNABLE_OWNER)).thenReturn(false);
        List<PropertyTagEntity> propertyTags = new ArrayList<>();
        propertyTags.add(new PropertyTagEntity());
        when(propertyTagEditingServiceMock.convertToTags(propertyTagsString)).thenReturn(propertyTags);

        // when
        editor.savePropertyDescriptorForResource(changingOwner, resourceId, descriptor, descriptor.foreignableFieldHashCode(), propertyTagsString);

        // then
        verify(foreignableServiceMock).verifyEditableByOwner(changingOwner, descriptor.foreignableFieldHashCode(),descriptor);

    }


    @Test
    public void saveTestingPropertyDescriptorForResourceShouldSaveAsSystemOwner() throws ForeignableOwnerViolationException, AMWException {
        // given
        ForeignableOwner changingOwner = ForeignableOwner.AMW;
        Integer resourceId = 1;
        PropertyDescriptorEntity descriptor = new PropertyDescriptorEntityBuilder().withId(2).build();
        String propertyTagsString = "propertyTagsString";

        ResourceEntity resourceEntityMock = mock(ResourceEntity.class);
        ResourceTypeEntity resourceTypeEntityMock = mock(ResourceTypeEntity.class);
        ResourceContextEntity resourceContextEntityMock = mock(ResourceContextEntity.class);
        ResourceTypeContextEntity resourceTypeContextEntityMock = mock(ResourceTypeContextEntity.class);

        when(entityManagerMock.find(ResourceEntity.class, resourceId)).thenReturn(resourceEntityMock);
        when(resourceEntityMock.getOrCreateContext(any(ContextEntity.class))).thenReturn(resourceContextEntityMock);
        when(resourceEntityMock.getResourceType()).thenReturn(resourceTypeEntityMock);
        when(resourceTypeEntityMock.getOrCreateContext(any(ContextEntity.class))).thenReturn(resourceTypeContextEntityMock);
        List<String> emptyList = new ArrayList<>();
        when(propertyValidationServiceMock.getDuplicatePropertyDescriptors(resourceContextEntityMock, resourceTypeContextEntityMock, descriptor)).thenReturn(emptyList);
        when(permissionBoundaryMock.hasPermission(Permission.IGNORE_FOREIGNABLE_OWNER)).thenReturn(false);
        List<PropertyTagEntity> propertyTags = new ArrayList<>();
        propertyTags.add(new PropertyTagEntity());
        when(propertyTagEditingServiceMock.convertToTags(propertyTagsString)).thenReturn(propertyTags);

        // when
        editor.saveTestingPropertyDescriptorForResource(resourceId, descriptor,descriptor.foreignableFieldHashCode(), propertyTagsString);

        // then
        verify(propertyDescriptorServiceMock).savePropertyDescriptorForOwner(ForeignableOwner.getSystemOwner(), resourceContextEntityMock, descriptor, propertyTags, resourceEntityMock);
    }


    @Test
    public void saveTestingPropertyDescriptorForResourceShouldVerifyIfEditableBySystemOwner() throws ForeignableOwnerViolationException, AMWException {
        // given
        ForeignableOwner changingOwner = ForeignableOwner.AMW;
        Integer resourceId = 1;
        PropertyDescriptorEntity descriptor = new PropertyDescriptorEntityBuilder().withId(2).build();
        String propertyTagsString = "propertyTagsString";

        ResourceEntity resourceEntityMock = mock(ResourceEntity.class);
        ResourceTypeEntity resourceTypeEntityMock = mock(ResourceTypeEntity.class);
        ResourceContextEntity resourceContextEntityMock = mock(ResourceContextEntity.class);
        ResourceTypeContextEntity resourceTypeContextEntityMock = mock(ResourceTypeContextEntity.class);

        when(entityManagerMock.find(ResourceEntity.class, resourceId)).thenReturn(resourceEntityMock);
        when(resourceEntityMock.getOrCreateContext(any(ContextEntity.class))).thenReturn(resourceContextEntityMock);
        when(resourceEntityMock.getResourceType()).thenReturn(resourceTypeEntityMock);
        when(resourceTypeEntityMock.getOrCreateContext(any(ContextEntity.class))).thenReturn(resourceTypeContextEntityMock);
        List<String> emptyList = new ArrayList<>();
        when(propertyValidationServiceMock.getDuplicatePropertyDescriptors(resourceContextEntityMock, resourceTypeContextEntityMock, descriptor)).thenReturn(emptyList);
        when(permissionBoundaryMock.hasPermission(Permission.IGNORE_FOREIGNABLE_OWNER)).thenReturn(false);
        List<PropertyTagEntity> propertyTags = new ArrayList<>();
        propertyTags.add(new PropertyTagEntity());
        when(propertyTagEditingServiceMock.convertToTags(propertyTagsString)).thenReturn(propertyTags);

        // when
        editor.saveTestingPropertyDescriptorForResource(resourceId, descriptor, descriptor.foreignableFieldHashCode(), propertyTagsString);

        // then
        verify(foreignableServiceMock).verifyEditableByOwner(ForeignableOwner.getSystemOwner(), descriptor.foreignableFieldHashCode(), descriptor);
    }


    @Test
    public void savePropertyDescriptorForResourceTypeShouldSaveWithOwner() throws ForeignableOwnerViolationException, AMWException {
        // given
        ForeignableOwner changingOwner = ForeignableOwner.AMW;
        Integer resourceTypeId = 1;
        PropertyDescriptorEntity descriptor = new PropertyDescriptorEntityBuilder().withId(2).build();
        String propertyTagsString = "propertyTagsString";

        ResourceTypeEntity resourceTypeEntityMock = mock(ResourceTypeEntity.class);
        ResourceTypeContextEntity resourceTypeContextEntityMock = mock(ResourceTypeContextEntity.class);

        when(entityManagerMock.find(ResourceTypeEntity.class, resourceTypeId)).thenReturn(resourceTypeEntityMock);
        when(resourceTypeEntityMock.getOrCreateContext(any(ContextEntity.class))).thenReturn(resourceTypeContextEntityMock);

        when(permissionBoundaryMock.hasPermission(Permission.IGNORE_FOREIGNABLE_OWNER)).thenReturn(false);
        List<PropertyTagEntity> propertyTags = new ArrayList<>();
        propertyTags.add(new PropertyTagEntity());
        when(propertyTagEditingServiceMock.convertToTags(propertyTagsString)).thenReturn(propertyTags);

        // when
        editor.savePropertyDescriptorForResourceType(changingOwner, resourceTypeId, descriptor,descriptor.foreignableFieldHashCode(), propertyTagsString);

        // then
        verify(propertyDescriptorServiceMock).savePropertyDescriptorForOwner(changingOwner, resourceTypeContextEntityMock, descriptor, propertyTags, resourceTypeEntityMock);
    }

    @Test
    public void savePropertyDescriptorForResourceTypeShouldVerifyIfEditableByOwner() throws ForeignableOwnerViolationException, AMWException {
        // given
        ForeignableOwner changingOwner = ForeignableOwner.AMW;
        Integer resourceTypeId = 1;
        PropertyDescriptorEntity descriptor = new PropertyDescriptorEntityBuilder().withId(2).build();
        String propertyTagsString = "propertyTagsString";

        ResourceTypeEntity resourceTypeEntityMock = mock(ResourceTypeEntity.class);
        ResourceTypeContextEntity resourceTypeContextEntityMock = mock(ResourceTypeContextEntity.class);

        when(entityManagerMock.find(ResourceTypeEntity.class, resourceTypeId)).thenReturn(resourceTypeEntityMock);
        when(resourceTypeEntityMock.getOrCreateContext(any(ContextEntity.class))).thenReturn(resourceTypeContextEntityMock);

        when(permissionBoundaryMock.hasPermission(Permission.IGNORE_FOREIGNABLE_OWNER)).thenReturn(false);
        List<PropertyTagEntity> propertyTags = new ArrayList<>();
        propertyTags.add(new PropertyTagEntity());
        when(propertyTagEditingServiceMock.convertToTags(propertyTagsString)).thenReturn(propertyTags);

        // when
        editor.savePropertyDescriptorForResourceType(changingOwner, resourceTypeId, descriptor,descriptor.foreignableFieldHashCode(), propertyTagsString);

        // then
        verify(foreignableServiceMock).verifyEditableByOwner(changingOwner, descriptor.foreignableFieldHashCode(), descriptor );
    }


    @Test
    public void saveTestingPropertyDescriptorForResourceTypeShouldSaveAsSystemOwner() throws ForeignableOwnerViolationException, AMWException {
        // given
        ForeignableOwner changingOwner = ForeignableOwner.AMW;
        Integer resourceTypeId = 1;
        PropertyDescriptorEntity descriptor = new PropertyDescriptorEntityBuilder().withId(2).build();
        String propertyTagsString = "propertyTagsString";

        ResourceTypeEntity resourceTypeEntityMock = mock(ResourceTypeEntity.class);
        ResourceTypeContextEntity resourceTypeContextEntityMock = mock(ResourceTypeContextEntity.class);

        when(entityManagerMock.find(ResourceTypeEntity.class, resourceTypeId)).thenReturn(resourceTypeEntityMock);
        when(resourceTypeEntityMock.getOrCreateContext(any(ContextEntity.class))).thenReturn(resourceTypeContextEntityMock);

        when(permissionBoundaryMock.hasPermission(Permission.IGNORE_FOREIGNABLE_OWNER)).thenReturn(false);
        List<PropertyTagEntity> propertyTags = new ArrayList<>();
        propertyTags.add(new PropertyTagEntity());
        when(propertyTagEditingServiceMock.convertToTags(propertyTagsString)).thenReturn(propertyTags);

        // when
        editor.saveTestingPropertyDescriptorForResourceType(resourceTypeId, descriptor,descriptor.foreignableFieldHashCode(), propertyTagsString);

        // then
        verify(propertyDescriptorServiceMock).savePropertyDescriptorForOwner(ForeignableOwner.getSystemOwner(), resourceTypeContextEntityMock, descriptor, propertyTags, resourceTypeEntityMock);
    }

    @Test
    public void saveTestingPropertyDescriptorForResourceTypeShouldVerifyIfEditableBySystemOwner() throws ForeignableOwnerViolationException, AMWException {
        // given
        ForeignableOwner changingOwner = ForeignableOwner.AMW;
        Integer resourceTypeId = 1;
        PropertyDescriptorEntity descriptor = new PropertyDescriptorEntityBuilder().withId(2).build();
        String propertyTagsString = "propertyTagsString";

        ResourceTypeEntity resourceTypeEntityMock = mock(ResourceTypeEntity.class);
        ResourceTypeContextEntity resourceTypeContextEntityMock = mock(ResourceTypeContextEntity.class);

        when(entityManagerMock.find(ResourceTypeEntity.class, resourceTypeId)).thenReturn(resourceTypeEntityMock);
        when(resourceTypeEntityMock.getOrCreateContext(any(ContextEntity.class))).thenReturn(resourceTypeContextEntityMock);

        when(permissionBoundaryMock.hasPermission(Permission.IGNORE_FOREIGNABLE_OWNER)).thenReturn(false);
        List<PropertyTagEntity> propertyTags = new ArrayList<>();
        propertyTags.add(new PropertyTagEntity());
        when(propertyTagEditingServiceMock.convertToTags(propertyTagsString)).thenReturn(propertyTags);

        // when
        editor.saveTestingPropertyDescriptorForResourceType(resourceTypeId, descriptor,descriptor.foreignableFieldHashCode(), propertyTagsString);

        // then
        verify(foreignableServiceMock).verifyEditableByOwner(ForeignableOwner.getSystemOwner(), descriptor.foreignableFieldHashCode(), descriptor );
    }

    @Test
    public void deletePropertyDescriptorForResourceShouldVerifyIfDeletableByOwner() throws ForeignableOwnerViolationException, AMWException {
        // given
        ForeignableOwner deletingOwner = ForeignableOwner.AMW;
        Integer resourceId = 1;
        PropertyDescriptorEntity descriptor = new PropertyDescriptorEntityBuilder().withId(2).build();

        ResourceEntity resourceEntityMock = mock(ResourceEntity.class);
        ResourceContextEntity resourceContextEntityMock = mock(ResourceContextEntity.class);

        when(entityManagerMock.find(ResourceEntity.class, resourceId)).thenReturn(resourceEntityMock);
        when(resourceEntityMock.getOrCreateContext(any(ContextEntity.class))).thenReturn(resourceContextEntityMock);
        when(permissionBoundaryMock.hasPermission(Permission.IGNORE_FOREIGNABLE_OWNER)).thenReturn(false);

        // when
        editor.deletePropertyDescriptorForResource(deletingOwner, resourceId, descriptor);

        // then
        verify(foreignableServiceMock).verifyDeletableByOwner(deletingOwner, descriptor);
    }


    @Test
    public void deletePropertyDescriptorForResourceTypeShouldVerifyIfDeletableByOwner() throws ForeignableOwnerViolationException, AMWException {
        // given
        ForeignableOwner deletingOwner = ForeignableOwner.AMW;
        Integer resourceId = 1;
        PropertyDescriptorEntity descriptor = new PropertyDescriptorEntityBuilder().withId(2).build();

        ResourceTypeEntity resourceTypeEntityMock = mock(ResourceTypeEntity.class);
        ResourceTypeContextEntity resourceTypeContextEntityMock = mock(ResourceTypeContextEntity.class);

        when(entityManagerMock.find(ResourceTypeEntity.class, resourceId)).thenReturn(resourceTypeEntityMock);
        when(resourceTypeEntityMock.getOrCreateContext(any(ContextEntity.class))).thenReturn(resourceTypeContextEntityMock);
        when(permissionBoundaryMock.hasPermission(Permission.IGNORE_FOREIGNABLE_OWNER)).thenReturn(false);

        // when
        editor.deletePropertyDescriptorForResourceType(deletingOwner, resourceId, descriptor);

        // then
        verify(foreignableServiceMock).verifyDeletableByOwner(deletingOwner, descriptor);
    }

    private ResourceEntity createWithIdNameAndTypeNameWithRelations(int i, String resourceGroupName, String resourceType, ResourceEntity relatedResource) {
        ResourceEntity resource = createWithIdNameAndTypeName(i, resourceGroupName, resourceType);

        ConsumedResourceRelationEntity relation = new ResourceRelationEntityBuilder().buildConsumedResRelEntity(resource, relatedResource, "identifier", null);

        resource.addConsumedRelation(relation);
        return resource;
    }

}
