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

package ch.puzzle.itc.mobiliar.business.property.control;

import ch.puzzle.itc.mobiliar.builders.PropertyDescriptorEntityBuilder;
import ch.puzzle.itc.mobiliar.business.environment.control.ContextDomainService;
import ch.puzzle.itc.mobiliar.business.environment.entity.AbstractContext;
import ch.puzzle.itc.mobiliar.business.foreignable.control.ForeignableService;
import ch.puzzle.itc.mobiliar.business.foreignable.entity.ForeignableOwner;
import ch.puzzle.itc.mobiliar.business.property.entity.PropertyDescriptorEntity;
import ch.puzzle.itc.mobiliar.business.property.entity.PropertyEntity;
import ch.puzzle.itc.mobiliar.business.property.entity.PropertyTagEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceTypeEntity;
import ch.puzzle.itc.mobiliar.business.security.control.PermissionService;
import ch.puzzle.itc.mobiliar.business.security.entity.Permission;
import ch.puzzle.itc.mobiliar.business.utils.ThreadLocalUtil;
import ch.puzzle.itc.mobiliar.common.exception.AMWException;
import ch.puzzle.itc.mobiliar.common.exception.NotAuthorizedException;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.*;


public class PropertyDescriptorServiceTest {

    @Mock
    EntityManager entityManagerMock;

    @Mock
    PermissionService permissionServiceMock;

    @Mock
    ContextDomainService contextServiceMock;

    @Mock
    PropertyValidationService propertyValidationServiceMock;

    @Mock
    PropertyTagEditingService propertyTagEditingServiceMock;

    @Mock
    ForeignableService foreignableServiceMock;

    @InjectMocks
    @Spy
    PropertyDescriptorService service = spy(new PropertyDescriptorService());

    int dummyResourceId = 100;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testManageChangeOfEncryptedPropertyDescriptorDecrypt() throws Exception {
        testManageChangeOfEncryptedPropertyDescriptor(Boolean.FALSE, true);
    }

    @Test
    public void testManageChangeOfEncryptedPropertyDescriptorEncrypt() throws Exception {
        testManageChangeOfEncryptedPropertyDescriptor(Boolean.TRUE, true);
    }

    @Test
    public void testManageChangeOfEncryptedPropertyDescriptorUnchanged() throws Exception {
        testManageChangeOfEncryptedPropertyDescriptor(null, true);
    }

    /**
     * We expect a @link{NotAuthorizedException} since the user tries to decrypt the property although permission is not given
     */
    @Test(expected = NotAuthorizedException.class)
    public void testManageChangeOfEncryptedPropertyDescriptorDecryptNotAuthorized() throws Exception {
        testManageChangeOfEncryptedPropertyDescriptor(Boolean.FALSE, false);
    }

    /**
     * We don't expect an exception since encryption is allowed
     */
    public void testManageChangeOfEncryptedPropertyDescriptorEncryptNotAuthorized() throws Exception {
        testManageChangeOfEncryptedPropertyDescriptor(Boolean.TRUE, false);
    }
    /**
     * We don't expect an exception since nothing is changed
     */
    @Test
    public void testManageChangeOfEncryptedPropertyDescriptorNotAuthorizedWithoutChange() throws Exception {
        testManageChangeOfEncryptedPropertyDescriptor(null, false);
    }

