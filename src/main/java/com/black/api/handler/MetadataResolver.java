package com.black.api.handler;

import com.alibaba.fastjson.JSONObject;
import com.black.core.sql.code.AliasColumnConvertHandler;

public interface MetadataResolver {


    boolean support(Object metadata);


    void resolve(Object metadata, JSONObject sonJson,
                 AliasColumnConvertHandler handler, boolean request);
}
