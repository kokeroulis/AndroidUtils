import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

import org.testng.annotations.Test;

import gr.kokeroulis.jsonapiparser.adapters.JsonMapperAdapter;
import gr.kokeroulis.jsonapiparser.models.JsonMapper;

/**
 * Created by kokeroulis on 11/14/15.
 */



public class test {

    private static final String test = "{\n" +
        "  \"links\": {\n" +
        "    \"self\": \"https://api.vivantehealth.com/dev/message/\"\n" +
        "  },\n" +
        "  \"data\": [\n" +
        "    {\n" +
        "      \"type\": \"message\",\n" +
        "      \"id\": \"2329\",\n" +
        "      \"attributes\": {\n" +
        "        \"body\": \"Message single this\",\n" +
        "        \"subject\": \"My Question\",\n" +
        "        \"type\": \"USER_MESSAGE\",\n" +
        "        \"state\": \"NEW\",\n" +
        "        \"created\": 1456855398,\n" +
        "        \"read_timestamp\": 1456855574,\n" +
        "        \"method\": \"WEB\",\n" +
        "        \"route_id\": \"2330\"\n" +
        "      },\n" +
        "      \"relationships\": {\n" +
        "        \"sender\": {\n" +
        "          \"links\": {\n" +
        "            \"self\": \"https://api.vivantehealth.com/dev/message/2329/relationships/sender\",\n" +
        "            \"related\": \"https://api.vivantehealth.com/dev/message/2329/sender\"\n" +
        "          },\n" +
        "          \"data\": {\n" +
        "            \"type\": \"user\",\n" +
        "            \"id\": \"7\"\n" +
        "          }\n" +
        "        },\n" +
        "        \"receiver\": {\n" +
        "          \"links\": {\n" +
        "            \"self\": \"https://api.vivantehealth.com/dev/message/2329/relationships/receiver\",\n" +
        "            \"related\": \"https://api.vivantehealth.com/dev/message/2329/receiver\"\n" +
        "          },\n" +
        "          \"data\": {\n" +
        "            \"type\": \"user\",\n" +
        "            \"id\": \"7643\"\n" +
        "          }\n" +
        "        },\n" +
        "        \"reply\": {\n" +
        "          \"links\": {\n" +
        "            \"self\": \"https://api.vivantehealth.com/dev/message/2329/relationships/reply\",\n" +
        "            \"related\": \"https://api.vivantehealth.com/dev/message/2329/reply\"\n" +
        "          }\n" +
        "        },\n" +
        "        \"answer\": {\n" +
        "          \"links\": {\n" +
        "            \"self\": \"https://api.vivantehealth.com/dev/message/2329/relationships/answer\",\n" +
        "            \"related\": \"https://api.vivantehealth.com/dev/message/2329/answer\"\n" +
        "          },\n" +
        "          \"data\": []\n" +
        "        },\n" +
        "        \"user_data_template\": {\n" +
        "          \"links\": {\n" +
        "            \"self\": \"https://api.vivantehealth.com/dev/message/2329/relationships/user_data_template\",\n" +
        "            \"related\": \"https://api.vivantehealth.com/dev/message/2329/user_data_template\"\n" +
        "          }\n" +
        "        }\n" +
        "      },\n" +
        "      \"links\": {\n" +
        "        \"self\": \"https://api.vivantehealth.com/dev/message/2329\"\n" +
        "      }\n" +
        "    }\n" +
        "  ],\n" +
        "  \"included\": [\n" +
        "    {\n" +
        "      \"type\": \"user\",\n" +
        "      \"id\": \"7\",\n" +
        "      \"attributes\": {\n" +
        "        \"created\": 1456056166,\n" +
        "        \"email\": \"nohponex+coach@gmail.com\",\n" +
        "        \"first_name\": \"Xenofon\",\n" +
        "        \"last_name\": \"Coach\",\n" +
        "        \"user_type\": \"COACH\",\n" +
        "        \"birth_date\": \"1991-08-23\",\n" +
        "        \"phone_mobile\": \"1235435435434\",\n" +
        "        \"avatar\": \"https://www.gravatar.com/avatar/2ce8ab698d5f008d98e4d01c85482ab1\"\n" +
        "      },\n" +
        "      \"relationships\": {\n" +
        "        \"condition\": {\n" +
        "          \"links\": {\n" +
        "            \"self\": \"https://api.vivantehealth.com/dev/user/7/relationships/condition\",\n" +
        "            \"related\": \"https://api.vivantehealth.com/dev/user/7/condition\"\n" +
        "          }\n" +
        "        }\n" +
        "      },\n" +
        "      \"links\": {\n" +
        "        \"self\": \"https://api.vivantehealth.com/dev/user/7\"\n" +
        "      }\n" +
        "    }\n" +
        "  ],\n" +
        "  \"meta\": {\n" +
        "    \"page\": {\n" +
        "      \"offset\": 0,\n" +
        "      \"limit\": 10\n" +
        "    },\n" +
        "    \"request_id\": \"320bfb45-6976-4603-b3db-fda68f556bd3\"\n" +
        "  },\n" +
        "  \"jsonapi\": {\n" +
        "    \"version\": \"1.0\"\n" +
        "  }\n" +
        "}";

    @Test void foo() throws Exception {
        Moshi moshi = new Moshi.Builder().add(new JsonMapperAdapter()).build();
        JsonAdapter<JsonMapper> adapter = moshi.adapter(JsonMapper.class);
        JsonMapper response = adapter.fromJson(test);
    }
}
