package com.black.api;

import com.black.syntax.SyntaxInterlocutor;

import java.util.Map;

public abstract class AbstractApiLocutor implements SyntaxInterlocutor {

    @Override
    public void interlude(String item, Map<String, Object> env, Object resolveResult) {
        Object obj = env.get(ApiV2Utils.HTTP_METHOD_NAME);
        if (obj ==  null){
            throw new IllegalStateException("not set http method");
        }
        HttpMethod httpMethod = (HttpMethod) obj;
        interludeHttpMethod(item, env, httpMethod);
    }

    public abstract void interludeHttpMethod(String item, Map<String, Object> source, HttpMethod method);
}
