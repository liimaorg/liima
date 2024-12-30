package ch.puzzle.itc.mobiliar.business.function.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class ValidFunctionContentValidator implements ConstraintValidator<ValidFunctionContent, String> {

    @Override
    public boolean isValid(String function, ConstraintValidatorContext constraintValidatorContext) {
        return function != null && !function.isEmpty();
    }
}
