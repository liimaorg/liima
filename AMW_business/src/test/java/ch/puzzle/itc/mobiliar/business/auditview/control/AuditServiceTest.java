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
import ch.puzzle.itc.mobiliar.business.property.entity.PropertyDescriptorEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceTypeEntity;
import ch.puzzle.itc.mobiliar.business.shakedown.entity.ShakedownTestEntity;
import ch.puzzle.itc.mobiliar.test.testrunner.PersistenceTestRunner;
import org.hamcrest.MatcherAssert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import static junit.framework.TestCase.assertNull;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@RunWith(PersistenceTestRunner.class)
public class AuditServiceTest {

    @InjectMocks
    private AuditService auditService;

    @PersistenceContext
    EntityManager entityManager;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        auditService.entityManager = entityManager;
        ThreadLocalUtil.destroy();
    }

    @Test
    public void shouldReturnNullIfEntityIsNotAudited() {
        // given
        ShakedownTestEntity shakedownTest = new ShakedownTestEntity();
        Integer id = 1;

        // when
        Object deletedEntity = auditService.getDeletedEntity(shakedownTest, id);

        // then
        assertNull(deletedEntity);
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
        int contextId = 1;

        // when
        auditService.setResourceTypeIdInThreadLocal(resourceTypeId, contextId);

        // then
        MatcherAssert.assertThat((Integer) ThreadLocalUtil.getThreadVariable(ThreadLocalUtil.KEY_RESOURCE_TYPE_ID), is(resourceTypeId));
    }

    @Test
    public void shouldSetContextIdInThreadLocalDuringSetResourceTypeIdInThreadLocal() {
        // given
        int resourceTypeId = 700;
        int contextId = 9;

        // when
        auditService.setResourceTypeIdInThreadLocal(resourceTypeId, contextId);

        // then
        MatcherAssert.assertThat((Integer) ThreadLocalUtil.getThreadVariable(ThreadLocalUtil.KEY_EDIT_CONTEXT_ID), is(contextId));
    }

    @Test
    public void shouldSetResourceIdInThreadLocal() {
        // given
        int resourceTypeId = 700;
        int contextId = 1;

        // when
        auditService.setResourceIdInThreadLocal(resourceTypeId, contextId);

        // then
        MatcherAssert.assertThat((Integer) ThreadLocalUtil.getThreadVariable(ThreadLocalUtil.KEY_RESOURCE_ID), is(resourceTypeId));
    }

    @Test
    public void shouldSetContextIdInThreadLocalDuringSetResourceIdInThreadLocal() {
        // given
        int resourceId = 700;
        int contextId = 9;

        // when
        auditService.setResourceIdInThreadLocal(resourceId, contextId);

        // then
        MatcherAssert.assertThat((Integer) ThreadLocalUtil.getThreadVariable(ThreadLocalUtil.KEY_EDIT_CONTEXT_ID), is(contextId));
    }

    @Test
    public void shouldSetResourceTypeIdInThreadLocalDuringStoreIdInThreadLocalForAuditLog_resourceType() {
        // given
        int resourceTypeId = 700;
        ResourceTypeEntity resourceType = new ResourceTypeEntity();
        resourceType.setId(resourceTypeId);
        int contextId = 1;

        // when
        auditService.storeIdInThreadLocalForAuditLog(resourceType, contextId);

        // then
        MatcherAssert.assertThat((Integer) ThreadLocalUtil.getThreadVariable(ThreadLocalUtil.KEY_RESOURCE_TYPE_ID), is(resourceTypeId));
    }

    @Test
    public void shouldSetContextIdInThreadLocalDuringStoreIdInThreadLocalForAuditLog_resourceType() {
        // given
        int resourceTypeId = 700;
        ResourceTypeEntity resourceType = new ResourceTypeEntity();
        resourceType.setId(resourceTypeId);
        int contextId = 9;

        // when
        auditService.storeIdInThreadLocalForAuditLog(resourceType, contextId);

        // then
        MatcherAssert.assertThat((Integer) ThreadLocalUtil.getThreadVariable(ThreadLocalUtil.KEY_EDIT_CONTEXT_ID), is(contextId));
    }

    @Test
    public void shouldSetResourceIdInThreadLocalDuringStoreIdInThreadLocalForAuditLog_resource() {
        // given
        int resourceId = 700;
        ResourceEntity resource = new ResourceEntity();
        resource.setId(resourceId);
        int contextId = 1;

        // when
        auditService.storeIdInThreadLocalForAuditLog(resource, contextId);

        // then
        MatcherAssert.assertThat((Integer) ThreadLocalUtil.getThreadVariable(ThreadLocalUtil.KEY_RESOURCE_ID), is(resourceId));
    }

    @Test
    public void shouldSetContextIdInThreadLocalDuringStoreIdInThreadLocalForAuditLog_resource() {
        // given
        int resourceId = 700;
        ResourceEntity resource = new ResourceEntity();
        resource.setId(resourceId);
        int contextId = 1;

        // when
        auditService.storeIdInThreadLocalForAuditLog(resource, contextId);

        // then
        MatcherAssert.assertThat((Integer) ThreadLocalUtil.getThreadVariable(ThreadLocalUtil.KEY_EDIT_CONTEXT_ID), is(contextId));
    }






}
