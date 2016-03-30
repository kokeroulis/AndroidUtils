package gr.kokeroulis.jsonapiparser;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.Map;

import gr.kokeroulis.jsonapiparser.models.MapperObject;

/**
 * Created by kokeroulis on 26/03/16.
 */
public class Mapper {
    private static final String ATTRIBUTES = "attributes";
    private static final String DATA = "data";
    private static final String RELATIONSHIPS = "relationships";
    private static final String TYPE = "type";
    private static final String INCLUDED = "included";
    private static final String ID = "id";


    //private final JsonApiJson json;
    private final Map<String, Object> elementObject;
    private final MapperObject jsonElement;
    private Map.Entry<String, Object> entrySet;

    public Mapper(final String json, final Class<?> elementClass, final Moshi moshi) throws IOException {
        //JsonAdapter<JsonApiJson> adapter = moshi.adapter(JsonApiJson.class);
        //this.json = adapter.fromJson(json);
        Type typeOfMap = Types.newParameterizedType(Map.class, String.class, Object.class);
        JsonAdapter<MapperObject> adapter = moshi.adapter(MapperObject.class);
        jsonElement = adapter.fromJson(json);

        elementObject = new LinkedHashMap<>();
        Field[] fields = elementClass.getDeclaredFields();
        for (Field field : fields) {
            if (!TypeUtils.isPublic(field)) {
                continue;
            } else if (TypeUtils.isAnnotationPresent(field, Relationship.class)) {
                getRelationships(field, getRelationshipsRawMap(jsonElement));
            } else if (hasAttributes(jsonElement)) {
                final Map<String, Object> attributes = getAttributesRawMap(jsonElement);
                getAttributes(field, attributes);
            }
            String foo = "asdadsads";
        }

        String foo = "test foo";
    }

    private void getAttributes(final Field field, final Map<String, Object> attributes) {
        for (Map.Entry<String, Object> entrySet : attributes.entrySet()) {
            if (entrySet.getKey().equals(field.getName())) {
                elementObject.put(entrySet.getKey(), entrySet.getValue());
            }
        }
    }

    // needs annotiation
    private void getRelationships(final Field field, final Map<String, Object> relationships) {
        final Relationship relationship = TypeUtils.getAnnotation(field, Relationship.class);
        for (Map.Entry<String, Object> entrySet : relationships.entrySet()) {
            if (entrySet.getKey().equals(field.getName())) {
                Map<String, Object> relMap = TypeUtils.castObjectToMap(entrySet.getValue());
                Map<String, Object> relData = getRelationshipData(relMap);
                if (relData.get(TYPE).equals(relationship.type())) {
                    Map<String, Object> helper = getRelationshipData(relMap);
                    final String id = helper.get(ID).toString();
                    getIncluded(field, jsonElement, relationship.type(), id, helper);
                    elementObject.put(field.getName(), helper);
                }
            }
        }
    }

    private void getIncluded(final Field field, final MapperObject object,
                             final String type, final String id, Map<String, Object> fieldMap) {

        for (Map<String, Object> includedEntry : object.included) {
            if (includedEntry.get(TYPE).equals(type) && includedEntry.get(ID).equals(id)) {
                Map<String, Object> includedAttributes =
                    TypeUtils.castObjectToMap(includedEntry.get(ATTRIBUTES));

                fieldMap.putAll(includedAttributes);
            }
        }
    }


    private static boolean hasAttributes(MapperObject object) {
        return TypeUtils.castObjectToMap(object.data.get(0)).containsKey(ATTRIBUTES);
    }

    private static Map<String, Object> getAttributesRawMap(MapperObject object) {
        return TypeUtils.castObjectToMap(
            TypeUtils.castObjectToMap(object.data.get(0)).get(ATTRIBUTES)
        );
    }

    private static Map<String, Object> getRelationshipsRawMap(MapperObject object) {
        return TypeUtils.castObjectToMap(
            TypeUtils.castObjectToMap(object.data.get(0)).get(RELATIONSHIPS)
        );
    }

    private static Map<String, Object> getRelationshipData(final Map<String, Object> relMap) {
        return TypeUtils.castObjectToMap(relMap.get(DATA));
    }

    private static Map<String, Object>
    getInnerRelationshipMap(final Map<String, Object> relationships, final String key) {
        return TypeUtils.castObjectToMap(relationships.get(key));
    }
}
