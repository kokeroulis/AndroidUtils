package gr.kokeroulis.jsonapiparser;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Map;

public final class TypeUtils {

    public static boolean isPublic(Field f) {
        return Modifier.isPublic(f.getModifiers());
    }

    public static Map<String, Object> castObjectToMap(final Object target) {
        if (!(target instanceof Map)) {
            throw new IllegalArgumentException(target.toString() + " Cannot be converted to map");
        }

        return (Map<String, Object>) target;
    }

    public static <A extends Annotation> boolean
    isAnnotationPresent(final Field field, Class<A> annotation) {
        Annotation isPresent = field.getAnnotation(annotation);
        return isPresent != null;
    }

    public static <A extends Annotation> A
    getAnnotation(final Field field, Class<A> annotation) {
        return field.getAnnotation(annotation);
    }
}
