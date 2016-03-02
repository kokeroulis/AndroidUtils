package gr.kokeroulis.jsonapiparser.models.json;

import java.util.List;
import java.util.Map;

import gr.kokeroulis.jsonapiparser.models.DataMapper;
import gr.kokeroulis.jsonapiparser.models.IncludedMapper;
import gr.kokeroulis.jsonapiparser.models.JsonMapper;

/**
 * Created by kokeroulis on 3/2/16.
 */
public class JsonApiJson {
    public List<DataMapper> data;
    public List<IncludedMapper> included;
}
