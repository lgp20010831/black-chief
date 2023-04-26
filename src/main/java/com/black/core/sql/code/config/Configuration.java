package com.black.core.sql.code.config;


import com.black.core.query.ClassWrapper;
import com.black.core.query.MethodWrapper;
import com.black.core.sql.annotation.*;
import com.black.core.sql.code.*;
import com.black.core.sql.code.datasource.ConnectionManagement;
import com.black.core.sql.code.log.Log;
import com.black.core.sql.code.session.SQLMethodType;
import com.black.core.sql.code.session.SQLSignalSession;
import com.black.core.sql.code.util.SqlNameUtils;
import com.black.core.tools.BeanUtil;
import com.black.core.util.StreamUtils;
import com.black.core.util.StringUtils;
import com.black.table.ForeignKey;
import com.black.table.PrimaryKey;
import com.black.table.TableMetadata;
import com.black.table.TableUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.util.*;
import java.util.Set;

@Log4j2
@Getter @Setter
public class Configuration {

    private String[] returnColumns;

    private Set<String> sqlSequences;

    private final MethodWrapper methodWrapper;

    private ClassWrapper<?> cw;

    //表名
    protected String tableName;

    protected Boolean dynamicParseMapping;

    //结果集中是否过滤掉 空值的 value key
    protected boolean filterNullValueKey;

    //java 别名和数据库列名转换
    protected AliasColumnConvertHandler columnConvertHandler;

    //全局配置类
    protected GlobalSQLConfiguration globalSQLConfiguration;

    //拼接 sql
    protected String applySql;

    protected Set<String> setValues;

    //当前生效的 session
    protected SQLSignalSession session;

    //查询返回结果
    protected Class<? extends Map> mapType = HashMap.class;

    // sql 方法类型
    protected SQLMethodType methodType;

    public Configuration(GlobalSQLConfiguration configuration, MethodWrapper mw){
        this(null, configuration, mw);
    }

    public Configuration(String tableName,
                         GlobalSQLConfiguration globalSQLConfiguration,
                         MethodWrapper mw) {
        this.tableName = tableName;
        this.globalSQLConfiguration = globalSQLConfiguration;
        methodWrapper = mw;
    }

    public String getTableName() {
        if (!StringUtils.hasText(tableName)){
            MethodWrapper mw = getMethodWrapper();
            String parseName = mw.getName();
            Connection connection = ConnectionManagement.getConnection(getDatasourceAlias());
            Set<String> currentTables = TableUtils.getCurrentTables(getDatasourceAlias(), connection);
            String tableName = SqlNameUtils.parseNameOfTableName(this, parseName);
            if (currentTables.contains(tableName)){
                if (log.isInfoEnabled()) {
                    log.info("parse namespecs of method: [{}] to table name: [{}]", parseName, tableName);
                }
                setTableName(tableName);
            }
        }
        return tableName;
    }

    public Boolean getDynamicParseMapping() {
        if (dynamicParseMapping == null){
            Method method = getMethodWrapper().getMethod();
            dynamicParseMapping = AnnotationUtils.getAnnotation(method, DynamicParserMapping.class) != null;
        }
        return dynamicParseMapping;
    }

    public String convertColumn(String alias){
        return getColumnConvertHandler().convertColumn(alias);
    }

    public String convertAlias(String column){
        return getColumnConvertHandler().convertAlias(column);
    }

    public Log getLog(){
        return getGlobalSQLConfiguration().getLog();
    }

    public Collection<GlobalSQLRunningListener> getRunningListener(){
        return getGlobalSQLConfiguration().getApplicationContext().getSQLRunningListeners();
    }

    public SQLMethodType getMethodType() {
        if (methodType != null) return methodType;
        SQLMethodType methodType;
        if (methodWrapper.hasAnnotation(Query.class)){
            methodType = SQLMethodType.QUERY;
        }else if (methodWrapper.hasAnnotation(Renew.class)){
            methodType = SQLMethodType.UPDATE;
        }else if (methodWrapper.hasAnnotation(Insert.class)){
            methodType = SQLMethodType.INSERT;
        }else if (methodWrapper.hasAnnotation(Delete.class)){
            methodType = SQLMethodType.DELETE;
        }else {
            methodType = SqlNameUtils.parseName(getMethodWrapper());
        }
        return this.methodType = methodType;
    }

    public boolean containField(String name){
        return getTableMetadata().getColumnNameSet().contains(name);
    }

    public boolean isforeignKey(String name){
        TableMetadata tableMetadata = getTableMetadata();
        ForeignKey foreignKey = tableMetadata.firstForeignKey();
        return foreignKey != null && foreignKey.getName().equals(name);
    }

    public boolean isPrimary(String name){
        String primaryName = getPrimaryName();
        return (primaryName != null && primaryName.equals(name));
    }

    public String getPrimaryName(){
        TableMetadata metadata = getTableMetadata();
        PrimaryKey primaryKey = metadata.firstPrimaryKey();
        return primaryKey == null ? null : primaryKey.getName();
    }

    public Class<? extends Map> getMapType() {
        Query annotation = methodWrapper.getAnnotation(Query.class);
        if (annotation != null){
            Class<? extends Map> mapType = annotation.mapType();
            if (BeanUtil.isSolidClass(mapType)){
                return this.mapType = mapType;
            }
        }
        return mapType;
    }


    public Connection getConnection(){
        return ConnectionManagement.getConnection(getDatasourceAlias());
    }

    public TableMetadata getTableMetadata() {
       return ConnectionManagement.employConnection(getDatasourceAlias(), connection -> {
           return TableUtils.getTableMetadata(getTableName(), connection);
       });
    }

    //获取所有列名
    public Set<String> getColumnNames(){
        return getTableMetadata().getColumnNameSet();
    }

    //获取数据源别名
    public Set<String> getDataAliases(){
        AliasColumnConvertHandler columnConvertHandler = getColumnConvertHandler();
        return StreamUtils.mapSet(getColumnNames(), columnConvertHandler::convertAlias);
    }

    public String getDatasourceAlias(){
        return getGlobalSQLConfiguration().getDataSourceAlias();
    }

    //获取拼接的sql
    public String getApplySql() {
        return StringUtils.hasText(applySql) ? " " + applySql : "";
    }

    //获取类型转换处理器
    public AliasColumnConvertHandler getColumnConvertHandler() {
        if (columnConvertHandler == null){
            columnConvertHandler = getGlobalSQLConfiguration().getConvertHandler();
        }
        return columnConvertHandler;
    }

}
