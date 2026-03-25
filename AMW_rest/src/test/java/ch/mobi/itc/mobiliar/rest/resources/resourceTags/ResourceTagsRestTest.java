package ch.mobi.itc.mobiliar.rest.resources.resourceTags;

import ch.mobi.itc.mobiliar.rest.dtos.ResourceTagDTO;
import ch.puzzle.itc.mobiliar.business.configurationtag.boundary.CreateTagUseCase;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class ResourceTagsRestTest {

    @InjectMocks
    ResourceTagsRest resourceTagsRest;

    @Mock
    ResourceLocator resourceLocator;

    @Mock
    CreateTagUseCase createTagUseCase;



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
    void createTag() throws NotFoundException {
        ResourceTagDTO tag = new ResourceTagDTO(1, "tag", new Date());
        ResourceEntity resourceEntity = new ResourceEntity();
        doReturn(new ResourceTagEntity()).when(createTagUseCase).createTag(any());

        Response response = resourceTagsRest.createTag(1, tag);
        assertEquals(200, response.getStatus());
        ResourceTagDTO entity = (ResourceTagDTO) response.getEntity();
        assertNotNull(entity);
    }
}