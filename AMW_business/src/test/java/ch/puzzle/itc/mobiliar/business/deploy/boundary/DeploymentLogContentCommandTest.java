package ch.puzzle.itc.mobiliar.business.deploy.boundary;

import org.junit.jupiter.api.Test;

import javax.validation.ConstraintViolationException;

import static org.junit.jupiter.api.Assertions.*;

public class DeploymentLogContentCommandTest {

    @Test
    public void throws_exception_when_name_is_null() {
        assertThrows(ConstraintViolationException.class, () -> new DeploymentLogContentCommand(null, "log-file"));
    }

    @Test
    public void throws_exception_when_filename_is_null() {
        assertThrows(ConstraintViolationException.class, () -> new DeploymentLogContentCommand(1234, null));
    }

    @Test
    public void throws_exception_when_filename_is_not_valid() {
        assertThrows(ConstraintViolationException.class,
                () -> new DeploymentLogContentCommand(1234, "pathtraversal/../notallowed.log"));
    }
}