    void testManageChangeOfEncryptedPropertyDescriptor(Boolean encrypt, boolean hasPermission) throws Exception {
        TypedQuery queryMock = mock(TypedQuery.class);
        when(entityManagerMock.createQuery(anyString(), any(Class.class))).thenReturn(queryMock);
        when(queryMock.setParameter(anyString(), any())).thenReturn(queryMock);
        if(hasPermission){
            doNothing().when(permissionServiceMock).checkPermissionAndFireException(any(Permission.class), anyString());
        }
        else{
            doThrow(NotAuthorizedException.class).when(permissionServiceMock).checkPermissionAndFireException(any(Permission.class), anyString());
        }
        PropertyEntity p = mock(PropertyEntity.class);
        when(queryMock.getResultList()).thenReturn(Arrays.asList(p));
        PropertyDescriptorEntity propertyDescriptorEntity = new PropertyDescriptorEntity();
        if(encrypt!=null){
            propertyDescriptorEntity.setEncrypt(encrypt);
        }
        propertyDescriptorEntity.setId(1);

        List<Integer> encryptedProperties;
        if(encrypt==null || encrypt){
               encryptedProperties = Collections.emptyList();
        }
        else{
            encryptedProperties = Arrays.asList(propertyDescriptorEntity.getId());
        }
        service.manageChangeOfEncryptedPropertyDescriptor(propertyDescriptorEntity, encryptedProperties, hasPermission);
        if(encrypt!=null){
            if(encrypt){
                verify(p, times(0)).decrypt();
                verify(p, times(1)).encrypt();
            }
            else{
                verify(p, times(1)).decrypt();
                verify(p, times(0)).encrypt();
            }
        }
        else{
            verify(p, times(0)).decrypt();
            verify(p, times(0)).encrypt();
        }

    }


    @Test(expected = AMWException.class)
    public void savePropertyDescriptorForOwnerWhenDescriptorIsNullShouldThrowException() throws AMWException {
        // given
        ForeignableOwner changingOwner = ForeignableOwner.AMW;

        AbstractContext abstractContextMock = mock(AbstractContext.class);
        PropertyDescriptorEntity descriptor = null;
        List<PropertyTagEntity> tags = new ArrayList<>();
        ResourceEntity resourceEntityMock = mock(ResourceEntity.class);

        // when
        service.savePropertyDescriptorForOwner(changingOwner, abstractContextMock, descriptor, tags, resourceEntityMock );
    }

    @Test(expected = AMWException.class)
    public void savePropertyDescriptorForOwnerWhenTechnicalKeyIsInvalidShouldThrowException() throws AMWException {
        // given
        ForeignableOwner changingOwner = ForeignableOwner.AMW;

        AbstractContext abstractContextMock = mock(AbstractContext.class);
        PropertyDescriptorEntity descriptor = new PropertyDescriptorEntityBuilder().build();;
        List<PropertyTagEntity> tags = new ArrayList<>();
        Assert.assertNull(descriptor.getId());
        ResourceEntity resourceEntityMock = mock(ResourceEntity.class);
        when(propertyValidationServiceMock.isValidTechnicalKey(descriptor.getPropertyName())).thenReturn(false);

        // when
        service.savePropertyDescriptorForOwner(changingOwner, abstractContextMock, descriptor, tags, resourceEntityMock);
    }

    @Test
    public void savePropertyDescriptorForOwnerWhenDescriptorIdIsNullShouldCreateNewPropertyDescriptorForOwner() throws AMWException {
        // given
        ForeignableOwner changingOwner = ForeignableOwner.AMW;

        AbstractContext abstractContextMock = mock(AbstractContext.class);
        PropertyDescriptorEntity descriptor = new PropertyDescriptorEntityBuilder().build();
        List<PropertyTagEntity> tags = new ArrayList<>();
        ResourceEntity resourceEntityMock = mock(ResourceEntity.class);
        when(propertyValidationServiceMock.isValidTechnicalKey(descriptor.getPropertyName())).thenReturn(true);
        Assert.assertNull(descriptor.getId());

        // when
        service.savePropertyDescriptorForOwner(changingOwner, abstractContextMock, descriptor, tags, resourceEntityMock);

        // then
        verify(abstractContextMock).addPropertyDescriptor(descriptor);
        verify(entityManagerMock).persist(descriptor);
        verify(propertyTagEditingServiceMock).updateTags(tags, descriptor);
        verify(entityManagerMock).persist(abstractContextMock);
    }

