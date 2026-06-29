package ch.puzzle.itc.mobiliar.business.resourcerelation.boundary;

import ch.puzzle.itc.mobiliar.business.property.entity.ResourceEditRelation;
import ch.puzzle.itc.mobiliar.business.property.entity.ResourceEditRelation.Mode;
import ch.puzzle.itc.mobiliar.business.releasing.entity.ReleaseEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceGroupEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceTypeEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.control.ResourceRelationService;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.AbstractResourceRelationEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ConsumedResourceRelationEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ProvidedResourceRelationEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ResourceRelationTypeEntity;
import ch.puzzle.itc.mobiliar.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ResourceRelationMapperTest {

    @InjectMocks
    ResourceRelationMapper mapper;

    @Mock
    ResourceRelationService resourceRelationService;


    @Test
    void toResourceEditRelation_null_throwsResourceNotFoundException() {
        //given
        when(resourceRelationService.getResourceRelation(1)).thenReturn(null);

        //then
        assertThrows(ResourceNotFoundException.class, () -> mapper.toResourceEditRelation(1));
    }

    @Test
    void toResourceEditRelation_consumed_returnsCorrectMode() throws ResourceNotFoundException {
        //given
        ResourceTypeEntity typeA = resourceTypeA();
        ResourceTypeEntity typeB = resourceTypeB();
        ResourceRelationTypeEntity relType = relType(typeA, typeB);
        ResourceEntity slaveResource = slave();

        ConsumedResourceRelationEntity relation = mock(ConsumedResourceRelationEntity.class);
        when(relation.getId()).thenReturn(1);
        when(relation.getSlaveResource()).thenReturn(slaveResource);
        when(relation.getResourceRelationType()).thenReturn(relType);
        when(relation.buildIdentifer()).thenReturn("identifier");

        when(resourceRelationService.getResourceRelation(1)).thenReturn(relation);

        //when
        ResourceEditRelation result = mapper.toResourceEditRelation(1);

        //then
        assertNotNull(result);
        assertEquals(Mode.CONSUMED, result.getMode());
    }

    @Test
    void toResourceEditRelation_provided_returnsCorrectMode() throws ResourceNotFoundException {
        //given
        ResourceTypeEntity typeA = resourceTypeA();
        ResourceTypeEntity typeB = resourceTypeB();
        ResourceRelationTypeEntity relType = relType(typeA, typeB);
        ResourceEntity slaveResource = slave();

        ProvidedResourceRelationEntity relation = mock(ProvidedResourceRelationEntity.class);
        when(relation.getId()).thenReturn(2);
        when(relation.getSlaveResource()).thenReturn(slaveResource);
        when(relation.getResourceRelationType()).thenReturn(relType);
        when(relation.buildIdentifer()).thenReturn("identifier");

        when(resourceRelationService.getResourceRelation(2)).thenReturn(relation);

        //when
        ResourceEditRelation result = mapper.toResourceEditRelation(2);

        //then
        assertNotNull(result);
        assertEquals(Mode.PROVIDED, result.getMode());
    }

    @Test
    void toResourceEditRelation_other_throwsResourceNotFoundException() {
        //given
        AbstractResourceRelationEntity relation = mock(AbstractResourceRelationEntity.class);
        when(relation.getId()).thenReturn(3);
        when(resourceRelationService.getResourceRelation(3)).thenReturn(relation);

        //then
        assertThrows(ResourceNotFoundException.class, () -> mapper.toResourceEditRelation(3));
    }

    private ResourceTypeEntity resourceTypeA() {
        ResourceTypeEntity typeA = mock(ResourceTypeEntity.class);
        when(typeA.getName()).thenReturn("resourceTypeA");
        return typeA;
    }

    private ResourceTypeEntity resourceTypeB() {
        ResourceTypeEntity typeB = mock(ResourceTypeEntity.class);
        when(typeB.getId()).thenReturn(500);
        when(typeB.getName()).thenReturn("resourceTypeB");
        return typeB;
    }

    private ResourceRelationTypeEntity relType(ResourceTypeEntity typeA, ResourceTypeEntity typeB) {
        ResourceRelationTypeEntity relType = mock(ResourceRelationTypeEntity.class);
        when(relType.getId()).thenReturn(400);
        when(relType.getIdentifier()).thenReturn("relationTypeIdentifier");
        when(relType.getResourceTypeA()).thenReturn(typeA);
        when(relType.getResourceTypeB()).thenReturn(typeB);
        return relType;
    }

    private ResourceEntity slave() {
        ResourceGroupEntity resourceGroup = mock(ResourceGroupEntity.class);
        when(resourceGroup.getId()).thenReturn(200);

        ReleaseEntity release = mock(ReleaseEntity.class);
        when(release.getId()).thenReturn(300);
        when(release.getName()).thenReturn("release1");
        when(release.getInstallationInProductionAt()).thenReturn(new Date());

        ResourceEntity slaveResource = mock(ResourceEntity.class);
        when(slaveResource.getId()).thenReturn(100);
        when(slaveResource.getName()).thenReturn("slaveResource");
        when(slaveResource.getResourceGroup()).thenReturn(resourceGroup);
        when(slaveResource.getRelease()).thenReturn(release);
        return slaveResource;
    }


}