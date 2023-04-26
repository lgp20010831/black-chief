package com.black.core.sql.code.wrapper;

import com.alibaba.fastjson.JSON;
import com.black.core.aop.servlet.ParameterWrapper;
import com.black.core.query.MethodWrapper;
import com.black.core.sql.SQLSException;
import com.black.core.sql.annotation.WriedRenewStatement;
import com.black.core.sql.code.AliasColumnConvertHandler;
import com.black.core.sql.code.aop.WrapperMethodNotQualifiedException;
import com.black.core.sql.code.mapping.GlobalMapping;
import com.black.core.sql.code.sqls.BoundStatement;
import com.black.core.sql.code.sqls.MappingVal;
import com.black.core.sql.unc.OperationType;
import com.black.core.util.AnnotationUtils;
import com.black.core.util.StringUtils;
import com.black.core.util.Utils;
import com.black.sql.SqlOutStatement;
import com.black.sql.SqlWriter;
import com.black.table.PrimaryKey;

import java.util.*;

public class UpdateStatementHandler extends AbstractStatemenHandler{
    @Override
    public boolean support(WrapperConfiguration configuration) {
        return configuration instanceof PowerWrapperConfiguration;
    }

    @Override
    public boolean supportCreateConfiguration(MethodWrapper wrapper) {
        return wrapper.parameterHasAnnotation(WriedRenewStatement.class);
    }

    @Override
    public WrapperConfiguration createConfiguration(MethodWrapper wrapper) throws WrapperMethodNotQualifiedException {
        ParameterWrapper parameter = wrapper.getSingleParameterByAnnotation(WriedRenewStatement.class);
        WrapperConfiguration configuration = new PowerWrapperConfiguration();
        configuration.setMw(wrapper);
        WriedRenewStatement annotation = parameter.getAnnotation(WriedRenewStatement.class);
        AnnotationUtils.loadAttribute(annotation, configuration, true).init();

        //替换掉 exclusion
        Set<String> exclusions = configuration.getAnnotationAualified(configuration.getJavaFieldNames());
        exclusions.clear();
        String[] condition = annotation.condition();
        if (condition.length == 0) {
            Collection<PrimaryKey> primaryKeys = configuration.getTableMetadata().getPrimaryKeys();
            if (primaryKeys.isEmpty()){
                throw new SQLSException("更新的查询条件, 在没有主键的情况下, 必须指名");
            }
            for (PrimaryKey primaryKey : primaryKeys) {
                exclusions.add(primaryKey.getName());
            }
        }else {
            exclusions.addAll(Arrays.asList(condition));
        }
        return configuration;
    }

    @Override
    public Object handler(Object arg, WrapperConfiguration configuration) {
        SqlOutStatement statement = SqlWriter.update(configuration.getTableName());
        BoundStatement boundStatement = handlerObject(arg, statement, configuration);
        if (arg == null){
            return boundStatement;
        }

        PowerWrapperConfiguration powerWrapperConfiguration = (PowerWrapperConfiguration) configuration;

        //调整下顺序
        //然后处理update的set
        Map<String, Object> primordialArgs;
        if (arg instanceof Map){
            primordialArgs = (Map<String, Object>) arg;
        }else {
            primordialArgs = JSON.parseObject(arg.toString());
        }

        //这些是参数中必须存在的key
        String[] properties = powerWrapperConfiguration.getRequiredProperties();
        for (String property : properties) {
            if (!primordialArgs.containsKey(property)){
                throw new RuntimeException("参数缺少:" + property);
            }
        }

        //将 auto injection 里的值注册到 sql sequence map 中
        for (String ai : powerWrapperConfiguration.getAutoInjection()) {
            String obtain = GlobalMapping.parseAndObtain(ai, true);
            String[] parses = StringUtils.split(obtain, "=", 2, "auto Injection values not parse");
            statement.writeSet(parses[0].trim(), parses[1], false);
        }

        Set<String> setSet = getSets(powerWrapperConfiguration);
        //获取自动注入的属性
        AliasColumnConvertHandler handler = configuration.getHandler();
        for (String set : setSet) {

            //获取外部传的值
            Object value = primordialArgs.get(set);
            //如果值为空则跳过
            if (value == null) continue;
            String column = handler.convertColumn(set);
            String seqValue;
            if (!statement.exisOperation(column, OperationType.UPDATE)) {
                statement.writeSetVariable(column, "?");
            }
            boundStatement.addMV(new MappingVal(OperationType.UPDATE, value, column));
        }

        return boundStatement;
    }

    protected Set<String> getSets(PowerWrapperConfiguration configuration){
        Set<String> result = new HashSet<>();
        String[] fields = configuration.getSetFields();
        if (fields.length == 1 && "*".equals(fields[0])){
            result.addAll(configuration.getJavaFieldNames());
            return result;
        }
        //如果 auto 注入优先级小那么要处理的字段需要加上
        return Utils.addAll(result, fields);
    }
}
