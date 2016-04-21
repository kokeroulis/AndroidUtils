/*
 * Copyright (C) 2015 Antonis Tsiapaliokas
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gr.kokeroulis.jsonapiparser;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import gr.kokeroulis.jsonapiparser.models.JsonMapper;
import retrofit.converter.ConversionException;
import retrofit.converter.Converter;
import retrofit.mime.TypedInput;
import retrofit.mime.TypedOutput;
import rx.Observable;

public class JsonApiConverter implements Converter {
    private Moshi mMoshi;

    public JsonApiConverter(Moshi moshi) {
        mMoshi = moshi;
    }

    @Override
    public Object fromBody(TypedInput body, Type type) throws ConversionException {
        try {
            InputStream in = body.in();
            String json = fromJsonApi(fromStream(in), type);

            if(String.class.equals(type)) {
                return json;
            }
            else {
                return mMoshi.adapter(type).fromJson(json);
            }

        } catch (Exception e) {
            throw new ConversionException(e);
        }
    }

    @Override
    public TypedOutput toBody(Object object) {
        try {
            // Authentication sends an empty body,
            // So check for it!
            if (String.class.equals(object.getClass())) {
                String emptyJson = (String) object;
                // Send an empty body if we have no data.
                // We need this for authorization
                if (emptyJson.isEmpty()) {
                    return new JsonTypedOutput("");
                }
            }

            final String json;
            final Annotation jsonRaw = object.getClass().getAnnotation(JsonRaw.class);
            if (jsonRaw == null) {
                json = toJsonApi(object);
            } else {
                json = mMoshi.adapter(Object.class).toJson(object);
            }

            return new JsonTypedOutput(json);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    private static class JsonTypedOutput implements TypedOutput {
        private String jsonString;


        JsonTypedOutput(String jsonString) { this.jsonString = jsonString; }

        @Override public String fileName() { return null; }
        @Override public String mimeType() { return "application/json"; }
        @Override public long length() {
            try {
                ByteArrayOutputStream bos = new ByteArrayOutputStream(jsonString.length());
                bos.write(jsonString.getBytes());
                return bos.size();
            } catch (Exception e) {
                return 0;
            }
        }
        @Override public void writeTo(OutputStream out) throws IOException { out.write(jsonString.getBytes()); }
    }

    private String fromStream(InputStream in) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        StringBuilder out = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            out.append(line);
            out.append("\r\n");
        }
        return out.toString();
    }

    private String fromJsonApi(String json, Type type) throws IOException {
        Map<String, Object> responseHash;
        try {
            responseHash = objectToMap(json);
        } catch (Exception e) {
            responseHash = null;
        }

        // if our response is an error response,
        // then we don't want to parse it
        if (responseHash != null && responseHash.get("errors") != null) {
            return json;
        }

        Class<?> typeClass = (Class<?>) type;
        if (TypeUtils.isAnnotationPresent(typeClass, Resource.class)) {
            Mapper<Object> mapper = new Mapper<>(json, type, mMoshi);
            Object data =  mapper.fromJson();
            JsonAdapter<Object> map = mMoshi.adapter(Object.class);
            return map.toJson(data);
        }
        JsonAdapter<JsonMapper> adapter = mMoshi.adapter(JsonMapper.class);
        JsonMapper response = adapter.fromJson(json);

        Type mapListType = Types.newParameterizedType(List.class, Map.class, String.class, Object.class);
        JsonAdapter<List<Map<String, Object>>> dataMapListAdapter = mMoshi.adapter(mapListType);

        Type mapType = Types.newParameterizedType(Map.class, String.class, Object.class);
        JsonAdapter<Map<String, Object>> dataMapAdapter = mMoshi.adapter(mapType);

        if (!List.class.isAssignableFrom(Types.getRawType(type))) { // its object
            return dataMapAdapter.toJson(response.formatedData.get(0));
        } else {
            return dataMapListAdapter.toJson(response.formatedData);
        }
    }

    private String toJsonApi(Object json) throws Exception {
        Map<String,Object> jsonMap = objectToMap(json);
        Map<String,Object> data = new HashMap<>();
        Map<String, Object> attributes = new HashMap<>();
        Map<String, Object> relationships = new HashMap<>();


        if (jsonMap.containsKey("id") && jsonMap.containsKey("type")) {
            attributes.putAll(jsonMap);
            Observable.from(attributes.entrySet())
                .subscribe(map -> jsonMap.remove(map.getKey()));

            final String id = attributes.remove("id").toString();
            final String type = attributes.remove("type").toString();
            jsonMap.put("attributes", attributes);
            jsonMap.put("type", type);

            Map<String, Object> helper = new HashMap<>();
            Map<String, Object> dataRel = new HashMap<>();
            Map<String, Object> typeRel = new HashMap<>();

            helper.put("id", id);
            helper.put("type", type + "_template");

            dataRel.put("data", helper);
            typeRel.put(type + "_template", dataRel);
            relationships.put("relationships", typeRel);

            jsonMap.putAll(relationships);

            if (attributes.get("value") instanceof Map) {
                data.put("data", jsonMap);
            } else {
                List<Map<String, Object>> listHelper = new ArrayList<>();
                listHelper.add(jsonMap);
                data.put("data", listHelper);
            }
        } else {
            Observable.from(jsonMap.entrySet())
                .filter(entry -> !entry.getKey().equals("type")
                    && !entry.getKey().equals("id"))
                .subscribe(map -> attributes.put(map.getKey(), map.getValue()));

            Observable.from(attributes.entrySet())
                .subscribe(map -> jsonMap.remove(map.getKey()));

            jsonMap.put("attributes", attributes);
            data.put("data", jsonMap);
        }

        return mMoshi.adapter(Object.class).toJson(data);
    }

    public Map<String, Object> objectToMap(Object object) throws Exception {
        final String jsonString = mMoshi.adapter(Object.class).toJson(object);
        return objectToMap(jsonString);
    }

    public Map<String, Object> objectToMap(String object) throws Exception {
        Type hash = Types.newParameterizedType(Map.class, String.class, Object.class);
        JsonAdapter<HashMap<String, Object>> adapter = mMoshi.adapter(hash);
        Map<String,Object> jsonHash = adapter.fromJson(object);
        return jsonHash;
    }
}