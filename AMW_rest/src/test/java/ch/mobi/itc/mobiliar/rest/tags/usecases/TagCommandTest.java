package ch.mobi.itc.mobiliar.rest.tags.usecases;

import ch.mobi.itc.mobiliar.rest.dtos.TagDTO;
import ch.puzzle.itc.mobiliar.common.exception.ValidationException;
import org.junit.Test;

import static org.junit.Assert.*;

public class TagCommandTest {


    @Test(expected = ValidationException.class)
    public void shouldNotValidateIfTagDtoIsNull() throws ValidationException {
        // given

        // when
        new TagCommand(null);

        // then
        fail("should have thrown exception");
    }

    @Test(expected = ValidationException.class)
    public void shouldNotValidateIfTagNameIsNull() throws ValidationException {
        // given

        // when
        new TagCommand(new TagDTO(null));

        // then
        fail("should have thrown exception");
    }

    @Test(expected = ValidationException.class)
    public void shouldNotValidateIfTagNameIsBlank() throws ValidationException {
        // given

        // when
        new TagCommand(new TagDTO(""));

        // then
        fail("should have thrown exception");
    }

    @Test(expected = ValidationException.class)
    public void shouldNotValidateIfTagNameIsOnlyWhiteSpace() throws ValidationException {
        // given

        // when
        new TagCommand(new TagDTO("  "));

        // then
    }

    @Test
    public void shouldCreateTagCommand() throws ValidationException {
        // given

        // when
        TagCommand tagCommand = new TagCommand(new TagDTO("tagName"));

        // then
        assertEquals("tagName", tagCommand.getName());
    }
}