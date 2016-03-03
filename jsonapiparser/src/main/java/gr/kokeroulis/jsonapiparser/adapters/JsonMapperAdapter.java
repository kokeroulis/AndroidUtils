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
import java.util.LinkedHashMap;
import java.util.Map;

import gr.kokeroulis.jsonapiparser.models.JsonMapper;
import gr.kokeroulis.jsonapiparser.models.RelationshipsModel;
import gr.kokeroulis.jsonapiparser.models.TypeIDMapper;
import gr.kokeroulis.jsonapiparser.models.JsonApiJson;
import rx.Observable;

public class JsonMapperAdapter {

    @FromJson
    JsonMapper fromJson(JsonApiJson json) {
        JsonMapper mapper = new JsonMapper();
        mapper.formatedData = new ArrayList<>();
        Observable.from(json.data)
            .flatMap(d -> Observable.from(d.relationships.entrySet())
                .filter(entry -> entry.getValue().data != null)
                .filter(entry -> {
                    RelationshipsModel rel = entry.getValue();
                    if (rel.data.size() > 0) {
                        TypeIDMapper type = rel.data.get(0);
                        return type.id != null && type.type != null;
                    } else {
                        return false;
                    }
                })
                .flatMap(relModel -> Observable.from(json.included)
                    .filter(f -> {
                        RelationshipsModel rel = relModel.getValue();
                        TypeIDMapper type = rel.data.get(0);
                        return type.id.equals(f.id) && type.type.equals(f.type);
                    })
                    .filter(included -> included != null && included.attributes != null)
                    .doOnNext(included -> included.attributes.put("id", included.id))
                    .map(included-> included.attributes)
                    .doOnNext(attrs -> {
                        Map<String, Object> helper = new LinkedHashMap<>();
                        helper.put(relModel.getKey(), attrs);
                        helper.put("id", d.id);
                        d.attributes.putAll(helper);
                    })))
        .subscribe();

        Observable.from(json.data)
            .doOnNext(d -> mapper.formatedData.add(d.attributes))
            .subscribe();
        return mapper;
    }
}
