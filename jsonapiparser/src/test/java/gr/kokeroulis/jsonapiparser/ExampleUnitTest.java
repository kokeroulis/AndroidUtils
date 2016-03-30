package gr.kokeroulis.jsonapiparser;

import com.squareup.moshi.Moshi;

import org.junit.Test;

import java.io.IOException;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */



public class ExampleUnitTest {


    @Test public void loadJson() {
        Moshi moshi = new Moshi.Builder().build();
        try {
            Mapper m = new Mapper(TestJson.TEST_JSON, Article.class, moshi);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}