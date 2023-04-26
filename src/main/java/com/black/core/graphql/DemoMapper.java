package com.black.core.graphql;

import com.black.graphql.annotation.GraphqlClient;
import com.black.graphql.annotation.ObjectParam;
import com.black.graphql.annotation.ResultAttributes;

import java.util.Map;

@GraphqlClient("http://10.20.252.11:30401/api")
@ResultAttributes("messages[code, field, message, template], successful")
public interface DemoMapper {


    @ResultAttributes("result[key, value, title, id, pid, code]")
    Map<String, Object> listPositionTree();


    @ResultAttributes("result[id]")
    Map<String, Object> addOrUpdatePosition(@ObjectParam("savePosition") Map<String, Object> map);
}
