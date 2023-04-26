package com.black.json;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public interface JsonParser {


    Object parse(String text);

    JSONObject parseJson(String text);

    JSONArray parseArray(String text);
}
