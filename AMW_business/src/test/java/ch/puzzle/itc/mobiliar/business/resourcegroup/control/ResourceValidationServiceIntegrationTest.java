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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import ch.puzzle.itc.mobiliar.builders.ResourceEntityBuilder;
import ch.puzzle.itc.mobiliar.builders.ResourceTypeEntityBuilder;
import ch.puzzle.itc.mobiliar.builders.SoftlinkRelationEntityBuilder;
import ch.puzzle.itc.mobiliar.business.resourcegroup.boundary.ResourceLocator;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceTypeEntity;
import ch.puzzle.itc.mobiliar.business.softlinkRelation.entity.SoftlinkRelationEntity;
import ch.puzzle.itc.mobiliar.common.exception.AMWException;
import ch.puzzle.itc.mobiliar.common.exception.ElementAlreadyExistsException;
import ch.puzzle.itc.mobiliar.test.testrunner.PersistenceTestExtension;

/**
 * Tests for {@link ResourceValidationService}
 */
@ExtendWith(PersistenceTestExtension.class)
public class ResourceValidationServiceIntegrationTest {

    @Spy
    @PersistenceContext
    protected EntityManager entityManager;

    @InjectMocks
    ResourcesScreenQueries queries;

    @InjectMocks
    ResourceValidationService service;

    @BeforeEach
    public void before() {
        MockitoAnnotations.openMocks(this);
        service.queries = queries;
    }

    @Test
    public void testValidateResourceName() throws Exception {
        // given
        ResourceEntity resourceA = new ResourceEntityBuilder().withName("test").build();
        ResourceEntity resourceB = new ResourceEntityBuilder().withName("amw").build();

        entityManager.persist(resourceA);
        entityManager.persist(resourceB);

        assertNotNull(entityManager.find(ResourceEntity.class, resourceA.getId()));

        // when
        assertThrows(AMWException.class, () -> {
            service.validateResourceName("amw", resourceA.getId());
        });
    }

    @Test
    public void testRenameResourceNameToSame() throws Exception {
        // given
        ResourceEntity resourceA = new ResourceEntityBuilder().withName("test").build();

        entityManager.persist(resourceA);

        assertNotNull(entityManager.find(ResourceEntity.class, resourceA.getId()));

        // when
        service.validateResourceName("test", resourceA.getId());
    }

    @Test
    public void shouldAllowToRenameAResourceToANameContainingUnderscore() throws Exception {
        // given
        String resourceName = "amw-wma";
        String newResourceName = "amw_wma";
        ResourceEntity resourceA = new ResourceEntityBuilder().withName(resourceName).build();
        entityManager.persist(resourceA);

        assertNotNull(entityManager.find(ResourceEntity.class, resourceA.getId()));

        // when
        service.validateResourceName(newResourceName, resourceA.getId());
    }

    @Test
    public void shouldAllowToAlterCaseOfSameResource() throws Exception {
        // given
        String resourceName = "amw";
        String newResourceName = "AMW";
        ResourceEntity resourceA = new ResourceEntityBuilder().withName(resourceName).build();
        entityManager.persist(resourceA);

        assertNotNull(entityManager.find(ResourceEntity.class, resourceA.getId()));

        // when
        service.validateResourceName(newResourceName, resourceA.getId());
    }

    @Test
    public void shouldNotAllowToAlterCaseOfDifferentResource() throws AMWException {
        // given
        ResourceEntity resourceA = new ResourceEntityBuilder().withName("myapp").build();
        entityManager.persist(resourceA);

        ResourceEntity resourceB = new ResourceEntityBuilder().withName("myapp1").build();
        entityManager.persist(resourceB);
        assertThrows(AMWException.class, () -> {
            service.validateResourceName("myApp", resourceB.getId());
        });
    }

    @Test
    public void testValidateResourceName_emptyName() throws Exception {
        assertThrows(AMWException.class, () -> {
            // when
            service.validateResourceName(null, null);
        });
    }

    @Test
    public void testValidateResourceName_emptyId() throws Exception {
        assertThrows(AMWException.class, () -> {
            // when
            service.validateResourceName("test", null);
        });
    }

    @Test
    public void testValidateResourceTypeName_emptyName() throws Exception {
        assertThrows(AMWException.class, () -> {
            // when
            service.validateResourceTypeName(null, "oldResourceTypeName");
        });
    }

