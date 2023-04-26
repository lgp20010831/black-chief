package com.black.syntax;

import com.black.api.FormatParser;

import java.util.Map;

public class JSONSyntaxResolver extends AbstractSyntaxResolver{

    private FormatParser formatParser;

    public JSONSyntaxResolver(){
        super("J: ");
        formatParser = new FormatParser();
    }

    @Override
    public Object resolver(String expression, Map<String, Object> source, SyntaxMetadataListener syntaxMetadataListener) {
        return formatParser.parseJSON(expression);
    }
}