    @Test(expected = AMWException.class)
    public void savePropertyDescriptorShouldNotCreateMultiplePropertyDescriptorsWithSameName() throws AMWException {
        // given
        ForeignableOwner changingOwner = ForeignableOwner.AMW;

        PropertyDescriptorEntity descriptor = new PropertyDescriptorEntityBuilder().withPropertyName("existing").build();
        AbstractContext abstractContextMock = mock(AbstractContext.class);
        PropertyDescriptorEntity newDescriptor = new PropertyDescriptorEntityBuilder().withPropertyName("existing").build();
        List<PropertyTagEntity> tags = new ArrayList<>();
        ResourceEntity resourceEntityMock = mock(ResourceEntity.class);
        when(propertyValidationServiceMock.isValidTechnicalKey(descriptor.getPropertyName())).thenReturn(true);
        when(abstractContextMock.getPropertyDescriptors()).thenReturn(new HashSet<>(Collections.singletonList(descriptor)));

        // when
        service.savePropertyDescriptorForOwner(changingOwner, abstractContextMock, newDescriptor, tags, resourceEntityMock);
    }

    @Test
    public void savePropertyDescriptorForOwnerWhenDescriptorIdIsNotNullAndSameOwnerShouldSavePropertyDescriptor() throws AMWException {
        // given
        ForeignableOwner changingOwner = ForeignableOwner.AMW;

        AbstractContext abstractContextMock = mock(AbstractContext.class);
        PropertyDescriptorEntity descriptor = new PropertyDescriptorEntityBuilder().withId(1).withOwner(changingOwner).build();
        List<PropertyTagEntity> tags = new ArrayList<>();
        when(propertyValidationServiceMock.isValidTechnicalKey(descriptor.getPropertyName())).thenReturn(true);
        Assert.assertNotNull(descriptor.getId());
        // mock (implicit verify for merge) merging of descriptor
        PropertyDescriptorEntity mergedPropertyDescriptorMock = mock(PropertyDescriptorEntity.class);
        when(entityManagerMock.merge(descriptor)).thenReturn(mergedPropertyDescriptorMock);

        // mocking the manageChangeOfEncryptedPropertyDescriptor
        when(entityManagerMock.find(PropertyDescriptorEntity.class, descriptor.getId())).thenReturn(descriptor);
        when(entityManagerMock.createQuery("select p from PropertyEntity p where p.descriptor=:descriptor", PropertyEntity.class)).thenReturn(mock(TypedQuery.class));
        ResourceEntity resourceEntityMock = mock(ResourceEntity.class);

        // when
        service.savePropertyDescriptorForOwner(changingOwner, abstractContextMock, descriptor, tags, resourceEntityMock);

        // then
        verify(propertyTagEditingServiceMock).updateTags(tags, mergedPropertyDescriptorMock);
    }

    @Test
    public void savePropertyDescriptorForOwnerWhenDescriptorIdIsNotNullNoForeignableFieldsChangedButDifferentOwnerShouldSavePropertyDescriptor() throws AMWException {
        // given
        ForeignableOwner changingOwner = ForeignableOwner.AMW;

        AbstractContext abstractContextMock = mock(AbstractContext.class);
        PropertyDescriptorEntity descriptor = new PropertyDescriptorEntityBuilder().withId(1).withOwner(ForeignableOwner.MAIA).build();
        List<PropertyTagEntity> tags = new ArrayList<>();
        when(propertyValidationServiceMock.isValidTechnicalKey(descriptor.getPropertyName())).thenReturn(true);
        Assert.assertNotNull(descriptor.getId());

        // mock (implicit verify for merge) merging of descriptor
        PropertyDescriptorEntity mergedPropertyDescriptorMock = mock(PropertyDescriptorEntity.class);
        when(entityManagerMock.merge(descriptor)).thenReturn(mergedPropertyDescriptorMock);

        // mocking the manageChangeOfEncryptedPropertyDescriptor
        when(entityManagerMock.createQuery("select p from PropertyEntity p where p.descriptor=:descriptor", PropertyEntity.class)).thenReturn(mock(TypedQuery.class));

        // return descriptor with same values (not changed fields)
        when(entityManagerMock.find(PropertyDescriptorEntity.class, descriptor.getId())).thenReturn(descriptor);
        ResourceEntity resourceEntityMock = mock(ResourceEntity.class);

        // when
        service.savePropertyDescriptorForOwner(changingOwner, abstractContextMock, descriptor, tags, resourceEntityMock);

        // then
        verify(propertyTagEditingServiceMock).updateTags(tags, mergedPropertyDescriptorMock);
    }


