package com.black.core.sql.code.impl.sqlarg_impl;

import com.black.core.aop.servlet.ParameterWrapper;
import com.black.core.query.MethodWrapper;
import com.black.core.sql.annotation.BlendMap;
import com.black.core.sql.annotation.BlendString;
import com.black.core.sql.code.MapArgHandler;
import com.black.core.sql.code.config.Configuration;
import com.black.core.sql.code.config.SyntaxConfigurer;
import com.black.core.sql.code.config.SyntaxManager;
import com.black.core.sql.code.packet.ExecutePacket;
import com.black.core.sql.code.parse.BlendObject;
import com.black.core.sql.code.parse.CharParser;
import com.black.core.sql.code.sqls.BoundStatement;
import com.black.core.sql.code.sqls.MappingVal;
import com.black.core.sql.code.util.SQLUtils;
import com.black.core.sql.unc.OperationType;
import com.black.core.util.StringUtils;
import com.black.sql.SqlOutStatement;
import com.black.table.TableMetadata;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class BlendMapArguramentResolver extends AbstractCommonArguramentResolver{

    Map<String, List<BlendObject>> cache = new ConcurrentHashMap<>();

    @Override
    void doCommon(Configuration configuration, ExecutePacket ep, Object value, ParameterWrapper pw) {
        BlendMap annotation = pw.getAnnotation(BlendMap.class);
        TableMetadata tableMetadata = configuration.getTableMetadata();
        Set<String> columnNameSet = tableMetadata.getColumnNameSet();
        String blendValue = getBlendValue(annotation, configuration, ep);
        List<BlendObject> objects = cache.computeIfAbsent(blendValue, CharParser::parseBlend);
        Map<String, Object> copyMap = SQLUtils.copyMap((Map<String, Object>) value);
        for (BlendObject blendObject : objects) {
            String operator = blendObject.getName();
            List<String> attributes = blendObject.getAttributes();
            for (String attribute : attributes) {
                boolean and = !attribute.startsWith("!");
                if (!and){
                    attribute = StringUtils.removeIfStartWith(attribute, "!");
                }
                if (copyMap.containsKey(attribute)){
                    Object val = copyMap.remove(attribute);
                    String column = configuration.convertColumn(attribute);
                    if (columnNameSet.contains(column)){
                        processor(operator, column, val, ep, and);
                    }
                }
            }
        }
        SqlOutStatement statement = ep.getStatement();
        copyMap.forEach((alias, val) -> {
            String column = configuration.convertColumn(alias);
            if (columnNameSet.contains(column)){
                MapArgHandler.wiredParamInStatement(statement, column, val);
            }
        });
    }

    private String getBlendValue(BlendMap annotation, Configuration configuration, ExecutePacket ep){
        //按照优先级处理
        //一级：注解
        String result = annotation.value();

        //二级, 参数
        if (!StringUtils.hasText(result)){
            MethodWrapper mw = configuration.getMethodWrapper();
            ParameterWrapper parameter = mw.getSingleParameterByAnnotation(BlendString.class);
            if (parameter != null){
                Object[] args = ep.getArgs();
                Object arg = args[parameter.getIndex()];
                if (arg != null){
                    result = arg.toString();
                }
            }
        }

        // 三级, 参数 syntax
        if (!StringUtils.hasText(result)){
            String syntax = SyntaxManager.callSyntax(configuration, ep, SyntaxConfigurer::getBlendSyntax);
            if (syntax != null){
                result = syntax;
            }
        }

        //四级 local syntax
        if (!StringUtils.hasText(result)){
            String localSyntax = SyntaxManager.localSyntax(configuration, SyntaxConfigurer::getBlendSyntax);
            if (localSyntax != null){
                result = localSyntax;
            }
        }
        return result;
    }

    private void processor(String operator, String columnName, Object val, ExecutePacket ep, boolean and){
        SqlOutStatement statement = ep.getStatement();
        BoundStatement nhStatement = ep.getNhStatement();
        if (val == null){
            statement.writeAftSeq(SqlOutStatement.getColumnOfStatement(statement, columnName) + " is null", and);
            return;
        }
        String string = SQLUtils.getString(val);
        switch (operator){
            case "eq":
                if (and){
                    statement.and();
                }else {
                    statement.or();
                }
                statement.writeEq(columnName, string, false);
                break;
            case "like":
            case "LIKE":
                if (and){
                    statement.and();
                }else {
                    statement.or();
                }
                statement.writeLike(columnName, string);
                break;
            case ">":
                statement.writeAftSeq(SqlOutStatement.getColumnOfStatement(statement, columnName) + " > " + string, and);
                break;
            case "<":
                statement.writeAftSeq(SqlOutStatement.getColumnOfStatement(statement, columnName) + " < " + string, and);
                break;
            case "<>":
                statement.writeAftSeq(SqlOutStatement.getColumnOfStatement(statement, columnName) + " <> " + string, and);
                break;
            case "<=":
                statement.writeAftSeq(SqlOutStatement.getColumnOfStatement(statement, columnName) + " <= " + string, and);
                break;
            case ">=":
                statement.writeAftSeq(SqlOutStatement.getColumnOfStatement(statement, columnName) + " >= " + string, and);
                break;
            case "in":
            case "IN":
                List<Object> list = SQLUtils.wrapList(val);
                if (list.isEmpty()){
                    statement.writeAftSeq(SqlOutStatement.getColumnOfStatement(statement, columnName) + " is null", and);
                }else {
                    String[] array = SQLUtils.createW(list.size());
                    statement.writeIn(columnName, false, array);
                    nhStatement.addMV(new MappingVal(OperationType.SELECT, list, columnName));
                }
                break;
            default:
                throw new IllegalStateException("无法支持的操作符: " + operator);
        }
    }

    @Override
    public boolean support(Configuration configuration, ParameterWrapper pw) {
        return pw.hasAnnotation(BlendMap.class) && Map.class.isAssignableFrom(pw.getType());
    }

    @Override
    protected String getColumnName(ParameterWrapper pw, Configuration configuration) {
        return null;
    }
}
