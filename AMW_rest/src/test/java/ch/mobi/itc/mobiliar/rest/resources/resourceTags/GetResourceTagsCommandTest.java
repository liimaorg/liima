package ch.mobi.itc.mobiliar.rest.resources.resourceTags;

import org.junit.jupiter.api.Test;

import javax.validation.ConstraintViolationException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GetResourceTagsCommandTest {


    @Test
    void shouldThrowExceptionWhenResourceIdIsMissing() {
        ConstraintViolationException ex = assertThrows(ConstraintViolationException.class, () -> new GetResourceTagsCommand(null));
        assertEquals("resourceId: must not be null", ex.getMessage());
    }

    @Test
    void shouldCreateGetResourceTagsCommand() {
        GetResourceTagsCommand command = new GetResourceTagsCommand(1);
        assertEquals(1, command.getResourceId());
    }
}