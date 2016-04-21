package gr.kokeroulis.jsonapiparser;

import java.util.List;

public class Article {
    public String type;
    public String id;
    public String title;
    @Relationship( type = "author")
    public Author author;

    @Relationship(type = "comments")
    public List<Comments> comments;
}
