package gr.kokeroulis.jsonapiparser;

import com.squareup.moshi.Json;

public class Author {
    @Json(name = "first-name")
    public String first_name;
    @Json(name = "last-name")
    public String last_name;
    public String twitter;
}