    @Test
    public void deletePropertyDescriptorByOwnerWhenDeletingOwnerIsOwnerOfDescriptorAndNoPropertiesShouldDeletePropertyDescriptor() throws AMWException {
        // given
        ForeignableOwner deletingOwner = ForeignableOwner.AMW;

        AbstractContext abstractContextMock = mock(AbstractContext.class);
        PropertyDescriptorEntity descriptor = new PropertyDescriptorEntityBuilder().withOwner(deletingOwner).withId(1).build();
        Assert.assertEquals(deletingOwner, descriptor.getOwner());

        TypedQuery<PropertyDescriptorEntity> queryMock = mock(TypedQuery.class);
        when(entityManagerMock.createQuery("from PropertyDescriptorEntity d  left join fetch d.propertyTags where d.id = :propertyDescriptorId ", PropertyDescriptorEntity.class)).thenReturn(queryMock);
        when(queryMock.getSingleResult()).thenReturn(descriptor);

        // when
        service.deletePropertyDescriptorByOwnerInResourceContext(descriptor, abstractContextMock, dummyResourceId);

        // then
        verify(entityManagerMock).remove(descriptor);
    }


    @Test(expected = AMWException.class)
    public void deletePropertyDescriptorByOwnerWhenDeletingOwnerIsOwnerOfDescriptorButHasPropertiesShouldThrowException() throws AMWException {
        // given
        ForeignableOwner deletingOwner = ForeignableOwner.AMW;

        AbstractContext abstractContextMock = mock(AbstractContext.class);
        Set<PropertyEntity> properties = new HashSet<>();
        properties.add(new PropertyEntity());
        PropertyDescriptorEntity descriptor = new PropertyDescriptorEntityBuilder().withOwner(deletingOwner).withId(1).withProperties(properties).build();
        Assert.assertEquals(deletingOwner, descriptor.getOwner());

        TypedQuery<PropertyDescriptorEntity> queryMock = mock(TypedQuery.class);
        when(entityManagerMock.createQuery("from PropertyDescriptorEntity d  left join fetch d.propertyTags where d.id = :propertyDescriptorId ", PropertyDescriptorEntity.class)).thenReturn(queryMock);
        when(queryMock.getSingleResult()).thenReturn(descriptor);

        // when
        service.deletePropertyDescriptorByOwnerInResourceContext(descriptor, abstractContextMock, dummyResourceId);
    }

    @Test
    public void deletePropertyDescriptorByOwnerIncludingPropertyValuesWhenDeletingOwnerIsOwnerOfDescriptorWithPropertiesShouldSucceed() throws AMWException {
        // given
        ForeignableOwner deletingOwner = ForeignableOwner.AMW;

        AbstractContext abstractContextMock = mock(AbstractContext.class);
        ResourceEntity resourceEntityMock = mock(ResourceEntity.class);
        Set<PropertyEntity> properties = new HashSet<>();
        properties.add(new PropertyEntity());
        PropertyDescriptorEntity descriptor = new PropertyDescriptorEntityBuilder().withOwner(deletingOwner).withId(1).withProperties(properties).build();
        Assert.assertEquals(deletingOwner, descriptor.getOwner());

        TypedQuery<PropertyDescriptorEntity> queryMock = mock(TypedQuery.class);
        when(entityManagerMock.createQuery("from PropertyDescriptorEntity d  left join fetch d.propertyTags where d.id = :propertyDescriptorId ", PropertyDescriptorEntity.class)).thenReturn(queryMock);
        when(queryMock.getSingleResult()).thenReturn(descriptor);

        // when
        service.deletePropertyDescriptorByOwnerIncludingPropertyValues(descriptor, abstractContextMock, resourceEntityMock);

        // then
        verify(entityManagerMock).remove(descriptor);
    }

