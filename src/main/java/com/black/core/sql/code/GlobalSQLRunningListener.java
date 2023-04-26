package com.black.core.sql.code;

import com.black.core.chain.Order;
import com.black.core.query.MethodWrapper;
import com.black.core.sql.SQLSException;
import com.black.core.sql.code.config.Configuration;
import com.black.core.sql.code.config.GlobalSQLConfiguration;
import com.black.core.sql.code.packet.ExecutePacket;
import com.black.core.sql.code.pattern.ExecuteBody;
import com.black.core.sql.code.sqls.SqlValueGroup;
import com.black.core.sql.unc.SqlValue;
import com.black.sql.SqlOutStatement;

import java.sql.Connection;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface GlobalSQLRunningListener extends Order {

    //添加操作时触发
    //如果 pipeline 模式不影响
    default List<Map<String, Object>> handlerData(List<Map<String, Object>> mapData){
        return mapData;
    }

    default SqlOutStatement processorStatement(SqlOutStatement outStatement, Configuration configuration, ExecutePacket arg){
        return outStatement;
    }

    //添加时触发
    //如果 pipeline 模式不影响
    default void postSqlValue(Configuration configuration, SqlValue sqlValue){}

    //无论什么模式都会在真正逻辑执行前执行
    default void beforeInvoke(GlobalSQLConfiguration globalSQLConfiguration, MethodWrapper mw, Object[] args){}

    //无论什么模式都会在真正逻辑执行之前执行, 前提是过程中没有发生异常
    default Object afterInvoke(GlobalSQLConfiguration globalSQLConfiguration, MethodWrapper mw, Object result){
        return result;
    }

    //无论什么模式, 当执行发生异常以后, 执行该方法, 该方法运行抛出异常, 抛出的异常将会替换调之前的异常
    default void throwableInvoke(GlobalSQLConfiguration globalSQLConfiguration, MethodWrapper mw, Throwable ex) throws Throwable{

    }

    //只会在 pipeline 模式触发
    default void beforeProcessExecution(Configuration configuration, Object[] args) throws Throwable {}

    //只会在 pipeline 模式触发
    default void afterProcessExecution(Configuration configuration, Object[] args) throws Throwable {}

    //创建新的连接时触发
    //如果 pipeline 模式不影响
    default void createNewConnection(Connection connection, String alias){}

    //丢弃一个连接时候触发
    //如果 pipeline 模式不影响
    default void abandonConnection(Connection connection, String alias){}

    //处理结果集时, 每次完成一个 map 的组装时触发
    //如果 pipeline 模式不影响
    default void postMapResult(Configuration configuration, Map<String, Object> mapResult){}

    //处理结果集时最终触发
    //如果 pipeline 模式不影响
    default void postFinallyMapResult(Configuration configuration, Collection<Map<String, Object>> mapResult){}

    default String postRunScriptExecuteSql(GlobalSQLConfiguration configuration, String sql){return sql;}

    default String postRunScriptSelectSql(GlobalSQLConfiguration configuration, String sql){return sql;}

    //处理查询语句 sql
    //如果 pipeline 模式不影响
    default String postQuerySql(Configuration configuration, String sql, List<SqlValueGroup> valueGroupList){
        return sql;
    }

    //处理删除语句 sql
    //如果 pipeline 模式不影响
    default String postDeleteSql(Configuration configuration, String sql, List<SqlValueGroup> valueGroupList){
        return sql;
    }

    //处理更新语句 sql
    //如果 pipeline 模式不影响
    default String postUpdateSql(Configuration configuration, String sql, List<SqlValueGroup> valueGroupList){
        return sql;
    }

    //处理添加语句 sql
    //如果 pipeline 模式不影响
    default String postInsertSql(Configuration configuration, String sql, List<SqlValueGroup> valueGroupList){return sql;}


    //拦截这一组数据, false: 放行
    //如果 pipeline 模式不影响
    default boolean interceptBatchs(String sql, SqlValueGroup sqlValueGroup){
        return false;
    }

    //拦截这组sql value 成功后回调
    default void intercptCallback(String sql, SqlValueGroup sqlValueGroup, ExecuteBody executeBody){}

    default void afterProcessorOfBatch(){}

    //当一组数据被加入批次后, 回调, 可以再次设置事务回滚点
    //如果 pipeline 模式不影响
    default void afterAddBatch(StatementWrapper statementWrapper, Connection connection){}

    //返回 false 阻止回滚
    //如果 pipeline 模式不影响
    default boolean processorThrowable(SQLSException ex, Connection connection){
        return true;
    }

}
