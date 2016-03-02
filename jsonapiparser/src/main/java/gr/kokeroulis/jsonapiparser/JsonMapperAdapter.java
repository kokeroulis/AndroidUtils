package gr.kokeroulis.jsonapiparser;

import com.squareup.moshi.FromJson;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Set;

import gr.kokeroulis.jsonapiparser.models.DataMapper;
import gr.kokeroulis.jsonapiparser.models.JsonMapper;
import gr.kokeroulis.jsonapiparser.models.RelationshipsModel;
import gr.kokeroulis.jsonapiparser.models.TypeIDMapper;
import gr.kokeroulis.jsonapiparser.models.json.JsonApiJson;
import rx.Observable;

public class JsonMapperAdapter {

    @FromJson
    JsonMapper fromJson(JsonApiJson json) {
        JsonMapper mapper = new JsonMapper();
        mapper.foo = new ArrayList<>();
        Observable.from(json.data)
            .flatMap(d -> Observable.from(d.relationships.entrySet())
                .filter(entry -> {
                    RelationshipsModel rel = entry.getValue();
                    return rel.data != null && rel.data.id != null && rel.data.type != null;
                })
                .flatMap(relModel -> Observable.from(json.included)
                    .filter(f -> {
                        RelationshipsModel rel = relModel.getValue();
                        TypeIDMapper type = rel.data;
                        return type.id.equals(f.id) && type.type.equals(f.type);
                    })
                    .filter(included -> included != null && included.attributes != null)
                    .map(included-> included.attributes)
                    .doOnNext(attrs -> d.attributes.put(relModel.getKey(), attrs))
                    .doOnNext(attrs -> d.attributes.put("id", d.id))
                    .doOnNext(attrs -> mapper.foo.add(d.attributes)))).subscribe();
        mapper.data = json.data;
        return mapper;
    }


    public static class Messages {
        public String subject;
        public String body;
        public String method;
        public int created;
        public transient int icon;
        public String id;
        public User sender;
        public String questionName;

    }

    public static class User {
        public int id;
        public String email;
        public String username;
        public String first_name;
        public String name;
        public String user_type;
        public String avatar;
    }

   /* @FromJson
    Messages fromMessages(AttributesMapper mapper) {
        return null;
    }*/

    public class foo implements JsonAdapter.Factory {

        @Override
        public JsonAdapter<DataMapper> create(Type type, Set<? extends Annotation> annotations, Moshi moshi) {
            return null;
        }
    }
}
