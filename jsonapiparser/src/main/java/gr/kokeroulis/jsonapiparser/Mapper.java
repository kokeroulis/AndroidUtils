package gr.kokeroulis.jsonapiparser;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by kokeroulis on 26/03/16.
 */
public class Mapper {
    private static final String ATTRIBUTES = "attributes";


    //private final JsonApiJson json;
    private final Map<String, Object> elementObject;
    private final Map<String, Object> jsonElement;
    private Map.Entry<String, Object> entrySet;

    public Mapper(final String json, final Class<?> elementClass, final Moshi moshi) throws IOException {
        //JsonAdapter<JsonApiJson> adapter = moshi.adapter(JsonApiJson.class);
        //this.json = adapter.fromJson(json);
        Type typeOfMap = Types.newParameterizedType(Map.class, String.class, Object.class);
        JsonAdapter<Map<String, Object>> adapter = moshi.adapter(typeOfMap);
        jsonElement = adapter.fromJson(json);

        elementObject = new LinkedHashMap<>();
        Field[] fields = elementClass.getDeclaredFields();
        for (Field field : fields) {
            if (!TypeUtils.isPublic(field)) {
                continue;
            }

            //TODO divergity between attributes and relationships
            if (jsonElement.containsKey(ATTRIBUTES)) {
                final Map<String, Object> attributes =
                    TypeUtils.castObjectToMap(jsonElement.get(ATTRIBUTES));
                getAttributes(field, attributes);
            }

        }
    }

    private void getAttributes(final Field field, final Map<String, Object> attributes) {
        for (Map.Entry<String, Object> entrySet : attributes.entrySet()) {
            if (entrySet.getKey().equals(field.getName())) {
                elementObject.put(entrySet.getKey(), entrySet.getValue());
            }
        }
    }

    // needs annotiation
    private void getRelationships() {}
}
