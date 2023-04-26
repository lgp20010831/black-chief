package com.black.api;

import com.black.syntax.AbstractSyntaxResolver;
import com.black.syntax.SyntaxMetadataListener;

import java.util.Map;

public abstract class AbstractApiRequestSyntaxResolver extends AbstractSyntaxResolver {


    public AbstractApiRequestSyntaxResolver(String flag) {
        super(flag);
    }

    @Override
    public Object resolver(String expression, Map<String, Object> source, SyntaxMetadataListener syntaxMetadataListener) {
        Object obj = source.get(ApiV2Utils.HTTP_METHOD_NAME);
        if (obj ==  null){
            throw new IllegalStateException("not set http method");
        }
        HttpMethod httpMethod = (HttpMethod) obj;
        resolveHttpMethod(expression, httpMethod, source);
        return null;
    }

    public abstract void resolveHttpMethod(String expression, HttpMethod httpMethod, Map<String, Object> source);
}
