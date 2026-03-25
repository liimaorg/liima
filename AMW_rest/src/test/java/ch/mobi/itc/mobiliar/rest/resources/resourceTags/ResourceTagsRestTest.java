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

import javax.validation.ConstraintViolationException;
import javax.ws.rs.core.Response;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class ResourceTagsRestTest {

    @InjectMocks
    ResourceTagsRest resourceTagsRest;

    @Mock
    TagConfigurationService tagConfigurationService;

    @Mock
    ResourceLocator resourceLocator;


    @Test
    void createTag_missing_resourceId() {
        ConstraintViolationException e = assertThrows(ConstraintViolationException.class, () -> resourceTagsRest.createTag(null, new ResourceTagDTO()));
        assertEquals("resourceId: may not be null", e.getMessage());
    }

    @Test
    void createTag_missing_resourceTagDTO() {
        ConstraintViolationException e = assertThrows(ConstraintViolationException.class, () -> resourceTagsRest.createTag(1, null));
        assertEquals("resourceTag: may not be null", e.getMessage());
    }

    @Test
    void createTag_resource_not_found() {
        ResourceTagDTO tag = new ResourceTagDTO(1, "tag", new Date());
        doReturn(null).when(resourceLocator).getResourceById(1);

        NotFoundException e = assertThrows(NotFoundException.class, () -> resourceTagsRest.createTag(1, tag));
        assertEquals("Resource not found for resource id 1", e.getMessage());
    }

    @Test
    void createTag_label_already_exists() {
        ResourceTagDTO tag = new ResourceTagDTO(1, "tag", new Date());
        ResourceEntity resourceEntity = new ResourceEntity();
        ResourceTagEntity existingTag = new ResourceTagEntity();
        existingTag.setLabel("tag"); // Same label as the DTO
        existingTag.setResource(resourceEntity);

        doReturn(resourceEntity).when(resourceLocator).getResourceById(1);
        doReturn(List.of(existingTag)).when(tagConfigurationService).loadTagLabelsForResource(resourceEntity);

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> resourceTagsRest.createTag(1, tag));
        assertEquals("Tag 'tag' already exists for resource id 1", e.getMessage());
    }

    @Test
    void createTag_label_already_exists_case_insensitive() {
        ResourceTagDTO tag = new ResourceTagDTO(1, "tag", new Date());
        ResourceEntity resourceEntity = new ResourceEntity();
        ResourceTagEntity existingTag = new ResourceTagEntity();
        existingTag.setLabel("taG"); // Same label as the DTO
        existingTag.setResource(resourceEntity);

        doReturn(resourceEntity).when(resourceLocator).getResourceById(1);
        doReturn(List.of(existingTag)).when(tagConfigurationService).loadTagLabelsForResource(resourceEntity);

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> resourceTagsRest.createTag(1, tag));
        assertEquals("Tag 'tag' already exists for resource id 1", e.getMessage());
    }

    @Test
    void createTag() throws NotFoundException {
        ResourceTagDTO tag = new ResourceTagDTO(1, "tag", new Date());
        ResourceEntity resourceEntity = new ResourceEntity();
        doReturn(resourceEntity).when(resourceLocator).getResourceById(1);
        doReturn(new ResourceTagEntity()).when(tagConfigurationService).tagConfiguration(1, "tag", tag.getTagDate());

        Response response = resourceTagsRest.createTag(1, tag);
        assertEquals(200, response.getStatus());
        ResourceTagDTO entity = (ResourceTagDTO) response.getEntity();
        assertNotNull(entity);
    }
}