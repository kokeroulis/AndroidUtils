package gr.kokeroulis.jsonapiparser;

/**
 * Created by kokeroulis on 30/03/16.
 */
public class Article {
    public String type;
    public String id;
    public String title;
    @Relationship( type = "people")
    public Author author;
}
