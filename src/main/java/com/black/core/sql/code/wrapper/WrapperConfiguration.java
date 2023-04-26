package com.black.core.sql.code.wrapper;


import com.black.core.json.ReflexUtils;
import com.black.core.query.ClassWrapper;
import com.black.core.query.MethodWrapper;
import com.black.core.sql.annotation.DataSourceAlias;
import com.black.core.sql.annotation.TableName;
import com.black.core.sql.code.AliasColumnConvertHandler;
import com.black.core.sql.code.BaseSQLApplicationContext;
import com.black.core.sql.code.datasource.ConnectionManagement;
import com.black.core.util.Assert;
import com.black.core.util.StringUtils;
import com.black.table.TableMetadata;
import com.black.table.TableUtils;
import lombok.Getter;
import lombok.Setter;

import java.sql.Connection;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
public class WrapperConfiguration {
    //作为 or 操作符的字段
    String[] orConditionFields;
    //like 操作符的字段
    String[] likeConditionFields;
    //条件 map, key:value 写法
    String[] conditionMap;
    //指定正序排序字段
    String[] orderByAsc;
    //指定倒序排序字段
    String[] orderByDesc;
    //有资格作为条件的字段
    String[] qualificationAsCondition;
    //无效的条件字段
    String[] invalidCondition;
    //整合了 qualificationAsCondition 和 invalidCondition 最终得出可以被当成字段的集合
    Set<String> annotationAualified;
    //表数据源
    TableMetadata tableMetadata;
    //表名
    String tableName;
    //数据源别名
    String dataSourceAlias;
    //如果参数是 array 操作符
    String ifArrayOperator;
    //如果参数是 array,那么对应的字段
    String ifArrayPointFieldName;
    //以 java 格式的数据库字段命名
    Set<String> javaFieldNames;
    //以 java 格式作为 or 操作符的字段
    Set<String> orFieldNames;
    //以 java 格式作为 like 操作符的字段
    Set<String> likeFiledNames;
    //java 名称和数据库名称转换器类型
    Class<? extends AliasColumnConvertHandler> convertHandlerType;
    //转换器的实例
    AliasColumnConvertHandler handler;

    //最终操作的是 map, 所有有key存在,但是value为空, 才会符合该处理条件
    boolean ignoreNullValue;

    boolean dynamic;

    //最后拼接的 sql
    String applySql;

    MethodWrapper mw;

    public WrapperConfiguration(){}

    public WrapperConfiguration(String tableName){
        this.tableName = tableName;
        init();
    }

    public WrapperConfiguration addOrFieldName(String fieldName){
        Set<String> orFieldNames = getOrFieldNames();
        orFieldNames.add(fieldName);
        return this;
    }

    public WrapperConfiguration addLikeFieldName(String fieldName){
        Set<String> filedNames = getlikeFiledNames();
        filedNames.add(fieldName);
        return this;
    }

    public WrapperConfiguration init(){
        if (!StringUtils.hasText(tableName)){
            if (mw != null){
                if (mw.hasAnnotation(TableName.class)) {
                    tableName = mw.getAnnotation(TableName.class).value();
                }else {
                    ClassWrapper<?> dcw;
                    if ((dcw = mw.getDeclaringClassWrapper()).hasAnnotation(TableName.class)) {
                        tableName = dcw.getAnnotation(TableName.class).value();
                    }
                }
            }

            if (!StringUtils.hasText(tableName))
                throw new IllegalArgumentException("wrapper 必须指定table name");
        }
        Connection connection = null;
        String datasourceAlias = null;
        if (!StringUtils.hasText(dataSourceAlias)){
            if (mw != null){
                if (mw.hasAnnotation(DataSourceAlias.class)) {
                    datasourceAlias = mw.getAnnotation(DataSourceAlias.class).value();
                }else {
                    ClassWrapper<?> dcw;
                    if ((dcw = mw.getDeclaringClassWrapper()).hasAnnotation(DataSourceAlias.class)) {
                        datasourceAlias = dcw.getAnnotation(DataSourceAlias.class).value();
                    }
                }
            }
            datasourceAlias = datasourceAlias == null ? BaseSQLApplicationContext.DEFAULT_ALIAS : datasourceAlias;
            if(datasourceAlias != null){
                connection = ConnectionManagement.getConnection(datasourceAlias);
            }
        }else {
            connection = ConnectionManagement.getConnection(dataSourceAlias);
        }
        tableMetadata = TableUtils.getTableMetadata(tableName, connection);
        Assert.notNull(tableMetadata, "无法获取表结构信息: " + tableName);
        return this;
    }

    public Set<String> getOrFieldNames() {
        if (orFieldNames == null){
            orFieldNames = new HashSet<>();
            orFieldNames.addAll(Arrays.asList(orConditionFields));
        }
        return orFieldNames;
    }

    public Set<String> getlikeFiledNames() {
        if (likeFiledNames == null){
            likeFiledNames = new HashSet<>();
            likeFiledNames.addAll(Arrays.asList(likeConditionFields));
        }
        return likeFiledNames;
    }
    public AliasColumnConvertHandler getHandler(){
        if (handler == null){
            handler = ReflexUtils.instance(convertHandlerType);
        }
        return handler;
    }

    public Set<String> getJavaFieldNames() {
        if (javaFieldNames == null){
            javaFieldNames = new HashSet<>();
            for (String columnName : getTableMetadata().getColumnNameSet()) {
                javaFieldNames.add(getHandler().convertAlias(columnName));
            }
        }
        return javaFieldNames;
    }

    public Set<String> getAnnotationAualified(Set<String> fieldNames) {
        if(annotationAualified == null){
            annotationAualified = new HashSet<>();

            //将有资格的数据保存
            if (qualificationAsCondition != null) {
                if (qualificationAsCondition.length == 1 && "*".equals(qualificationAsCondition[0])){
                    annotationAualified.addAll(fieldNames);
                }else {
                    annotationAualified.addAll(Arrays.asList(qualificationAsCondition));
                }
            }
            if (invalidCondition != null){
                for (String icon : invalidCondition) {
                    annotationAualified.remove(icon);
                }
            }
        }
        return annotationAualified;
    }

}