    @Test
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
        assertThrows(ElementAlreadyExistsException.class, () -> {
            service.validateResourceTypeName(typeNameB, typeNameA);
        });
    }

    @Test
    public void shouldAllowToRenameAResourceTypeToANameContainingUnderscore() throws Exception {
        // given
        String typeName = "type-amw";
        String newTypeName = "type_amw";
        ResourceTypeEntity typeA = new ResourceTypeEntityBuilder().buildResourceTypeEntity(typeName, null, false);
        entityManager.persist(typeA);

        assertNotNull(entityManager.find(ResourceTypeEntity.class, typeA.getId()));

        // when
        service.validateResourceTypeName(newTypeName, typeName);
    }

    @Test
    public void shouldAllowToAlterCaseOfResourceTypeName() throws Exception {
        // given
        String typeName = "typeamw";
        String newTypeName = "typeAMW";
        ResourceTypeEntity typeA = new ResourceTypeEntityBuilder().buildResourceTypeEntity(typeName, null, false);
        entityManager.persist(typeA);

        assertNotNull(entityManager.find(ResourceTypeEntity.class, typeA.getId()));

        // when
        service.validateResourceTypeName(newTypeName, typeName);
    }

    @Test
    public void testValidateSoftlinkId() throws Exception {
        // given
        String softlinkref = "softlinkRef";
        ResourceTypeEntity cpiType = new ResourceTypeEntityBuilder()
                .buildResourceTypeEntity(ResourceLocator.WS_CPI_TYPE, null, false);
        entityManager.persist(cpiType);
        ResourceTypeEntity ppiType = new ResourceTypeEntityBuilder()
                .buildResourceTypeEntity(ResourceLocator.WS_PPI_TYPE, null, false);
        entityManager.persist(ppiType);
        ResourceEntity cpi = new ResourceEntityBuilder().withType(cpiType).withName("cpi").build();
        entityManager.persist(cpi);
        ResourceEntity ppi = new ResourceEntityBuilder().withType(cpiType).withName("ppi").withSoftlinkId(softlinkref)
                .build();
        entityManager.persist(ppi);
        SoftlinkRelationEntity softlinkRel = new SoftlinkRelationEntityBuilder().withSoftlinkRef("softlinkref")
                .withCpiResource(cpi).build();
        entityManager.persist(softlinkRel);
        ResourceEntity ppi2 = new ResourceEntityBuilder().withType(cpiType).withName("ppi2").build();
        entityManager.persist(ppi2);

        assertNotNull(entityManager.find(SoftlinkRelationEntity.class, softlinkRel.getId()));

        // when
        assertThrows(AMWException.class, () -> {
            service.validateSoftlinkId(softlinkref, ppi2.getResourceGroup().getId());
        });
    }

    @Test
    public void testValidateSoftlinkId_emptySoftlinkId() throws Exception {
        // given
        String softlinkref = "softlinkRef";
        ResourceTypeEntity cpiType = new ResourceTypeEntityBuilder()
                .buildResourceTypeEntity(ResourceLocator.WS_CPI_TYPE, null, false);
        entityManager.persist(cpiType);
        ResourceTypeEntity ppiType = new ResourceTypeEntityBuilder()
                .buildResourceTypeEntity(ResourceLocator.WS_PPI_TYPE, null, false);
        entityManager.persist(ppiType);
        ResourceEntity cpi = new ResourceEntityBuilder().withType(cpiType).withName("cpi").build();
        entityManager.persist(cpi);
        ResourceEntity ppi = new ResourceEntityBuilder().withType(cpiType).withName("ppi").withSoftlinkId(softlinkref)
                .build();
        entityManager.persist(ppi);
        SoftlinkRelationEntity softlinkRel = new SoftlinkRelationEntityBuilder().withSoftlinkRef("softlinkref")
                .withCpiResource(cpi).build();
        entityManager.persist(softlinkRel);

        assertNotNull(entityManager.find(SoftlinkRelationEntity.class, softlinkRel.getId()));

        // when
        service.validateSoftlinkId(null, ppi.getResourceGroup().getId());
    }

    @Test
    public void testValidateSoftlinkId_emptyResourceGroupId() throws Exception {
        // given
        String softlinkref = "softlinkRef";
        ResourceTypeEntity cpiType = new ResourceTypeEntityBuilder()
                .buildResourceTypeEntity(ResourceLocator.WS_CPI_TYPE, null, false);
        entityManager.persist(cpiType);
        ResourceTypeEntity ppiType = new ResourceTypeEntityBuilder()
                .buildResourceTypeEntity(ResourceLocator.WS_PPI_TYPE, null, false);
        entityManager.persist(ppiType);
        ResourceEntity cpi = new ResourceEntityBuilder().withType(cpiType).withName("cpi").build();
        entityManager.persist(cpi);
        ResourceEntity ppi = new ResourceEntityBuilder().withType(cpiType).withName("ppi").withSoftlinkId(softlinkref)
                .build();
        entityManager.persist(ppi);
        SoftlinkRelationEntity softlinkRel = new SoftlinkRelationEntityBuilder().withSoftlinkRef("softlinkref")
                .withCpiResource(cpi).build();
        entityManager.persist(softlinkRel);

        assertNotNull(entityManager.find(SoftlinkRelationEntity.class, softlinkRel.getId()));

        // when
        assertThrows(AMWException.class, () -> {
            service.validateSoftlinkId(softlinkref, null);
        });
    }
}