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

import com.google.gson.reflect.TypeToken;
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

import retrofit.converter.ConversionException;
import retrofit.converter.Converter;
import retrofit.mime.TypedInput;
import retrofit.mime.TypedOutput;
import rx.Observable;

public class JsonApiConverter implements Converter {

    private static String RELATIONSHIP_KEY = "relationships";
    private static String ATTRIBUTES_KEY = "attributes";
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

        JsonAdapter<JsonApiResponse> adapter = mMoshi.adapter(JsonApiResponse.class);
        JsonApiResponse response = adapter.fromJson(json);
        List<Map<String, Object>> data = new ArrayList<>();

        response.data()
                .doOnNext(stringObjectMap -> {
                    stringObjectMap.putAll((Map<String, Object>) stringObjectMap.get(ATTRIBUTES_KEY));
                    stringObjectMap.remove(ATTRIBUTES_KEY);

                    // We don't need to parse Authentication object!
                    if (stringObjectMap.get(RELATIONSHIP_KEY) == null) {
                        return;
                    }
                    Observable.from(((Map<String, Object>) stringObjectMap.get(RELATIONSHIP_KEY)).keySet())
                        .filter(key -> ((Map<String, Object>) stringObjectMap.get(RELATIONSHIP_KEY)).get(key) instanceof Map || ((Map<String, Object>) stringObjectMap.get(RELATIONSHIP_KEY)).get(key) instanceof List)
                            .subscribe(key -> {
                                stringObjectMap.put(key, ((Map<String, Object>) ((Map<String, Object>) stringObjectMap.get(RELATIONSHIP_KEY)).get(key)).get("data"));

                                Observable<Map<String, Object>> inner;
                                Object includedLinks = stringObjectMap.get(key);

                                if (includedLinks instanceof List) inner = Observable.from((List<Map<String, Object>>) includedLinks);
                                else inner = Observable.just((Map<String, Object>) includedLinks);

                                inner.forEach(link -> response.included()
                                        .filter(included -> included.get("type").equals(link.get("type"))
                                                && included.get("id").equals(link.get("id")))
                                        .last()
                                        .subscribe(included -> {
                                            link.putAll((Map<String, Object>) included.get(ATTRIBUTES_KEY));
                                        }, error -> {
                                            // catch exception. We need this one!
                                            // look at the comments of JsonApiResponse
                                        }));
                            });
                    stringObjectMap.remove(RELATIONSHIP_KEY);
                })
                .subscribe(datum -> data.add(datum));

        String formatted;

        // List must preserve their structure when they
        // are a single item.
        // TL;DR don't convert the list into to a string
        // when it has only 1 item!
        String listTypeToString = TypeToken.get(type).getRawType().toString();
        boolean isInstanceOfList = listTypeToString.equals("interface java.util.List");
        if (data.size() == 1 && !isInstanceOfList) {
            if (data.get(0) == null) {
                return json;
            } else {
                formatted = mMoshi.adapter(Object.class).toJson(data.get(0));
            }
        } else {
            formatted =  mMoshi.adapter(Object.class).toJson(data);
        }

        return formatted;
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