    @Test
    public void deletePropertyDescriptorByOwnerWhenDeletingOwnerIsOwnerOfDescriptorWithTagsAndNoPropertiesShouldDeleteAllTags() throws AMWException {
        // given
        ForeignableOwner deletingOwner = ForeignableOwner.AMW;

        AbstractContext abstractContextMock = mock(AbstractContext.class);
        PropertyTagEntity tag1 = new PropertyTagEntity();
        tag1.setId(1);
        PropertyTagEntity tag2 = new PropertyTagEntity();
        tag1.setId(2);
        PropertyDescriptorEntity descriptor = new PropertyDescriptorEntityBuilder().withOwner(deletingOwner).withTags(tag1, tag2).withId(1).build();
        Assert.assertEquals(deletingOwner, descriptor.getOwner());


        TypedQuery<PropertyDescriptorEntity> queryMock = mock(TypedQuery.class);
        when(entityManagerMock.createQuery("from PropertyDescriptorEntity d  left join fetch d.propertyTags where d.id = :propertyDescriptorId ", PropertyDescriptorEntity.class)).thenReturn(queryMock);
        when(queryMock.getSingleResult()).thenReturn(descriptor);

        when(entityManagerMock.find(PropertyDescriptorEntity.class, descriptor.getId())).thenReturn(descriptor);
        when(entityManagerMock.find(PropertyTagEntity.class, tag1.getId())).thenReturn(tag1);
        when(entityManagerMock.find(PropertyTagEntity.class, tag2.getId())).thenReturn(tag2);


        // when
        service.deletePropertyDescriptorByOwnerInResourceContext(descriptor, abstractContextMock, dummyResourceId);

        // then
        verify(entityManagerMock).remove(tag1);
        verify(entityManagerMock).remove(tag2);
    }

    @Test
    public void shouldStoreResourceIdInThreadLocalForDuringPropertyUpdateInResourceContext() throws AMWException {
        // given
        Integer resourceIdForAuditLog = 200;
        ForeignableOwner deletingOwner = ForeignableOwner.AMW;
        Set<PropertyEntity> properties = new HashSet<>();
        properties.add(new PropertyEntity());
        PropertyDescriptorEntity descriptor = new PropertyDescriptorEntityBuilder().withOwner(deletingOwner).withId(1).withProperties(properties).build();
        AbstractContext abstractContextMock = mock(AbstractContext.class);
        doNothing().when(service).removePropertyDescriptorByOwner(eq(descriptor), eq(abstractContextMock), anyBoolean());
        doReturn(descriptor).when(service).getPropertyDescriptorWithTags(anyInt());

        // when
        service.deletePropertyDescriptorByOwnerInResourceContext(descriptor, abstractContextMock, resourceIdForAuditLog);

        // then
        assertThat("The resourceId Param must be stored as ThreadLocal variable for auditing (envers)",
                ThreadLocalUtil.getThreadVariable(ThreadLocalUtil.KEY_RESOURCE_ID), is(CoreMatchers.notNullValue()));
        int resourceId = (int) ThreadLocalUtil.getThreadVariable(ThreadLocalUtil.KEY_RESOURCE_ID);
        assertThat(resourceId, is(resourceIdForAuditLog));
    }

