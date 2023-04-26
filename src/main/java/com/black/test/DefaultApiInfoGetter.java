package com.black.test;

import com.black.api.Configuration;
import com.black.api.HttpApiParser;
import com.black.api.HttpMethod;
import com.black.core.query.ClassWrapper;
import com.black.core.query.MethodWrapper;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultApiInfoGetter implements ApiInfoGetter{

    private Map<Method, HttpMethod> cache = new ConcurrentHashMap<>();

    private final HttpApiParser apiParser;

    public DefaultApiInfoGetter(Connection connection) {
        Configuration configuration = new Configuration();
        configuration.setConnection(connection);
        apiParser = new HttpApiParser(configuration);
    }

    @Override
    public String[] getRequestUrls(MethodWrapper mw, ClassWrapper<?> cw) {
        HttpMethod httpMethod = cache.computeIfAbsent(mw.getMethod(), m -> processorMethod(mw, cw));
        return httpMethod.getRequestUrl().toArray(new String[0]);
    }

    @Override
    public Map<String, String> getHeaderMap(MethodWrapper mw, ClassWrapper<?> cw) {
        HttpMethod httpMethod = cache.computeIfAbsent(mw.getMethod(), m -> processorMethod(mw, cw));
        return httpMethod.getHeaders();
    }

    @Override
    public String[] getRequestMethods(MethodWrapper mw, ClassWrapper<?> cw) {
        HttpMethod httpMethod = cache.computeIfAbsent(mw.getMethod(), m -> processorMethod(mw, cw));
        return httpMethod.getRequestMethod().toArray(new String[0]);
    }

    @Override
    public Object getRequestExample(MethodWrapper mw, ClassWrapper<?> cw) {
        HttpMethod httpMethod = cache.computeIfAbsent(mw.getMethod(), m -> processorMethod(mw, cw));
        return httpMethod.getRequestJSON();
    }

    protected HttpMethod processorMethod(MethodWrapper mw, ClassWrapper<?> cw){
        try {
            return apiParser.parseMethod(mw, cw.get());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
