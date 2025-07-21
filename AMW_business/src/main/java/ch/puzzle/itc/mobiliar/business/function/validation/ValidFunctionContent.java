package ch.puzzle.itc.mobiliar.business.function.validation;

import javax.validation.Constraint;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
@Target({ FIELD })
@Retention(RUNTIME)
@Constraint(validatedBy = ValidFunctionContentValidator.class)
@Documented
public @interface ValidFunctionContent {

    String message() default "The function contains empty space";

    Class<?>[] groups() default {};

    Class<?>[] payload() default {};
}
