package com.black.sql_v2.utils;

import com.black.core.util.Assert;
import com.black.sql.SqlOutStatement;
import com.black.sql_v2.Environment;
import com.black.sql_v2.JDBCEnvironmentLocal;
import com.black.sql_v2.SqlExecutor;

import java.util.Map;

public class DictUtils {

    public static SqlOutStatement handlerTxt(String[] expressions, SqlOutStatement statement){
        Environment instance = JDBCEnvironmentLocal.getEnvironment();
        //创建新的语句还替换原有语句
        instance.getLog().debug("[SQL] -- dictSqlHandler create new statement replace old statement");
        SqlOutStatement newStatement = createNewStatement(statement);
        Map<String, Object> env = JDBCEnvironmentLocal.getEnv();
        //解析表达式
        for (String expression : expressions) {
            SqlV2Utils.parseExpression(expression, newStatement, env);
        }
        return newStatement;
    }

    public static SqlOutStatement createNewStatement(SqlOutStatement statement){
        SqlExecutor executor = JDBCEnvironmentLocal.getExecutor();
        Assert.notNull(executor, "can not find executor");
        Object[] params = JDBCEnvironmentLocal.getParams();
        Assert.notNull(executor, "can not find params");
        return executor.createSelectStatement(statement.getTableName(), true, params);
    }

}
