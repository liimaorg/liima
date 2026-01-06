package ch.puzzle.itc.mobiliar.business.property.boundary;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import ch.puzzle.itc.mobiliar.common.exception.ValidationException;

public class TagCommandTest {

    @Test
    public void shouldNotValidateIfTagNameIsNull() throws ValidationException {
        assertThrows(ValidationException.class, () -> {
            new TagCommand(null);
        });
    }

    @Test
    public void shouldNotValidateIfTagNameIsBlank() throws ValidationException {
        assertThrows(ValidationException.class, () -> {
            new TagCommand("");
        });
    }

    @Test
    public void shouldNotValidateIfTagNameIsOnlyWhiteSpace() throws ValidationException {
        assertThrows(ValidationException.class, () -> {
            new TagCommand("    ");
        });
    }

    @Test
    public void shouldCreateTagCommand() throws ValidationException {
        TagCommand tagCommand = new TagCommand("tagName");
        assertEquals("tagName", tagCommand.getName());
    }
}