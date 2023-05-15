package com.black.syntax;

import com.black.utils.ServiceUtils;

import java.util.Map;

public class DefaultSyntaxResolver extends AbstractSyntaxResolver{

    public DefaultSyntaxResolver(){
        super("");
    }

    @Override
    public Object resolver(String expression, Map<String, Object> source, SyntaxMetadataListener syntaxMetadataListener) {
        if (syntaxMetadataListener == null){
            return ServiceUtils.getByExpression(source, expression);
        }else {
            SyntaxMetadata metadata = SyntaxUtils.findValue(expression, source);
            Object value = metadata.getValue();
            if (syntaxMetadataListener != null){
                value = syntaxMetadataListener.modify(metadata);
            }
            return value;
        }
    }
}
