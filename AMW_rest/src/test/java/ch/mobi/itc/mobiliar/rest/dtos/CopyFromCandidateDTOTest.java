package ch.mobi.itc.mobiliar.rest.dtos;

import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceGroup;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceGroupEntity;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CopyFromCandidateDTOTest {

    @Test
    void shouldReturnEmptyListWhenNoGroups() {
        ResourceEntity resource = mock(ResourceEntity.class);
        List<ResourceGroup> groups = new ArrayList<>();

        List<CopyFromCandidateDTO> result = CopyFromCandidateDTO.from(resource, groups);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldSkipGroupsWithEmptyReleaseMap() {
        ResourceEntity resource = mock(ResourceEntity.class);
        ResourceGroupEntity resourceGroup = mock(ResourceGroupEntity.class);
        when(resource.getResourceGroup()).thenReturn(resourceGroup);
        when(resourceGroup.getId()).thenReturn(1);

        ResourceGroup group1 = mock(ResourceGroup.class);
        when(group1.getReleaseToResourceMap()).thenReturn(new LinkedHashMap<>());

        List<ResourceGroup> groups = new ArrayList<>();
        groups.add(group1);

        List<CopyFromCandidateDTO> result = CopyFromCandidateDTO.from(resource, groups);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldIncludeGroupWithReleases() {
        ResourceEntity resource = mock(ResourceEntity.class);
        ResourceGroupEntity resourceGroup = mock(ResourceGroupEntity.class);
        when(resource.getResourceGroup()).thenReturn(resourceGroup);
        when(resourceGroup.getId()).thenReturn(1);

        ResourceGroup group1 = mock(ResourceGroup.class);
        when(group1.getId()).thenReturn(2);
        when(group1.getName()).thenReturn("TestGroup");
        LinkedHashMap<String, Integer> releaseMap = new LinkedHashMap<>();
        releaseMap.put("v1.0", 100);
        releaseMap.put("v2.0", 200);
        when(group1.getReleaseToResourceMap()).thenReturn(releaseMap);

        List<ResourceGroup> groups = new ArrayList<>();
        groups.add(group1);

        List<CopyFromCandidateDTO> result = CopyFromCandidateDTO.from(resource, groups);

        assertNotNull(result);
        assertEquals(1, result.size());
        
        CopyFromCandidateDTO candidate = result.get(0);
        assertEquals(2, candidate.getGroupId());
        assertEquals("TestGroup", candidate.getGroupName());
        assertEquals(2, candidate.getReleases().size());
        
        assertEquals("v1.0", candidate.getReleases().get(0).getReleaseName());
        assertEquals(100, candidate.getReleases().get(0).getResourceId());
        assertEquals("v2.0", candidate.getReleases().get(1).getReleaseName());
        assertEquals(200, candidate.getReleases().get(1).getResourceId());
    }

    @Test
    void shouldExcludeResourceOwnGroupWhenNoReleases() {
        ResourceEntity resource = mock(ResourceEntity.class);
        ResourceGroupEntity resourceGroup = mock(ResourceGroupEntity.class);
        when(resource.getResourceGroup()).thenReturn(resourceGroup);
        when(resourceGroup.getId()).thenReturn(1);

        ResourceGroup ownGroup = mock(ResourceGroup.class);
        when(ownGroup.getId()).thenReturn(1);
        when(ownGroup.getName()).thenReturn("OwnGroup");
        LinkedHashMap<String, Integer> releaseMap = new LinkedHashMap<>();
        when(ownGroup.getReleaseToResourceMap()).thenReturn(releaseMap);

        List<ResourceGroup> groups = new ArrayList<>();
        groups.add(ownGroup);

        List<CopyFromCandidateDTO> result = CopyFromCandidateDTO.from(resource, groups);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldIncludeResourceOwnGroupWhenHasReleases() {
        ResourceEntity resource = mock(ResourceEntity.class);
        ResourceGroupEntity resourceGroup = mock(ResourceGroupEntity.class);
        when(resource.getResourceGroup()).thenReturn(resourceGroup);
        when(resourceGroup.getId()).thenReturn(1);

        ResourceGroup ownGroup = mock(ResourceGroup.class);
        when(ownGroup.getId()).thenReturn(1);
        when(ownGroup.getName()).thenReturn("OwnGroup");
        LinkedHashMap<String, Integer> releaseMap = new LinkedHashMap<>();
        releaseMap.put("v1.0", 100);
        when(ownGroup.getReleaseToResourceMap()).thenReturn(releaseMap);

        List<ResourceGroup> groups = new ArrayList<>();
        groups.add(ownGroup);

        List<CopyFromCandidateDTO> result = CopyFromCandidateDTO.from(resource, groups);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getGroupId());
        assertEquals("OwnGroup", result.get(0).getGroupName());
    }

    @Test
    void shouldHandleMultipleGroups() {
        ResourceEntity resource = mock(ResourceEntity.class);
        ResourceGroupEntity resourceGroup = mock(ResourceGroupEntity.class);
        when(resource.getResourceGroup()).thenReturn(resourceGroup);
        when(resourceGroup.getId()).thenReturn(1);

        ResourceGroup group1 = mock(ResourceGroup.class);
        when(group1.getId()).thenReturn(2);
        when(group1.getName()).thenReturn("Group1");
        LinkedHashMap<String, Integer> releaseMap1 = new LinkedHashMap<>();
        releaseMap1.put("v1.0", 100);
        when(group1.getReleaseToResourceMap()).thenReturn(releaseMap1);

        ResourceGroup group2 = mock(ResourceGroup.class);
        when(group2.getId()).thenReturn(3);
        when(group2.getName()).thenReturn("Group2");
        LinkedHashMap<String, Integer> releaseMap2 = new LinkedHashMap<>();
        releaseMap2.put("v2.0", 200);
        releaseMap2.put("v3.0", 300);
        when(group2.getReleaseToResourceMap()).thenReturn(releaseMap2);

        ResourceGroup emptyGroup = mock(ResourceGroup.class);
        when(emptyGroup.getReleaseToResourceMap()).thenReturn(new LinkedHashMap<>());

        List<ResourceGroup> groups = new ArrayList<>();
        groups.add(group1);
        groups.add(emptyGroup);
        groups.add(group2);

        List<CopyFromCandidateDTO> result = CopyFromCandidateDTO.from(resource, groups);

        assertNotNull(result);
        assertEquals(2, result.size());
        
        assertEquals("Group1", result.get(0).getGroupName());
        assertEquals(1, result.get(0).getReleases().size());
        
        assertEquals("Group2", result.get(1).getGroupName());
        assertEquals(2, result.get(1).getReleases().size());
    }

    @Test
    void shouldPreserveReleaseMapOrder() {
        ResourceEntity resource = mock(ResourceEntity.class);
        ResourceGroupEntity resourceGroup = mock(ResourceGroupEntity.class);
        when(resource.getResourceGroup()).thenReturn(resourceGroup);
        when(resourceGroup.getId()).thenReturn(1);

        ResourceGroup group = mock(ResourceGroup.class);
        when(group.getId()).thenReturn(2);
        when(group.getName()).thenReturn("TestGroup");
        LinkedHashMap<String, Integer> releaseMap = new LinkedHashMap<>();
        releaseMap.put("v1.0", 100);
        releaseMap.put("v2.0", 200);
        releaseMap.put("v3.0", 300);
        when(group.getReleaseToResourceMap()).thenReturn(releaseMap);

        List<ResourceGroup> groups = new ArrayList<>();
        groups.add(group);

        List<CopyFromCandidateDTO> result = CopyFromCandidateDTO.from(resource, groups);

        assertNotNull(result);
        assertEquals(1, result.size());
        
        List<CopyFromReleaseDTO> releases = result.get(0).getReleases();
        assertEquals(3, releases.size());
        assertEquals("v1.0", releases.get(0).getReleaseName());
        assertEquals("v2.0", releases.get(1).getReleaseName());
        assertEquals("v3.0", releases.get(2).getReleaseName());
    }
}