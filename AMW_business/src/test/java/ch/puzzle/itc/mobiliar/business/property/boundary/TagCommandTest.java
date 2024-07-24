package ch.puzzle.itc.mobiliar.business.property.boundary;

import ch.puzzle.itc.mobiliar.common.exception.ValidationException;
import org.junit.Test;

import static org.junit.Assert.*;

public class TagCommandTest {


    @Test(expected = ValidationException.class)
    public void shouldNotValidateIfTagNameIsNull() throws ValidationException {
        // given

        // when
        new TagCommand(null);

        // then
        fail("should have thrown exception");
    }

    @Test(expected = ValidationException.class)
    public void shouldNotValidateIfTagNameIsBlank() throws ValidationException {
        // given

        // when
        new TagCommand("");

        // then
        fail("should have thrown exception");
    }

    @Test(expected = ValidationException.class)
    public void shouldNotValidateIfTagNameIsOnlyWhiteSpace() throws ValidationException {
        // given

        // when
        new TagCommand("    ");

        // then
    }

    @Test
    public void shouldCreateTagCommand() throws ValidationException {
        // given

        // when
        TagCommand tagCommand = new TagCommand("tagName");

        // then
        assertEquals("tagName", tagCommand.getName());
    }
}