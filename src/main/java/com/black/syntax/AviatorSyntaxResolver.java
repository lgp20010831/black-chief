package com.black.syntax;

import com.black.aviator.AviatorManager;

import java.util.Map;

public class AviatorSyntaxResolver extends AbstractSyntaxResolver{

    public AviatorSyntaxResolver(){
        super("A: ");
    }

    @Override
    public Object resolver(String expression, Map<String, Object> source, SyntaxMetadataListener syntaxMetadataListener) {
        return AviatorManager.getInstance().execute(expression, source);
    }
}
