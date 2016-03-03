/*
 * Copyright (C) 2016 Antonis Tsiapaliokas
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
package gr.kokeroulis.jsonapiparser.adapters;

import com.squareup.moshi.FromJson;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import gr.kokeroulis.jsonapiparser.models.JsonApiJson;
import gr.kokeroulis.jsonapiparser.models.JsonMapper;
import rx.Observable;

public class JsonMapperAdapter {

    @FromJson
    JsonMapper fromJson(JsonApiJson json) {
        JsonMapper mapper = new JsonMapper();
        mapper.formatedData = new ArrayList<>();

        Observable.from(json.data)
            .doOnNext(dataMapper -> {
                Object attrs = dataMapper.get("attributes");
                dataMapper.putAll(castObjectToMap(attrs));
                dataMapper.remove("attributes");

                if (!dataMapper.containsKey("relationships")) {
                    return;
                }
                Observable
                    .just(dataMapper.get("relationships"))
                    .map(JsonMapperAdapter::castObjectToMap)
                    .flatMap(relationship -> Observable.from(relationship.entrySet()))
                    .doOnNext(stringObjectEntry -> {
                        Observable.just(stringObjectEntry)
                            .map(relEntry -> relEntry.getValue())
                            .map(JsonMapperAdapter::castObjectToMap)
                            .map(entryData -> entryData.get("data"))
                            .filter(relData -> relData != null && json.included != null)
                            .flatMap(relData -> {
                                if (relData instanceof List) {
                                    return Observable.from((List<Map<String, Object>>) relData)
                                        .flatMap(currentRelData ->
                                            getIncludedFromRel(json, currentRelData))
                                        .toList();

                                } else {
                                    return Observable.just((Map<String, Object>) relData)
                                        .flatMap(currentRelData ->
                                            getIncludedFromRel(json, currentRelData));
                                }
                            })
                            .subscribe(o -> {
                                dataMapper.put(stringObjectEntry.getKey(), o);
                            });

                    }).subscribe();
            }).subscribe(stringObjectMap -> {
                mapper.formatedData.add(stringObjectMap);
            });


        return mapper;
    }

    private static Map<String, Object> castObjectToMap(Object obj) {
        if (!(obj instanceof Map)) {
            throw new IllegalArgumentException(" Object is not instance of map.");
        }

        return (Map<String, Object>) obj;
    }

    private static Observable<Map<String, Object>> getIncludedFromRel(JsonApiJson json, Map<String, Object> relData) {
        return Observable.from(json.included)
            .filter(included -> included.get("type").equals(relData.get("type"))
                && included.get("id").equals(relData.get("id")))
            .map(included -> {
                relData.putAll(castObjectToMap(included.get("attributes")));
                if (included.containsKey("relationships")) {
                    relData.putAll(castObjectToMap(included.get("relationships")));
                }
                return relData;
            });
    }
}
