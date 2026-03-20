package ch.mobi.itc.mobiliar.rest.resources.resourceTags;

import ch.mobi.itc.mobiliar.rest.dtos.ResourceTagDTO;
import ch.puzzle.itc.mobiliar.business.configurationtag.control.TagConfigurationService;
import ch.puzzle.itc.mobiliar.business.configurationtag.entity.ResourceTagEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.boundary.ResourceLocator;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.common.exception.NotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.ws.rs.core.Response;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResourceTagsRestTest {

    @InjectMocks
    ResourceTagsRest resourceTagsRest;

    @Mock
    TagConfigurationService tagConfigurationService;

    @Mock
    ResourceLocator resourceLocator;


    @Test
    void getTagsForResource_throwsNotFoundException() {
        when(resourceLocator.getResourceById(anyInt())).thenReturn(null);

        NotFoundException notFoundException = assertThrows(NotFoundException.class, () -> resourceTagsRest.getTagsForResource(1));

        assertEquals("Resource not found", notFoundException.getMessage());
    }


    @Test
    void getTagsForResource_returnsTags() throws NotFoundException {
        ResourceEntity resourceEntity = new ResourceEntity();
        when(resourceLocator.getResourceById(1)).thenReturn(resourceEntity);

        List<ResourceTagEntity> tags = List.of(new ResourceTagEntity(1, resourceEntity, "tag1", new Date(), 1L), new ResourceTagEntity(2, resourceEntity, "tag1", new Date(), 2L), new ResourceTagEntity(3, resourceEntity, "tag1", new Date(), 3L));

        when(tagConfigurationService.loadTagLabelsForResource(resourceEntity)).thenReturn(tags);

        Response result = resourceTagsRest.getTagsForResource(1);

        assertEquals(Response.Status.OK.getStatusCode(), result.getStatus());
        List<ResourceTagDTO> tagDtos = (List<ResourceTagDTO>) result.getEntity();
        assertEquals(3, tagDtos.size());
    }

    @Test
    void createTag_missing_resourceId() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> resourceTagsRest.createTag(null, new ResourceTagDTO()));
        assertEquals("Resource ID is required", e.getMessage());
    }

    @Test
    void createTag_missing_resourceTagDTO() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> resourceTagsRest.createTag(1, null));
        assertEquals("Tag data is required", e.getMessage());
    }
    @Test
    void createTag_missing_tag_label() {
        ResourceTagDTO resourceTagDTO = new ResourceTagDTO(1, null, null);
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> resourceTagsRest.createTag(1, null));
        assertEquals("Tag data is required", e.getMessage());
    }

}