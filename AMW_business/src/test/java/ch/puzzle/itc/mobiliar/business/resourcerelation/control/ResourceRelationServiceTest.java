package ch.puzzle.itc.mobiliar.business.resourcerelation.control;

import ch.puzzle.itc.mobiliar.builders.ResourceEntityBuilder;
import ch.puzzle.itc.mobiliar.builders.ResourceGroupEntityBuilder;
import ch.puzzle.itc.mobiliar.business.auditview.control.AuditService;
import ch.puzzle.itc.mobiliar.business.auditview.control.ThreadLocalUtil;
import ch.puzzle.itc.mobiliar.business.integration.entity.util.ResourceTypeEntityBuilder;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceGroupEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceTypeEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceFactory;
import ch.puzzle.itc.mobiliar.business.resourcegroup.control.ResourceTypeProvider;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ResourceRelationTypeEntity;
import ch.puzzle.itc.mobiliar.business.security.control.PermissionService;
import ch.puzzle.itc.mobiliar.common.exception.ElementAlreadyExistsException;
import ch.puzzle.itc.mobiliar.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.Spy;

import javax.persistence.EntityManager;
import java.util.Collections;
import java.util.Set;
import java.util.HashSet;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
public class ResourceRelationServiceTest {

    @Mock
    EntityManager entityManager;

    @Mock
    PermissionService permissionService;

    @Mock
    ResourceTypeProvider resourceTypeProvider;

    @Mock
    Logger log;

    @Spy
    AuditService auditService = spy(new AuditService());

    @Spy
    @InjectMocks
    ResourceRelationService resourceRelationService = spy(new ResourceRelationService());

    @BeforeEach
    public void setUp() {
        ThreadLocalUtil.destroy();
    }

    @Test
    public void storeResourceWhenAddRelationByGroup_provided() throws ResourceNotFoundException, ElementAlreadyExistsException {
        // given
        int resourceId = 5555;
        int slaveId = 2;
        boolean provided = true;
        String relationName = "ad22";
        String typeIdentifier = "tytype";
        ResourceTypeEntity resourceType = new ResourceTypeEntityBuilder().name("typename").build();
        ResourceEntity resourceEntity = new ResourceEntityBuilder().withId(resourceId).withType(resourceType).build();
        ResourceGroupEntity resourceGroupEntity = new ResourceGroupEntityBuilder().buildResourceGroupEntity("myName", Collections.EMPTY_SET, true);
        resourceGroupEntity.setResourceType(resourceType);
        when(entityManager.find(ResourceEntity.class, resourceId)).thenReturn(resourceEntity);
        when(entityManager.find(ResourceGroupEntity.class, slaveId)).thenReturn(resourceGroupEntity);
        doNothing().when(resourceRelationService).doAddResourceRelationForAllReleases(resourceId, slaveId, provided, relationName, typeIdentifier);

        // when
        resourceRelationService.addRelationByGroup(resourceId, slaveId, provided, relationName, typeIdentifier);

        // then
        assertThat((Integer) ThreadLocalUtil.getThreadVariable(ThreadLocalUtil.KEY_RESOURCE_ID), is(resourceId));
    }

    @Test
    public void storeResourceWhenAddRelationByGroup_consumed() throws ResourceNotFoundException, ElementAlreadyExistsException {
        // given
        int resourceId = 5555;
        int slaveId = 2;
        boolean provided = false;
        String relationName = "ad22";
        String typeIdentifier = "tytype";
        ResourceTypeEntity resourceType = new ResourceTypeEntityBuilder().name("typename").build();
        ResourceEntity resourceEntity = new ResourceEntityBuilder().withId(resourceId).withType(resourceType).build();
        ResourceGroupEntity resourceGroupEntity = new ResourceGroupEntityBuilder().buildResourceGroupEntity("myName", Collections.EMPTY_SET, true);
        resourceGroupEntity.setResourceType(resourceType);
        when(entityManager.find(ResourceEntity.class, resourceId)).thenReturn(resourceEntity);
        when(entityManager.find(ResourceGroupEntity.class, slaveId)).thenReturn(resourceGroupEntity);
        doNothing().when(resourceRelationService).doAddResourceRelationForAllReleases(resourceId, slaveId, provided, relationName, typeIdentifier);

        // when
        resourceRelationService.addRelationByGroup(resourceId, slaveId, provided, relationName, typeIdentifier);

        // then
        assertThat((Integer) ThreadLocalUtil.getThreadVariable(ThreadLocalUtil.KEY_RESOURCE_ID), is(resourceId));
    }

