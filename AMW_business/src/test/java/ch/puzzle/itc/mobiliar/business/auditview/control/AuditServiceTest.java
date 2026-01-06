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

package ch.puzzle.itc.mobiliar.business.auditview.control;

import ch.puzzle.itc.mobiliar.builders.PropertyDescriptorEntityBuilder;
import ch.puzzle.itc.mobiliar.business.auditview.entity.AuditViewEntry;
import ch.puzzle.itc.mobiliar.business.auditview.entity.Auditable;
import ch.puzzle.itc.mobiliar.business.database.entity.MyRevisionEntity;
import ch.puzzle.itc.mobiliar.business.property.entity.PropertyDescriptorEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceContextEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceTypeContextEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceTypeEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ConsumedResourceRelationEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ProvidedResourceRelationEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ResourceRelationContextEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ResourceRelationTypeEntity;
import ch.puzzle.itc.mobiliar.test.testrunner.PersistenceTestExtension;
import org.hamcrest.MatcherAssert;
import org.hibernate.envers.RevisionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.hamcrest.MatcherAssert.assertThat;

@ExtendWith(PersistenceTestExtension.class)
public class AuditServiceTest {

    @InjectMocks
    private AuditService auditService;

    @PersistenceContext
    private EntityManager entityManager;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        auditService.entityManager = entityManager;
        ThreadLocalUtil.destroy();
    }

    @Test
    public void shouldReturnNullIfEntityIsNotDeleted() {
        // given
        PropertyDescriptorEntity propertyDescriptor = new PropertyDescriptorEntityBuilder().build();
        entityManager.persist(propertyDescriptor);
        Integer id = propertyDescriptor.getId();

        // when
        Object deletedEntity = auditService.getDeletedEntity(new PropertyDescriptorEntity(), id);

        // then
        assertNull(deletedEntity);
    }

    @Test
    public void shouldReturnEntityIfEntityIsDeleted() {
        // given
        entityManager.createNativeQuery("INSERT INTO TAMW_REVINFO (id, timestamp, username, v) VALUES (112358,1484756699669, 'test', 0)").executeUpdate();
        entityManager.createNativeQuery("INSERT INTO TAMW_PROPERTYDESCRIPTOR_AUD (id, rev, revtype, propertyname) VALUES (9992, 112358, 2, 'proforma')").executeUpdate();

        // when
        PropertyDescriptorEntity deletedEntity = (PropertyDescriptorEntity) auditService.getDeletedEntity(new PropertyDescriptorEntity(), 9992);

        // then
        assertThat(deletedEntity.getId(), is(9992));
        assertThat(deletedEntity.getPropertyName(), is("proforma"));
    }

    @Test
    public void shouldSetResourceTypeIdInThreadLocal() {
        // given
        int resourceTypeId = 700;

        // when
        auditService.setResourceTypeIdInThreadLocal(resourceTypeId);

        // then
        MatcherAssert.assertThat((Integer) ThreadLocalUtil.getThreadVariable(ThreadLocalUtil.KEY_RESOURCE_TYPE_ID), is(resourceTypeId));
    }


    @Test
    public void shouldSetResourceIdInThreadLocal() {
        // given
        int resourceTypeId = 700;

        // when
        auditService.setResourceIdInThreadLocal(resourceTypeId);

        // then
        MatcherAssert.assertThat((Integer) ThreadLocalUtil.getThreadVariable(ThreadLocalUtil.KEY_RESOURCE_ID), is(resourceTypeId));
    }

    @Test
    public void shouldSetResourceTypeIdInThreadLocalDuringStoreIdInThreadLocalForAuditLog_resourceType() {
        // given
        int resourceTypeId = 700;
        ResourceTypeEntity resourceType = new ResourceTypeEntity();
        resourceType.setId(resourceTypeId);

        // when
        auditService.storeIdInThreadLocalForAuditLog(resourceType);

        // then
        MatcherAssert.assertThat((Integer) ThreadLocalUtil.getThreadVariable(ThreadLocalUtil.KEY_RESOURCE_TYPE_ID), is(resourceTypeId));
    }

    @Test
    public void shouldSetResourceIdInThreadLocalDuringStoreIdInThreadLocalForAuditLog_resource() {
        // given
        int resourceId = 700;
        ResourceEntity resource = new ResourceEntity();
        resource.setId(resourceId);

        // when
        auditService.storeIdInThreadLocalForAuditLog(resource);

        // then
        MatcherAssert.assertThat((Integer) ThreadLocalUtil.getThreadVariable(ThreadLocalUtil.KEY_RESOURCE_ID), is(resourceId));
    }


    @Test
    public void shouldSetResourceIdInThreadLocalDuringStoreIdInThreadLocalForAuditLog_consumedResource() {
        // given
        int masterResourceId = 500;
        int slaveResourceId = 300;
        int resourceId = 700;

        ResourceEntity masterResource = new ResourceEntity();
        masterResource.setId(masterResourceId);

        ResourceEntity slaveResource = new ResourceEntity();
        slaveResource.setId(slaveResourceId);

        ConsumedResourceRelationEntity consumedResource = new ConsumedResourceRelationEntity();
        consumedResource.setMasterResource(masterResource);
        consumedResource.setSlaveResource(slaveResource);
        consumedResource.setId(resourceId);

        // when
        auditService.storeIdInThreadLocalForAuditLog(consumedResource);

        // then
        MatcherAssert.assertThat(ThreadLocalUtil.getThreadVariable(ThreadLocalUtil.KEY_RESOURCE_TYPE_ID), is(nullValue()));
        MatcherAssert.assertThat((Integer) ThreadLocalUtil.getThreadVariable(ThreadLocalUtil.KEY_RESOURCE_ID), is(masterResourceId));
    }

    @Test
    public void shouldSetResourceIdInThreadLocalDuringStoreIdInThreadLocalForAuditLog_providedResource() {
        // given
        int masterResourceId = 500;
        int slaveResourceId = 300;
        int resourceId = 700;

        ResourceEntity masterResource = new ResourceEntity();
        masterResource.setId(masterResourceId);

        ResourceEntity slaveResource = new ResourceEntity();
        slaveResource.setId(slaveResourceId);

        ProvidedResourceRelationEntity consumedResource = new ProvidedResourceRelationEntity();
        consumedResource.setMasterResource(masterResource);
        consumedResource.setSlaveResource(slaveResource);
        consumedResource.setId(resourceId);

        // when
        auditService.storeIdInThreadLocalForAuditLog(consumedResource);

        // then
        MatcherAssert.assertThat(ThreadLocalUtil.getThreadVariable(ThreadLocalUtil.KEY_RESOURCE_TYPE_ID), is(nullValue()));
        MatcherAssert.assertThat((Integer) ThreadLocalUtil.getThreadVariable(ThreadLocalUtil.KEY_RESOURCE_ID), is(masterResourceId));
    }

    @Test
    public void shouldSetResourceIdInThreadLocalDuringStoreIdInThreadLocalForAuditLog_resourceRelationTypeEntity() {
        // given
        int idA = 500;
        int idB = 300;

        ResourceTypeEntity resourceTypeA = new ResourceTypeEntity();
        resourceTypeA.setId(idA);
        ResourceTypeEntity resourceTypeB = new ResourceTypeEntity();
        resourceTypeB.setId(idB);
        ResourceRelationTypeEntity resourceRelationTypeEntity = new ResourceRelationTypeEntity();
        resourceRelationTypeEntity.setResourceTypes(resourceTypeA, resourceTypeB);

        // when
        auditService.storeIdInThreadLocalForAuditLog(resourceRelationTypeEntity);

        // then
        MatcherAssert.assertThat(ThreadLocalUtil.getThreadVariable(ThreadLocalUtil.KEY_RESOURCE_ID), is(nullValue()));
        MatcherAssert.assertThat((Integer) ThreadLocalUtil.getThreadVariable(ThreadLocalUtil.KEY_RESOURCE_TYPE_ID), is(idA));
    }

    @Test
    public void shouldSetResourceIdInThreadLocalForAbstractContext_resourceContextEntity() {
        // given
        int resourceId = 456;
        ResourceContextEntity resourceContextEntity = new ResourceContextEntity();
        ResourceEntity resource = new ResourceEntity();
        resource.setId(resourceId);
        resourceContextEntity.setContextualizedObject(resource);

        // when
        auditService.storeIdInThreadLocalForAuditLog(resourceContextEntity);

        // then
        MatcherAssert.assertThat((Integer) ThreadLocalUtil.getThreadVariable(ThreadLocalUtil.KEY_RESOURCE_ID), is(resourceId));
        MatcherAssert.assertThat(ThreadLocalUtil.getThreadVariable(ThreadLocalUtil.KEY_RESOURCE_TYPE_ID), is(nullValue()));
    }

    @Test
    public void shouldSetResourceTypeIdInThreadLocalForAbstractContext_resourceTypeContextEntity() {
        // given
        int resourceTypeId = 456;
        ResourceTypeContextEntity resourceContextEntity = new ResourceTypeContextEntity();
        ResourceTypeEntity resourceType = new ResourceTypeEntity();
        resourceType.setId(resourceTypeId);
        resourceContextEntity.setContextualizedObject(resourceType);

        // when
        auditService.storeIdInThreadLocalForAuditLog(resourceContextEntity);

        // then
        MatcherAssert.assertThat(ThreadLocalUtil.getThreadVariable(ThreadLocalUtil.KEY_RESOURCE_ID), is(nullValue()));
        MatcherAssert.assertThat((Integer) ThreadLocalUtil.getThreadVariable(ThreadLocalUtil.KEY_RESOURCE_TYPE_ID), is(resourceTypeId));
    }

    @Test
    public void shouldHandleConsumedContextualizedObjectAsNull() {
        // given
        ResourceRelationContextEntity resourceRelationContextEntity = new ResourceRelationContextEntity();
        resourceRelationContextEntity.setContextualizedObject(null);

        // when
        auditService.storeIdInThreadLocalForAuditLog(resourceRelationContextEntity);

        // then
        MatcherAssert.assertThat(ThreadLocalUtil.getThreadVariable(ThreadLocalUtil.KEY_RESOURCE_ID), is(nullValue()));
        MatcherAssert.assertThat(ThreadLocalUtil.getThreadVariable(ThreadLocalUtil.KEY_RESOURCE_TYPE_ID), is(nullValue()));
    }

    @Test
    public void shouldSetResourceIdInThreadLocalForAbstractContext_consumedResourceRelationEntity() {
        // given
        int resourceId = 456;
        ResourceEntity resource = new ResourceEntity();
        resource.setId(resourceId);
        ConsumedResourceRelationEntity consumedResourceRelationEntity = new ConsumedResourceRelationEntity();
        consumedResourceRelationEntity.setMasterResource(resource);
        ResourceRelationContextEntity resourceRelationContextEntity = new ResourceRelationContextEntity();
        resourceRelationContextEntity.setContextualizedObject(consumedResourceRelationEntity);

        // when
        auditService.storeIdInThreadLocalForAuditLog(resourceRelationContextEntity);

        // then
        MatcherAssert.assertThat((Integer) ThreadLocalUtil.getThreadVariable(ThreadLocalUtil.KEY_RESOURCE_ID), is(resourceId));
        MatcherAssert.assertThat(ThreadLocalUtil.getThreadVariable(ThreadLocalUtil.KEY_RESOURCE_TYPE_ID), is(nullValue()));
    }

    @Test
    public void shouldAuditViewEntryIsRelevantBecauseOldAndNewValueAreNotEqual(){
        // given
        String oldValue = "Technical Key: appLogLevel-tmp";
        String newValue = "Technical Key: appLogLevel";
        MyRevisionEntity revisionEntity = new MyRevisionEntity();
        AuditViewEntry entry = AuditViewEntry.builder(revisionEntity, RevisionType.MOD)
                .oldValue(oldValue)
                .value(newValue)
                .type(EMPTY)
                .build();

        // when
        boolean relevant = auditService.isAuditViewEntryRelevant(entry, Collections.<Integer, AuditViewEntry>emptyMap());

        // then
        assertThat(relevant, is(true));
    }


    @Test
    public void shouldAuditViewEntryIsNotRelevantBecauseOldAndNewValueAreEqual(){
        // given
        String oldValue = "Technical Key: appLogLevel";
        String newValue = "Technical Key: appLogLevel";
        MyRevisionEntity revisionEntity = new MyRevisionEntity();
        AuditViewEntry entry = AuditViewEntry.builder(revisionEntity, RevisionType.MOD)
                .oldValue(oldValue)
                .value(newValue)
                .type(EMPTY)
                .build();

        // when
        boolean relevant = auditService.isAuditViewEntryRelevant(entry, Collections.<Integer, AuditViewEntry>emptyMap());

        // then
        assertThat(relevant, is(false));
    }

    @Test
    public void shouldReturnFalseWhenEntryIsNull(){
        // given
        // when
        boolean relevant = auditService.isAuditViewEntryRelevant(null, Collections.<Integer, AuditViewEntry>emptyMap());

        // then
        assertThat(relevant, is(false));
    }


    @Test
    public void shouldReturnTrueIfEntryIsNotAlreadyInList(){
        // given
        String oldValue = "Technical Key: appLogLevel-tmp";
        String newValue = "Technical Key: appLogLevel";
        MyRevisionEntity revisionEntity = new MyRevisionEntity();
        AuditViewEntry entryInList = AuditViewEntry.builder(revisionEntity, RevisionType.MOD)
                .oldValue(oldValue)
                .value(newValue)
                .type(EMPTY)
                .build();
        AuditViewEntry newEntry = AuditViewEntry.builder(revisionEntity, RevisionType.MOD)
                .oldValue(oldValue + ".")
                .value(newValue)
                .type(EMPTY)
                .build();
        Map<Integer, AuditViewEntry> allAuditViewEntries = new HashMap<>(1);
        allAuditViewEntries.put(entryInList.hashCode(), entryInList);

        // when
        boolean relevant = auditService.isAuditViewEntryRelevant(newEntry, allAuditViewEntries);

        // then
        assertThat(relevant, is(true));
    }

    @Test
    public void shouldReturnFalseIfEntryIsAlreadyInList(){
        // given
        String oldValue = "Technical Key: appLogLevel-tmp";
        String newValue = "Technical Key: appLogLevel";
        MyRevisionEntity revisionEntity = new MyRevisionEntity();
        AuditViewEntry entryInList = AuditViewEntry.builder(revisionEntity, RevisionType.MOD)
                .oldValue(oldValue)
                .value(newValue)
                .type(EMPTY)
                .build();
        Map<Integer, AuditViewEntry> allAuditViewEntries = new HashMap<>(1);
        allAuditViewEntries.put(entryInList.hashCode(), entryInList);

        // when
        boolean relevant = auditService.isAuditViewEntryRelevant(entryInList, allAuditViewEntries);

        // then
        assertThat(relevant, is(false));
    }

    @Test
    public void shouldReturnTrueIfRevisionTypeIsAdd(){
        // given
        String value = EMPTY;
        MyRevisionEntity revisionEntity = new MyRevisionEntity();
        AuditViewEntry entryInList = AuditViewEntry.builder(revisionEntity, RevisionType.ADD)
                .oldValue(value)
                .value(value)
                .build();
        Map<Integer, AuditViewEntry> allAuditViewEntries = new HashMap<>(1);
        allAuditViewEntries.put(entryInList.hashCode(), entryInList);

        // when
        boolean relevant = auditService.isAuditViewEntryRelevant(entryInList, allAuditViewEntries);

        // then
        assertThat(relevant, is(true));
    }

    @Test
    public void shouldReturnTrueIfRevisionTypeIsDel(){
        // given
        String value = EMPTY;
        MyRevisionEntity revisionEntity = new MyRevisionEntity();
        AuditViewEntry entryInList = AuditViewEntry.builder(revisionEntity, RevisionType.DEL)
                .oldValue(value)
                .value(value)
                .build();
        Map<Integer, AuditViewEntry> allAuditViewEntries = new HashMap<>(1);
        allAuditViewEntries.put(entryInList.hashCode(), entryInList);

        // when
        boolean relevant = auditService.isAuditViewEntryRelevant(entryInList, allAuditViewEntries);

        // then
        assertThat(relevant, is(true));
    }

    @Test
    public void shouldReturnTrueIfTypeIsTemplateDescriptor_valuesEqual(){
        // given
        String value = EMPTY;
        MyRevisionEntity revisionEntity = new MyRevisionEntity();
        AuditViewEntry entryInList = AuditViewEntry.builder(revisionEntity, RevisionType.MOD)
                .oldValue(value)
                .value(value)
                .type(Auditable.TYPE_TEMPLATE_DESCRIPTOR)
                .build();
        Map<Integer, AuditViewEntry> allAuditViewEntries = new HashMap<>(1);

        // when
        boolean relevant = auditService.isAuditViewEntryRelevant(entryInList, allAuditViewEntries);

        // then
        assertThat(relevant, is(true));
    }

    @Test
    public void shouldReturnTrueIfTypeIsTemplateDescriptor_valuesNotEqual(){
        // given
        String value = EMPTY;
        MyRevisionEntity revisionEntity = new MyRevisionEntity();
        AuditViewEntry entryInList = AuditViewEntry.builder(revisionEntity, RevisionType.MOD)
                .oldValue(value)
                .value("abcd")
                .type(Auditable.TYPE_TEMPLATE_DESCRIPTOR)
                .build();
        Map<Integer, AuditViewEntry> allAuditViewEntries = new HashMap<>(1);

        // when
        boolean relevant = auditService.isAuditViewEntryRelevant(entryInList, allAuditViewEntries);

        // then
        assertThat(relevant, is(true));
    }

    @Test
    public void shouldReturnTrueForObfuscatedValuesWhenValuesAreEqual(){
        // given
        MyRevisionEntity revisionEntity = new MyRevisionEntity();
        AuditViewEntry entryInList = AuditViewEntry.builder(revisionEntity, RevisionType.MOD)
                .oldValue("abcd")
                .value("abcd")
                .type(Auditable.TYPE_TEMPLATE_DESCRIPTOR)
                .isObfuscatedValue(true)
                .build();
        Map<Integer, AuditViewEntry> allAuditViewEntries = new HashMap<>(1);

        // when
        boolean relevant = auditService.isAuditViewEntryRelevant(entryInList, allAuditViewEntries);

        // then
        assertThat(relevant, is(true));
    }


    @Test
    public void shouldReturnTrueForObfuscatedValuesWhenValuesAreNotEqual(){
        // given
        String value = EMPTY;
        MyRevisionEntity revisionEntity = new MyRevisionEntity();
        AuditViewEntry entryInList = AuditViewEntry.builder(revisionEntity, RevisionType.MOD)
                .oldValue(value)
                .value("abcd")
                .type(Auditable.TYPE_TEMPLATE_DESCRIPTOR)
                .isObfuscatedValue(true)
                .build();
        Map<Integer, AuditViewEntry> allAuditViewEntries = new HashMap<>(1);

        // when
        boolean relevant = auditService.isAuditViewEntryRelevant(entryInList, allAuditViewEntries);

        // then
        assertThat(relevant, is(true));
    }
}
