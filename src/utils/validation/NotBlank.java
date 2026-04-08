package utils.validation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface NotBlank {
    String message() default "must not be blank";
}
