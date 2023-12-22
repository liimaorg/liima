package ch.puzzle.itc.mobiliar.business.deploy.boundary;

import org.junit.Test;

import javax.validation.ConstraintViolationException;

import static org.junit.Assert.*;

public class DeploymentLogContentCommandTest {

    @Test(expected = ConstraintViolationException.class)
    public void throws_exception_when_name_is_null() {
        // given

        // when
        new DeploymentLogContentCommand(null, "log-file");

        // then
        fail("should have thrown exception");
    }

    @Test(expected = ConstraintViolationException.class)
    public void throws_exception_when_filename_is_null() {
        // given

        // when
        new DeploymentLogContentCommand(1234, null);

        // then
        fail("should have thrown exception");
    }

    @Test(expected = ConstraintViolationException.class)
    public void throws_exception_when_filename_is_not_valid() {
        // given

        // when
        new DeploymentLogContentCommand(1234, "pathtraversal/../notallowed.log");

        // then
    }
}