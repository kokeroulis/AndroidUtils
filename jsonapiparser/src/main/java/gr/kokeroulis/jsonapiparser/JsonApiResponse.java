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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.Observable;

public class JsonApiResponse {
    private Object data;
    private List<Map<String, Object>> included;
    private Map<String, Object> meta;
    private Map<String, Object> links;

    public Observable<Map<String, Object>> included() {

        if (included == null) {
            // All the above chaos comes from the fact that
            // not all of our jsons contains an included object.
            // So we do the following workaround.
            // Rx java cannot create an observable from null
            // So we must add a dummy body here which is never
            // going to be converted to an object, because
            // it is unrelated with the rest of our data.
            // Also rx java cannot iterate into to an empty array
            // that's why we are creating a dummy object.
            included = new ArrayList<Map<String, Object>>();
            Map<String, Object> dummy = new HashMap<>();
            dummy.put("type", "dumyyyyy");
            dummy.put("id", "-111111");
            included.add(dummy);
            return Observable.from(included);
        }
        return Observable.from(included);
    }

    public Observable<Map<String, Object>> data() {
        if(data instanceof List) return Observable.from((List<Map<String,Object>>)data);
        else return Observable.just((Map<String,Object>)data);
    }
}