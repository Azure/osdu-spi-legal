package org.opengroup.osdu.legal.tags.validation;

import jakarta.validation.Constraint;
import java.lang.annotation.*;
import jakarta.validation.Payload;

@Target({ ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = { OtherRelevantDataCountriesValidator.class })
@Documented
public @interface ValidOtherRelevantDataCountries {

    String message() default "Invalid ORDC. Each value should match one of the ISO alpha 3 codes and should not have embargo restrictions. Found: ${validatedValue}.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
