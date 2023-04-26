package com.black.core.mybatis.source;

import com.black.core.mybatis.source.annotation.SQLClient;
import com.black.core.mybatis.source.annotation.SQLDynamicallyClients;
import org.springframework.core.annotation.AnnotationUtils;

import java.util.HashMap;
import java.util.Map;

public class AnnotationSqlClientsHandler {


    private final Class<?> targetClass;

    private final AttributeParser attributeParser;

    public AnnotationSqlClientsHandler(Class<?> targetClass, AttributeParser attributeParser) {
        this.targetClass = targetClass;
        this.attributeParser = attributeParser;
    }

    public boolean canParse(){
        return targetClass != null && AnnotationUtils.getAnnotation(targetClass, SQLDynamicallyClients.class) != null;
    }

    public Map<String, DataSourceConnectWrapper> parserTarget(){
        SQLDynamicallyClients dynamicallyClients = AnnotationUtils.getAnnotation(targetClass, SQLDynamicallyClients.class);
        SQLClient[] sqlClients = dynamicallyClients.value();
        Map<String, DataSourceConnectWrapper> result = new HashMap<>();
        for (SQLClient sqlClient : sqlClients) {
            String alias = sqlClient.alias();
            if (result.containsKey(alias)){
                throw new RuntimeException("发现重名的连接源: " + alias);
            }
            DataSourceConnectWrapper wrapper = new DataSourceConnectWrapper(alias);
            wrapper.url(attributeParser.getReallyValue(sqlClient.url()));
            wrapper.driver(attributeParser.getReallyValue(sqlClient.driver()));
            wrapper.username(attributeParser.getReallyValue(sqlClient.username()));
            wrapper.password(attributeParser.getReallyValue(sqlClient.password()));
            result.put(alias, wrapper);
        }
        return result;
    }
}
