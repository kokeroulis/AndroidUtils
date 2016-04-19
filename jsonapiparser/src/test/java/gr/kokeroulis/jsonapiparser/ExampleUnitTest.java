package gr.kokeroulis.jsonapiparser;

import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;

import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

public class ExampleUnitTest {


    @Test public void loadJson() {
        Moshi moshi = new Moshi.Builder().build();
        try {
            Type mapType = Types.newParameterizedType(List.class, Article.class);
            Mapper<List<Article>> m = new Mapper<>(TestJson.TEST_JSON, mapType, moshi);
            List<Article> a = m.fromJson();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}