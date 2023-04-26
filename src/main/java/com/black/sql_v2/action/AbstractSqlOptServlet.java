package com.black.sql_v2.action;


import com.black.core.util.Assert;
import com.black.sql_v2.Opt;
import com.black.sql_v2.Sql;
import com.black.sql_v2.SqlExecutor;
import com.black.sql_v2.TableNameOpt;
import com.black.sql_v2.javassist.Agentable;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public abstract class AbstractSqlOptServlet implements Opt, TableNameOpt {

    public static final String DEFAULT_ALIAS = Sql.DEFAULT_ALIAS;

    @Override
    public String getAlias(){
        return DEFAULT_ALIAS;
    }

    @Override
    public String getTableName(){
        return null;
    }
    
    protected SqlExecutor opt(){
        return Sql.opt(getAlias());
    }
    
    protected Object list0(Map<String, Object> json){
        return list0(json, getTableName());
    }

    @Agentable
    public Object list0(Map<String, Object> json, String tableName){
        Assert.notNull(tableName, "table name is null");
        return opt().query(tableName, json).jsonList();
    }

    protected Object queryById0(Serializable id){
        return queryById0(id, getTableName());
    }

    @Agentable
    public Object queryById0(Serializable id, String tableName){
        Assert.notNull(tableName, "table name is null");
        return opt().queryById(tableName, id).json();
    }


    protected Object single0(Map<String, Object> json){
        return single0(json, getTableName());
    }

    @Agentable
    public Object single0(Map<String, Object> json, String tableName){
        Assert.notNull(tableName, "table name is null");
        return opt().query(tableName, json).json();
    }

    protected void save0(Object json){
        save0(json, getTableName());
    }

    @Agentable
    public void save0(Object json, String tableName){
        Assert.notNull(tableName, "table name is null");
        opt().save(tableName, json);
    }

    protected <T> void saveBatch0(Collection<T> array){
        saveBatch0(array, getTableName());
    }

    @Agentable
    public  <T> void saveBatch0(Collection<T> array, String tableName){
        Assert.notNull(tableName, "table name is null");
        opt().saveBatch(tableName, array);
    }
    
    protected void delete0(Map<String, Object> json){
        delete0(json, getTableName());
    }

    @Agentable
    public void delete0(Map<String, Object> json, String tableName){
        Assert.notNull(tableName, "table name is null");
        opt().delete(tableName, json);
    }


    protected void deleteById0(Serializable id){
        deleteById0(id, getTableName());
    }

    @Agentable
    public void deleteById0(Serializable id, String tableName){
        Assert.notNull(tableName, "table name is null");
        opt().deleteById(tableName, id);
    }

    protected void deleteByIds0( List<Object> ids){
        deleteByIds0(ids, getTableName());
    }

    @Agentable
    public void deleteByIds0( List<Object> ids, String tableName){
        Assert.notNull(tableName, "table name is null");
        opt().deleteById(tableName, ids);
    }

}
