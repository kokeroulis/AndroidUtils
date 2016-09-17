package gr.kokeroulis.jsonapiparser.retrofit;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

import java.io.IOException;
import java.lang.annotation.Annotation;

import gr.kokeroulis.jsonapiparser.JsonRaw;
import gr.kokeroulis.jsonapiparser.Resource;
import gr.kokeroulis.jsonapiparser.TypeUtils;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.Buffer;
import retrofit2.Converter;

public class RetrofitRequestBodyConverter<T> implements Converter<T, RequestBody> {
    private static final MediaType MEDIA_TYPE = MediaType.parse("application/json; charset=UTF-8");
    private final Moshi mMoshi;
    private final JsonAdapter<T> mAdapter;

    public RetrofitRequestBodyConverter(Moshi moshi, JsonAdapter<T> adapter) {
        mMoshi = moshi;
        mAdapter = adapter;
    }

    @Override
    public RequestBody convert(T value) throws IOException {
        final Buffer buffer = new Buffer();
        final Annotation jsonRaw = value.getClass().getAnnotation(JsonRaw.class);
        if (TypeUtils.isAnnotationPresent(value.getClass(), Resource.class)) {
            throw new IllegalArgumentException("This kind of request is not supported any more. Please contact us!");
        } else if (jsonRaw == null) {
            mAdapter.toJson(buffer, value);
            return RequestBody.create(MEDIA_TYPE, buffer.readByteString());
        } else {
            mMoshi.adapter(Object.class).toJson(buffer, value);
            return RequestBody.create(MEDIA_TYPE, buffer.readByteString());
        }
    }
}
