package gr.kokeroulis.jsonapiparser.retrofit;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Map;

import gr.kokeroulis.jsonapiparser.JsonRaw;
import gr.kokeroulis.jsonapiparser.Mapper;
import gr.kokeroulis.jsonapiparser.Resource;
import gr.kokeroulis.jsonapiparser.TypeUtils;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.Buffer;
import retrofit2.Converter;

public class RetrofitRequestBodyConverter<T> implements Converter<T, RequestBody> {
    private static final MediaType MEDIA_TYPE = MediaType.parse("application/json; charset=UTF-8");
    private final Moshi mMoshi;

    public RetrofitRequestBodyConverter(Moshi moshi) {
        mMoshi = moshi;
    }

    @Override
    public RequestBody convert(T value) throws IOException {
        final String json;
        final Annotation jsonRaw = value.getClass().getAnnotation(JsonRaw.class);
        if (TypeUtils.isAnnotationPresent(value.getClass(), Resource.class)) {
            Mapper<Map<String, Object>> mapper = Mapper.nullSafe(mMoshi);
            Type mapType = Types.newParameterizedType(Map.class, String.class, Object.class);
            JsonAdapter<Map<String, Object>> adapter = mMoshi.adapter(mapType);
            Map<String, Object> data = null;

            try {
                data = mapper.toJson(value);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        if (jsonRaw == null) {
            throw new IllegalArgumentException("this kind of post request is unsupported " + value.toString());
        } else {
            Buffer buffer = new Buffer();
            mMoshi.adapter(Object.class).toJson(buffer, value);
            return RequestBody.create(MEDIA_TYPE, buffer.readByteString());
        }
    }
}
