package utils.validation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Pattern {
    String regexp();
    String message() default "must match pattern";
}
