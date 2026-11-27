package ch.puzzle.itc.mobiliar.business.resourcerelation.boundary;

import ch.puzzle.itc.mobiliar.business.property.entity.ResourceEditRelation;
import ch.puzzle.itc.mobiliar.business.resourcegroup.boundary.GetResourceUseCase;
import ch.puzzle.itc.mobiliar.business.resourcegroup.control.ResourceEditService;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceTypeEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.control.ResourceRelationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetResourceRelationsServiceTest {

    @InjectMocks
    GetResourceRelationsService service;

    @Mock
    GetResourceUseCase getResourceUseCase;

    @Mock
    ResourceEditService resourceEditService;

    @Mock
    ResourceRelationService resourceRelationService;

    @Test
    void resolvingOneRelationLeavesOnlyTheOtherRelationOfTheSameTypeUnresolved() throws Exception {
        ResourceEntity resource = mock(ResourceEntity.class);
        ResourceTypeEntity type = mock(ResourceTypeEntity.class);
        when(resource.getResourceType()).thenReturn(type);
        when(type.getName()).thenReturn("Custom");
        when(getResourceUseCase.getResourceById(any())).thenReturn(resource);

        ResourceEditRelation primary = relation(null, 20, "primary");
        ResourceEditRelation secondary = relation(null, 21, "secondary");
        Map<ResourceEditRelation.Mode, List<ResourceEditRelation>> relations =
                new EnumMap<>(ResourceEditRelation.Mode.class);
        relations.put(ResourceEditRelation.Mode.TYPE, Arrays.asList(primary, secondary));
        relations.put(ResourceEditRelation.Mode.CONSUMED, Collections.emptyList());
        when(resourceEditService.loadResourceRelationsForEdit(1)).thenReturn(relations);

        assertEquals(Arrays.asList(primary, secondary), service.getGroupedRelations(1).getUnresolved());

        ResourceEditRelation resolved = relation(30, 21, "secondary");
        relations.put(ResourceEditRelation.Mode.CONSUMED, Collections.singletonList(resolved));
        when(resourceRelationService.getBestMatchingRelationRelease(anyList(), eq(resource))).thenReturn(resolved);

        GroupedRelations result = service.getGroupedRelations(1);
        assertEquals(Collections.singletonList(primary), result.getUnresolved());
        assertEquals(resolved, result.getConsumed().get(0).getBest());
    }

    private ResourceEditRelation relation(Integer id, int typeId, String identifier) {
        return new ResourceEditRelation(id, 2, identifier, "Service", 3, 4, "current",
                5, "Webservice", "Custom", typeId, identifier,
                id == null ? "TYPE" : "CONSUMED", null);
    }
}