    @Test
    public void shouldStoreResourceTypeIdInThreadLocalDuringPropertyUpdateInResourceTypeContext() throws AMWException {
        // given
        Integer resourceTypeIdForAuditLog = 1;
        ForeignableOwner deletingOwner = ForeignableOwner.AMW;
        Set<PropertyEntity> properties = new HashSet<>();
        properties.add(new PropertyEntity());
        PropertyDescriptorEntity descriptor = new PropertyDescriptorEntityBuilder().withOwner(deletingOwner).withId(1).withProperties(properties).build();
        AbstractContext abstractContextMock = mock(AbstractContext.class);
        doNothing().when(service).removePropertyDescriptorByOwner(eq(descriptor), eq(abstractContextMock), anyBoolean());
        doReturn(descriptor).when(service).getPropertyDescriptorWithTags(anyInt());

        // when
        service.deletePropertyDescriptorByOwnerInResourceTypeContext(descriptor, abstractContextMock, resourceTypeIdForAuditLog);

        // then
        assertThat("The resourceTypeId Param must be stored as ThreadLocal variable for auditing (envers)",
                ThreadLocalUtil.getThreadVariable(ThreadLocalUtil.KEY_RESOURCE_TYPE_ID), is(CoreMatchers.notNullValue()));
        int resourceTypeId = (int) ThreadLocalUtil.getThreadVariable(ThreadLocalUtil.KEY_RESOURCE_TYPE_ID);
        assertThat(resourceTypeId, is(resourceTypeIdForAuditLog));
    }

    @Test
    public void shouldStoreResourceIdInThreadLocalDuringPropertyDescriptorCreation() throws AMWException {
        // given
        ForeignableOwner changingOwner = ForeignableOwner.AMW;
        Integer expectedResourceId = 99;
        AbstractContext abstractContextMock = mock(AbstractContext.class);
        PropertyDescriptorEntity newDescriptor = new PropertyDescriptorEntityBuilder().build();
        List<PropertyTagEntity> tags = new ArrayList<>();
        ResourceEntity resourceEntityMock = mock(ResourceEntity.class);
        when(propertyValidationServiceMock.isValidTechnicalKey(newDescriptor.getPropertyName())).thenReturn(true);
        when(resourceEntityMock.getId()).thenReturn(expectedResourceId);

        // when
        service.savePropertyDescriptorForOwner(changingOwner, abstractContextMock, newDescriptor, tags, resourceEntityMock );

        // then
        assertThat("The resourceId Param must be stored as ThreadLocal variable for auditing (envers)",
                ThreadLocalUtil.getThreadVariable(ThreadLocalUtil.KEY_RESOURCE_ID), is(CoreMatchers.notNullValue()));
        int actualResourceId = (int) ThreadLocalUtil.getThreadVariable(ThreadLocalUtil.KEY_RESOURCE_ID);
        assertThat(actualResourceId, is(expectedResourceId));
    }

    @Test
    public void shouldStoreResourceIdInThreadLocalDuringPropertyDescriptorUpdate() throws AMWException {
        //given
        ForeignableOwner changingOwner = ForeignableOwner.AMW;
        Integer expectedResourceId = 99;
        AbstractContext abstractContextMock = mock(AbstractContext.class);
        PropertyDescriptorEntity descriptorToUpdate = new PropertyDescriptorEntityBuilder().withId(1).withOwner(ForeignableOwner.MAIA).build();
        List<PropertyTagEntity> tags = new ArrayList<>();
        when(propertyValidationServiceMock.isValidTechnicalKey(descriptorToUpdate.getPropertyName())).thenReturn(true);
        Assert.assertNotNull(descriptorToUpdate.getId());
        // mock (implicit verify for merge) merging of descriptorToUpdate
        PropertyDescriptorEntity mergedPropertyDescriptorMock = mock(PropertyDescriptorEntity.class);
        when(entityManagerMock.merge(descriptorToUpdate)).thenReturn(mergedPropertyDescriptorMock);
        // mocking the manageChangeOfEncryptedPropertyDescriptor
        when(entityManagerMock.createQuery("select p from PropertyEntity p where p.descriptor=:descriptor", PropertyEntity.class)).thenReturn(mock(TypedQuery.class));
        // return descriptorToUpdate with same values (not changed fields)
        when(entityManagerMock.find(PropertyDescriptorEntity.class, descriptorToUpdate.getId())).thenReturn(descriptorToUpdate);
        ResourceEntity resourceEntityMock = mock(ResourceEntity.class);
        when(resourceEntityMock.getId()).thenReturn(expectedResourceId);

        // when
        service.savePropertyDescriptorForOwner(changingOwner, abstractContextMock, descriptorToUpdate, tags, resourceEntityMock );

        // then
        assertThat("The resourceTypeId Param must be stored as ThreadLocal variable for auditing (envers)",
                ThreadLocalUtil.getThreadVariable(ThreadLocalUtil.KEY_RESOURCE_ID), is(CoreMatchers.notNullValue()));
        int actualResourceId = (int) ThreadLocalUtil.getThreadVariable(ThreadLocalUtil.KEY_RESOURCE_ID);
        assertThat(actualResourceId, is(expectedResourceId));
    }

