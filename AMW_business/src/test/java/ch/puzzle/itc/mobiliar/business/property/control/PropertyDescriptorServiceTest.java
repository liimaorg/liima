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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.Spy;

import ch.puzzle.itc.mobiliar.builders.PropertyDescriptorEntityBuilder;
import ch.puzzle.itc.mobiliar.builders.PropertyEntityBuilder;
import ch.puzzle.itc.mobiliar.business.auditview.control.AuditService;
import ch.puzzle.itc.mobiliar.business.auditview.control.ThreadLocalUtil;
import ch.puzzle.itc.mobiliar.business.environment.control.ContextDomainService;
import ch.puzzle.itc.mobiliar.business.environment.entity.AbstractContext;
import ch.puzzle.itc.mobiliar.business.foreignable.control.ForeignableService;
import ch.puzzle.itc.mobiliar.business.foreignable.entity.ForeignableOwner;
import ch.puzzle.itc.mobiliar.business.property.entity.PropertyDescriptorEntity;
import ch.puzzle.itc.mobiliar.business.property.entity.PropertyEntity;
import ch.puzzle.itc.mobiliar.business.property.entity.PropertyTagEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceContextEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceTypeEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ConsumedResourceRelationEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ResourceRelationContextEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ResourceRelationTypeEntity;
import ch.puzzle.itc.mobiliar.business.security.control.PermissionService;
import ch.puzzle.itc.mobiliar.common.exception.AMWException;
import ch.puzzle.itc.mobiliar.common.exception.NotAuthorizedException;

