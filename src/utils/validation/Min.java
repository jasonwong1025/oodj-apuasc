package utils.validation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Min {
    long value();
    String message() default "must be at least {value}";
}
