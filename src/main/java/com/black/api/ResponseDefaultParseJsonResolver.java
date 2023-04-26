package com.black.api;

import java.util.Map;

import static com.black.api.ApiV2Utils.DEFAULT_FLAG;

public class ResponseDefaultParseJsonResolver extends AbstractApiResponseSyntaxResolver{

    private final FormatParser formatParser;

    public ResponseDefaultParseJsonResolver() {
        super(DEFAULT_FLAG);
        formatParser = new FormatParser();
    }

    @Override
    public Object resolveHttpMethod(String expression, HttpMethod httpMethod, Map<String, Object> source) {
        return formatParser.parseJSON(expression);
    }
}