    @Test
    public void shouldGenerateDistinctNamesAcrossSlaveReleases() throws Exception {
        ResourceEntity master = prepareRelationResources("APPLICATION");

        resourceRelationService.doAddResourceRelationForAllReleases(1, 2, false, null, null);
        resourceRelationService.doAddResourceRelationForAllReleases(1, 2, false, null, null);
        resourceRelationService.doAddResourceRelationForAllReleases(1, 2, false, null, null);

        assertEquals(6, master.getConsumedMasterRelations().size());
        assertEquals(Set.of("slave", "slave_1", "slave_2"), master.getConsumedMasterRelations().stream()
                .map(relation -> relation.getIdentifier()).collect(Collectors.toSet()));
    }

    @Test
    public void shouldSkipExistingSuffixesAndPreserveExplicitNames() throws Exception {
        ResourceEntity master = prepareRelationResources("APPLICATION");
        resourceRelationService.doAddResourceRelationForAllReleases(1, 2, false, "slave_1", null);
        resourceRelationService.doAddResourceRelationForAllReleases(1, 2, false, null, null);

        assertEquals(Set.of("slave_1", "slave_2"), master.getConsumedMasterRelations().stream()
                .map(relation -> relation.getIdentifier()).collect(Collectors.toSet()));
        assertThrows(ElementAlreadyExistsException.class,
                () -> resourceRelationService.doAddResourceRelationForAllReleases(1, 2, false, "slave_1", null));
    }

    @Test
    public void shouldPreserveUnnamedRelationsForCustomResourceTypes() throws Exception {
        ResourceEntity master = prepareRelationResources("custom");

        resourceRelationService.doAddResourceRelationForAllReleases(1, 2, false, null, null);

        assertEquals(Collections.singleton(null), master.getConsumedMasterRelations().stream()
                .map(relation -> relation.getIdentifier()).collect(Collectors.toSet()));
    }

    @Test
    public void shouldSuffixRelationsCreatedByTheOldUiWithoutAnIdentifier() throws Exception {
        ResourceEntity master = prepareRelationResources("APPLICATION");
        ResourceGroupEntity slaveGroup = entityManager.find(ResourceGroupEntity.class, 2);
        ResourceRelationTypeEntity relationType = new ResourceRelationTypeEntity();
        relationType.setResourceTypes(master.getResourceType(), slaveGroup.getResourceType());
        slaveGroup.getResources().forEach(slave -> master.addConsumedResourceRelation(slave, relationType, null));

        resourceRelationService.doAddResourceRelationForAllReleases(1, 2, false, null, null);

        assertEquals(2, master.getConsumedMasterRelations().stream()
                .filter(relation -> "slave_1".equals(relation.getIdentifier())).count());
    }

    private ResourceEntity prepareRelationResources(String masterTypeName) {
        ResourceEntity master = ResourceFactory.createNewResource("master");
        master.setId(1);
        master.setResourceType(new ResourceTypeEntityBuilder().name(masterTypeName).build());
        ResourceEntity slave = ResourceFactory.createNewResource("slave");
        slave.setId(3);
        slave.setResourceType(new ResourceTypeEntityBuilder().name("Database").build());
        slave.getResourceGroup().setId(2);
        ResourceEntity otherRelease = ResourceFactory.createNewResource(slave.getResourceGroup());
        otherRelease.setId(4);
        otherRelease.setResourceType(slave.getResourceType());
        slave.getResourceGroup().setResources(new HashSet<>(Set.of(slave, otherRelease)));
        ResourceRelationTypeEntity relationType = new ResourceRelationTypeEntity();
        relationType.setResourceTypes(master.getResourceType(), slave.getResourceType());
        when(entityManager.find(ResourceEntity.class, 1)).thenReturn(master);
        when(entityManager.find(ResourceGroupEntity.class, 2)).thenReturn(slave.getResourceGroup());
        when(resourceTypeProvider.getOrCreateResourceRelationTypeIncludingParents(
                master.getResourceType(), slave.getResourceType(), null)).thenReturn(relationType);
        return master;
    }
}
