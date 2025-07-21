package ch.puzzle.itc.mobiliar.business.function.validation;

import ch.puzzle.itc.mobiliar.common.util.NameChecker;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class ValidFunctionNameValidator implements ConstraintValidator<ValidFunctionName, String> {

    @Override
    public boolean isValid(String functionName, ConstraintValidatorContext constraintValidatorContext) {
        if (functionName == null) return false;
        return NameChecker.isNameValid(functionName);
    }
}
