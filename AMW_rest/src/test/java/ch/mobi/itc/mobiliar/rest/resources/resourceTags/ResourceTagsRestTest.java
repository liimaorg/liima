package ch.mobi.itc.mobiliar.rest.resources.resourceTags;

import ch.mobi.itc.mobiliar.rest.dtos.ResourceTagDTO;
import ch.puzzle.itc.mobiliar.business.configurationtag.boundary.CreateResourceTagUseCase;
import ch.puzzle.itc.mobiliar.business.configurationtag.boundary.ListResourceTagsUseCase;
import ch.puzzle.itc.mobiliar.business.configurationtag.entity.ResourceTagEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.common.exception.NotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.validation.ConstraintViolationException;
import javax.ws.rs.core.Response;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class ResourceTagsRestTest {

    @InjectMocks
    ResourceTagsRest resourceTagsRest;

    @Mock
    CreateResourceTagUseCase createResourceTagUseCase;

    @Mock
    ListResourceTagsUseCase listResourceTagsUseCase;


    @Test
    void getResourceTags_missing_resourceId() {
        ConstraintViolationException e = assertThrows(ConstraintViolationException.class, () -> resourceTagsRest.getResourceTags(null));
        assertEquals("resourceId: must not be null", e.getMessage());
    }

    @Test
    void getResourceTags() throws NotFoundException {
        Date tagDate = new Date();
        doReturn(List.of(new ResourceTagEntity(1, new ResourceEntity(), "tag-label", tagDate, 2))).when(listResourceTagsUseCase).getTags(1);

        Response response = resourceTagsRest.getResourceTags(1);

        assertEquals(200, response.getStatus());
        List<ResourceTagDTO> tags = (List<ResourceTagDTO>) response.getEntity();
        assertEquals(1, tags.size());
        assertEquals(1, tags.get(0).getId());
        assertEquals("tag-label", tags.get(0).getLabel());
        assertEquals(tagDate, tags.get(0).getTagDate());
    }

    @Test
    void createResourceTag_missing_Parameters() {
        ConstraintViolationException e = assertThrows(ConstraintViolationException.class, () -> resourceTagsRest.createResourceTag(null, new CreateResourceTagPayload(null, null)));

        assertEquals(3, e.getConstraintViolations().size());
        assertTrue(e.getMessage().contains("resourceId: resourceId may not be null"));
        assertTrue(e.getMessage().contains("label: label may not be null"));
        assertTrue(e.getMessage().contains("tagDate: tagDate may not be null"));
    }

    @Test
    void createResourceTag() throws NotFoundException {
        CreateResourceTagPayload tag = new CreateResourceTagPayload("tag", new Date());
        doReturn(new ResourceTagEntity()).when(createResourceTagUseCase).createTag(any());

        Response response = resourceTagsRest.createResourceTag(1, tag);
        assertEquals(200, response.getStatus());
        ResourceTagDTO entity = (ResourceTagDTO) response.getEntity();
        assertNotNull(entity);
    }
}