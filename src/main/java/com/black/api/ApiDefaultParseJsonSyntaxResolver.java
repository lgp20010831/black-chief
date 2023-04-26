package com.black.api;

import com.alibaba.fastjson.JSON;

import java.util.Map;

import static com.black.api.ApiV2Utils.DEFAULT_FLAG;

public class ApiDefaultParseJsonSyntaxResolver extends AbstractApiRequestSyntaxResolver {


    private final FormatParser formatParser;

    public ApiDefaultParseJsonSyntaxResolver() {
        super(DEFAULT_FLAG);
        formatParser = new FormatParser();
    }

    @Override
    public void resolveHttpMethod(String expression, HttpMethod httpMethod, Map<String, Object> source) {
        String demo = formatParser.parse(expression);
        JSON json = formatParser.parseJSON(expression);
        httpMethod.setRequestDome(demo);
        httpMethod.setRequestJSON(json);
        httpMethod.setRequestInvokeDome(JSONTool.formatJson(json == null ? null : json.toString()));
    }
}
