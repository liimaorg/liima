package ch.mobi.itc.mobiliar.rest.resources.resourceTags;

import org.junit.jupiter.api.Test;

import javax.validation.ConstraintViolationException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GetResourceTagsCommandTest {


    @Test
    void shouldThrowExceptionWhenResourceIdIsMissing() {
        ConstraintViolationException ex = assertThrows(ConstraintViolationException.class, () -> new ch.puzzle.itc.mobiliar.business.resourcegroup.boundary.GetResourceTagsCommand(null));
        assertEquals("resourceId: must not be null", ex.getMessage());
    }

    @Test
    void shouldCreateGetResourceTagsCommand() {
        ch.puzzle.itc.mobiliar.business.resourcegroup.boundary.GetResourceTagsCommand command = new ch.puzzle.itc.mobiliar.business.resourcegroup.boundary.GetResourceTagsCommand(1);
        assertEquals(1, command.getResourceId());
    }
}