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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
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
    private Gson mGson;

    public JsonApiConverter(Gson gson) {
        mGson = gson;
    }

    @Override
    public Object fromBody(TypedInput body, Type type) throws ConversionException {
        try {
            InputStream in = body.in();
            String json = fromJsonApi(fromStream(in), type);

            if(String.class.equals(type)) return json;
            else return mGson.fromJson(json, type);

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
                if (emptyJson != null && emptyJson.isEmpty()) {
                    return new JsonTypedOutput("");
                }
            }

            String json = toJsonApi(object);
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

    private static String fromStream(InputStream in) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        StringBuilder out = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            out.append(line);
            out.append("\r\n");
        }
        return out.toString();
    }

    private static String fromJsonApi(String json, Type type) throws UnsupportedEncodingException {
        Gson gson = new Gson();

        HashMap<String, Object> responseHash;
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

        JsonApiResponse response = gson.fromJson(json, JsonApiResponse.class);
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
        if(data.size() == 1 && !isInstanceOfList) {
            formatted = data.get(0) == null ? json : gson.toJson(data.get(0));
        } else {
            formatted =  gson.toJson(data);
        }

        return formatted;
    }

    private static String toJsonApi(Object json) throws Exception {
        Gson gson = new Gson();
        Map<String,Object> jsonMap = objectToMap(json);
        Map<String,Object> data = new HashMap<>();
        Map<String, Object> attributes = new HashMap<>();

        Observable.from(jsonMap.entrySet())
                .filter(stringObjectEntry -> !stringObjectEntry.getKey().equals("type"))
                .subscribe(map -> attributes.put(map.getKey(), map.getValue()));

        Observable.from(attributes.entrySet())
                .subscribe(map -> jsonMap.remove(map.getKey()));

        jsonMap.put("attributes", attributes);
        data.put("data", jsonMap);

        return gson.toJson(data);
    }

    public static HashMap<String, Object> objectToMap(Object object) throws Exception {
        Gson gson = new Gson();
        final String jsonString = gson.toJson(object);
        return objectToMap(jsonString);
    }

    public static HashMap<String, Object> objectToMap(String object) throws Exception {
        HashMap<String,Object> jsonMap = new Gson().fromJson(object, new TypeToken<HashMap<String, Object>>() {
        }.getType());

        return jsonMap;
    }


}