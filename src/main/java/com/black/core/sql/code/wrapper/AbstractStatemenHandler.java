package com.black.core.sql.code.wrapper;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.black.core.sql.code.AliasColumnConvertHandler;
import com.black.core.sql.code.mapping.GlobalMapping;
import com.black.core.sql.code.sqls.MappingVal;
import com.black.core.sql.code.sqls.BoundStatement;
import com.black.core.sql.code.util.SQLUtils;
import com.black.core.sql.unc.OperationType;
import com.black.sql.SqlOutStatement;
import com.black.table.PrimaryKey;
import org.springframework.util.StringUtils;

import java.util.*;

public abstract class AbstractStatemenHandler implements StatementPlusWrapperHandler{

    protected BoundStatement handlerObject(Object arg,
                                           SqlOutStatement statement,
                                           WrapperConfiguration configuration){

        BoundStatement newHappyStatement = new BoundStatement(statement);
        if (arg != null){

            //在拿到参数类型
            Class<?> argClass = arg.getClass();
            if (String.class.equals(argClass)){
                handlerString((String) arg, newHappyStatement, configuration);
            }else if (Map.class.isAssignableFrom(argClass)){
                handlerJson(new JSONObject((Map<String, Object>) arg), newHappyStatement, configuration);
            }else if (List.class.isAssignableFrom(argClass)){
                throw new RuntimeException("暂不支持参数是 array 类型");
            }else if (Object.class.equals(argClass)){
                throw new RuntimeException("参数必须明确, 不能用 Object 涵盖");
            }else {
                throw new RuntimeException("无法处理的参数类型:" + argClass);
            }
        }
        return newHappyStatement;
    }

    protected void handlerString(String strSource,
                                 BoundStatement sqlWrapper,
                                 WrapperConfiguration configuration){
        try {
            JSONObject parse = JSON.parseObject(strSource);
            handlerJson(parse, sqlWrapper, configuration);
        }catch (JSONException parseJsonError){
            throw new RuntimeException("数据源无法转成 json");
        }
    }

    protected void handlerJson(JSONObject json,
                               BoundStatement statement,
                               WrapperConfiguration configuration){

        json = processorJson(json, configuration);
        //处理 and 连接符
        Map<String, Object> dynamicArgs = handlerAynamicArgs(json, configuration);
        doHandler(statement, dynamicArgs, configuration, true);

        //处理 or 连接符
        Map<String, Object> orDynamicArgs = handlerOrAynamicArgs(json, configuration);
        doHandler(statement, orDynamicArgs, configuration, false);

        //处理排序
        handlerOrder(statement, configuration);

        //最后拼接sql
        handlerApplySql(statement, configuration);
    }

    //加个 json
    protected JSONObject processorJson(JSONObject json, WrapperConfiguration configuration){
        JSONObject newJson = new JSONObject(json);
        for (String cm : configuration.getConditionMap()) {
            cm = GlobalMapping.parseAndObtain(cm, true);
            String[] kv = cm.split("=");
            if(kv.length == 2){
                newJson.put(kv[0].trim(), kv[1]);
            }
        }
        return newJson;
    }

    //把需要 and 的参数提取出来
    protected Map<String, Object> handlerAynamicArgs(JSONObject json, WrapperConfiguration configuration){
        Set<String> orFieldNames = configuration.getOrFieldNames();
        Map<String, Object> andMap = new HashMap<>();
        for (String key : json.keySet()) {
            if (!orFieldNames.contains(key)){
                andMap.put(key, json.get(key));
            }
        }
        return andMap;
    }

    //把需要 or 的参数提取出来
    protected Map<String, Object> handlerOrAynamicArgs(JSONObject json, WrapperConfiguration configuration){
        Set<String> orFieldNames = configuration.getOrFieldNames();
        Map<String, Object> orMap = new HashMap<>();
        for (String key : json.keySet()) {
            if (orFieldNames.contains(key)){
                orMap.put(key, json.get(key));
            }
        }
        return orMap;
    }

    protected void handlerApplySql(BoundStatement happyStatement,
                                   WrapperConfiguration configuration){

        String applySql = configuration.getApplySql();
        happyStatement.getStatement().writeLastSql(applySql);
    }

    protected void handlerOrder(BoundStatement happyStatement,
                                WrapperConfiguration configuration){
        String[] orderByAsc = configuration.getOrderByAsc();
        happyStatement.getStatement().writeLastOrderByAsc(orderByAsc);
        String[] orderByDesc = configuration.getOrderByDesc();
        happyStatement.getStatement().writeLastOrderByDesc(orderByDesc);
    }

    protected void doHandler(BoundStatement statement,
                             Map<String, Object> source,
                             WrapperConfiguration configuration,
                             boolean and){
        boolean annotationIgnore = true;
        boolean ignore = true;

        Set<String> limits = configuration.getJavaFieldNames();
        Set<String> annotationExclusions = configuration.getAnnotationAualified(limits);
        for (String alias : source.keySet()) {
            if (limits.contains(alias)){
                ignore = false;
            }
            if (annotationExclusions.contains(alias)){
                annotationIgnore = false;
            }
        }
        if ((source.isEmpty() || ignore || annotationIgnore) && !configuration.isDynamic()){
            return;
        }

        if (and){
            statement.getStatement().writeAnd(i -> {
                source.forEach((k ,v) ->{
                    if (configuration.isDynamic() || annotationExclusions.contains(k)){
                        doFillValue(i, k, v, configuration, statement);
                    }
                });
            });

        }else {
            statement.getStatement().writeOr(i -> {
                source.forEach((k ,v) ->{
                    if (configuration.isDynamic() || annotationExclusions.contains(k)){
                        doFillValue(i, k, v, configuration, statement);
                    }
                });
            });
        }

    }

    protected void doFillValue(SqlOutStatement statement, String name,
                               Object value, WrapperConfiguration configuration, BoundStatement happyStatement){
        //name 必须存在于
        Set<String> limits = configuration.getJavaFieldNames();

        //要填充的属性, 必须存在于实体类字段name中
        if (!configuration.isDynamic() && !limits.contains(name)){
            return;
        }
        Set<String> likeSet = configuration.getlikeFiledNames();
        AliasColumnConvertHandler handler = configuration.getHandler();
        boolean ignoreNullValue = configuration.isIgnoreNullValue();
        String column = handler.convertColumn(name);
        if (likeSet.contains(name)) {
            if (value != null ){
                statement.writeLike(column, value.toString());
            }

        }else {
            if (value != null || !ignoreNullValue){
                if(value instanceof Collection){
                    Collection<?> vlist = (Collection<?>) value;
                    String[] ws = SQLUtils.createW(vlist.size());
                    statement.writeIn(column, false, ws);
                }else {
                    statement.writeEq(column, "?", false);
                }
                happyStatement.addMV(new MappingVal(OperationType.SELECT, value, column));
            }
        }
    }

    protected String ifNullIsAndId(String name, WrapperConfiguration configuration){
        if (!StringUtils.hasText(name)) {
            PrimaryKey primaryKey = configuration.getTableMetadata().firstPrimaryKey();
            return primaryKey == null ? null : primaryKey.getName();
        }
        return name;
    }
}