@ExtendWith(MockitoExtension.class)
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

    @Spy
    AuditService auditService = spy(new AuditService());

    @InjectMocks
    @Spy
    PropertyDescriptorService service = spy(new PropertyDescriptorService());

    int dummyResourceId = 100;

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
     * We expect a @link{NotAuthorizedException} since the user tries to decrypt the
     * property although permission is not given
     */
    @Test
    public void testManageChangeOfEncryptedPropertyDescriptorDecryptNotAuthorized() throws Exception {
        assertThrows(NotAuthorizedException.class, () -> {
            testManageChangeOfEncryptedPropertyDescriptor(Boolean.FALSE, false);
        });
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
        PropertyEntity p = mock(PropertyEntity.class);
        PropertyDescriptorEntity propertyDescriptorEntity = new PropertyDescriptorEntity();
        if (encrypt != null) {
            propertyDescriptorEntity.setEncrypt(encrypt);
        }
        propertyDescriptorEntity.setId(1);

        List<Integer> encryptedProperties;
        if (encrypt == null || encrypt) {
            encryptedProperties = Collections.emptyList();
        } else {
            encryptedProperties = Arrays.asList(propertyDescriptorEntity.getId());
        }

        boolean willEncrypt = Boolean.TRUE.equals(encrypt);
        boolean willDecrypt = Boolean.FALSE.equals(encrypt);
        if (willEncrypt || willDecrypt) {
            TypedQuery<PropertyEntity> queryMock = mock(TypedQuery.class);
            when(entityManagerMock.createQuery(anyString(), eq(PropertyEntity.class)))
                    .thenReturn(queryMock);
            when(queryMock.setParameter(eq("descriptor"), any())).thenReturn(queryMock);
            when(queryMock.getResultList()).thenReturn(Arrays.asList(p));
        }
        service.manageChangeOfEncryptedPropertyDescriptor(propertyDescriptorEntity, encryptedProperties,
                hasPermission);
        if (encrypt != null) {
            if (encrypt) {
                verify(p, times(0)).decrypt();
                verify(p, times(1)).encrypt();
            } else {
                verify(p, times(1)).decrypt();
                verify(p, times(0)).encrypt();
            }
        } else {
            verify(p, times(0)).decrypt();
            verify(p, times(0)).encrypt();
        }
    }

    @Test
    public void savePropertyDescriptorForOwnerWhenDescriptorIsNullShouldThrowException() throws AMWException {
        // given
        ForeignableOwner changingOwner = ForeignableOwner.AMW;

        AbstractContext abstractContextMock = mock(AbstractContext.class);
        PropertyDescriptorEntity descriptor = null;
        List<PropertyTagEntity> tags = new ArrayList<>();
        ResourceEntity resourceEntityMock = mock(ResourceEntity.class);

        // when
        assertThrows(AMWException.class, () -> {
            service.savePropertyDescriptorForOwner(changingOwner, abstractContextMock, descriptor, tags,
                    resourceEntityMock);
        });
    }

    @Test
    public void savePropertyDescriptorForOwnerWhenTechnicalKeyIsInvalidShouldThrowException() throws AMWException {
        // given
        ForeignableOwner changingOwner = ForeignableOwner.AMW;

        AbstractContext abstractContextMock = mock(AbstractContext.class);
        PropertyDescriptorEntity descriptor = new PropertyDescriptorEntityBuilder().build();

        List<PropertyTagEntity> tags = new ArrayList<>();
        assertNull(descriptor.getId());
        ResourceEntity resourceEntityMock = mock(ResourceEntity.class);
        when(propertyValidationServiceMock.isValidTechnicalKey(descriptor.getPropertyName()))
                .thenReturn(false);

        // when
        assertThrows(AMWException.class, () -> {
            service.savePropertyDescriptorForOwner(changingOwner, abstractContextMock, descriptor, tags,
                    resourceEntityMock);
        });
    }

    @Test
    public void savePropertyDescriptorForOwnerWhenDescriptorIdIsNullShouldCreateNewPropertyDescriptorForOwner()
            throws AMWException {
        // given
        ForeignableOwner changingOwner = ForeignableOwner.AMW;

        AbstractContext abstractContextMock = mock(AbstractContext.class);
        PropertyDescriptorEntity descriptor = new PropertyDescriptorEntityBuilder().build();
        List<PropertyTagEntity> tags = new ArrayList<>();
        ResourceEntity resourceEntityMock = mock(ResourceEntity.class);
        when(propertyValidationServiceMock.isValidTechnicalKey(descriptor.getPropertyName())).thenReturn(true);
        assertNull(descriptor.getId());

        // when
        service.savePropertyDescriptorForOwner(changingOwner, abstractContextMock, descriptor, tags,
                resourceEntityMock);

        // then
        verify(abstractContextMock).addPropertyDescriptor(descriptor);
        verify(entityManagerMock).persist(descriptor);
        verify(propertyTagEditingServiceMock).updateTags(tags, descriptor);
        verify(entityManagerMock).persist(abstractContextMock);
    }

    @Test
    public void savePropertyDescriptorShouldNotCreateMultiplePropertyDescriptorsWithSameName() throws AMWException {
        // given
        ForeignableOwner changingOwner = ForeignableOwner.AMW;

        PropertyDescriptorEntity descriptor = new PropertyDescriptorEntityBuilder()
                .withPropertyName("existing")
                .build();
        AbstractContext abstractContextMock = mock(AbstractContext.class);
        PropertyDescriptorEntity newDescriptor = new PropertyDescriptorEntityBuilder()
                .withPropertyName("existing")
                .build();
        List<PropertyTagEntity> tags = new ArrayList<>();
        ResourceEntity resourceEntityMock = mock(ResourceEntity.class);
        when(propertyValidationServiceMock.isValidTechnicalKey(descriptor.getPropertyName()))
                .thenReturn(true);
        when(abstractContextMock.getPropertyDescriptors())
                .thenReturn(new HashSet<>(Collections.singletonList(descriptor)));

        // when
        assertThrows(AMWException.class, () -> {
            service.savePropertyDescriptorForOwner(changingOwner, abstractContextMock, newDescriptor, tags,
                    resourceEntityMock);
        });
    }

    @Test
    public void savePropertyDescriptorForOwnerWhenDescriptorIdIsNotNullAndSameOwnerShouldSavePropertyDescriptor()
            throws AMWException {
        // given
        ForeignableOwner changingOwner = ForeignableOwner.AMW;

        AbstractContext abstractContextMock = mock(AbstractContext.class);
        PropertyDescriptorEntity descriptor = new PropertyDescriptorEntityBuilder().withId(1)
                .withOwner(changingOwner)
                .build();
        List<PropertyTagEntity> tags = new ArrayList<>();
        when(propertyValidationServiceMock.isValidTechnicalKey(descriptor.getPropertyName())).thenReturn(true);
        assertNotNull(descriptor.getId());
        // mock (implicit verify for merge) merging of descriptor
        PropertyDescriptorEntity mergedPropertyDescriptorMock = mock(PropertyDescriptorEntity.class);
        when(entityManagerMock.merge(descriptor)).thenReturn(mergedPropertyDescriptorMock);

        // mocking the manageChangeOfEncryptedPropertyDescriptor
        when(entityManagerMock.find(PropertyDescriptorEntity.class, descriptor.getId())).thenReturn(descriptor);
        ResourceEntity resourceEntityMock = mock(ResourceEntity.class);

        // when
        service.savePropertyDescriptorForOwner(changingOwner, abstractContextMock, descriptor, tags,
                resourceEntityMock);

        // then
        verify(propertyTagEditingServiceMock).updateTags(tags, mergedPropertyDescriptorMock);
    }

    @Test
    public void savePropertyDescriptorForOwnerWhenDescriptorIdIsNotNullNoForeignableFieldsChangedButDifferentOwnerShouldSavePropertyDescriptor()
            throws AMWException {
        // given
        ForeignableOwner changingOwner = ForeignableOwner.AMW;

        AbstractContext abstractContextMock = mock(AbstractContext.class);
        PropertyDescriptorEntity descriptor = new PropertyDescriptorEntityBuilder().withId(1)
                .withOwner(ForeignableOwner.MAIA).build();
        List<PropertyTagEntity> tags = new ArrayList<>();
        when(propertyValidationServiceMock.isValidTechnicalKey(descriptor.getPropertyName())).thenReturn(true);
        assertNotNull(descriptor.getId());

        // mock (implicit verify for merge) merging of descriptor
        PropertyDescriptorEntity mergedPropertyDescriptorMock = mock(PropertyDescriptorEntity.class);
        when(entityManagerMock.merge(descriptor)).thenReturn(mergedPropertyDescriptorMock);

        // return descriptor with same values (not changed fields)
        when(entityManagerMock.find(PropertyDescriptorEntity.class, descriptor.getId())).thenReturn(descriptor);
        ResourceEntity resourceEntityMock = mock(ResourceEntity.class);

        // when
        service.savePropertyDescriptorForOwner(changingOwner, abstractContextMock, descriptor, tags,
                resourceEntityMock);

        // then
        verify(propertyTagEditingServiceMock).updateTags(tags, mergedPropertyDescriptorMock);
    }

    @Test
    public void deletePropertyDescriptorByOwnerWhenDeletingOwnerIsOwnerOfDescriptorAndNoPropertiesShouldDeletePropertyDescriptor()
            throws AMWException {
        // given
        ForeignableOwner deletingOwner = ForeignableOwner.AMW;

        AbstractContext abstractContextMock = mock(AbstractContext.class);
        PropertyDescriptorEntity descriptor = new PropertyDescriptorEntityBuilder().withOwner(deletingOwner)
                .withId(1)
                .build();
        assertEquals(deletingOwner, descriptor.getOwner());

        TypedQuery<PropertyDescriptorEntity> queryMock = mock(TypedQuery.class);
        when(entityManagerMock.createQuery(
                "from PropertyDescriptorEntity d  left join fetch d.propertyTags where d.id = :propertyDescriptorId ",
                PropertyDescriptorEntity.class)).thenReturn(queryMock);
        when(queryMock.getSingleResult()).thenReturn(descriptor);

        // when
        service.deletePropertyDescriptorByOwnerInResourceContext(descriptor, abstractContextMock,
                dummyResourceId);

        // then
        verify(entityManagerMock).remove(descriptor);
    }

    @Test
    public void deletePropertyDescriptorByOwnerWhenDeletingOwnerIsOwnerOfDescriptorButHasPropertiesShouldThrowException()
            throws AMWException {
        // given
        ForeignableOwner deletingOwner = ForeignableOwner.AMW;

        AbstractContext abstractContextMock = mock(AbstractContext.class);
        Set<PropertyEntity> properties = new HashSet<>();
        properties.add(new PropertyEntity());
        PropertyDescriptorEntity descriptor = new PropertyDescriptorEntityBuilder().withOwner(deletingOwner)
                .withId(1).withProperties(properties).build();
        assertEquals(deletingOwner, descriptor.getOwner());

        TypedQuery<PropertyDescriptorEntity> queryMock = mock(TypedQuery.class);
        when(entityManagerMock.createQuery(
                "from PropertyDescriptorEntity d  left join fetch d.propertyTags where d.id = :propertyDescriptorId ",
                PropertyDescriptorEntity.class)).thenReturn(queryMock);
        when(queryMock.getSingleResult()).thenReturn(descriptor);
        // when
        assertThrows(AMWException.class, () -> {
            service.deletePropertyDescriptorByOwnerInResourceContext(descriptor, abstractContextMock,
                    dummyResourceId);
        });
    }

    @Test
    public void deletePropertyDescriptorByOwnerIncludingPropertyValuesWhenDeletingOwnerIsOwnerOfDescriptorWithPropertiesOnResourceShouldSucceed()
            throws AMWException {
        // given
        ForeignableOwner deletingOwner = ForeignableOwner.AMW;

        AbstractContext abstractContextMock = mock(AbstractContext.class);
        ResourceEntity resourceEntityMock = mock(ResourceEntity.class);
        PropertyEntity property = new PropertyEntity();
        Set<PropertyEntity> properties = new HashSet<>();
        properties.add(property);
        PropertyDescriptorEntity descriptor = new PropertyDescriptorEntityBuilder().withOwner(deletingOwner)
                .withId(1)
                .withProperties(properties).build();
        assertEquals(deletingOwner, descriptor.getOwner());
        ResourceContextEntity resourceContextEntityMock = mock(ResourceContextEntity.class);

        TypedQuery<PropertyDescriptorEntity> queryMock = mock(TypedQuery.class);
        when(entityManagerMock.createQuery(
                "from PropertyDescriptorEntity d  left join fetch d.propertyTags where d.id = :propertyDescriptorId ",
                PropertyDescriptorEntity.class)).thenReturn(queryMock);
        when(queryMock.getSingleResult()).thenReturn(descriptor);
        when(resourceEntityMock.getContexts()).thenReturn(Collections.singleton(resourceContextEntityMock));
        when(resourceContextEntityMock.getProperties()).thenReturn(properties);

        // when
        service.deletePropertyDescriptorByOwnerIncludingPropertyValues(descriptor, abstractContextMock,
                resourceEntityMock);

        // then
        verify(resourceContextEntityMock).removeProperty(property);
        verify(entityManagerMock).remove(descriptor);
    }

    @Test
    public void deletePropertyDescriptorByOwnerIncludingPropertyValuesWhenDeletingOwnerIsOwnerOfDescriptorWithPropertiesOnResourceRelationShouldSucceed()
            throws AMWException {
        // given
        ForeignableOwner deletingOwner = ForeignableOwner.AMW;

        AbstractContext abstractContextMock = mock(AbstractContext.class);
        ResourceEntity resourceEntityMock = mock(ResourceEntity.class);
        PropertyEntity property = new PropertyEntity();
        Set<PropertyEntity> properties = new HashSet<>();
        properties.add(property);
        PropertyDescriptorEntity descriptor = new PropertyDescriptorEntityBuilder().withOwner(deletingOwner)
                .withId(1)
                .withProperties(properties).build();
        assertEquals(deletingOwner, descriptor.getOwner());
        ResourceContextEntity resourceContextEntityMock = mock(ResourceContextEntity.class);
        ConsumedResourceRelationEntity consumedResourceRelationEntityMock = mock(
                ConsumedResourceRelationEntity.class);
        ResourceRelationContextEntity resourceRelationContextEntityMock = mock(
                ResourceRelationContextEntity.class);

        TypedQuery<PropertyDescriptorEntity> queryMock = mock(TypedQuery.class);
        when(entityManagerMock.createQuery(
                "from PropertyDescriptorEntity d  left join fetch d.propertyTags where d.id = :propertyDescriptorId ",
                PropertyDescriptorEntity.class)).thenReturn(queryMock);
        when(queryMock.getSingleResult()).thenReturn(descriptor);
        when(resourceEntityMock.getContexts()).thenReturn(Collections.singleton(resourceContextEntityMock));
        when(resourceEntityMock.getConsumedSlaveRelations())
                .thenReturn(Collections.singleton(consumedResourceRelationEntityMock));
        when(consumedResourceRelationEntityMock.getContexts())
                .thenReturn(Collections.singleton(resourceRelationContextEntityMock));
        when(resourceRelationContextEntityMock.getProperties()).thenReturn(properties);

        // when
        service.deletePropertyDescriptorByOwnerIncludingPropertyValues(descriptor, abstractContextMock,
                resourceEntityMock);

        // then
        verify(resourceRelationContextEntityMock).removeProperty(property);
        verify(entityManagerMock).remove(descriptor);
    }

    @Test
    public void deletePropertyDescriptorByOwnerWhenDeletingOwnerIsOwnerOfDescriptorWithTagsAndNoPropertiesShouldDeleteAllTags()
            throws AMWException {
        // given
        ForeignableOwner deletingOwner = ForeignableOwner.AMW;

        AbstractContext abstractContextMock = mock(AbstractContext.class);
        PropertyTagEntity tag1 = new PropertyTagEntity();
        tag1.setId(1);
        PropertyTagEntity tag2 = new PropertyTagEntity();
        tag1.setId(2);
        PropertyDescriptorEntity descriptor = new PropertyDescriptorEntityBuilder().withOwner(deletingOwner)
                .withTags(tag1, tag2).withId(1).build();
        assertEquals(deletingOwner, descriptor.getOwner());

        TypedQuery<PropertyDescriptorEntity> queryMock = mock(TypedQuery.class);
        when(entityManagerMock.createQuery(
                "from PropertyDescriptorEntity d  left join fetch d.propertyTags where d.id = :propertyDescriptorId ",
                PropertyDescriptorEntity.class)).thenReturn(queryMock);
        when(queryMock.getSingleResult()).thenReturn(descriptor);

        when(entityManagerMock.find(PropertyTagEntity.class, tag1.getId())).thenReturn(tag1);
        when(entityManagerMock.find(PropertyTagEntity.class, tag2.getId())).thenReturn(tag2);

        // when
        service.deletePropertyDescriptorByOwnerInResourceContext(descriptor, abstractContextMock,
                dummyResourceId);

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
        PropertyDescriptorEntity descriptor = new PropertyDescriptorEntityBuilder().withOwner(deletingOwner)
                .withId(1)
                .withProperties(properties).build();
        AbstractContext abstractContextMock = mock(AbstractContext.class);
        doNothing().when(service).removePropertyDescriptorByOwner(eq(descriptor), eq(abstractContextMock),
                anyBoolean());
        doReturn(descriptor).when(service).getPropertyDescriptorWithTags(anyInt());

        // when
        service.deletePropertyDescriptorByOwnerInResourceContext(descriptor, abstractContextMock,
                resourceIdForAuditLog);

        // then
        assertThat("The resourceId Param must be stored as ThreadLocal variable for auditing (envers)",
                ThreadLocalUtil.getThreadVariable(ThreadLocalUtil.KEY_RESOURCE_ID),
                is(CoreMatchers.notNullValue()));
        int resourceId = (int) ThreadLocalUtil.getThreadVariable(ThreadLocalUtil.KEY_RESOURCE_ID);
        assertThat(resourceId, is(resourceIdForAuditLog));
    }

    @Test
    public void shouldStoreResourceTypeIdInThreadLocalDuringPropertyUpdateInResourceTypeContext()
            throws AMWException {
        // given
        Integer resourceTypeIdForAuditLog = 1;
        ForeignableOwner deletingOwner = ForeignableOwner.AMW;
        Set<PropertyEntity> properties = new HashSet<>();
        properties.add(new PropertyEntity());
        PropertyDescriptorEntity descriptor = new PropertyDescriptorEntityBuilder().withOwner(deletingOwner)
                .withId(1)
                .withProperties(properties).build();
        AbstractContext abstractContextMock = mock(AbstractContext.class);
        doNothing().when(service).removePropertyDescriptorByOwner(eq(descriptor), eq(abstractContextMock),
                anyBoolean());
        doReturn(descriptor).when(service).getPropertyDescriptorWithTags(anyInt());

        // when
        service.deletePropertyDescriptorByOwnerInResourceTypeContext(descriptor, abstractContextMock,
                resourceTypeIdForAuditLog);

        // then
        assertThat("The resourceTypeId Param must be stored as ThreadLocal variable for auditing (envers)",
                ThreadLocalUtil.getThreadVariable(ThreadLocalUtil.KEY_RESOURCE_TYPE_ID),
                is(CoreMatchers.notNullValue()));
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
        when(propertyValidationServiceMock.isValidTechnicalKey(newDescriptor.getPropertyName()))
                .thenReturn(true);
        when(resourceEntityMock.getId()).thenReturn(expectedResourceId);

        // when
        service.savePropertyDescriptorForOwner(changingOwner, abstractContextMock, newDescriptor, tags,
                resourceEntityMock);

        // then
        assertThat("The resourceId Param must be stored as ThreadLocal variable for auditing (envers)",
                ThreadLocalUtil.getThreadVariable(ThreadLocalUtil.KEY_RESOURCE_ID),
                is(CoreMatchers.notNullValue()));
        int actualResourceId = (int) ThreadLocalUtil.getThreadVariable(ThreadLocalUtil.KEY_RESOURCE_ID);
        assertThat(actualResourceId, is(expectedResourceId));
    }

    @Test
    public void shouldStoreResourceIdInThreadLocalDuringPropertyDescriptorUpdate() throws AMWException {
        // given
        ForeignableOwner changingOwner = ForeignableOwner.AMW;
        Integer expectedResourceId = 99;
        AbstractContext abstractContextMock = mock(AbstractContext.class);
        PropertyDescriptorEntity descriptorToUpdate = new PropertyDescriptorEntityBuilder().withId(1)
                .withOwner(ForeignableOwner.MAIA).build();
        List<PropertyTagEntity> tags = new ArrayList<>();
        when(propertyValidationServiceMock.isValidTechnicalKey(descriptorToUpdate.getPropertyName()))
                .thenReturn(true);
        assertNotNull(descriptorToUpdate.getId());
        // mock (implicit verify for merge) merging of descriptorToUpdate
        PropertyDescriptorEntity mergedPropertyDescriptorMock = mock(PropertyDescriptorEntity.class);
        when(entityManagerMock.merge(descriptorToUpdate)).thenReturn(mergedPropertyDescriptorMock);
        // return descriptorToUpdate with same values (not changed fields)
        when(entityManagerMock.find(PropertyDescriptorEntity.class, descriptorToUpdate.getId()))
                .thenReturn(descriptorToUpdate);
        ResourceEntity resourceEntityMock = mock(ResourceEntity.class);
        when(resourceEntityMock.getId()).thenReturn(expectedResourceId);

        // when
        service.savePropertyDescriptorForOwner(changingOwner, abstractContextMock, descriptorToUpdate, tags,
                resourceEntityMock);

        // then
        assertThat("The resourceId Param must be stored as ThreadLocal variable for auditing (envers)",
                ThreadLocalUtil.getThreadVariable(ThreadLocalUtil.KEY_RESOURCE_ID),
                is(CoreMatchers.notNullValue()));
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
        when(propertyValidationServiceMock.isValidTechnicalKey(newDescriptor.getPropertyName()))
                .thenReturn(true);

        // when
        service.savePropertyDescriptorForOwner(changingOwner, abstractContextMock, newDescriptor, tags,
                resourceTypeEntityMock);

        // then
        assertThat("The resourceTypeId Param must be stored as ThreadLocal variable for auditing (envers)",
                ThreadLocalUtil.getThreadVariable(ThreadLocalUtil.KEY_RESOURCE_TYPE_ID),
                is(CoreMatchers.notNullValue()));
        int actualResourceTypeId = (int) ThreadLocalUtil
                .getThreadVariable(ThreadLocalUtil.KEY_RESOURCE_TYPE_ID);
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
        when(propertyValidationServiceMock.isValidTechnicalKey(newDescriptor.getPropertyName()))
                .thenReturn(true);
        when(entityManagerMock.find(PropertyDescriptorEntity.class, newDescriptor.getId()))
                .thenReturn(newDescriptor);
        when(entityManagerMock.merge(newDescriptor)).thenReturn(newDescriptor);

        // when
        service.savePropertyDescriptorForOwner(changingOwner, abstractContextMock, newDescriptor, tags,
                resourceTypeEntityMock);

        // then
        assertThat("The resourceTypeId Param must be stored as ThreadLocal variable for auditing (envers)",
                ThreadLocalUtil.getThreadVariable(ThreadLocalUtil.KEY_RESOURCE_TYPE_ID),
                is(CoreMatchers.notNullValue()));
        int actualResourceTypeId = (int) ThreadLocalUtil
                .getThreadVariable(ThreadLocalUtil.KEY_RESOURCE_TYPE_ID);
        assertThat(actualResourceTypeId, is(resourceTypeId));
    }

    @Test
    public void shouldStoreResourceIdInThreadLocalWhenPropertyDescriptorsInlcudingPropertyValuesAreDeleted()
            throws AMWException {
        // given
        ForeignableOwner deletingOwner = ForeignableOwner.AMW;
        Integer resourceId = 22;

        AbstractContext abstractContextMock = mock(AbstractContext.class);
        ResourceEntity resourceEntityMock = mock(ResourceEntity.class);
        when(resourceEntityMock.getId()).thenReturn(resourceId);
        Set<PropertyEntity> properties = new HashSet<>();
        properties.add(new PropertyEntity());
        PropertyDescriptorEntity descriptor = new PropertyDescriptorEntityBuilder().withOwner(deletingOwner)
                .withId(1)
                .withProperties(properties).build();
        assertEquals(deletingOwner, descriptor.getOwner());

        TypedQuery<PropertyDescriptorEntity> queryMock = mock(TypedQuery.class);
        when(entityManagerMock.createQuery(
                "from PropertyDescriptorEntity d  left join fetch d.propertyTags where d.id = :propertyDescriptorId ",
                PropertyDescriptorEntity.class)).thenReturn(queryMock);
        when(queryMock.getSingleResult()).thenReturn(descriptor);

        // when
        service.deletePropertyDescriptorByOwnerIncludingPropertyValues(descriptor, abstractContextMock,
                resourceEntityMock);

        // then
        assertThat("The resourceId Param must be stored as ThreadLocal variable for auditing (envers)",
                ThreadLocalUtil.getThreadVariable(ThreadLocalUtil.KEY_RESOURCE_ID),
                is(CoreMatchers.notNullValue()));
        int actualResourceId = (int) ThreadLocalUtil.getThreadVariable(ThreadLocalUtil.KEY_RESOURCE_ID);
        assertThat(actualResourceId, is(resourceId));
    }

    @Test
    public void deletePropertyDescriptorByOwnerIncludingPropertyValuesWhenDeletingOwnerIsOwnerOfDescriptorDefinedOnResourceTypeWithPropertiesOnResourceShouldSucceed()
            throws AMWException {
        // given
        ForeignableOwner deletingOwner = ForeignableOwner.AMW;

        AbstractContext abstractContextMock = mock(AbstractContext.class);
        ResourceTypeEntity resourceTypeEntityMock = mock(ResourceTypeEntity.class);
        ResourceEntity resourceEntityMock = mock(ResourceEntity.class);
        ResourceContextEntity resourceContextEntityMock = mock(ResourceContextEntity.class);
        PropertyDescriptorEntity descriptor = new PropertyDescriptorEntityBuilder().withOwner(deletingOwner)
                .withId(1)
                .build();
        PropertyEntity property = new PropertyEntityBuilder().buildPropertyEntity("propVal", descriptor);
        descriptor.addProperty(property);
        Set<PropertyEntity> properties = new HashSet<>();
        properties.add(property);

        TypedQuery<PropertyDescriptorEntity> queryMock = mock(TypedQuery.class);
        when(entityManagerMock.createQuery(
                "from PropertyDescriptorEntity d  left join fetch d.propertyTags where d.id = :propertyDescriptorId ",
                PropertyDescriptorEntity.class)).thenReturn(queryMock);
        when(queryMock.getSingleResult()).thenReturn(descriptor);
        when(resourceTypeEntityMock.getResources()).thenReturn(Collections.singleton(resourceEntityMock));
        when(resourceEntityMock.getContexts()).thenReturn(Collections.singleton(resourceContextEntityMock));
        when(resourceContextEntityMock.getProperties()).thenReturn(properties);

        // when
        service.deletePropertyDescriptorByOwnerIncludingPropertyValues(descriptor, abstractContextMock,
                resourceTypeEntityMock);

        // then
        verify(resourceContextEntityMock).removeProperty(property);
        verify(entityManagerMock).remove(descriptor);
    }

    @Test
    public void deletePropertyDescriptorByOwnerIncludingPropertyValuesWhenDeletingOwnerIsOwnerOfDescriptorDefinedOnResourceTypeWithPropertiesOnResourceRelationShouldSucceed()
            throws AMWException {
        // given
        ForeignableOwner deletingOwner = ForeignableOwner.AMW;

        AbstractContext abstractContextMock = mock(AbstractContext.class);
        ResourceTypeEntity resourceTypeEntityMock = mock(ResourceTypeEntity.class);
        PropertyEntity property = new PropertyEntity();
        Set<PropertyEntity> properties = new HashSet<>();
        properties.add(property);
        PropertyDescriptorEntity descriptor = new PropertyDescriptorEntityBuilder().withOwner(deletingOwner)
                .withId(1)
                .withProperties(properties).build();
        assertEquals(deletingOwner, descriptor.getOwner());
        ResourceRelationTypeEntity resourceRelationTypeEntityMock = mock(ResourceRelationTypeEntity.class);
        ConsumedResourceRelationEntity consumedResourceRelationEntityMock = mock(
                ConsumedResourceRelationEntity.class);
        ResourceRelationContextEntity resourceRelationContextEntityMock = mock(
                ResourceRelationContextEntity.class);

        TypedQuery<PropertyDescriptorEntity> queryMock = mock(TypedQuery.class);
        when(entityManagerMock.createQuery(
                "from PropertyDescriptorEntity d  left join fetch d.propertyTags where d.id = :propertyDescriptorId ",
                PropertyDescriptorEntity.class)).thenReturn(queryMock);
        when(queryMock.getSingleResult()).thenReturn(descriptor);
        when(resourceTypeEntityMock.getResourceRelationTypesB())
                .thenReturn(Collections.singleton(resourceRelationTypeEntityMock));
        when(resourceRelationTypeEntityMock.getConsumedResourceRelations())
                .thenReturn(Collections.singleton(consumedResourceRelationEntityMock));
        when(consumedResourceRelationEntityMock.getContexts())
                .thenReturn(Collections.singleton(resourceRelationContextEntityMock));
        when(resourceRelationContextEntityMock.getProperties()).thenReturn(properties);

        // when
        service.deletePropertyDescriptorByOwnerIncludingPropertyValues(descriptor, abstractContextMock,
                resourceTypeEntityMock);

        // then
        verify(resourceRelationContextEntityMock).removeProperty(property);
        verify(entityManagerMock).remove(descriptor);
    }

}