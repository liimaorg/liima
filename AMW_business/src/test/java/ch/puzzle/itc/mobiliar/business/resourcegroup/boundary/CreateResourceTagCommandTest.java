package ch.puzzle.itc.mobiliar.business.resourcegroup.boundary;

import org.junit.jupiter.api.Test;

import javax.validation.ConstraintViolationException;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CreateResourceTagCommandTest {

    @Test
    void throws_exception_when_resource_id_is_null() {
        ConstraintViolationException e = assertThrows(ConstraintViolationException.class, () -> new CreateResourceTagCommand(null, "tag", new Date()));
        assertEquals("resourceId: resourceId may not be null", e.getMessage());
    }

    @Test
    void throws_exception_when_label_is_null() {
        ConstraintViolationException e = assertThrows(ConstraintViolationException.class, () -> new CreateResourceTagCommand(1, null, new Date()));
        assertEquals("label: label may not be null", e.getMessage());
    }

    @Test
    void throws_exception_when_tag_date_is_null() {
        ConstraintViolationException e = assertThrows(ConstraintViolationException.class, () -> new CreateResourceTagCommand(1, "tag", null));
        assertEquals("tagDate: tagDate may not be null", e.getMessage());
    }

    @Test
    void does_not_throw_exception() {
        new CreateResourceTagCommand(1, "tag", new Date());
    }
}
