package ch.puzzle.itc.mobiliar.business.apps.validation;


import javax.validation.Constraint;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({ FIELD })
@Retention(RUNTIME)
@Constraint(validatedBy = ValidAppNameValidator.class)
@Documented
public @interface ValidAppName {

    String message() default "The name contains empty space or dots";

    Class<?>[] groups() default {};

    Class<?>[] payload() default {};
}
