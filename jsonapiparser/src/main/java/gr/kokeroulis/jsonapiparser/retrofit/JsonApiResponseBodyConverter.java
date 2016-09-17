package gr.kokeroulis.jsonapiparser.retrofit;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import gr.kokeroulis.jsonapiparser.Mapper;
import gr.kokeroulis.jsonapiparser.Resource;
import gr.kokeroulis.jsonapiparser.TypeUtils;
import gr.kokeroulis.jsonapiparser.models.JsonMapper;
import okhttp3.ResponseBody;
import okio.BufferedSource;
import retrofit2.Converter;

public class JsonApiResponseBodyConverter <T> implements Converter<ResponseBody, T> {
    private final Type type;
    private final Moshi mMoshi;
    private final JsonAdapter<T> mAdapter;

    public JsonApiResponseBodyConverter(Type type, Moshi moshi, JsonAdapter<T> adapter) {
        this.type = type;
        this.mMoshi = moshi;
        this.mAdapter = adapter;
    }

    @Override
    public T convert(ResponseBody value) throws IOException {
        BufferedSource source = value.source();
        final String json = value.string();

        Class <?> typeClass;

        if (List.class.isAssignableFrom(Types.getRawType(type))) {
            typeClass = (Class<?>) Types.collectionElementType(type, List.class);
        } else {
            typeClass = (Class<?>) type;
        }
        if (TypeUtils.isAnnotationPresent(typeClass, Resource.class)) {
            Mapper<Object> mapper = new Mapper<>(json, type, mMoshi);
            Object data =  mapper.fromJson();
            final String dataJson = mMoshi.adapter(Object.class).toJson(data);
            return mAdapter.fromJson(dataJson);
        }
        JsonAdapter<JsonMapper> adapter = mMoshi.adapter(JsonMapper.class);
        JsonMapper response = adapter.fromJson(json);

        Type mapListType = Types.newParameterizedType(List.class, Map.class, String.class, Object.class);
        JsonAdapter<List<Map<String, Object>>> dataMapListAdapter = mMoshi.adapter(mapListType);

        Type mapType = Types.newParameterizedType(Map.class, String.class, Object.class);
        JsonAdapter<Map<String, Object>> dataMapAdapter = mMoshi.adapter(mapType);

        if (!List.class.isAssignableFrom(Types.getRawType(type))) { // its object
            final String dataJson = dataMapAdapter.toJson(response.formatedData.get(0));
            return mAdapter.fromJson(dataJson);
        } else {
            final String dataJson = dataMapListAdapter.toJson(response.formatedData);
            return mAdapter.fromJson(dataJson);
        }
    }
}
