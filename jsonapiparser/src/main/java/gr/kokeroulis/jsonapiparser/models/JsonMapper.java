package gr.kokeroulis.jsonapiparser.models;

import java.util.List;
import java.util.Map;

public class JsonMapper {
    public List<DataMapper> data;
    public List<IncludedMapper> included;
    public transient List<Map<String, Object>> formatedData;
    public transient List<Map<String, Object>> foo;
}
