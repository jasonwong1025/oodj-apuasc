package utils.validation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Size {
    int min() default 0;
    int max() default Integer.MAX_VALUE;
    String message() default "size must be between {min} and {max}";
}
