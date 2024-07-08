package ch.puzzle.itc.mobiliar.business.deploy.validation;


import javax.validation.Constraint;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({ FIELD })
@Retention(RUNTIME)
@Constraint(validatedBy = ValidFileNameValidator.class)
@Documented
public @interface ValidFileName {

    String message() default "The log file contains a file separator";

    Class<?>[] groups() default {};

    Class<?>[] payload() default {};
}
