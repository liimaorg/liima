/*
 * AMW - Automated Middleware allows you to manage the configurations of
 * your Java EE applications on an unlimited number of different environments
 * with various versions, including the automated deployment of those apps.
 * Copyright (C) 2013-2026 by Puzzle ITC
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

import ch.puzzle.itc.mobiliar.builders.ResourceEntityBuilder;
import ch.puzzle.itc.mobiliar.builders.ResourceRelationEntityBuilder;
import ch.puzzle.itc.mobiliar.business.auditview.control.AuditService;
import ch.puzzle.itc.mobiliar.business.property.control.PropertyValueService;
import ch.puzzle.itc.mobiliar.business.property.entity.ResourceEditProperty;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceGroupEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceTypeEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.control.ResourceRelationService;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.AbstractResourceRelationEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ConsumedResourceRelationEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ProvidedResourceRelationEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ResourceRelationTypeEntity;
import ch.puzzle.itc.mobiliar.business.security.boundary.PermissionBoundary;
import ch.puzzle.itc.mobiliar.common.exception.NotFoundException;
import ch.puzzle.itc.mobiliar.common.exception.ResourceNotFoundException;
import ch.puzzle.itc.mobiliar.common.exception.ValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.persistence.EntityManager;
import java.util.Collections;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UpdateRelationPropertiesServiceTest {

    @InjectMocks
    UpdateRelationPropertiesService service;

    @Mock
    EntityManager entityManager;

    @Mock
    ResourceRelationService resourceRelationService;

    @Mock
    PropertyEditor propertyEditor;

    @Mock
    PropertyValueService propertyValueService;

    @Mock
    PermissionBoundary permissionBoundary;

    @Mock
    AuditService auditService;

    private ResourceTypeEntity createResourceType(String name) {
        ResourceTypeEntity type = new ResourceTypeEntity();
        type.setName(name);
        return type;
    }

    private ResourceEntity createResource(String name, ResourceTypeEntity type, Integer id) {
        ResourceGroupEntity group = new ResourceGroupEntity();
        group.setId(id);
        group.setName(name);

        ResourceEntity resource = new ResourceEntity();
        resource.setId(id);
        resource.setResourceGroup(group);
        resource.setResourceType(type);
        return resource;
    }

    private ResourceEntity createResourceWithRelease(String name, ResourceTypeEntity type, Integer id,
                                                      Integer releaseId, String releaseName) {
        ResourceGroupEntity group = new ResourceGroupEntity();
        group.setId(id);
        group.setName(name);

        ResourceEntity resource = new ResourceEntity();
        resource.setId(id);
        resource.setResourceGroup(group);
        resource.setResourceType(type);

        ch.puzzle.itc.mobiliar.business.releasing.entity.ReleaseEntity release =
                new ch.puzzle.itc.mobiliar.business.releasing.entity.ReleaseEntity();
        release.setId(releaseId);
        release.setName(releaseName);
        resource.setRelease(release);

        return resource;
    }

    private ConsumedResourceRelationEntity createConsumedRelation(Integer id, String identifier,
                                                                   ResourceEntity master, ResourceEntity slave,
                                                                   ResourceRelationTypeEntity relType) {
        ConsumedResourceRelationEntity relation = new ConsumedResourceRelationEntity();
        relation.setId(id);
        relation.setIdentifier(identifier);
        relation.setMasterResource(master);
        relation.setSlaveResource(slave);
        relation.setResourceRelationType(relType);
        master.addConsumedRelation(relation);
        return relation;
    }

    private ResourceRelationTypeEntity createRelationType(Integer id, String identifier,
                                                         ResourceTypeEntity typeA, ResourceTypeEntity typeB) {
        ResourceRelationTypeEntity relType = new ResourceRelationTypeEntity();
        relType.setId(id);
        relType.setIdentifier(identifier);
        relType.setResourceTypes(typeA, typeB);
        return relType;
    }

    @Test
    public void shouldThrowResourceNotFoundWhenRelationDoesNotExist() {
        // given
        when(resourceRelationService.getResourceRelation(99)).thenReturn(null);

        // when / then
        assertThrows(ResourceNotFoundException.class,
                () -> service.updateResourceRelationIdentifier(99, "newIdentifier"));
    }

    @Test
    public void shouldThrowValidationExceptionWhenNewIdentifierIsNull() {
        // when / then
        assertThrows(ValidationException.class,
                () -> service.updateResourceRelationIdentifier(1, null));
    }

    @Test
    public void shouldThrowValidationExceptionWhenNewIdentifierIsEmpty() {
        // when / then
        assertThrows(ValidationException.class,
                () -> service.updateResourceRelationIdentifier(1, ""));
    }

    @Test
    public void shouldUpdateRelationIdentifierWhenChanged() throws Exception {
        // given - use non-default resource type to avoid qualified identifier logic
        ResourceTypeEntity masterType = createResourceType("CUSTOM_MASTER");
        ResourceTypeEntity slaveType = createResourceType("DATABASE");
        ResourceEntity master = createResourceWithRelease("master", masterType, 1, 100, "Release 1.0");
        ResourceEntity slave = createResourceWithRelease("slave", slaveType, 2, 101, "Release 1.0");
        ResourceRelationTypeEntity relType = createRelationType(50, "typeId", masterType, slaveType);
        ConsumedResourceRelationEntity relation = createConsumedRelation(10, "oldIdentifier", master, slave, relType);

        // Mock is called 3 times: twice in updateResourceRelationIdentifier, once in handleRelationIdentifierUpdate via getConsumedRelation
        when(resourceRelationService.getResourceRelation(10)).thenReturn(relation, relation, relation);

        // when
        service.updateResourceRelationIdentifier(10, "newIdentifier");

        // then - verify identifier was changed on the relation
        assertEquals("newIdentifier", relation.getIdentifier());
    }

    @Test
    public void shouldNotUpdateWhenIdentifierUnchanged() throws Exception {
        // given - use non-default resource type to avoid qualified identifier logic
        ResourceTypeEntity masterType = createResourceType("CUSTOM_MASTER");
        ResourceTypeEntity slaveType = createResourceType("DATABASE");
        ResourceEntity master = createResourceWithRelease("master", masterType, 1, 100, "Release 1.0");
        ResourceEntity slave = createResourceWithRelease("slave", slaveType, 2, 101, "Release 1.0");
        ResourceRelationTypeEntity relType = createRelationType(50, "typeId", masterType, slaveType);
        ConsumedResourceRelationEntity relation = createConsumedRelation(10, "sameIdentifier", master, slave, relType);

        when(resourceRelationService.getResourceRelation(10)).thenReturn(relation);

        // when
        service.updateResourceRelationIdentifier(10, "sameIdentifier");

        // then
        assertEquals("sameIdentifier", relation.getIdentifier());
        verify(entityManager, never()).merge(any(ConsumedResourceRelationEntity.class));
    }

    @Test
    public void shouldThrowNotFoundWhenResourceTypeRelationDoesNotExist() {
        // given
        when(entityManager.find(ResourceRelationTypeEntity.class, 99)).thenReturn(null);

        // when / then
        assertThrows(NotFoundException.class,
                () -> service.updateResourceTypeRelationIdentifier(99, "newIdentifier"));
    }

    @Test
    public void shouldThrowValidationExceptionWhenTypeRelationIdentifierIsNull() {
        // when / then
        assertThrows(ValidationException.class,
                () -> service.updateResourceTypeRelationIdentifier(1, null));
    }

    @Test
    public void shouldThrowValidationExceptionWhenTypeRelationIdentifierIsEmpty() {
        // when / then
        assertThrows(ValidationException.class,
                () -> service.updateResourceTypeRelationIdentifier(1, ""));
    }

    @Test
    public void shouldUpdateResourceTypeRelationIdentifierWhenChanged() throws Exception {
        // given - use non-default resource type for master to avoid qualified identifier logic
        ResourceTypeEntity typeA = createResourceType("CUSTOM_TYPE");
        ResourceTypeEntity typeB = createResourceType("DATABASE");
        ResourceRelationTypeEntity relationType = createRelationType(20, "oldTypeId", typeA, typeB);

        when(entityManager.find(ResourceRelationTypeEntity.class, 20)).thenReturn(relationType);

        // when
        service.updateResourceTypeRelationIdentifier(20, "newTypeId");

        // then
        assertEquals("newTypeId", relationType.getIdentifier());
        verify(entityManager).merge(relationType);
    }

    @Test
    public void shouldNotUpdateResourceTypeRelationWhenIdentifierUnchanged() throws Exception {
        // given - use non-default resource type for master to avoid qualified identifier logic
        ResourceTypeEntity typeA = createResourceType("CUSTOM_TYPE");
        ResourceTypeEntity typeB = createResourceType("DATABASE");
        ResourceRelationTypeEntity relationType = createRelationType(20, "sameId", typeA, typeB);

        when(entityManager.find(ResourceRelationTypeEntity.class, 20)).thenReturn(relationType);

        // when
        service.updateResourceTypeRelationIdentifier(20, "sameId");

        // then
        assertEquals("sameId", relationType.getIdentifier());
        verify(entityManager, never()).merge(any(ResourceRelationTypeEntity.class));
    }
}
