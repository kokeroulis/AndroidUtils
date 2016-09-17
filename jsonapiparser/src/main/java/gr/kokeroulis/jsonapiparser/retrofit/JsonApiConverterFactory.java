package gr.kokeroulis.jsonapiparser.retrofit;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;

public class JsonApiConverterFactory extends Converter.Factory {
    private final Moshi mMoshi;

    public JsonApiConverterFactory(Moshi moshi) {
        mMoshi = moshi;
    }

    @Override
    public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations, Retrofit retrofit) {
        final JsonAdapter<?> adapter = mMoshi.adapter(type);
        return new JsonApiResponseBodyConverter<>(type, mMoshi, adapter);
    }

    @Override
    public Converter<?, RequestBody> requestBodyConverter(Type type, Annotation[] parameterAnnotations, Annotation[] methodAnnotations, Retrofit retrofit) {
        final JsonAdapter<?> adapter = mMoshi.adapter(type);
        return new RetrofitRequestBodyConverter<>(mMoshi, adapter);
    }
}
