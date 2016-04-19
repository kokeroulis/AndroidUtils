package gr.kokeroulis.jsonapiparser;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import gr.kokeroulis.jsonapiparser.models.MapperObject;

public class Mapper<T> {
    private static final String ATTRIBUTES = "attributes";
    private static final String DATA = "data";
    private static final String RELATIONSHIPS = "relationships";
    private static final String TYPE = "type";
    private static final String INCLUDED = "included";
    private static final String ID = "id";


    private final MapperObject jsonElement;
    private final Moshi mMoshi;
    private final Type classType;

    public Mapper(final String json, final Type classType, final Moshi moshi) throws IOException {
        JsonAdapter<MapperObject> adapter = moshi.adapter(MapperObject.class);
        mMoshi = moshi;
        jsonElement = adapter.fromJson(json);
        this.classType = classType;
    }


    public T fromJson() throws IOException {
        Class<?> type = Types.getRawType(classType);
        if (List.class.isAssignableFrom(type)) {
            Type actualType = Types.collectionElementType(classType, List.class);
            Class<?> elementClass = (Class<?>) actualType;
            List<Map<String, Object>> mapList = new ArrayList<>();
            for (Map<String, Object> resourceData : jsonElement.data) {
                mapList.add(parseResource(resourceData, elementClass));
            }

            JsonAdapter<T> adapterObject = mMoshi.adapter(classType);
            Type mapType = Types.newParameterizedType(List.class, Map.class, String.class, Object.class);
            JsonAdapter<List<Map<String, Object>>> mapAdapter = mMoshi.adapter(mapType);

            return adapterObject.fromJson(mapAdapter.toJson(mapList));
        } else {
            Map<String, Object> mapObject = parseResource(
                TypeUtils.castObjectToMap(jsonElement.data.get(0)),
                classType.getClass()
            );

            JsonAdapter<T> adapterObject = mMoshi.adapter(classType);
            Type mapType = Types.newParameterizedType(Map.class, String.class, Object.class);
            JsonAdapter<Map<String, Object>> mapAdapter = mMoshi.adapter(mapType);
            return adapterObject.fromJson(mapAdapter.toJson(mapObject));
        }
    }

    private Map<String, Object> parseResource(Map<String, Object> resourceData, Class<?> elementClass) {
        Field[] fields = elementClass.getDeclaredFields();
        Map<String, Object> helperResource = new LinkedHashMap<>();
        for (Field field : fields) {
            if (!TypeUtils.isPublic(field)) {
                continue;
            }

            if (TypeUtils.isAnnotationPresent(field, Relationship.class)) {
                helperResource.putAll(getRelationships(field, getRelationshipsRawMap(resourceData)));
            } else if (hasAttributes(resourceData)) {
                final Map<String, Object> attributes = getAttributesRawMap(resourceData);
                helperResource.putAll(getAttributes(field, attributes));
            }
        }

        return helperResource;
    }

    private Map<String, Object> getAttributes(final Field field, final Map<String, Object> attributes) {
        Map<String, Object> helperMap = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entrySet : attributes.entrySet()) {
            if (entrySet.getKey().equals(field.getName())) {
                helperMap.put(entrySet.getKey(), entrySet.getValue());
            }
        }

        return helperMap;
    }

    // needs annotiation
    private Map<String, Object> getRelationships(final Field field, final Map<String, Object> relationships) {
        Map<String, Object> helperMap = new LinkedHashMap<>();
        final Relationship relationship = TypeUtils.getAnnotation(field, Relationship.class);
        for (Map.Entry<String, Object> entrySet : relationships.entrySet()) {
            if (entrySet.getKey().equals(relationship.type())) {
                Map<String, Object> relMap = TypeUtils.castObjectToMap(entrySet.getValue());
                Object relMapDataRaw = relMap.get(DATA);
                if (List.class.isAssignableFrom(relMapDataRaw.getClass())) {
                    List<Object> relList = (List<Object>) relMapDataRaw;
                    List<Map<String, Object>> helperList = new ArrayList<>();
                    for (Object ob : relList) {
                        Map<String, Object> relObjectMap = TypeUtils.castObjectToMap(ob);
                        helperList.add(getRelationshipsForObject(relObjectMap, field));
                    }
                    helperMap.put(field.getName(), helperList);
                } else {
                    Map<String, Object> relData = getRelationshipData(relMap);
                    helperMap.put(field.getName(), getRelationshipsForObject(relData, field));
                }
            }
        }

        return helperMap;
    }

    private Map<String, Object> getRelationshipsForObject(final Map<String, Object> relData, final Field field) {
        Map<String, Object> helper = new HashMap<>();
        helper.putAll(relData);
        final String id = helper.get(ID).toString();
        final String type = helper.get(TYPE).toString();
        if (jsonElement.included != null) {
            getIncluded(jsonElement, type, id, helper);
        }

        return helper;
    }

    private void getIncluded(final MapperObject object,
                             final String type, final String id, Map<String, Object> fieldMap) {

        for (Map<String, Object> includedEntry : object.included) {
            if (includedEntry.get(TYPE).equals(type) && includedEntry.get(ID).equals(id)) {
                Map<String, Object> includedAttributes =
                    TypeUtils.castObjectToMap(includedEntry.get(ATTRIBUTES));

                fieldMap.putAll(includedAttributes);
            }
        }
    }

    private static boolean hasAttributes(Map<String, Object> object) {
        return object.containsKey(ATTRIBUTES);
    }

    private static Map<String, Object> getAttributesRawMap(Map<String, Object> object) {
        return TypeUtils.castObjectToMap(object.get(ATTRIBUTES));
    }

    private static Map<String, Object> getRelationshipsRawMap(Map<String, Object> object) {
        return TypeUtils.castObjectToMap(object.get(RELATIONSHIPS));
    }

    private static Map<String, Object> getRelationshipData(final Map<String, Object> relMap) {
        return TypeUtils.castObjectToMap(relMap.get(DATA));
    }

    private static Map<String, Object>
    getInnerRelationshipMap(final Map<String, Object> relationships, final String key) {
        return TypeUtils.castObjectToMap(relationships.get(key));
    }
}
