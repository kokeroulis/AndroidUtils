package gr.kokeroulis.jsonapiparser;

import java.util.List;

/**
 * Created by kokeroulis on 30/03/16.
 */
public class Article {
    public String type;
    public String id;
    public String title;
    @Relationship( type = "author") // na ginei author.
    public Author author;

    @Relationship(type = "comments")
    public List<Comments> comments;
}
