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

import ch.puzzle.itc.mobiliar.builders.ResourceEntityBuilder;
import ch.puzzle.itc.mobiliar.builders.ResourceTypeEntityBuilder;
import ch.puzzle.itc.mobiliar.builders.SoftlinkRelationEntityBuilder;
import ch.puzzle.itc.mobiliar.business.resourcegroup.boundary.ResourceLocator;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceTypeEntity;
import ch.puzzle.itc.mobiliar.business.softlinkRelation.entity.SoftlinkRelationEntity;
import ch.puzzle.itc.mobiliar.common.exception.AMWException;
import ch.puzzle.itc.mobiliar.common.exception.ElementAlreadyExistsException;
import ch.puzzle.itc.mobiliar.test.testrunner.PersistenceTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import static org.junit.Assert.assertNotNull;

/**
 * Tests for {@link ResourceValidationService}
 */
@RunWith(PersistenceTestRunner.class)
public class ResourceValidationServiceIntegrationTest {

    @Spy
    @PersistenceContext
    protected EntityManager entityManager;

    @InjectMocks
    ResourcesScreenQueries queries;

    @InjectMocks
    ResourceValidationService service;

    @Before
    public void before() {
        MockitoAnnotations.initMocks(this);
        service.queries = queries;
    }

    @Test(expected = AMWException.class)
    public void testValidateResourceName() throws Exception {
        // given
        String resourceName = "amw";
        ResourceEntity resourceA = new ResourceEntityBuilder().withName(resourceName).build();
        entityManager.persist(resourceA);

        assertNotNull(entityManager.find(ResourceEntity.class, resourceA.getId()));

        // when
        service.validateResourceName(resourceName);
    }

    @Test(expected = AMWException.class)
    public void testValidateResourceName_emptyName() throws Exception {
        // when
        service.validateResourceName(null);
    }

    @Test(expected = AMWException.class)
    public void testValidateResourceTypeName_emptyName() throws Exception {
        // when
        service.validateResourceTypeName(null, "oldResourceTypeName");
    }

    @Test(expected = ElementAlreadyExistsException.class)
    public void testValidateResourceTypeName() throws Exception {
        // given
        String typeNameA = "typeA";
        ResourceTypeEntity typeA = new ResourceTypeEntityBuilder().buildResourceTypeEntity(typeNameA, null, false);

        String typeNameB = "typeB";
        ResourceTypeEntity typeB = new ResourceTypeEntityBuilder().buildResourceTypeEntity(typeNameB, null, false);

        entityManager.persist(typeA);
        entityManager.persist(typeB);

        assertNotNull(entityManager.find(ResourceTypeEntity.class, typeA.getId()));
        assertNotNull(entityManager.find(ResourceTypeEntity.class, typeB.getId()));

        // when
        service.validateResourceTypeName(typeNameB, typeNameA);
    }

    @Test(expected = AMWException.class)
    public void testValidateSoftlinkId() throws Exception {
        // given
        String softlinkref = "softlinkRef";
        ResourceTypeEntity cpiType = new ResourceTypeEntityBuilder().buildResourceTypeEntity(ResourceLocator.WS_CPI_TYPE, null, false);
        entityManager.persist(cpiType);
        ResourceTypeEntity ppiType = new ResourceTypeEntityBuilder().buildResourceTypeEntity(ResourceLocator.WS_PPI_TYPE, null, false);
        entityManager.persist(ppiType);
        ResourceEntity cpi = new ResourceEntityBuilder().withType(cpiType).withName("cpi").build();
        entityManager.persist(cpi);
        ResourceEntity ppi = new ResourceEntityBuilder().withType(cpiType).withName("ppi").withSoftlinkId(softlinkref).build();
        entityManager.persist(ppi);
        SoftlinkRelationEntity softlinkRel = new SoftlinkRelationEntityBuilder().withSoftlinkRef("softlinkref").withCpiResource(cpi).build();
        entityManager.persist(softlinkRel);
        ResourceEntity ppi2 = new ResourceEntityBuilder().withType(cpiType).withName("ppi2").build();
        entityManager.persist(ppi2);

        assertNotNull(entityManager.find(SoftlinkRelationEntity.class, softlinkRel.getId()));

        // when
        service.validateSoftlinkId(softlinkref, ppi2.getResourceGroup().getId());
    }


    @Test
    public void testValidateSoftlinkId_emptySoftlinkId() throws Exception {
        // given
        String softlinkref = "softlinkRef";
        ResourceTypeEntity cpiType = new ResourceTypeEntityBuilder().buildResourceTypeEntity(ResourceLocator.WS_CPI_TYPE, null, false);
        entityManager.persist(cpiType);
        ResourceTypeEntity ppiType = new ResourceTypeEntityBuilder().buildResourceTypeEntity(ResourceLocator.WS_PPI_TYPE, null, false);
        entityManager.persist(ppiType);
        ResourceEntity cpi = new ResourceEntityBuilder().withType(cpiType).withName("cpi").build();
        entityManager.persist(cpi);
        ResourceEntity ppi = new ResourceEntityBuilder().withType(cpiType).withName("ppi").withSoftlinkId(softlinkref).build();
        entityManager.persist(ppi);
        SoftlinkRelationEntity softlinkRel = new SoftlinkRelationEntityBuilder().withSoftlinkRef("softlinkref").withCpiResource(cpi).build();
        entityManager.persist(softlinkRel);

        assertNotNull(entityManager.find(SoftlinkRelationEntity.class, softlinkRel.getId()));

        // when
        service.validateSoftlinkId(null, ppi.getResourceGroup().getId());
    }

    @Test(expected = AMWException.class)
    public void testValidateSoftlinkId_emptyResourceGroupId() throws Exception {
        // given
        String softlinkref = "softlinkRef";
        ResourceTypeEntity cpiType = new ResourceTypeEntityBuilder().buildResourceTypeEntity(ResourceLocator.WS_CPI_TYPE, null, false);
        entityManager.persist(cpiType);
        ResourceTypeEntity ppiType = new ResourceTypeEntityBuilder().buildResourceTypeEntity(ResourceLocator.WS_PPI_TYPE, null, false);
        entityManager.persist(ppiType);
        ResourceEntity cpi = new ResourceEntityBuilder().withType(cpiType).withName("cpi").build();
        entityManager.persist(cpi);
        ResourceEntity ppi = new ResourceEntityBuilder().withType(cpiType).withName("ppi").withSoftlinkId(softlinkref).build();
        entityManager.persist(ppi);
        SoftlinkRelationEntity softlinkRel = new SoftlinkRelationEntityBuilder().withSoftlinkRef("softlinkref").withCpiResource(cpi).build();
        entityManager.persist(softlinkRel);

        assertNotNull(entityManager.find(SoftlinkRelationEntity.class, softlinkRel.getId()));

        // when
        service.validateSoftlinkId(softlinkref, null);
    }
}