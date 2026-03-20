package ch.mobi.itc.mobiliar.rest.resources.resourceTags;

import ch.mobi.itc.mobiliar.rest.dtos.ResourceTagDTO;
import org.junit.jupiter.api.Test;

import javax.validation.ConstraintViolationException;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CreateResourceTagCommandTest {

    @Test
    void throws_exception_when_resource_id_is_null() {
        assertThrows(ConstraintViolationException.class, () -> new CreateResourceTagCommand(null, new ResourceTagDTO()));
    }

    @Test
    void throws_exception_when_resource_tag_is_null() {
        assertThrows(ConstraintViolationException.class, () -> new CreateResourceTagCommand(1, null));
    }

    @Test
    void throws_exception_when_resource_tag_label_is_null() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> new CreateResourceTagCommand(1, new ResourceTagDTO(1, null, new Date())));
        assertEquals("Label must not be null or empty", e.getMessage());
    }

    @Test
    void throws_exception_when_resource_tag_date_is_null() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> new CreateResourceTagCommand(1, new ResourceTagDTO(1, "tag1", null)));
        assertEquals("Tag date must not be null", e.getMessage());
    }

    @Test
    void does_not_throw_exception() {
        new CreateResourceTagCommand(1, new ResourceTagDTO(1, "tag", new Date()));
    }
}