    @Test
    public void shouldStoreResourceTypeIdInThreadLocalDuringPropertyDescriptorCreation() throws AMWException {
        // given
        ForeignableOwner changingOwner = ForeignableOwner.AMW;
        Integer resourceTypeId = 2;
        AbstractContext abstractContextMock = mock(AbstractContext.class);
        PropertyDescriptorEntity newDescriptor = new PropertyDescriptorEntityBuilder().build();
        List<PropertyTagEntity> tags = new ArrayList<>();
        ResourceTypeEntity resourceTypeEntityMock = mock(ResourceTypeEntity.class);
        doReturn(resourceTypeId).when(resourceTypeEntityMock).getId();
        when(propertyValidationServiceMock.isValidTechnicalKey(newDescriptor.getPropertyName())).thenReturn(true);

        // when
        service.savePropertyDescriptorForOwner(changingOwner, abstractContextMock, newDescriptor, tags, resourceTypeEntityMock);

        // then
        assertThat("The resourceTypeId Param must be stored as ThreadLocal variable for auditing (envers)",
                ThreadLocalUtil.getThreadVariable(ThreadLocalUtil.KEY_RESOURCE_TYPE_ID), is(CoreMatchers.notNullValue()));
        int actualResourceTypeId = (int) ThreadLocalUtil.getThreadVariable(ThreadLocalUtil.KEY_RESOURCE_TYPE_ID);
        assertThat(actualResourceTypeId, is(resourceTypeId));
    }

    @Test
    public void shouldStoreResourceTypeIdInThreadLocalDuringPropertyDescriptorUpdate() throws AMWException {
        // given
        ForeignableOwner changingOwner = ForeignableOwner.AMW;
        Integer resourceTypeId = 2;
        AbstractContext abstractContextMock = mock(AbstractContext.class);
        PropertyDescriptorEntity newDescriptor = new PropertyDescriptorEntityBuilder().withId(2).build();
        List<PropertyTagEntity> tags = new ArrayList<>();
        ResourceTypeEntity resourceTypeEntityMock = mock(ResourceTypeEntity.class);
        doReturn(resourceTypeId).when(resourceTypeEntityMock).getId();
        when(propertyValidationServiceMock.isValidTechnicalKey(newDescriptor.getPropertyName())).thenReturn(true);
        when(entityManagerMock.find(PropertyDescriptorEntity.class, newDescriptor.getId())).thenReturn(newDescriptor);
        when(entityManagerMock.merge(newDescriptor)).thenReturn(newDescriptor);

        // when
        service.savePropertyDescriptorForOwner(changingOwner, abstractContextMock, newDescriptor, tags, resourceTypeEntityMock);

        // then
        assertThat("The resourceTypeId Param must be stored as ThreadLocal variable for auditing (envers)",
                ThreadLocalUtil.getThreadVariable(ThreadLocalUtil.KEY_RESOURCE_TYPE_ID), is(CoreMatchers.notNullValue()));
        int actualResourceTypeId = (int) ThreadLocalUtil.getThreadVariable(ThreadLocalUtil.KEY_RESOURCE_TYPE_ID);
        assertThat(actualResourceTypeId, is(resourceTypeId));
    }


}