package com.black.sql_v2.result;

import com.black.core.sql.code.AliasColumnConvertHandler;
import com.black.sql.QueryResultSetParser;
import com.black.sql.SqlOutStatement;
import com.black.sql_v2.JDBCEnvironmentLocal;
import com.black.sql_v2.SqlExecutor;
import com.black.sql_v2.SqlV2Pack;
import com.black.sql_v2.utils.SqlV2Utils;

import java.sql.ResultSet;

public class ResultSetParserCreator {


    public static QueryResultSetParser create(SqlOutStatement statement, ResultSet resultSet, SqlExecutor executor){
        AliasColumnConvertHandler convertHandler = executor.getEnvironment().getConvertHandler();
        if (SqlV2Utils.isSelectStatement(statement)){
            SqlV2Pack pack = JDBCEnvironmentLocal.getPack();
            SqlV2ResultSetParser parser = new SqlV2ResultSetParser(resultSet, pack);
            parser.setStatement(statement);
            parser.setConvertHandler(convertHandler);
            return parser;
        }else {
            QueryResultSetParser parser = new QueryResultSetParser(resultSet);
            parser.setConvertHandler(convertHandler);
            return parser;
        }
    }

}
