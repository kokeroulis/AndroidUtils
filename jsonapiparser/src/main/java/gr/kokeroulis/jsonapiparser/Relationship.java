package gr.kokeroulis.jsonapiparser;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(value = ElementType.FIELD)
public @interface Relationship {
    String type();

    /* Sometimes we have two relationships which has
     * the same type inside the included.
     * so we use this attribute in order to separate them
     */
    String fieldNameFallback() default "";
}
