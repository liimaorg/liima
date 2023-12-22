package ch.puzzle.itc.mobiliar.business.utils;


import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.Set;
import javax.validation.Validator;

import static javax.validation.Validation.buildDefaultValidatorFactory;

public class Validation {

    // Your IDE may complain that the ValidatorFactory needs to be closed, but if we do that here,
    // we break the contract of ValidatorFactory#close.
    private final static Validator validator =
            buildDefaultValidatorFactory().getValidator();

    /**
     * Evaluates all Bean Validation annotations on the subject.
     */
    public static <T> void validate(T subject) {
        Set<ConstraintViolation<T>> violations = validator.validate(subject);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
    }
}

