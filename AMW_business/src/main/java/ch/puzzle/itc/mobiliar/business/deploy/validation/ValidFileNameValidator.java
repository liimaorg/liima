package ch.puzzle.itc.mobiliar.business.deploy.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.io.File;

public class ValidFileNameValidator implements ConstraintValidator<ValidFileName, String> {


    @Override
    public boolean isValid(String fileName, ConstraintValidatorContext constraintValidatorContext) {
        if (fileName == null) return false;
        return !fileName.contains(File.separator);
    }
}
