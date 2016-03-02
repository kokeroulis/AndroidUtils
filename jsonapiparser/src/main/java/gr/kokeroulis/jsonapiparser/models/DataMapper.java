package gr.kokeroulis.jsonapiparser.models;

import java.util.Map;

/**
 * Created by kokeroulis on 3/2/16.
 */
public class DataMapper {
    public String type;
    public String id;
    public Map<String, Object> attributes;
    public Map<String, RelationshipsModel> relationships;
}
