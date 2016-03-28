package gr.kokeroulis.jsonapiparser;

import com.squareup.moshi.Types;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Map;

/**
 * Created by kokeroulis on 26/03/16.
 */
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
}
