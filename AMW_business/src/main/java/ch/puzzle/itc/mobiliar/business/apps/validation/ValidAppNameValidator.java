package ch.puzzle.itc.mobiliar.business.apps.validation;

import ch.puzzle.itc.mobiliar.common.util.NameChecker;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class ValidAppNameValidator implements ConstraintValidator<ValidAppName, String> {

    @Override
    public boolean isValid(String appName, ConstraintValidatorContext constraintValidatorContext) {
        if (appName == null) return false;
        return NameChecker.isNameValid(appName);
    }
}
