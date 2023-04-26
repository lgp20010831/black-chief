package com.black.core.sql.code.wrapper;

import com.alibaba.fastjson.JSON;
import com.black.core.query.MethodWrapper;
import com.black.core.sql.annotation.WriedDeleteStatement;
import com.black.core.sql.code.aop.WrapperMethodNotQualifiedException;
import com.black.core.sql.code.sqls.BoundStatement;
import com.black.core.util.AnnotationUtils;
import com.black.sql.SqlOutStatement;
import com.black.sql.SqlWriter;

import java.util.Map;

public class DeleteStatementHandler extends AbstractStatemenHandler{

    @Override
    public boolean supportCreateConfiguration(MethodWrapper wrapper) {
        return wrapper.parameterHasAnnotation(WriedDeleteStatement.class);
    }

    @Override
    public WrapperConfiguration createConfiguration(MethodWrapper wrapper) throws WrapperMethodNotQualifiedException {
        DeleteWrapperConfiguration configuration = new DeleteWrapperConfiguration();
        WriedDeleteStatement annotation = wrapper.getSingleParameterByAnnotation(WriedDeleteStatement.class)
                .getAnnotation(WriedDeleteStatement.class);
        configuration.setMw(wrapper);
        return AnnotationUtils.loadAttribute(annotation, configuration, true).init();
    }

    @Override
    public boolean support(WrapperConfiguration configuration) {
        return configuration instanceof DeleteWrapperConfiguration;
    }

    @Override
    public Object handler(Object arg, WrapperConfiguration configuration) {
        DeleteWrapperConfiguration deleteWrapperConfiguration = (DeleteWrapperConfiguration) configuration;
        SqlOutStatement statement = SqlWriter.delete(configuration.getTableName());
        BoundStatement boundStatement = handlerObject(arg, statement, configuration);
        Map<String, Object> primordialArgs;
        if (arg instanceof Map){
            primordialArgs = (Map<String, Object>) arg;
        }else {
            primordialArgs = JSON.parseObject(arg.toString());
        }
        //这些是参数中必须存在的key
        String[] properties = deleteWrapperConfiguration.getRequiredProperties();
        for (String property : properties) {
            if (!primordialArgs.containsKey(property)){
                throw new RuntimeException("参数缺少:" + property);
            }
        }

        return boundStatement;
    }
}
