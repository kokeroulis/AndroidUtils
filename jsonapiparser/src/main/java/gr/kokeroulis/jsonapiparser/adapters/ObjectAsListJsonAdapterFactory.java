/* from Jake Wharton https://github.com/square/moshi/issues/109 */
package gr.kokeroulis.jsonapiparser.adapters;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.JsonReader;
import com.squareup.moshi.JsonWriter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public final class ObjectAsListJsonAdapterFactory implements JsonAdapter.Factory {
    @Override public JsonAdapter<?> create(Type type, Set<? extends Annotation> annotations, Moshi moshi) {
        if (!List.class.isAssignableFrom(Types.getRawType(type))) {
            return null;
        }
        JsonAdapter<List<Object>> listDelegate = moshi.nextAdapter(this, type, annotations);
        Type innerType = Types.collectionElementType(type, List.class);
        JsonAdapter<Object> objectDelegate = moshi.adapter(innerType, annotations);
        return new ListJsonAdapter<>(listDelegate, objectDelegate);
    }

    static class ListJsonAdapter<T> extends JsonAdapter<List<T>> {
        private final JsonAdapter<List<T>> listDelegate;
        private final JsonAdapter<T> objectDelegate;

        private ListJsonAdapter(JsonAdapter<List<T>> listDelegate, JsonAdapter<T> objectDelegate) {
            this.listDelegate = listDelegate;
            this.objectDelegate = objectDelegate;
        }

        @Override public List<T> fromJson(JsonReader jsonReader) throws IOException {
            if (jsonReader.peek() == JsonReader.Token.BEGIN_OBJECT) {
                return Collections.singletonList(objectDelegate.fromJson(jsonReader));
            } else {
                return listDelegate.fromJson(jsonReader);
            }
        }

        @Override public void toJson(JsonWriter jsonWriter, List<T> list) throws IOException {
            listDelegate.toJson(jsonWriter, list);
        }
    }
}
